# ============================================================================
# SAAVYBOOTVUE: ROADMAP, TODO & DOMAIN ARCHITECTURE PLAN
# ============================================================================
# Vision: Transform SaavyBootVue into an AI-Native, Modular Monolith (Modulith)
# where loosely coupled domain micro-plugins (Party, Invoicing, Properties, etc.)
# provide production-ready enterprise capabilities with zero compile-time friction.
# ============================================================================

## 📌 HIGH PRIORITY DOMAIN IMPLEMENTATION PLANS

---

### 1. 🏢 Party Plugin Extraction (`plugins/party-plugin`)
**Status**: [ ] Planned / Ready for Execution  
**Goal**: Extract the `Party` domain (Customers, Vendors, Contacts, Addresses) from `plugins/invoice-plugin` into an independent bounded context module, decoupling the `invoice-plugin` using UI metadata references (`optionsEndpoint = "parties"`).

#### Step-by-step Execution Plan:
- [ ] **1.1 Server Structure & Entity Migration**
  - Create `plugins/party-plugin/server/src/main/java/com/saavy/party/`:
    - `entity/Party.java` (Domain entity with `@UiMaster(title = "Party Management", optionLabel = "name")` and `@Table(name = "party")`).
    - `entity/PartyType.java` (Enum: `CUSTOMER`, `VENDOR`, `BOTH`).
    - `dto/PartyDTO.java` (UI DTO with `@UiField` annotations).
    - `repository/PartyRepository.java` (Extends `BaseJpaRepository<Party, Long>`).
    - `service/PartyService.java` (Extends `JPAService<Party, PartyDTO, Long>`).
    - `controller/PartyController.java` (REST controller at `/api/parties`).
    - `config/PartyPluginConfiguration.java` (Spring `@Configuration`).
    - `config/PartyEntityRegistryProvider.java` (Registers `Party` / `PartyDTO` in `EntityRegistryProvider`).
- [ ] **1.2 Database Changelog Migration**
  - Create `plugins/db/changelog/party-plugin/01-party-changelog.xml` with table creation & seed demo data (Sample Customers & Vendors).
  - Register `party-plugin/01-party-changelog.xml` in `db.changelog-master.xml`.
- [ ] **1.3 Client UI Component & Manifest**
  - Create `plugins/party-plugin/client/src/views/pages/PartyManagement.vue` using `<GenericCrud entityName="parties" />`.
  - Create `plugins/party-plugin/client/src/index.js` with menu route `/party-management` under Master Data / CRM menu.
- [ ] **1.4 Decouple `invoice-plugin`**
  - Remove `Party*.java` and `PartyManagement.vue` from `plugins/invoice-plugin`.
  - In `Invoice.java`, keep `partyId` (Long) + `counterpartyName` (String snapshot).
  - In `CustomerInvoiceDTO.java` / `VendorInvoiceDTO.java`, declare:
    ```java
    @UiField(
        label = "Party / Customer",
        type = FieldType.MANY_TO_ONE,
        optionsEndpoint = "parties",
        optionLabel = "name",
        optionValue = "id"
    )
    private Long partyId;
    ```
- [ ] **1.5 Plugin Registry & Verification**
  - Register `party-plugin` in `PluginConfigService.java` (`initKnownPlugins()`).
  - Run `./gradlew compileJava` to verify 0 compilation errors across all modules.

---

### 2. ⚙️ Properties & Settings Registry Plugin (`plugins/properties-plugin`)
**Status**: [ ] Planned  
**Goal**: Provide a dynamic, database-backed Key-Value / Master Data & Lookup Registry for runtime system settings, tenant configurations, and shared picklists without server restarts.

#### Step-by-step Execution Plan:
- [ ] **2.1 Core Property Entity & DTO**
  - Create `plugins/properties-plugin/server/src/main/java/com/saavy/properties/`:
    - `entity/SystemProperty.java` (`id`, `category`, `propertyKey`, `propertyValue`, `label`, `dataType`, `description`, `isEncrypted`, `isActive`).
    - `dto/SystemPropertyDTO.java` with `@UiMaster(title = "System Properties & Master Data")`.
    - `repository/SystemPropertyRepository.java` with query methods:
      - `findByCategoryAndIsActiveTrue(String category)`
      - `findByPropertyKey(String propertyKey)`
- [ ] **2.2 High-Performance Caching & Access Service**
  - Implement `PropertyRegistryService`:
    - In-memory cache (Spring `@Cacheable("properties")` or Caffeine Cache) for sub-millisecond lookups.
    - Helper methods: `getString(key)`, `getInteger(key)`, `getBigDecimal(key)`, `getBoolean(key)`.
    - Cache eviction upon update/save (`@CacheEvict`).
  - **⚡ Redis Consideration (Distributed Caching & Real-Time Sync)**:
    - *Why it's a great move*: In multi-node deployments (load-balanced containers), updating a setting on Node A needs immediate cache invalidation on Node B.
    - *L1/L2 Tiered Cache*: Local Caffeine (L1: nanosecond lookups) + Redis (L2: distributed sync across nodes).
    - *Redis Pub/Sub*: Broadcast property updates to all running nodes & live Vue clients via WebSockets.
    - *Graceful Fallback*: Automatically use local in-memory caching if Redis host is not configured (`@ConditionalOnProperty`).
    - *Atomic Counters*: Use Redis `INCR` for atomic document/invoice sequence generation (e.g. `INV-2026-0001`).
- [ ] **2.3 Dynamic Picklist REST API**
  - Implement `/api/properties/lookup?category={category}` returning `{ value: propertyKey, label: label }` pairs.
  - Allows any plugin to populate dynamic dropdowns:
    ```java
    @UiField(
        label = "Payment Terms",
        type = FieldType.DROPDOWN,
        optionsEndpoint = "properties/lookup?category=PAYMENT_TERMS"
    )
    private String paymentTerms;
    ```
- [ ] **2.4 Client UI Screen**
  - Create `plugins/properties-plugin/client/src/views/pages/PropertyManagement.vue` using `<GenericCrud entityName="system-properties" />` grouped by category.
  - Manifest `index.js` registering menu item under **System Settings**.
- [ ] **2.5 Liquibase Seed Data**
  - Seed default categories: `PAYMENT_TERMS` (Net 15, Net 30, Due Upon Receipt), `TAX_TYPES` (Standard VAT 12%, Zero-Rated, Exempt), `CURRENCIES` (USD, EUR, PHP, SGD).

---

### 3. 📈 Profit & Loss Plugin (`plugins/pnl-plugin`)
**Status**: [ ] Planned  
**Goal**: MVP for financial reporting and analytics. Allows businesses to upload generic financial CSV files, persists structured P&L line entries, and provides interactive analytics and SPI Dashboard widgets.

#### Step-by-step Execution Plan:
- [ ] **3.1 Domain Entity & DTO Structure**
  - Create `plugins/pnl-plugin/server/src/main/java/com/saavy/pnl/`:
    - `entity/PnlRecord.java`:
      - `id` (Long, PK)
      - `period` (LocalDate / YearMonth, e.g. `2026-01-01`)
      - `type` (Enum: `REVENUE`, `COGS`, `OPERATING_EXPENSE`, `TAX`, `OTHER_INCOME`)
      - `category` (String, e.g. "SaaS Subscriptions", "Salaries", "AWS Cloud Hosting")
      - `amount` (BigDecimal, signed or unsigned based on type)
      - `currency` (String, default "USD")
      - `sourceFileName` (String, for audit trail)
      - `uploadBatchId` (UUID, allows reverting/replacing a specific CSV upload)
      - `notes` (String)
    - `dto/PnlRecordDTO.java` with `@UiMaster(title = "Profit & Loss Ledger")`.
    - `repository/PnlRecordRepository.java` with custom aggregation queries:
      - `findMonthlySummary(LocalDate from, LocalDate to)`
      - `findExpenseBreakdownByCategory(LocalDate from, LocalDate to)`
- [ ] **3.2 CSV Upload & Ingestion Engine**
  - Standard CSV Template (`period,type,category,amount,currency,notes`):
    ```csv
    period,type,category,amount,currency,notes
    2026-01-01,REVENUE,Product Sales,45000.00,USD,January gross revenue
    2026-01-01,COGS,Server Hosting,3200.00,USD,AWS & Cloudflare
    2026-01-01,OPERATING_EXPENSE,Payroll,18000.00,USD,Core engineering team
    ```
  - REST Endpoint: `POST /api/pnl/upload-csv` (MultipartFile) with validation and batch insert.
  - Template Download: `GET /api/pnl/template-csv` (Provides ready-to-use CSV template for users).
- [ ] **3.3 Financial Analytics & Summary REST API**
  - `GET /api/pnl/summary?year=2026`: Returns computed Net Profit, Gross Margin, Total Revenue, and Total Expenses.
  - `GET /api/pnl/category-breakdown?period=2026-01`: Returns category distribution for charts.
- [ ] **3.4 SPI Dashboard Widgets (`PnlDashboardWidgetProvider`)**
  - KPI Stat Cards: "Net Profit (YTD)", "Total Revenue (YTD)", "Gross Profit Margin %".
  - Multi-Bar / Line Chart: Monthly Revenue vs. Total Operating Expenses.
  - Doughnut Chart: Expense Breakdown by Category.
- [ ] **3.5 Client UI & Ingestion Screen**
  - Create `plugins/pnl-plugin/client/src/views/pages/PnlManagement.vue`:
    - Drag-and-drop CSV FileUpload component with template download button.
    - Filterable data table with summary row totals (Total Revenue, Total Expense, Net Income).
  - Register route `/pnl-management` in `index.js` under **Financial Reports** menu.
- [ ] **3.6 Liquibase Changelog & Seed Data**
  - Create `plugins/db/changelog/pnl-plugin/01-pnl-changelog.xml` with `pnl_record` table and sample monthly financial data.

---

### 4. ⏱️ Attendance & Bundy Clock Plugin with QR Codes (`plugins/attendance-plugin`)
**Status**: [ ] Planned  
**Goal**: Digital Bundy Clock and Employee Time & Attendance tracking system with QR code badge scanning, Kiosk mode, real-time timesheets, and HR dashboard widgets.

#### Step-by-step Execution Plan:
- [ ] **4.1 Domain Entity & DTO Structure**
  - Create `plugins/attendance-plugin/server/src/main/java/com/saavy/attendance/`:
    - `entity/AttendanceLog.java`:
      - `id` (Long, PK)
      - `employeeId` (Long or String reference to Party / Employee)
      - `employeeName` (String snapshot)
      - `timestamp` (LocalDateTime)
      - `logType` (Enum: `CLOCK_IN`, `CLOCK_OUT`, `BREAK_OUT`, `BREAK_IN`)
      - `verificationMethod` (Enum: `QR_CODE`, `MANUAL_ADMIN`, `PIN`)
      - `kioskDeviceId` (String / IP Address)
      - `latitude` / `longitude` (Optional geolocation)
      - `status` (Enum: `ON_TIME`, `LATE`, `EARLY_DEPARTURE`, `OVERTIME`)
      - `notes` (String)
    - `entity/EmployeeBadge.java`:
      - `id` (Long, PK)
      - `employeeId` (Long)
      - `qrToken` (Unique HMAC token or UUID for QR code encoding)
      - `pinCode` (Optional fallback 4-digit PIN)
      - `isActive` (Boolean)
    - `dto/AttendanceLogDTO.java` with `@UiMaster(title = "Attendance Logs & Timesheets")`.
    - `repository/AttendanceLogRepository.java` with daily/monthly shift aggregation queries.
- [ ] **4.2 QR Code Engine & Verification Service**
  - Integrate `zxing` (Zebra Crossing) for backend QR code badge generation:
    - `GET /api/attendance/badges/{employeeId}/qr`: Returns PNG/SVG QR code image for badge printing.
  - REST Endpoint `POST /api/attendance/scan-qr`:
    - Decodes QR payload / token.
    - Determines auto-action (e.g. if last action was `CLOCK_IN`, default next action to `CLOCK_OUT` or `BREAK_OUT`).
    - Calculates late minutes / shift duration.
    - Returns instant employee greeting + time confirmation.
- [ ] **4.3 Fullscreen Bundy Clock Kiosk UI (`BundyClockKiosk.vue`)**
  - High-visibility digital wall clock with live date/time.
  - Camera QR scanner using `html5-qrcode` (front/back camera selection).
  - Audio chime/beep + visual green/red feedback for successful clock-ins.
  - Anti-passback & debounce protection (prevents accidental double-scanning within 60 seconds).
  - Quick manual PIN entry fallback tab.
- [ ] **4.4 HR Admin Timesheets & Badge Management**
  - `AttendanceManagement.vue`: Filterable timesheet table with daily present/absent summary, shift hours computation, and CSV/Excel export.
  - `BadgeGenerator.vue`: Printable employee ID badge cards with company logo and QR codes.
- [ ] **4.5 SPI Dashboard Widgets (`AttendanceDashboardWidgetProvider`)**
  - KPI Stat Cards: "Present Today", "Late Arrivals", "On Break", "Absent".
  - Real-Time Live Feed Widget: Scrolling list of recent clock-ins/outs.
- [ ] **4.6 Liquibase Changelog & Seed Data**
  - Create `plugins/db/changelog/attendance-plugin/01-attendance-changelog.xml` with attendance tables and demo employee badges.

---

### 5. 🔄 Workflow & Automation Orchestration Plugin (`plugins/workflow-plugin`)
**Status**: [ ] Planned  
**Goal**: Act as the central business process & automation conductor. Listens to domain events across plugins (Invoices, Attendance, P&L, Party), evaluates configurable business rules (Trigger-Condition-Action), and manages human approval tasks without tight cross-plugin coupling.

#### Step-by-step Execution Plan:
- [ ] **5.1 Domain Event Bus & Unified Contract**
  - Create `DomainEvent.java` base class in core/shared SPI:
    - `eventName` (e.g. `INVOICE_CREATED`, `INVOICE_PAID`, `ATTENDANCE_LATE`, `PARTY_CREATED`)
    - `sourcePlugin` (String)
    - `entityId` (String/Long)
    - `payload` (Map<String, Object> or JSON string)
    - `timestamp` (LocalDateTime)
- [ ] **5.2 Workflow Entities & Schema**
  - Create `plugins/workflow-plugin/server/src/main/java/com/saavy/workflow/`:
    - `entity/WorkflowDefinition.java`:
      - `id` (Long, PK)
      - `name` (String, e.g. "Over $5,000 Invoice Approval", "Late Attendance Alert")
      - `triggerEvent` (String, e.g. `INVOICE_CREATED`)
      - `conditionExpression` (SpEL or JSON rule: `#payload.amount > 5000`)
      - `actionType` (Enum: `REQUIRE_APPROVAL`, `SEND_NOTIFICATION`, `CALL_REST_ENDPOINT`, `RECORD_PNL_ENTRY`)
      - `actionConfig` (JSON configuration for the action)
      - `isActive` (Boolean)
    - `entity/WorkflowExecution.java`:
      - Audit trail (`id`, `definitionId`, `status` [PENDING, RUNNING, COMPLETED, FAILED, WAITING_APPROVAL], `triggerPayload`, `executionLogs`, `startedAt`, `completedAt`)
    - `entity/WorkflowTask.java` (Human Approvals):
      - `id`, `executionId`, `assignedRole` (e.g. `ROLE_MANAGER`), `title`, `status` [PENDING, APPROVED, REJECTED], `approverComments`, `resolvedAt`
    - `repository/` and `dto/` classes for all workflow entities.
- [ ] **5.3 Workflow Execution Engine (`WorkflowEngineService`)**
  - Listen for domain events using Spring `@EventListener(DomainEvent.class)`.
  - Evaluate conditions dynamically using Spring Expression Language (SpEL).
  - Execute registered `WorkflowActionHandler` beans:
    - `NotificationActionHandler` (In-app alert / Email)
    - `TaskApprovalActionHandler` (Generates pending task in user inbox)
    - `RestEndpointActionHandler` (Invokes another plugin's REST API asynchronously)
- [ ] **5.4 Client UI Screens & Task Inbox**
  - Create `plugins/workflow-plugin/client/src/views/pages/`:
    - `WorkflowDefinitions.vue`: UI to manage, enable/disable, and configure trigger-condition-action rules.
    - `TaskInbox.vue`: User approval inbox for pending review tasks with one-click **Approve** / **Reject** buttons.
    - `WorkflowAuditLogs.vue`: Real-time execution log viewer showing successes, failures, and execution timings.
  - Manifest `index.js` registering menu routes under **Workflows & Approvals**.
- [ ] **5.5 SPI Dashboard Widgets (`WorkflowDashboardWidgetProvider`)**
  - KPI Stat Cards: "Pending Approvals" (highlighted with badge if > 0), "Automations Run Today".
  - Real-Time Activity Widget: Feed of recent automated actions and approvals.
- [ ] **5.6 Liquibase Changelog & Seed Data**
  - Create `plugins/db/changelog/workflow-plugin/01-workflow-changelog.xml` with tables for definitions, executions, tasks, and sample default automation rules.

## 🚀 PROMPT-TO-SOFTWARE ENGINE & AI ASSISTANT ROADMAP

### [ ] 1. CONTEXT ENGINEERING & AI RULE SYSTEM
- [ ] **1.1 Create Master AI System Prompt (`.aiassistant/AI_SYSTEM_PROMPT.md`)**
  - Define strict architectural boundaries (Host Shell vs Dynamic Plugins).
  - Enforce standard directory conventions: `plugins/[name]-plugin/(client/ + server/)`.
  - Establish coding standards: DTO patterns, ModelMapper, BaseController inheritance.
- [ ] **1.2 Create `.cursorrules` & `AGENT.md` for IDE AI Assistants**
  - Target Cursor, Windsurf, GitHub Copilot Workspace, Antigravity, and Claude Code.
  - Direct AI to never alter core server/client when creating new domain modules.
- [ ] **1.3 Create UI Field Annotation Dictionary & Specification**
  - Document all `@UiField` types: `text`, `number`, `date`, `datetime`, `checkbox`, `enum`, `textarea`, `manyToOne`, `dropdown`.
  - Document `@UiMaster` properties: `title`, `dialogHeader`, `optionLabel`, `messages`, `masterEndPoint`.
  - Document `@UiDetail` properties: `key`, `title`, `parentField`, `parentValue`, `messages`, `detailEndpoint`.

---

### [ ] 2. GOLDEN CODE RECIPES & TEMPLATES (`.aiassistant/templates/`)
- [ ] **2.1 Single-Entity CRUD Recipe**
  - Java Entity with `@UiMaster` & `@UiField` annotations.
  - DTO with validation & serialization.
  - Repository extending `BaseJpaRepository<E, ID>`.
  - Service extending `JPAService<E, D, ID>`.
  - Controller extending `BaseController<E, D, ID>` with `@PreAuthorize`.
  - Liquibase XML changelog changeset with preConditions.
  - `EntityRegistryProvider` SPI registration.
  - Vue 3 screen using `<GenericCrud>`.
  - Plugin manifest `index.js` with routes and menu items.
- [ ] **2.2 Master-Detail (Parent-Child) Hierarchy Recipe**
  - Parent Entity + DTO with `@UiDetail` & `@JsonManagedReference`.
  - Child Entity + DTO with `@JsonBackReference` & parent foreign key.
  - Liquibase foreign key constraint changeset.
  - Vue 3 screen using `<GenericMasterDetail>` (modal / tabbed / embedded layouts).
- [ ] **2.3 Enum & Reference Options Recipe**
  - Java Enum with `getEnumOptions` JSON helper.
  - `@UiField(type = "enum", enumOptions = Status.getEnumOptions)`.
  - Dropdown / Select UI rendering.
- [ ] **2.4 File Upload & Attachment Recipe**
  - BaseFile entity inheritance for PDF, images, invoices.
  - `@UiField(type = "file", fileNameField = "fileName", contentTypeField = "contentType")`.
  - File streaming / download REST endpoints.

---

### [ ] 3. AUTOMATED SCAFFOLDING & BUILD TASKS
- [ ] **3.1 Plugin Generator Gradle Task (`gradle createPlugin`)**
  - Command: `./gradlew createPlugin -Pname=warehouse`
  - Generates `plugins/warehouse-plugin/` directory structure.
  - Creates boilerplate `build.gradle`, `index.js`, and package directories.
- [ ] **3.2 Automated Compile & Lint Verification Task (`gradle verifyPlugins`)**
  - Compiles all subprojects and tests dynamic frontend bundling in one check.
- [ ] **3.3 Liquibase Automated Diff Integration**
  - Task: `./gradlew :plugins:[name]-plugin:server:liquibaseJpaDiffChangeLog`
  - Auto-generates changelogs from Hibernate entity diffs directly into the plugin.

---

### [ ] 4. DOMAIN ARCHETYPES & FUTURE EXPANSION
- [ ] **4.1 Warehouse & Inventory Management (`plugins/inventory-plugin`)**
  - Entities: Item, Warehouse, Bin Location, Stock Transfer, Stock Adjustment.
- [ ] **4.2 Human Resources & Payroll (`plugins/hr-plugin`)**
  - Entities: Employee, Department, Position, Leave Request, Payroll Run.
- [ ] **4.3 Point of Sale / Retail (`plugins/pos-plugin`)**
  - Entities: Cash Register, Till Session, POS Receipt, Payment Line.
- [ ] **4.4 Domain Events & Messaging (`core/events`)**
  - Spring `ApplicationEventPublisher` for asynchronous domain triggers (e.g. `InvoicePaidEvent` -> updates Account Balance).

---

### [ ] 5. SECURITY, RBAC & MULTI-TENANCY GUIDELINES
- [ ] **5.1 Role-Based Route Gating in Plugin Manifests**
  - Extend `index.js` with `roles: ['ROLE_ADMIN', 'ROLE_MANAGER']`.
  - Auto-hide navigation items if user lacks required authorities.
- [ ] **5.2 Multi-Tenant Data Isolation Pattern**
  - `@TenantId` / Organization filtering in `JPAService` base queries.

---

## ✅ COMPLETED MILESTONES
- [x] **Dynamic Plugin Auto-Discovery**: Gradle auto-detects and compiles any folder matching `plugins/*-plugin` in `settings.gradle` and `build.gradle`.
- [x] **Cross-Plugin Drop Table Protection**: Safe Liquibase tasks in `saavy-plugin-server.gradle` prevent accidental drops of host or peer tables.
- [x] **GenericCrud Metadata Engine**: Enhanced `<GenericCrud>` & `<GenericForm>` supporting text, number, date, datetime, enum, boolean/checkbox, textarea, and dynamic lookups.
- [x] **Plugin SPI Dashboard Widgets**: Pluggable Stats Cards, Doughnut Charts, and Overdue Tables filtered dynamically when plugins are enabled/disabled.
- [x] **Decoupled Architecture Verification**: Multi-module Gradle build compiles cleanly with zero cross-plugin compile-time dependencies.
