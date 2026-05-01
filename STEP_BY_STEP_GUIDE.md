# Step-by-Step Full Stack Development Guide

This guide walks you through the complete workflow of developing and deploying a full-stack web application using GitHub, Codespaces, Spring Boot, Vue, PostgreSQL, and Render.

---

## Part 1: Fork and Setup in GitHub & Codespaces

### Step 1.1: Fork the Repository

1. Go to [https://github.com/blats002/saavy-fullstack-starter](https://github.com/blats002/saavy-fullstack-starter)
2. Click the **Fork** button in the top-right corner
3. Name your fork (e.g., `my-fullstack-app`)
4. Click **Create fork**

You now have your own copy of the starter project.

### Step 1.2: Open in GitHub Codespaces

1. On your forked repository, click the green **Code** button
2. Select the **Codespaces** tab
3. Click **Create codespace on main**
4. Wait for the Codespace to load (this may take 2-3 minutes)

You now have a full development environment in the browser with VS Code.

---

## Part 2: Run PostgreSQL Locally (or in Codespaces)

### Step 2.1: Start PostgreSQL Container (Docker Compose)

In the terminal within Codespaces:

```bash
docker-compose up -d
```

This starts a PostgreSQL container with the database `saavy_db`.

Wait for the container to be ready. You should see:
```
saavy_db database system is ready to accept connections
```

Verify the connection:
```bash
psql -U postgres -h localhost -d saavy_db -c "SELECT 1;"
```

If you see `1`, PostgreSQL is running successfully.

---

## Part 3: Start Spring Boot Backend

### Step 3.1: Build and Run Spring Boot

In the terminal:

```bash
cd server
./gradlew bootRun --args='--spring.profiles.active=development'
```

Wait for the server to start. You should see:
```
Tomcat started on port(s): 8080
```

The backend API is now running at `http://localhost:8080`.

---

## Part 4: Add a New Column to the Test Entity

Now we'll extend the Test entity by adding a new column. This demonstrates how developers can modify the schema.

### Step 4.1: Modify the Test Entity

Open `server/src/main/java/org/saavy/entity/Test.java`

Add a new field to the Test entity:

```java
@Entity
@Table(name = "test")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "text_1")
    private String text1;

    @Column(name = "size")
    @JdbcTypeCode(SqlTypes.INTEGER)
    private Integer size;

    // NEW COLUMN - Add this field
    @Column(name = "description")
    private String description;
}
```

Save the file.

### Step 4.2: Modify the TestDTO

Open `server/src/main/java/org/saavy/entity/TestDTO.java`

Add the same field:

```java
public class TestDTO {
    private Long id;
    private String text1;
    private Integer size;
    
    // NEW COLUMN - Add this field
    private String description;

    // Include getters and setters (or use @Getter @Setter annotation)
}
```

Save the file.

---

## Part 5: Generate Liquibase Migration

### Step 5.1: Compile and Generate Migration

In the `server/` directory, run:

```bash
./gradlew liquibaseJpaDiffChangeLog
```

This command:
- Compiles the Java code
- Compares the JPA entities (current schema) with the database (existing schema)
- Generates a migration file with the difference

You should see output like:

```
Liquibase History
liquibase:liquibaseJpaDiffChangeLog PASSED
```

### Step 5.2: Review the Generated Migration

Open `server/src/main/resources/db/changelog/generated-jpa-diff-changelog.xml`

It should contain a new column definition similar to:

```xml
<changeSet id="1744070400000-1" author="liquibase-hibernate">
    <addColumn tableName="test">
        <column name="description" type="VARCHAR(255)"/>
    </addColumn>
</changeSet>
```

### Step 5.3: Apply the Migration

Restart Spring Boot or let Spring Boot auto-apply the migration on next startup:

```bash
./gradlew bootRun --args='--spring.profiles.active=development'
```

Spring Boot will:
1. Read the migration files from the changelog
2. Apply any pending migrations to the database
3. Update the database schema

Verify in the database:

```bash
psql -U postgres -h localhost -d saavy_db -c "\d test"
```

You should see the new `description` column.

---

## Part 6: Update the Frontend Vue Component

The `CrudTest.vue` page is now generic and driven by a field configuration array. That means you no longer need to manually add table columns and form fields in two places.

### Step 6.1: Update the field configuration in `CrudTest.vue`

Open `client/src/views/pages/CrudTest.vue`

Find the `formConfig` array near the top of the file. Add a new field definition for `description`:

```js
{
    name: 'description',
    label: 'Description',
    type: 'textarea',
    sortable: false,
    displayInTable: false,
    displayInForm: true,
    editable: true,
    required: false,
    width: '100%'
}
```

Because the page is generic, the table and form will update automatically from this configuration.

### Step 6.2: Show the new field in the table (optional)

If you also want the new field to appear as a table column, set:

```js
displayInTable: true
```

The dynamic table renderer will add the column automatically.

### Step 6.3: Update validation if needed

If `description` should be required, set `required: true` in `formConfig` for that field.

The generic validation logic will then enforce it automatically.

---

## Part 7: Test the New Feature Locally

### Step 7.1: Verify the Frontend Loads

In the terminal, navigate to the client folder:

```bash
cd client
npm run dev
```

Open `http://localhost:5173` in the browser.

### Step 7.2: Test Creating a New Test Record

1. Click the **New** button
2. Fill in:
   - Text: "My Test"
   - Size: 100
   - Description: "This is my first test with a description"
3. Click **Save**

The new test should appear in the table with all visible fields.

### Step 7.3: Verify the Data in the Database

```bash
psql -U postgres -h localhost -d saavy_db -c "SELECT id, text_1, size, description FROM test;"
```

                                                                                                                                                                                                                                            ```bash
                                                                                                                                                                                                                                            cd client
                                                                                                                                                                                                                                            npm run dev
                                                                                                                                                                                                                                            ```

                                                                                                                                                                                                                                            Open http://localhost:5173 in the browser.

                                                                                                                                                                                                                                            ### Step 7.2: Test Creating a New Test Record

                                                                                                                                                                                                                                            1. Click the **New** button
                                                                                                                                                                                                                                            2. Fill in:
                                                                                                                                                                                                                                               - Text: "My Test"
                                                                                                                                                                                                                                                  - Size: 100
                                                                                                                                                                                                                                                     - Description: "This is my first test with a description"
                                                                                                                                                                                                                                                     3. Click **Save**

                                                                                                                                                                                                                                                     The new test should appear in the table with all three fields visible.

                                                                                                                                                                                                                                                     ### Step 7.3: Verify the Data in the Database

                                                                                                                                                                                                                                                     ```bash
                                                                                                                                                                                                                                                     psql -U postgres -h localhost -d saavy_db -c "SELECT id, text_1, size, description FROM test;"
                                                                                                                                                                                                                                                     ```

                                                                                                                                                                                                                                                     ---

                                                                                                                                                                                                                                                     ## Part 8: Commit Changes to Git

                                                                                                                                                                                                                                                     ### Step 8.1: Open Source Control in Codespaces

                                                                                                                                                                                                                                                     In VS Code (left sidebar), click the **Source Control** icon (or Ctrl+Shift+G).

                                                                                                                                                                                                                                                     ### Step 8.2: Stage Changes

                                                                                                                                                                                                                                                     You should see modified files listed:
                                                                                                                                                                                                                                                     - `server/src/main/java/org/saavy/entity/Test.java`
                                                                                                                                                                                                                                                     - `server/src/main/java/org/saavy/entity/TestDTO.java`
                                                                                                                                                                                                                                                     - `server/src/main/resources/db/changelog/generated-jpa-diff-changelog.xml`
                                                                                                                                                                                                                                                     - `client/src/views/pages/CrudTest.vue`

                                                                                                                                                                                                                                                     Click the **+** icon next to each file to stage them, or click the **+** next to "Changes" to stage all.

                                                                                                                                                                                                                                                     ### Step 8.3: Commit the Changes

                                                                                                                                                                                                                                                     In the commit message box, type:

                                                                                                                                                                                                                                                     ```
                                                                                                                                                                                                                                                     Add description field to Test entity

                                                                                                                                                                                                                                                     - Add description column to Test.java entity
                                                                                                                                                                                                                                                     - Add description field to TestDTO
                                                                                                                                                                                                                                                     - Generate Liquibase migration for new column
                                                                                                                                                                                                                                                     - Update CrudTest.vue to display and edit description field
                                                                                                                                                                                                                                                     ```

                                                                                                                                                                                                                                                     Click the **Commit** button (or press Ctrl+Enter).

                                                                                                                                                                                                                                                     ### Step 8.4: Push to GitHub

                                                                                                                                                                                                                                                     Click the **Sync Changes** button or click the **...** menu and select **Push**.

                                                                                                                                                                                                                                                     Your changes are now pushed to your GitHub fork.

                                                                                                                                                                                                                                                     ---

                                                                                                                                                                                                                                                     ## Part 9: Deploy to Render

                                                                                                                                                                                                                                                     ### Step 9.1: Create a Render Account

                                                                                                                                                                                                                                                     1. Go to [https://render.com](https://render.com)
                                                                                                                                                                                                                                                     2. Click **Sign up** and create an account using GitHub
                                                                                                                                                                                                                                                     3. Authorize Render to access your GitHub account

                                                                                                                                                                                                                                                     ### Step 9.2: Create a PostgreSQL Service

                                                                                                                                                                                                                                                     1. Go to your Render Dashboard
                                                                                                                                                                                                                                                     2. Click **New +** → **PostgreSQL**
                                                                                                                                                                                                                                                     3. Fill in:
                                                                                                                                                                                                                                                        - Name: `saavy-db`
                                                                                                                                                                                                                                                           - Database Name: `saavy_db`
                                                                                                                                                                                                                                                              - User: `postgres`
                                                                                                                                                                                                                                                                 - Region: Select closest region
                                                                                                                                                                                                                                                                    - PostgreSQL Version: 15
                                                                                                                                                                                                                                                                       - Plan: Free (for testing)
                                                                                                                                                                                                                                                                       4. Click **Create Database**

                                                                                                                                                                                                                                                                       Wait for the database to be created. Copy the **Internal Database URL** (you'll need this).

                                                                                                                                                                                                                                                                       Example: `postgresql://postgres:xxxxx@localhost:5432/saavy_db`

                                                                                                                                                                                                                                                                       ### Step 9.3: Create a Web Service for Spring Boot

                                                                                                                                                                                                                                                                       1. Click **New +** → **Web Service**
                                                                                                                                                                                                                                                                       2. Select your GitHub fork repository
                                                                                                                                                                                                                                                                       3. Fill in:
                                                                                                                                                                                                                                                                          - Name: `saavy-fullstack-api`
                                                                                                                                                                                                                                                                             - Environment: Docker
                                                                                                                                                                                                                                                                                - Region: Same as database
                                                                                                                                                                                                                                                                                   - Plan: Free (for testing)
                                                                                                                                                                                                                                                                                   4. Click **Create Web Service**

                                                                                                                                                                                                                                                                                   Render will start building and deploying.

                                                                                                                                                                                                                                                                                   ### Step 9.4: Configure Environment Variables for Web Service

                                                                                                                                                                                                                                                                                   While the build is running, click on the Web Service to view its settings.

                                                                                                                                                                                                                                                                                   Go to the **Environment** tab and add:

                                                                                                                                                                                                                                                                                   ```
                                                                                                                                                                                                                                                                                   SPRING_DATASOURCE_URL=postgresql://postgres:PASSWORD@saavy-db:5432/saavy_db
                                                                                                                                                                                                                                                                                   SPRING_DATASOURCE_USERNAME=postgres
                                                                                                                                                                                                                                                                                   SPRING_DATASOURCE_PASSWORD=PASSWORD
                                                                                                                                                                                                                                                                                   SPRING_JPA_HIBERNATE_DDL_AUTO=validate
                                                                                                                                                                                                                                                                                   SPRING_PROFILE_ACTIVE=production
                                                                                                                                                                                                                                                                                   ```

                                                                                                                                                                                                                                                                                   Replace `PASSWORD` with the password from your PostgreSQL service.

                                                                                                                                                                                                                                                                                   ### Step 9.5: Create a Web Service for Vue Frontend

                                                                                                                                                                                                                                                                                   1. Click **New +** → **Web Service**
                                                                                                                                                                                                                                                                                   2. Select your GitHub fork repository
                                                                                                                                                                                                                                                                                   3. Fill in:
                                                                                                                                                                                                                                                                                      - Name: `saavy-fullstack-web`
                                                                                                                                                                                                                                                                                         - Environment: Node
                                                                                                                                                                                                                                                                                            - Build Command: `cd client && npm install && npm run build`
                                                                                                                                                                                                                                                                                               - Start Command: `cd client && npm run preview`
                                                                                                                                                                                                                                                                                                  - Region: Same as others
                                                                                                                                                                                                                                                                                                     - Plan: Free (for testing)
                                                                                                                                                                                                                                                                                                     4. Click **Create Web Service**

                                                                                                                                                                                                                                                                                                     ### Step 9.6: Wait for Deployments to Complete

                                                                                                                                                                                                                                                                                                     Both services will build and deploy. This may take 5-10 minutes.

                                                                                                                                                                                                                                                                                                     Once both show a green **Live** status, your app is deployed!

                                                                                                                                                                                                                                                                                                     ### Step 9.7: Test Your Deployed App

                                                                                                                                                                                                                                                                                                     1. Go to the Vue frontend Web Service
                                                                                                                                                                                                                                                                                                     2. Click the service URL (e.g., `https://saavy-fullstack-web.onrender.com`)
                                                                                                                                                                                                                                                                                                     3. Test the CRUD functionality
                                                                                                                                                                                                                                                                                                     4. Create, read, update, and delete tests

                                                                                                                                                                                                                                                                                                     ---

                                                                                                                                                                                                                                                                                                     ## Part 10: Make Another Change (Iteration)

                                                                                                                                                                                                                                                                                                     ### Step 10.1: Modify the Entity Again

                                                                                                                                                                                                                                                                                                     For example, add a new field `category` to the Test entity.

                                                                                                                                                                                                                                                                                                     ### Step 10.2: Run the Liquibase Task Again

                                                                                                                                                                                                                                                                                                     ```bash
                                                                                                                                                                                                                                                                                                     cd server
                                                                                                                                                                                                                                                                                                     ./gradlew liquibaseJpaDiffChangeLog
                                                                                                                                                                                                                                                                                                     ```

                                                                                                                                                                                                                                                                                                     ### Step 10.3: Update the Frontend

                                                                                                                                                                                                                                                                                                     Add the new field to `CrudTest.vue`.

                                                                                                                                                                                                                                                                                                     ### Step 10.4: Commit and Push

                                                                                                                                                                                                                                                                                                     ```bash
                                                                                                                                                                                                                                                                                                     git add .
                                                                                                                                                                                                                                                                                                     git commit -m "Add category field to Test entity"
                                                                                                                                                                                                                                                                                                     git push
                                                                                                                                                                                                                                                                                                     ```

                                                                                                                                                                                                                                                                                                     ### Step 10.5: Render Auto-Deploys

                                                                                                                                                                                                                                                                                                     Render will automatically:
                                                                                                                                                                                                                                                                                                     1. Detect the new commit
                                                                                                                                                                                                                                                                                                     2. Rebuild the Docker image
                                                                                                                                                                                                                                                                                                     3. Apply Liquibase migrations
                                                                                                                                                                                                                                                                                                     4. Deploy the new version

                                                                                                                                                                                                                                                                                                     No manual steps needed!

                                                                                                                                                                                                                                                                                                     ---

                                                                                                                                                                                                                                                                                                     ## Troubleshooting

                                                                                                                                                                                                                                                                                                     ### PostgreSQL Connection Issues

                                                                                                                                                                                                                                                                                                     Make sure Docker is running:
                                                                                                                                                                                                                                                                                                     ```bash
                                                                                                                                                                                                                                                                                                     docker ps
                                                                                                                                                                                                                                                                                                     ```

                                                                                                                                                                                                                                                                                                     Restart if needed:
                                                                                                                                                                                                                                                                                                     ```bash
                                                                                                                                                                                                                                                                                                     docker-compose restart
                                                                                                                                                                                                                                                                                                     ```

                                                                                                                                                                                                                                                                                                     ### Liquibase Migration Fails

                                                                                                                                                                                                                                                                                                     Check the database connection in `build.gradle`:
                                                                                                                                                                                                                                                                                                     ```gradle
                                                                                                                                                                                                                                                                                                     def liquibaseUrl = "jdbc:postgresql://localhost:5432/saavy_db"
                                                                                                                                                                                                                                                                                                     def liquibaseUsername = 'postgres'
                                                                                                                                                                                                                                                                                                     def liquibasePassword = 'password'
                                                                                                                                                                                                                                                                                                     ```

                                                                                                                                                                                                                                                                                                     Make sure PostgreSQL is running and credentials are correct.

                                                                                                                                                                                                                                                                                                     ### Spring Boot Won't Start

                                                                                                                                                                                                                                                                                                     Check the logs:
                                                                                                                                                                                                                                                                                                     ```bash
                                                                                                                                                                                                                                                                                                     ./gradlew bootRun --args='--spring.profiles.active=development' 2>&1 | tail -50
                                                                                                                                                                                                                                                                                                     ```

                                                                                                                                                                                                                                                                                                     Common issues:
                                                                                                                                                                                                                                                                                                     - Port 8080 already in use
                                                                                                                                                                                                                                                                                                     - Database not running
                                                                                                                                                                                                                                                                                                     - Incorrect database URL or credentials

                                                                                                                                                                                                                                                                                                     ### Vue Frontend Not Building

                                                                                                                                                                                                                                                                                                     Make sure Node.js 16+ is installed:
                                                                                                                                                                                                                                                                                                     ```bash
                                                                                                                                                                                                                                                                                                     node --version
                                                                                                                                                                                                                                                                                                     npm --version
                                                                                                                                                                                                                                                                                                     ```

                                                                                                                                                                                                                                                                                                     Clear node_modules and reinstall:
                                                                                                                                                                                                                                                                                                     ```bash
                                                                                                                                                                                                                                                                                                     cd client
                                                                                                                                                                                                                                                                                                     rm -rf node_modules
                                                                                                                                                                                                                                                                                                     npm install
                                                                                                                                                                                                                                                                                                     npm run dev
                                                                                                                                                                                                                                                                                                     ```

                                                                                                                                                                                                                                                                                                     ---

                                                                                                                                                                                                                                                                                                     ## Summary

                                                                                                                                                                                                                                                                                                     You've now completed a full cycle:

                                                                                                                                                                                                                                                                                                     1. ✅ Forked the GitHub repository
                                                                                                                                                                                                                                                                                                     2. ✅ Opened in Codespaces
                                                                                                                                                                                                                                                                                                     3. ✅ Started PostgreSQL
                                                                                                                                                                                                                                                                                                     4. ✅ Ran Spring Boot backend
                                                                                                                                                                                                                                                                                                     5. ✅ Added a new column to the Test entity
                                                                                                                                                                                                                                                                                                     6. ✅ Generated Liquibase migration
                                                                                                                                                                                                                                                                                                     7. ✅ Updated the Vue frontend
                                                                                                                                                                                                                                                                                                     8. ✅ Tested locally
                                                                                                                                                                                                                                                                                                     9. ✅ Committed and pushed to GitHub
                                                                                                                                                                                                                                                                                                     10. ✅ Deployed to Render
                                                                                                                                                                                                                                                                                                     11. ✅ Verified the deployed app

                                                                                                                                                                                                                                                                                                     From here, make more changes to entities and forms, and Render will auto-deploy them. This workflow scales to full production apps!

                                                                                                                                                                                                                                                                                                     ---

                                                                                                                                                                                                                                                                                                     ## Next Steps

                                                                                                                                                                                                                                                                                                     - Add authentication (Spring Security)
                                                                                                                                                                                                                                                                                                     - Add more complex entities and relationships
                                                                                                                                                                                                                                                                                                     - Configure CORS for frontend-backend communication
                                                                                                                                                                                                                                                                                                     - Add API documentation (Swagger/OpenAPI)
                                                                                                                                                                                                                                                                                                     - Implement pagination and filtering
                                                                                                                                                                                                                                                                                                     - Add unit tests
                                                                                                                                                                                                                                                                                                     - Monitor app performance on Render

                                                                                                                                                                                                                                                                                                     Good luck with your full-stack development journey!
