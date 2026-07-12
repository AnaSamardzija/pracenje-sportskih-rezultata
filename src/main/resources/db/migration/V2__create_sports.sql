CREATE TABLE sports
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    scoring_mode VARCHAR(20) NOT NULL,
    allow_draw BOOLEAN NOT NULL,
    min_players_per_side INT NOT NULL,
    max_players_per_side INT,
    best_of INT,
    points_to_win_set INT,
    points_for_win INT NOT NULL,
    points_for_draw INT NOT NULL,
    points_for_loss INT NOT NULL,
    active BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sports_name UNIQUE (name)
) ENGINE = InnoDB;
