<script lang="ts">
    import {formatScore, OUTCOME_BADGES, resultLabel, sideNames, type MatchResponse} from '../types/match.types';
    import {formatDate} from '../utils/date';

    // Jedan meč u listi (list-group): prva strana levo, druga desno, rezultat u sredini; ceo red vodi na detalje.
    // Sa playerId (profil igrača) levo stoji i ishod meča za tog igrača (W / D / L).
    let {match, playerId}: {match: MatchResponse; playerId?: string} = $props();

    const playerSide = $derived(playerId ? match.sides.find(side => side.players.some(p => p.id === playerId)) : undefined);

    const home = $derived(match.sides[0]);
    const away = $derived(match.sides[1]);

    const result = $derived(resultLabel(home, away));
    const sets = $derived(home.setScores?.length ? formatScore(home, away) : '');
</script>

<a class="list-group-item list-group-item-action d-flex align-items-center gap-3 py-3" href="#/matches/{match.id}">
    {#if playerSide}
        <span class="badge outcome-badge {OUTCOME_BADGES[playerSide.outcome].css}" title={OUTCOME_BADGES[playerSide.outcome].label}>
            {OUTCOME_BADGES[playerSide.outcome].letter}
        </span>
    {/if}
    <div class="flex-grow-1 overflow-hidden">
        <div class="text-muted small mb-2 text-truncate">
            <i class="bi bi-bullseye me-1"></i>{match.sport.name} · {match.group.name} · {formatDate(match.playedAt)}
        </div>
        <div class="match-line">
            <div class="match-side text-end" class:match-side-winner={home.winner}>
                {#if home.winner}<i class="bi bi-trophy-fill text-warning me-1"></i>{/if}{sideNames(home)}
            </div>
            <div class="text-center">
                <span class="match-score">{result}</span>
                {#if sets}
                    <div class="text-muted small text-nowrap mt-1">{sets}</div>
                {/if}
            </div>
            <div class="match-side" class:match-side-winner={away.winner}>
                {sideNames(away)}{#if away.winner}<i class="bi bi-trophy-fill text-warning ms-1"></i>{/if}
            </div>
        </div>
    </div>
</a>
