DELIMITER $$

CREATE PROCEDURE sp_update_player_stats(IN p_user_id BIGINT, IN p_group_id BIGINT, IN p_sport_id BIGINT, IN p_outcome VARCHAR(20), IN p_delta INT)
BEGIN
    IF p_outcome IS NOT NULL THEN
        IF p_delta > 0 THEN
            INSERT INTO player_stats (user_id, group_id, sport_id, wins, draws, losses)
            VALUES (p_user_id, p_group_id, p_sport_id, IF(p_outcome = 'WIN', p_delta, 0), IF(p_outcome = 'DRAW', p_delta, 0), IF(p_outcome = 'LOSS', p_delta, 0))
            ON DUPLICATE KEY UPDATE
                wins = wins + VALUES(wins),
                draws = draws + VALUES(draws),
                losses = losses + VALUES(losses);
        ELSE
            UPDATE player_stats
            SET wins = wins + IF(p_outcome = 'WIN', p_delta, 0),
                draws = draws + IF(p_outcome = 'DRAW', p_delta, 0),
                losses = losses + IF(p_outcome = 'LOSS', p_delta, 0)
            WHERE user_id = p_user_id AND group_id = p_group_id AND sport_id = p_sport_id;

            DELETE FROM player_stats
            WHERE user_id = p_user_id AND group_id = p_group_id AND sport_id = p_sport_id
                AND wins = 0 AND draws = 0 AND losses = 0;
        END IF;
    END IF;
END $$

CREATE TRIGGER trg_match_side_players_after_insert
AFTER INSERT ON match_side_players
FOR EACH ROW
BEGIN
    DECLARE v_group_id BIGINT;
    DECLARE v_sport_id BIGINT;
    DECLARE v_outcome VARCHAR(20);

    SELECT m.group_id, m.sport_id, s.outcome
    INTO v_group_id, v_sport_id, v_outcome
    FROM match_sides s
    INNER JOIN matches m ON m.id = s.match_id
    WHERE s.id = NEW.match_side_id;

    CALL sp_update_player_stats(NEW.user_id, v_group_id, v_sport_id, v_outcome, 1);
END $$

CREATE TRIGGER trg_match_side_players_after_delete
AFTER DELETE ON match_side_players
FOR EACH ROW
BEGIN
    DECLARE v_group_id BIGINT;
    DECLARE v_sport_id BIGINT;
    DECLARE v_outcome VARCHAR(20);

    SELECT m.group_id, m.sport_id, s.outcome
    INTO v_group_id, v_sport_id, v_outcome
    FROM match_sides s
    INNER JOIN matches m ON m.id = s.match_id
    WHERE s.id = OLD.match_side_id;

    CALL sp_update_player_stats(OLD.user_id, v_group_id, v_sport_id, v_outcome, -1);
END $$

CREATE TRIGGER trg_matches_after_update
AFTER UPDATE ON matches
FOR EACH ROW
BEGIN
    DECLARE v_done BOOLEAN DEFAULT FALSE;
    DECLARE v_user_id BIGINT;
    DECLARE v_outcome VARCHAR(20);
    DECLARE c_players CURSOR FOR
        SELECT p.user_id, s.outcome
        FROM match_sides s
        INNER JOIN match_side_players p ON p.match_side_id = s.id
        WHERE s.match_id = NEW.id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;

    IF NEW.group_id <> OLD.group_id OR NEW.sport_id <> OLD.sport_id THEN
        OPEN c_players;

        players_loop: LOOP
            FETCH c_players INTO v_user_id, v_outcome;

            IF v_done THEN
                LEAVE players_loop;
            END IF;

            CALL sp_update_player_stats(v_user_id, OLD.group_id, OLD.sport_id, v_outcome, -1);
            CALL sp_update_player_stats(v_user_id, NEW.group_id, NEW.sport_id, v_outcome, 1);
        END LOOP;

        CLOSE c_players;
    END IF;
END $$

DELIMITER ;
