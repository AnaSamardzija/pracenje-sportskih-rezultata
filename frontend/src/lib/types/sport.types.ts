// Vrednosti su tačno enum-i SportType i ScoringMode sa backend-a
export type SportType = 'INDIVIDUAL' | 'TEAM';
export type ScoringMode = 'POINTS' | 'SETS' | 'OUTCOME';

// maxPlayersPerSide, bestOf i pointsToWinSet nisu obavezni (bestOf i pointsToWinSet imaju smisla samo za SETS)
export interface SportRulesDto {
    allowDraw: boolean;
    minPlayersPerSide: number;
    maxPlayersPerSide: number | null;
    bestOf: number | null;
    pointsToWinSet: number | null;
    pointsForWin: number;
    pointsForDraw: number;
    pointsForLoss: number;
}

export interface SportRequest {
    name: string;
    type: SportType;
    scoringMode: ScoringMode;
    rules: SportRulesDto;
}

export interface SportResponse {
    id: string;
    name: string;
    type: SportType;
    scoringMode: ScoringMode;
    rules: SportRulesDto;
    active: boolean;
}

// Kratak prikaz sporta unutar drugog odgovora (npr. u meču)
export interface SportSummaryDto {
    id: string;
    name: string;
}

export const SPORT_TYPE_LABELS: Record<SportType, string> = {
    INDIVIDUAL: 'Individual',
    TEAM: 'Team'
};

export const SCORING_MODE_LABELS: Record<ScoringMode, string> = {
    POINTS: 'Points',
    SETS: 'Sets',
    OUTCOME: 'Outcome'
};

export const SCORING_MODES: ScoringMode[] = ['POINTS', 'SETS', 'OUTCOME'];

export const SCORING_MODE_HINTS: Record<ScoringMode, string> = {
    POINTS: 'Each side gets a final score, e.g. 3 : 1.',
    SETS: 'The result is a list of sets, e.g. 6-3 4-6 6-2.',
    OUTCOME: 'Only win, draw or loss is recorded, e.g. chess.'
};

export const SCORING_MODE_ICONS: Record<ScoringMode, string> = {
    POINTS: 'bi-123',
    SETS: 'bi-list-ol',
    OUTCOME: 'bi-flag-fill'
};

// Broj igrača po strani za prikaz: „1“, „2–4“ ili „2+“ kad gornja granica nije zadata
export function playersPerSide(rules: SportRulesDto): string {
    const {minPlayersPerSide: min, maxPlayersPerSide: max} = rules;
    if (max === null) return `${min}+`;
    return min === max ? `${min}` : `${min}–${max}`;
}
