<script lang="ts">
    import type {Snippet} from 'svelte';

    // Traka zaglavlja preko cele širine ekrana: naslov stranice levo, glavne radnje desno (actions).
    // overlap ostavlja mesto dole da sadržaj ispod (npr. kartice statistike) delimično pređe preko trake.
    let {
        title,
        icon,
        subtitle,
        overlap = false,
        actions
    }: {
        title: string;
        icon: string;
        subtitle?: string;
        overlap?: boolean;
        actions?: Snippet;
    } = $props();
</script>

<header class="page-header" class:page-header-overlap={overlap}>
    <div class="container d-flex flex-wrap justify-content-between align-items-center gap-3 page-fade">
        <div>
            <h1 class="page-header-title"><i class="bi {icon} me-2"></i>{title}</h1>
            {#if subtitle}
                <p class="page-header-subtitle mb-0">{subtitle}</p>
            {/if}
        </div>

        {#if actions}
            <div class="d-flex flex-wrap align-items-center gap-3">
                {@render actions()}
            </div>
        {/if}
    </div>
</header>
