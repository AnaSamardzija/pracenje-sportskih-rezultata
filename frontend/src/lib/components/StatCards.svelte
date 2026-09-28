<script lang="ts">
    import type {PlayerStatsResponse} from '../types/stats.types';

    // Četiri kartice statistike igrača (početna i profil); scope opisuje na šta se bodovi odnose, npr. „in Tenis“
    let {stats, scope = 'across all sports'}: {stats: PlayerStatsResponse; scope?: string} = $props();
</script>

<div class="row g-3">
    <div class="col-6 col-lg-3">
        <div class="card h-100">
            <div class="card-body">
                <div class="stat-label"><i class="bi bi-calendar-event me-2"></i>Matches</div>
                <div class="stat-value">{stats.total}</div>
                <div class="text-muted small">{stats.wins} W · {stats.draws} D · {stats.losses} L</div>
            </div>
        </div>
    </div>
    <div class="col-6 col-lg-3">
        <div class="card h-100">
            <div class="card-body">
                <div class="stat-label"><i class="bi bi-percent me-2"></i>Win rate</div>
                <div class="stat-value">{stats.winPercentage}%</div>
                <progress class="stat-progress mt-2" max="100" value={stats.winPercentage} aria-label="Win rate"></progress>
            </div>
        </div>
    </div>
    <div class="col-6 col-lg-3">
        <div class="card h-100">
            <div class="card-body">
                <div class="stat-label"><i class="bi bi-star me-2"></i>Points</div>
                <div class="stat-value">{stats.points}</div>
                <div class="text-muted small">{scope}</div>
            </div>
        </div>
    </div>
    <div class="col-6 col-lg-3">
        <div class="card h-100">
            <div class="card-body">
                <div class="stat-label"><i class="bi bi-fire me-2"></i>Best streak</div>
                <div class="stat-value">{stats.longestWinStreak}</div>
                <div class="text-muted small">wins in a row</div>
            </div>
        </div>
    </div>
</div>
