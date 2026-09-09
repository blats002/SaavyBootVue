#!/usr/bin/env bash
# ==============================================================================
# SaavyBootVue - OCI Instance Bootstrap & Readiness Script
# ==============================================================================
# Prepares a brand new Oracle Cloud Infrastructure (OCI) Ubuntu instance:
#   1. Updates and upgrades system packages
#   2. Installs Docker, Docker Compose, Git, curl, jq, htop
#   3. Enables Docker daemon and grants non-root execution permissions ($USER)
#   4. Provisions 2GB Swap space (prevents OOM on 1GB AMD Micro shapes)
#   5. Configures OCI OS-level firewall (iptables & netfilter-persistent) for:
#      - Port 8080 (Application)
#      - Port 80   (HTTP)
#      - Port 443  (HTTPS)
#
# Usage (Run directly via SSH):
#   curl -sSL https://raw.githubusercontent.com/blats002/SaavyBootVue/uss-enterprise/scripts/setup-oci.sh | bash
#
# Or run locally from cloned repo:
#   chmod +x scripts/setup-oci.sh && ./scripts/setup-oci.sh
# ==============================================================================

set -e

# ANSI Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Determine target non-root user
TARGET_USER="${SUDO_USER:-$USER}"
if [ -z "$TARGET_USER" ] || [ "$TARGET_USER" = "root" ]; then
    if id -u ubuntu &>/dev/null; then
        TARGET_USER="ubuntu"
    fi
fi

echo -e "${CYAN}${BOLD}"
echo "=================================================================="
echo "    🛡️  OCI Ubuntu Instance Setup & Readiness Initializer         "
echo "=================================================================="
echo -e "${NC}"
log_info "Target user for Docker configuration: ${BOLD}${TARGET_USER}${NC}"

# ------------------------------------------------------------------------------
# 1. Update and Upgrade Operating System
# ------------------------------------------------------------------------------
log_info "Step 1/5: Updating package lists and upgrading system packages..."
export DEBIAN_FRONTEND=noninteractive
sudo apt-get update -y
sudo apt-get upgrade -y -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold"

# ------------------------------------------------------------------------------
# 2. Install Core Utilities & Tools
# ------------------------------------------------------------------------------
log_info "Step 2/5: Installing core tools (git, curl, jq, htop, iptables-persistent)..."
sudo apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    git \
    jq \
    htop \
    iptables-persistent \
    netfilter-persistent

# ------------------------------------------------------------------------------
# 3. Install and Configure Docker & Docker Compose
# ------------------------------------------------------------------------------
log_info "Step 3/5: Installing Docker Engine & Docker Compose plugin..."
if ! command -v docker &> /dev/null; then
    sudo apt-get install -y docker.io docker-compose-plugin
else
    # Ensure compose plugin is installed
    sudo apt-get install -y docker-compose-plugin || true
fi

# Enable and start Docker service
sudo systemctl enable docker
sudo systemctl start docker

# Add user to docker group
if [ -n "$TARGET_USER" ] && [ "$TARGET_USER" != "root" ]; then
    sudo usermod -aG docker "$TARGET_USER"
    log_success "Added user '${TARGET_USER}' to the 'docker' group."
fi

# ------------------------------------------------------------------------------
# 4. Provision Swap Space (Essential for OCI Free Tier)
# ------------------------------------------------------------------------------
log_info "Step 4/5: Checking RAM and configuring Swap space..."
TOTAL_MEM_MB=$(free -m | awk '/^Mem:/{print $2}')
TOTAL_SWAP_MB=$(free -m | awk '/^Swap:/{print $2}')

if [ -n "$TOTAL_MEM_MB" ] && [ "$TOTAL_MEM_MB" -lt 3000 ] && [ "$TOTAL_SWAP_MB" -lt 1024 ]; then
    if [ ! -f /swapfile ]; then
        log_info "Creating 2GB swapfile at /swapfile to prevent OOM errors during builds..."
        sudo fallocate -l 2G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
        sudo chmod 600 /swapfile
        sudo mkswap /swapfile
        sudo swapon /swapfile
        if ! grep -q "/swapfile" /etc/fstab; then
            echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
        fi
        log_success "2GB Swap partition created and activated."
    else
        sudo swapon /swapfile 2>/dev/null || true
        log_info "/swapfile already exists."
    fi
else
    log_info "Sufficient memory/swap detected (${TOTAL_MEM_MB}MB RAM, ${TOTAL_SWAP_MB}MB Swap)."
fi

# ------------------------------------------------------------------------------
# 5. Configure OCI OS Firewall Rules
# ------------------------------------------------------------------------------
log_info "Step 5/5: Configuring OCI OS firewall for ports 8080, 80, and 443..."

# Oracle Cloud Ubuntu images include default iptables REJECT rules.
# We must insert ACCEPT rules before the REJECT lines.
for PORT in 8080 80 443; do
    if ! sudo iptables -C INPUT -p tcp --dport "$PORT" -j ACCEPT 2>/dev/null; then
        sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport "$PORT" -j ACCEPT 2>/dev/null || \
        sudo iptables -I INPUT 1 -p tcp --dport "$PORT" -j ACCEPT
        log_info "Allowed inbound TCP port ${PORT} in iptables."
    fi
done

# Persist iptables rules
if command -v netfilter-persistent &>/dev/null; then
    sudo netfilter-persistent save 2>/dev/null || true
fi

# Also configure UFW if active
if command -v ufw &>/dev/null && sudo ufw status | grep -q "Status: active"; then
    sudo ufw allow 8080/tcp >/dev/null
    sudo ufw allow 80/tcp >/dev/null
    sudo ufw allow 443/tcp >/dev/null
    sudo ufw reload >/dev/null
    log_info "Updated UFW rules for ports 8080, 80, 443."
fi

PUBLIC_IP=$(curl -s --max-time 3 https://ifconfig.me || curl -s --max-time 3 https://icanhazip.com || echo "YOUR_OCI_PUBLIC_IP")

echo ""
echo -e "${GREEN}${BOLD}==================================================================${NC}"
echo -e "${GREEN}${BOLD}      🎉 OCI INSTANCE IS READY FOR SAAVYBOOTVUE DEPLOYMENT!       ${NC}"
echo -e "${GREEN}${BOLD}==================================================================${NC}"
echo ""
echo -e "  ${BOLD}Installed & Configured:${NC}"
echo -e "    ✔ Docker & Docker Compose Plugin (active & enabled)"
echo -e "    ✔ User '${TARGET_USER}' granted non-sudo Docker access"
echo -e "    ✔ 2GB Swapfile activated"
echo -e "    ✔ OS Firewall opened for ports 8080, 80, 443"
echo ""
echo -e "  ${BOLD}Next Step - Clone & Deploy:${NC}"
echo -e "    ${CYAN}git clone https://github.com/blats002/SaavyBootVue.git${NC}"
echo -e "    ${CYAN}cd SaavyBootVue${NC}"
echo -e "    ${CYAN}./scripts/deploy-oci.sh${NC}"
echo ""
echo -e "  ${YELLOW}${BOLD}Note:${NC} Remember to open port 8080 in your OCI VCN Ingress Rules."
echo -e "${GREEN}${BOLD}==================================================================${NC}"
echo ""
