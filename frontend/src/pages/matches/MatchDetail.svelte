<script lang="ts">
    import {push} from 'svelte-spa-router';
    import {deleteMatch, getMatch} from '../../lib/api/matches.api';
    import {getGroup} from '../../lib/api/groups.api';
    import {authStore} from '../../lib/auth/auth.store';
    import ConfirmModal from '../../lib/components/ConfirmModal.svelte';
    import PageHeader from '../../lib/components/PageHeader.svelte';
    import {OUTCOME_BADGES, sideNames, type MatchResponse, type MatchSideResponse} from '../../lib/types/match.types';
    import {formatDateTime} from '../../lib/utils/date';

    let {params}: {params?: {id?: string}} = $props();
    const matchId = $derived(params?.id ?? '');

    let match = $state<MatchResponse | null>(null);
    let canManage = $state(false);
    let loading = $state(true);
    let error = $state<string | null>(null);
    let showDelete = $state(false);
    let deleting = $state(false);

    const myId = $derived($authStore.user?.id);
    const title = $derived(match ? `${sideNames(match.sides[0])} vs ${sideNames(match.sides[1])}` : 'Match');
    const subtitle = $derived(match ? `${match.sport.name} · ${match.group.name} · ${formatDateTime(match.playedAt)}` : undefined);

    // Za POINTS i SETS u sredini je rezultat (za SETS broj osvojenih setova), a za OUTCOME se zna samo ishod
    const result = $derived.by(() => {
        if (!match) return '';
        const [home, away] = match.sides;
        if (home.score !== null && away.score !== null) return `${home.score} : ${away.score}`;
        return home.outcome === 'DRAW' ? 'Draw' : 'vs';
    });
    const setCount = $derived(match?.sides[0].setScores?.length ?? 0);

    // Učitavanje prati id iz adrese: ruter ne pravi stranicu ponovo kad se promeni samo id (npr. /matches/3 → /matches/5)
    $effect(() => {
        const id = matchId;
        match = null;
        canManage = false;
        loading = true;
        error = null;
        load(id);
    });

    // Menjati i brisati meč može učesnik ili GROUP_ADMIN grupe; ulogu u grupi pitamo samo kad korisnik ne igra.
    // Odgovor koji stigne za id koji više nije u adresi se odbacuje.
    async function load(id: string) {
        try {
            const loaded = await getMatch(id);
            const plays = loaded.sides.some(side => side.players.some(p => p.id === myId));
            const manage = plays || await isGroupAdmin(loaded.group.id);
            if (id !== matchId) return;
            match = loaded;
            canManage = manage;
        } catch (e) {
            if (id !== matchId) return;
            error = e instanceof Error ? e.message : 'Failed to load match';
        } finally {
            if (id === matchId) loading = false;
        }
    }

    // Služi samo za prikaz dugmadi; ako grupa ne može da se učita, dugmad se ne prikazuju, a backend svakako proverava sam
    async function isGroupAdmin(groupId: string): Promise<boolean> {
        try {
            return (await getGroup(groupId)).myRole === 'GROUP_ADMIN';
        } catch {
            return false;
        }
    }

    async function handleDelete() {
        error = null;
        deleting = true;
        try {
            await deleteMatch(matchId);
            push('/matches');
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to delete match';
        } finally {
            deleting = false;
        }
    }
</script>

{#snippet sideBlock(side: MatchSideResponse)}
    {@const badge = OUTCOME_BADGES[side.outcome]}
    <div class="text-center overflow-hidden">
        <span class="badge {badge.css} mb-3">{badge.label}</span>
        {#each side.players as player (player.id)}
            <div class="d-flex flex-wrap align-items-center justify-content-center gap-2 mb-2">
                <span class="icon-circle icon-circle-xs flex-shrink-0">{player.username[0].toUpperCase()}</span>
                <span class="text-break" class:fw-semibold={side.winner}>{player.username}</span>
                {#if player.id === myId}
                    <span class="badge text-bg-primary">You</span>
                {/if}
            </div>
        {/each}
    </div>
{/snippet}

<PageHeader {title} icon="bi-calendar-event" {subtitle}>
    {#snippet actions()}
        {#if match && canManage}
            <a class="btn btn-light" href="#/matches/{match.id}/edit"><i class="bi bi-pencil me-1"></i>Edit</a>
            <button class="btn btn-outline-light" disabled={deleting} onclick={() => showDelete = true}>
                <i class="bi bi-trash me-1"></i>Delete
            </button>
        {/if}
    {/snippet}
</PageHeader>

<div class="container py-4 page-fade">
    {#if error}
        <div class="alert alert-danger d-flex align-items-center">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
        </div>
    {/if}

    {#if loading}
        <div class="text-center py-5">
            <div class="spinner-border text-primary"></div>
        </div>
    {:else if !match}
        <a class="btn btn-outline-secondary" href="#/matches"><i class="bi bi-arrow-left me-1"></i>Back to matches</a>
    {:else}
        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-trophy me-2 text-primary"></i>Result
                    </div>
                    <div class="card-body py-4">
                        <!-- Strane jedna naspram druge, rezultat u sredini -->
                        <div class="scoreboard">
                            {@render sideBlock(match.sides[0])}
                            <div class="scoreboard-score">{result}</div>
                            {@render sideBlock(match.sides[1])}
                        </div>
                    </div>

                    {#if setCount > 0}
                        <div class="table-responsive border-top">
                            <table class="table align-middle text-center mb-0">
                                <thead>
                                <tr>
                                    <th class="text-start">Side</th>
                                    {#each {length: setCount}, s}
                                        <th>Set {s + 1}</th>
                                    {/each}
                                    <th>Sets</th>
                                </tr>
                                </thead>
                                <tbody>
                                {#each match.sides as side, i (i)}
                                    {@const other = match.sides[1 - i]}
                                    <tr>
                                        <td class="text-start text-nowrap" class:fw-semibold={side.winner}>{sideNames(side)}</td>
                                        {#each side.setScores ?? [] as games, s (s)}
                                            <td class:fw-bold={games > (other.setScores?.[s] ?? 0)}>{games}</td>
                                        {/each}
                                        <td class="fw-bold">{side.score}</td>
                                    </tr>
                                {/each}
                                </tbody>
                            </table>
                        </div>
                    {/if}
                </div>
            </div>

            <!-- Bočna kolona: gde i kada je meč odigran i ko ga je uneo -->
            <div class="col-lg-4">
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-info-circle me-2 text-primary"></i>Details
                    </div>
                    <ul class="list-group list-group-flush">
                        <li class="list-group-item d-flex justify-content-between gap-3">
                            <span class="text-muted">Sport</span><span class="text-end">{match.sport.name}</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between gap-3">
                            <span class="text-muted">Group</span>
                            <a class="text-end text-decoration-none" href="#/groups/{match.group.id}">{match.group.name}</a>
                        </li>
                        <li class="list-group-item d-flex justify-content-between gap-3">
                            <span class="text-muted">Played</span><span class="text-end">{formatDateTime(match.playedAt)}</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between gap-3">
                            <span class="text-muted">Recorded by</span><span class="text-end">{match.recordedBy.username}</span>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    {/if}
</div>

<ConfirmModal
    bind:show={showDelete}
    title="Delete match"
    message="Delete this match permanently? It will no longer count in rankings and statistics."
    onConfirm={handleDelete}
/>
