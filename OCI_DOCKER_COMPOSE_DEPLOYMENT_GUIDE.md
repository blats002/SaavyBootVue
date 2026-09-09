# Deploy This Project to Oracle Cloud Infrastructure Using Docker Compose

This guide explains how to deploy this project to an Oracle Cloud Infrastructure (OCI) Compute instance using Docker Compose.

The deployment will run:

- **PostgreSQL** in a Docker container
- **Spring Boot application** (with bundled Vue frontend) in a Docker container
- Both services connected through Docker Compose internal networking

---

## 1. Deployment Overview

The project includes a Docker Compose setup with two services:

| Service | Purpose | Exposed Port |
|---|---|---|
| `postgres` | PostgreSQL database | `5432` (optional) |
| `app` | Spring Boot application + Vue frontend | `8080` |

The application will be available at: `http://YOUR_OCI_PUBLIC_IP:8080`

---

## 2. Prerequisites

You will need:

- An Oracle Cloud Infrastructure account
- An OCI Compute instance (Ubuntu 22.04 LTS or newer recommended; AMD x86 or Ampere A1 ARM64)
- SSH access to the instance
- Docker & Docker Compose installed on the instance
- Port `8080` opened in OCI networking rules (Security Lists / NSG) and OS firewall

---

## 3. Create an OCI Compute Instance

In the OCI Console:

1. Go to **Compute** &rarr; **Instances**.
2. Click **Create instance**.
3. Enter a name for the instance (e.g., `saavy-app-server`).
4. Select an **Ubuntu** image (e.g., Ubuntu 22.04 or 24.04).
5. Select an instance shape:
   - **Ampere A1 (ARM64)**: `VM.Standard.A1.Flex` (Up to 4 OCPUs, 24 GB RAM free)
   - **AMD (x86)**: `VM.Standard.E2.1.Micro` (1 OCPU, 1 GB RAM)
6. Add your SSH public key.
7. Select or create a **Virtual Cloud Network (VCN)** and public subnet.
8. Ensure **Assign a public IPv4 address** is checked.
9. Click **Create**.

After the instance is created, copy the **Public IP Address** (`YOUR_OCI_PUBLIC_IP`).

---

## 4. Open Port 8080 in OCI (VCN Ingress Rules)

The application runs on port `8080`, so OCI must allow inbound traffic to that port.

In the OCI Console:

1. Go to **Networking** &rarr; **Virtual Cloud Networks**.
2. Open your VCN and click on the **Public Subnet** used by your Compute instance.
3. Click on the associated **Default Security List** (or your Network Security Group).
4. Under **Ingress Rules**, click **Add Ingress Rules**:
   - **Source Type**: `CIDR`
   - **Source CIDR**: `0.0.0.0/0` (or your trusted IP range)
   - **IP Protocol**: `TCP`
   - **Destination Port Range**: `8080`
   - **Description**: `Allow Spring Boot web traffic`
5. Click **Add Ingress Rules**.

---

## 5. Connect to the OCI Instance

From your local machine terminal, connect using SSH:

```bash
ssh ubuntu@YOUR_OCI_PUBLIC_IP
```

If your private key is in a specific location:

```bash
ssh -i /path/to/private-key ubuntu@YOUR_OCI_PUBLIC_IP
```

---

## 6. Update the Server

Update the package lists and upgrade existing packages:

```bash
sudo apt update && sudo apt upgrade -y
```

---

## 7. Install Docker

Install Docker and start the service:

```bash
sudo apt install -y docker.io
sudo systemctl enable docker
sudo systemctl start docker
```

Verify Docker is running:

```bash
docker --version
sudo systemctl status docker
```

---

## 8. Allow Your User to Run Docker Without Sudo

Add the `ubuntu` user to the `docker` group:

```bash
sudo usermod -aG docker $USER
newgrp docker
```

Verify you can run Docker commands without `sudo`:

```bash
docker ps
```

---

## 9. Install Docker Compose

Install the modern Docker Compose plugin:

```bash
sudo apt install -y docker-compose-plugin
```

Verify Docker Compose:

```bash
docker compose version
```

---

## 10. Install Git

Install Git if it is not already installed:

```bash
sudo apt install -y git
git --version
```

---

## 11. Clone the Project onto the OCI Instance

Clone your repository onto the server:

```bash
git clone YOUR_REPOSITORY_URL
cd SaavyBootVue
```

*(Replace `YOUR_REPOSITORY_URL` with your actual Git repository URL and `SaavyBootVue` with your cloned folder name).*

---

## 12. Confirm Required Deployment Files

Verify the required deployment files exist in the project root:

```bash
ls -l docker-compose.yml
ls -l server/DockerFile
```

---

## 13. Review the Docker Compose Services

The setup includes two services in `docker-compose.yml`:

- **`postgres`**: The database container (using PostgreSQL).
- **`app`**: The application container (builds the Spring Boot server and bundle).
- **`saavy_network`**: Internal bridge network linking `app` to `postgres` via the hostname `postgres`.

---

## 14. Update Production Database Credentials

Before starting the containers, update the default database password in `docker-compose.yml`:

```bash
nano docker-compose.yml
```

Update `POSTGRES_PASSWORD` and `SPRING_DATASOURCE_PASSWORD` with a strong password:

```yaml
services:
  postgres:
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: YourStrongPassword123!
      POSTGRES_DB: saavy_db

  app:
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/saavy_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: YourStrongPassword123!
```

> [!IMPORTANT]
> Make sure `POSTGRES_PASSWORD` and `SPRING_DATASOURCE_PASSWORD` are identical.

Save and exit in `nano`: `CTRL + O` &rarr; `Enter` &rarr; `CTRL + X`.

---

## 15. Optional: Secure PostgreSQL (Do Not Expose Port 5432 Publicly)

For production environments, PostgreSQL should not be exposed to the public internet.

In `docker-compose.yml`, comment out or remove the port mapping under `postgres`:

```yaml
    # ports:
    #   - "5432:5432"
```

The `app` container will still communicate with `postgres:5432` internally via Docker's bridge network.

---

## 16. Configure OCI OS Firewall (`iptables` / `ufw`)

> [!CAUTION]
> **OCI Specific Gotcha**: Oracle Cloud Ubuntu images include pre-configured `iptables` rules that reject inbound traffic on ports other than `22`. Even if port `8080` is open in the OCI VCN Security List, you must also allow it inside the operating system.

Run the following commands on the instance:

```bash
# Allow inbound TCP traffic on port 8080 in iptables
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
sudo apt install -y iptables-persistent
sudo netfilter-persistent save

# If UFW is enabled, also allow port 8080:
sudo ufw allow 8080/tcp
sudo ufw reload
```

---

## 17. Build and Start the Containers

From the project root directory, run:

```bash
docker compose up --build -d
```

This will:
1. Pull the PostgreSQL container image.
2. Build the application container using `server/DockerFile`.
3. Start the PostgreSQL database and wait for health checks.
4. Run Liquibase database migrations automatically.
5. Start the Spring Boot application on port `8080`.

---

## 18. Check Container Status

Check if both containers are running and healthy:

```bash
docker compose ps
```

Expected output:
```text
NAME                 IMAGE               COMMAND                  SERVICE      STATUS                    PORTS
saavy_boot_postgres  postgres:latest     "docker-entrypoint.s…"   postgres     Up (healthy)              5432/tcp
saavy_boot_app       saavybootvue-app    "java -jar app.jar -…"   app          Up                        0.0.0.0:8080->8080/tcp
```

---

## 19. View Application & Database Logs

To view streaming logs for all services:

```bash
docker compose logs -f
```

To view only application logs:

```bash
docker compose logs -f app
```

To view only database logs:

```bash
docker compose logs -f postgres
```

---

## 20. Test the Deployment from the Instance

Test that the application responds locally on the instance:

```bash
curl http://localhost:8080
```

You should receive the HTML response from the application.

---

## 21. Access the Application from Your Browser

Open your browser and navigate to:

```text
http://YOUR_OCI_PUBLIC_IP:8080
```

### Default Login Credentials:
- **Admin**: `admin` / `admin123` (`ROLE_ADMIN`)
- **User**: `user` / `user123` (`ROLE_USER`)

---

## 22. Troubleshooting

If the application is not reachable from the browser:

1. **Check if containers are running**:
   ```bash
   docker compose ps
   ```
2. **Inspect application logs for errors**:
   ```bash
   docker compose logs -f app
   ```
3. **Verify port 8080 is listening on the host**:
   ```bash
   sudo ss -tulpn | grep 8080
   ```
4. **Check OCI Security List / NSG**:
   Confirm that an Ingress Rule exists for `0.0.0.0/0` &rarr; TCP Port `8080`.
5. **Check OCI OS-level `iptables`**:
   ```bash
   sudo iptables -L INPUT -n --line-numbers
   ```
   Ensure port `8080` is accepted before any `REJECT` or `DROP` rules.

---

## 23. Restart the Deployment

Restart all services:
```bash
docker compose restart
```

Restart only the application:
```bash
docker compose restart app
```

Restart only PostgreSQL:
```bash
docker compose restart postgres
```

---

## 24. Stop the Deployment

To stop and remove containers while preserving database data:

```bash
docker compose down
```

*(The database data is preserved in the `postgres_data` Docker volume).*

---

## 25. Stop and Delete Database Data (Full Reset)

To stop containers and delete the PostgreSQL database volume:

```bash
docker compose down -v
```

> [!WARNING]
> This permanently deletes all database records and tables.

---

## 26. Deploy Application Updates

When you push updates to Git, deploy the new version with:

```bash
cd SaavyBootVue
git pull
docker compose up --build -d
docker compose logs -f app
```

---

## 27. Rebuild Without Docker Cache

If you want a clean rebuild without cached layers:

```bash
docker compose build --no-cache
docker compose up -d
```

---

## 28. Useful Docker Commands

```bash
# View live container resource usage (CPU, RAM)
docker stats

# View running containers
docker ps

# View all containers (including stopped)
docker ps -a

# View Docker volume storage
docker volume ls

# Clean up unused images and build cache
docker system prune -f
```

---

## 29. Complete Fresh Deployment Command Cheat-Sheet

```bash
# 1. Install dependencies
sudo apt update && sudo apt upgrade -y
sudo apt install -y docker.io docker-compose-plugin git iptables-persistent
sudo systemctl enable docker && sudo systemctl start docker
sudo usermod -aG docker $USER
newgrp docker

# 2. Open firewall in OCI OS
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
sudo netfilter-persistent save

# 3. Clone and deploy
git clone YOUR_REPOSITORY_URL
cd SaavyBootVue
nano docker-compose.yml  # Update passwords
docker compose up --build -d
docker compose ps
docker compose logs -f app
```

---

## 30. Quick Update Cheat-Sheet

```bash
cd SaavyBootVue
git pull
docker compose up --build -d
docker compose logs -f app
```
