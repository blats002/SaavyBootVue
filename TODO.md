# ============================================================================
# SAAVYBOOTVUE: ROADMAP, TODO & DOMAIN ARCHITECTURE PLAN
# ============================================================================
# Vision: Transform SaavyBootVue into an AI-Native, Modular Monolith (Modulith)
# where loosely coupled domain micro-plugins (Master Data & Topology, Invoicing, Properties, etc.)
# provide production-ready enterprise capabilities with zero compile-time friction.
# ============================================================================

## 📌 HIGH PRIORITY DOMAIN IMPLEMENTATION PLANS

---

### 1. 🌐 Universal Master Data, Asset & Topology Engine (`plugins/master-data-plugin`)
**Status**: [ ] Planned / Ready for Execution  
**Goal**: Build a unified Master Data Management (MDM) & Graph Topology Ontology Engine. Serves as the foundational registry for **Actors (Parties)**, **Things (Assets / Inventory / Network Nodes)**, **Places (Facilities / GeoLocations)**, and the **Dynamic Graph Relationships (Topology Edges)** connecting them. Replaces and supersedes standalone `party-plugin`.

#### Step-by-step Execution Plan:
- [ ] **1.1 Core Entity Polymorphism & Inheritance Architecture**
  - Create `plugins/master-data-plugin/server/src/main/java/com/saavy/masterdata/`:
    - **🏛️ Party Subsystem (Actors & Social Entities):**
      - `entity/party/Party.java` (Abstract / Base Entity with `@Inheritance(strategy = InheritanceType.JOINED)`).
      - `entity/party/Person.java` (First name, last name, DOB, tax ID, gender).
      - `entity/party/GroupOrganization.java` (Legal name, registration number, org type: `COMPANY`, `COMMUNITY`, `DEPARTMENT`, `CLUB`).
      - `entity/party/PartyRole.java` (Role assignments: `CUSTOMER`, `VENDOR`, `EMPLOYEE`, `PARENT`, `CHILD`, `MEMBER`, `DRIVER`).
    - **📦 Asset & Resource Subsystem (Physical, Logical & Inventory Nodes):**
      - `entity/asset/Asset.java` (Base Asset with serial number, asset tag, status: `ACTIVE`, `MAINTENANCE`, `DECOMMISSIONED`).
      - `entity/asset/PhysicalAsset.java` (Cars, fleet vehicles, machinery, buildings, hardware tools).
      - `entity/asset/InventoryItem.java` (SKU, barcode, UOM, reorder thresholds, inventory categorization).
      - `entity/asset/NetworkNode.java` (Routers, switches, servers, IP addresses, MAC addresses, port capacities).
    - **📍 Spatial & GeoLocation Subsystem (Places & Containment):**
      - `entity/facility/Facility.java` (Name, facility type: `WAREHOUSE`, `OFFICE`, `DATA_CENTER`, `STORE`).
      - `entity/facility/GeoLocation.java` (Latitude, Longitude, Altitude, GeoJSON polygon, physical address).
      - `entity/facility/StorageLocation.java` (Aisle, Rack, Shelf, Bin coordinates for warehouse spatial mapping).
- [ ] **1.2 Valuation, Costing & Pricing Engine (COGS & Sales)**
  - `entity/pricing/ValuationProfile.java`:
    - Cost of Goods Sold (`cogsUnitCost`, currency, last purchase price, average landed cost).
    - Sales Pricing (`listPrice`, `wholesalePrice`, `minimumPrice`, `taxCategory`).
  - Associated dynamically with `InventoryItem` and `PhysicalAsset` for real-time P&L calculations.
- [ ] **1.3 Dynamic Graph Topology & Relationship Engine (`EntityRelationship`)**
  - `entity/topology/EntityRelationship.java`:
    - `id` (Long, PK)
    - `sourceEntityType` (e.g. `PARTY`, `ASSET`, `FACILITY`, `NETWORK_NODE`)
    - `sourceEntityId` (Long)
    - `targetEntityType` (e.g. `PARTY`, `ASSET`, `FACILITY`, `NETWORK_NODE`)
    - `targetEntityId` (Long)
    - `relationshipType` (Enum / String: `CONNECTED_TO`, `STORED_IN`, `OWNED_BY`, `ASSIGNED_TO`, `PARENT_OF`, `PART_OF_BOM`, `MANAGED_BY`)
    - `metadataJson` (JSON attributes: e.g. Port: `eth0/1`, Bandwidth: `10Gbps`, Capacity: `500kg`)
    - `validFrom`, `validTo` (Temporal validity)
  - `service/topology/TopologyGraphService.java`:
    - Graph traversal queries (`findConnectedNodes`, `findShortestPath`, `findSubtreeHierarchies`, `findOrphanNodes`).
- [ ] **1.4 Dynamic UI & Graph Visualization Components**
  - Create `plugins/master-data-plugin/client/src/views/pages/`:
    - `PartyManagement.vue`: Split Person & Organization directory with role badges via `<GenericCrud>`.
    - `AssetManagement.vue`: Unified asset catalog (Fleet, Machinery, Inventory SKUs).
    - `TopologyGraphViewer.vue`: Interactive node-and-edge visualizer (powered by Cytoscape.js or Vis.js) supporting network topologies, org charts, and family trees.
    - `WarehouseGeoMap.vue`: Interactive map and warehouse bin locator with Leaflet / OpenStreetMap.
  - Manifest `index.js` registering menu items under **Master Data & Topology**.
- [ ] **1.5 Cross-Plugin Decoupling (`invoice-plugin`, `attendance-plugin`, `pnl-plugin`)**
  - `invoice-plugin`: Points party selectors to `/api/master-data/parties` and item selectors to `/api/master-data/items` with auto-filling COGS and sales prices.
  - `attendance-plugin`: References `Person` (as Employee) from `master-data-plugin`.
  - `pnl-plugin`: Calculates inventory asset depreciation and COGS dynamically from `ValuationProfile`.
- [ ] **1.6 Liquibase Changelog & Seed Data**
  - Create `plugins/db/changelog/master-data-plugin/01-master-data-changelog.xml` with polymorphic tables, foreign keys, and 3 rich demo datasets:
    - *Network Topology Demo:* Core Router ➔ Distribution Switch ➔ Edge Server.
    - *Warehouse Fleet Demo:* Warehouse A ➔ Bin 04 ➔ SKU + Delivery Van ➔ Driver.
    - *Community / Family Demo:* Community Group ➔ Parent ➔ Child.

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
**Status**: [x] Completed  
**Goal**: Digital Bundy Clock and Employee Time & Attendance tracking system with QR code badge scanning, Kiosk mode, real-time timesheets, and HR dashboard widgets.

#### Step-by-step Execution Plan:
- [x] **4.1 Domain Entity & DTO Structure**
  - Created `plugins/attendance-plugin/server/src/main/java/org/saavy/`:
    - `entity/AttendanceLog.java` & `entity/AttendanceLogDTO.java` with `@UiMaster` and `@UiField`.
    - `entity/EmployeeBadge.java` & `entity/EmployeeBadgeDTO.java` with `@UiMaster` and `@UiField`.
    - `reference/AttendanceLogType.java`, `reference/VerificationMethod.java`, `reference/AttendanceStatus.java`.
    - `repository/AttendanceLogRepository.java` & `repository/EmployeeBadgeRepository.java`.
- [x] **4.2 QR Code Engine & Verification Service**
  - `<GenericQRCode.vue>` component with `vue-qrcode` for client-side badge rendering.
  - `<GenericQRReader.vue>` with `vue-qrcode-reader` camera stream and target frame.
  - REST Endpoints:
    - `POST /api/attendance/scan-qr`: Instant punch processing with auto-detect IN vs OUT & anti-passback.
    - `GET /api/attendance/today-summary`: Turnout and tardiness statistics.
    - `GET /api/attendance/live-feed`: Recent 10 clock punches.
- [x] **4.3 Fullscreen Bundy Clock Kiosk UI (`BundyClockKiosk.vue`)**
  - High-visibility digital wall clock with live date/time.
  - Camera QR scanner using `<GenericQRReader>`.
  - Synthesized Web Audio API chime and visual green/orange/red feedback.
  - Debounce protection and fallback manual PIN keypad dialog.
- [x] **4.4 HR Admin Timesheets & Badge Management**
  - `AttendanceManagement.vue`: Filterable timesheet table via `<GenericCrud>`.
  - `BadgeGenerator.vue`: Printable employee ID cards with scannable QR codes rendered with `<GenericQRCode>`.
- [x] **4.5 SPI Dashboard Widgets**
  - `AttendanceDashboardCardProvider`: "Present Today" & "Late Arrivals" metric cards.
  - `AttendanceDashboardTableProvider`: "Today's Live Attendance Feed" table on the main dashboard.
- [x] **4.6 Liquibase Changelog & Seed Data**
  - `plugins/db/changelog/attendance-plugin/01-attendance-changelog.xml` with table schemas and 4 demo employee badges.

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

---

### 6. ⏱️ Attendance & Time-Tracking Plugin Enhancements (`plugins/attendance-plugin`)
**Status**: [ ] Planned / Roadmap  
**Goal**: Elevate the time & attendance module from terminal kiosk punch logging into an enterprise-grade workforce management and payroll-ready suite.

#### Step-by-step Execution Plan:
- [ ] **6.1 Shift Schedule Configuration Table (`WorkShift` Entity)**
  - Create `entity/WorkShift.java` (`id`, `shiftName`, `startTime`, `endTime`, `gracePeriodMinutes`, `isDefault`, `isActive`).
  - Create `dto/WorkShiftDTO.java`, `repository/WorkShiftRepository.java`, `service/WorkShiftService.java`, and controller at `/api/work-shifts`.
  - Associate `WorkShift` with `EmployeeBadge` (per-employee or per-department shift schedule).
  - Update `AttendanceKioskService.java` to compute late/on-time status against the employee's assigned `WorkShift` instead of the hardcoded `09:15 AM` threshold.
- [ ] **6.2 CSV / Excel Timesheet Payroll Export**
  - Add a one-click **"Export Timesheet (Payroll CSV)"** toolbar button in `AttendanceManagement.vue`.
  - Support date-range selection (Pay Period) and generate formatted payroll CSV/Excel containing Employee ID, Name, Department, Date, Clock In, Clock Out, Break Durations, Total Hours Worked, and Status.
- [ ] **6.3 WebSocket / SSE Real-Time Live Feed Broadcast**
  - Integrate Spring WebSocket / Server-Sent Events (SSE) in `AttendanceKioskService` to broadcast punch events instantly on `/topic/attendance-feed`.
  - Update admin screens and SPI Dashboard widgets to reflect live employee check-ins in real time with zero manual page refreshing.

---

### 7. 📄 Reporting, EDI & Dynamic JQL Query Engine (`plugins/report-plugin`)
**Status**: [ ] Planned  
**Goal**: Enterprise reporting and B2B Electronic Data Interchange (EDI) generator. Allows storing dynamic JPQL / JQL queries in the database to aggregate data across multiple plugins (`master-data`, `invoices`, `pnl`, `attendance`), rendering formatted PDF/Excel reports, and generating standard EDIFACT/ANSI X12 data exchange files.

#### Step-by-step Execution Plan:
- [ ] **7.1 Dynamic Query Store & Cross-Plugin JQL Engine**
  - Create `plugins/report-plugin/server/src/main/java/com/saavy/report/`:
    - `entity/ReportDefinition.java`:
      - `id` (Long, PK)
      - `reportCode` (String, unique e.g. `MONTHLY_SALES_SUMMARY`, `EDI_810_INVOICE`)
      - `reportName` (String)
      - `queryType` (Enum: `JPQL`, `NATIVE_SQL`, `JSON_DSL`)
      - `queryPayload` (Text: Stored JPQL query string or JSON DSL)
      - `outputFormat` (Enum: `PDF`, `EXCEL_XLSX`, `CSV`, `EDI_X12`, `EDIFACT`, `XML_UBL`, `JSON`)
      - `templatePayload` (Text: Thymeleaf HTML, Jasper XML, or EDI segment mapping rules)
      - `parametersSchemaJson` (JSON Schema defining expected user inputs, e.g. `$startDate`, `$partyId`)
    - `service/ReportQueryExecutionService.java`:
      - Safely execute database-stored JPQL queries spanning multiple plugins (e.g. `SELECT p.name, i.invoiceNumber, i.totalAmount FROM Invoice i JOIN Party p ON i.partyId = p.id WHERE i.issueDate BETWEEN :start AND :end`).
      - Read-Only query sandbox with SQL-injection prevention and execution timeout limits.
- [ ] **7.2 Multi-Format Document Generation Engine**
  - **📊 Excel / Spreadsheet Generator (`ExcelReportRenderer`):**
    - Apache POI integration generating styled `.xlsx` spreadsheets with headers, numeric formats, and summary formulas.
  - **📑 PDF Generator (`PdfReportRenderer`):**
    - High-fidelity PDF rendering with Thymeleaf + OpenPDF / Flying Saucer or JasperReports.
  - **📋 CSV / TSV Streamer:** High-throughput streaming for million-row datasets without high memory overhead.
- [ ] **7.3 B2B Electronic Data Interchange (EDI) Engine**
  - `service/edi/EdiGeneratorService.java`:
    - **ANSI X12 Standard:** Support for Transaction Sets `810` (Invoice), `850` (Purchase Order), and `856` (Ship Notice / ASN).
    - **UN/EDIFACT Standard:** Support for `INVOIC`, `ORDERS`, `DESADV`.
    - **e-Invoicing UBL 2.1 (XML / PEPPOL):** Standard European/International XML electronic invoice format.
- [ ] **7.4 Scheduled Delivery & Automated Distribution**
  - `entity/ReportSchedule.java` (Cron expression, recipients, distribution channel: `EMAIL`, `SFTP`, `OCI_OBJECT_STORAGE`).
  - Integration with `plugins/workflow-plugin` for automated trigger-based generation (e.g., generate EDI 810 automatically upon `INVOICE_APPROVED`).
- [ ] **7.5 Client UI Screens & PrimeVue DataTable Viewer**
  - Create `plugins/report-plugin/client/src/views/pages/`:
    - `ReportDefinitionManagement.vue`: Visual report builder with interactive JQL / SQL code editor and parameter tester.
    - `ReportViewer.vue`: Interactive report runner powered by **PrimeVue `<DataTable>`**:
      - On-screen tabular display with **server-side multi-column sorting** and **per-column matchMode filters** (contains, startsWith, between dates, equals).
      - Dynamic column toggler (show/hide columns), row totals, and formatted cell renderers (currency, badges, dates).
      - One-click Toolbar Export buttons: `[Download PDF]`, `[Export Excel .xlsx]`, `[Generate EDI]`, `[Export CSV]`.
    - `EdiConfiguration.vue`: Partner EDI identifier setup (ISA/GS sender/receiver IDs, delimiter configs).
  - Manifest `index.js` registering menu items under **Reports & EDI**.
- [ ] **7.6 Liquibase Changelog & Seed Data**
  - Create `plugins/db/changelog/report-plugin/01-report-changelog.xml` with report tables and 3 starter templates:
    - *Cross-Plugin Sales & Customer Aging PDF Report* (Joins `invoices` + `parties`).
    - *Department Payroll & Attendance Excel Summary* (Joins `attendance_log` + `employee`).
    - *Standard ANSI X12 810 EDI Invoice Template*.

---

### 8. 🔄 Generic ETL, File Ingestion & Raw Data Staging Engine (`plugins/etl-plugin`)
**Status**: [ ] Planned  
**Goal**: Universal, schema-agnostic ETL (Extract, Transform, Load) and raw data staging engine. Ingests arbitrary CSV, TSV, Excel, and fixed-width files into a database-backed raw staging ledger using metadata definitions (`FileRawDataDefinition`), enabling asynchronous transformation, validation, and routing into downstream domain plugins (e.g., biometric timesheets $\rightarrow$ HRIS payroll, vendor catalog $\rightarrow$ Master Data inventory, bank statements $\rightarrow$ P&L ledger).

#### Step-by-step Execution Plan:
- [ ] **8.1 Multi-Format File Raw Data Definition & Path Mapping Subsystem**
  - Create `plugins/etl-plugin/server/src/main/java/com/saavy/etl/`:
    - `entity/FileRawDataDefinition.java`:
      - `id` (Long, PK)
      - `definitionCode` (String, unique e.g. `BIOMETRIC_TIMESHEET_CSV`, `BANK_TRANSACTION_XML`, `SUPPLIER_PRICE_YAML`, `LEGACY_JSON_DUMP`)
      - `definitionName` (String)
      - `fileType` (Enum: `CSV`, `TSV`, `EXCEL_XLSX`, `JSON`, `XML`, `YAML`, `FIXED_WIDTH`)
      - `rootRecordPath` (String: JSONPath / XPath query to extract record array, e.g. `$.data.invoices[*]` for JSON/YAML, or `//Envelope/Body/Item` for XML)
      - `delimiter`, `quoteChar`, `escapeChar`, `skipHeaderRows` (Integer, default 1 for flat files)
      - `targetConsumerPlugin` (String e.g. `attendance-plugin`, `hris-plugin`, `pnl-plugin`, `master-data-plugin`)
      - `isAutoProcess` (Boolean: automatically trigger transformation after ingestion)
    - `entity/FileColumnMapping.java`:
      - `definitionId` (Long, FK)
      - `mappingStrategy` (Enum: `BY_HEADER_NAME`, `BY_COLUMN_INDEX`, `BY_JSON_PATH`, `BY_XPATH`, `BY_FIXED_WIDTH_OFFSET`)
      - `sourceIdentifier` (String: e.g. `"Employee_ID"` [Header], `"0"` [Index], `"profile.firstName"` [JSONPath], `"./ns:TaxTotal/ns:TaxAmount"` [XPath], `"0:10"` [Offset:Length])
      - `targetFieldName` (String: e.g. `"employeeBadgeNumber"`)
      - `dataType` (Enum: `STRING`, `INTEGER`, `BIGDECIMAL`, `LOCAL_DATE`, `LOCAL_DATETIME`, `BOOLEAN`)
      - `dateFormat` (String: e.g. `"yyyy-MM-dd HH:mm:ss"`, `"MM/dd/yyyy"`, ISO-8601)
      - `isRequired` (Boolean), `defaultValue` (String)
- [ ] **8.2 Two-Stage Ingestion & Raw Data Staging Store**
  - `entity/RawDataBatch.java`:
    - `id` (UUID / Long, PK)
    - `definitionId` (Long, FK)
    - `originalFileName` (String)
    - `totalRows` (Integer), `successfulRows` (Integer), `failedRows` (Integer)
    - `status` (Enum: `UPLOADED`, `INGESTED`, `PROCESSING`, `COMPLETED`, `PARTIAL_FAILURE`, `FAILED`, `REVERTED`)
    - `uploadedBy` (String), `uploadedAt` (LocalDateTime)
  - `entity/RawDataRecord.java`:
    - `id` (Long, PK), `batchId` (UUID/Long, FK), `rowIndex` (Integer)
    - `rawJsonPayload` (PostgreSQL JSONB: stores extracted key-value pairs or column array)
    - `status` (Enum: `PENDING`, `TRANSFORMED`, `ERROR`, `SKIPPED`)
    - `errorMessage` (Text: validation or parsing error details)
    - `targetEntityId` (String/Long: ID of the generated domain entity for traceability)
- [ ] **8.3 Pluggable Data Pipeline & Transformation Router (`EtlDataConsumer` SPI)**
  - Define core interface `EtlDataConsumer`:
    ```java
    public interface EtlDataConsumer {
        String getTargetPluginName();
        boolean supportsDefinition(String definitionCode);
        TransformationResult processBatch(RawDataBatch batch, List<RawDataRecord> records);
    }
    ```
  - Implement Consumer Handlers across domain plugins:
    - `BiometricAttendanceConsumer` $\rightarrow$ parses raw clock logs into `AttendanceLog` records and generates timesheets.
    - `PnlTransactionConsumer` $\rightarrow$ transforms bank statements into `PnlRecord` financial entries.
    - `SupplierItemCatalogConsumer` $\rightarrow$ transforms vendor price lists into `InventoryItem` and `ValuationProfile` in `master-data-plugin`.
- [ ] **8.4 Optical & AI Document Parsing (PDF, Scanned Invoices & Image Receipts)**
  - `service/optical/DocumentParserService.java`:
    - **Digital PDF Invoices:** Apache PDFBox / Tabula for direct tabular text and line item extraction from native PDF invoices.
    - **Scanned Invoices & Paper Receipts (OCR & Vision AI):**
      - Local on-premise OCR via Tesseract (Tess4J) for offline character recognition.
      - Pluggable Multimodal Vision LLM connector (Gemini Flash / OpenAI Vision / Claude / Local Ollama) to extract complex unstructured receipts/invoices into structured JSON:
        ```json
        {
          "invoiceNumber": "INV-2026-9081",
          "vendorName": "Acme Industrial Supplies",
          "issueDate": "2026-09-10",
          "lineItems": [
            { "description": "Hydraulic Valve 20mm", "qty": 4, "unitPrice": 120.00, "total": 480.00 }
          ],
          "subtotal": 480.00, "taxAmount": 57.60, "grandTotal": 537.60
        }
        ```
    - Extracted JSON is piped directly into standard `RawDataRecord` (JSONB) for instant visual preview, human verification, and one-click conversion into `invoice-plugin`!
- [ ] **8.5 Asynchronous Batch Processing, Replay & Error Quarantine**
  - Fast, chunked streaming ingestion (OpenCSV / FastCSV / Apache POI Streaming) to handle 100,000+ rows without JVM memory strain.
  - **🏛️ Legacy System Data Migration & Safe Rollback:**
    - Safely migrate historical databases (from old MySQL, QuickBooks, SAP, Access, or Excel sheets) into `SaavyBootVue`.
    - **One-Click Batch Revert / Undo:** If legacy data was mapped incorrectly, the system safely deletes the created domain entities and resets the batch with zero database corruption.
    - **External Reference Tracking:** Retains legacy system primary keys (`legacyId`) in the audit payload for full traceability.
  - **Replay & Reprocess:** Ability to re-run transformations on existing `RawDataBatch` if downstream business logic changes with zero file re-uploads.
  - **Error Quarantine & Inline Correction:** Failed rows are highlighted in UI; users can correct values inline and click **"Retry Failed Rows"**.
- [ ] **8.6 Client UI Screens & Staging Dashboard**
  - Create `plugins/etl-plugin/client/src/views/pages/`:
    - `EtlDefinitionManager.vue`: Drag-and-drop column mapper linking CSV headers/indexes to target fields with instant preview.
    - `EtlIngestionHub.vue`: Universal file dropzone supporting CSV, Excel, XML, JSON, YAML, PDF, and PNG/JPG invoice images with real-time processing progress bar.
    - `RawBatchInspector.vue`: Interactive PrimeVue DataTable inspecting raw staged data, error logs, and one-click `[Reprocess Batch]` / `[Revert Batch]` actions.
  - Manifest `index.js` registering menu items under **Data Ingestion (ETL)**.
- [ ] **8.7 Liquibase Changelog & Seed Data**
  - Create `plugins/db/changelog/etl-plugin/01-etl-changelog.xml` with batch, record, and definition tables with JSONB indexes.
  - Seed 5 default definitions: *Invoice PDF/Image AI Parser*, *Legacy Customer & Vendor Migration CSV*, *Biometric Time Clock CSV*, *Bank Statement CSV*, *Inventory SKU Batch Upload*.
- [ ] **8.8 🔄 Refactor & Migrate `pnl-plugin` Ingestion Foundation into `etl-plugin`**
  - **Existing Codes to Pull & Generalize**:
    - `PnlCsvIngestionService.java` $\rightarrow$ Generalize the RFC-4180 quotation parsing, streaming row iteration, and batch validation logic into generic `EtlCsvParserService`.
    - `PnlIngestionDTO`, `PnlIngestionBatchDTO`, `PnlIngestionResultDTO` $\rightarrow$ Unify into standard `RawDataBatchDTO` / `RawDataRecordDTO` contracts.
    - `GenericFileUploadDialog.vue` $\rightarrow$ Integrate directly with `EtlIngestionHub.vue` or keep as the shared lightweight dropzone component.
    - Dynamic Entity Auto-Registration Pattern (auto-creating unrecognized Chart of Accounts categories/codes) $\rightarrow$ Generalize into an optional `AutoRegisterMissingReferenceData` strategy on `FileColumnMapping`.
  - **Downstream Decoupling**:
    - Implement `PnlTransactionConsumer implements EtlDataConsumer` inside `pnl-plugin`.
    - Route `/api/pnl/ingest/csv` to submit raw data batches to `etl-plugin`, giving P&L financial data full audit staging, error quarantine, and one-click batch revert capabilities.

---

## 🛡️ PRODUCTION HARDENING & ENTERPRISE INFRASTRUCTURE ROADMAP

### [ ] 1. SECURITY & AUTHENTICATION HARDENING
- [ ] **1.1 Refresh Token Rotation & Session Revocation**
  - Implement short-lived JWT Access Tokens (15 mins) + secure HTTP-Only Refresh Tokens (7 days).
  - Persist refresh tokens in database with device fingerprinting and instant revocation/logout capabilities.
- [ ] **1.2 Rate Limiting & Brute-Force Defense**
  - Implement Bucket4j or Redis-backed rate limiting on authentication endpoints (`/api/auth/login`, `/api/auth/register`, `/api/auth/forgot-password`).
  - IP-based and user-based throttling to defend against DDoS and brute-force credential stuffing.
- [ ] **1.3 RFC 7807 Standardized API Error Handling**
  - Migrate global error handling to Spring 6's standard `ProblemDetail` specification.
  - Uniform, predictable JSON error payloads across all host and plugin REST APIs without leaking sensitive stack traces.
- [ ] **1.4 Self-Service Password Reset & Account Verification**
  - Secure time-limited cryptographic token generation with SHA-256 hashing.
  - Email notification provider integration (Spring Mail / SendGrid / AWS SES) for password recovery and email confirmations.
- [ ] **1.5 Role-Based Dynamic Plugin Gating (RBAC in `PluginConfig`)**
  - **Entity & Schema Extension (`PluginConfig.java`):**
    - Add `Set<Role> allowedRoles` (or `allowed_roles` join table) to specify which user roles have access to each plugin.
    - Default configuration: If `allowedRoles` is empty or includes `ROLE_USER`, it is globally accessible to all authenticated users.
  - **Spring Security Dynamic API Gating (`PluginAuthorizationFilter`):**
    - Intercept all incoming `/api/{pluginName}/**` REST requests.
    - Match current user's `Authentication.getAuthorities()` against the plugin's `allowedRoles`.
    - Automatically return `403 Forbidden` if the user's role is not permitted for that specific plugin.
  - **Frontend Dynamic Navigation & Route Protection:**
    - Filter `/api/plugins/active` to return only plugins permitted for the logged-in user.
    - `pluginLoader.js` & `AppMenu.vue`: Automatically show/hide sidebar menu items and dashboard widgets based on user roles.
    - Vue Router `beforeEach` navigation guard: Prevent unauthorized direct URL navigation to disabled plugin routes.
  - **Admin Plugin Configuration UI (`PluginManagement.vue`):**
    - Add a PrimeVue `MultiSelect` to assign roles to plugins dynamically with live activation/deactivation per role.

---

### [ ] 2. OBSERVABILITY, METRICS & HEALTH MONITORING
- [ ] **2.1 Spring Boot Actuator & Container Probes**
  - Enable `/actuator/health` with dedicated `liveness` and `readiness` probe endpoints for Docker and Kubernetes.
  - Expose `/actuator/metrics` and Prometheus endpoint `/actuator/prometheus` for Grafana monitoring.
- [ ] **2.2 Request Correlation & MDC Tracing**
  - Create a servlet filter generating and propagating a unique `X-Correlation-ID` (or `X-Request-ID`) per incoming request.
  - Bind the correlation ID to SLF4J MDC to correlate log traces across controllers, background threads, and database queries.
- [ ] **2.3 Structured Cloud JSON Logging**
  - Configure Logback with JSON encoder for production profile.
  - Seamless ingestion into cloud logging systems (OCI Logging, Datadog, Grafana Loki, AWS CloudWatch).
- [ ] **2.4 Automated Instance Resiliency, Self-Healing & Uptime Monitoring (Auto-Restart Watchdog)**
  - **Container Healthcheck & Auto-Restart Policies:**
    - Configure `restart: unless-stopped` in `docker-compose.yml` across all backend and database services.
    - Implement Docker native `healthcheck` invoking `/actuator/health/liveness` every 30s.
  - **Automated Self-Healing Watchdog (`autoheal`):**
    - Deploy lightweight `autoheal` sidecar container monitoring all running client containers; automatically restarts frozen, deadlocked, or OOM containers within 5 seconds.
  - **Uptime Monitoring & Real-Time Alerting (Uptime Kuma / Better Stack):**
    - Set up Uptime Kuma monitoring client subdomains (`*.duckdns.org` / custom domains) with instant push notifications via Telegram, Discord, Slack, or Email upon downtime and recovery events.
  - **Host OS Watchdog (Systemd):**
    - Configure `saavy-app.service` systemd unit on OCI / VPS with `Restart=always` and `RestartSec=10s` to ensure immediate container fleet recovery upon VM host reboots.

---

### [ ] 3. PERFORMANCE TUNING & CACHING ARCHITECTURE
- [ ] **3.1 Database Connection Pool Tuning (HikariCP)**
  - Configure production connection pool settings: `maximumPoolSize`, `minimumIdle`, `connectionTimeout`, and `leakDetectionThreshold = 2000ms`.
- [ ] **3.2 Database Index Audit & Query Optimization**
  - Audit and apply compound database indexes on frequently filtered and joined columns across all plugins (`party_id`, `created_at`, `status`, `tenant_id`, `user_id`).
- [ ] **3.3 Tiered L1/L2 Caching (Caffeine + Redis)**
  - Integrate L1 in-memory Caffeine Cache for local lookups + optional L2 Redis Cache for multi-node deployments with `@Cacheable` and automatic cache eviction.

---

### [ ] 4. AUTOMATED CI/CD & TESTING INFRASTRUCTURE
- [ ] **4.1 GitHub Actions Continuous Integration (`.github/workflows/ci.yml`)**
  - Automated pull request verification: runs `./gradlew check test`, `npm run lint`, and client bundle verification.
  - Docker container build verification on push to `main` branch.
- [ ] **4.2 Real Database Integration Testing with Testcontainers**
  - Configure Testcontainers for PostgreSQL in `server/src/test` to run real integration tests without requiring a local database.

---

### [ ] 5. FRONTEND NETWORK RESILIENCE & UX
- [ ] **5.1 Axios Silent 401 Token Refresh Interceptor**
  - Intercept expired JWT `401 Unauthorized` responses in Axios, queue pending API requests, refresh token in background, and replay requests transparently without logging the user out.
- [ ] **5.2 Global Frontend Error Boundary & Toast Notifier**
  - Catch uncaught Vue lifecycle errors and Axios 500 errors globally with user-friendly PrimeVue Toast notifications.
- [ ] **5.3 Progressive Web App (PWA) & Asset Optimization**
  - Configure Vite PWA plugin for static asset precaching, offline landing pages, and instant client-side asset delivery.

---

### [ ] 6. 🚀 HORIZONTAL SCALABILITY & MICROSERVICE EXTRACTION PATTERN
- [ ] **6.1 Standalone Spring Boot Bootstrap Recipe for Plugins**
  - Provide a standardized migration playbook to spin off any `plugins/[name]-plugin/server` into an independent `@SpringBootApplication` repository.
  - Enable independent horizontal container autoscaling (e.g. 10x instances of `attendance-plugin` or `warehouse-plugin` during peak traffic).
- [ ] **6.2 API Gateway Dynamic Path Routing (Caddy / Envoy / Spring Cloud Gateway)**
  - Configure path-based reverse proxy routing (`/api/warehouse/*` $\rightarrow$ `warehouse-service:8081`, fallback `/*` $\rightarrow$ monolith:8080).
  - Zero changes required on the Vue 3 frontend or `<GenericCrud>` layer.
- [ ] **6.3 Distributed JWT Verification & Stateless Security**
  - Standardize stateless JWT token decoding across standalone microservice pods using shared public keys / HMAC secrets without tight auth-server coupling.
- [ ] **6.4 Distributed Event Bus (Kafka / RabbitMQ / Redis Pub/Sub)**
  - Upgrade Spring `@EventListener` domain events to distributed pub/sub brokers for high-throughput asynchronous communication across extracted services.
- [ ] **6.5 Database Splitting & Read-Replica Pooling**
  - Support independent database instances/schemas per extracted microservice with HikariCP read-replicas to effortlessly handle millions of concurrent connections.

---

### [ ] 7. 🗄️ DYNAMIC PRIMEVUE DATATABLE SORTING & COLUMN FILTERING IN BASECONTROLLER
- [ ] **7.1 Native PrimeVue Lazy Load Protocol in `BaseController`**
  - Extend `/api/{entity}/page` to parse PrimeVue's standard data table query parameters:
    - `page` & `rows` (0-indexed page index & page size).
    - `sortField` & `sortOrder` (`1` for ASC, `-1` for DESC) $\rightarrow$ dynamically map to Spring Data `Sort.by(direction, sortField)`.
    - `multiSortMeta` (Support multi-column sorting: `[{ field: 'status', order: 1 }, { field: 'createdDate', order: -1 }]`).
    - `filters` (JSON string or query parameters representing column filters).
- [ ] **7.2 Dynamic Generic JPA Specification Filter Builder (`PrimeVueFilterSpecification`)**
  - Translate PrimeVue column `matchMode` values automatically into JPA Criteria predicates without writing manual queries:
    - `contains` / `notContains` $\rightarrow$ `cb.like(cb.lower(path), "%val%")`
    - `startsWith` / `endsWith` $\rightarrow$ `cb.like(...)`
    - `equals` / `notEquals` $\rightarrow$ `cb.equal(path, val)`
    - `in` $\rightarrow$ `path.in(valList)`
    - `between` / `dateIs` / `dateBefore` / `dateAfter` $\rightarrow$ `cb.between(path, start, end)`
    - `greaterThan` / `greaterThanOrEqualTo` / `lessThan` / `lessThanOrEqualTo` $\rightarrow$ numeric comparisons.
- [ ] **7.3 Global `<GenericCrud>` Server-Side Filter & Sort Integration**
  - Update frontend `<GenericCrud.vue>` to pass PrimeVue's `@page`, `@sort`, and `@filter` events to `BaseController` automatically.
  - Instantly empowers **all existing and future plugins** (Invoices, Master Data, Reports, Attendance) with lightning-fast server-side filtering on million-row tables with zero boilerplate!

---

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
- [x] **Time & Attendance Plugin (`attendance-plugin`)**: Full-featured attendance suite with standalone Bundy Clock Kiosk (zero-scrollbar tablet scaling, synthesized Web Audio chime, camera stream QR reader & PIN fallback), Employee Badge Studio with dynamic `BaseFile` avatar upload & high-res PNG download, timesheet logs, and dynamic Spring Security SPI customizers.
- [x] **Dynamic Plugin Auto-Discovery**: Gradle auto-detects and compiles any folder matching `plugins/*-plugin` in `settings.gradle` and `build.gradle`.
- [x] **Cross-Plugin Drop Table Protection**: Safe Liquibase tasks in `saavy-plugin-server.gradle` prevent accidental drops of host or peer tables.
- [x] **GenericCrud Metadata Engine**: Enhanced `<GenericCrud>` & `<GenericForm>` supporting text, number, date, datetime, enum, boolean/checkbox, textarea, and dynamic lookups.
- [x] **Plugin SPI Dashboard Widgets**: Pluggable Stats Cards, Doughnut Charts, and Overdue Tables filtered dynamically when plugins are enabled/disabled.
- [x] **Decoupled Architecture Verification**: Multi-module Gradle build compiles cleanly with zero cross-plugin compile-time dependencies.
