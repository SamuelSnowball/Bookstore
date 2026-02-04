#!/bin/bash

#####################
# ECS Deploy Script #
#####################

# Deploy Bookstore application to AWS ECS
# Usage: ./ecs_deploy.sh

set -e

AWS_REGION="eu-west-2"
CLUSTER_NAME="default"
SERVICE_NAME="bookstore-service"
TASK_DEFINITION="bookstore-app"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Bookstore ECS Deployment Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Step 1: Check if cluster exists, create if not
echo -e "${YELLOW}Step 1: Checking ECS cluster...${NC}"
CLUSTER_EXISTS=$(aws ecs describe-clusters \
  --clusters ${CLUSTER_NAME} \
  --region ${AWS_REGION} \
  --query "clusters[?status=='ACTIVE'].clusterName" \
  --output text 2>/dev/null || echo "")

if [ -z "$CLUSTER_EXISTS" ]; then
    echo -e "${YELLOW}Creating ECS cluster: ${CLUSTER_NAME}${NC}"
    aws ecs create-cluster \
      --cluster-name ${CLUSTER_NAME} \
      --region ${AWS_REGION} \
      --capacity-providers FARGATE \
      --default-capacity-provider-strategy capacityProvider=FARGATE,weight=1
    echo -e "${GREEN}✓ Cluster created${NC}"
else
    echo -e "${GREEN}✓ Cluster already exists${NC}"
fi
echo ""

# Step 2: Register task definition
echo -e "${YELLOW}Step 2: Registering task definition...${NC}"
TASK_DEF_FILE="ecs-task-definition.json"

if [ ! -f "$TASK_DEF_FILE" ]; then
    echo -e "${RED}Error: Task definition file not found at ${TASK_DEF_FILE}${NC}"
    exit 1
fi

TASK_DEF_REVISION=$(aws ecs register-task-definition \
  --cli-input-json file://${TASK_DEF_FILE} \
  --region ${AWS_REGION} \
  --query "taskDefinition.revision" \
  --output text)

echo -e "${GREEN}✓ Task definition registered: ${TASK_DEFINITION}:${TASK_DEF_REVISION}${NC}"
echo ""

# Step 3: Get VPC information
echo -e "${YELLOW}Step 3: Getting VPC information...${NC}"
VPC_ID=$(aws ec2 describe-vpcs \
  --region ${AWS_REGION} \
  --filters "Name=is-default,Values=true" \
  --query "Vpcs[0].VpcId" \
  --output text)

if [ "$VPC_ID" == "None" ] || [ -z "$VPC_ID" ]; then
    echo -e "${RED}Error: No default VPC found. Please create a VPC first.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ VPC ID: ${VPC_ID}${NC}"

# Get subnets
SUBNETS=$(aws ec2 describe-subnets \
  --region ${AWS_REGION} \
  --filters "Name=vpc-id,Values=${VPC_ID}" \
  --query "Subnets[*].SubnetId" \
  --output text)

SUBNET_ARRAY=($SUBNETS)
if [ ${#SUBNET_ARRAY[@]} -lt 2 ]; then
    echo -e "${RED}Error: Need at least 2 subnets for Fargate. Found: ${#SUBNET_ARRAY[@]}${NC}"
    exit 1
fi

SUBNET_1=${SUBNET_ARRAY[0]}
SUBNET_2=${SUBNET_ARRAY[1]}
echo -e "${GREEN}✓ Using subnets: ${SUBNET_1}, ${SUBNET_2}${NC}"
echo ""

# Step 4: Create security group
echo -e "${YELLOW}Step 4: Setting up security group...${NC}"
SG_NAME="bookstore-ecs-sg"
SG_DESC="Security group for Bookstore ECS tasks"

# Check if security group exists
SG_ID=$(aws ec2 describe-security-groups \
  --region ${AWS_REGION} \
  --filters "Name=group-name,Values=${SG_NAME}" "Name=vpc-id,Values=${VPC_ID}" \
  --query "SecurityGroups[0].GroupId" \
  --output text 2>/dev/null || echo "None")

if [ "$SG_ID" == "None" ] || [ -z "$SG_ID" ]; then
    echo -e "${YELLOW}Creating security group: ${SG_NAME}${NC}"
    SG_ID=$(aws ec2 create-security-group \
      --region ${AWS_REGION} \
      --group-name ${SG_NAME} \
      --description "${SG_DESC}" \
      --vpc-id ${VPC_ID} \
      --query "GroupId" \
      --output text)
    
    # Allow HTTP traffic on port 80 (UI)
    aws ec2 authorize-security-group-ingress \
      --region ${AWS_REGION} \
      --group-id ${SG_ID} \
      --protocol tcp \
      --port 80 \
      --cidr 0.0.0.0/0
    
    # Allow API traffic on port 9000 (Auth/Gateway)
    aws ec2 authorize-security-group-ingress \
      --region ${AWS_REGION} \
      --group-id ${SG_ID} \
      --protocol tcp \
      --port 9000 \
      --cidr 0.0.0.0/0
    
    # Allow all traffic within the security group (for inter-container communication)
    aws ec2 authorize-security-group-ingress \
      --region ${AWS_REGION} \
      --group-id ${SG_ID} \
      --protocol -1 \
      --source-group ${SG_ID}
    
    echo -e "${GREEN}✓ Security group created: ${SG_ID}${NC}"
else
    echo -e "${GREEN}✓ Security group already exists: ${SG_ID}${NC}"
fi
echo ""

# Step 5: Check if service exists
echo -e "${YELLOW}Step 5: Checking if service exists...${NC}"
SERVICE_EXISTS=$(aws ecs describe-services \
  --cluster ${CLUSTER_NAME} \
  --services ${SERVICE_NAME} \
  --region ${AWS_REGION} \
  --query "services[?status=='ACTIVE'].serviceName" \
  --output text 2>/dev/null || echo "")

if [ -n "$SERVICE_EXISTS" ]; then
    echo -e "${YELLOW}Service exists. Updating service...${NC}"
    aws ecs update-service \
      --cluster ${CLUSTER_NAME} \
      --service ${SERVICE_NAME} \
      --task-definition ${TASK_DEFINITION} \
      --desired-count 1 \
      --force-new-deployment \
      --region ${AWS_REGION}
    echo -e "${GREEN}✓ Service updated${NC}"
else
    echo -e "${YELLOW}Creating new service...${NC}"
    aws ecs create-service \
      --cluster ${CLUSTER_NAME} \
      --service-name ${SERVICE_NAME} \
      --task-definition ${TASK_DEFINITION} \
      --desired-count 1 \
      --launch-type FARGATE \
      --network-configuration "awsvpcConfiguration={subnets=[${SUBNET_1},${SUBNET_2}],securityGroups=[${SG_ID}],assignPublicIp=ENABLED}" \
      --region ${AWS_REGION}
    echo -e "${GREEN}✓ Service created${NC}"
fi
echo ""

# Step 6: Wait for service to become stable
echo -e "${YELLOW}Step 6: Waiting for service to become stable...${NC}"
echo -e "${YELLOW}This may take a few minutes...${NC}"
aws ecs wait services-stable \
  --cluster ${CLUSTER_NAME} \
  --services ${SERVICE_NAME} \
  --region ${AWS_REGION}

echo -e "${GREEN}✓ Service is stable${NC}"
echo ""

# Step 7: Get public IP
echo -e "${YELLOW}Step 7: Getting task information...${NC}"
TASK_ARN=$(aws ecs list-tasks \
  --cluster ${CLUSTER_NAME} \
  --service-name ${SERVICE_NAME} \
  --region ${AWS_REGION} \
  --query "taskArns[0]" \
  --output text)

if [ -n "$TASK_ARN" ] && [ "$TASK_ARN" != "None" ]; then
    ENI_ID=$(aws ecs describe-tasks \
      --cluster ${CLUSTER_NAME} \
      --tasks ${TASK_ARN} \
      --region ${AWS_REGION} \
      --query "tasks[0].attachments[0].details[?name=='networkInterfaceId'].value" \
      --output text)
    
    PUBLIC_IP=$(aws ec2 describe-network-interfaces \
      --region ${AWS_REGION} \
      --network-interface-ids ${ENI_ID} \
      --query "NetworkInterfaces[0].Association.PublicIp" \
      --output text)
    
    echo -e "${GREEN}✓ Task is running${NC}"
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${GREEN}Deployment Complete!${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    echo -e "${GREEN}Public IP: ${PUBLIC_IP}${NC}"
    echo ""
    echo -e "Access your application at:"
    echo -e "  UI:  ${GREEN}http://${PUBLIC_IP}${NC}"
    echo -e "  API: ${GREEN}http://${PUBLIC_IP}:9000${NC}"
    echo ""
    echo -e "View logs at:"
    echo -e "  ${BLUE}https://eu-west-2.console.aws.amazon.com/cloudwatch/home?region=eu-west-2#logsV2:log-groups/log-group//ecs/bookstore-app${NC}"
    echo ""
else
    echo -e "${YELLOW}Task is starting up. Check status with:${NC}"
    echo -e "  aws ecs describe-services --cluster ${CLUSTER_NAME} --services ${SERVICE_NAME} --region ${AWS_REGION}"
fi
