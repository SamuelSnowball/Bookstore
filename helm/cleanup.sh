#!/bin/bash
set -e

echo "🗑️  Cleaning up Bookstore deployment from Kubernetes..."

# Uninstall Helm releases
echo "📦 Uninstalling Helm releases..."
helm uninstall bookstore-ui -n bookstore 2>/dev/null || echo "  bookstore-ui not found"
helm uninstall payment-service -n bookstore 2>/dev/null || echo "  payment-service not found"
helm uninstall order-service -n bookstore 2>/dev/null || echo "  order-service not found"
helm uninstall entity-service -n bookstore 2>/dev/null || echo "  entity-service not found"
helm uninstall auth-service -n bookstore 2>/dev/null || echo "  auth-service not found"

# Delete MySQL
echo "🗄️  Deleting MySQL..."
kubectl delete statefulset mysql -n bookstore 2>/dev/null || echo "  MySQL StatefulSet not found"
kubectl delete service mysql -n bookstore 2>/dev/null || echo "  MySQL Service not found"
kubectl delete pvc mysql-pvc -n bookstore 2>/dev/null || echo "  MySQL PVC not found"

# Delete secrets
echo "🔐 Deleting secrets..."
kubectl delete secret mysql-secret -n bookstore 2>/dev/null || echo "  mysql-secret not found"
kubectl delete secret stripe-secret -n bookstore 2>/dev/null || echo "  stripe-secret not found"

# Delete namespace
echo "🗂️  Deleting bookstore namespace..."
kubectl delete namespace bookstore 2>/dev/null || echo "  Namespace not found"

echo ""
echo "✅ Cleanup complete!"
echo ""
echo "To verify:"
echo "  kubectl get all -n bookstore"
echo ""
