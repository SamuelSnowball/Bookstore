#!/bin/bash

#################################################
# Associate Elastic IP with Running ECS Task   #
#################################################

# This script associates a static Elastic IP with the currently running ECS task
# Run this if the main deployment script fails at the EIP association step

set -e

AWS_REGION="eu-west-2"
CLUSTER_NAME="default"
SERVICE_NAME="bookstore-service"
EIP_TAG="bookstore-ecs-eip"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Associate Elastic IP with ECS Task${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Get or create Elastic IP
echo -e "${YELLOW}Step 1: Getting Elastic IP...${NC}"
ALLOCATION_ID=$(aws ec2 describe-addresses \
  --region ${AWS_REGION} \
  --filters "Name=tag:Name,Values=${EIP_TAG}" \
  --query "Addresses[0].AllocationId" \
  --output text 2>/dev/null || echo "None")

if [ "$ALLOCATION_ID" == "None" ] || [ -z "$ALLOCATION_ID" ]; then
    echo -e "${YELLOW}Allocating new Elastic IP...${NC}"
    ALLOCATION_ID=$(aws ec2 allocate-address \
      --region ${AWS_REGION} \
      --domain vpc \
      --query "AllocationId" \
      --output text)
    
    aws ec2 create-tags \
      --region ${AWS_REGION} \
      --resources ${ALLOCATION_ID} \
      --tags "Key=Name,Value=${EIP_TAG}"
    
    echo -e "${GREEN}✓ Elastic IP allocated: ${ALLOCATION_ID}${NC}"
else
    echo -e "${GREEN}✓ Using existing Elastic IP: ${ALLOCATION_ID}${NC}"
fi

ELASTIC_IP=$(aws ec2 describe-addresses \
  --region ${AWS_REGION} \
  --allocation-ids ${ALLOCATION_ID} \
  --query "Addresses[0].PublicIp" \
  --output text)

echo -e "${GREEN}Static IP: ${ELASTIC_IP}${NC}"
echo ""

# Get running task
echo -e "${YELLOW}Step 2: Finding running task...${NC}"
TASK_ARN=$(aws ecs list-tasks \
  --cluster ${CLUSTER_NAME} \
  --service-name ${SERVICE_NAME} \
  --region ${AWS_REGION} \
  --desired-status RUNNING \
  --query "taskArns[0]" \
  --output text)

if [ -z "$TASK_ARN" ] || [ "$TASK_ARN" == "None" ]; then
    echo -e "${RED}Error: No running tasks found for service ${SERVICE_NAME}${NC}"
    echo -e "${YELLOW}Make sure your ECS service is running first.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Found task: ${TASK_ARN}${NC}"
echo ""

# Get ENI
echo -e "${YELLOW}Step 3: Getting network interface...${NC}"
ENI_ID=$(aws ecs describe-tasks \
  --cluster ${CLUSTER_NAME} \
  --tasks ${TASK_ARN} \
  --region ${AWS_REGION} \
  --query "tasks[0].attachments[0].details[?name=='networkInterfaceId'].value" \
  --output text)

if [ -z "$ENI_ID" ]; then
    echo -e "${RED}Error: Could not find network interface for task${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Network Interface: ${ENI_ID}${NC}"
echo ""

# Disassociate if already associated
echo -e "${YELLOW}Step 4: Checking current associations...${NC}"
CURRENT_ASSOCIATION=$(aws ec2 describe-addresses \
  --region ${AWS_REGION} \
  --allocation-ids ${ALLOCATION_ID} \
  --query "Addresses[0].AssociationId" \
  --output text 2>/dev/null || echo "None")

if [ "$CURRENT_ASSOCIATION" != "None" ] && [ -n "$CURRENT_ASSOCIATION" ]; then
    echo -e "${YELLOW}Disassociating from previous resource...${NC}"
    aws ec2 disassociate-address \
      --region ${AWS_REGION} \
      --association-id ${CURRENT_ASSOCIATION} 2>/dev/null || true
    echo -e "${GREEN}✓ Disassociated${NC}"
fi
echo ""

# Associate
echo -e "${YELLOW}Step 5: Associating Elastic IP with task...${NC}"
ASSOCIATION_ID=$(aws ec2 associate-address \
  --region ${AWS_REGION} \
  --allocation-id ${ALLOCATION_ID} \
  --network-interface-id ${ENI_ID} \
  --allow-reassociation \
  --query "AssociationId" \
  --output text)

echo -e "${GREEN}✓ Association successful!${NC}"
echo -e "${GREEN}Association ID: ${ASSOCIATION_ID}${NC}"
echo ""

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Elastic IP Associated Successfully!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${GREEN}Static Public IP: ${ELASTIC_IP}${NC}"
echo ""
echo -e "Access your application at:"
echo -e "  UI:  ${GREEN}http://${ELASTIC_IP}${NC}"
echo -e "  API: ${GREEN}http://${ELASTIC_IP}:9000${NC}"
echo ""
echo -e "${YELLOW}Note: This IP will remain even after task restarts.${NC}"
echo -e "${YELLOW}Re-run this script after deployments to reassociate.${NC}"
echo ""
