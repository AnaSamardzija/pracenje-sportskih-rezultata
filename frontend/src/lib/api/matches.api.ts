import {apiFetch, errorMessage} from './client';
import type {MatchResponse} from '../types/match.types';

const BASE = '/api/v1/matches';

// Backend vraća mečeve od najnovijeg; nepostojeća grupa, sport ili igrač u filteru je 404
export async function getAllMatches(filters: {groupId?: string; sportId?: string; playerId?: string} = {}): Promise<MatchResponse[]> {
    const params = new URLSearchParams();
    if (filters.groupId) params.set('groupId', filters.groupId);
    if (filters.sportId) params.set('sportId', filters.sportId);
    if (filters.playerId) params.set('playerId', filters.playerId);

    const res = await apiFetch(params.size ? `${BASE}?${params}` : BASE);
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to load matches'));
    return res.json();
}
