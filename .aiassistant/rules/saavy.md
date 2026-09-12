---
apply: always
---

# SaavyBootVue Engineering & Testing Rules

## 1. Mandatory Automated Testing
- Whenever implementing new features, creating new plugins, or modifying existing code, always write or update corresponding automated tests (`src/test/java/...`).
- Cover unit logic (services, utility mappers, calculations) and integration points (controllers, security permissions, repositories).

## 2. Strict Test Failure Reporting Protocol (CRITICAL)
- If a test fails during build, verification, or plan implementation:
  1. **DO NOT silently modify, disable, or delete the test** to force a green build.
  2. **Report the failure immediately to the user first**, including:
     - **Failing Test:** Class and method name.
     - **Error Message / Assertion Diff:** Expected vs. Actual output.
     - **Root Cause Analysis:** Explanation of why it failed (implementation defect vs. intentional requirement change).
     - **Proposed Solution:** Options to resolve the issue.
  3. Wait for confirmation or alignment before altering existing test assertions.

## 3. Architecture & Modular Boundaries
- Keep host shell decoupled from plugins.
- Plugins communicate via REST endpoints, dynamic UI metadata (`@UiField`, `@UiMaster`), and domain events.
- Never add hard cross-plugin Java entity compile dependencies.
