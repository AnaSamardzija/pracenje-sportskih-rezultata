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

// Izjednačeni igrači (isti bodovi i pobede) dele mesto, a sledeće se preskače (1, 1, 3)
export interface RankingEntryResponse {
    rank: number;
    playerId: string;
    username: string;
    wins: number;
    draws: number;
    losses: number;
    total: number;
    points: number;
}
