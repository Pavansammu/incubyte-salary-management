# Salary Management — Requirements (v1)

## 1. Goal
Replace ACME's Excel-based salary tracking with a web application, so that the HR Manager can
**maintain salary data for ~10,000 employees across countries** and **answer "how do we pay people?"**
without manual spreadsheet work.

**Persona:** HR Manager. One user, internal tool, comfortable with data but not technical.

**Success looks like:** the HR Manager can find any employee in seconds, update a salary safely,
and see country and role pay insights on one screen, all without opening Excel.

## 2. Questions the HR Manager must be able to answer
1. What is the **minimum, maximum and average salary in a country**?
2. What is the **average salary for a job title within a country**?
3. How many people do we employ in each country, and how is pay **distributed** (salary bands)?
4. What does a **specific employee** earn, and in which currency?

## 3. Scope — features in v1
| # | Feature | Notes |
|---|---------|-------|
| F1 | **Employee list** | Paginated, sortable table; search by name; filter by country, job title and department. Must stay fast at 10k rows (server-side paging). |
| F2 | **Add / edit / delete employee** | Fields: full name, job title, department, country, salary (annual), currency, employment type, hire date. Validation with clear messages (e.g. salary > 0, required fields). |
| F3 | **Salary insights by country** | Min, max, average salary and headcount per country, in that country's currency. |
| F4 | **Salary insights by job title** | Average (plus min/max) salary for each job title within a selected country. |
| F5 | **Pay distribution** | Headcount per salary band for a selected country. |
| F6 | **Seed data** | Script that creates 10,000 realistic employees (deterministic, re-runnable). |

**Non-functional:** list and insight API responses under ~300 ms at 10k rows (DB indexes on country and job title,
aggregation done in SQL, not in Java); input validated on both UI and API; consistent JSON error format;
unit tests for the core logic; one-command local setup.

## 4. Deliberately left out (and why)
| Left out | Reason |
|----------|--------|
| **Login / roles** | Single HR-Manager persona and an internal tool; auth adds effort but no insight value for this exercise. First item on the roadmap (SSO / role-based access). |
| **Currency conversion** | Converting pay to one currency needs a rates source and a policy (which date's rate?). Wrong rates would give misleading answers, so v1 reports each country **in its own currency**, which is how HR compares pay locally. |
| **Payroll, tax, payslips** | A different product with country-specific legal rules. Out of scope for salary *management and insight*. |
| **Excel import / export** | Valuable for migration, but the seed script covers initial data. Planned next (CSV import of the existing sheets). |
| **Salary history / audit trail** | Important for compliance, but it doubles the data model. v1 stores the current salary; history is a planned addition. |
| **Approval workflows, bonuses, equity** | No requirement stated; would add complexity without answering the core question. |

## 5. Assumptions (to confirm with the team)
- Salary is an **annual gross amount** stored with an ISO currency code; one currency per country.
- One employee belongs to exactly one country, department and job title at a time.
- "Delete" is a hard delete in v1 (no soft-delete or archive).
- 10,000 employees is the expected scale; the design should not break at 10x (paging + SQL aggregation).

## 6. Tech choices (summary — details in `docs/DESIGN.md`)
Java 21 + Spring Boot 3 REST API · SQLite (relational, zero-ops, fits 10k rows) · Angular + Angular Material UI ·
JUnit 5 / Mockito tests · deployed as a single service with the UI served alongside the API.
