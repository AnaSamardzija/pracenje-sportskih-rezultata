<script lang="ts">
    import {onMount} from 'svelte';
    import {getMyStats} from '../lib/api/users.api';
    import {getAllMatches} from '../lib/api/matches.api';
    import {getAllGroups} from '../lib/api/groups.api';
    import {authStore} from '../lib/auth/auth.store';
    import PageHeader from '../lib/components/PageHeader.svelte';
    import {STORAGE_LABELS} from '../lib/types/auth.types';
    import {GROUP_ROLE_LABELS, type GroupResponse} from '../lib/types/group.types';
    import {formatScore, type MatchOutcome, type MatchResponse} from '../lib/types/match.types';
    import type {PlayerStatsResponse} from '../lib/types/stats.types';
    import {formatDate} from '../lib/utils/date';

    const RECENT_MATCHES = 5;

    const OUTCOME_BADGES: Record<MatchOutcome, {letter: string; css: string}> = {
        WIN: {letter: 'W', css: 'text-bg-success'},
        DRAW: {letter: 'D', css: 'text-bg-secondary'},
        LOSS: {letter: 'L', css: 'text-bg-danger'}
    };

    const user = $derived($authStore.user);

    let stats = $state<PlayerStatsResponse | null>(null);
    let matches = $state<MatchResponse[]>([]);
    let groups = $state<GroupResponse[]>([]);
    let loading = $state(true);
    let error = $state<string | null>(null);

    // Nov korisnik (bez grupa i mečeva) umesto praznih blokova dobija korake za početak
    const isNewUser = $derived(stats?.total === 0 && groups.length === 0);

    const greeting = $derived.by(() => {
        const hour = new Date().getHours();
        const part = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening';
        return `${part}, ${user?.firstName || user?.username || ''}`;
    });

    const subtitle = $derived(
        `Signed in to ${$authStore.storageType ? STORAGE_LABELS[$authStore.storageType] : '—'}. Here is how you are doing.`
    );

    // Tri nezavisna poziva idu istovremeno; za poslednje mečeve backend već vraća listu od najnovijeg
    onMount(async () => {
        try {
            const [myStats, myMatches, myGroups] = await Promise.all([
                getMyStats(),
                getAllMatches({playerId: user!.id}),
                getAllGroups({mine: true})
            ]);
            stats = myStats;
            matches = myMatches.slice(0, RECENT_MATCHES);
            groups = myGroups;
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to load your dashboard';
        } finally {
            loading = false;
        }
    });

    // Meč iz ugla prijavljenog korisnika: njegova strana, protivnici i rezultat sa njegovim brojem prvim
    function view(match: MatchResponse) {
        const mine = match.sides.find(s => s.players.some(p => p.id === user?.id)) ?? match.sides[0];
        const others = match.sides.filter(s => s !== mine);
        return {
            outcome: OUTCOME_BADGES[mine.outcome],
            opponents: others.flatMap(s => s.players.map(p => p.username)).join(', '),
            score: others.length === 1 ? formatScore(mine, others[0]) : ''
        };
    }
</script>

<PageHeader title={greeting} icon="bi-house" {subtitle} overlap/>

<div class="container pb-5 page-fade">
    {#if error}
        <div class="alert alert-danger d-flex align-items-center page-header-pull">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
        </div>
    {:else if loading}
        <div class="card page-header-pull">
            <div class="card-body text-center py-5">
                <div class="spinner-border text-primary"></div>
            </div>
        </div>
    {:else if stats}
        <!-- Kartice statistike delimično prelaze preko trake zaglavlja -->
        <div class="row g-3 page-header-pull mb-4">
            <div class="col-6 col-lg-3">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="stat-label"><i class="bi bi-calendar-event me-2"></i>Matches</div>
                        <div class="stat-value">{stats.total}</div>
                        <div class="text-muted small">{stats.wins} W · {stats.draws} D · {stats.losses} L</div>
                    </div>
                </div>
            </div>
            <div class="col-6 col-lg-3">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="stat-label"><i class="bi bi-percent me-2"></i>Win rate</div>
                        <div class="stat-value">{stats.winPercentage}%</div>
                        <progress class="stat-progress mt-2" max="100" value={stats.winPercentage} aria-label="Win rate"></progress>
                    </div>
                </div>
            </div>
            <div class="col-6 col-lg-3">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="stat-label"><i class="bi bi-star me-2"></i>Points</div>
                        <div class="stat-value">{stats.points}</div>
                        <div class="text-muted small">across all sports</div>
                    </div>
                </div>
            </div>
            <div class="col-6 col-lg-3">
                <div class="card stat-card h-100">
                    <div class="card-body">
                        <div class="stat-label"><i class="bi bi-fire me-2"></i>Best streak</div>
                        <div class="stat-value">{stats.longestWinStreak}</div>
                        <div class="text-muted small">wins in a row</div>
                    </div>
                </div>
            </div>
        </div>

        {#if isNewUser}
            <div class="card">
                <div class="card-body p-4">
                    <h5 class="mb-1">Get started</h5>
                    <p class="text-muted mb-4">Everything happens inside groups: that is where you play, record matches and compete in rankings.</p>
                    <div class="row g-4">
                        <div class="col-md-4">
                            <div class="step">
                                <span class="step-number">1</span>
                                <div>
                                    <div class="fw-semibold">Join or create a group</div>
                                    <p class="text-muted small mb-2">Find your club, office league or friends.</p>
                                    <a class="btn btn-primary btn-sm" href="#/groups">Browse groups</a>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="step">
                                <span class="step-number">2</span>
                                <div>
                                    <div class="fw-semibold">Record your first match</div>
                                    <p class="text-muted small mb-0">Enter the result right after you play.</p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="step">
                                <span class="step-number">3</span>
                                <div>
                                    <div class="fw-semibold">Climb the rankings</div>
                                    <p class="text-muted small mb-0">Points are awarded by each sport's rules.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        {:else}
            <div class="row g-4">
                <div class="col-lg-8">
                    <div class="card h-100">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <span><i class="bi bi-clock-history me-2 text-primary"></i>Recent matches</span>
                            <a class="small fw-normal text-decoration-none" href="#/matches">All matches <i class="bi bi-arrow-right"></i></a>
                        </div>
                        {#if matches.length === 0}
                            <div class="card-body text-center text-muted py-5">
                                <i class="bi bi-calendar-x fs-2 d-block mb-2"></i>
                                No matches yet. Record your first one after you play.
                            </div>
                        {:else}
                            <div class="list-group list-group-flush">
                                {#each matches as match (match.id)}
                                    {@const v = view(match)}
                                    <a class="list-group-item list-group-item-action d-flex align-items-center gap-3 py-3" href="#/matches/{match.id}">
                                        <span class="badge outcome-badge {v.outcome.css}">{v.outcome.letter}</span>
                                        <div class="flex-grow-1 overflow-hidden">
                                            <div class="fw-semibold text-truncate">vs {v.opponents}</div>
                                            <div class="text-muted small text-truncate">{match.sport.name} · {match.group.name} · {formatDate(match.playedAt)}</div>
                                        </div>
                                        {#if v.score}
                                            <span class="fw-semibold text-nowrap">{v.score}</span>
                                        {/if}
                                    </a>
                                {/each}
                            </div>
                        {/if}
                    </div>
                </div>

                <div class="col-lg-4">
                    <div class="card h-100">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <span><i class="bi bi-people me-2 text-primary"></i>My groups</span>
                            <a class="small fw-normal text-decoration-none" href="#/groups">All groups <i class="bi bi-arrow-right"></i></a>
                        </div>
                        {#if groups.length === 0}
                            <div class="card-body text-center text-muted py-5">
                                <i class="bi bi-people fs-2 d-block mb-2"></i>
                                You are not in any group yet.
                                <div class="mt-3">
                                    <a class="btn btn-primary btn-sm" href="#/groups">Browse groups</a>
                                </div>
                            </div>
                        {:else}
                            <div class="list-group list-group-flush">
                                {#each groups as group (group.id)}
                                    <a class="list-group-item list-group-item-action d-flex align-items-center gap-3 py-3" href="#/groups/{group.id}">
                                        <span class="icon-circle icon-circle-xs flex-shrink-0">{group.name[0].toUpperCase()}</span>
                                        <div class="flex-grow-1 overflow-hidden">
                                            <div class="fw-semibold text-truncate">{group.name}</div>
                                            <div class="text-muted small"><i class="bi bi-person me-1"></i>{group.memberCount} members</div>
                                        </div>
                                        {#if group.myRole === 'GROUP_ADMIN'}
                                            <span class="badge bg-primary-subtle text-primary-emphasis">{GROUP_ROLE_LABELS[group.myRole]}</span>
                                        {/if}
                                    </a>
                                {/each}
                            </div>
                        {/if}
                    </div>
                </div>
            </div>
        {/if}
    {/if}
</div>
