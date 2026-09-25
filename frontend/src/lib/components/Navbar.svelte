<script lang="ts">
    import {push} from 'svelte-spa-router';
    import {authStore} from '../auth/auth.store';
    import {logout} from '../auth/auth.service';
    import {STORAGE_LABELS} from '../types/auth.types';

    function handleLogout() {
        logout();
        push('/login');
    }
</script>

<nav class="navbar navbar-app" data-bs-theme="dark">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="#/">
            <i class="bi bi-trophy-fill"></i>
            Sports Results
        </a>

        <div class="d-flex align-items-center gap-2">
            {#if $authStore.storageType}
                <span class="badge rounded-pill text-bg-light" title="Database selected at sign in">
                    <i class="bi bi-database me-1"></i>{STORAGE_LABELS[$authStore.storageType]}
                </span>
            {/if}
            <button class="btn btn-outline-light btn-sm" title="Sign out" aria-label="Sign out" onclick={handleLogout}>
                <i class="bi bi-box-arrow-right"></i>
                <span class="d-none d-sm-inline ms-1">Sign out</span>
            </button>
        </div>
    </div>
</nav>
