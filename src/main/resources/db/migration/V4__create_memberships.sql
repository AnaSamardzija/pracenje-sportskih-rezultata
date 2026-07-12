CREATE TABLE memberships
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    role_in_group VARCHAR(20) NOT NULL,
    joined_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_memberships_user_group UNIQUE (user_id, group_id),
    CONSTRAINT fk_memberships_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_memberships_group FOREIGN KEY (group_id) REFERENCES groups (id)
) ENGINE = InnoDB;
