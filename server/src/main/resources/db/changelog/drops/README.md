# Schema Drops & Destructive Migrations

Place all destructive schema changelogs (such as `<dropTable>`, `<dropColumn>`, or table deprecation cleanups) in this directory as numbered changelog XML files (e.g., `01-drop-deprecated-tables.xml`).

### Why are drop changelogs placed here?
1. **Host-Level Control**: The host `server` application controls the persistent lifecycle of the database.
2. **Safe Plugin Removal**: When a plugin is deleted or its entities are removed, placing the drop changeset here prevents broken migration histories in `DATABASECHANGELOG`.
3. **Execution Guarantee**: Migrations in this folder are executed via `db.changelog-master.xml` after all plugin schemas have loaded.

### Example Changeset
```xml
<?xml version="1.1" encoding="UTF-8" standalone="no"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <changeSet id="drop-invoice-drafts-001" author="admin">
        <preConditions onFail="MARK_RAN">
            <tableExists tableName="invoice_drafts"/>
        </preConditions>
        <dropTable tableName="invoice_drafts"/>
    </changeSet>
</databaseChangeLog>
```
