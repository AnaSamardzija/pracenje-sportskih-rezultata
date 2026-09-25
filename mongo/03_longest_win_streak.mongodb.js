// Najduži niz pobeda: pandan procedure sp_longest_win_streak.
// sportId = null znači svi sportovi.
use('dual-db');
var playerId = "id-igraca";
var sportId = null;

var streakFilter = {"sides.playerIds": playerId};
if (sportId !== null) {
	streakFilter.sportId = sportId;
}

var cursor = db.matches.find(streakFilter).sort({playedAt: 1, _id: 1});
var current = 0;
var longest = 0;

while (cursor.hasNext()) {
	var match = cursor.next();
	var side = match.sides.find(s => s.playerIds.includes(playerId));

	if (side.outcome === "WIN") {
		current++;
		longest = Math.max(longest, current);
	} else {
		current = 0;
	}
}

print(longest);
