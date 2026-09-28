<script lang="ts">
    import {formatScore, sideNames, type MatchResponse} from '../types/match.types';
    import {formatDate} from '../utils/date';

    // Jedan meč u listi (list-group): prva strana levo, druga desno, rezultat u sredini; ceo red vodi na detalje
    let {match}: {match: MatchResponse} = $props();

    const home = $derived(match.sides[0]);
    const away = $derived(match.sides[1]);

    // Za POINTS i SETS u sredini je rezultat (za SETS broj osvojenih setova), a za OUTCOME se zna samo ishod
    const result = $derived(
        home.score !== null && away.score !== null
            ? `${home.score} : ${away.score}`
            : home.outcome === 'DRAW' ? 'Draw' : 'vs'
    );
    const sets = $derived(home.setScores?.length ? formatScore(home, away) : '');
</script>

<a class="list-group-item list-group-item-action py-3" href="#/matches/{match.id}">
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
</a>
