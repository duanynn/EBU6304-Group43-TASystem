# EBU6304-Group43-TASystem

**Teaching Assistant Recruitment System** — BUPT International School, EBU6304 Software Engineering Group Project (Group 43).

Lightweight **Java Servlet/JSP** web application. All data is stored in **JSON/text files** (no database, no Spring Boot), as required by the coursework specification.

---

## Team Members (Group 43)

| Name (姓名) | QMID (学号) | GitHub Username |
| :--- | :--- | :--- |
| Zhang Zhen | 231222235 | [@zxccvv114514](https://github.com/zxccvv114514) |
| Yifan Wu | 231222534 | [@wyfbean](https://github.com/wyfbean) |
| Boning Mai | 231220367 | [@duanynn](https://github.com/duanynn) |
| Yunhe Zhang | 231221205 | [@Cwleoniszyh](https://github.com/Cwleoniszyh) |
| Zaiyou Wang | 231222464 | [@buaobuyinbugu](https://github.com/buaobuyinbugu) |
| Yuqing Shen | 231221319 | [@Absinthe433](https://github.com/Absinthe433) |

---

## Alignment with Project Specification (PDF)

| Requirement | Implementation |
|-------------|----------------|
| Servlet/JSP web app, no DB | WAR on Tomcat; `DataStore` persists JSON under `WEB-INF/data/` |
| TA profile & CV upload | `ta/profile.jsp`, PDF/DOC/DOCX parsing (`CvParsingService`) |
| TA find jobs & apply | `ta/jobBoard.jsp`, search/sort, apply + confirm flow |
| TA check application status | `ta/myApplications.jsp`, interview accept/decline |
| MO post jobs | `mo/postJob.jsp` (Module TA / Invigilation / Other) |
| MO select applicants | `mo/applicants.jsp` (pending / interview / accept / reject) |
| Admin TA workload | `admin/workload.jsp`, per-student accepted course count |
| AI skill matching & gaps | `SkillMatchService` + `AiAdviceService` (DashScope + local fallback) |
| Workload control | Configurable max courses per TA; MO sees workload before accept |

---

## Prerequisites

| Item | Version (tested) |
|------|------------------|
| JDK | 17+ |
| Apache Maven | 3.9+ |
| Apache Tomcat | 11.x (Jakarta Servlet 6) |

---

## Build, Deploy, and Run

### 1. Package

```powershell
cd "D:\path\to\EBU6304-Group43-TASystem"
mvn clean package
```

Output: `target/ta-recruitment-system.war`

### 2. Deploy to Tomcat

| PDF requirement | Our implementation |
|:----------------|:-------------------|
| Servlet/JSP, no DB | WAR on Tomcat · `DataStore` → `WEB-INF/data/*.json` |
| TA profile & CV | `ta/profile.jsp` · PDF/DOC/DOCX · `CvParsingService` |
| Find jobs & apply | `ta/jobBoard.jsp` · search/sort · apply confirm |
| Application status | `ta/myApplications.jsp` · interview accept/decline |
| MO post jobs | `mo/postJob.jsp` · Module TA / Invigilation / Other |
| MO select applicants | `mo/applicants.jsp` · pending / interview / accept / reject |
| Admin workload | `admin/workload.jsp` · per-student course count |
| AI skill match & gaps | `SkillMatchService` + `AiAdviceService` |
| Workload limits | Configurable max courses · MO sees load before accept |

```powershell
Copy-Item "target\ta-recruitment-system.war" "D:\Tomcat\...\webapps\" -Force
```

### 3. Start Tomcat

```powershell
cd "D:\Tomcat\...\bin"
.\startup.bat
```

### 4. Open the application

**Base URL (context path):**

```
http://localhost:8080/ta-recruitment-system/
```

**Login page:**

```
http://localhost:8080/ta-recruitment-system/login.jsp
```

> If port or context path differs, replace `8080` / `ta-recruitment-system` accordingly.

### 5. Run unit tests

```powershell
mvn test
```

Optional coverage report:

```powershell
mvn verify
```

Report: `target/site/jacoco/index.html`

---

## Demo Test Accounts

Seeded on **first startup**; known IDs are **patched on Tomcat restart**. Credentials are also shown on the login page.

| Role | ID | Password | Notes |
|------|-----|----------|--------|
| **TA** | `2021001001` | `123` | ID suffix `001001` (forgot password); availability Mon 08:00–12:00, Wed 14:00–18:00 |
| **TA** | `2021001002` | `123` | ID suffix `001002` |
| **MO** | `0000000001` | `123` | MO home + sample job *Software Engineering* |
| **Admin** | `admin` | `admin` | System config, workload, open jobs |

**MO accounts** are created by Admin (`/admin/users`), not self-registration. **TA** can register at `/register`.

---

## AI Configuration (DashScope / 百炼)

1. Log in as **Admin** → **System Configuration** (`/admin/config`).
2. Enter **DashScope API Key**, optional Endpoint and Model → Save.
3. Settings are written to `WEB-INF/data/config.json`.

Environment variable fallback:

```powershell
$env:DASHSCOPE_API_KEY="your_key_here"
$env:DASHSCOPE_MODEL="qwen-plus"
```

Without API key, the system uses **local rule-based** skill match and advice (still demonstrable).

---

## Data Storage

| Path (under Tomcat) | Content |
|---------------------|---------|
| `WEB-INF/data/users/*.json` | User accounts (TA/MO/Admin) |
| `WEB-INF/data/jobs.json` | Job postings |
| `WEB-INF/data/applications.json` | Applications |
| `WEB-INF/data/config.json` | Semester, deadlines, API keys, limits |
| `WEB-INF/data/cvs/` | Uploaded CV files |
| `WEB-INF/data/avatars/` | Uploaded avatars |

---

## Project Structure

```
src/main/java/bupt/is/ta/
  model/          User, Job, Application, Config, ...
  service/        Business logic (match, schedule, AI, CV, ...)
  store/          DataStore (JSON persistence)
  web/            Servlets, AuthFilter
  util/           Schedule, captcha, display helpers
src/main/webapp/
  login.jsp, register.jsp, forgotPassword.jsp
  ta/             Student portal JSPs
  mo/             Module organiser JSPs
  admin/          Admin portal JSPs
  css/, js/       Styles and client scripts
src/test/java/    JUnit 5 unit tests
```

---

## Recommended Demo Flow (for video / viva)

1. **Login** as TA → Job Board → **Apply** → confirm page (AI + Schedule Fit).
2. **Profile**: upload CV, edit skills & weekly availability.
3. **My Schedule**: timetable 08:00–23:00.
4. **Login** as MO → Post job → **Applicants** → Interview / Accept.
5. **Login** as Admin → Overview, Workload, Config, Open Jobs.
6. Show **forgot password** (TA) and **error** case (e.g. wrong password or closed application period).

---

## User Manual — Where to Take Screenshots

The final submission asks for a **user manual with at least one screenshot per main screen**.  
Use full browser window or a clear crop of the **main content area** (include top nav so the role is obvious).

**Base:** `http://localhost:8080/ta-recruitment-system`

### Public (no login)

| # | Screen | URL | What to capture |
|---|--------|-----|-----------------|
| 1 | **Login** | `/login.jsp` | Title, demo accounts box, login form |
| 2 | **TA registration** | `/register` | Registration form + weekly availability rows |
| 3 | **Forgot password** | `/forgotPassword` | Student ID, captcha, ID suffix fields |

### TA (Student) — login `2021001001` / `123`

| # | Screen | URL | What to capture |
|---|--------|-----|-----------------|
| 4 | **Job Board** | `/ta/jobs` | Job cards, fit %, search/sort, Apply button |
| 5 | **Apply confirmation** | Click **Apply** on a job (POST `/ta/apply`) | **Important:** AI Overall, Rule Match, **Schedule Fit %**, skills panels |
| 6 | **My Applications** | `/ta/applications` | Status list (Pending / Interviewing / Accepted) |
| 7 | **My Profile** | `/ta/profile` | Avatar, CV upload, parsed fields, weekly availability slots |
| 8 | **My Schedule** | `/ta/schedule` | Weekly grid 08:00–23:00, legend, availability blocks |

**Optional TA screenshots:** interview alert on job board; account menu (⋮) → theme / change password.

### MO (Module Organiser) — login `0000000001` / `123`

| # | Screen | URL | What to capture |
|---|--------|-----|-----------------|
| 9 | **MO Home** | `/mo/home` | College field, course list |
| 10 | **MO Dashboard** | `/mo/dashboard` | Posted jobs table, open/closed status |
| 11 | **Post Job** | `/mo/postJob` | Job type, schedule slots, description, AI generate (if configured) |
| 12 | **Applicants** | `/mo/applicants?jobId=<id>` | Applicant table: fit %, schedule fit, workload, Accept/Interview/Reject |
| 13 | **View CV** | Click **View CV** on an applicant | `/mo/cv/view?...` — student CV page |

Get `jobId` from Dashboard (job list) or Admin Open Jobs.

### Admin — login `admin` / `admin`

| # | Screen | URL | What to capture |
|---|--------|-----|-----------------|
| 14 | **Overview** | `/admin/overview` | User/job/application counts |
| 15 | **Workload** | `/admin/workload` | Per-TA accepted course statistics |
| 16 | **Users** | `/admin/users` | MO account creation form + user list |
| 17 | **System config** | `/admin/config` | Semester, deadline, max courses, DashScope API |
| 18 | **Open jobs** | `/admin/openJobs` | All open positions school-wide |



## Licence & Coursework

Developed for **EBU6304 Software Engineering** coursework. For academic use by Group 43 and assessors only.
