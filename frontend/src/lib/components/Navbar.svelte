<script lang="ts">
    import {push, router} from 'svelte-spa-router';
    import active from 'svelte-spa-router/active';
    import {authStore} from '../auth/auth.store';
    import {logout} from '../auth/auth.service';
    import {STORAGE_LABELS} from '../types/auth.types';
    import {userInitials} from '../types/user.types';

    // Glavne stranice; regex drži link aktivnim i na podstranicama (npr. /groups/5)
    const links = [
        {href: '#/', label: 'Home', icon: 'bi-house', path: /^\/$/},
        {href: '#/groups', label: 'Groups', icon: 'bi-people', path: /^\/groups/},
        {href: '#/matches', label: 'Matches', icon: 'bi-calendar-event', path: /^\/matches/},
        {href: '#/rankings', label: 'Rankings', icon: 'bi-bar-chart-line', path: /^\/rankings/},
        {href: '#/sports', label: 'Sports', icon: 'bi-bullseye', path: /^\/sports/}
    ];

    // Meni na telefonu i padajući meni naloga otvaraju se bez Bootstrap JS-a, samo klasom show
    let menuOpen = $state(false);
    let userMenuOpen = $state(false);

    const user = $derived($authStore.user);
    const fullName = $derived(user ? [user.firstName, user.lastName].filter(Boolean).join(' ') : '');

    // Posle prelaska na drugu stranicu oba menija se zatvaraju
    $effect(() => {
        router.location;
        menuOpen = false;
        userMenuOpen = false;
    });

    function handleLogout() {
        logout();
        push('/login');
    }
</script>

<!-- Klik bilo gde van padajućeg menija ga zatvara -->
<svelte:window onclick={() => userMenuOpen = false}/>

<nav class="navbar navbar-expand-lg navbar-app" data-bs-theme="dark">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="#/">
            <i class="bi bi-trophy-fill"></i>
            Sports Results
        </a>

        <div class="d-flex align-items-center gap-2 order-lg-last">
            <a class="btn btn-primary btn-sm" href="#/matches/new" title="Record match" aria-label="Record match">
                <i class="bi bi-plus-lg"></i><span class="d-none d-sm-inline ms-1">Record match</span>
            </a>

            {#if user}
                <div class="dropdown">
                    <button class="btn btn-link p-0 border-0"
                            aria-label="Account menu"
                            aria-expanded={userMenuOpen}
                            onclick={e => { e.stopPropagation(); userMenuOpen = !userMenuOpen; }}>
                        <span class="icon-circle icon-circle-xs">{userInitials(user)}</span>
                    </button>

                    <ul class="dropdown-menu dropdown-menu-end shadow" class:show={userMenuOpen} data-bs-popper="static" data-bs-theme="light">
                        <li class="px-3 py-2">
                            <div class="fw-semibold">{fullName || user.username}</div>
                            <div class="text-muted small">@{user.username}</div>
                            {#if $authStore.storageType}
                                <span class="badge bg-primary-subtle text-primary-emphasis mt-2">
                                    <i class="bi bi-database me-1"></i>{STORAGE_LABELS[$authStore.storageType]}
                                </span>
                            {/if}
                        </li>
                        <li><hr class="dropdown-divider"></li>
                        <li>
                            <a class="dropdown-item" href="#/profile"><i class="bi bi-person-circle me-2"></i>My profile</a>
                        </li>
                        <li><hr class="dropdown-divider"></li>
                        <li>
                            <button class="dropdown-item text-danger" onclick={handleLogout}>
                                <i class="bi bi-box-arrow-right me-2"></i>Sign out
                            </button>
                        </li>
                    </ul>
                </div>
            {/if}

            <button class="navbar-toggler border-0"
                    aria-label="Toggle navigation"
                    aria-expanded={menuOpen}
                    onclick={() => menuOpen = !menuOpen}>
                <span class="navbar-toggler-icon"></span>
            </button>
        </div>

        <div class="collapse navbar-collapse" class:show={menuOpen}>
            <ul class="navbar-nav ms-lg-4 me-auto">
                {#each links as link (link.href)}
                    <li class="nav-item">
                        <a class="nav-link" href={link.href} use:active={link.path}>
                            <i class="bi {link.icon} me-2 me-lg-1"></i>{link.label}
                        </a>
                    </li>
                {/each}
            </ul>
        </div>
    </div>
</nav>
