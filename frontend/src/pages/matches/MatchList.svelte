<script lang="ts">
    import {onMount} from 'svelte';
    import {replace} from 'svelte-spa-router';
    import {getAllMatches} from '../../lib/api/matches.api';
    import {getAllGroups} from '../../lib/api/groups.api';
    import {getAllSports} from '../../lib/api/sports.api';
    import {authStore} from '../../lib/auth/auth.store';
    import MatchItem from '../../lib/components/MatchItem.svelte';
    import PageHeader from '../../lib/components/PageHeader.svelte';
    import type {GroupResponse} from '../../lib/types/group.types';
    import type {MatchResponse} from '../../lib/types/match.types';
    import type {SportResponse} from '../../lib/types/sport.types';
    import {queryParams, withQuery} from '../../lib/utils/query';

    type Filters = {groupId: string; sportId: string; mine: boolean};

    let matches = $state<MatchResponse[]>([]);
    let groups = $state<GroupResponse[]>([]);
    let sports = $state<SportResponse[]>([]);
    let loading = $state(true);
    let error = $state<string | null>(null);

    // Filteri stoje u adresi (npr. #/matches?groupId=3&mine=true), pa druga stranica može da otvori već filtriranu listu,
    // a osvežavanje i dugme nazad ih čuvaju
    const filters = $derived.by((): Filters => {
        const params = queryParams();
        return {
            groupId: params.get('groupId') ?? '',
            sportId: params.get('sportId') ?? '',
            mine: params.get('mine') === 'true'
        };
    });
    const hasFilters = $derived(filters.groupId !== '' || filters.sportId !== '' || filters.mine);

    // Grupe i sportovi služe samo za padajuće liste filtera
    onMount(async () => {
        try {
            [groups, sports] = await Promise.all([getAllGroups(), getAllSports()]);
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to load filters';
        }
    });

    $effect(() => {
        load(filters);
    });

    // Svaka promena filtera šalje nov zahtev; odgovor koji stigne posle novijeg zahteva se odbacuje
    let lastRequest = 0;

    async function load(f: Filters) {
        const request = ++lastRequest;
        loading = true;
        error = null;
        try {
            const result = await getAllMatches({
                groupId: f.groupId,
                sportId: f.sportId,
                playerId: f.mine ? $authStore.user?.id : undefined
            });
            if (request !== lastRequest) return;
            matches = result;
        } catch (e) {
            if (request !== lastRequest) return;
            matches = [];
            error = e instanceof Error ? e.message : 'Failed to load matches';
        } finally {
            if (request === lastRequest) loading = false;
        }
    }

    // Promena filtera menja samo adresu; učitavanje pokreće $effect iznad
    function applyFilters(next: Filters) {
        replace(withQuery('/matches', {groupId: next.groupId, sportId: next.sportId, mine: next.mine ? 'true' : ''}));
    }

    const clearFilters = () => applyFilters({groupId: '', sportId: '', mine: false});
</script>

<PageHeader title="Matches" icon="bi-calendar-event" subtitle="Every recorded match, newest first.">
    {#snippet actions()}
        <a class="btn btn-light" href="#/matches/new">
            <i class="bi bi-plus-lg me-1"></i>Record match
        </a>
    {/snippet}
</PageHeader>

<div class="container py-4 page-fade">
    {#if error}
        <div class="alert alert-danger d-flex align-items-center">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
        </div>
    {/if}

    <div class="row g-4">
        <!-- Na telefonu filteri idu iznad liste, a na velikom ekranu u bočnu kolonu -->
        <div class="col-lg-4 order-lg-last">
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span><i class="bi bi-funnel me-2 text-primary"></i>Filters</span>
                    {#if hasFilters}
                        <button class="btn btn-sm btn-link text-decoration-none p-0" onclick={clearFilters}>Clear</button>
                    {/if}
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <label class="form-label" for="filterGroup">Group</label>
                        <select id="filterGroup"
                                class="form-select"
                                onchange={e => applyFilters({...filters, groupId: e.currentTarget.value})}>
                            <option value="" selected={filters.groupId === ''}>All groups</option>
                            {#each groups as group (group.id)}
                                <option value={group.id} selected={filters.groupId === group.id}>{group.name}</option>
                            {/each}
                        </select>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="filterSport">Sport</label>
                        <select id="filterSport"
                                class="form-select"
                                onchange={e => applyFilters({...filters, sportId: e.currentTarget.value})}>
                            <option value="" selected={filters.sportId === ''}>All sports</option>
                            {#each sports as sport (sport.id)}
                                <option value={sport.id} selected={filters.sportId === sport.id}>{sport.name}</option>
                            {/each}
                        </select>
                    </div>
                    <div class="form-check form-switch">
                        <input id="filterMine"
                               class="form-check-input"
                               type="checkbox"
                               role="switch"
                               checked={filters.mine}
                               onchange={e => applyFilters({...filters, mine: e.currentTarget.checked})}/>
                        <label class="form-check-label" for="filterMine">Only my matches</label>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-8">
            <div class="card">
                <div class="card-header">
                    <i class="bi bi-list-ul me-2 text-primary"></i>Results
                    {#if !loading}
                        <span class="badge bg-primary-subtle text-primary-emphasis ms-1">{matches.length}</span>
                    {/if}
                </div>

                {#if loading}
                    <div class="card-body text-center py-5">
                        <div class="spinner-border text-primary"></div>
                    </div>
                {:else if matches.length === 0}
                    <div class="card-body text-center text-muted py-5">
                        <i class="bi bi-calendar-x fs-1 d-block mb-2"></i>
                        {#if hasFilters}
                            No matches for these filters.
                            <div class="mt-3">
                                <button class="btn btn-outline-primary btn-sm" onclick={clearFilters}>Clear filters</button>
                            </div>
                        {:else}
                            No matches yet. Record the first one after you play.
                            <div class="mt-3">
                                <a class="btn btn-primary btn-sm" href="#/matches/new">Record match</a>
                            </div>
                        {/if}
                    </div>
                {:else}
                    <div class="list-group list-group-flush">
                        {#each matches as match (match.id)}
                            <MatchItem {match}/>
                        {/each}
                    </div>
                {/if}
            </div>
        </div>
    </div>
</div>
