<script lang="ts">
    import {onMount} from 'svelte';
    import {replace} from 'svelte-spa-router';
    import {getRanking} from '../../lib/api/rankings.api';
    import {getAllGroups} from '../../lib/api/groups.api';
    import {getAllSports} from '../../lib/api/sports.api';
    import PageHeader from '../../lib/components/PageHeader.svelte';
    import RankingTable from '../../lib/components/RankingTable.svelte';
    import type {GroupResponse} from '../../lib/types/group.types';
    import type {SportResponse} from '../../lib/types/sport.types';
    import type {RankingEntryResponse} from '../../lib/types/stats.types';
    import {queryParams, withQuery} from '../../lib/utils/query';

    type Filters = {groupId: string; sportId: string};

    let entries = $state<RankingEntryResponse[]>([]);
    let groups = $state<GroupResponse[]>([]);
    let sports = $state<SportResponse[]>([]);
    let loading = $state(true);
    let error = $state<string | null>(null);
    // Greška pri učitavanju padajućih lista je odvojena, da je ne obriše sledeće učitavanje liste
    let filtersError = $state<string | null>(null);

    // Filteri stoje u adresi (npr. #/rankings?groupId=3&sportId=1), kao na listi mečeva
    const filters = $derived.by((): Filters => {
        const params = queryParams();
        return {groupId: params.get('groupId') ?? '', sportId: params.get('sportId') ?? ''};
    });
    const hasFilters = $derived(filters.groupId !== '' || filters.sportId !== '');

    // Sport iz adrese koji nije među aktivnim (obrisan) i dalje filtrira, pa ga i padajuća lista mora pokazati
    const deletedSport = $derived(filters.sportId !== '' && sports.length > 0 && !sports.some(s => s.id === filters.sportId));

    // Naslov kartice kaže šta se rangira, npr. „Tenis · Tenis kvarta“
    const scope = $derived(
        [sports.find(s => s.id === filters.sportId)?.name ?? (deletedSport ? 'Deleted sport' : 'All sports'),
         groups.find(g => g.id === filters.groupId)?.name ?? 'All groups'].join(' · ')
    );

    onMount(async () => {
        try {
            [groups, sports] = await Promise.all([getAllGroups(), getAllSports()]);
        } catch (e) {
            filtersError = e instanceof Error ? e.message : 'Failed to load filters';
        }
    });

    $effect(() => {
        load(filters);
    });

    // Odgovor koji stigne posle novijeg zahteva se odbacuje
    let lastRequest = 0;

    async function load(f: Filters) {
        const request = ++lastRequest;
        loading = true;
        error = null;
        try {
            const result = await getRanking(f);
            if (request !== lastRequest) return;
            entries = result;
        } catch (e) {
            if (request !== lastRequest) return;
            entries = [];
            error = e instanceof Error ? e.message : 'Failed to load rankings';
        } finally {
            if (request === lastRequest) loading = false;
        }
    }

    function applyFilters(next: Filters) {
        replace(withQuery('/rankings', next));
    }
</script>

<PageHeader title="Rankings" icon="bi-bar-chart-line" subtitle="Who is on top, by group and by sport."/>

<div class="container py-4 page-fade">
    {#if error}
        <div class="alert alert-danger d-flex align-items-center">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
        </div>
    {/if}

    <div class="row g-4">
        <!-- Na telefonu filteri idu iznad tabele, a na velikom ekranu u bočnu kolonu -->
        <div class="col-lg-4 order-lg-last">
            <div class="card mb-4">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span><i class="bi bi-funnel me-2 text-primary"></i>Filters</span>
                    {#if hasFilters}
                        <button class="btn btn-sm btn-link text-decoration-none p-0" onclick={() => applyFilters({groupId: '', sportId: ''})}>Clear</button>
                    {/if}
                </div>
                <div class="card-body">
                    {#if filtersError}
                        <div class="alert alert-danger py-2 small">{filtersError}</div>
                    {/if}
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
                    <div>
                        <label class="form-label" for="filterSport">Sport</label>
                        <select id="filterSport"
                                class="form-select"
                                onchange={e => applyFilters({...filters, sportId: e.currentTarget.value})}>
                            <option value="" selected={filters.sportId === ''}>All sports</option>
                            {#each sports as sport (sport.id)}
                                <option value={sport.id} selected={filters.sportId === sport.id}>{sport.name}</option>
                            {/each}
                            {#if deletedSport}
                                <option value={filters.sportId} selected>Deleted sport</option>
                            {/if}
                        </select>
                    </div>
                </div>
            </div>

            <div class="card d-none d-lg-block">
                <div class="card-header">
                    <i class="bi bi-info-circle me-2 text-primary"></i>How ranking works
                </div>
                <ul class="list-group list-group-flush small text-muted">
                    <li class="list-group-item">Each match gives points by its sport's rules for a win, draw or loss.</li>
                    <li class="list-group-item">Players are ordered by points, then wins. Tied players share a place.</li>
                    <li class="list-group-item">Matches of deleted sports still count.</li>
                </ul>
            </div>
        </div>

        <div class="col-lg-8">
            <div class="card">
                <div class="card-header text-truncate">
                    <i class="bi bi-trophy me-2 text-primary"></i>{scope}
                </div>

                {#if loading}
                    <div class="card-body text-center py-5">
                        <div class="spinner-border text-primary"></div>
                    </div>
                {:else if entries.length === 0}
                    <div class="card-body text-center text-muted py-5">
                        <i class="bi bi-bar-chart fs-1 d-block mb-2"></i>
                        No matches recorded here yet, so there is no ranking.
                    </div>
                {:else}
                    <RankingTable {entries}/>
                {/if}
            </div>
        </div>
    </div>
</div>
