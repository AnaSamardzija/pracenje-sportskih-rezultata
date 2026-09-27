<script lang="ts">
    import {onMount} from 'svelte';
    import {getAllGroups, joinGroup} from '../../lib/api/groups.api';
    import PageHeader from '../../lib/components/PageHeader.svelte';
    import {GROUP_ROLE_LABELS, type GroupResponse} from '../../lib/types/group.types';

    const SEARCH_DELAY_MS = 300;

    let groups = $state<GroupResponse[]>([]);
    let loading = $state(true);
    let error = $state<string | null>(null);
    let mine = $state(false);
    let search = $state('');
    let joiningId = $state<string | null>(null);

    let searchTimer: ReturnType<typeof setTimeout>;

    onMount(load);

    async function load() {
        loading = true;
        error = null;
        try {
            groups = await getAllGroups({mine, search: search.trim()});
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to load groups';
        } finally {
            loading = false;
        }
    }

    function showMine(value: boolean) {
        mine = value;
        load();
    }

    // Pretraga ide na backend tek kad korisnik zastane sa kucanjem, a ne na svako slovo
    function handleSearch() {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(load, SEARCH_DELAY_MS);
    }

    // Posle pridruživanja red se menja na licu mesta, bez ponovnog učitavanja liste
    async function handleJoin(group: GroupResponse) {
        error = null;
        joiningId = group.id;
        try {
            await joinGroup(group.id);
            groups = groups.map(g => g.id === group.id ? {...g, myRole: 'MEMBER', memberCount: g.memberCount + 1} : g);
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to join group';
        } finally {
            joiningId = null;
        }
    }
</script>

<PageHeader title="Groups" icon="bi-people" subtitle="Groups are where you play, record matches and compete in rankings.">
    {#snippet actions()}
        <a class="btn btn-light" href="#/groups/new">
            <i class="bi bi-plus-lg me-1"></i>New group
        </a>
    {/snippet}
</PageHeader>

<div class="container py-4 page-fade">
    {#if error}
        <div class="alert alert-danger d-flex align-items-center">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
        </div>
    {/if}

    <div class="row g-4">
        <div class="col-lg-8">
            <div class="card">
                <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
                    <ul class="nav nav-pills nav-pills-app">
                        <li class="nav-item">
                            <button class="nav-link" class:active={!mine} onclick={() => showMine(false)}>All groups</button>
                        </li>
                        <li class="nav-item">
                            <button class="nav-link" class:active={mine} onclick={() => showMine(true)}>My groups</button>
                        </li>
                    </ul>
                    <div class="input-group input-group-sm search-box">
                        <span class="input-group-text"><i class="bi bi-search"></i></span>
                        <input class="form-control"
                               type="search"
                               placeholder="Search by name"
                               aria-label="Search groups by name"
                               bind:value={search}
                               oninput={handleSearch}/>
                    </div>
                </div>

                {#if loading}
                    <div class="card-body text-center py-5">
                        <div class="spinner-border text-primary"></div>
                    </div>
                {:else if groups.length === 0}
                    <div class="card-body text-center text-muted py-5">
                        <i class="bi bi-people fs-1 d-block mb-2"></i>
                        {#if search.trim()}
                            No groups match "{search.trim()}".
                        {:else if mine}
                            You are not in any group yet.
                            <div class="mt-3">
                                <button class="btn btn-primary btn-sm" onclick={() => showMine(false)}>Browse all groups</button>
                            </div>
                        {:else}
                            No groups yet. Be the first to create one.
                        {/if}
                    </div>
                {:else}
                    <ul class="list-group list-group-flush">
                        {#each groups as group (group.id)}
                            <li class="list-group-item d-flex align-items-center gap-3 py-3">
                                <span class="icon-circle icon-circle-xs flex-shrink-0">{group.name[0].toUpperCase()}</span>
                                <div class="flex-grow-1 overflow-hidden">
                                    <a class="fw-semibold text-decoration-none d-block text-truncate" href="#/groups/{group.id}">{group.name}</a>
                                    {#if group.description}
                                        <div class="text-muted small text-truncate">{group.description}</div>
                                    {/if}
                                    <div class="text-muted small">
                                        <i class="bi bi-person me-1"></i>{group.memberCount} {group.memberCount === 1 ? 'member' : 'members'}
                                    </div>
                                </div>
                                {#if group.myRole}
                                    <span class="badge bg-primary-subtle text-primary-emphasis">{GROUP_ROLE_LABELS[group.myRole]}</span>
                                {:else}
                                    <button class="btn btn-sm btn-outline-primary text-nowrap"
                                            disabled={joiningId === group.id}
                                            onclick={() => handleJoin(group)}>
                                        <i class="bi bi-box-arrow-in-right me-1"></i>Join
                                    </button>
                                {/if}
                                <a class="btn btn-sm btn-link text-decoration-none px-1" href="#/groups/{group.id}" title="Open group" aria-label="Open {group.name}">
                                    <i class="bi bi-chevron-right"></i>
                                </a>
                            </li>
                        {/each}
                    </ul>
                {/if}
            </div>
        </div>

        <!-- Bočna kolona: šta ko može u grupi -->
        <div class="col-lg-4">
            <div class="card">
                <div class="card-header">
                    <i class="bi bi-info-circle me-2 text-primary"></i>How groups work
                </div>
                <ul class="list-group list-group-flush">
                    <li class="list-group-item">
                        <div class="fw-semibold"><i class="bi bi-person text-primary me-2"></i>Member</div>
                        <div class="text-muted small">Plays and records matches with other members and appears in the group rankings.</div>
                    </li>
                    <li class="list-group-item">
                        <div class="fw-semibold"><i class="bi bi-shield-check text-primary me-2"></i>Admin</div>
                        <div class="text-muted small">Edits or deletes the group, adds members by username and removes them. Whoever creates a group becomes its admin.</div>
                    </li>
                    <li class="list-group-item">
                        <div class="fw-semibold"><i class="bi bi-box-arrow-in-right text-primary me-2"></i>Joining</div>
                        <div class="text-muted small">Any group is open: join it yourself, or ask its admin to add you.</div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>
