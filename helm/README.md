# Bookstore Kubernetes Deployment

This directory contains Helm charts for deploying the Bookstore application to Kubernetes.

## Architecture

The application consists of microservices:

- **Auth Service** (port 9000) - User authentication and JWT tokens
- **Entity Service** (port 9001) - Manages books, authors, and addresses
- **Order Service** (port 9002) - Handles shopping cart and orders  
- **Payment Service** (port 9003) - Processes Stripe payments
- **Bookstore UI** (port 80) - React frontend application
- **MySQL** (port 3306) - Database for all services

## Quick Start - Docker Desktop Kubernetes

The easiest way to deploy to local Docker Desktop Kubernetes:

```bash
cd helm
chmod +x deploy-local.sh
./deploy-local.sh
```

This script will:
- Create the `bookstore` namespace
- Create necessary secrets (MySQL, Stripe)
- Build all Docker images locally
- Deploy MySQL with persistent storage
- Deploy all microservices using Helm
- Wait for services to be ready

### Access the Application

After deployment:

```bash
# Access via NodePort (automatically exposed on port 30080)
# Visit: http://localhost:30080

# OR use port-forward
kubectl port-forward -n bookstore svc/bookstore-ui 5173:80
# Visit: http://localhost:5173
```

### Cleanup

To remove the entire deployment:

```bash
chmod +x cleanup.sh
./cleanup.sh
```

## Manual Deployment

If you prefer manual control over the deployment:

## Prerequisites

- Kubernetes cluster (Docker Desktop, minikube, kind, or cloud provider)
- Helm 3.x installed
- kubectl configured to access your cluster
- Docker for building images

## Building Docker Images

### Build the UI
```bash
cd ui
docker build -t bookstore-ui:latest .
```

### Build the Backend Services
```bash
cd backend

# Auth Service
docker build -t bookstore-auth:latest -f auth_service/Dockerfile .

# Entity Service
docker build -t bookstore-entity:latest -f entity_service/Dockerfile .

# Order Service
docker build -t bookstore-order:latest -f order_service/Dockerfile .

# Payment Service
docker build -t bookstore-payment:latest -f payment_service/Dockerfile .
```

## Deploying to Kubernetes

### 1. Create Kubernetes Secrets

```bash
# MySQL credentials
kubectl create secret generic mysql-secret \
  --from-literal=username=root \
  --from-literal=password=yourpassword

# Stripe API key
kubectl create secret generic stripe-secret \
  --from-literal=secret-key=your_stripe_secret_key
```

### 2. Deploy MySQL (if not already deployed)

```bash
kubectl apply -f - <<EOF
apiVersion: v1
kind: Service
metadata:
  name: mysql
spec:
  ports:
  - port: 3306
  selector:
    app: mysql
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
spec:
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: password
        - name: MYSQL_DATABASE
          value: mydatabase
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
      volumes:
      - name: mysql-storage
        emptyDir: {}
EOF
```

### 3. Deploy Services using Helm

```bash
# Deploy Auth Service
helm install auth-service ./helm/auth-service -n bookstore

# Deploy Entity Service
helm install entity-service ./helm/entity-service -n bookstore

# Deploy Order Service
helm install order-service ./helm/order-service -n bookstore

# Deploy Payment Service
helm install payment-service ./helm/payment-service -n bookstore

# Deploy UI
helm install bookstore-ui ./helm/bookstore-ui -n bookstore
```

### 4. Verify Deployments

```bash
kubectl get pods -n bookstore
kubectl get services -n bookstore
```

### 5. Access the Application

For local development with port forwarding:
```bash
# Forward UI
kubectl port-forward -n bookstore service/bookstore-ui 5173:80

# Forward services (if testing API directly)
kubectl port-forward -n bookstore service/auth-service 9000:9000
kubectl port-forward -n bookstore service/entity-service 9001:9001
kubectl port-forward -n bookstore service/order-service 9002:9002
kubectl port-forward -n bookstore service/payment-service 9003:9003
```

Access the application at http://localhost:5173

## Updating Deployments

```bash
# Upgrade a chart after making changes
helm upgrade auth-service ./helm/auth-service -n bookstore
helm upgrade entity-service ./helm/entity-service -n bookstore
helm upgrade order-service ./helm/order-service -n bookstore
helm upgrade payment-service ./helm/payment-service -n bookstore
helm upgrade bookstore-ui ./helm/bookstore-ui -n bookstore
```

## Uninstalling

```bash
helm uninstall auth-service -n bookstore
helm uninstall entity-service -n bookstore
helm uninstall order-service -n bookstore
helm uninstall payment-service -n bookstore
helm uninstall bookstore-ui -n bookstore
kubectl delete namespace bookstore
```

## Configuration

Each service has two values files:

- `values.yaml` - Production defaults
- `values-local.yaml` - Local Docker Desktop settings (uses `pullPolicy: Never` for local images)

Customize configuration in these files:

- Resource limits/requests
- Replica count
- Environment variables
- Image repository and tag
- Service type and ports

Example:
```yaml
# helm/entity-service/values.yaml
replicaCount: 3  # Scale to 3 replicas
resources:
  requests:
    cpu: 1000m
    memory: 1Gi
```

## Monitoring

Add health check endpoints to your services (`/actuator/health`) for Kubernetes liveness and readiness probes (already configured in the Helm charts).

## Troubleshooting

```bash
# View logs
kubectl logs -f deployment/entity-service
kubectl logs -f deployment/bookstore-ui

# Describe pod for events
kubectl describe pod <pod-name>

# Get into a pod shell
kubectl exec -it <pod-name> -- /bin/sh
```
