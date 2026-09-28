<script lang="ts">
    import {push, router} from 'svelte-spa-router';
    import {createMatch, getMatch, updateMatch} from '../../lib/api/matches.api';
    import {getAllGroups, getMembers} from '../../lib/api/groups.api';
    import {getAllSports} from '../../lib/api/sports.api';
    import {authStore} from '../../lib/auth/auth.store';
    import PageHeader from '../../lib/components/PageHeader.svelte';
    import type {GroupResponse, MemberResponse} from '../../lib/types/group.types';
    import type {MatchOutcome, MatchRequest, MatchResponse, MatchSideRequest, PlayerDto} from '../../lib/types/match.types';
    import {
        playersPerSide,
        SCORING_MODE_HINTS,
        SCORING_MODE_ICONS,
        SCORING_MODE_LABELS,
        type SportResponse
    } from '../../lib/types/sport.types';
    import {toDateTimeInput} from '../../lib/utils/date';

    // Rezultat jednog seta: gemovi prve i druge strane
    type SetRow = {home: number | null; away: number | null};
    // Za OUTCOME korisnik bira samo ko je pobedio; ishod svake strane se izvodi pri slanju
    type Winner = 'HOME' | 'DRAW' | 'AWAY';
    type Candidate = {id: string; username: string; note: string | null};

    const SIDES = [0, 1];

    // Ista forma za nov meč (/matches/new) i izmenu (/matches/:id/edit)
    let {params}: {params?: {id?: string}} = $props();
    const editId = $derived(params?.id ?? null);
    const isEdit = $derived(editId !== null);

    let groups = $state<GroupResponse[]>([]);
    let sports = $state<SportResponse[]>([]);
    let members = $state<MemberResponse[]>([]);

    // Pri izmeni: grupa je zaključana (backend ne dozvoljava premeštanje), a pamte se i igrači i sport meča,
    // da bi se prikazali i igrač koji je u međuvremenu napustio grupu i sport koji je u međuvremenu obrisan
    let lockedGroupName = $state('');
    let originalPlayers = $state<PlayerDto[]>([]);
    let originalSportName = $state('');

    let groupId = $state('');
    let sportId = $state('');
    let playedAt = $state(toDateTimeInput(new Date()));
    let players = $state<string[][]>([[], []]);
    let scores = $state<(number | null)[]>([null, null]);
    let setRows = $state<SetRow[]>([]);
    let winner = $state<Winner | null>(null);

    let loading = $state(true);
    let loadFailed = $state(false);
    let saving = $state(false);
    let error = $state<string | null>(null);

    const maxPlayedAt = toDateTimeInput(new Date());

    const myId = $derived($authStore.user?.id);
    const sport = $derived(sports.find(s => s.id === sportId) ?? null);
    const maxPlayers = $derived(sport?.rules.maxPlayersPerSide ?? null);
    const bestOf = $derived(sport?.rules.bestOf ?? null);

    // Igrači koji se mogu izabrati: aktivni članovi grupe, plus već izabrani koji to više nisu (backend ih odbija, pa se vide sa napomenom)
    const candidates = $derived.by((): Candidate[] => {
        const selected = new Set(players.flat());
        const list: Candidate[] = members
            .filter(m => m.active || selected.has(m.userId))
            .map(m => ({id: m.userId, username: m.username, note: m.active ? null : 'deactivated'}));
        for (const p of originalPlayers) {
            if (selected.has(p.id) && !list.some(c => c.id === p.id)) {
                list.push({id: p.id, username: p.username, note: 'left the group'});
            }
        }
        return list;
    });

    // Strana se u rezultatu zove po izabranim igračima, a dok ih nema „Side 1“ / „Side 2“
    function sideLabel(side: number): string {
        const names = players[side].map(id => candidates.find(c => c.id === id)?.username).filter(Boolean);
        return names.length ? names.join(', ') : `Side ${side + 1}`;
    }

    // Grupa iz adrese, npr. #/matches/new?groupId=3 (dugme za unos meča na stranici grupe)
    const queryGroupId = $derived(new URLSearchParams(router.querystring ?? '').get('groupId'));

    // Učitavanje prati id i grupu iz adrese: ruter ne pravi stranicu ponovo kad se promeni samo id ili upit
    // (npr. „Record match“ iz menija dok je forma otvorena), pa se forma tada vraća na početak
    $effect(() => {
        init(editId, queryGroupId);
    });

    function reset() {
        groupId = '';
        sportId = '';
        playedAt = toDateTimeInput(new Date());
        players = [[], []];
        scores = [null, null];
        setRows = [];
        winner = null;
        originalPlayers = [];
    }

    async function init(id: string | null, fromUrl: string | null) {
        reset();
        loading = true;
        loadFailed = false;
        error = null;
        try {
            if (id) {
                const [match, allSports] = await Promise.all([getMatch(id), getAllSports()]);
                if (id !== editId) return;
                sports = allSports;
                fillFrom(match);
            } else {
                const [myGroups, allSports] = await Promise.all([getAllGroups({mine: true}), getAllSports()]);
                if (editId !== null || fromUrl !== queryGroupId) return;
                groups = myGroups;
                sports = allSports;
                // Grupa iz adrese, a ako je korisnik u samo jednoj grupi, ta
                const preselected = myGroups.find(g => g.id === fromUrl) ?? (myGroups.length === 1 ? myGroups[0] : null);
                if (preselected) selectGroup(preselected.id);
            }
        } catch (e) {
            if (id !== editId) return;
            error = e instanceof Error ? e.message : 'Failed to load match';
            loadFailed = true;
        } finally {
            if (id === editId) loading = false;
        }
    }

    function fillFrom(match: MatchResponse) {
        const [home, away] = match.sides;
        groupId = match.group.id;
        lockedGroupName = match.group.name;
        sportId = match.sport.id;
        originalSportName = match.sport.name;
        playedAt = match.playedAt.slice(0, 16);
        originalPlayers = match.sides.flatMap(side => side.players);
        players = match.sides.map(side => side.players.map(p => p.id));
        if (home.setScores?.length) {
            setRows = home.setScores.map((games, i) => ({home: games, away: away.setScores?.[i] ?? null}));
            scores = [null, null];
        } else {
            scores = [home.score, away.score];
        }
        winner = home.outcome === 'DRAW' ? 'DRAW' : home.outcome === 'WIN' ? 'HOME' : 'AWAY';
    }

    // Članovi se učitavaju za izabranu grupu; odgovor za grupu koja više nije izabrana se odbacuje
    $effect(() => {
        const id = groupId;
        members = [];
        if (id) loadMembers(id);
    });

    async function loadMembers(id: string) {
        try {
            const list = await getMembers(id);
            if (id === groupId) members = list;
        } catch (e) {
            if (id === groupId) error = e instanceof Error ? e.message : 'Failed to load members';
        }
    }

    // Nova grupa znači nove igrače; prijavljeni korisnik je unapred na prvoj strani, jer najčešće unosi svoj meč
    function selectGroup(id: string) {
        groupId = id;
        players = [myId ? [myId] : [], []];
    }

    // Unos rezultata zavisi od načina bodovanja, pa se posle promene sporta prazni; višak igrača se skida
    function selectSport(id: string) {
        sportId = id;
        const rules = sports.find(s => s.id === id)?.rules;
        const max = rules?.maxPlayersPerSide ?? null;
        if (max !== null) players = players.map(list => list.slice(0, max));
        setRows = defaultSets(rules?.bestOf ?? null);
        scores = [null, null];
        winner = null;
    }

    // Za „best of 3“ meč ima bar 2 seta, za „best of 5“ bar 3; bez ograničenja počinje se od jednog
    function defaultSets(limit: number | null): SetRow[] {
        return Array.from({length: limit ? Math.ceil(limit / 2) : 1}, () => ({home: null, away: null}));
    }

    // Kad strana sme da ima samo jednog igrača, klik na drugog ga zamenjuje
    function togglePlayer(side: number, id: string) {
        const list = players[side];
        if (list.includes(id)) {
            players[side] = list.filter(p => p !== id);
        } else if (maxPlayers === 1) {
            players[side] = [id];
        } else if (maxPlayers === null || list.length < maxPlayers) {
            players[side] = [...list, id];
        }
    }

    // Igrač ne može biti na obe strane, a puna strana ne prima nove igrače
    function isDisabled(side: number, id: string): boolean {
        if (players[1 - side].includes(id)) return true;
        return !players[side].includes(id) && maxPlayers !== null && maxPlayers > 1 && players[side].length >= maxPlayers;
    }

    function outcomeOf(side: number): MatchOutcome {
        if (winner === 'DRAW') return 'DRAW';
        return (winner === 'HOME') === (side === 0) ? 'WIN' : 'LOSS';
    }

    function sideRequest(current: SportResponse, side: number): MatchSideRequest {
        const request: MatchSideRequest = {playerIds: players[side], score: null, setScores: null, outcome: null};
        if (current.scoringMode === 'POINTS') request.score = scores[side];
        if (current.scoringMode === 'SETS') request.setScores = setRows.map(row => (side === 0 ? row.home : row.away) ?? 0);
        if (current.scoringMode === 'OUTCOME') request.outcome = outcomeOf(side);
        return request;
    }

    async function handleSubmit(e: SubmitEvent) {
        e.preventDefault();
        error = null;
        if (!sport) return;

        // Broj igrača browser ne može da proveri (to nisu polja forme), pa se proverava ovde; ostalo proverava backend
        const min = sport.rules.minPlayersPerSide;
        const shortSide = SIDES.find(side => players[side].length < min);
        if (shortSide !== undefined) {
            error = `Side ${shortSide + 1} needs at least ${min} ${min === 1 ? 'player' : 'players'}.`;
            return;
        }

        const data: MatchRequest = {
            sportId,
            groupId,
            playedAt,
            sides: SIDES.map(side => sideRequest(sport, side))
        };

        saving = true;
        try {
            const saved = editId ? await updateMatch(editId, data) : await createMatch(data);
            push(`/matches/${saved.id}`);
        } catch (err) {
            error = err instanceof Error ? err.message : 'Failed to save match';
        } finally {
            saving = false;
        }
    }
</script>

<PageHeader title={isEdit ? 'Edit match' : 'Record match'}
            icon={isEdit ? 'bi-pencil-square' : 'bi-plus-circle'}
            subtitle="Pick the group, sport and players, then enter the result. The winner is decided by the sport's rules."/>

<div class="container py-4 page-fade">
    <div class="row justify-content-center">
        <div class="col-lg-9 col-xl-8">

            {#if error}
                <div class="alert alert-danger d-flex align-items-center">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
                </div>
            {/if}

            {#if loading}
                <div class="text-center py-5">
                    <div class="spinner-border text-primary"></div>
                </div>
            {:else if loadFailed}
                <a class="btn btn-outline-secondary" href="#/matches"><i class="bi bi-arrow-left me-1"></i>Back to matches</a>
            {:else if !isEdit && groups.length === 0}
                <div class="card">
                    <div class="card-body text-center text-muted py-5">
                        <i class="bi bi-people fs-1 d-block mb-2"></i>
                        Matches are recorded inside a group. Join or create one first.
                        <div class="mt-3">
                            <a class="btn btn-primary btn-sm" href="#/groups">Browse groups</a>
                        </div>
                    </div>
                </div>
            {:else}
                <form onsubmit={handleSubmit}>
                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-info-circle me-2 text-primary"></i>Match
                        </div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label class="form-label" for="group">Group</label>
                                {#if isEdit}
                                    <input id="group" class="form-control" value={lockedGroupName} disabled/>
                                    <div class="form-text">A match cannot be moved to another group.</div>
                                {:else}
                                    <select id="group" class="form-select" required onchange={e => selectGroup(e.currentTarget.value)}>
                                        <option value="" disabled selected={groupId === ''}>Choose a group</option>
                                        {#each groups as group (group.id)}
                                            <option value={group.id} selected={groupId === group.id}>{group.name}</option>
                                        {/each}
                                    </select>
                                {/if}
                            </div>

                            <div class="row g-3">
                                <div class="col-sm-6">
                                    <label class="form-label" for="sport">Sport</label>
                                    <select id="sport" class="form-select" required onchange={e => selectSport(e.currentTarget.value)}>
                                        <option value="" disabled selected={!sport}>Choose a sport</option>
                                        {#each sports as s (s.id)}
                                            <option value={s.id} selected={sportId === s.id}>{s.name}</option>
                                        {/each}
                                    </select>
                                    {#if isEdit && sportId && !sport}
                                        <div class="form-text text-danger">The original sport ({originalSportName}) has been deleted. Pick an active sport to save changes.</div>
                                    {:else if sport}
                                        <div class="form-text">
                                            <i class="bi {SCORING_MODE_ICONS[sport.scoringMode]} me-1"></i>{SCORING_MODE_LABELS[sport.scoringMode]} ·
                                            {playersPerSide(sport.rules)} {sport.rules.maxPlayersPerSide === 1 ? 'player' : 'players'} per side
                                        </div>
                                    {/if}
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label" for="playedAt">Played at</label>
                                    <input id="playedAt" type="datetime-local" class="form-control" max={maxPlayedAt} bind:value={playedAt} required/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card mb-4">
                        <div class="card-header">
                            <i class="bi bi-people me-2 text-primary"></i>Players
                        </div>
                        <div class="card-body">
                            {#if !groupId}
                                <p class="text-muted mb-0">Choose a group first.</p>
                            {:else}
                                <div class="row g-4">
                                    {#each SIDES as side (side)}
                                        <div class="col-md-6">
                                            <div class="d-flex align-items-center gap-2 mb-2">
                                                <span class="fw-semibold">Side {side + 1}</span>
                                                <span class="badge bg-primary-subtle text-primary-emphasis">{players[side].length}</span>
                                            </div>
                                            <div class="d-flex flex-wrap gap-2" role="group" aria-label="Side {side + 1} players">
                                                {#each candidates as candidate (candidate.id)}
                                                    {@const selected = players[side].includes(candidate.id)}
                                                    <button type="button"
                                                            class="btn btn-sm rounded-pill {selected ? 'btn-primary' : 'btn-outline-primary'}"
                                                            aria-pressed={selected}
                                                            disabled={isDisabled(side, candidate.id)}
                                                            onclick={() => togglePlayer(side, candidate.id)}>
                                                        {#if selected}<i class="bi bi-check-lg me-1"></i>{/if}{candidate.username}{#if candidate.id === myId}&nbsp;(you){/if}
                                                        {#if candidate.note}<span class="small"> · {candidate.note}</span>{/if}
                                                    </button>
                                                {/each}
                                            </div>
                                        </div>
                                    {/each}
                                </div>
                                <div class="form-text mt-3">
                                    A player can be on only one side.
                                    {#if candidates.length < 2}
                                        This group needs at least two active members to record a match.
                                    {/if}
                                </div>
                            {/if}
                        </div>
                    </div>

                    {#if sport}
                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="bi {SCORING_MODE_ICONS[sport.scoringMode]} me-2 text-primary"></i>Result
                            </div>
                            <div class="card-body">
                                {#if sport.scoringMode === 'POINTS'}
                                    <div class="row g-3">
                                        {#each SIDES as side (side)}
                                            <div class="col-6">
                                                <label class="form-label text-truncate d-block" for="score-{side}">{sideLabel(side)}</label>
                                                <input id="score-{side}" type="number" class="form-control" min="0" bind:value={scores[side]} required/>
                                            </div>
                                        {/each}
                                    </div>
                                {:else if sport.scoringMode === 'SETS'}
                                    <div class="row g-2 mb-2 small text-muted">
                                        <div class="col-2"></div>
                                        <div class="col text-truncate">{sideLabel(0)}</div>
                                        <div class="col text-truncate">{sideLabel(1)}</div>
                                        <div class="col-auto"><span class="btn btn-sm invisible"><i class="bi bi-x-lg"></i></span></div>
                                    </div>
                                    {#each setRows as row, i (i)}
                                        <div class="row g-2 align-items-center mb-2">
                                            <div class="col-2 text-muted small text-nowrap">Set {i + 1}</div>
                                            <div class="col">
                                                <input type="number" class="form-control" min="0" aria-label="Set {i + 1}, side 1" bind:value={row.home} required/>
                                            </div>
                                            <div class="col">
                                                <input type="number" class="form-control" min="0" aria-label="Set {i + 1}, side 2" bind:value={row.away} required/>
                                            </div>
                                            <div class="col-auto">
                                                <button type="button"
                                                        class="btn btn-sm btn-outline-danger"
                                                        class:invisible={setRows.length === 1}
                                                        title="Remove set"
                                                        aria-label="Remove set {i + 1}"
                                                        onclick={() => setRows.splice(i, 1)}>
                                                    <i class="bi bi-x-lg"></i>
                                                </button>
                                            </div>
                                        </div>
                                    {/each}
                                    <button type="button"
                                            class="btn btn-sm btn-outline-primary mt-1"
                                            disabled={bestOf !== null && setRows.length >= bestOf}
                                            onclick={() => setRows.push({home: null, away: null})}>
                                        <i class="bi bi-plus-lg me-1"></i>Add set
                                    </button>
                                    {#if bestOf}
                                        <span class="form-text ms-2">Best of {bestOf} sets.</span>
                                    {/if}
                                {:else}
                                    <div class="btn-group w-100 flex-wrap" role="group" aria-label="Winner">
                                        <input id="winner-home" class="btn-check" type="radio" name="winner" value="HOME" bind:group={winner} required/>
                                        <label class="btn btn-outline-primary text-truncate" for="winner-home">{sideLabel(0)} won</label>
                                        {#if sport.rules.allowDraw}
                                            <input id="winner-draw" class="btn-check" type="radio" name="winner" value="DRAW" bind:group={winner}/>
                                            <label class="btn btn-outline-primary" for="winner-draw">Draw</label>
                                        {/if}
                                        <input id="winner-away" class="btn-check" type="radio" name="winner" value="AWAY" bind:group={winner}/>
                                        <label class="btn btn-outline-primary text-truncate" for="winner-away">{sideLabel(1)} won</label>
                                    </div>
                                {/if}
                                <div class="form-text mt-2">{SCORING_MODE_HINTS[sport.scoringMode]}</div>
                            </div>
                        </div>
                    {/if}

                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-primary" disabled={saving}>
                            {#if saving}
                                <span class="spinner-border spinner-border-sm me-2"></span>Saving...
                            {:else}
                                <i class="bi bi-check-lg me-2"></i>{isEdit ? 'Save changes' : 'Record match'}
                            {/if}
                        </button>
                        <a class="btn btn-outline-secondary" href={isEdit ? `#/matches/${editId}` : '#/matches'}>Cancel</a>
                    </div>
                </form>
            {/if}
        </div>
    </div>
</div>
