INSERT INTO sports (name, type, scoring_mode, allow_draw, min_players_per_side, max_players_per_side, best_of, points_to_win_set, points_for_win, points_for_draw, points_for_loss, active)
VALUES ('Tennis', 'INDIVIDUAL', 'SETS', FALSE, 1, 1, 3, 6, 3, 1, 0, TRUE),
       ('Table Tennis', 'INDIVIDUAL', 'SETS', FALSE, 1, 1, 5, 11, 3, 1, 0, TRUE),
       ('Chess', 'INDIVIDUAL', 'OUTCOME', TRUE, 1, 1, NULL, NULL, 2, 1, 0, TRUE),
       ('Football', 'TEAM', 'POINTS', TRUE, 1, 11, NULL, NULL, 3, 1, 0, TRUE),
       ('Basketball', 'TEAM', 'POINTS', FALSE, 1, 5, NULL, NULL, 3, 1, 0, TRUE),
       ('Volleyball', 'TEAM', 'SETS', FALSE, 2, 6, 5, 25, 3, 1, 0, TRUE);
