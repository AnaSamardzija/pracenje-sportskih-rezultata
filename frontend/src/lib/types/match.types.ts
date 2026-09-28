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

// Šalje se samo polje koje odgovara načinu bodovanja sporta (score, setScores ili outcome), ostala su null,
// jer backend na suvišno polje vraća 422; pobednika za POINTS i SETS računa sam
export interface MatchSideRequest {
    playerIds: string[];
    score: number | null;
    setScores: number[] | null;
    outcome: MatchOutcome | null;
}

// playedAt je lokalno vreme bez zone, npr. „2026-09-20T18:30“, i ne sme biti u budućnosti
export interface MatchRequest {
    sportId: string;
    groupId: string;
    playedAt: string;
    sides: MatchSideRequest[];
}

export interface MatchResponse {
    id: string;
    sport: SportSummaryDto;
    group: GroupSummaryDto;
    playedAt: string;
    recordedBy: PlayerDto;
    sides: MatchSideResponse[];
}

// Značka ishoda iz ugla jedne strane (W / D / L)
export const OUTCOME_BADGES: Record<MatchOutcome, {letter: string; label: string; css: string}> = {
    WIN: {letter: 'W', label: 'Win', css: 'text-bg-success'},
    DRAW: {letter: 'D', label: 'Draw', css: 'text-bg-secondary'},
    LOSS: {letter: 'L', label: 'Loss', css: 'text-bg-danger'}
};

// Igrači jedne strane u jednom redu, npr. „pera, mika“ za timski meč
export function sideNames(side: MatchSideResponse): string {
    return side.players.map(p => p.username).join(', ');
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
