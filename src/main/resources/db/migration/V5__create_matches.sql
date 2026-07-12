CREATE TABLE matches
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    sport_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    played_at DATETIME(6) NOT NULL,
    recorded_by BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_matches_sport FOREIGN KEY (sport_id) REFERENCES sports (id),
    CONSTRAINT fk_matches_group FOREIGN KEY (group_id) REFERENCES groups (id),
    CONSTRAINT fk_matches_recorded_by FOREIGN KEY (recorded_by) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE TABLE match_sides
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    match_id BIGINT NOT NULL,
    score INT,
    outcome VARCHAR(20),
    is_winner BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_match_sides_match FOREIGN KEY (match_id) REFERENCES matches (id)
) ENGINE = InnoDB;

CREATE TABLE match_side_players
(
    match_side_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (match_side_id, user_id),
    CONSTRAINT fk_match_side_players_side FOREIGN KEY (match_side_id) REFERENCES match_sides (id),
    CONSTRAINT fk_match_side_players_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE TABLE match_side_set_scores
(
    match_side_id BIGINT NOT NULL,
    set_index INT NOT NULL,
    score INT NOT NULL,
    PRIMARY KEY (match_side_id, set_index),
    CONSTRAINT fk_match_side_set_scores_side FOREIGN KEY (match_side_id) REFERENCES match_sides (id)
) ENGINE = InnoDB;
