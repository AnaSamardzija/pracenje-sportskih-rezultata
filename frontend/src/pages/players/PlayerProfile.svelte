<script lang="ts">
    import {replace} from 'svelte-spa-router';
    import {getAllMatches} from '../../lib/api/matches.api';
    import {getUserStats} from '../../lib/api/users.api';
    import {authStore} from '../../lib/auth/auth.store';
    import MatchItem from '../../lib/components/MatchItem.svelte';
    import PageHeader from '../../lib/components/PageHeader.svelte';
    import StatCards from '../../lib/components/StatCards.svelte';
    import type {MatchResponse} from '../../lib/types/match.types';
    import type {PlayerStatsResponse} from '../../lib/types/stats.types';
    import {queryParams, withQuery} from '../../lib/utils/query';

    // Profil igrača, i sopstveni i tuđi: statistika iz /users/{id}/stats (daje i username) i istorija mečeva.
    // Sport je u adresi (npr. #/players/5?sportId=2) i sužava i statistiku i istoriju.
    let {params}: {params?: {id?: string}} = $props();
    const playerId = $derived(params?.id ?? '');
    const sportId = $derived(queryParams().get('sportId') ?? '');

    let matches = $state<MatchResponse[]>([]);
    let stats = $state<PlayerStatsResponse | null>(null);
    let username = $state('');
    let loading = $state(true);
    let error = $state<string | null>(null);

    const isMe = $derived(playerId === $authStore.user?.id);

    // Sportovi koje je igrač igrao, sa brojem mečeva, izvedeni iz cele istorije (najigraniji prvi)
    const playedSports = $derived.by(() => {
        const counts = new Map<string, {id: string; name: string; count: number}>();
        for (const match of matches) {
            const entry = counts.get(match.sport.id) ?? {id: match.sport.id, name: match.sport.name, count: 0};
            entry.count++;
            counts.set(match.sport.id, entry);
        }
        return [...counts.values()].sort((a, b) => b.count - a.count);
    });
    const sportName = $derived(playedSports.find(s => s.id === sportId)?.name ?? null);
    const shownMatches = $derived(sportId ? matches.filter(m => m.sport.id === sportId) : matches);

    // Istorija se učitava jednom po igraču (sport se filtrira ovde), a statistika i za svaki sport posebno.
    // Ruter ne pravi stranicu ponovo kad se promeni samo id (npr. sa profila na profil), pa se tada sve vraća na početak.
    $effect(() => {
        const id = playerId;
        matches = [];
        stats = null;
        username = '';
        loading = true;
        error = null;
        loadMatches(id);
    });

    $effect(() => {
        loadStats(playerId, sportId);
    });

    async function loadMatches(id: string) {
        try {
            const result = await getAllMatches({playerId: id});
            if (id === playerId) matches = result;
        } catch (e) {
            if (id === playerId) error = e instanceof Error ? e.message : 'Failed to load matches';
        } finally {
            if (id === playerId) loading = false;
        }
    }

    // Odgovor za igrača ili sport koji više nisu u adresi se odbacuje
    async function loadStats(id: string, sport: string) {
        try {
            const result = await getUserStats(id, sport || undefined);
            if (id !== playerId || sport !== sportId) return;
            stats = result;
            username = result.username;
        } catch (e) {
            if (id !== playerId || sport !== sportId) return;
            error = e instanceof Error ? e.message : 'Failed to load statistics';
        }
    }

    function showSport(id: string) {
        replace(withQuery(`/players/${playerId}`, {sportId: id}));
    }
</script>

<PageHeader title={username || 'Player'}
            icon="bi-person-circle"
            subtitle={isMe ? 'Your matches and statistics.' : 'Matches and statistics.'}
            overlap>
    {#snippet actions()}
        {#if isMe}
            <a class="btn btn-light" href="#/profile"><i class="bi bi-gear me-1"></i>Account settings</a>
        {/if}
    {/snippet}
</PageHeader>

<div class="container pb-5 page-fade">
    {#if error}
        <div class="alert alert-danger d-flex align-items-center page-header-pull">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
        </div>
    {:else if !stats}
        <div class="card page-header-pull">
            <div class="card-body text-center py-5">
                <div class="spinner-border text-primary"></div>
            </div>
        </div>
    {:else}
        <!-- Kartice statistike delimično prelaze preko trake zaglavlja, kao na početnoj -->
        <div class="page-header-pull mb-4">
            <StatCards {stats} scope={sportName ? `in ${sportName}` : 'across all sports'}/>
        </div>

        <div class="row g-4">
            <!-- Na telefonu izbor sporta ide iznad istorije, a na velikom ekranu u bočnu kolonu -->
            <div class="col-lg-4 order-lg-last">
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-bullseye me-2 text-primary"></i>Sports
                    </div>
                    <div class="card-body">
                        <nav class="nav nav-pills nav-pills-app flex-column gap-1" aria-label="Filter by sport">
                            <button class="nav-link d-flex justify-content-between align-items-center" class:active={!sportId} onclick={() => showSport('')}>
                                All sports <span class="badge bg-primary-subtle text-primary-emphasis">{matches.length}</span>
                            </button>
                            {#each playedSports as sport (sport.id)}
                                <button class="nav-link d-flex justify-content-between align-items-center text-start" class:active={sportId === sport.id} onclick={() => showSport(sport.id)}>
                                    {sport.name} <span class="badge bg-primary-subtle text-primary-emphasis">{sport.count}</span>
                                </button>
                            {/each}
                        </nav>
                    </div>
                </div>
            </div>

            <div class="col-lg-8">
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-clock-history me-2 text-primary"></i>Match history
                        {#if sportName}<span class="text-muted fw-normal"> · {sportName}</span>{/if}
                    </div>
                    {#if loading}
                        <div class="card-body text-center py-5">
                            <div class="spinner-border text-primary"></div>
                        </div>
                    {:else if shownMatches.length === 0}
                        <div class="card-body text-center text-muted py-5">
                            <i class="bi bi-calendar-x fs-1 d-block mb-2"></i>
                            {isMe ? 'You have not played any matches here yet.' : 'No matches here yet.'}
                        </div>
                    {:else}
                        <div class="list-group list-group-flush">
                            {#each shownMatches as match (match.id)}
                                <MatchItem {match} {playerId}/>
                            {/each}
                        </div>
                    {/if}
                </div>
            </div>
        </div>
    {/if}
</div>
