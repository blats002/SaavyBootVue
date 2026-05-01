# Rendering PostgreSQL Deployment Guide

This guide explains how to deploy your Spring Boot application to Render with PostgreSQL.

## Prerequisites

- Render account (https://render.com)
- Application built and ready to deploy

## Setup Steps

### 1. Create PostgreSQL Database on Render

1. Log in to your Render dashboard
2. Click **New +** → **PostgreSQL**
3. Configure:
   - **Name**: `saavy-postgres` (or your preference)
   - **Database**: `saavy_db`
   - **User**: `postgres` (or custom username)
   - **Region**: Choose your region
   - **Plan**: Free tier available for testing

4. After creation, Render provides a **Internal Database URL** (for services) and **External Database URL** (for external connections)

### 2. Create Web Service on Render

1. Click **New +** → **Web Service**
2. Connect your repository or deploy via Docker
3. Configure:
   - **Name**: `saavy-app`
   - **Environment**: `Java`
   - **Build Command**: `./gradlew build`
   - **Start Command**: `java -jar server/build/libs/server-0.0.1-SNAPSHOT.jar --spring.profiles.active=production`

### 3. Add Environment Variables

In your Render Web Service settings, add these environment variables:

```
DB_HOST=<your-postgres-instance>.c.renders.com
DB_PORT=5432
DB_NAME=saavy_db
DB_USER=postgres
DB_PASSWORD=<paste-from-postgres-dashboard>
SPRING_PROFILES_ACTIVE=production
```

**To find these values:**
- Go to your PostgreSQL instance dashboard on Render
- Copy the connection details from the connection string
- Example connection string: `postgresql://postgres:password123@dpg-abc123.c.renders.com:5432/saavy_db?sslmode=require`

### 4. Deploy

Push your code to the connected repository, and Render will automatically deploy.

### 5. Verify Connection

Check your Web Service logs on Render dashboard to ensure the database connection is successful.

## Important Notes

- The `application-production.properties` uses `sslmode=require` for secure connections (required by Render)
- `ddl-auto` is set to `validate` in production to prevent accidental schema changes
- Connection pooling is configured for production use
- Free tier PostgreSQL databases on Render have limitations; upgrade if needed

## Troubleshooting

**Connection refused:**
- Verify DB_HOST, DB_PORT, and DB_PASSWORD are correct
- Ensure the PostgreSQL instance is running
- Check that your Web Service has network access (they're on same Render account by default)

**SSL errors:**
- Render requires SSL - ensure `?sslmode=require` is in the connection URL
- This is already configured in `application-production.properties`

**Liquibase errors:**
- Liquibase migrations run automatically; check logs
- If migrations fail, review your `db/changelog/db.changelog-master.xml`
