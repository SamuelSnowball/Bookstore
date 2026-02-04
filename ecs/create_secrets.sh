#!/bin/bash

# Create AWS Secrets Manager secrets for the Bookstore application
# Usage: ./create_secrets.sh <mysql-password> <stripe-api-key>
#   mysql-password: Password for MySQL root user
#   stripe-api-key: Your Stripe API key (sk_test_... or sk_live_...)

set -e

AWS_REGION="eu-west-2"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Check if required arguments are provided
if [ $# -ne 2 ]; then
    echo -e "${RED}Error: Missing required arguments${NC}"
    echo ""
    echo "Usage: $0 <mysql-password> <stripe-api-key>"
    echo ""
    echo "Example:"
    echo "  $0 'my-secure-password' 'sk_test_51ABC...'"
    echo ""
    exit 1
fi

MYSQL_PASSWORD="$1"
STRIPE_API_KEY="$2"

echo -e "${GREEN}Creating AWS Secrets Manager secrets...${NC}"

# Create MySQL password secret
echo -e "${YELLOW}Creating MySQL password secret...${NC}"
aws secretsmanager create-secret \
  --name bookstore/mysql-password \
  --description "MySQL root password for Bookstore application" \
  --secret-string "${MYSQL_PASSWORD}" \
  --region ${AWS_REGION} 2>/dev/null || {
    echo "Secret already exists, updating..."
    aws secretsmanager update-secret \
      --secret-id bookstore/mysql-password \
      --secret-string "${MYSQL_PASSWORD}" \
      --region ${AWS_REGION}
  }

echo -e "${GREEN}✓ MySQL password secret created/updated${NC}"

# Create Stripe API Key secret
echo -e "${YELLOW}Creating Stripe API key secret...${NC}"

aws secretsmanager create-secret \
  --name bookstore/stripe-api-key \
  --description "Stripe API key for Bookstore payment processing" \
  --secret-string "${STRIPE_API_KEY}" \
  --region ${AWS_REGION} 2>/dev/null || {
    echo "Secret already exists, updating..."
    aws secretsmanager update-secret \
      --secret-id bookstore/stripe-api-key \
      --secret-string "${STRIPE_API_KEY}" \
      --region ${AWS_REGION}
  }

echo -e "${GREEN}✓ Stripe API key secret created/updated${NC}"
echo ""
echo -e "${GREEN}All secrets created successfully!${NC}"
echo ""
echo -e "${YELLOW}To view secrets:${NC}"
echo "aws secretsmanager list-secrets --region ${AWS_REGION}"
