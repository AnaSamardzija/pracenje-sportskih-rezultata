<script lang="ts">
    import {onMount} from 'svelte';
    import {deleteSport, getAllSports, restoreSport} from '../../lib/api/sports.api';
    import {authStore} from '../../lib/auth/auth.store';
    import ConfirmModal from '../../lib/components/ConfirmModal.svelte';
    import PageHeader from '../../lib/components/PageHeader.svelte';
    import {
        playersPerSide,
        SCORING_MODE_HINTS,
        SCORING_MODE_ICONS,
        SCORING_MODE_LABELS,
        SCORING_MODES,
        SPORT_TYPE_LABELS,
        type SportResponse
    } from '../../lib/types/sport.types';
    import {isSystemAdmin} from '../../lib/types/user.types';

    // Katalog vide svi; dodavanje, izmenu, brisanje i vraćanje obrisanih samo SYSTEM_ADMIN
    const isAdmin = $derived(isSystemAdmin($authStore.user));

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

<PageHeader title="Sports" icon="bi-bullseye" subtitle="Sports you can record matches in, with their scoring rules.">
    {#snippet actions()}
        {#if isAdmin}
            <div class="form-check form-switch mb-0">
                <input id="showDeleted"
                       class="form-check-input"
                       type="checkbox"
                       role="switch"
                       checked={showDeleted}
                       onchange={toggleDeleted}/>
                <label class="form-check-label" for="showDeleted">Show deleted</label>
            </div>
            <a class="btn btn-light" href="#/sports/new">
                <i class="bi bi-plus-lg me-1"></i>New sport
            </a>
        {/if}
    {/snippet}
</PageHeader>

<div class="container py-4 page-fade">
    {#if error}
        <div class="alert alert-danger d-flex align-items-center">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
        </div>
    {/if}

    <div class="row g-4">
        <div class="col-lg-8 col-xl-9">
            <div class="card">
                {#if loading}
                    <div class="card-body text-center py-5">
                        <div class="spinner-border text-primary"></div>
                    </div>
                {:else if sports.length === 0}
                    <div class="card-body text-center text-muted py-5">
                        <i class="bi bi-bullseye fs-1 d-block mb-2"></i>
                        No sports yet.
                    </div>
                {:else}
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead>
                            <tr>
                                <th>Sport</th>
                                <th class="d-none d-sm-table-cell">Scoring</th>
                                <th class="d-none d-md-table-cell text-center">Players</th>
                                <th class="d-none d-md-table-cell text-center">Sets</th>
                                <th class="d-none d-md-table-cell text-center">Draws</th>
                                <th class="text-center text-nowrap" title="Ranking points for a win / draw / loss">W / D / L</th>
                                {#if isAdmin}
                                    <th><span class="visually-hidden">Actions</span></th>
                                {/if}
                            </tr>
                            </thead>
                            <tbody>
                            {#each sports as sport (sport.id)}
                                <tr class:row-inactive={!sport.active}>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <i class="bi {sport.type === 'TEAM' ? 'bi-people-fill' : 'bi-person-fill'} text-primary"></i>
                                            <span class="fw-semibold">{sport.name}</span>
                                            {#if !sport.active}
                                                <span class="badge text-bg-secondary">Deleted</span>
                                            {/if}
                                        </div>
                                        <div class="text-muted small">
                                            {SPORT_TYPE_LABELS[sport.type]}<span class="d-sm-none"> · {SCORING_MODE_LABELS[sport.scoringMode]}</span>
                                        </div>
                                    </td>
                                    <td class="d-none d-sm-table-cell text-nowrap">
                                        <i class="bi {SCORING_MODE_ICONS[sport.scoringMode]} text-primary me-1"></i>{SCORING_MODE_LABELS[sport.scoringMode]}
                                    </td>
                                    <td class="d-none d-md-table-cell text-center">{playersPerSide(sport.rules)}</td>
                                    <td class="d-none d-md-table-cell text-center text-nowrap">
                                        {#if sport.scoringMode === 'SETS'}
                                            <span title="Best of {sport.rules.bestOf ?? '—'} sets, {sport.rules.pointsToWinSet ?? '—'} points to win a set">
                                                Best of {sport.rules.bestOf ?? '—'}
                                            </span>
                                        {:else}
                                            <span class="text-muted">—</span>
                                        {/if}
                                    </td>
                                    <td class="d-none d-md-table-cell text-center">
                                        {#if sport.rules.allowDraw}
                                            <i class="bi bi-check-lg text-success" title="Draws allowed"></i>
                                        {:else}
                                            <span class="text-muted" title="No draws">—</span>
                                        {/if}
                                    </td>
                                    <td class="text-center text-nowrap">
                                        {sport.rules.pointsForWin} / {sport.rules.allowDraw ? sport.rules.pointsForDraw : '—'} / {sport.rules.pointsForLoss}
                                    </td>
                                    {#if isAdmin}
                                        <td class="text-end text-nowrap">
                                            {#if sport.active}
                                                <a class="btn btn-sm btn-outline-primary" href="#/sports/{sport.id}/edit" title="Edit" aria-label="Edit {sport.name}">
                                                    <i class="bi bi-pencil"></i>
                                                </a>
                                                <button class="btn btn-sm btn-outline-danger" title="Delete" aria-label="Delete {sport.name}" onclick={() => confirmDelete(sport)}>
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            {:else}
                                                <button class="btn btn-sm btn-outline-primary" onclick={() => handleRestore(sport.id)}>
                                                    <i class="bi bi-arrow-counterclockwise me-1"></i>Restore
                                                </button>
                                            {/if}
                                        </td>
                                    {/if}
                                </tr>
                            {/each}
                            </tbody>
                        </table>
                    </div>
                {/if}
            </div>
        </div>

        <!-- Bočna kolona: kako se čitaju pravila iz tabele -->
        <div class="col-lg-4 col-xl-3">
            <div class="card">
                <div class="card-header">
                    <i class="bi bi-info-circle me-2 text-primary"></i>How scoring works
                </div>
                <ul class="list-group list-group-flush">
                    {#each SCORING_MODES as mode (mode)}
                        <li class="list-group-item">
                            <div class="fw-semibold"><i class="bi {SCORING_MODE_ICONS[mode]} text-primary me-2"></i>{SCORING_MODE_LABELS[mode]}</div>
                            <div class="text-muted small">{SCORING_MODE_HINTS[mode]}</div>
                        </li>
                    {/each}
                    <li class="list-group-item">
                        <div class="fw-semibold"><i class="bi bi-star text-primary me-2"></i>Ranking points</div>
                        <div class="text-muted small">Every match gives points for a win, draw or loss (W / D / L), by the rules of its sport.</div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>

<ConfirmModal
    bind:show={showModal}
    title="Delete sport"
    message="Delete '{selected?.name}'? It will disappear from the catalog and no new matches can be recorded in it. Existing matches and rankings stay, and you can restore the sport later."
    onConfirm={handleDelete}
/>
