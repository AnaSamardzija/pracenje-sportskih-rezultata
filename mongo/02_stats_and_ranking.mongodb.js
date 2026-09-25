// Statistika igrača: pandan funkcija fn_total_matches, fn_win_percentage i fn_points_for_outcome.
// sportId = null znači svi sportovi.
use('dual-db');
var playerId = "id-igraca";
var sportId = null;

var playerFilter = {"sides.playerIds": playerId};
if (sportId !== null) {
	playerFilter.sportId = sportId;
}

db.matches.aggregate([
	{
		$match: playerFilter
	},
	{
		$unwind: "$sides"
	},
	{
		$unwind: "$sides.playerIds"
	},
	{
		$match: {
			"sides.playerIds": playerId,
			"sides.outcome": {$ne: null}
		}
	},
	{
		$addFields: {
			sportObjectId: {$toObjectId: "$sportId"}
		}
	},
	{
		$lookup: {
			from: "sports",
			localField: "sportObjectId",
			foreignField: "_id",
			as: "sport"
		}
	},
	{
		$unwind: "$sport"
	},
	{
		$group: {
			_id: "$sides.playerIds",
			wins: {$sum: {$cond: [{$eq: ["$sides.outcome", "WIN"]}, 1, 0]}},
			draws: {$sum: {$cond: [{$eq: ["$sides.outcome", "DRAW"]}, 1, 0]}},
			losses: {$sum: {$cond: [{$eq: ["$sides.outcome", "LOSS"]}, 1, 0]}},
			total: {$sum: 1},
			points: {
				$sum: {
					$cond: [
						{$eq: ["$sides.outcome", "WIN"]},
						"$sport.rules.pointsForWin",
						{$cond: [{$eq: ["$sides.outcome", "DRAW"]}, "$sport.rules.pointsForDraw", "$sport.rules.pointsForLoss"]}
					]
				}
			}
		}
	},
	{
		$addFields: {
			playerObjectId: {$toObjectId: "$_id"}
		}
	},
	{
		$lookup: {
			from: "users",
			localField: "playerObjectId",
			foreignField: "_id",
			as: "user"
		}
	},
	{
		$unwind: "$user"
	},
	{
		$project: {
			_id: 0,
			playerId: "$_id",
			username: "$user.username",
			wins: "$wins",
			draws: "$draws",
			losses: "$losses",
			total: "$total",
			points: "$points",
			winPercentage: {
				$divide: [
					{$floor: {$add: [{$multiply: [{$divide: [{$multiply: [100, "$wins"]}, "$total"]}, 100]}, 0.5]}},
					100
				]
			}
		}
	}
]);

// Rang-lista: pandan procedure sp_group_ranking.
// groupId = null i sportId = null znače sve grupe i sve sportove.
use('dual-db');
var groupId = null;
var sportId = null;

var rankingFilter = {};
if (groupId !== null) {
	rankingFilter.groupId = groupId;
}
if (sportId !== null) {
	rankingFilter.sportId = sportId;
}

db.matches.aggregate([
	{
		$match: rankingFilter
	},
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
		$addFields: {
			sportObjectId: {$toObjectId: "$sportId"}
		}
	},
	{
		$lookup: {
			from: "sports",
			localField: "sportObjectId",
			foreignField: "_id",
			as: "sport"
		}
	},
	{
		$unwind: "$sport"
	},
	{
		$group: {
			_id: "$sides.playerIds",
			wins: {$sum: {$cond: [{$eq: ["$sides.outcome", "WIN"]}, 1, 0]}},
			draws: {$sum: {$cond: [{$eq: ["$sides.outcome", "DRAW"]}, 1, 0]}},
			losses: {$sum: {$cond: [{$eq: ["$sides.outcome", "LOSS"]}, 1, 0]}},
			total: {$sum: 1},
			points: {
				$sum: {
					$cond: [
						{$eq: ["$sides.outcome", "WIN"]},
						"$sport.rules.pointsForWin",
						{$cond: [{$eq: ["$sides.outcome", "DRAW"]}, "$sport.rules.pointsForDraw", "$sport.rules.pointsForLoss"]}
					]
				}
			}
		}
	},
	{
		$addFields: {
			playerObjectId: {$toObjectId: "$_id"}
		}
	},
	{
		$lookup: {
			from: "users",
			localField: "playerObjectId",
			foreignField: "_id",
			as: "user"
		}
	},
	{
		$unwind: "$user"
	},
	{
		$addFields: {
			score: {points: "$points", wins: "$wins"}
		}
	},
	{
		$setWindowFields: {
			sortBy: {score: -1},
			output: {
				rank: {$rank: {}}
			}
		}
	},
	{
		$sort: {rank: 1, "user.username": 1}
	},
	{
		$project: {
			_id: 0,
			rank: "$rank",
			playerId: "$_id",
			username: "$user.username",
			wins: "$wins",
			draws: "$draws",
			losses: "$losses",
			total: "$total",
			points: "$points"
		}
	}
]);
