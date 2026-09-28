package rs.ac.ni.pmf.ana.dualdb.service;

import org.springframework.stereotype.Component;
import rs.ac.ni.pmf.ana.dualdb.exception.InvalidOperationException;
import rs.ac.ni.pmf.ana.dualdb.model.Match;
import rs.ac.ni.pmf.ana.dualdb.model.MatchOutcome;
import rs.ac.ni.pmf.ana.dualdb.model.MatchSide;
import rs.ac.ni.pmf.ana.dualdb.model.Sport;
import rs.ac.ni.pmf.ana.dualdb.model.SportRules;

import java.util.List;

/**
 * Sistem određuje pobednika iz unetog rezultata, prema scoringMode-u sporta.
 * Proverava i da uneti oblik rezultata odgovara modu, pa na svakoj strani
 * postavlja score (za SETS), winner i outcome.
 */
@Component
public class MatchResultResolver
{
	public void applyResult(final Sport sport, final Match match)
	{
		switch (sport.getScoringMode())
		{
			case POINTS -> applyPoints(sport, match);
			case SETS -> applySets(sport, match);
			case OUTCOME -> applyOutcome(sport, match);
		}
	}

	private void applyPoints(final Sport sport, final Match match)
	{
		for (final MatchSide side : match.getSides())
		{
			if (side.getScore() == null)
			{
				throw new InvalidOperationException("score is required on every side for POINTS scoring");
			}

			if (side.getScore() < 0)
			{
				throw new InvalidOperationException("score cannot be negative");
			}

			requireNoSetScores(side, "POINTS");
			requireNoOutcome(side, "POINTS");
		}

		decideByScore(sport, match);
	}

	private void applySets(final Sport sport, final Match match)
	{
		final int setCount = match.getSides().get(0).getSetScores().size();

		for (final MatchSide side : match.getSides())
		{
			if (side.getSetScores().isEmpty())
			{
				throw new InvalidOperationException("setScores are required on every side for SETS scoring");
			}

			if (side.getSetScores().size() != setCount)
			{
				throw new InvalidOperationException("all sides must have the same number of sets");
			}

			if (side.getSetScores().stream().anyMatch(score -> score == null || score < 0))
			{
				throw new InvalidOperationException("set score cannot be null or negative");
			}

			if (side.getScore() != null)
			{
				throw new InvalidOperationException("score is computed by the system for SETS scoring");
			}

			requireNoOutcome(side, "SETS");
		}

		final SportRules rules = sport.getRules();

		if (rules != null && rules.getBestOf() != null && setCount > rules.getBestOf())
		{
			throw new InvalidOperationException("a match cannot have more than " + rules.getBestOf() + " sets");
		}

		for (final MatchSide side : match.getSides())
		{
			side.setScore(setsWonBy(side, match.getSides(), setCount));
		}

		decideByScore(sport, match);
	}

	private void applyOutcome(final Sport sport, final Match match)
	{
		for (final MatchSide side : match.getSides())
		{
			if (side.getOutcome() == null)
			{
				throw new InvalidOperationException("outcome is required on every side for OUTCOME scoring");
			}

			if (side.getScore() != null)
			{
				throw new InvalidOperationException("score is not used for OUTCOME scoring");
			}

			requireNoSetScores(side, "OUTCOME");
		}

		final long draws = match.getSides().stream()
				.filter(side -> side.getOutcome() == MatchOutcome.DRAW)
				.count();

		if (draws > 0)
		{
			if (draws != match.getSides().size())
			{
				throw new InvalidOperationException("a draw must be recorded on every side");
			}

			requireDrawAllowed(sport);
		}
		else if (match.getSides().stream().filter(side -> side.getOutcome() == MatchOutcome.WIN).count() != 1)
		{
			throw new InvalidOperationException("exactly one side must have outcome WIN");
		}

		match.getSides().forEach(side -> side.setWinner(side.getOutcome() == MatchOutcome.WIN));
	}

	private void decideByScore(final Sport sport, final Match match)
	{
		final int best = match.getSides().stream()
				.mapToInt(MatchSide::getScore)
				.max()
				.orElse(0);

		final boolean draw = match.getSides().stream()
				.filter(side -> side.getScore() == best)
				.count() > 1;

		if (draw)
		{
			requireDrawAllowed(sport);
		}

		for (final MatchSide side : match.getSides())
		{
			final boolean winner = !draw && side.getScore() == best;

			side.setWinner(winner);
			side.setOutcome(draw ? MatchOutcome.DRAW : winner ? MatchOutcome.WIN : MatchOutcome.LOSS);
		}
	}

	private int setsWonBy(final MatchSide side, final List<MatchSide> sides, final int setCount)
	{
		int won = 0;

		for (int setIndex = 0; setIndex < setCount; setIndex++)
		{
			final int index = setIndex;
			final int best = sides.stream()
					.mapToInt(other -> other.getSetScores().get(index))
					.max()
					.orElse(0);

			final boolean shared = sides.stream()
					.filter(other -> other.getSetScores().get(index) == best)
					.count() > 1;

			if (!shared && side.getSetScores().get(setIndex) == best)
			{
				won++;
			}
		}

		return won;
	}

	private void requireDrawAllowed(final Sport sport)
	{
		if (sport.getRules() == null || !sport.getRules().isAllowDraw())
		{
			throw new InvalidOperationException("Sport " + sport.getName() + " does not allow a draw");
		}
	}

	private void requireNoSetScores(final MatchSide side, final String scoringMode)
	{
		if (!side.getSetScores().isEmpty())
		{
			throw new InvalidOperationException("setScores are not used for " + scoringMode + " scoring");
		}
	}

	private void requireNoOutcome(final MatchSide side, final String scoringMode)
	{
		if (side.getOutcome() != null)
		{
			throw new InvalidOperationException("outcome is computed by the system for " + scoringMode + " scoring");
		}
	}
}
