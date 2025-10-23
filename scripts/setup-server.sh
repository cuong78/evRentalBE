#!/bin/bash

# =============================================================================
# EVRental Backend - Server Setup Script
# =============================================================================
# Mô tả: Script tự động setup server cho dự án EVRental Backend
# Cách dùng: 
#   1. SSH vào server
#   2. curl -O https://raw.githubusercontent.com/cuong78/evRentalBE/main/scripts/setup-server.sh
#   3. chmod +x setup-server.sh
#   4. ./setup-server.sh
# =============================================================================

set -e  # Dừng script nếu có lỗi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Header
clear
echo "========================================================================"
echo "     EVRental Backend - Server Setup Script"
echo "========================================================================"
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    print_error "Please run as root (use: sudo -i)"
    exit 1
fi

print_info "Starting server setup..."

# =============================================================================
# 1. Update system
# =============================================================================
print_info "Step 1/7: Updating system packages..."
apt update && apt upgrade -y
apt install -y curl git wget nano htop net-tools ufw
print_success "System updated successfully"

# =============================================================================
# 2. Install Docker
# =============================================================================
print_info "Step 2/7: Installing Docker..."

if command -v docker &> /dev/null; then
    print_warning "Docker is already installed. Skipping..."
    docker --version
else
    # Remove old versions
    apt remove -y docker docker-engine docker.io containerd runc 2>/dev/null || true
    
    # Install Docker
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    rm get-docker.sh
    
    # Start Docker
    systemctl enable docker
    systemctl start docker
    
    print_success "Docker installed successfully"
    docker --version
fi

# =============================================================================
# 3. Install Docker Compose
# =============================================================================
print_info "Step 3/7: Installing Docker Compose..."

if command -v docker compose version &> /dev/null; then
    print_warning "Docker Compose is already installed. Skipping..."
    docker compose version
else
    apt install -y docker-compose-plugin
    print_success "Docker Compose installed successfully"
    docker compose version
fi

# =============================================================================
# 4. Configure Firewall
# =============================================================================
print_info "Step 4/7: Configuring firewall..."

# Enable UFW if not enabled
if ! ufw status | grep -q "Status: active"; then
    print_warning "UFW is not active. Enabling..."
    
    # Allow SSH first (to avoid lockout)
    ufw allow 22/tcp
    
    # Allow application ports
    ufw allow 8080/tcp
    ufw allow 5432/tcp
    
    # Enable UFW
    echo "y" | ufw enable
    
    print_success "Firewall configured and enabled"
else
    print_warning "UFW is already active. Updating rules..."
    ufw allow 22/tcp
    ufw allow 8080/tcp
    ufw allow 5432/tcp
    ufw reload
    print_success "Firewall rules updated"
fi

ufw status

# =============================================================================
# 5. Create project directory
# =============================================================================
print_info "Step 5/7: Creating project directory..."

PROJECT_DIR="/opt/evrental"

if [ -d "$PROJECT_DIR" ]; then
    print_warning "Project directory already exists. Skipping..."
else
    mkdir -p $PROJECT_DIR
    print_success "Project directory created: $PROJECT_DIR"
fi

cd $PROJECT_DIR

# =============================================================================
# 6. Create .env file
# =============================================================================
print_info "Step 6/7: Creating .env file..."

if [ -f ".env" ]; then
    print_warning ".env file already exists. Creating backup..."
    mv .env .env.backup.$(date +%Y%m%d%H%M%S)
fi

# Prompt for configuration
echo ""
echo "========================================================================"
echo "     Configuration"
echo "========================================================================"
echo ""

read -p "Enter database password [default: ChangeMe123!]: " DB_PASSWORD
DB_PASSWORD=${DB_PASSWORD:-ChangeMe123!}

read -p "Enter JWT secret (min 32 chars) [default: auto-generate]: " JWT_SECRET
if [ -z "$JWT_SECRET" ]; then
    JWT_SECRET=$(openssl rand -base64 48)
fi

read -p "Enter your email for sending mails: " MAIL_USERNAME
read -sp "Enter your email app password: " MAIL_PASSWORD
echo ""

read -p "Enter your server IP or domain: " SERVER_HOST

# Create .env file
cat > .env << EOF
# ==================== DATABASE ====================
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/evrental
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD

POSTGRES_DB=evrental
POSTGRES_USER=postgres
POSTGRES_PASSWORD=$DB_PASSWORD

# ==================== JWT ====================
JWT_SECRET=$JWT_SECRET
JWT_EXPIRATION_MS=3600000
JWT_REFRESH_EXPIRATION_MS=86400000

# ==================== EMAIL ====================
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=$MAIL_USERNAME
SPRING_MAIL_PASSWORD=$MAIL_PASSWORD

# ==================== FRONTEND URLs ====================
FRONTEND_URL_BASE=http://$SERVER_HOST:3000
FRONTEND_URL_PAYMENT_RETURN=http://$SERVER_HOST:3000/payment/vnpay-return
FRONTEND_URL_EMAIL_VERIFICATION=http://$SERVER_HOST:3000/verify-email

# ==================== VNPAY ====================
PAYMENT_VNPAY_TMN_CODE=F9EZ1X6Z
PAYMENT_VNPAY_SECRET_KEY=XV3C5IN6HACS0NWN5OHG1IDLYOZWI1VX
PAYMENT_VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
PAYMENT_VNPAY_IP_ADDRESS=127.0.0.1
PAYMENT_VNPAY_WALLET_RETURN_URL=http://$SERVER_HOST:8080/api/v1/payment/wallet/return

# ==================== CLOUDINARY ====================
CLOUDINARY_CLOUD_NAME=duklfdbqf
CLOUDINARY_API_KEY=896897124846695
CLOUDINARY_API_SECRET=OPLZ7KB09RIZZXJlN7Im8w75w9k

# ==================== SERVER ====================
SERVER_PORT=8080
TZ=Asia/Ho_Chi_Minh
EOF

chmod 600 .env
print_success ".env file created and secured"

# =============================================================================
# 7. Setup SSH key for GitHub Actions
# =============================================================================
print_info "Step 7/7: Setting up SSH key for GitHub Actions..."

SSH_KEY_PATH="$HOME/.ssh/github_actions_key"

if [ -f "$SSH_KEY_PATH" ]; then
    print_warning "SSH key already exists. Skipping..."
else
    ssh-keygen -t rsa -b 4096 -f $SSH_KEY_PATH -N ""
    cat $SSH_KEY_PATH.pub >> $HOME/.ssh/authorized_keys
    chmod 600 $HOME/.ssh/authorized_keys
    print_success "SSH key created successfully"
fi

# =============================================================================
# Summary
# =============================================================================
echo ""
echo "========================================================================"
echo "     Setup Completed Successfully! 🎉"
echo "========================================================================"
echo ""
print_success "All steps completed!"
echo ""
echo "📋 Summary:"
echo "  - Docker version: $(docker --version)"
echo "  - Docker Compose version: $(docker compose version)"
echo "  - Project directory: $PROJECT_DIR"
echo "  - .env file: $PROJECT_DIR/.env"
echo "  - SSH key: $SSH_KEY_PATH"
echo ""
echo "========================================================================"
echo "     Next Steps"
echo "========================================================================"
echo ""
echo "1. Copy the PRIVATE SSH key for GitHub Secrets:"
echo "   ${BLUE}cat $SSH_KEY_PATH${NC}"
echo ""
echo "2. Add this key to GitHub Secrets as: ${GREEN}SERVER_SSH_KEY${NC}"
echo ""
echo "3. Add other secrets to GitHub:"
echo "   - DOCKER_HUB_USERNAME"
echo "   - DOCKER_HUB_ACCESS_TOKEN"
echo "   - SERVER_HOST: ${GREEN}$SERVER_HOST${NC}"
echo "   - SERVER_USER: ${GREEN}root${NC}"
echo "   - SERVER_PORT: ${GREEN}22${NC}"
echo ""
echo "4. Push code to GitHub main branch to trigger deployment"
echo ""
echo "5. Check health after deployment:"
echo "   ${BLUE}curl http://localhost:8080/actuator/health${NC}"
echo ""
echo "========================================================================"
echo ""

# Display SSH private key
print_info "SSH Private Key (copy this to GitHub Secrets):"
echo "========================================================================"
cat $SSH_KEY_PATH
echo "========================================================================"
echo ""

print_warning "⚠️  IMPORTANT: Keep this SSH key secure and don't share publicly!"
echo ""

# Create health check script
print_info "Creating health check script..."

cat > /opt/evrental/health-check.sh << 'EOF'
#!/bin/bash

echo "🏥 EVRental Backend Health Check"
echo "================================="

echo ""
echo "📦 Container Status:"
cd /opt/evrental
docker compose ps

echo ""
echo "🔍 Health Endpoint:"
curl -s http://localhost:8080/actuator/health | jq '.' 2>/dev/null || curl -s http://localhost:8080/actuator/health

echo ""
echo "💾 Disk Space:"
df -h / | tail -1

echo ""
echo "🧠 Memory Usage:"
free -h | grep Mem

echo ""
echo "📋 Recent App Logs:"
docker compose logs --tail=20 app
EOF

chmod +x /opt/evrental/health-check.sh
print_success "Health check script created: /opt/evrental/health-check.sh"

echo ""
print_success "Setup script completed! You can now proceed with GitHub deployment."
echo ""
