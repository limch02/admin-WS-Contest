-- ==========================================
--  Database Schema : Classroom Reservation System
--  ERD 기반 5개 테이블 (2025)
--  H2 Database용
-- ==========================================

SET REFERENTIAL_INTEGRITY FALSE;

-- ==========================================
-- 1. 사용자 (Users)
-- ==========================================
DROP TABLE IF EXISTS Users CASCADE;
CREATE TABLE Users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ID CHAR(9) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(50) NOT NULL,
    name VARCHAR(15) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    role VARCHAR(10) NOT NULL DEFAULT 'STUDENT' CHECK (role IN ('ADMIN', 'STUDENT')),
    reservation_num INT DEFAULT 0
);

-- ==========================================
-- 2. 강의실 (Classrooms)
-- ==========================================
DROP TABLE IF EXISTS Classrooms CASCADE;
CREATE TABLE Classrooms (
    room_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(15) NOT NULL,
    location VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    has_whiteboard TINYINT(1) DEFAULT 0,
    has_projector TINYINT(1) DEFAULT 0,
    reserve_count INT DEFAULT 0
);

-- ==========================================
-- 3. 예약 (Reservation)
-- ==========================================
DROP TABLE IF EXISTS Reservation CASCADE;
CREATE TABLE Reservation (
    reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_started_at DATETIME NOT NULL,
    reservation_ended_at DATETIME NOT NULL,
    reservation_num INT DEFAULT 0,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CANCELLED'))
);

ALTER TABLE Reservation
    ADD CONSTRAINT fk_reservation_room
        FOREIGN KEY (room_id) REFERENCES Classrooms(room_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_reservation_user
        FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT chk_reservation_time
        CHECK (reservation_started_at < reservation_ended_at);

-- ==========================================
-- 4. 예약 참여자 (Reserve_User)
-- ==========================================
DROP TABLE IF EXISTS Reserve_User CASCADE;
CREATE TABLE Reserve_User (
    reservation_participant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL
);

ALTER TABLE Reserve_User
    ADD CONSTRAINT fk_participant_user
        FOREIGN KEY (student_id) REFERENCES Users(user_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_participant_reservation
        FOREIGN KEY (reservation_id) REFERENCES Reservation(reservation_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT uk_student_reservation
        UNIQUE (student_id, reservation_id);

-- ==========================================
-- 5. 예약 대기열 (Wait_List)
-- ==========================================
DROP TABLE IF EXISTS Wait_List CASCADE;
CREATE TABLE Wait_List (
    waitlist_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reserved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    queue_position BIGINT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'WAITING' CHECK (status IN ('WAITING', 'APPROVED', 'CANCELLED')),
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    reservation_started_at DATETIME NOT NULL,
    reservation_ended_at DATETIME NOT NULL
);

ALTER TABLE Wait_List
    ADD CONSTRAINT fk_wait_user
        FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_wait_room
        FOREIGN KEY (room_id) REFERENCES Classrooms(room_id)
        ON DELETE CASCADE,
    ADD CONSTRAINT uk_waitlist_user_room_time
        UNIQUE (user_id, room_id, reservation_started_at, reservation_ended_at);

-- ==========================================
-- 인덱스 (성능 개선용)
-- ==========================================
CREATE INDEX idx_reservation_room_time
    ON Reservation (room_id, reservation_started_at, reservation_ended_at);
CREATE INDEX idx_reservation_user
    ON Reservation (user_id);
CREATE INDEX idx_reservation_status
    ON Reservation (status);
CREATE INDEX idx_waitlist_room_time
    ON Wait_List (room_id, reservation_started_at, reservation_ended_at);
CREATE INDEX idx_waitlist_queue
    ON Wait_List (room_id, reservation_started_at, reservation_ended_at, queue_position);
CREATE INDEX idx_user_role
    ON Users (role);

SET REFERENTIAL_INTEGRITY TRUE;