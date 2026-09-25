CREATE TABLE player_stats
(
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    sport_id BIGINT NOT NULL,
    wins INT NOT NULL,
    draws INT NOT NULL,
    losses INT NOT NULL,
    PRIMARY KEY (user_id, group_id, sport_id),
    CONSTRAINT fk_player_stats_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_player_stats_group FOREIGN KEY (group_id) REFERENCES groups (id),
    CONSTRAINT fk_player_stats_sport FOREIGN KEY (sport_id) REFERENCES sports (id)
) ENGINE = InnoDB;
