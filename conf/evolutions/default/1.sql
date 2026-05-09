# --- !Ups

CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME NULL,
    duration_minutes INT NULL,
    calculated_fee DECIMAL(10,2) NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE IF EXISTS payments;