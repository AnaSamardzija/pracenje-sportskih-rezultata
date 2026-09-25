DELIMITER $$

CREATE PROCEDURE sp_rebuild_player_stats()
BEGIN
    DECLARE v_done BOOLEAN DEFAULT FALSE;
    DECLARE v_user_id BIGINT;
    DECLARE v_group_id BIGINT;
    DECLARE v_sport_id BIGINT;
    DECLARE v_outcome VARCHAR(20);
    DECLARE c_results CURSOR FOR
        SELECT p.user_id, m.group_id, m.sport_id, s.outcome
        FROM matches m
        INNER JOIN match_sides s ON s.match_id = m.id
        INNER JOIN match_side_players p ON p.match_side_id = s.id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    DELETE FROM player_stats;

    OPEN c_results;

    results_loop: LOOP
        FETCH c_results INTO v_user_id, v_group_id, v_sport_id, v_outcome;

        IF v_done THEN
            LEAVE results_loop;
        END IF;

        CALL sp_update_player_stats(v_user_id, v_group_id, v_sport_id, v_outcome, 1);
    END LOOP;

    CLOSE c_results;

    COMMIT;
END $$

CREATE EVENT ev_rebuild_player_stats
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_DATE + INTERVAL 1 DAY + INTERVAL 3 HOUR
DO CALL sp_rebuild_player_stats() $$

DELIMITER ;

CALL sp_rebuild_player_stats();
