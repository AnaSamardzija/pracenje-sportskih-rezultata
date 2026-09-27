<script lang="ts">
    import {onMount} from 'svelte';
    import {deleteSport, getAllSports, restoreSport} from '../../lib/api/sports.api';
    import {hasRole} from '../../lib/auth/auth.guard';
    import ConfirmModal from '../../lib/components/ConfirmModal.svelte';
    import {
        playersPerSide,
        SCORING_MODE_ICONS,
        SCORING_MODE_LABELS,
        SPORT_TYPE_LABELS,
        type SportResponse
    } from '../../lib/types/sport.types';

    // Katalog vide svi; dodavanje, izmenu, brisanje i vraćanje obrisanih samo SYSTEM_ADMIN
    const isAdmin = hasRole('SYSTEM_ADMIN');

    let sports = $state<SportResponse[]>([]);
    let loading = $state(true);
    let error = $state<string | null>(null);
    let showDeleted = $state(false);

    let showModal = $state(false);
    let selected = $state<SportResponse | null>(null);

    onMount(load);

    async function load() {
        loading = true;
        error = null;
        try {
            sports = await getAllSports(showDeleted);
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to load sports';
        } finally {
            loading = false;
        }
    }

    function toggleDeleted(e: Event) {
        showDeleted = (e.currentTarget as HTMLInputElement).checked;
        load();
    }

    function confirmDelete(sport: SportResponse) {
        selected = sport;
        showModal = true;
    }

    // Obrisan sport ostaje u listi samo kad su obrisani prikazani, i to kao neaktivan
    async function handleDelete() {
        if (!selected) return;
        const id = selected.id;
        error = null;
        try {
            await deleteSport(id);
            sports = showDeleted
                ? sports.map(s => s.id === id ? {...s, active: false} : s)
                : sports.filter(s => s.id !== id);
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to delete sport';
        }
    }

    async function handleRestore(id: string) {
        error = null;
        try {
            const restored = await restoreSport(id);
            sports = sports.map(s => s.id === id ? restored : s);
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to restore sport';
        }
    }
</script>

<div class="container py-4 page-fade">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
        <div>
            <h2 class="mb-1"><i class="bi bi-bullseye me-2 text-primary"></i>Sports</h2>
            <p class="text-muted mb-0">Sports you can record matches in, with their scoring rules.</p>
        </div>

        {#if isAdmin}
            <div class="d-flex align-items-center gap-3">
                <div class="form-check form-switch mb-0">
                    <input id="showDeleted"
                           class="form-check-input"
                           type="checkbox"
                           role="switch"
                           checked={showDeleted}
                           onchange={toggleDeleted}/>
                    <label class="form-check-label" for="showDeleted">Show deleted</label>
                </div>
                <a class="btn btn-primary" href="#/sports/new">
                    <i class="bi bi-plus-lg me-1"></i>New sport
                </a>
            </div>
        {/if}
    </div>

    {#if error}
        <div class="alert alert-danger d-flex align-items-center">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
        </div>
    {/if}

    {#if loading}
        <div class="text-center py-5">
            <div class="spinner-border text-primary"></div>
        </div>
    {:else if sports.length === 0}
        <div class="card">
            <div class="card-body text-center text-muted py-5">
                <i class="bi bi-bullseye fs-1 d-block mb-2"></i>
                No sports yet.
            </div>
        </div>
    {:else}
        <div class="row row-cols-1 row-cols-md-2 row-cols-xl-3 g-4">
            {#each sports as sport (sport.id)}
                <div class="col">
                    <div class="card card-hover h-100" class:card-inactive={!sport.active}>
                        <div class="card-body">
                            <div class="d-flex align-items-center gap-3 mb-3">
                                <span class="icon-circle icon-circle-sm flex-shrink-0">
                                    <i class="bi {sport.type === 'TEAM' ? 'bi-people-fill' : 'bi-person-fill'}"></i>
                                </span>
                                <div class="overflow-hidden">
                                    <h5 class="mb-1 text-truncate" title={sport.name}>{sport.name}</h5>
                                    <span class="badge text-bg-primary">{SPORT_TYPE_LABELS[sport.type]}</span>
                                    <span class="badge bg-primary-subtle text-primary-emphasis">
                                        <i class="bi {SCORING_MODE_ICONS[sport.scoringMode]} me-1"></i>{SCORING_MODE_LABELS[sport.scoringMode]}
                                    </span>
                                    {#if !sport.active}
                                        <span class="badge text-bg-secondary">Deleted</span>
                                    {/if}
                                </div>
                            </div>

                            <table class="table table-sm mb-0">
                                <tbody>
                                <tr>
                                    <th class="text-muted fw-normal">Players per side</th>
                                    <td class="text-end">{playersPerSide(sport.rules)}</td>
                                </tr>
                                {#if sport.scoringMode === 'SETS'}
                                    <tr>
                                        <th class="text-muted fw-normal">Best of</th>
                                        <td class="text-end">{sport.rules.bestOf ?? '—'} sets</td>
                                    </tr>
                                    <tr>
                                        <th class="text-muted fw-normal">Points to win a set</th>
                                        <td class="text-end">{sport.rules.pointsToWinSet ?? '—'}</td>
                                    </tr>
                                {/if}
                                <tr>
                                    <th class="text-muted fw-normal">Draws</th>
                                    <td class="text-end">{sport.rules.allowDraw ? 'Allowed' : 'Not allowed'}</td>
                                </tr>
                                <tr>
                                    <th class="text-muted fw-normal">Points (win / draw / loss)</th>
                                    <td class="text-end">
                                        {sport.rules.pointsForWin} / {sport.rules.allowDraw ? sport.rules.pointsForDraw : '—'} / {sport.rules.pointsForLoss}
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>

                        {#if isAdmin}
                            <div class="card-footer d-flex justify-content-end gap-2">
                                {#if sport.active}
                                    <a class="btn btn-sm btn-outline-primary" href="#/sports/{sport.id}/edit">
                                        <i class="bi bi-pencil me-1"></i>Edit
                                    </a>
                                    <button class="btn btn-sm btn-outline-danger" onclick={() => confirmDelete(sport)}>
                                        <i class="bi bi-trash me-1"></i>Delete
                                    </button>
                                {:else}
                                    <button class="btn btn-sm btn-outline-primary" onclick={() => handleRestore(sport.id)}>
                                        <i class="bi bi-arrow-counterclockwise me-1"></i>Restore
                                    </button>
                                {/if}
                            </div>
                        {/if}
                    </div>
                </div>
            {/each}
        </div>
    {/if}
</div>

<ConfirmModal
    bind:show={showModal}
    title="Delete sport"
    message="Delete '{selected?.name}'? It will disappear from the catalog and no new matches can be recorded in it. Existing matches and rankings stay, and you can restore the sport later."
    onConfirm={handleDelete}
/>
