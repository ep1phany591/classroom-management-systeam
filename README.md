# 教室管理系统

一个基于 Spring Boot + Vue 的教室管理系统，支持管理员、教师、学生三类角色。系统围绕教室资源、课程信息和教室借用流程展开，包含基础数据维护、教室空闲查询、借用申请、借用取消、审计日志和使用率分析等功能。

## 功能简介

- 用户登录：支持管理员、教师、学生三类角色登录。
- 基础数据管理：管理员可维护教师、学生、课程、学期和教室信息。
- 教室借用：教师或管理员可提交教室借用申请。
- 空闲查询：支持按日、按周查询教室空闲情况。
- 排课查询：支持查看每日、每周教室使用安排。
- 数据库模块：提供教室 CRUD、借用事务、触发器审计和使用率分析接口。
- 安全防护：包含登录令牌、密码哈希、接口鉴权、输入校验和 SQL 注入防护。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 前端 | Vue 3、Element Plus、Axios |
| 后端 | Spring Boot 3.5、Java 24 |
| 持久层 | MyBatis Plus、JdbcTemplate |
| 数据库 | SQL Server |
| 数据库对象 | 表、外键、唯一约束、检查约束、视图、存储过程、事务、触发器 |

## 项目结构

```text
classroom-management
├─ backend                 # Spring Boot 后端
│  ├─ src/main/java/com/classroom
│  │  ├─ config             # 拦截器、跨域、异常处理
│  │  ├─ controller         # REST 接口
│  │  ├─ dto                # 请求和响应 DTO
│  │  ├─ entity             # 实体类
│  │  ├─ mapper             # MyBatis Plus Mapper
│  │  └─ service            # 业务逻辑和数据库模块调用
│  └─ src/main/resources
│     └─ application.yml    # 后端配置
├─ frontend                # 前端静态页面
│  ├─ index.html
│  ├─ css
│  └─ js
├─ sql
│  ├─ schema.sql           # 数据库初始化脚本
│  └─ README.md            # 数据库说明
└─ README.md
```

## 数据库初始化

数据库脚本位于 `sql/schema.sql`。该脚本会删除并重建 `classroom_management` 数据库，请在执行前确认没有需要保留的数据。

```powershell
cd D:\workspace\classroom-management\sql
sqlcmd -S localhost -U sa -P "你的数据库密码" -i schema.sql
```

脚本会创建：

- 基础表：`admin`、`teacher`、`student`、`course`、`classroom`、`classroom_borrow` 等。
- 字典表：院系、楼栋、教室类型、教室状态、借用状态。
- 关系表：`student_course`。
- 存储过程：教室 CRUD、教师/学生 CRUD、借用事务、使用率分析。
- 触发器：`trg_classroom_borrow_audit`，用于记录借用表增删改日志。
- 视图：教室详情、教师详情、学生详情和教室使用率分析视图。

更多数据库说明见 [sql/README.md](sql/README.md)。

## 后端启动

进入后端目录：

```powershell
cd D:\workspace\classroom-management\backend
```

建议通过环境变量设置数据库密码，避免把真实密码写入代码仓库：

```powershell
$env:DB_PASSWORD="你的数据库密码"
```

如果本机已安装 Maven：

```powershell
mvn spring-boot:run
```

后端默认端口：

```text
http://localhost:8080
```

## 前端启动

不要直接双击 `frontend/index.html` 使用 `file://` 打开，否则浏览器可能触发本地文件安全限制。

建议使用本地 HTTP 服务：

```powershell
cd D:\workspace\classroom-management\frontend
python -m http.server 5500
```

然后访问：

```text
http://127.0.0.1:5500/index.html
```

前端接口地址在 `frontend/js/api.js` 中配置，默认请求：

```text
http://localhost:8080/api
```

## 默认账号

数据库初始化脚本会写入以下测试账号：

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `123456` |
| 教师 | `t001` | `123456` |
| 学生 | `s001` | `123456` |

首次登录后，系统会自动将明文密码升级为 PBKDF2 哈希。

## 核心接口示例

| 功能 | 方法 | 地址 |
| --- | --- | --- |
| 登录 | POST | `/api/auth/login` |
| 教室列表 | GET | `/api/classroom/list` |
| 新增教室 | POST | `/api/classroom` |
| 借用列表 | GET | `/api/borrow/list` |
| 新增借用 | POST | `/api/borrow` |
| 取消借用 | PUT | `/api/borrow/cancel/{id}` |
| 每日空闲查询 | GET | `/api/borrow/daily-free` |
| 数据库模块审计日志 | GET | `/api/database-module/audit-logs` |
| 教室使用率分析 | GET | `/api/database-module/analysis` |

## 数据库模块说明

本项目数据库模块主要围绕 `classroom` 和 `classroom_borrow` 两张核心表展开：

- `classroom`：通过存储过程完成教室信息的增、删、查、改。
- `classroom_borrow`：通过事务存储过程完成借用、取消和删除。
- 借用事务会检查教室状态、教师、课程、日期和节次，并使用锁避免同一时间段重复借用。
- 触发器会自动记录借用表的新增、修改和删除操作。
- 分析视图和分析存储过程用于统计教室借用次数和占用节次数。

## 注意事项

- 上传 GitHub 前不要提交真实数据库密码。
- 建议将 `application.yml` 中的密码保持为环境变量形式，例如 `${DB_PASSWORD:}`。
- 如果前端出现 `file:` 安全源错误，请使用本地 HTTP 服务启动前端。
- 如果教师或学生登录异常，请确认数据库脚本和后端实体映射保持一致。

