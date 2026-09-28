package rs.ac.ni.pmf.ana.dualdb.dto.mapper;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.dto.ranking.PlayerStatsResponse;
import rs.ac.ni.pmf.ana.dualdb.dto.ranking.RankingEntryResponse;
import rs.ac.ni.pmf.ana.dualdb.model.PlayerStats;
import rs.ac.ni.pmf.ana.dualdb.model.RankingEntry;

@Component
public class RankingMapper
{
	public RankingEntryResponse toResponse(final RankingEntry entry)
	{
		final PlayerStats stats = entry.getStats();

		return RankingEntryResponse.builder()
				.rank(entry.getRank())
				.playerId(stats.getPlayerId())
				.username(stats.getUsername())
				.wins(stats.getWins())
				.draws(stats.getDraws())
				.losses(stats.getLosses())
				.total(stats.getTotal())
				.points(stats.getPoints())
				.build();
	}

	public PlayerStatsResponse toResponse(final PlayerStats stats)
	{
		return PlayerStatsResponse.builder()
				.playerId(stats.getPlayerId())
				.username(stats.getUsername())
				.wins(stats.getWins())
				.draws(stats.getDraws())
				.losses(stats.getLosses())
				.total(stats.getTotal())
				.points(stats.getPoints())
				.winPercentage(stats.getWinPercentage())
				.longestWinStreak(stats.getLongestWinStreak())
				.build();
	}
}
