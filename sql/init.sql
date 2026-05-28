-- ============================================================
-- AstroLog 数据库初始化脚本
-- 版本: 1.0
-- 所有表使用 CREATE TABLE IF NOT EXISTS，可重复执行
-- 引擎: InnoDB, 字符集: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS astrolog
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE astrolog;

-- -----------------------------------------------------------
-- 1. users 用户表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)   NOT NULL UNIQUE,
    password      VARCHAR(255)  NOT NULL,
    role          ENUM('observer','admin') NOT NULL DEFAULT 'observer',
    avatar_path   VARCHAR(255),
    city          VARCHAR(100),
    default_lat   DECIMAL(9,6),
    default_lon   DECIMAL(9,6),
    login_attempts TINYINT       DEFAULT 0,
    locked_until  DATETIME,
    last_login    DATETIME,
    create_time   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 2. celestial_bodies 星体信息表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS celestial_bodies (
    body_id        INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    type           VARCHAR(20)  NOT NULL,
    constellation  VARCHAR(50),
    ra_h           INT,
    ra_m           INT,
    dec_deg        INT,
    dec_min        INT,
    magnitude      DECIMAL(4,2),
    distance_ly    DECIMAL(10,2),
    messier_number INT,
    ngc_number     INT,
    best_season    VARCHAR(20),
    description    TEXT,
    image_path     VARCHAR(255),
    INDEX idx_bodies_type (type),
    INDEX idx_bodies_constellation (constellation),
    INDEX idx_bodies_messier (messier_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 3. observation_sites 观测地点表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS observation_sites (
    site_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT           NOT NULL,
    name         VARCHAR(100)  NOT NULL,
    latitude     DECIMAL(9,6)  NOT NULL,
    longitude    DECIMAL(9,6)  NOT NULL,
    altitude     INT,
    bortle_scale TINYINT,
    best_time    VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_sites_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 4. equipment 器材表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS equipment (
    equip_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT           NOT NULL,
    name          VARCHAR(100)  NOT NULL,
    type          VARCHAR(50),
    aperture      DECIMAL(5,2),
    focal_length  INT,
    purchase_date DATE,
    status        VARCHAR(20)   DEFAULT '在用',
    description   TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_equip_user (user_id),
    INDEX idx_equip_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 5. observations 观测记录表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS observations (
    obs_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT           NOT NULL,
    body_id      INT           NOT NULL,
    site_id      INT,
    obs_time     DATETIME      NOT NULL,
    location_lat DECIMAL(9,6),
    location_lon DECIMAL(9,6),
    weather      VARCHAR(50),
    seeing       TINYINT,
    moon_phase   VARCHAR(20),
    note         TEXT,
    create_time  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (body_id) REFERENCES celestial_bodies(body_id) ON DELETE CASCADE,
    FOREIGN KEY (site_id) REFERENCES observation_sites(site_id) ON DELETE SET NULL,
    INDEX idx_obs_user (user_id),
    INDEX idx_obs_body (body_id),
    INDEX idx_obs_time (obs_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 6. obs_equipment 观测-器材关联表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS obs_equipment (
    obs_id   INT NOT NULL,
    equip_id INT NOT NULL,
    PRIMARY KEY (obs_id, equip_id),
    FOREIGN KEY (obs_id) REFERENCES observations(obs_id) ON DELETE CASCADE,
    FOREIGN KEY (equip_id) REFERENCES equipment(equip_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 7. equipment_maintenance 器材维护日志表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS equipment_maintenance (
    maint_id        INT AUTO_INCREMENT PRIMARY KEY,
    equip_id        INT            NOT NULL,
    maint_date      DATE           NOT NULL,
    description     TEXT,
    cost            DECIMAL(10,2),
    next_maint_date DATE,
    FOREIGN KEY (equip_id) REFERENCES equipment(equip_id) ON DELETE CASCADE,
    INDEX idx_maint_equip (equip_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 8. observation_tags 标签字典表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS observation_tags (
    tag_id INT AUTO_INCREMENT PRIMARY KEY,
    name   VARCHAR(50) NOT NULL UNIQUE,
    color  VARCHAR(7)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 9. obs_tag_relation 观测-标签关联表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS obs_tag_relation (
    obs_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (obs_id, tag_id),
    FOREIGN KEY (obs_id) REFERENCES observations(obs_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES observation_tags(tag_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 10. user_favorites 用户收藏表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_favorites (
    user_id     INT       NOT NULL,
    body_id     INT       NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, body_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (body_id) REFERENCES celestial_bodies(body_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 11. operation_logs 操作日志表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS operation_logs (
    log_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL,
    operation   VARCHAR(50)  NOT NULL,
    detail      TEXT,
    ip_address  VARCHAR(45),
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_logs_user (user_id),
    INDEX idx_logs_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
