// Pandan tabele player_stats: pobede, nerešeni i porazi po igraču, grupi i sportu.
// Ne čuva se, nego se računa iz mečeva pri svakom pozivu.
use('dual-db');
db.matches.aggregate([
	{
		$unwind: "$sides"
	},
	{
		$match: {
			"sides.outcome": {$ne: null}
		}
	},
	{
		$unwind: "$sides.playerIds"
	},
	{
		$group: {
			_id: {
				playerId: "$sides.playerIds",
				groupId: "$groupId",
				sportId: "$sportId"
			},
			wins: {$sum: {$cond: [{$eq: ["$sides.outcome", "WIN"]}, 1, 0]}},
			draws: {$sum: {$cond: [{$eq: ["$sides.outcome", "DRAW"]}, 1, 0]}},
			losses: {$sum: {$cond: [{$eq: ["$sides.outcome", "LOSS"]}, 1, 0]}}
		}
	},
	{
		$project: {
			_id: 0,
			playerId: "$_id.playerId",
			groupId: "$_id.groupId",
			sportId: "$_id.sportId",
			wins: "$wins",
			draws: "$draws",
			losses: "$losses"
		}
	},
	{
		$sort: {playerId: 1, groupId: 1, sportId: 1}
	}
]);
