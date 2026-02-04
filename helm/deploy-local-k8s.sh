#!/bin/bash
set -e

echo "🚀 Deploying Bookstore to local Docker Desktop Kubernetes..."

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl not found. Please install kubectl."
    exit 1
fi

# Check if helm is available
if ! command -v helm &> /dev/null; then
    echo "❌ helm not found. Please install Helm."
    exit 1
fi

# Check if docker-desktop context is available
CONTEXT=$(kubectl config current-context)
echo "📍 Current context: $CONTEXT"

# Create namespace
echo "📦 Creating bookstore namespace..."
kubectl create namespace bookstore --dry-run=client -o yaml | kubectl apply -f -

# Create secrets
echo "🔐 Creating secrets..."

# MySQL secret
kubectl create secret generic mysql-secret \
  --from-literal=username=root \
  --from-literal=password=root \
  --namespace=bookstore \
  --dry-run=client -o yaml | kubectl apply -f -

# Stripe secret (read from .env file if exists)
if [ -f "../.env" ]; then
    source ../.env
    kubectl create secret generic stripe-secret \
      --from-literal=api-key="${STRIPE_API_KEY}" \
      --namespace=bookstore \
      --dry-run=client -o yaml | kubectl apply -f -
else
    echo "⚠️  .env file not found. Creating placeholder Stripe secret..."
    kubectl create secret generic stripe-secret \
      --from-literal=api-key="sk_test_placeholder" \
      --namespace=bookstore \
      --dry-run=client -o yaml | kubectl apply -f -
fi

# Build Docker images with Docker Desktop's daemon
echo "🐳 Building Docker images..."
cd ..

echo "  Building auth-service..."
docker build -t bookstore-auth:latest -f backend/auth_service/Dockerfile backend

echo "  Building entity-service..."
docker build -t bookstore-entity:latest -f backend/entity_service/Dockerfile backend

echo "  Building order-service..."
docker build -t bookstore-order:latest -f backend/order_service/Dockerfile backend

echo "  Building payment-service..."
docker build -t bookstore-payment:latest -f backend/payment_service/Dockerfile backend

echo "  Building UI..."
docker build -t bookstore-ui:latest ui

cd helm

# Deploy MySQL
echo "📊 Deploying MySQL..."
kubectl apply -f - <<EOF
apiVersion: v1
kind: Service
metadata:
  name: mysql
  namespace: bookstore
spec:
  ports:
  - port: 3306
    targetPort: 3306
  selector:
    app: mysql
  clusterIP: None
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
  namespace: bookstore
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
  namespace: bookstore
spec:
  serviceName: mysql
  replicas: 1
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
        image: mysql:9.0
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
          name: mysql
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
        livenessProbe:
          exec:
            command:
            - mysqladmin
            - ping
            - -h
            - localhost
            - -u
            - root
            - -proot
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          exec:
            command:
            - mysqladmin
            - ping
            - -h
            - localhost
            - -u
            - root
            - -proot
          initialDelaySeconds: 10
          periodSeconds: 5
      volumes:
      - name: mysql-storage
        persistentVolumeClaim:
          claimName: mysql-pvc
EOF

# Wait for MySQL to be ready
echo "⏳ Waiting for MySQL to be ready..."
kubectl wait --for=condition=ready pod -l app=mysql --namespace=bookstore --timeout=120s

# Deploy microservices using Helm
echo "🎯 Deploying Auth Service..."
helm upgrade --install auth-service ./auth-service \
  --namespace=bookstore \
  -f auth-service/values-local.yaml

echo "🎯 Deploying Entity Service..."
helm upgrade --install entity-service ./entity-service \
  --namespace=bookstore \
  -f entity-service/values-local.yaml

echo "🎯 Deploying Order Service..."
helm upgrade --install order-service ./order-service \
  --namespace=bookstore \
  -f order-service/values-local.yaml

echo "🎯 Deploying Payment Service..."
helm upgrade --install payment-service ./payment-service \
  --namespace=bookstore \
  -f payment-service/values-local.yaml

echo "🎯 Deploying UI..."
helm upgrade --install bookstore-ui ./bookstore-ui \
  --namespace=bookstore \
  -f bookstore-ui/values-local.yaml

echo ""
echo "✅ Deployment complete!"
echo ""
echo "📋 Check status with:"
echo "   kubectl get pods -n bookstore"
echo ""
echo "🌐 Access the application:"
echo "   kubectl port-forward -n bookstore svc/bookstore-ui 5173:80"
echo "   Then visit: http://localhost:5173"
echo ""
echo "🔍 View logs:"
echo "   kubectl logs -n bookstore -l app=auth-service -f"
echo "   kubectl logs -n bookstore -l app=entity-service -f"
echo "   kubectl logs -n bookstore -l app=order-service -f"
echo "   kubectl logs -n bookstore -l app=payment-service -f"
echo "   kubectl logs -n bookstore -l app=bookstore-ui -f"
echo ""
