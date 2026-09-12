# Implementation Plan: Option B — Self-Hosted Private Cloud (OCI) with License Key Subscriptions

This plan outlines the architecture, implementation steps, and automation required to launch **Option B**: enabling business customers to deploy **SaavyBootVue** into their own **Oracle Cloud (OCI) Always Free Tier** ($0/mo hosting cost for them) while purchasing recurring **License Keys** ($29–$49/mo) from you.

---

## 1. Architecture Overview

```
 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                         YOUR LICENSING & CHECKOUT                           │
 │                                                                             │
 │   [ Customer Checkout ] ──► [ Lemon Squeezy / Stripe ]                      │
 │                                    │                                        │
 │                                    ▼ (Webhook)                              │
 │                     [ RSA-Signed License Key Issued ]                       │
 └────────────────────────────────────┬────────────────────────────────────────┘
                                      │ (License Key: e.g. SAAVY-PRO-...)
                                      ▼
 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                      CUSTOMER'S PRIVATE OCI FREE INSTANCE                   │
 │                                                                             │
 │   1. Customer runs: curl -sSL https://.../setup-oci.sh | bash               │
 │   2. Docker Compose boots: Spring Boot + PostgreSQL + Vue (Port 80/443)    │
 │   3. Customer logs in & enters License Key                                  │
 │   4. App validates signature locally (RSA Public Key) or via ping           │
 │   5. Full P&L, YoY Dashboard, Invoicing, Attendance & Exports unlocked!    │
 └─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Key Architectural Choices

1. **Offline Cryptographic Validation (RSA / JWT Signed Tokens)**:
   - The license key is an RSA-signed token containing `{ companyName, tier, expiresAt, maxUsers, plugins: ['pnl', 'invoice', 'attendance'] }`.
   - The app validates it locally using your embedded Public Key, meaning customer instances work even if their firewall blocks outbound web traffic.
   - Optional periodic online heartbeat with a 14-day offline grace period.
2. **Default Free / Community Tier**:
   - When installed without a key, provide a **14-day Free Trial** or **Community Plan** with basic features (e.g., standard P&L report) and prompt for a Pro License to unlock **YoY Comparative Dashboard**, **Formatted Excel/Image Exports**, and **Multi-Company Management**.

---

## 3. Proposed Implementation Components

### A. Backend Licensing Subsystem (`server` & `plugins/*`)

#### 1. `LicenseService.java` (`server/src/main/java/org/saavy/services/LicenseService.java`)
- Manages license storage in database / `plugin_config`.
- Validates cryptographic RSA signature and expiration date (`expiresAt`).
- Exposes helper methods: `isFeatureEnabled(String pluginKey)`, `getLicenseTier()`, `isLicenseActive()`, `getDaysRemaining()`.

#### 2. `LicenseController.java` (`server/src/main/java/org/saavy/controllers/LicenseController.java`)
- `GET /api/license/status`: Returns current license details (Tier, Expiry Date, Company Name, Active Plugins, Days Remaining).
- `POST /api/license/activate`: Accepts and validates a new license key payload.
- `POST /api/license/trial`: Activates a 14-day full-feature trial on first install.

#### 3. Security & Plugin Protections
- Protect advanced plugin endpoints (e.g. `/api/pnl/report/export/excel`, `/api/pnl/dashboard`) with license validation checks.

---

### B. Frontend License Management & Activation UI (`client`)

#### 1. `LicenseManagerDialog.vue` (`client/src/components/LicenseManagerDialog.vue`)
- Modal accessible from top navigation bar / settings.
- Displays license status badge (`Active - Pro`, `Trial - 12 days left`, `Expired`).
- Clean input field for pasting license keys with instant activation feedback and notification.

#### 2. `AppTopbar.vue` (`client/src/layout/AppTopbar.vue`)
- Displays small subscription status indicator (e.g., `Pro License` or `Trial (12d)`).

---

### C. Automated 1-Click OCI Setup & Update Scripts

#### 1. `scripts/setup-oci.sh` & `scripts/deploy-oci.sh`
- Enhance one-line installer with interactive domain prompt (or free fallback `nip.io` / `duckdns.org` domain).
- Configure automated Caddy / Nginx reverse proxy with automated Let's Encrypt SSL.
- Configure systemd service `saavy-app.service` to ensure Docker Compose starts automatically on VM reboot.

#### 2. `scripts/update-oci.sh`
- One-command updater for customers:
  ```bash
  curl -sSL https://raw.githubusercontent.com/blats002/SaavyBootVue/uss-enterprise/scripts/update-oci.sh | bash
  ```
  Pulls latest release tags, applies incremental Liquibase migrations, and restarts containers with zero data loss.

---

### D. License Generator CLI & Webhook Integration

#### 1. `tools/license-generator/`
- Standalone tool for you to generate signed customer license keys:
  ```bash
  npm run generate-license -- --company="Acme Corp" --tier="PRO" --months=12 --plugins="pnl,invoice,attendance"
  ```
- Optional Lemon Squeezy / Stripe webhook handler that auto-generates and emails the license key upon successful checkout.

---

### E. Automated Instance Resiliency, Self-Healing & Monitoring

#### 1. Auto-Restart & Healthcheck Policies (`docker-compose.yml`)
- Add `restart: unless-stopped` across application and PostgreSQL containers.
- Add Docker native `healthcheck` probing `/actuator/health` every 30 seconds.

#### 2. Self-Healing Watchdog Container (`autoheal`)
- Sidecar container that monitors health status and automatically restarts unresponsive, deadlocked, or OOM containers within 5 seconds.

#### 3. Real-Time Uptime Monitoring & Instant Alerts
- Uptime Kuma integration to monitor client subdomains (`*.duckdns.org` / custom domains) with instant push alerts via Telegram, Discord, Slack, or Email.

#### 4. Host OS Crash Recovery (`saavy-app.service`)
- Systemd unit with `Restart=always` ensuring the entire Docker fleet resumes immediately after any VM host reboot or maintenance window.

---

## 4. Verification Plan

### Automated Tests
1. **License Cryptographic Verification Tests**:
   - Verify that valid RSA-signed keys succeed and unlock all features.
   - Verify that tampered keys, expired keys, or wrong public keys fail validation.
2. **Grace Period & Degradation Tests**:
   - Verify that expired licenses provide clear renewal banners without corrupting existing business data.

### Manual Verification on OCI
1. Launch a fresh OCI Always Free Ubuntu instance.
2. Run `setup-oci.sh` and verify complete deployment in < 2 minutes.
3. Access UI via browser, verify 14-day trial activation, apply a generated Pro License key, and verify P&L Excel and YoY Dashboard unlock.
4. Run `update-oci.sh` and verify seamless zero-loss update.
