<script lang="ts">
    import {push} from 'svelte-spa-router';
    import active from 'svelte-spa-router/active';
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
            <span class="d-none d-lg-inline">Sports Results</span>
        </a>

        <!-- Link ostaje aktivan i na podstranicama (npr. /sports/new), zato regex umesto tačne putanje -->
        <ul class="navbar-nav flex-row gap-3 me-auto">
            <li class="nav-item">
                <a class="nav-link" href="#/sports" title="Sports" aria-label="Sports" use:active={/^\/sports/}>
                    <i class="bi bi-bullseye"></i>
                    <span class="d-none d-sm-inline ms-1">Sports</span>
                </a>
            </li>
        </ul>

        <div class="d-flex align-items-center gap-2">
            {#if $authStore.storageType}
                <span class="badge rounded-pill text-bg-light" title="Database selected at sign in">
                    <i class="bi bi-database me-1"></i>{STORAGE_LABELS[$authStore.storageType]}
                </span>
            {/if}
            <!-- use:active dodaje klasu active kad je otvorena stranica profila -->
            <a class="btn btn-outline-light btn-sm" href="#/profile" title="My profile" aria-label="My profile" use:active>
                <i class="bi bi-person-circle"></i>
                <span class="d-none d-sm-inline ms-1">Profile</span>
            </a>
            <button class="btn btn-outline-light btn-sm" title="Sign out" aria-label="Sign out" onclick={handleLogout}>
                <i class="bi bi-box-arrow-right"></i>
                <span class="d-none d-sm-inline ms-1">Sign out</span>
            </button>
        </div>
    </div>
</nav>
