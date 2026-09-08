# SaavyBootVue

A modern, extensible, full-stack web application starter architecture powered by **Spring Boot 3.5 (Java 21)** and **Vue 3 (PrimeVue + Vite)** with a dynamic **modular plugin system**, **metadata-driven generic CRUD UI**, **JWT authentication**, and **Liquibase database versioning**.

---

## 📑 Table of Contents

- [Overview & Key Features](#-overview--key-features)
- [Tech Stack](#-tech-stack)
- [Project Architecture](#-project-architecture)
- [Directory Structure](#-directory-structure)
- [Prerequisites](#-prerequisites)
- [Getting Started (Quickstart)](#-getting-started-quickstart)
  - [1. Start PostgreSQL](#1-start-postgresql-database)
  - [2. Start Spring Boot Backend](#2-start-spring-boot-backend)
  - [3. Start Vue Frontend](#3-start-vue-frontend)
  - [4. Access the Application](#4-access-the-application)
- [How the Dynamic Plugin Architecture Works](#-how-the-dynamic-plugin-architecture-works)
  - [Backend Auto-Discovery](#backend-auto-discovery)
  - [Frontend Auto-Discovery](#frontend-auto-discovery)
  - [Metadata-Driven Generic UI](#metadata-driven-generic-ui)
- [Step-by-Step Guide: Adding a New Plugin with New Pages](#-step-by-step-guide-adding-a-new-plugin-with-new-pages)
  - [Step 1: Create the Plugin Directory Structure](#step-1-create-the-plugin-directory-structure)
  - [Step 2: Implement the Server Module](#step-2-implement-the-server-module)
  - [Step 3: Implement the Client Module](#step-3-implement-the-client-module)
  - [Step 4: Generate or Write Database Migrations](#step-4-generate-or-write-database-migrations)
  - [Step 5: Run and Verify](#step-5-run-and-verify)
- [Common Gradle & NPM Tasks](#-common-gradle--npm-tasks)
- [Production Build & Docker Deployment](#-production-build--docker-deployment)

---

## 🚀 Overview & Key Features

- **Dynamic Plugin System**: Add new features, APIs, and UI pages inside `plugins/` without modifying core host application code.
- **Zero-Config Plugin Discovery**:
  - **Gradle** automatically scans and includes plugin server subprojects into Spring Boot at build and runtime.
  - **Vite** automatically discovers plugin client entrypoints, dynamically registering Vue Router routes and PrimeVue navigation menus.
- **Metadata-Driven CRUD**: Annotate JPA entities with `@UiMaster` and `@UiField`; the frontend `<GenericCrud>` component auto-generates tables, sorting, filters, forms, and dialogs.
- **Liquibase Modular Migrations**: Schema changes for both the core app and plugins are version-controlled and auto-applied on startup.
- **Built-in Security**: Spring Security with JWT authentication and role-based authorization (`ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_USER`).

---

## 🛠 Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 21, Spring Boot 3.5.x, Spring Data JPA, Spring Security (JWT), Liquibase, Lombok, ModelMapper |
| **Frontend** | Vue 3 (Composition API / `<script setup>`), Vite 8, PrimeVue 3 (Sakai Theme), PrimeFlex, PrimeIcons, Axios, Vue Router |
| **Database** | PostgreSQL (supported), H2 (in-memory / test), MySQL |
| **Build & Tooling** | Gradle Multi-Project, Docker & Docker Compose, Node.js & npm |

---

## 🏗 Directory Structure

```text
SaavyBootVue/
├── build.gradle              # Top-level Gradle build configuration
├── settings.gradle           # Auto-discovers plugin server subprojects
├── docker-compose.yml        # PostgreSQL and app container configurations
├── gradle/
│   ├── saavy-server.gradle          # Core server Gradle configuration
│   ├── saavy-client.gradle          # Client build Gradle tasks
│   └── saavy-plugin-server.gradle   # Reusable plugin server Gradle template
├── server/                   # Host Spring Boot core application
│   ├── src/main/java/org/saavy/     # Security, base controllers, core entities
│   └── src/main/resources/          # application.properties & Liquibase master changelog
├── client/                   # Host Vue 3 + PrimeVue frontend
│   ├── src/layout/                  # AppLayout, AppMenu, AppTopbar
│   ├── src/plugins/pluginLoader.js  # Dynamic plugin scanner and loader (Vite glob)
│   ├── src/router/index.js          # Vue Router registering host & plugin routes
│   └── src/views/                   # Core views (Dashboard, Login, Crud, etc.)
└── plugins/                  # Modular plugin extensions directory
    └── starter-plugin/       # Reference plugin implementation
        ├── client/           # Plugin Vue components, views, and manifest
        │   └── src/
        │       ├── index.js                     # Plugin manifest (routes, menu, etc.)
        │       └── views/pages/SampleItemManagement.vue
        └── server/           # Plugin Spring Boot module
            ├── build.gradle                     # Reuses saavy-plugin-server.gradle
            └── src/main/
                ├── java/org/saavy/              # Entities, controllers, services
                └── resources/db/changelog/plugins/ # Plugin Liquibase XML changelogs
```

---

## 📋 Prerequisites

Before running the project locally, ensure you have installed:

- **JDK 21** (e.g., Eclipse Temurin, Amazon Corretto, or OpenJDK)
- **Node.js 18+** and **npm**
- **Docker** & **Docker Compose** (for running PostgreSQL)
- **Git**

---

## ⚡ Getting Started (Quickstart)

### 1. Start PostgreSQL Database

Launch the PostgreSQL container using Docker Compose:

```bash
docker-compose up -d postgres
```

- **Host**: `localhost:5432`
- **Database**: `saavy_db`
- **Username**: `postgres`
- **Password**: `password`

### 2. Start Spring Boot Backend

From the project root:

```bash
# On Linux / macOS / Codespaces:
./gradlew bootRun

# On Windows:
gradlew.bat bootRun
```

The Spring Boot backend will start on **`http://localhost:8080`**.

> [!TIP]
> To run with JVM remote debug enabled (listening on port `5005`):
> ```bash
> ./gradlew bootRunDebug
> ```

### 3. Start Vue Frontend

In a separate terminal window:

```bash
cd client
npm install
npm run dev
```

The Vite dev server will start on **`http://localhost:5173`** (or `http://localhost:8080` in single-bundle mode).

### 4. Access the Application

Open your browser to `http://localhost:5173` (or the URL printed by Vite).

**Default Login Credentials:**
- **Username**: `admin`
- **Password**: `admin123`
- **Roles**: `ROLE_ADMIN`

---

## 🧩 How the Dynamic Plugin Architecture Works

### Backend Auto-Discovery

1. **Gradle Subproject Detection**: `settings.gradle` inspects all directories in `plugins/*/server`. If `build.gradle` exists, it registers `:plugins:<plugin-name>:server` as a subproject.
2. **Runtime Classpath Injection**: `gradle/saavy-server.gradle` dynamically attaches all plugin subprojects to `server` as `runtimeOnly` dependencies.
3. **Spring Component & Entity Scanning**: The host application's `@SpringBootApplication` scans `org.saavy.*`. Any beans (`@Service`, `@RestController`, `@Component`) and JPA entities (`@Entity`) inside plugin modules are automatically wired.
4. **Liquibase Migration Inclusion**: `db.changelog-master.xml` includes `<includeAll path="db/changelog/plugins" errorIfMissingOrEmpty="false"/>`, executing all XML changelogs found in plugin resource paths.

### Frontend Auto-Discovery

1. **Vite Glob Manifest Loading**: `client/src/plugins/pluginLoader.js` scans the workspace using:
   ```javascript
   const pluginModules = import.meta.glob('../../plugins/*/client/src/index.js', { eager: true });
   ```
2. **Dynamic Route Registration**: Every plugin's `index.js` exports a `routes` array that is merged into the Vue router at application startup.
3. **Menu Auto-Injection**: Navigation entries defined under `menu` in `index.js` are automatically appended to the sidebar navigation menu in PrimeVue (`AppMenu.vue`).
4. **Dashboard Overrides**: If a plugin exports a `dashboard` component, it seamlessly replaces the default home page dashboard.

### Metadata-Driven Generic UI

Instead of creating manual forms, tables, and dialogs from scratch, you can decorate backend entities with UI annotations:

```java
@Entity
@Table(name = "sample_item")
@UiMaster(
    title = "Sample Items",
    dialogHeader = "Sample Item Details",
    optionLabel = "name",
    masterEndPoint = "sample-items"
)
public class SampleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @Column(nullable = false)
    @UiField(label = "Name", type = "text", required = true, sortable = true, order = 2)
    private String name;

    @Column
    @UiField(label = "Price", type = "number", required = true, sortable = true, order = 3)
    private double price;
}
```

The frontend `<GenericCrud>` component fetches the metadata via `/api/sample-items/meta` and renders the full CRUD table, dialog forms, validation, sorting, filtering, and notifications automatically.

---

## 📖 Step-by-Step Guide: Adding a New Plugin with New Pages

Follow this tutorial to create a new plugin named `inventory-plugin` that adds an **Inventory Management** page.

### Step 1: Create the Plugin Directory Structure

Create the following folder structure under `plugins/inventory-plugin`:

```text
plugins/
└── inventory-plugin/
    ├── client/
    │   └── src/
    │       ├── index.js
    │       └── views/pages/
    │           └── InventoryManagement.vue
    └── server/
        ├── build.gradle
        └── src/main/
            ├── java/org/saavy/
            │   ├── config/
            │   │   └── InventoryEntityRegistryProvider.java
            │   ├── controllers/
            │   │   └── InventoryItemController.java
            │   ├── entity/
            │   │   ├── InventoryItem.java
            │   │   ├── InventoryItemDTO.java
            │   │   └── InventoryItemRepository.java
            │   └── services/
            │       └── InventoryItemService.java
            └── resources/
                └── db/changelog/plugins/
                    └── 02-inventory-plugin-changelog.xml
```

---

### Step 2: Implement the Server Module

#### 1. Plugin Gradle Build (`plugins/inventory-plugin/server/build.gradle`)

```groovy
apply from: file("${projectDir}/../../../gradle/saavy-plugin-server.gradle")
```

#### 2. Entity (`plugins/inventory-plugin/server/src/main/java/org/saavy/entity/InventoryItem.java`)

```java
package org.saavy.entity;

import jakarta.persistence.*;
import lombok.*;
import org.saavy.component.UiField;
import org.saavy.component.UiMaster;

@Entity
@Table(name = "inventory_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@UiMaster(
    title = "Inventory Items",
    dialogHeader = "Inventory Item Details",
    optionLabel = "sku",
    messages = "{\"created\":\"Inventory Item Created\",\"updated\":\"Inventory Item Updated\",\"deleted\":\"Inventory Item Deleted\"}",
    masterEndPoint = "inventory-items"
)
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    @UiField(label = "SKU Code", type = "text", required = true, sortable = true, order = 2)
    private String sku;

    @Column(nullable = false)
    @UiField(label = "Item Name", type = "text", required = true, sortable = true, order = 3)
    private String name;

    @Column(nullable = false)
    @UiField(label = "Quantity", type = "number", required = true, sortable = true, order = 4)
    private Integer quantity;

    @Column(nullable = false)
    @UiField(label = "Unit Price", type = "number", required = true, sortable = true, order = 5)
    private Double unitPrice;
}
```

#### 3. DTO (`plugins/inventory-plugin/server/src/main/java/org/saavy/entity/InventoryItemDTO.java`)

```java
package org.saavy.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryItemDTO {
    private Long id;
    private String sku;
    private String name;
    private Integer quantity;
    private Double unitPrice;
}
```

#### 4. Repository (`plugins/inventory-plugin/server/src/main/java/org/saavy/entity/InventoryItemRepository.java`)

```java
package org.saavy.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
}
```

#### 5. Service (`plugins/inventory-plugin/server/src/main/java/org/saavy/services/InventoryItemService.java`)

```java
package org.saavy.services;

import org.saavy.entity.InventoryItem;
import org.saavy.entity.InventoryItemDTO;
import org.saavy.entity.InventoryItemRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryItemService extends JPAService<InventoryItem, InventoryItemDTO, Long> {

    public InventoryItemService(InventoryItemRepository repository) {
        super(repository, InventoryItem.class, InventoryItemDTO.class);
    }
}
```

#### 6. Controller (`plugins/inventory-plugin/server/src/main/java/org/saavy/controllers/InventoryItemController.java`)

```java
package org.saavy.controllers;

import org.saavy.entity.InventoryItem;
import org.saavy.entity.InventoryItemDTO;
import org.saavy.services.InventoryItemService;
import org.saavy.services.JPAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory-items")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class InventoryItemController extends BaseController<InventoryItem, InventoryItemDTO, Long> {

    @Autowired
    private InventoryItemService inventoryItemService;

    @Override
    protected JPAService<InventoryItem, InventoryItemDTO, Long> getService() {
        return inventoryItemService;
    }
}
```

#### 7. Entity Registry Provider (`plugins/inventory-plugin/server/src/main/java/org/saavy/config/InventoryEntityRegistryProvider.java`)

```java
package org.saavy.config;

import org.saavy.component.EntityRegistryProvider;
import org.saavy.entity.InventoryItem;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InventoryEntityRegistryProvider implements EntityRegistryProvider {

    @Override
    public Map<String, Class<?>> getEntities() {
        return Map.ofEntries(
            Map.entry("inventory-items", InventoryItem.class)
        );
    }
}
```

---

### Step 3: Implement the Client Module

#### 1. Vue Page Component (`plugins/inventory-plugin/client/src/views/pages/InventoryManagement.vue`)

You can either use the metadata-driven `<GenericCrud>` component:

```html
<script setup>
import { onMounted, ref } from 'vue';
import createJpaService from '@core/service/JPAService';
import GenericCrud from '@core/components/GenericCrud.vue';
import ProgressSpinner from 'primevue/progressspinner';

const inventoryService = createJpaService('inventory-items');
const meta = ref({});
const isLoaded = ref(false);

onMounted(async () => {
    try {
        meta.value = await inventoryService.getMasterMeta();
    } catch (e) {
        console.error('Failed to load metadata:', e);
    } finally {
        isLoaded.value = true;
    }
});
</script>

<template>
    <div v-if="isLoaded">
        <GenericCrud
            :title="meta.title || 'Inventory Items'"
            :dialogHeader="meta.dialogHeader || 'Inventory Item Details'"
            :fields="meta.fields || []"
            :service="inventoryService"
            :messages="meta.messages || {}"
        />
    </div>
    <div v-else class="card flex justify-content-center p-5">
        <ProgressSpinner />
    </div>
</template>
```

> [!NOTE]
> You can also create completely custom Vue pages with custom forms, charts, or tables and connect them to `/api/inventory-items` using `createJpaService('inventory-items')` or standard Axios calls.

#### 2. Plugin Entrypoint Manifest (`plugins/inventory-plugin/client/src/index.js`)

```javascript
import InventoryManagement from './views/pages/InventoryManagement.vue';

export default {
    name: 'inventory-plugin',
    routes: [
        {
            path: '/pages/inventory-items',
            name: 'inventory-management',
            component: InventoryManagement,
            meta: { breadcrumb: ['Pages', 'Inventory Management'] }
        }
    ],
    menu: [
        {
            label: 'Inventory Management',
            items: [
                {
                    label: 'Inventory Stock',
                    icon: 'pi pi-fw pi-box',
                    to: '/pages/inventory-items'
                }
            ]
        }
    ]
};
```

---

### Step 4: Generate or Write Database Migrations

#### Option A: Automatic Liquibase Diff Generation

You can generate the migration changelog automatically from your JPA entities:

```bash
./gradlew :plugins:inventory-plugin:server:liquibaseJpaDiffChangeLog
```

#### Option B: Manual Liquibase Changelog

Create `plugins/inventory-plugin/server/src/main/resources/db/changelog/plugins/02-inventory-plugin-changelog.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.27.xsd">

    <changeSet author="saavy" id="inventory-plugin-001">
        <createTable tableName="inventory_item">
            <column autoIncrement="true" name="id" type="BIGINT">
                <constraints nullable="false" primaryKey="true" primaryKeyName="inventory_item_pk"/>
            </column>
            <column name="sku" type="VARCHAR(255)">
                <constraints nullable="false" unique="true" uniqueConstraintName="uc_inventory_sku"/>
            </column>
            <column name="name" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <column name="quantity" type="INT">
                <constraints nullable="false"/>
            </column>
            <column name="unit_price" type="FLOAT(53)">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

---

### Step 5: Run and Verify

1. Restart the Spring Boot backend (`./gradlew bootRun`). Liquibase will automatically execute the new changelog on startup.
2. In the Vue frontend (`npm run dev`), notice that:
   - The **Inventory Management** menu item automatically appears in the sidebar.
   - Navigating to `/pages/inventory-items` displays the full CRUD interface.
   - Creating, editing, sorting, and deleting items works immediately end-to-end.

---

## 🛠 Common Gradle & NPM Tasks

| Task / Command | Scope | Description |
|---|---|---|
| `./gradlew bootRun` | Root | Runs Spring Boot host server with all active plugins mounted |
| `./gradlew bootRunDebug` | Root | Runs Spring Boot with JVM remote debugger on port `5005` |
| `./gradlew serve` | Root | Starts the Vite development server for the frontend |
| `./gradlew buildClient` | Root | Compiles and builds the production frontend bundle into `dist/` |
| `./gradlew clean` | Root | Cleans build directories across host and all plugins |
| `./gradlew build` | Root | Builds combined backend jar with embedded frontend |
| `cd client && npm run dev` | Client | Starts Vite hot-reload development server |
| `cd client && npm run build` | Client | Builds production assets |
| `cd client && npm run lint` | Client | Runs ESLint and Prettier checks |

---

## 📦 Production Build & Docker Deployment

### Single-JAR Deployment

The Gradle build process automatically bundles the compiled Vue frontend into Spring Boot's `static/` resources directory:

```bash
# Build the single deployable executable JAR:
./gradlew build -x test
```

The output JAR will be generated at `server/build/libs/server-0.0.1-SNAPSHOT.jar`. Run it with:

```bash
java -jar server/build/libs/server-0.0.1-SNAPSHOT.jar --spring.profiles.active=production
```

### Full Docker Compose Deployment

To build and run both the database and the full-stack containerized application:

```bash
docker-compose up --build -d
```

Access the production application at `http://localhost:8080`.
