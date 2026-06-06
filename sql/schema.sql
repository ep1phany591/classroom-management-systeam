USE master;
GO

IF DB_ID('classroom_management') IS NOT NULL
BEGIN
    ALTER DATABASE classroom_management SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE classroom_management;
END
GO

CREATE DATABASE classroom_management;
GO
USE classroom_management;
GO

CREATE TABLE dbo.department (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(30) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    CONSTRAINT uq_department_code UNIQUE(code),
    CONSTRAINT uq_department_name UNIQUE(name),
    CONSTRAINT ck_department_code CHECK(code NOT LIKE '%[^A-Za-z0-9_-]%'),
    CONSTRAINT ck_department_name CHECK(LEN(LTRIM(RTRIM(name))) > 0)
);

CREATE TABLE dbo.student_class (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    department_id BIGINT NOT NULL,
    class_name NVARCHAR(100) NOT NULL,
    CONSTRAINT fk_student_class_department FOREIGN KEY(department_id) REFERENCES dbo.department(id),
    CONSTRAINT uq_student_class UNIQUE(department_id, class_name),
    CONSTRAINT ck_student_class_name CHECK(LEN(LTRIM(RTRIM(class_name))) > 0)
);

CREATE TABLE dbo.building (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    CONSTRAINT uq_building_name UNIQUE(name),
    CONSTRAINT ck_building_name CHECK(LEN(LTRIM(RTRIM(name))) > 0)
);

CREATE TABLE dbo.classroom_type (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL,
    CONSTRAINT uq_classroom_type_name UNIQUE(name),
    CONSTRAINT ck_classroom_type_name CHECK(LEN(LTRIM(RTRIM(name))) > 0)
);

CREATE TABLE dbo.classroom_status (
    id INT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name NVARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT ck_classroom_status_id CHECK(id BETWEEN 1 AND 3)
);

CREATE TABLE dbo.borrow_status (
    id INT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name NVARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT ck_borrow_status_id CHECK(id BETWEEN 1 AND 2)
);

CREATE TABLE dbo.admin (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name NVARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    create_time DATETIME2 NOT NULL CONSTRAINT df_admin_create_time DEFAULT GETDATE(),
    CONSTRAINT uq_admin_username UNIQUE(username),
    CONSTRAINT ck_admin_username CHECK(username NOT LIKE '%[^A-Za-z0-9_.-]%' AND LEN(username) BETWEEN 3 AND 50),
    CONSTRAINT ck_admin_name CHECK(LEN(LTRIM(RTRIM(name))) > 0)
);

CREATE TABLE dbo.teacher (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name NVARCHAR(50) NOT NULL,
    title NVARCHAR(50),
    department_id BIGINT,
    phone VARCHAR(20),
    email VARCHAR(100),
    create_time DATETIME2 NOT NULL CONSTRAINT df_teacher_create_time DEFAULT GETDATE(),
    CONSTRAINT uq_teacher_username UNIQUE(username),
    CONSTRAINT fk_teacher_department FOREIGN KEY(department_id) REFERENCES dbo.department(id),
    CONSTRAINT ck_teacher_username CHECK(username NOT LIKE '%[^A-Za-z0-9_.-]%' AND LEN(username) BETWEEN 3 AND 50),
    CONSTRAINT ck_teacher_name CHECK(LEN(LTRIM(RTRIM(name))) > 0)
);

CREATE TABLE dbo.student (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name NVARCHAR(50) NOT NULL,
    class_id BIGINT,
    phone VARCHAR(20),
    email VARCHAR(100),
    create_time DATETIME2 NOT NULL CONSTRAINT df_student_create_time DEFAULT GETDATE(),
    CONSTRAINT uq_student_username UNIQUE(username),
    CONSTRAINT fk_student_class FOREIGN KEY(class_id) REFERENCES dbo.student_class(id),
    CONSTRAINT ck_student_username CHECK(username NOT LIKE '%[^A-Za-z0-9_.-]%' AND LEN(username) BETWEEN 3 AND 50),
    CONSTRAINT ck_student_name CHECK(LEN(LTRIM(RTRIM(name))) > 0)
);

CREATE TABLE dbo.semester (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_current BIT NOT NULL CONSTRAINT df_semester_is_current DEFAULT 0,
    create_time DATETIME2 NOT NULL CONSTRAINT df_semester_create_time DEFAULT GETDATE(),
    CONSTRAINT uq_semester_name UNIQUE(name),
    CONSTRAINT ck_semester_name CHECK(LEN(LTRIM(RTRIM(name))) > 0),
    CONSTRAINT ck_semester_date CHECK(start_date <= end_date)
);
CREATE UNIQUE INDEX uq_semester_one_current ON dbo.semester(is_current) WHERE is_current=1;

CREATE TABLE dbo.course (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    teacher_id BIGINT,
    semester_id BIGINT NOT NULL,
    credit DECIMAL(3,1),
    hours INT,
    description NVARCHAR(500),
    create_time DATETIME2 NOT NULL CONSTRAINT df_course_create_time DEFAULT GETDATE(),
    CONSTRAINT uq_course_code UNIQUE(code),
    CONSTRAINT fk_course_teacher FOREIGN KEY(teacher_id) REFERENCES dbo.teacher(id),
    CONSTRAINT fk_course_semester FOREIGN KEY(semester_id) REFERENCES dbo.semester(id),
    CONSTRAINT ck_course_name CHECK(LEN(LTRIM(RTRIM(name))) > 0),
    CONSTRAINT ck_course_code CHECK(code NOT LIKE '%[^A-Za-z0-9_-]%' AND LEN(code) BETWEEN 2 AND 50),
    CONSTRAINT ck_course_credit CHECK(credit IS NULL OR credit > 0 AND credit <= 20),
    CONSTRAINT ck_course_hours CHECK(hours IS NULL OR hours > 0 AND hours <= 500)
);

CREATE TABLE dbo.student_course (
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrolled_at DATETIME2 NOT NULL CONSTRAINT df_student_course_enrolled_at DEFAULT GETDATE(),
    CONSTRAINT pk_student_course PRIMARY KEY(student_id, course_id),
    CONSTRAINT fk_student_course_student FOREIGN KEY(student_id) REFERENCES dbo.student(id),
    CONSTRAINT fk_student_course_course FOREIGN KEY(course_id) REFERENCES dbo.course(id)
);

CREATE TABLE dbo.classroom (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    building_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    name NVARCHAR(50) NOT NULL,
    floor_num INT,
    capacity INT NOT NULL,
    has_projector BIT NOT NULL CONSTRAINT df_classroom_projector DEFAULT 0,
    has_ac BIT NOT NULL CONSTRAINT df_classroom_ac DEFAULT 0,
    room_status INT NOT NULL CONSTRAINT df_classroom_status DEFAULT 1,
    description NVARCHAR(500),
    create_time DATETIME2 NOT NULL CONSTRAINT df_classroom_create_time DEFAULT GETDATE(),
    CONSTRAINT uq_classroom_building_name UNIQUE(building_id, name),
    CONSTRAINT fk_classroom_building FOREIGN KEY(building_id) REFERENCES dbo.building(id),
    CONSTRAINT fk_classroom_type FOREIGN KEY(room_type_id) REFERENCES dbo.classroom_type(id),
    CONSTRAINT fk_classroom_status FOREIGN KEY(room_status) REFERENCES dbo.classroom_status(id),
    CONSTRAINT ck_classroom_name CHECK(LEN(LTRIM(RTRIM(name))) > 0),
    CONSTRAINT ck_classroom_floor CHECK(floor_num IS NULL OR floor_num BETWEEN -5 AND 100),
    CONSTRAINT ck_classroom_capacity CHECK(capacity BETWEEN 1 AND 1000)
);

CREATE TABLE dbo.classroom_borrow (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    course_id BIGINT,
    borrow_date DATE NOT NULL,
    start_period INT NOT NULL,
    end_period INT NOT NULL,
    purpose NVARCHAR(200),
    borrow_status INT NOT NULL CONSTRAINT df_borrow_status DEFAULT 1,
    create_time DATETIME2 NOT NULL CONSTRAINT df_borrow_create_time DEFAULT GETDATE(),
    CONSTRAINT fk_borrow_classroom FOREIGN KEY(classroom_id) REFERENCES dbo.classroom(id),
    CONSTRAINT fk_borrow_teacher FOREIGN KEY(teacher_id) REFERENCES dbo.teacher(id),
    CONSTRAINT fk_borrow_course FOREIGN KEY(course_id) REFERENCES dbo.course(id),
    CONSTRAINT fk_borrow_status FOREIGN KEY(borrow_status) REFERENCES dbo.borrow_status(id),
    CONSTRAINT ck_borrow_period CHECK(start_period BETWEEN 1 AND 10 AND end_period BETWEEN 1 AND 10 AND start_period <= end_period),
    CONSTRAINT ck_borrow_purpose CHECK(purpose IS NULL OR LEN(purpose) <= 200)
);

CREATE TABLE dbo.database_audit_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    operation_type VARCHAR(10) NOT NULL,
    record_id BIGINT,
    detail NVARCHAR(500),
    created_at DATETIME2 NOT NULL CONSTRAINT df_audit_created_at DEFAULT GETDATE(),
    CONSTRAINT ck_audit_table CHECK(table_name='classroom_borrow'),
    CONSTRAINT ck_audit_operation CHECK(operation_type IN('INSERT','UPDATE','DELETE'))
);
GO

CREATE INDEX idx_teacher_department ON dbo.teacher(department_id);
CREATE INDEX idx_student_class ON dbo.student(class_id);
CREATE INDEX idx_student_class_department ON dbo.student_class(department_id);
CREATE INDEX idx_course_teacher ON dbo.course(teacher_id);
CREATE INDEX idx_course_semester ON dbo.course(semester_id);
CREATE INDEX idx_student_course_course ON dbo.student_course(course_id);
CREATE INDEX idx_classroom_type ON dbo.classroom(room_type_id);
CREATE INDEX idx_borrow_room_date_status ON dbo.classroom_borrow(classroom_id,borrow_date,borrow_status,start_period,end_period);
CREATE INDEX idx_borrow_teacher_date ON dbo.classroom_borrow(teacher_id,borrow_date);
CREATE INDEX idx_audit_created_at ON dbo.database_audit_log(created_at DESC);
GO

CREATE SEQUENCE dbo.department_code_seq AS BIGINT START WITH 100 INCREMENT BY 1;
GO

CREATE VIEW dbo.vw_teacher_detail AS
SELECT t.id,t.username,t.password,t.name,t.title,d.name AS department,t.phone,t.email,t.create_time
FROM dbo.teacher t LEFT JOIN dbo.department d ON d.id=t.department_id;
GO
CREATE VIEW dbo.vw_student_detail AS
SELECT s.id,s.username,s.password,s.name,c.class_name,d.name AS department,s.phone,s.email,s.create_time
FROM dbo.student s
LEFT JOIN dbo.student_class c ON c.id=s.class_id
LEFT JOIN dbo.department d ON d.id=c.department_id;
GO
CREATE VIEW dbo.vw_classroom_detail AS
SELECT r.id,r.name,b.name AS building,r.floor_num,r.capacity,t.name AS room_type,
       r.has_projector,r.has_ac,r.room_status,r.description,r.create_time
FROM dbo.classroom r
JOIN dbo.building b ON b.id=r.building_id
JOIN dbo.classroom_type t ON t.id=r.room_type_id;
GO
CREATE VIEW dbo.vw_classroom_usage_analysis AS
SELECT r.id AS classroom_id,r.name AS classroom_name,r.building,r.capacity,
       COUNT(b.id) AS total_borrow_count,
       SUM(CASE WHEN b.borrow_status=1 THEN 1 ELSE 0 END) AS active_borrow_count,
       SUM(CASE WHEN b.borrow_status=1 THEN b.end_period-b.start_period+1 ELSE 0 END) AS occupied_period_count
FROM dbo.vw_classroom_detail r
LEFT JOIN dbo.classroom_borrow b ON b.classroom_id=r.id
GROUP BY r.id,r.name,r.building,r.capacity;
GO

CREATE TRIGGER dbo.trg_classroom_borrow_audit ON dbo.classroom_borrow
AFTER INSERT,UPDATE,DELETE AS
BEGIN
    SET NOCOUNT ON;
    INSERT dbo.database_audit_log(table_name,operation_type,record_id,detail)
    SELECT 'classroom_borrow','INSERT',i.id,CONCAT(N'Borrow created: classroom=',i.classroom_id,N', teacher=',i.teacher_id,N', date=',i.borrow_date)
    FROM inserted i LEFT JOIN deleted d ON d.id=i.id WHERE d.id IS NULL;
    INSERT dbo.database_audit_log(table_name,operation_type,record_id,detail)
    SELECT 'classroom_borrow','UPDATE',i.id,CONCAT(N'Borrow updated: status ',d.borrow_status,N' -> ',i.borrow_status)
    FROM inserted i JOIN deleted d ON d.id=i.id;
    INSERT dbo.database_audit_log(table_name,operation_type,record_id,detail)
    SELECT 'classroom_borrow','DELETE',d.id,N'Borrow deleted'
    FROM deleted d LEFT JOIN inserted i ON i.id=d.id WHERE i.id IS NULL;
END
GO

CREATE PROCEDURE dbo.sp_teacher_list @name NVARCHAR(50)=NULL AS
BEGIN SET NOCOUNT ON; SELECT * FROM dbo.vw_teacher_detail WHERE @name IS NULL OR name LIKE N'%'+@name+N'%' ORDER BY id DESC; END
GO
CREATE PROCEDURE dbo.sp_teacher_get @id BIGINT AS BEGIN SET NOCOUNT ON; SELECT * FROM dbo.vw_teacher_detail WHERE id=@id; END
GO
CREATE PROCEDURE dbo.sp_teacher_create @username VARCHAR(50),@password VARCHAR(255),@name NVARCHAR(50),@title NVARCHAR(50)=NULL,@department NVARCHAR(100)=NULL,@phone VARCHAR(20)=NULL,@email VARCHAR(100)=NULL AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @department_id BIGINT=NULL;
    IF @department IS NOT NULL BEGIN
        IF NOT EXISTS(SELECT 1 FROM dbo.department WHERE name=@department) INSERT dbo.department(code,name) VALUES(CONCAT('D',NEXT VALUE FOR dbo.department_code_seq),@department);
        SELECT @department_id=id FROM dbo.department WHERE name=@department;
    END
    INSERT dbo.teacher(username,password,name,title,department_id,phone,email) VALUES(@username,@password,@name,@title,@department_id,@phone,@email);
    SELECT * FROM dbo.vw_teacher_detail WHERE id=SCOPE_IDENTITY();
END
GO
CREATE PROCEDURE dbo.sp_teacher_update @id BIGINT,@username VARCHAR(50),@password VARCHAR(255)=NULL,@name NVARCHAR(50),@title NVARCHAR(50)=NULL,@department NVARCHAR(100)=NULL,@phone VARCHAR(20)=NULL,@email VARCHAR(100)=NULL AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @department_id BIGINT=NULL;
    IF @department IS NOT NULL BEGIN
        IF NOT EXISTS(SELECT 1 FROM dbo.department WHERE name=@department) INSERT dbo.department(code,name) VALUES(CONCAT('D',NEXT VALUE FOR dbo.department_code_seq),@department);
        SELECT @department_id=id FROM dbo.department WHERE name=@department;
    END
    UPDATE dbo.teacher SET username=@username,password=COALESCE(@password,password),name=@name,title=@title,department_id=@department_id,phone=@phone,email=@email WHERE id=@id;
    IF @@ROWCOUNT=0 THROW 50020,N'Teacher not found',1;
    SELECT * FROM dbo.vw_teacher_detail WHERE id=@id;
END
GO
CREATE PROCEDURE dbo.sp_teacher_delete @id BIGINT AS BEGIN SET NOCOUNT ON; DELETE dbo.teacher WHERE id=@id; IF @@ROWCOUNT=0 THROW 50020,N'Teacher not found',1; END
GO

CREATE PROCEDURE dbo.sp_student_list @name NVARCHAR(50)=NULL AS
BEGIN SET NOCOUNT ON; SELECT * FROM dbo.vw_student_detail WHERE @name IS NULL OR name LIKE N'%'+@name+N'%' ORDER BY id DESC; END
GO
CREATE PROCEDURE dbo.sp_student_get @id BIGINT AS BEGIN SET NOCOUNT ON; SELECT * FROM dbo.vw_student_detail WHERE id=@id; END
GO
CREATE PROCEDURE dbo.sp_student_create @username VARCHAR(50),@password VARCHAR(255),@name NVARCHAR(50),@class_name NVARCHAR(100)=NULL,@department NVARCHAR(100)=NULL,@phone VARCHAR(20)=NULL,@email VARCHAR(100)=NULL AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @class_id BIGINT=NULL,@department_id BIGINT=NULL;
    IF @class_name IS NOT NULL BEGIN
        IF @department IS NULL THROW 50030,N'Department is required when class is set',1;
        IF NOT EXISTS(SELECT 1 FROM dbo.department WHERE name=@department) INSERT dbo.department(code,name) VALUES(CONCAT('D',NEXT VALUE FOR dbo.department_code_seq),@department);
        SELECT @department_id=id FROM dbo.department WHERE name=@department;
        IF NOT EXISTS(SELECT 1 FROM dbo.student_class WHERE department_id=@department_id AND class_name=@class_name) INSERT dbo.student_class(department_id,class_name) VALUES(@department_id,@class_name);
        SELECT @class_id=id FROM dbo.student_class WHERE department_id=@department_id AND class_name=@class_name;
    END
    INSERT dbo.student(username,password,name,class_id,phone,email) VALUES(@username,@password,@name,@class_id,@phone,@email);
    SELECT * FROM dbo.vw_student_detail WHERE id=SCOPE_IDENTITY();
END
GO
CREATE PROCEDURE dbo.sp_student_update @id BIGINT,@username VARCHAR(50),@password VARCHAR(255)=NULL,@name NVARCHAR(50),@class_name NVARCHAR(100)=NULL,@department NVARCHAR(100)=NULL,@phone VARCHAR(20)=NULL,@email VARCHAR(100)=NULL AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @class_id BIGINT=NULL,@department_id BIGINT=NULL;
    IF @class_name IS NOT NULL BEGIN
        IF @department IS NULL THROW 50030,N'Department is required when class is set',1;
        IF NOT EXISTS(SELECT 1 FROM dbo.department WHERE name=@department) INSERT dbo.department(code,name) VALUES(CONCAT('D',NEXT VALUE FOR dbo.department_code_seq),@department);
        SELECT @department_id=id FROM dbo.department WHERE name=@department;
        IF NOT EXISTS(SELECT 1 FROM dbo.student_class WHERE department_id=@department_id AND class_name=@class_name) INSERT dbo.student_class(department_id,class_name) VALUES(@department_id,@class_name);
        SELECT @class_id=id FROM dbo.student_class WHERE department_id=@department_id AND class_name=@class_name;
    END
    UPDATE dbo.student SET username=@username,password=COALESCE(@password,password),name=@name,class_id=@class_id,phone=@phone,email=@email WHERE id=@id;
    IF @@ROWCOUNT=0 THROW 50031,N'Student not found',1;
    SELECT * FROM dbo.vw_student_detail WHERE id=@id;
END
GO
CREATE PROCEDURE dbo.sp_student_delete @id BIGINT AS BEGIN SET NOCOUNT ON; DELETE dbo.student WHERE id=@id; IF @@ROWCOUNT=0 THROW 50031,N'Student not found',1; END
GO

CREATE PROCEDURE dbo.sp_course_list @name NVARCHAR(100)=NULL,@teacher_id BIGINT=NULL,@student_id BIGINT=NULL AS
BEGIN
    SET NOCOUNT ON;
    SELECT c.* FROM dbo.course c
    WHERE (@name IS NULL OR c.name LIKE N'%'+@name+N'%') AND (@teacher_id IS NULL OR c.teacher_id=@teacher_id)
      AND (@student_id IS NULL OR EXISTS(SELECT 1 FROM dbo.student_course sc WHERE sc.course_id=c.id AND sc.student_id=@student_id))
    ORDER BY c.id DESC;
END
GO

CREATE PROCEDURE dbo.sp_classroom_list @name NVARCHAR(50)=NULL,@building NVARCHAR(100)=NULL AS
BEGIN SET NOCOUNT ON; SELECT * FROM dbo.vw_classroom_detail WHERE (@name IS NULL OR name LIKE N'%'+@name+N'%') AND (@building IS NULL OR building=@building) ORDER BY id DESC; END
GO
CREATE PROCEDURE dbo.sp_classroom_get @id BIGINT AS BEGIN SET NOCOUNT ON; SELECT * FROM dbo.vw_classroom_detail WHERE id=@id; END
GO
CREATE PROCEDURE dbo.sp_classroom_create @name NVARCHAR(50),@building NVARCHAR(100),@floor_num INT=NULL,@capacity INT,@room_type NVARCHAR(50),@has_projector BIT=0,@has_ac BIT=0,@room_status INT=1,@description NVARCHAR(500)=NULL AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @building_id BIGINT,@room_type_id BIGINT;
    IF NOT EXISTS(SELECT 1 FROM dbo.building WHERE name=@building) INSERT dbo.building(name) VALUES(@building);
    IF NOT EXISTS(SELECT 1 FROM dbo.classroom_type WHERE name=@room_type) INSERT dbo.classroom_type(name) VALUES(@room_type);
    SELECT @building_id=id FROM dbo.building WHERE name=@building;
    SELECT @room_type_id=id FROM dbo.classroom_type WHERE name=@room_type;
    INSERT dbo.classroom(building_id,room_type_id,name,floor_num,capacity,has_projector,has_ac,room_status,description)
    VALUES(@building_id,@room_type_id,@name,@floor_num,@capacity,@has_projector,@has_ac,@room_status,@description);
    SELECT * FROM dbo.vw_classroom_detail WHERE id=SCOPE_IDENTITY();
END
GO
CREATE PROCEDURE dbo.sp_classroom_update @id BIGINT,@name NVARCHAR(50),@building NVARCHAR(100),@floor_num INT=NULL,@capacity INT,@room_type NVARCHAR(50),@has_projector BIT=0,@has_ac BIT=0,@room_status INT=1,@description NVARCHAR(500)=NULL AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @building_id BIGINT,@room_type_id BIGINT;
    IF NOT EXISTS(SELECT 1 FROM dbo.building WHERE name=@building) INSERT dbo.building(name) VALUES(@building);
    IF NOT EXISTS(SELECT 1 FROM dbo.classroom_type WHERE name=@room_type) INSERT dbo.classroom_type(name) VALUES(@room_type);
    SELECT @building_id=id FROM dbo.building WHERE name=@building;
    SELECT @room_type_id=id FROM dbo.classroom_type WHERE name=@room_type;
    UPDATE dbo.classroom SET building_id=@building_id,room_type_id=@room_type_id,name=@name,floor_num=@floor_num,capacity=@capacity,has_projector=@has_projector,has_ac=@has_ac,room_status=@room_status,description=@description WHERE id=@id;
    IF @@ROWCOUNT=0 THROW 50010,N'Classroom not found',1;
    SELECT * FROM dbo.vw_classroom_detail WHERE id=@id;
END
GO
CREATE PROCEDURE dbo.sp_classroom_delete @id BIGINT AS
BEGIN SET NOCOUNT ON; IF EXISTS(SELECT 1 FROM dbo.classroom_borrow WHERE classroom_id=@id) THROW 50011,N'Classroom has borrow records',1; DELETE dbo.classroom WHERE id=@id; IF @@ROWCOUNT=0 THROW 50010,N'Classroom not found',1; END
GO

CREATE PROCEDURE dbo.sp_borrow_list @classroom_id BIGINT=NULL,@teacher_id BIGINT=NULL,@borrow_date DATE=NULL AS
BEGIN SET NOCOUNT ON; SELECT * FROM dbo.classroom_borrow WHERE (@classroom_id IS NULL OR classroom_id=@classroom_id) AND (@teacher_id IS NULL OR teacher_id=@teacher_id) AND (@borrow_date IS NULL OR borrow_date=@borrow_date) ORDER BY borrow_date DESC,start_period; END
GO
CREATE PROCEDURE dbo.sp_borrow_get @id BIGINT AS BEGIN SET NOCOUNT ON; SELECT * FROM dbo.classroom_borrow WHERE id=@id; END
GO
CREATE PROCEDURE dbo.sp_borrow_create_transaction @classroom_id BIGINT,@teacher_id BIGINT,@course_id BIGINT=NULL,@borrow_date DATE,@start_period INT,@end_period INT,@purpose NVARCHAR(200)=NULL AS
BEGIN
    SET NOCOUNT ON; SET XACT_ABORT ON;
    BEGIN TRY
        BEGIN TRANSACTION;
        IF @borrow_date<CAST(GETDATE() AS DATE) THROW 50008,N'Borrow date cannot be in the past',1;
        IF @start_period<1 OR @end_period>10 OR @start_period>@end_period THROW 50001,N'Invalid period range',1;
        IF NOT EXISTS(SELECT 1 FROM dbo.classroom WHERE id=@classroom_id AND room_status=1) THROW 50002,N'Classroom unavailable',1;
        IF NOT EXISTS(SELECT 1 FROM dbo.teacher WHERE id=@teacher_id) THROW 50005,N'Teacher not found',1;
        IF @course_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM dbo.course WHERE id=@course_id AND (teacher_id IS NULL OR teacher_id=@teacher_id)) THROW 50006,N'Course not found or teacher mismatch',1;
        IF EXISTS(SELECT 1 FROM dbo.classroom_borrow WITH(UPDLOCK,HOLDLOCK) WHERE classroom_id=@classroom_id AND borrow_date=@borrow_date AND borrow_status=1 AND start_period<=@end_period AND end_period>=@start_period) THROW 50003,N'Classroom occupied',1;
        INSERT dbo.classroom_borrow(classroom_id,teacher_id,course_id,borrow_date,start_period,end_period,purpose) VALUES(@classroom_id,@teacher_id,@course_id,@borrow_date,@start_period,@end_period,@purpose);
        SELECT * FROM dbo.classroom_borrow WHERE id=SCOPE_IDENTITY();
        COMMIT TRANSACTION;
    END TRY BEGIN CATCH IF @@TRANCOUNT>0 ROLLBACK TRANSACTION; THROW; END CATCH
END
GO
CREATE PROCEDURE dbo.sp_borrow_cancel_transaction @id BIGINT AS
BEGIN SET NOCOUNT ON; SET XACT_ABORT ON; BEGIN TRY BEGIN TRANSACTION; UPDATE dbo.classroom_borrow SET borrow_status=2 WHERE id=@id AND borrow_status=1; IF @@ROWCOUNT=0 THROW 50004,N'Borrow not found or cancelled',1; SELECT * FROM dbo.classroom_borrow WHERE id=@id; COMMIT TRANSACTION; END TRY BEGIN CATCH IF @@TRANCOUNT>0 ROLLBACK TRANSACTION; THROW; END CATCH END
GO
CREATE PROCEDURE dbo.sp_borrow_delete_transaction @id BIGINT AS
BEGIN SET NOCOUNT ON; SET XACT_ABORT ON; BEGIN TRY BEGIN TRANSACTION; DELETE dbo.classroom_borrow WHERE id=@id; IF @@ROWCOUNT=0 THROW 50007,N'Borrow not found',1; COMMIT TRANSACTION; END TRY BEGIN CATCH IF @@TRANCOUNT>0 ROLLBACK TRANSACTION; THROW; END CATCH END
GO
CREATE PROCEDURE dbo.sp_classroom_usage_analysis @start_date DATE=NULL,@end_date DATE=NULL AS
BEGIN
    SET NOCOUNT ON;
    SELECT r.id AS classroom_id,r.name AS classroom_name,r.building,r.capacity,COUNT(b.id) AS borrow_count,SUM(CASE WHEN b.borrow_status=1 THEN b.end_period-b.start_period+1 ELSE 0 END) AS occupied_period_count
    FROM dbo.vw_classroom_detail r LEFT JOIN dbo.classroom_borrow b ON b.classroom_id=r.id AND (@start_date IS NULL OR b.borrow_date>=@start_date) AND (@end_date IS NULL OR b.borrow_date<=@end_date)
    GROUP BY r.id,r.name,r.building,r.capacity ORDER BY occupied_period_count DESC,r.id;
END
GO

INSERT dbo.classroom_status(id,code,name) VALUES(1,'AVAILABLE',N'Available'),(2,'OCCUPIED',N'Occupied'),(3,'MAINTENANCE',N'Maintenance');
INSERT dbo.borrow_status(id,code,name) VALUES(1,'ACTIVE',N'Active'),(2,'CANCELLED',N'Cancelled');
INSERT dbo.department(code,name) VALUES('CS',N'Computer Science'),('MATH',N'Mathematics');
INSERT dbo.student_class(department_id,class_name) VALUES(1,N'CS2101'),(2,N'MATH2101');
INSERT dbo.building(name) VALUES(N'Building 1'),(N'Building 2');
INSERT dbo.classroom_type(name) VALUES(N'Lecture Hall'),(N'Multimedia Room'),(N'Classroom');
INSERT dbo.admin(username,password,name,phone,email) VALUES('admin','123456',N'Administrator','13800000000','admin@school.edu.cn');
INSERT dbo.semester(name,start_date,end_date,is_current) VALUES(N'2026 Spring','2026-02-16','2026-07-01',1);
INSERT dbo.teacher(username,password,name,title,department_id) VALUES('t001','123456',N'Teacher Zhang',N'Professor',1),('t002','123456',N'Teacher Li',N'Associate Professor',2);
INSERT dbo.student(username,password,name,class_id) VALUES('s001','123456',N'Student Wang',1),('s002','123456',N'Student Zhao',2);
INSERT dbo.classroom(building_id,room_type_id,name,floor_num,capacity,has_projector,has_ac) VALUES(1,1,N'101',1,120,1,1),(1,2,N'201',2,80,1,1),(2,3,N'301',3,60,0,0);
INSERT dbo.course(name,code,teacher_id,semester_id,credit,hours) VALUES(N'Database Principles','CS301',1,1,3.0,48),(N'Advanced Mathematics','MATH201',2,1,4.0,64);
INSERT dbo.student_course(student_id,course_id) VALUES(1,1),(2,2);
GO

SELECT name,type_desc FROM sys.objects WHERE name LIKE 'sp_%' OR name LIKE 'trg_%' OR name LIKE 'vw_%' ORDER BY type_desc,name;
GO
