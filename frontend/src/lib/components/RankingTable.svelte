<script lang="ts">
    import {authStore} from '../auth/auth.store';
    import type {RankingEntryResponse} from '../types/stats.types';

    // Rang-lista (stranica rang-lista i tab u grupi); prva tri mesta su obojena, a red prijavljenog korisnika istaknut
    let {entries}: {entries: RankingEntryResponse[]} = $props();

    const myId = $derived($authStore.user?.id);
</script>

<div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
        <thead>
        <tr>
            <th class="text-center">#</th>
            <th>Player</th>
            <th class="text-center" title="Wins">W</th>
            <th class="text-center" title="Draws">D</th>
            <th class="text-center" title="Losses">L</th>
            <th class="text-center d-none d-sm-table-cell">Played</th>
            <th class="text-end">Points</th>
        </tr>
        </thead>
        <tbody>
        {#each entries as entry (entry.playerId)}
            <tr class:row-me={entry.playerId === myId}>
                <td class="text-center">
                    <span class="rank-badge" class:rank-1={entry.rank === 1} class:rank-2={entry.rank === 2} class:rank-3={entry.rank === 3}>{entry.rank}</span>
                </td>
                <td>
                    <a class="fw-semibold text-decoration-none" href="#/players/{entry.playerId}">{entry.username}</a>
                    {#if entry.playerId === myId}
                        <span class="badge text-bg-primary ms-1">You</span>
                    {/if}
                </td>
                <td class="text-center">{entry.wins}</td>
                <td class="text-center">{entry.draws}</td>
                <td class="text-center">{entry.losses}</td>
                <td class="text-center d-none d-sm-table-cell text-muted">{entry.total}</td>
                <td class="text-end fw-bold">{entry.points}</td>
            </tr>
        {/each}
        </tbody>
    </table>
</div>
