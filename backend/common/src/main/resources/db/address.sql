-- User Address Table
CREATE TABLE
    IF NOT EXISTS user_address (
        id int NOT NULL AUTO_INCREMENT,
        user_id INTEGER NOT NULL,
        street_address VARCHAR(255) NOT NULL,
        city VARCHAR(100) NOT NULL,
        state VARCHAR(100),
        postal_code VARCHAR(20) NOT NULL,
        country VARCHAR(100) NOT NULL,
        is_default BIT(1) DEFAULT 0, -- Not using BOOLEAN as otherwise JOOQ generates a byte
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (id),
        INDEX idx_user_address_user_id (user_id), -- Fast lookup on select from address where user_id = ?
        CONSTRAINT fk_user_address_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE
    );