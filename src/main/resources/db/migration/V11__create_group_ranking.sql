DELIMITER $$

CREATE PROCEDURE sp_group_ranking(IN p_group_id BIGINT, IN p_sport_id BIGINT)
BEGIN
    SELECT RANK() OVER (ORDER BY r.points DESC, r.wins DESC) AS rank,
           r.user_id,
           u.username,
           r.wins,
           r.draws,
           r.losses,
           r.wins + r.draws + r.losses AS total,
           r.points
    FROM (
        SELECT ps.user_id,
               SUM(ps.wins) AS wins,
               SUM(ps.draws) AS draws,
               SUM(ps.losses) AS losses,
               SUM(ps.wins * fn_points_for_outcome(ps.sport_id, 'WIN')
                   + ps.draws * fn_points_for_outcome(ps.sport_id, 'DRAW')
                   + ps.losses * fn_points_for_outcome(ps.sport_id, 'LOSS')) AS points
        FROM player_stats ps
        WHERE (p_group_id IS NULL OR ps.group_id = p_group_id)
            AND (p_sport_id IS NULL OR ps.sport_id = p_sport_id)
        GROUP BY ps.user_id
    ) r
    INNER JOIN users u ON u.id = r.user_id
    ORDER BY rank, u.username COLLATE utf8mb4_bin;
END $$

DELIMITER ;
