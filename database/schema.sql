CREATE DATABASE IF NOT EXISTS BBS DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE BBS;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(30) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'NEW_USER',
    banned TINYINT(1) NOT NULL DEFAULT 0,
    history_enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    topic VARCHAR(30) NOT NULL,
    region VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    pinned TINYINT(1) NOT NULL DEFAULT 0,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    like_score INT NOT NULL DEFAULT 0,
    dislike_score INT NOT NULL DEFAULT 0,
    favorite_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_posts_search(topic, region, created_at),
    INDEX idx_posts_rank(pinned, like_score, favorite_count),
    CONSTRAINT fk_posts_author FOREIGN KEY(author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    like_score INT NOT NULL DEFAULT 0,
    dislike_score INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comments_post(post_id, created_at),
    CONSTRAINT fk_comments_post FOREIGN KEY(post_id) REFERENCES posts(id),
    CONSTRAINT fk_comments_author FOREIGN KEY(author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS post_votes (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    value TINYINT NOT NULL,
    weight INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(user_id, post_id),
    CONSTRAINT fk_post_votes_user FOREIGN KEY(user_id) REFERENCES users(id),
    CONSTRAINT fk_post_votes_post FOREIGN KEY(post_id) REFERENCES posts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comment_votes (
    user_id BIGINT NOT NULL,
    comment_id BIGINT NOT NULL,
    value TINYINT NOT NULL,
    weight INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(user_id, comment_id),
    CONSTRAINT fk_comment_votes_user FOREIGN KEY(user_id) REFERENCES users(id),
    CONSTRAINT fk_comment_votes_comment FOREIGN KEY(comment_id) REFERENCES comments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS favorites (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(user_id, post_id),
    CONSTRAINT fk_favorites_user FOREIGN KEY(user_id) REFERENCES users(id),
    CONSTRAINT fk_favorites_post FOREIGN KEY(post_id) REFERENCES posts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS browse_history (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(user_id, post_id),
    CONSTRAINT fk_history_user FOREIGN KEY(user_id) REFERENCES users(id),
    CONSTRAINT fk_history_post FOREIGN KEY(post_id) REFERENCES posts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL,
    post_id BIGINT NULL,
    comment_id BIGINT NULL,
    reason VARCHAR(255) NOT NULL,
    weight INT NOT NULL,
    handled TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_reports_handled(handled, created_at),
    CONSTRAINT fk_reports_user FOREIGN KEY(reporter_id) REFERENCES users(id),
    CONSTRAINT fk_reports_post FOREIGN KEY(post_id) REFERENCES posts(id),
    CONSTRAINT fk_reports_comment FOREIGN KEY(comment_id) REFERENCES comments(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    post_id BIGINT NULL,
    comment_id BIGINT NULL,
    type VARCHAR(30) NOT NULL,
    message VARCHAR(255) NOT NULL,
    read_flag TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notifications_user(recipient_id, read_flag, created_at),
    CONSTRAINT fk_notifications_recipient FOREIGN KEY(recipient_id) REFERENCES users(id),
    CONSTRAINT fk_notifications_actor FOREIGN KEY(actor_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sms_codes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) NOT NULL,
    purpose VARCHAR(20) NOT NULL,
    code VARCHAR(8) NOT NULL,
    used TINYINT(1) NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sms_lookup(phone, purpose, code, used, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users(username, password_hash, phone, role)
SELECT 'admin', SHA2('admin123', 256), '10000', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');
