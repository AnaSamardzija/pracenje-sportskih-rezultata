<script lang="ts">
    import {onMount, untrack} from 'svelte';
    import {replace} from 'svelte-spa-router';
    import {deactivateUser, getAllUsers, restoreUser} from '../../lib/api/users.api';
    import ConfirmModal from '../../lib/components/ConfirmModal.svelte';
    import PageHeader from '../../lib/components/PageHeader.svelte';
    import {fullNameOf, ROLE_LABELS, type Role, userInitials, type UserDto} from '../../lib/types/user.types';
    import {queryParams, withQuery} from '../../lib/utils/query';

    type Status = 'all' | 'active' | 'deactivated';
    type Filters = {status: Status; search: string};

    const SEARCH_DELAY_MS = 300;

    const STATUS_TABS: {value: Status; label: string}[] = [
        {value: 'all', label: 'All'},
        {value: 'active', label: 'Active'},
        {value: 'deactivated', label: 'Deactivated'}
    ];

    let users = $state<UserDto[]>([]);
    let loading = $state(true);
    let error = $state<string | null>(null);
    let busyId = $state<string | null>(null);

    let showModal = $state(false);
    let selected = $state<UserDto | null>(null);

    // Status i pretraga stoje u adresi (npr. #/admin/users?status=active&search=ana), pa ih osvežavanje i povratak unazad čuvaju
    const filters = $derived.by((): Filters => {
        const params = queryParams();
        const status = params.get('status');
        return {
            status: status === 'active' || status === 'deactivated' ? status : 'all',
            search: params.get('search') ?? ''
        };
    });

    // Backend nema pretragu korisnika, pa se spisak učitava jednom i filtrira ovde
    const visibleUsers = $derived.by(() => {
        const term = filters.search.toLowerCase();
        return users.filter(u =>
            (filters.status === 'all' || u.active === (filters.status === 'active'))
            && (!term || [u.username, u.email, fullNameOf(u)].some(v => v.toLowerCase().includes(term)))
        );
    });

    const counts = $derived({
        all: users.length,
        active: users.filter(u => u.active).length,
        deactivated: users.filter(u => !u.active).length,
        admins: users.filter(u => u.roles.includes('SYSTEM_ADMIN')).length
    });

    // Polje pretrage ima svoju vrednost dok korisnik kuca; u adresu ide tek posle pauze
    let search = $state(queryParams().get('search') ?? '');
    let searchTimer: ReturnType<typeof setTimeout>;

    // Kad se adresa promeni spolja (npr. klik na Manage users u meniju), polje prati pretragu iz adrese;
    // untrack da kucanje samo po sebi ne bi pokretalo ovaj efekat
    $effect(() => {
        const fromUrl = filters.search;
        if (untrack(() => search.trim()) !== fromUrl) search = fromUrl;
    });

    onMount(load);

    async function load() {
        loading = true;
        error = null;
        try {
            users = await getAllUsers();
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to load users';
        } finally {
            loading = false;
        }
    }

    function applyFilters(next: Filters) {
        replace(withQuery('/admin/users', {status: next.status === 'all' ? '' : next.status, search: next.search}));
    }

    const showStatus = (status: Status) => applyFilters({...filters, status});

    function handleSearch() {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(() => applyFilters({...filters, search: search.trim()}), SEARCH_DELAY_MS);
    }

    function confirmDeactivate(user: UserDto) {
        selected = user;
        showModal = true;
    }

    // Posle deaktivacije i vraćanja red se menja na licu mesta; filter statusa ga sam skloni ako više ne pripada prikazu
    async function handleDeactivate() {
        if (!selected) return;
        const id = selected.id;
        error = null;
        busyId = id;
        try {
            await deactivateUser(id);
            users = users.map(u => u.id === id ? {...u, active: false} : u);
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to deactivate user';
        } finally {
            busyId = null;
        }
    }

    async function handleRestore(id: string) {
        error = null;
        busyId = id;
        try {
            const restored = await restoreUser(id);
            users = users.map(u => u.id === id ? restored : u);
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to restore user';
        } finally {
            busyId = null;
        }
    }
</script>

<PageHeader title="Manage users" icon="bi-shield-lock" subtitle="Edit accounts, change roles and deactivate or restore users."/>

<div class="container py-4 page-fade">
    {#if error}
        <div class="alert alert-danger d-flex align-items-center">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
        </div>
    {/if}

    <div class="row g-4">
        <div class="col-lg-8 col-xl-9">
            <div class="card">
                <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
                    <ul class="nav nav-pills nav-pills-app">
                        {#each STATUS_TABS as tab (tab.value)}
                            <li class="nav-item">
                                <button class="nav-link" class:active={filters.status === tab.value} onclick={() => showStatus(tab.value)}>
                                    {tab.label} <span class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis ms-1">{counts[tab.value]}</span>
                                </button>
                            </li>
                        {/each}
                    </ul>
                    <div class="input-group input-group-sm search-box">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input class="form-control"
                               type="search"
                               placeholder="Search users"
                               aria-label="Search by username, name or email"
                               bind:value={search}
                               oninput={handleSearch}/>
                    </div>
                </div>

                {#if loading}
                    <div class="card-body text-center py-5">
                        <div class="spinner-border text-primary"></div>
                    </div>
                {:else if visibleUsers.length === 0}
                    <div class="card-body text-center text-muted py-5">
                        <i class="bi bi-person-x fs-1 d-block mb-2"></i>
                        {#if filters.search}
                            No users match "{filters.search}".
                        {:else if filters.status === 'deactivated'}
                            No deactivated users.
                        {:else}
                            No other users yet.
                        {/if}
                    </div>
                {:else}
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead>
                            <tr>
                                <th>User</th>
                                <th class="d-none d-md-table-cell">Email</th>
                                <th class="d-none d-sm-table-cell">Roles</th>
                                <th>Status</th>
                                <th><span class="visually-hidden">Actions</span></th>
                            </tr>
                            </thead>
                            <tbody>
                            {#each visibleUsers as user (user.id)}
                                <tr class:row-inactive={!user.active}>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <span class="icon-circle icon-circle-xs flex-shrink-0">{userInitials(user)}</span>
                                            <div class="overflow-hidden">
                                                <a class="fw-semibold text-decoration-none d-block text-truncate" href="#/players/{user.id}">
                                                    {fullNameOf(user) || user.username}
                                                </a>
                                                <div class="text-muted small text-truncate">@{user.username}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="d-none d-md-table-cell text-break">{user.email}</td>
                                    <td class="d-none d-sm-table-cell">
                                        {#each user.roles as role (role)}
                                            <span class="badge me-1 {role === 'SYSTEM_ADMIN' ? 'text-bg-primary' : 'bg-primary-subtle text-primary-emphasis'}">
                                                {ROLE_LABELS[role as Role] ?? role}
                                            </span>
                                        {/each}
                                    </td>
                                    <td>
                                        {#if user.active}
                                            <span class="badge text-bg-success">Active</span>
                                        {:else}
                                            <span class="badge text-bg-secondary">Deactivated</span>
                                        {/if}
                                    </td>
                                    <td class="text-end text-nowrap">
                                        {#if user.active}
                                            <a class="btn btn-sm btn-outline-primary" href="#/admin/users/{user.id}/edit" title="Edit" aria-label="Edit {user.username}">
                                                <i class="bi bi-pencil"></i>
                                            </a>
                                            <button class="btn btn-sm btn-outline-danger"
                                                    title="Deactivate"
                                                    aria-label="Deactivate {user.username}"
                                                    disabled={busyId === user.id}
                                                    onclick={() => confirmDeactivate(user)}>
                                                <i class="bi bi-person-slash"></i>
                                            </button>
                                        {:else}
                                            <button class="btn btn-sm btn-outline-primary" disabled={busyId === user.id} onclick={() => handleRestore(user.id)}>
                                                <i class="bi bi-arrow-counterclockwise me-1"></i>Restore
                                            </button>
                                        {/if}
                                    </td>
                                </tr>
                            {/each}
                            </tbody>
                        </table>
                    </div>
                {/if}
            </div>
        </div>

        <!-- Bočna kolona: brojke i šta znači deaktivacija -->
        <div class="col-lg-4 col-xl-3">
            <div class="card mb-4">
                <div class="card-header">
                    <i class="bi bi-people me-2 text-primary"></i>Overview
                </div>
                <ul class="list-group list-group-flush">
                    <li class="list-group-item d-flex justify-content-between">
                        <span><i class="bi bi-person-check text-success me-2"></i>Active</span><span class="fw-semibold">{counts.active}</span>
                    </li>
                    <li class="list-group-item d-flex justify-content-between">
                        <span><i class="bi bi-person-slash text-secondary me-2"></i>Deactivated</span><span class="fw-semibold">{counts.deactivated}</span>
                    </li>
                    <li class="list-group-item d-flex justify-content-between">
                        <span><i class="bi bi-shield-lock text-primary me-2"></i>System admins</span><span class="fw-semibold">{counts.admins}</span>
                    </li>
                </ul>
                <div class="card-body border-top text-muted small">
                    Your own account is not listed; change it in <a href="#/profile">Account settings</a>.
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <i class="bi bi-info-circle me-2 text-primary"></i>Deactivation
                </div>
                <div class="card-body text-muted small">
                    A deactivated user cannot sign in and is signed out right away. Their matches, rankings and
                    memberships stay, and in groups where they were admin the oldest active member takes over.
                    Restoring brings the account back as it was, except for those admin roles.
                </div>
            </div>
        </div>
    </div>
</div>

<ConfirmModal
    bind:show={showModal}
    title="Deactivate user"
    message="Deactivate @{selected?.username}? They will be signed out and will not be able to sign in. Their matches and rankings stay, and you can restore the account later."
    confirmLabel="Deactivate"
    onConfirm={handleDeactivate}
/>
