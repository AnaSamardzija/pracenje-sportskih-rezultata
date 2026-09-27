<script lang="ts">
    import {onMount} from 'svelte';
    import {push} from 'svelte-spa-router';
    import {createSport, getSport, updateSport} from '../../lib/api/sports.api';
    import PageHeader from '../../lib/components/PageHeader.svelte';
    import {
        SCORING_MODE_HINTS,
        SCORING_MODE_ICONS,
        SCORING_MODE_LABELS,
        SCORING_MODES,
        type ScoringMode,
        type SportRequest,
        type SportType
    } from '../../lib/types/sport.types';

    // Ista forma za novi sport (/sports/new) i izmenu (/sports/:id/edit)
    let {params}: {params?: {id?: string}} = $props();

    const editId = $derived(params?.id ?? null);
    const isEdit = $derived(editId !== null);

    let name = $state('');
    let type = $state<SportType>('INDIVIDUAL');
    let scoringMode = $state<ScoringMode>('POINTS');
    let allowDraw = $state(false);
    let minPlayersPerSide = $state(1);
    let maxPlayersPerSide = $state<number | null>(1);
    let bestOf = $state<number | null>(3);
    let pointsToWinSet = $state<number | null>(6);
    let pointsForWin = $state(3);
    let pointsForDraw = $state(1);
    let pointsForLoss = $state(0);

    let loadingData = $state(false);
    let loadFailed = $state(false);
    let saving = $state(false);
    let error = $state<string | null>(null);

    onMount(async () => {
        if (!editId) return;
        loadingData = true;
        try {
            const sport = await getSport(editId);
            name = sport.name;
            type = sport.type;
            scoringMode = sport.scoringMode;
            allowDraw = sport.rules.allowDraw;
            minPlayersPerSide = sport.rules.minPlayersPerSide;
            maxPlayersPerSide = sport.rules.maxPlayersPerSide;
            bestOf = sport.rules.bestOf;
            pointsToWinSet = sport.rules.pointsToWinSet;
            pointsForWin = sport.rules.pointsForWin;
            pointsForDraw = sport.rules.pointsForDraw;
            pointsForLoss = sport.rules.pointsForLoss;
        } catch (e) {
            error = e instanceof Error ? e.message : 'Sport not found';
            loadFailed = true;
        } finally {
            loadingData = false;
        }
    });

    async function handleSubmit(e: SubmitEvent) {
        e.preventDefault();
        error = null;
        saving = true;

        // Polja za setove imaju smisla samo za SETS; za ostale modove šalju se kao null
        const isSets = scoringMode === 'SETS';
        const data: SportRequest = {
            name: name.trim(),
            type,
            scoringMode,
            rules: {
                allowDraw,
                minPlayersPerSide,
                maxPlayersPerSide,
                bestOf: isSets ? bestOf : null,
                pointsToWinSet: isSets ? pointsToWinSet : null,
                pointsForWin,
                pointsForDraw,
                pointsForLoss
            }
        };

        try {
            if (editId) {
                await updateSport(editId, data);
            } else {
                await createSport(data);
            }
            push('/sports');
        } catch (err) {
            error = err instanceof Error ? err.message : 'Failed to save sport';
        } finally {
            saving = false;
        }
    }
</script>

<PageHeader title={isEdit ? 'Edit sport' : 'New sport'}
            icon={isEdit ? 'bi-pencil-square' : 'bi-plus-circle'}
            subtitle="Scoring mode and rules decide how results are entered and how ranking points are awarded."/>

<div class="container py-4 page-fade">
    <div class="row justify-content-center">
        <div class="col-lg-9 col-xl-8">

            {#if error}
                <div class="alert alert-danger d-flex align-items-center">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
                </div>
            {/if}

            {#if loadingData}
                <div class="text-center py-5">
                    <div class="spinner-border text-primary"></div>
                </div>
            {:else if loadFailed}
                <a class="btn btn-outline-secondary" href="#/sports"><i class="bi bi-arrow-left me-1"></i>Back to sports</a>
            {:else}
                <form onsubmit={handleSubmit}>
                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-info-circle me-2 text-primary"></i>Basics
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label class="form-label" for="name">Name</label>
                                <input id="name" class="form-control" bind:value={name} required/>
                            </div>

                            <fieldset class="mb-3">
                                <legend class="form-label fs-6">Type</legend>
                                <div class="btn-group w-100">
                                    <input id="type-individual" class="btn-check" type="radio" value="INDIVIDUAL" bind:group={type}/>
                                    <label class="btn btn-outline-primary" for="type-individual">
                                        <i class="bi bi-person-fill me-1"></i>Individual
                                    </label>
                                    <input id="type-team" class="btn-check" type="radio" value="TEAM" bind:group={type}/>
                                    <label class="btn btn-outline-primary" for="type-team">
                                        <i class="bi bi-people-fill me-1"></i>Team
                                    </label>
                                </div>
                            </fieldset>

                            <fieldset>
                                <legend class="form-label fs-6">Scoring mode</legend>
                                <div class="btn-group w-100">
                                    {#each SCORING_MODES as mode (mode)}
                                        <input id="mode-{mode}" class="btn-check" type="radio" value={mode} bind:group={scoringMode}/>
                                        <label class="btn btn-outline-primary" for="mode-{mode}">
                                            <i class="bi {SCORING_MODE_ICONS[mode]} me-1"></i>{SCORING_MODE_LABELS[mode]}
                                        </label>
                                    {/each}
                                </div>
                                <div class="form-text">{SCORING_MODE_HINTS[scoringMode]}</div>
                            </fieldset>
                        </div>
                    </div>

                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-sliders me-2 text-primary"></i>Rules
                        </div>
                        <div class="card-body">
                            <!-- max ne sme biti manji od min; browser to proverava preko min atributa, backend vraća 422 -->
                            <div class="row g-3 mb-3">
                                <div class="col-sm-6">
                                    <label class="form-label" for="minPlayers">Min players per side</label>
                                    <input id="minPlayers" type="number" class="form-control" min="1" bind:value={minPlayersPerSide} required/>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label" for="maxPlayers">Max players per side <span class="text-muted small">(optional)</span></label>
                                    <input id="maxPlayers" type="number" class="form-control" min={minPlayersPerSide || 1} bind:value={maxPlayersPerSide}/>
                                    <div class="form-text">Leave empty for no upper limit.</div>
                                </div>
                            </div>

                            {#if scoringMode === 'SETS'}
                                <div class="row g-3 mb-3">
                                    <div class="col-sm-6">
                                        <label class="form-label" for="bestOf">Best of (sets) <span class="text-muted small">(optional)</span></label>
                                        <input id="bestOf" type="number" class="form-control" min="1" bind:value={bestOf}/>
                                        <div class="form-text">Maximum number of sets in a match.</div>
                                    </div>
                                    <div class="col-sm-6">
                                        <label class="form-label" for="pointsToWinSet">Points to win a set <span class="text-muted small">(optional)</span></label>
                                        <input id="pointsToWinSet" type="number" class="form-control" min="1" bind:value={pointsToWinSet}/>
                                    </div>
                                </div>
                            {/if}

                            <div class="form-check form-switch mb-3">
                                <input id="allowDraw" class="form-check-input" type="checkbox" role="switch" bind:checked={allowDraw}/>
                                <label class="form-check-label" for="allowDraw">Draws are allowed</label>
                            </div>

                            <p class="form-label mb-2">Ranking points per match</p>
                            <div class="row g-3">
                                <div class="col-4">
                                    <label class="form-label small text-muted" for="pointsForWin">Win</label>
                                    <input id="pointsForWin" type="number" class="form-control" min="0" bind:value={pointsForWin} required/>
                                </div>
                                <div class="col-4">
                                    <label class="form-label small text-muted" for="pointsForDraw">Draw</label>
                                    <input id="pointsForDraw" type="number" class="form-control" min="0" bind:value={pointsForDraw} disabled={!allowDraw} required/>
                                </div>
                                <div class="col-4">
                                    <label class="form-label small text-muted" for="pointsForLoss">Loss</label>
                                    <input id="pointsForLoss" type="number" class="form-control" min="0" bind:value={pointsForLoss} required/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-primary" disabled={saving}>
                            {#if saving}
                                <span class="spinner-border spinner-border-sm me-2"></span>Saving...
                            {:else}
                                <i class="bi bi-check-lg me-2"></i>{isEdit ? 'Save changes' : 'Create sport'}
                            {/if}
                        </button>
                        <a class="btn btn-outline-secondary" href="#/sports">Cancel</a>
                    </div>
                </form>
            {/if}
        </div>
    </div>
</div>
