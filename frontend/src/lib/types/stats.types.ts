// winPercentage je 0–100, na dve decimale
export interface PlayerStatsResponse {
    playerId: string;
    username: string;
    wins: number;
    draws: number;
    losses: number;
    total: number;
    points: number;
    winPercentage: number;
    longestWinStreak: number;
}
