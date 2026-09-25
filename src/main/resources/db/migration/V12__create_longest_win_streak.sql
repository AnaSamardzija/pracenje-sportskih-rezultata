DELIMITER $$

CREATE PROCEDURE sp_longest_win_streak(IN p_user_id BIGINT, IN p_sport_id BIGINT, OUT p_streak INT)
BEGIN
    DECLARE v_done BOOLEAN DEFAULT FALSE;
    DECLARE v_outcome VARCHAR(20);
    DECLARE v_current INT DEFAULT 0;
    DECLARE c_outcomes CURSOR FOR
        SELECT s.outcome
        FROM matches m
        INNER JOIN match_sides s ON s.match_id = m.id
        INNER JOIN match_side_players p ON p.match_side_id = s.id
        WHERE p.user_id = p_user_id AND (p_sport_id IS NULL OR m.sport_id = p_sport_id)
        ORDER BY m.played_at, m.id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;

    SET p_streak = 0;

    OPEN c_outcomes;

    outcomes_loop: LOOP
        FETCH c_outcomes INTO v_outcome;

        IF v_done THEN
            LEAVE outcomes_loop;
        END IF;

        IF v_outcome = 'WIN' THEN
            SET v_current = v_current + 1;
            SET p_streak = GREATEST(p_streak, v_current);
        ELSE
            SET v_current = 0;
        END IF;
    END LOOP;

    CLOSE c_outcomes;
END $$

DELIMITER ;
