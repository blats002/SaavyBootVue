# Deploy This Project to Oracle Cloud Infrastructure Using Docker Compose

This guide explains how to deploy this project to an Oracle Cloud Infrastructure Compute instance using Docker Compose.

The deployment will run:

- PostgreSQL in a Docker container
- The application in a Docker container
- Both services connected through Docker Compose networking

## 1. Deployment Overview

The project includes a Docker Compose setup with two services:

| Service | Purpose | Exposed Port |
|---|---|---|
| `postgres` | PostgreSQL database | `5432` |
| `app` | Spring Boot application | `8080` |

The application will be available at: [http://YOUR_OCI_PUBLIC_IP:8080](http://YOUR_OCI_PUBLIC_IP:8080)


## 2. Prerequisites

You need:

- An Oracle Cloud Infrastructure account
- An OCI Compute instance
- SSH access to the instance
- Docker installed on the instance
- Docker Compose installed on the instance
- Port `8080` opened in OCI networking rules

Recommended OCI instance image: Ubuntu 22.04 or newer


## 3. Create an OCI Compute Instance

In the OCI Console:

1. Go to **Compute**
2. Click **Instances**
3. Click **Create instance**
4. Enter a name for the instance
5. Select an Ubuntu image
6. Select an instance shape
7. Add your SSH public key
8. Select or create a Virtual Cloud Network
9. Make sure the instance has a public IPv4 address
10. Click **Create**

After the instance is created, copy the public IP address.

You will use it later as: YOUR_OCI_PUBLIC_IP

## 4. Open Port 8080 in OCI

The application runs on port `8080`, so OCI must allow inbound traffic to that port.

In the OCI Console:

1. Go to **Networking**
2. Open your **Virtual Cloud Network**
3. Open the subnet used by your Compute instance
4. Open the associated **Security List** or **Network Security Group**
5. Add an ingress rule: Source CIDR: 0.0.0.0/0 IP Protocol: TCP Destination Port Range: 8080

For production, restrict `Source CIDR` to trusted IP ranges if possible.

## 5. Connect to the OCI Instance

From your local machine, connect using SSH: bash ssh ubuntu@YOUR_OCI_PUBLIC_IP

Example: bash ssh ubuntu@123.45.67.89

If your private key is not the default SSH key, use: bash ssh -i /path/to/private-key ubuntu@YOUR_OCI_PUBLIC_IP

## 6. Update the Server

After connecting to the OCI instance, update the package list: bash sudo apt update sudo apt upgrade -y

## 7. Install Docker

Install Docker: bash sudo apt install -y docker.io

Enable Docker: bash sudo systemctl enable docker

Start Docker: bash sudo systemctl start docker

Check Docker status: bash sudo systemctl status docker

Verify Docker is installed: bash docker --version

## 8. Allow Your User to Run Docker

Add the current user to the Docker group: bash sudo usermod -aG docker $USER

Apply the group change: bash newgrp docker

Verify Docker works without `sudo`: bash docker ps

## 9. Install Docker Compose

Install the Docker Compose plugin: bash sudo apt install -y docker-compose-plugin

Verify Docker Compose: bash docker compose version

## 10. Install Git

Install Git if it is not already installed: bash sudo apt install -y git

Verify Git: bash git --version

## 11. Clone the Project onto the OCI Instance

Clone the project repository: bash git clone YOUR_REPOSITORY_URL

Go into the project directory: bash cd saavy-fullstack-starter

Replace `YOUR_REPOSITORY_URL` with your actual Git repository URL.

## 12. Confirm Required Deployment Files

From the project root, confirm these files exist: docker-compose.yml server/DockerFile

Run: bash ls



Then: bash ls server



## 13. Review the Docker Compose Services

The deployment uses: postgres app



The `postgres` service provides the database.

The `app` service builds the application using the Dockerfile in the server directory.

The application connects to PostgreSQL through the internal Docker Compose hostname: postgres

The application is exposed publicly on: 8080

## 14. Update Production Database Credentials

Before deploying to OCI, change the default database password.

Open the Docker Compose file: bash nano docker-compose.yml

Update the PostgreSQL password: yaml POSTGRES_PASSWORD: your-secure-database-password

Also update the application datasource password to match: yaml SPRING_DATASOURCE_PASSWORD: your-secure-database-password

Make sure both values are identical.

Example: yaml POSTGRES_PASSWORD: MyStrongPassword123! SPRING_DATASOURCE_PASSWORD: MyStrongPassword123!

Save and close the file.

In `nano`: CTRL + O ENTER CTRL + X

## 15. Optional: Avoid Exposing PostgreSQL Publicly

For production, PostgreSQL usually should not be exposed to the public internet.

If you do not need to connect to PostgreSQL from outside the server, remove or comment out this section from the `postgres` service:
    yaml ports:
    - "5432:5432"

The application can still connect to PostgreSQL internally through Docker Compose.

The database will remain reachable by the application using: postgres:5432

## 16. Build and Start the Containers

From the project root, run: bash docker compose up --build -d

This command will:

1. Pull the PostgreSQL image
2. Build the application image
3. Start the database container
4. Wait for PostgreSQL to become healthy
5. Start the application container
6. Expose the application on port `8080`

## 17. Check Container Status

Run: bash docker compose ps

You should see both services running.

Expected services: postgres app

## 18. View Application Logs

To view all logs: bash docker compose logs -f

To view only the application logs: bash docker compose logs -f app

To view only PostgreSQL logs: bash docker compose logs -f postgres

## 19. Test the Deployment from the OCI Instance

From inside the OCI instance, test the application: bash curl [http://localhost:8080](http://localhost:8080)

If the app is running, you should receive an HTTP response.

## 20. Access the Application from a Browser

Open your browser and visit: http://YOUR_OCI_PUBLIC_IP:8080

Example: http://123.45.67.89:8080

## 21. If the Application Is Not Reachable

Check the Docker containers: bash docker compose ps

Check application logs: bash docker compose logs -f app

Check if the server is listening on port `8080`: bash sudo ss -tulpn | grep 8080

Check OCI ingress rules and confirm port `8080` is open.

## 22. Check Ubuntu Firewall

OCI networking rules are required, but the Ubuntu firewall can also block traffic.

Check firewall status: bash sudo ufw status

If UFW is active, allow port `8080`: bash sudo ufw allow 8080/tcp

Reload UFW: bash sudo ufw reload

## 23. Restart the Deployment

Restart all services: bash docker compose restart
Restart only the application: bash docker compose restart app

Restart only PostgreSQL: bash docker compose restart postgres

## 24. Stop the Deployment

To stop and remove the containers: bash docker compose down

This keeps the PostgreSQL Docker volume.

## 25. Stop and Delete Database Data

Only run this if you want to delete the database data: bash docker compose down -v

Warning: this permanently removes the PostgreSQL volume.

## 26. Deploy New Application Updates

To update the deployed application: bash cd saavy-fullstack-starter git pull docker compose up --build -d

Then check the logs: bash docker compose logs -f app

## 27. Rebuild Without Cache

If the deployment is not picking up changes, rebuild without cache: bash docker compose build --no-cache docker compose up -d

## 28. Useful Docker Commands

List running containers: bash docker ps

List all containers: bash docker ps -a

List Docker images: bash docker images

List Docker volumes: bash docker volume ls

View container resource usage: bash docker stats

Remove unused Docker resources: bash docker system prune -f


## 29. Complete Fresh Deployment Command Summary

Use this for a new OCI server:
bash sudo apt update sudo apt upgrade -y sudo apt install -y docker.io docker-compose-plugin git sudo systemctl enable docker sudo systemctl start docker sudo usermod -aG docker $USER newgrp docker
git clone YOUR_REPOSITORY_URL cd saavy-fullstack-starter
docker compose up --build -d docker compose ps docker compose logs -f app

Then open: http://YOUR_OCI_PUBLIC_IP:8080

## 30. Complete Update Deployment Command Summary

Use this when the app is already deployed: bash cd saavy-fullstack-starter git pull docker compose up --build -d docker compose logs -f app

## 34. Troubleshooting Checklist

If the app does not load in the browser, check:

1. Is the OCI instance running?
2. Is the public IP correct?
3. Is port `8080` open in the OCI Security List or Network Security Group?
4. Are the Docker containers running?
5. Is the app container logging any errors?
6. Is PostgreSQL healthy?
7. Is Ubuntu firewall blocking the port?
8. Is the application listening on port `8080`?

Useful commands: bash docker compose ps docker compose logs -f app docker compose logs -f postgres sudo ss -tulpn | grep 8080 sudo ufw status
































