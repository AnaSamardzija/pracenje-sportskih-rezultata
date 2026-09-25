DELIMITER $$

CREATE FUNCTION fn_points_for_outcome(p_sport_id BIGINT, p_outcome VARCHAR(20))
RETURNS INT
NOT DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_points INT;

    SELECT CASE p_outcome
               WHEN 'WIN' THEN points_for_win
               WHEN 'DRAW' THEN points_for_draw
               WHEN 'LOSS' THEN points_for_loss
           END
    INTO v_points
    FROM sports
    WHERE id = p_sport_id;

    RETURN IFNULL(v_points, 0);
END $$

CREATE FUNCTION fn_total_matches(p_user_id BIGINT, p_sport_id BIGINT)
RETURNS INT
NOT DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_total INT;

    SELECT SUM(wins + draws + losses)
    INTO v_total
    FROM player_stats
    WHERE user_id = p_user_id AND (p_sport_id IS NULL OR sport_id = p_sport_id);

    RETURN IFNULL(v_total, 0);
END $$

CREATE FUNCTION fn_win_percentage(p_user_id BIGINT, p_sport_id BIGINT)
RETURNS DECIMAL(5, 2)
NOT DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_wins INT;
    DECLARE v_total INT;

    SET v_total = fn_total_matches(p_user_id, p_sport_id);

    IF v_total = 0 THEN
        RETURN 0;
    END IF;

    SELECT SUM(wins)
    INTO v_wins
    FROM player_stats
    WHERE user_id = p_user_id AND (p_sport_id IS NULL OR sport_id = p_sport_id);

    RETURN ROUND(100 * v_wins / v_total, 2);
END $$

DELIMITER ;
