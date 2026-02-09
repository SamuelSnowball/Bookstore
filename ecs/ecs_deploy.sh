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

# Use the same subnets as the ALB
SUBNET_1="subnet-07768dd6f1605e3c4"  # eu-west-2a
SUBNET_2="subnet-09e465f1bff16d82f"  # eu-west-2b
echo -e "${GREEN}✓ Using ALB subnets: ${SUBNET_1}, ${SUBNET_2}${NC}"
echo ""

# Step 4: Create security group
echo -e "${YELLOW}Step 4: Setting up security group...${NC}"
SG_NAME="bookstore-ecs-sg"
SG_DESC="Security group for Bookstore ECS tasks"

# Get ALB security groups
echo -e "${YELLOW}Getting ALB security groups...${NC}"
ALB_ARN="arn:aws:elasticloadbalancing:eu-west-2:469860694479:loadbalancer/app/BookstoreALB/65a7ea5f02347f17"
ALB_SG=$(aws elbv2 describe-load-balancers \
  --load-balancer-arns ${ALB_ARN} \
  --region ${AWS_REGION} \
  --query "LoadBalancers[0].SecurityGroups[0]" \
  --output text)
echo -e "${GREEN}✓ ALB Security Group: ${ALB_SG}${NC}"

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
    
    # Allow HTTP traffic on port 80 from ALB
    aws ec2 authorize-security-group-ingress \
      --region ${AWS_REGION} \
      --group-id ${SG_ID} \
      --protocol tcp \
      --port 80 \
      --source-group ${ALB_SG}
    
    # Allow all traffic within the security group (for inter-container communication)
    aws ec2 authorize-security-group-ingress \
      --region ${AWS_REGION} \
      --group-id ${SG_ID} \
      --protocol -1 \
      --source-group ${SG_ID}
    
    echo -e "${GREEN}✓ Security group created: ${SG_ID}${NC}"
else
    echo -e "${GREEN}✓ Security group already exists: ${SG_ID}${NC}"
    
    # Check if ALB rule exists, add if not
    RULE_EXISTS=$(aws ec2 describe-security-group-rules \
      --region ${AWS_REGION} \
      --filters "Name=group-id,Values=${SG_ID}" \
      --query "SecurityGroupRules[?FromPort==\`80\` && ReferencedGroupInfo.GroupId=='${ALB_SG}'].SecurityGroupRuleId" \
      --output text)
    
    if [ -z "$RULE_EXISTS" ]; then
        echo -e "${YELLOW}Adding rule to allow traffic from ALB...${NC}"
        aws ec2 authorize-security-group-ingress \
          --region ${AWS_REGION} \
          --group-id ${SG_ID} \
          --protocol tcp \
          --port 80 \
          --source-group ${ALB_SG} 2>/dev/null || echo -e "${YELLOW}Rule may already exist${NC}"
        echo -e "${GREEN}✓ ALB ingress rule added${NC}"
    else
        echo -e "${GREEN}✓ ALB ingress rule already exists${NC}"
    fi
fi
echo ""

# Step 5: Create Target Group for Load Balancer
echo -e "${YELLOW}Step 5: Setting up target group for load balancer...${NC}"
TARGET_GROUP_NAME="bookstore-ui-tg"
ALB_ARN="arn:aws:elasticloadbalancing:eu-west-2:469860694479:loadbalancer/app/BookstoreALB/65a7ea5f02347f17"

# Check if target group exists
TG_ARN=$(aws elbv2 describe-target-groups \
  --region ${AWS_REGION} \
  --names ${TARGET_GROUP_NAME} \
  --query "TargetGroups[0].TargetGroupArn" \
  --output text 2>/dev/null || echo "None")

if [ "$TG_ARN" == "None" ] || [ -z "$TG_ARN" ]; then
    echo -e "${YELLOW}Creating target group: ${TARGET_GROUP_NAME}${NC}"
    TG_ARN=$(MSYS_NO_PATHCONV=1 aws elbv2 create-target-group \
      --region ${AWS_REGION} \
      --name ${TARGET_GROUP_NAME} \
      --protocol HTTP \
      --port 80 \
      --vpc-id ${VPC_ID} \
      --target-type ip \
      --health-check-enabled \
      --health-check-path / \
      --health-check-interval-seconds 30 \
      --health-check-timeout-seconds 5 \
      --healthy-threshold-count 2 \
      --unhealthy-threshold-count 3 \
      --query "TargetGroups[0].TargetGroupArn" \
      --output text)
    
    echo -e "${GREEN}✓ Target group created: ${TG_ARN}${NC}"
    
    # Create listener on port 80 if it doesn't exist
    LISTENER_ARN=$(aws elbv2 describe-listeners \
      --load-balancer-arn ${ALB_ARN} \
      --region ${AWS_REGION} \
      --query "Listeners[?Port==\`80\`].ListenerArn" \
      --output text 2>/dev/null || echo "")
    
    if [ -z "$LISTENER_ARN" ]; then
        echo -e "${YELLOW}Creating listener on port 80...${NC}"
        aws elbv2 create-listener \
          --region ${AWS_REGION} \
          --load-balancer-arn ${ALB_ARN} \
          --protocol HTTP \
          --port 80 \
          --default-actions Type=forward,TargetGroupArn=${TG_ARN}
        echo -e "${GREEN}✓ Listener created${NC}"
    else
        echo -e "${YELLOW}Updating existing listener to use new target group...${NC}"
        aws elbv2 modify-listener \
          --region ${AWS_REGION} \
          --listener-arn ${LISTENER_ARN} \
          --default-actions Type=forward,TargetGroupArn=${TG_ARN}
        echo -e "${GREEN}✓ Listener updated${NC}"
    fi
else
    echo -e "${GREEN}✓ Target group already exists: ${TG_ARN}${NC}"
fi
echo ""

# Step 6: Check if service exists
echo -e "${YELLOW}Step 6: Checking if service exists...${NC}"
SERVICE_EXISTS=$(aws ecs describe-services \
  --cluster ${CLUSTER_NAME} \
  --services ${SERVICE_NAME} \
  --region ${AWS_REGION} \
  --query "services[?status=='ACTIVE'].serviceName" \
  --output text 2>/dev/null || echo "")

if [ -n "$SERVICE_EXISTS" ]; then
    echo -e "${YELLOW}Service exists. Checking load balancer configuration...${NC}"
    
    # Check current load balancer port
    CURRENT_LB_PORT=$(aws ecs describe-services \
      --cluster ${CLUSTER_NAME} \
      --services ${SERVICE_NAME} \
      --region ${AWS_REGION} \
      --query "services[0].loadBalancers[0].containerPort" \
      --output text 2>/dev/null || echo "")
    
    if [ "$CURRENT_LB_PORT" == "5173" ] || [ -n "$CURRENT_LB_PORT" ] && [ "$CURRENT_LB_PORT" != "80" ]; then
        echo -e "${RED}Port mismatch detected (current: ${CURRENT_LB_PORT}, new: 80).${NC}"
        echo -e "${YELLOW}Deleting and recreating service with correct configuration...${NC}"
        
        # Scale down to 0
        aws ecs update-service \
          --cluster ${CLUSTER_NAME} \
          --service ${SERVICE_NAME} \
          --desired-count 0 \
          --region ${AWS_REGION} > /dev/null
        
        echo -e "${YELLOW}Waiting for tasks to stop...${NC}"
        sleep 10
        
        # Delete the service
        aws ecs delete-service \
          --cluster ${CLUSTER_NAME} \
          --service ${SERVICE_NAME} \
          --region ${AWS_REGION} > /dev/null
        
        echo -e "${YELLOW}Waiting for service deletion to complete...${NC}"
        while true; do
            SERVICE_STATUS=$(aws ecs describe-services \
              --cluster ${CLUSTER_NAME} \
              --services ${SERVICE_NAME} \
              --region ${AWS_REGION} \
              --query "services[0].status" \
              --output text 2>/dev/null || echo "MISSING")
            
            if [ "$SERVICE_STATUS" == "INACTIVE" ] || [ "$SERVICE_STATUS" == "MISSING" ]; then
                echo -e "${GREEN}✓ Service deleted${NC}"
                break
            fi
            echo -e "${YELLOW}Service status: ${SERVICE_STATUS}. Waiting...${NC}"
            sleep 5
        done
        
        # Wait for targets to fully deregister
        echo -e "${YELLOW}Waiting for targets to deregister from target group...${NC}"
        while true; do
            DRAINING_COUNT=$(aws elbv2 describe-target-health \
              --target-group-arn ${TG_ARN} \
              --region ${AWS_REGION} \
              --query "length(TargetHealthDescriptions[?TargetHealth.State=='draining'])" \
              --output text 2>/dev/null || echo "0")
            
            if [ "$DRAINING_COUNT" == "0" ]; then
                echo -e "${GREEN}✓ All targets deregistered${NC}"
                break
            fi
            echo -e "${YELLOW}${DRAINING_COUNT} target(s) still draining...${NC}"
            sleep 10
        done
        
        # Create new service with correct load balancer config
        echo -e "${YELLOW}Creating new service with load balancer on port 80...${NC}"
        aws ecs create-service \
          --cluster ${CLUSTER_NAME} \
          --service-name ${SERVICE_NAME} \
          --task-definition ${TASK_DEFINITION} \
          --desired-count 1 \
          --launch-type FARGATE \
          --network-configuration "awsvpcConfiguration={subnets=[${SUBNET_1},${SUBNET_2}],securityGroups=[${SG_ID}],assignPublicIp=ENABLED}" \
          --load-balancers "targetGroupArn=${TG_ARN},containerName=ui,containerPort=80" \
          --health-check-grace-period-seconds 60 \
          --region ${AWS_REGION}
        echo -e "${GREEN}✓ Service created with load balancer${NC}"
    else
        # Service configuration is correct, just update task definition
        aws ecs update-service \
          --cluster ${CLUSTER_NAME} \
          --service ${SERVICE_NAME} \
          --task-definition ${TASK_DEFINITION} \
          --desired-count 1 \
          --force-new-deployment \
          --region ${AWS_REGION}
        echo -e "${GREEN}✓ Service updated${NC}"
    fi
else
    echo -e "${YELLOW}Creating new service with load balancer...${NC}"
    aws ecs create-service \
      --cluster ${CLUSTER_NAME} \
      --service-name ${SERVICE_NAME} \
      --task-definition ${TASK_DEFINITION} \
      --desired-count 1 \
      --launch-type FARGATE \
      --network-configuration "awsvpcConfiguration={subnets=[${SUBNET_1},${SUBNET_2}],securityGroups=[${SG_ID}],assignPublicIp=ENABLED}" \
      --load-balancers "targetGroupArn=${TG_ARN},containerName=ui,containerPort=80" \
      --health-check-grace-period-seconds 60 \
      --region ${AWS_REGION}
    echo -e "${GREEN}✓ Service created with load balancer${NC}"
fi
echo ""

# Step 7: Wait for service to become stable
echo -e "${YELLOW}Step 7: Waiting for service to become stable...${NC}"
echo -e "${YELLOW}This may take a few minutes...${NC}"
aws ecs wait services-stable \
  --cluster ${CLUSTER_NAME} \
  --services ${SERVICE_NAME} \
  --region ${AWS_REGION}

echo -e "${GREEN}✓ Service is stable${NC}"
echo ""

# Step 8: Get public IP and Load Balancer DNS
echo -e "${YELLOW}Step 8: Getting task and load balancer information...${NC}"
ALB_DNS="BookstoreALB-734492841.eu-west-2.elb.amazonaws.com"

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
    echo -e "${GREEN}Load Balancer DNS: ${ALB_DNS}${NC}"
    echo -e "${GREEN}Public IP: ${PUBLIC_IP}${NC}"
    echo ""
    echo -e "Access your application at:"
    echo -e "  ${GREEN}http://${ALB_DNS}${NC} ${BLUE}(via Load Balancer - recommended)${NC}"
    echo -e "  ${GREEN}http://${PUBLIC_IP}${NC} ${YELLOW}(direct to task)${NC}"
    echo ""
    echo -e "API (direct):"
    echo -e "  ${GREEN}http://${PUBLIC_IP}:9000${NC}"
    echo ""
    echo -e "View logs at:"
    echo -e "  ${BLUE}https://eu-west-2.console.aws.amazon.com/cloudwatch/home?region=eu-west-2#logsV2:log-groups/log-group//ecs/bookstore-app${NC}"
    echo ""
else
    echo -e "${YELLOW}Task is starting up. Check status with:${NC}"
    echo -e "  aws ecs describe-services --cluster ${CLUSTER_NAME} --services ${SERVICE_NAME} --region ${AWS_REGION}"
fi
