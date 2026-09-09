#!/usr/bin/env bash
# ==============================================================================
# SaavyBootVue - Oracle Cloud Infrastructure (OCI) Automated Deployment Script
# ==============================================================================
# Supports: Ubuntu 20.04 / 22.04 / 24.04 (AMD x86 and Ampere A1 ARM64)
#
# Usage:
#   ./scripts/deploy-oci.sh             # Full auto-setup, build, & deploy
#   ./scripts/deploy-oci.sh --update    # Pull latest Git changes & redeploy
#   ./scripts/deploy-oci.sh --logs      # Stream container logs
#   ./scripts/deploy-oci.sh --status    # Check containers & services health
#   ./scripts/deploy-oci.sh --restart   # Restart services
#   ./scripts/deploy-oci.sh --stop      # Stop all services
#   ./scripts/deploy-oci.sh --reset-db  # Stop services and reset database
# ==============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Determine script & project root directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${PROJECT_ROOT}"

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

print_banner() {
    echo -e "${CYAN}${BOLD}"
    echo "=================================================================="
    echo "      🚀 SaavyBootVue - OCI Automated Deployment System           "
    echo "=================================================================="
    echo -e "${NC}"
}

# ------------------------------------------------------------------------------
# 1. System Prerequisite Check & Installation
# ------------------------------------------------------------------------------
check_and_install_prerequisites() {
    log_info "Checking system prerequisites..."

    local NEED_APT_UPDATE=false

    # Check curl
    if ! command -v curl &> /dev/null; then
        log_warn "curl is missing. Installing..."
        sudo apt-get update -y && NEED_APT_UPDATE=true
        sudo apt-get install -y curl
    fi

    # Check git
    if ! command -v git &> /dev/null; then
        log_warn "git is missing. Installing..."
        [ "$NEED_APT_UPDATE" = false ] && sudo apt-get update -y && NEED_APT_UPDATE=true
        sudo apt-get install -y git
    fi

    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_warn "Docker is not installed. Installing Docker..."
        [ "$NEED_APT_UPDATE" = false ] && sudo apt-get update -y && NEED_APT_UPDATE=true
        sudo apt-get install -y docker.io iptables-persistent
        sudo systemctl enable docker
        sudo systemctl start docker
        sudo usermod -aG docker "$USER"
        log_success "Docker installed successfully."
    fi

    # Check Docker Compose (Plugin or standalone)
    if ! docker compose version &> /dev/null && ! command -v docker-compose &> /dev/null; then
        log_warn "Docker Compose plugin is missing. Installing..."
        [ "$NEED_APT_UPDATE" = false ] && sudo apt-get update -y && NEED_APT_UPDATE=true
        sudo apt-get install -y docker-compose-plugin
        log_success "Docker Compose plugin installed."
    fi

    log_success "All prerequisites are satisfied."
}

# ------------------------------------------------------------------------------
# 2. Low Memory Check & Swap Space Provisioning
# ------------------------------------------------------------------------------
setup_swap_if_needed() {
    local TOTAL_MEM_MB
    TOTAL_MEM_MB=$(free -m | awk '/^Mem:/{print $2}')
    local TOTAL_SWAP_MB
    TOTAL_SWAP_MB=$(free -m | awk '/^Swap:/{print $2}')

    if [ -n "$TOTAL_MEM_MB" ] && [ "$TOTAL_MEM_MB" -lt 2048 ] && [ "$TOTAL_SWAP_MB" -lt 1024 ]; then
        log_warn "Detected low memory (${TOTAL_MEM_MB}MB RAM) with insufficient swap (${TOTAL_SWAP_MB}MB)."
        log_info "Creating 2GB swapfile to prevent Out-Of-Memory (OOM) errors during build..."

        if [ ! -f /swapfile ]; then
            sudo fallocate -l 2G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
            sudo chmod 600 /swapfile
            sudo mkswap /swapfile
            sudo swapon /swapfile
            echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
            log_success "2GB Swap successfully configured."
        else
            sudo swapon /swapfile 2>/dev/null || true
        fi
    fi
}

# ------------------------------------------------------------------------------
# 3. OCI OS Firewall Configuration (iptables & UFW)
# ------------------------------------------------------------------------------
configure_oci_firewall() {
    local PORT="${APP_PORT:-8080}"
    log_info "Verifying OS firewall rules for application port ${PORT}..."

    # Configure iptables if present (standard on OCI Ubuntu images)
    if command -v iptables &> /dev/null; then
        if ! sudo iptables -C INPUT -p tcp --dport "$PORT" -j ACCEPT 2>/dev/null; then
            log_info "Adding iptables rule to allow inbound TCP on port ${PORT}..."
            sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport "$PORT" -j ACCEPT 2>/dev/null || \
            sudo iptables -I INPUT 1 -p tcp --dport "$PORT" -j ACCEPT
            
            if command -v netfilter-persistent &> /dev/null; then
                sudo netfilter-persistent save 2>/dev/null || true
            fi
            log_success "iptables rule added for port ${PORT}."
        fi
    fi

    # Configure UFW if enabled
    if command -v ufw &> /dev/null && sudo ufw status | grep -q "Status: active"; then
        log_info "UFW is active. Allowing port ${PORT}/tcp..."
        sudo ufw allow "${PORT}/tcp" > /dev/null
        sudo ufw reload > /dev/null
        log_success "UFW rule added."
    fi
}

# ------------------------------------------------------------------------------
# 4. Environment & Secrets Management (.env)
# ------------------------------------------------------------------------------
setup_environment() {
    if [ ! -f .env ]; then
        log_info "No .env file found. Creating from .env.example..."
        if [ -f .env.example ]; then
            cp .env.example .env
        else
            cat <<EOF > .env
DB_USER=postgres
DB_PASSWORD=SaavySecurePass$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 12 ; echo '')!
DB_NAME=saavy_db
APP_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:8080,http://localhost:5173,http://127.0.0.1:5173
EOF
        fi

        # Generate a secure random DB password if default is detected
        local RANDOM_PASS
        RANDOM_PASS="Saavy_$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 16 ; echo '')"
        sed -i "s/YourSecurePassword123!/${RANDOM_PASS}/g" .env || true
        log_success ".env configuration initialized."
    fi

    # Export variables from .env
    set -a
    # shellcheck disable=SC1091
    source .env
    set +a
}

# ------------------------------------------------------------------------------
# 5. Build and Deploy Containers
# ------------------------------------------------------------------------------
deploy_containers() {
    log_info "Building and launching Docker containers..."
    
    # Run Docker Compose with BuildKit enabled
    DOCKER_BUILDKIT=1 docker compose up --build -d

    log_success "Containers started in detached mode."
}

# ------------------------------------------------------------------------------
# 6. Health Check Verification
# ------------------------------------------------------------------------------
wait_for_health() {
    local PORT="${APP_PORT:-8080}"
    log_info "Waiting for application to initialize on port ${PORT}..."

    local RETRIES=45
    local COUNT=0
    local HEALTHY=false

    while [ $COUNT -lt $RETRIES ]; do
        if curl -s -f "http://localhost:${PORT}/api/auth/me" &>/dev/null || curl -s "http://localhost:${PORT}" | grep -q "html" 2>/dev/null; then
            HEALTHY=true
            break
        fi
        sleep 2
        COUNT=$((COUNT + 1))
        echo -n "."
    done
    echo ""

    if [ "$HEALTHY" = true ]; then
        log_success "Application is online and healthy!"
    else
        log_warn "Application is still starting up or taking longer than expected."
        log_info "You can monitor live logs using: ./scripts/deploy-oci.sh --logs"
    fi
}

# ------------------------------------------------------------------------------
# 7. Print Deployment Summary
# ------------------------------------------------------------------------------
print_summary() {
    local PORT="${APP_PORT:-8080}"
    local PUBLIC_IP
    PUBLIC_IP=$(curl -s --max-time 3 https://ifconfig.me || curl -s --max-time 3 https://icanhazip.com || echo "YOUR_OCI_PUBLIC_IP")

    echo ""
    echo -e "${GREEN}${BOLD}==================================================================${NC}"
    echo -e "${GREEN}${BOLD}              🎉 DEPLOYMENT COMPLETE!                             ${NC}"
    echo -e "${GREEN}${BOLD}==================================================================${NC}"
    echo ""
    echo -e "  ${BOLD}Application URL:${NC}      ${CYAN}http://${PUBLIC_IP}:${PORT}${NC}"
    echo -e "  ${BOLD}Local URL:${NC}            ${CYAN}http://localhost:${PORT}${NC}"
    echo ""
    echo -e "  ${BOLD}Default Credentials:${NC}"
    echo -e "    • Admin:  ${YELLOW}admin${NC} / ${YELLOW}admin123${NC} (ROLE_ADMIN)"
    echo -e "    • User:   ${YELLOW}user${NC}  / ${YELLOW}user123${NC}  (ROLE_USER)"
    echo ""
    echo -e "  ${BOLD}Useful Commands:${NC}"
    echo -e "    • View logs:        ${CYAN}./scripts/deploy-oci.sh --logs${NC}"
    echo -e "    • Update app:       ${CYAN}./scripts/deploy-oci.sh --update${NC}"
    echo -e "    • Check status:     ${CYAN}./scripts/deploy-oci.sh --status${NC}"
    echo -e "    • Stop containers:  ${CYAN}./scripts/deploy-oci.sh --stop${NC}"
    echo ""
    echo -e "  ${YELLOW}${BOLD}Note:${NC} Make sure port ${PORT} is open in your OCI VCN Ingress Rules."
    echo -e "${GREEN}${BOLD}==================================================================${NC}"
    echo ""
}

# ------------------------------------------------------------------------------
# Main Dispatcher
# ------------------------------------------------------------------------------
main() {
    print_banner

    case "$1" in
        --update|update)
            log_info "Updating application..."
            if [ -d .git ]; then
                git pull
            fi
            setup_environment
            deploy_containers
            wait_for_health
            print_summary
            ;;
        --logs|logs)
            shift
            docker compose logs -f "$@"
            ;;
        --status|status)
            docker compose ps
            ;;
        --restart|restart)
            shift
            docker compose restart "$@"
            log_success "Services restarted."
            ;;
        --stop|stop|down)
            docker compose down
            log_success "Services stopped."
            ;;
        --reset-db|reset-db)
            log_warn "This will delete all database volumes and data."
            read -p "Are you sure? (y/N): " -r CONFIRM
            if [[ "$CONFIRM" =~ ^[Yy]$ ]]; then
                docker compose down -v
                log_success "Database and services completely reset."
            else
                log_info "Operation cancelled."
            fi
            ;;
        --help|help|-h)
            echo "Usage: ./scripts/deploy-oci.sh [OPTION]"
            echo ""
            echo "Options:"
            echo "  (none)        Full setup, build, and deployment"
            echo "  --update      Pull latest git updates and redeploy"
            echo "  --logs [svc]  Stream logs for all or specific service (app/postgres)"
            echo "  --status      Show container status and health"
            echo "  --restart     Restart container services"
            echo "  --stop        Stop and tear down container services"
            echo "  --reset-db    Stop services and delete database volume"
            echo "  --help        Show this help message"
            ;;
        *)
            check_and_install_prerequisites
            setup_swap_if_needed
            setup_environment
            configure_oci_firewall
            deploy_containers
            wait_for_health
            print_summary
            ;;
    esac
}

main "$@"
