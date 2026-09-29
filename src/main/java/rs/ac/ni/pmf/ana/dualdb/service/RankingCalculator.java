package rs.ac.ni.pmf.ana.dualdb.service;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.model.Match;
import rs.ac.ni.pmf.ana.dualdb.model.MatchOutcome;
import rs.ac.ni.pmf.ana.dualdb.model.MatchSide;
import rs.ac.ni.pmf.ana.dualdb.model.PlayerStats;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;
import rs.ac.ni.pmf.ana.dualdb.model.SportRules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sabira mečeve u statistiku po igraču. Čista logika, bez baze — zato se bodovi
 * računaju po meču, iz pravila sporta tog meča, pa rezultat važi i kad je lista
 * filtrirana po sportu i kad nije.
 */
@Component
public class RankingCalculator
{
	public List<PlayerStats> aggregate(final List<Match> matches, final Map<String, Sport> sportsById)
	{
		final Map<String, PlayerStats> statsByPlayer = new LinkedHashMap<>();

		for (final Match match : matches)
		{
			final SportRules rules = sportsById.get(match.getSportId()).getRules();

			for (final MatchSide side : match.getSides())
			{
				for (final String playerId : side.getPlayerIds())
				{
					final PlayerStats stats = statsByPlayer.computeIfAbsent(playerId,
							id -> PlayerStats.builder().playerId(id).build());

					count(stats, side.getOutcome(), rules);
				}
			}
		}

		statsByPlayer.values().forEach(this::applyWinPercentage);

		return new ArrayList<>(statsByPlayer.values());
	}

	public PlayerStats empty(final String playerId)
	{
		return PlayerStats.builder().playerId(playerId).build();
	}

	private void count(final PlayerStats stats, final MatchOutcome outcome, final SportRules rules)
	{
		switch (outcome)
		{
			case WIN -> stats.setWins(stats.getWins() + 1);
			case DRAW -> stats.setDraws(stats.getDraws() + 1);
			case LOSS -> stats.setLosses(stats.getLosses() + 1);
		}

		stats.setTotal(stats.getTotal() + 1);
		stats.setPoints(stats.getPoints() + pointsFor(outcome, rules));
	}

	private int pointsFor(final MatchOutcome outcome, final SportRules rules)
	{
		return switch (outcome)
		{
			case WIN -> rules.getPointsForWin();
			case DRAW -> rules.getPointsForDraw();
			case LOSS -> rules.getPointsForLoss();
		};
	}

	private void applyWinPercentage(final PlayerStats stats)
	{
		if (stats.getTotal() == 0)
		{
			stats.setWinPercentage(0);
			return;
		}

		final double percentage = 100.0 * stats.getWins() / stats.getTotal();

		stats.setWinPercentage(Math.round(percentage * 100) / 100.0);
	}
}
