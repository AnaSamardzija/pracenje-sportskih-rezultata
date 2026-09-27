import type {SportSummaryDto} from './sport.types';
import type {GroupSummaryDto} from './group.types';

// Vrednosti su tačno enum MatchOutcome sa backend-a
export type MatchOutcome = 'WIN' | 'DRAW' | 'LOSS';

export interface PlayerDto {
    id: string;
    username: string;
}

// score postoji samo za POINTS, setScores samo za SETS; za OUTCOME se zna samo ishod
export interface MatchSideResponse {
    players: PlayerDto[];
    score: number | null;
    setScores: number[] | null;
    outcome: MatchOutcome;
    winner: boolean;
}

export interface MatchResponse {
    id: string;
    sport: SportSummaryDto;
    group: GroupSummaryDto;
    playedAt: string;
    recordedBy: PlayerDto;
    sides: MatchSideResponse[];
}

// Rezultat iz ugla jedne strane (njen rezultat prvi): „6-3 4-6 6-2“, „3 : 1“ ili prazno kad se beleži samo ishod
export function formatScore(mine: MatchSideResponse, other: MatchSideResponse): string {
    if (mine.setScores?.length && other.setScores?.length) {
        return mine.setScores.map((s, i) => `${s}-${other.setScores![i]}`).join(' ');
    }
    if (mine.score !== null && other.score !== null) {
        return `${mine.score} : ${other.score}`;
    }
    return '';
}
