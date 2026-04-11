# EBU6304-Group43-TASystem
Teaching Assistant Recruitment System for BUPT International School - EBU6304 Group 43
## Team Members (Group 43)
| Name (姓名) | QMID (学号) | GitHub Username |
| :--- | :--- | :--- |
| [Student Zhang Zhen] | [231222235] | [@zxccvv114514] |
| [Student Yifan Wu] | [231222534] | [@wyfbean] |
| [Student Boning Mai] | [231220367] | [@duanynn] |
| [Student Yunhe Zhang] | [231221205] | [@Cwleoniszyh] |
| [Student Zaiyou Wang] | [231222464] | [@buaobuyinbugu] |
| [Student Yuqing Shen] | [231221319] | [@Absinthe433] |

## Data Storage

All persistent data is stored as plain JSON files on the server's file system. There is no external database. The data directory is initialised at `<webapp-deploy-root>/WEB-INF/data/` when the application starts.

### Structured data (always in the data directory)

| File / Path | Contents |
|---|---|
| `WEB-INF/data/users/` | One JSON file per user account (TA, MO, Admin), named `<userId>.json` |
| `WEB-INF/data/users.json` | Aggregate copy of all user accounts (kept for compatibility) |
| `WEB-INF/data/jobs.json` | All TA job postings |
| `WEB-INF/data/applications.json` | All student applications and their statuses |
| `WEB-INF/data/config.json` | System-wide configuration |

### CV / resume files

The location of uploaded CV files is controlled by the **Storage Mode** setting on the Admin → System Config page:

| Mode | Where files are stored | Notes |
|---|---|---|
| **WEBAPP** | Inside the deployed web application: `<webapp-deploy-root>/<Relative CV Storage Path>` (default: `WEB-INF/data/cvs/`) | Simple, no extra setup required. Files **may be lost** if the application is redeployed or cleaned. |
| **USER_HOME** | In the server OS user's home directory: `~/ebu_data/cvs/` | Files **persist across redeployments**. Requires the server process to have write access to the home directory. |

The Admin can switch between these modes and see the resolved absolute paths on the **System Config** page.

