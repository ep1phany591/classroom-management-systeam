# Database setup

`schema.sql` is the single source of truth for the classroom management database.

Warning: running this script drops and recreates the `classroom_management` database.

```powershell
cd D:\workspace\classroom-management\sql
sqlcmd -S localhost -U sa -P "YOUR_PASSWORD" -i schema.sql
```

## Third normal form design

The schema separates independent facts into dedicated tables:

| Category | Tables |
| --- | --- |
| Dictionary tables | `department`, `building`, `classroom_type`, `classroom_status`, `borrow_status` |
| Core entity tables | `admin`, `teacher`, `student`, `student_class`, `semester`, `course`, `classroom`, `classroom_borrow` |
| Relation tables | `student_course` |
| Operation log | `database_audit_log` |

Examples:

- A teacher stores `department_id`, not a repeated department name.
- A student stores `class_id`; the class determines its department.
- A classroom stores `building_id` and `room_type_id`, not repeated text.
- `student_course` represents the many-to-many relationship between students and courses.
- Status values are foreign keys to controlled dictionaries.

Compatibility views expose readable names to the backend:

`vw_teacher_detail`, `vw_student_detail`, `vw_classroom_detail`, `vw_classroom_usage_analysis`.

## Important constraints

The database enforces:

- Primary keys on every entity and composite primary key on `student_course`.
- Foreign keys for all cross-table references.
- Unique usernames, course codes, department codes and names.
- Unique classroom names within one building.
- At most one current semester through a filtered unique index.
- Valid semester date order, classroom capacity, floor range, course credit, course hours and borrow periods.
- Controlled classroom and borrow statuses through dictionary foreign keys.
- Transactional borrow creation with locking to reject overlapping active bookings.
- Trigger audit records for borrow insert, update and delete operations.

## Stored modules

| Type | Objects |
| --- | --- |
| Teacher CRUD | `sp_teacher_list`, `sp_teacher_get`, `sp_teacher_create`, `sp_teacher_update`, `sp_teacher_delete` |
| Student CRUD | `sp_student_list`, `sp_student_get`, `sp_student_create`, `sp_student_update`, `sp_student_delete` |
| Course query | `sp_course_list` |
| Classroom CRUD | `sp_classroom_list`, `sp_classroom_get`, `sp_classroom_create`, `sp_classroom_update`, `sp_classroom_delete` |
| Borrow CRUD | `sp_borrow_list`, `sp_borrow_get` |
| Borrow transactions | `sp_borrow_create_transaction`, `sp_borrow_cancel_transaction`, `sp_borrow_delete_transaction` |
| Trigger | `trg_classroom_borrow_audit` |
| Analysis | `vw_classroom_usage_analysis`, `sp_classroom_usage_analysis` |

The regular backend endpoints call these procedures where normalized joins or transaction rules are needed.
The admin-only endpoints under `/api/database-module/**` expose audit logs and analysis operations.
