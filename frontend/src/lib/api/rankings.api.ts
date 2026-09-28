import {apiFetch, errorMessage} from './client';
import {withQuery} from '../utils/query';
import type {RankingEntryResponse} from '../types/stats.types';

const BASE = '/api/v1/rankings';

// Bez filtera se računaju svi mečevi; nepostojeća grupa ili sport je 404
export async function getRanking(filters: {groupId?: string; sportId?: string} = {}): Promise<RankingEntryResponse[]> {
    const res = await apiFetch(withQuery(BASE, filters));
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to load rankings'));
    return res.json();
}
