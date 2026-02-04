#!/bin/bash

#####################
# ECR Deploy Script #
#####################

# Build and push all images to AWS ECR
# Usage: ./ecr_deploy.sh [service-name]
#   service-name: Optional. If provided, only deploys that service (auth|entity|order|payment|ui)
#   If no service specified, deploys all services

set -e  # Exit on error

# Configuration
AWS_REGION="eu-west-2"
AWS_ACCOUNT_ID="469860694479"
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Authenticate to ECR
authenticate_ecr() {
    log_info "Authenticating to AWS ECR..."
    aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
    if [ $? -eq 0 ]; then
        log_info "Successfully authenticated to ECR"
    else
        log_error "Failed to authenticate to ECR"
        exit 1
    fi
}

# Build, tag, and push a service
deploy_service() {
    local SERVICE_NAME=$1
    local DOCKERFILE_PATH=$2
    local BUILD_CONTEXT=$3
    local LOCAL_IMAGE="bookstore-${SERVICE_NAME}:latest"
    local ECR_IMAGE="${ECR_REGISTRY}/bookstore-${SERVICE_NAME}:latest"
    
    log_info "========================================"
    log_info "Deploying ${SERVICE_NAME} service"
    log_info "========================================"
    
    # Build the image
    log_info "Building ${LOCAL_IMAGE}..."
    if [ "$SERVICE_NAME" == "ui" ]; then
        docker build -t ${LOCAL_IMAGE} -f ${DOCKERFILE_PATH} ${BUILD_CONTEXT}
    else
        docker build -t ${LOCAL_IMAGE} -f ${DOCKERFILE_PATH} ${BUILD_CONTEXT}
    fi
    
    if [ $? -ne 0 ]; then
        log_error "Failed to build ${SERVICE_NAME}"
        return 1
    fi
    log_info "Successfully built ${LOCAL_IMAGE}"
    
    # Tag the image for ECR
    log_info "Tagging ${LOCAL_IMAGE} as ${ECR_IMAGE}..."
    docker tag ${LOCAL_IMAGE} ${ECR_IMAGE}
    
    if [ $? -ne 0 ]; then
        log_error "Failed to tag ${SERVICE_NAME}"
        return 1
    fi
    log_info "Successfully tagged ${ECR_IMAGE}"
    
    # Push to ECR
    log_info "Pushing ${ECR_IMAGE} to ECR..."
    docker push ${ECR_IMAGE}
    
    if [ $? -ne 0 ]; then
        log_error "Failed to push ${SERVICE_NAME}"
        return 1
    fi
    log_info "Successfully pushed ${ECR_IMAGE}"
    log_info ""
}

# Deploy all services
deploy_all() {
    log_info "Starting deployment of all services to ECR"
    log_info ""
    
    # Auth Service
    deploy_service "auth" "backend/auth_service/Dockerfile" "backend"
    
    # Entity Service
    deploy_service "entity" "backend/entity_service/Dockerfile" "backend"
    
    # Order Service
    deploy_service "order" "backend/order_service/Dockerfile" "backend"
    
    # Payment Service
    deploy_service "payment" "backend/payment_service/Dockerfile" "backend"
    
    # UI Service
    deploy_service "ui" "ui/Dockerfile" "ui"
    
    log_info "========================================"
    log_info "All services deployed successfully!"
    log_info "========================================"
}

# Main script
main() {
    # Check if AWS CLI is installed
    if ! command -v aws &> /dev/null; then
        log_error "AWS CLI is not installed. Please install it first."
        exit 1
    fi
    
    # Check if Docker is running
    if ! docker info &> /dev/null; then
        log_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    
    # Authenticate to ECR
    authenticate_ecr
    
    # Check if specific service is requested
    if [ -n "$1" ]; then
        case $1 in
            auth)
                deploy_service "auth" "backend/auth_service/Dockerfile" "backend"
                ;;
            entity)
                deploy_service "entity" "backend/entity_service/Dockerfile" "backend"
                ;;
            order)
                deploy_service "order" "backend/order_service/Dockerfile" "backend"
                ;;
            payment)
                deploy_service "payment" "backend/payment_service/Dockerfile" "backend"
                ;;
            ui)
                deploy_service "ui" "ui/Dockerfile" "ui"
                ;;
            *)
                log_error "Unknown service: $1"
                log_info "Valid services: auth, entity, order, payment, ui"
                exit 1
                ;;
        esac
    else
        # Deploy all services
        deploy_all
    fi
    
    log_info "Deployment complete!"
}

# Run main function
main "$@"
