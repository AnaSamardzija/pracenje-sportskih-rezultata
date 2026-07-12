CREATE TABLE groups
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_by BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_groups_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE = InnoDB;
