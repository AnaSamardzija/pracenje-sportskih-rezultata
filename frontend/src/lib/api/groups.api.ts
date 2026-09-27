import {apiFetch, errorMessage} from './client';
import type {AddMemberRequest, GroupRequest, GroupResponse, MemberResponse} from '../types/group.types';

const BASE = '/api/v1/groups';

// mine = samo grupe prijavljenog korisnika, search = deo imena; oba filtera su opciona
export async function getAllGroups(filters: {mine?: boolean; search?: string} = {}): Promise<GroupResponse[]> {
    const params = new URLSearchParams();
    if (filters.mine) params.set('mine', 'true');
    if (filters.search) params.set('search', filters.search);

    const res = await apiFetch(params.size ? `${BASE}?${params}` : BASE);
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to load groups'));
    return res.json();
}

export async function getGroup(id: string): Promise<GroupResponse> {
    const res = await apiFetch(`${BASE}/${id}`);
    if (!res.ok) throw new Error(await errorMessage(res, 'Group not found'));
    return res.json();
}

// Ko napravi grupu, postaje njen GROUP_ADMIN
export async function createGroup(data: GroupRequest): Promise<GroupResponse> {
    const res = await apiFetch(BASE, {
        method: 'POST',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to create group'));
    return res.json();
}

export async function updateGroup(id: string, data: GroupRequest): Promise<GroupResponse> {
    const res = await apiFetch(`${BASE}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to update group'));
    return res.json();
}

// Grupa sa odigranim mečevima se ne može obrisati (422)
export async function deleteGroup(id: string): Promise<void> {
    const res = await apiFetch(`${BASE}/${id}`, {method: 'DELETE'});
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to delete group'));
}

export async function getMembers(id: string): Promise<MemberResponse[]> {
    const res = await apiFetch(`${BASE}/${id}/members`);
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to load members'));
    return res.json();
}

// Samo GROUP_ADMIN; nepostojeći korisnik 404, deaktiviran 422, već član 409
export async function addMember(id: string, data: AddMemberRequest): Promise<MemberResponse> {
    const res = await apiFetch(`${BASE}/${id}/members`, {
        method: 'POST',
        body: JSON.stringify(data)
    });
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to add member'));
    return res.json();
}

export async function joinGroup(id: string): Promise<void> {
    const res = await apiFetch(`${BASE}/${id}/members/me`, {method: 'POST'});
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to join group'));
}

// Poslednji admin koji ode predaje ulogu najstarijem članu; jedini član ne može da ode (422)
export async function leaveGroup(id: string): Promise<void> {
    const res = await apiFetch(`${BASE}/${id}/members/me`, {method: 'DELETE'});
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to leave group'));
}

export async function removeMember(id: string, userId: string): Promise<void> {
    const res = await apiFetch(`${BASE}/${id}/members/${userId}`, {method: 'DELETE'});
    if (!res.ok) throw new Error(await errorMessage(res, 'Failed to remove member'));
}
