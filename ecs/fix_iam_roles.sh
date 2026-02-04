#!/bin/bash

# Fix IAM roles for ECS task execution
# This creates the trust relationships and permissions needed for ECS

set -e

AWS_REGION="eu-west-2"
ACCOUNT_ID="469860694479"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}Fixing ECS IAM roles...${NC}"
echo ""

# 1. Create trust policy for ecsTaskExecutionRole
echo -e "${YELLOW}1. Configuring ecsTaskExecutionRole...${NC}"

TRUST_POLICY='{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}'

# Check if role exists, create or update
aws iam get-role --role-name ecsTaskExecutionRole --region ${AWS_REGION} 2>/dev/null || {
  echo "Creating ecsTaskExecutionRole..."
  aws iam create-role \
    --role-name ecsTaskExecutionRole \
    --assume-role-policy-document "$TRUST_POLICY"
}

# Update trust policy
aws iam update-assume-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-document "$TRUST_POLICY"

# Attach AWS managed policy for ECS task execution
aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy 2>/dev/null || true

# Add Secrets Manager permissions
SECRETS_POLICY="{
  \"Version\": \"2012-10-17\",
  \"Statement\": [
    {
      \"Effect\": \"Allow\",
      \"Action\": [
        \"secretsmanager:GetSecretValue\"
      ],
      \"Resource\": [
        \"arn:aws:secretsmanager:${AWS_REGION}:${ACCOUNT_ID}:secret:bookstore/*\"
      ]
    }
  ]
}"

aws iam put-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-name SecretsManagerAccess \
  --policy-document "$SECRETS_POLICY"

echo -e "${GREEN}✓ ecsTaskExecutionRole configured${NC}"
echo ""

# 2. Create trust policy for ecsTaskRole (for application runtime)
echo -e "${YELLOW}2. Configuring ecsTaskRole...${NC}"

# Check if role exists, create or update
aws iam get-role --role-name ecsTaskRole --region ${AWS_REGION} 2>/dev/null || {
  echo "Creating ecsTaskRole..."
  aws iam create-role \
    --role-name ecsTaskRole \
    --assume-role-policy-document "$TRUST_POLICY"
}

# Update trust policy
aws iam update-assume-role-policy \
  --role-name ecsTaskRole \
  --policy-document "$TRUST_POLICY"

echo -e "${GREEN}✓ ecsTaskRole configured${NC}"
echo ""

echo -e "${GREEN}All IAM roles configured successfully!${NC}"
echo ""
echo -e "${YELLOW}Note: It may take a few seconds for IAM changes to propagate.${NC}"
echo -e "${YELLOW}Wait 10-15 seconds before deploying to ECS.${NC}"
