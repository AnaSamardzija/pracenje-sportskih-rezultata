<script lang="ts">
    import {push} from 'svelte-spa-router';
    import {
        addMember,
        deleteGroup,
        getGroup,
        getMembers,
        joinGroup,
        leaveGroup,
        removeMember
    } from '../../lib/api/groups.api';
    import {authStore} from '../../lib/auth/auth.store';
    import ConfirmModal from '../../lib/components/ConfirmModal.svelte';
    import PageHeader from '../../lib/components/PageHeader.svelte';
    import {GROUP_ROLE_LABELS, type GroupResponse, type MemberResponse} from '../../lib/types/group.types';
    import {formatDate} from '../../lib/utils/date';

    let {params}: {params?: {id?: string}} = $props();
    const groupId = $derived(params?.id ?? '');

    let group = $state<GroupResponse | null>(null);
    let members = $state<MemberResponse[]>([]);
    let loading = $state(true);
    let error = $state<string | null>(null);
    let busy = $state(false);

    // Dodavanje člana ima svoju poruku, da greška (npr. nepostojeći username) stoji pored forme
    let newMember = $state('');
    let adding = $state(false);
    let addError = $state<string | null>(null);
    let addSuccess = $state<string | null>(null);

    let showLeave = $state(false);
    let showDelete = $state(false);
    let showRemove = $state(false);
    let memberToRemove = $state<MemberResponse | null>(null);

    const myId = $derived($authStore.user?.id);
    const isAdmin = $derived(group?.myRole === 'GROUP_ADMIN');

    // Učitavanje prati id iz adrese: ruter ne pravi stranicu ponovo kad se promeni samo id (npr. /groups/3 → /groups/5),
    // pa se tada sve vraća na početak, da dugmad nikad ne rade nad grupom koja nije prikazana
    $effect(() => {
        const id = groupId;
        group = null;
        members = [];
        loading = true;
        error = null;
        newMember = '';
        addError = null;
        addSuccess = null;
        load(id);
    });

    // Grupa i članovi se uvek učitavaju zajedno: posle izlaska ili izbacivanja backend može da promeni i ulogu i broj članova.
    // Odgovor koji stigne za id koji više nije u adresi se odbacuje.
    async function load(id = groupId) {
        try {
            const [loadedGroup, loadedMembers] = await Promise.all([getGroup(id), getMembers(id)]);
            if (id !== groupId) return;
            group = loadedGroup;
            members = loadedMembers;
        } catch (e) {
            if (id !== groupId) return;
            error = e instanceof Error ? e.message : 'Failed to load group';
        } finally {
            if (id === groupId) loading = false;
        }
    }

    async function run(action: () => Promise<void>) {
        error = null;
        busy = true;
        try {
            await action();
            await load();
        } catch (e) {
            error = e instanceof Error ? e.message : 'Something went wrong';
        } finally {
            busy = false;
        }
    }

    const handleJoin = () => run(() => joinGroup(groupId));
    const handleLeave = () => run(() => leaveGroup(groupId));

    function confirmRemove(member: MemberResponse) {
        memberToRemove = member;
        showRemove = true;
    }

    function handleRemove() {
        const member = memberToRemove;
        if (member) run(() => removeMember(groupId, member.userId));
    }

    async function handleDelete() {
        error = null;
        try {
            await deleteGroup(groupId);
            push('/groups');
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to delete group';
        }
    }

    async function handleAddMember(e: SubmitEvent) {
        e.preventDefault();
        addError = null;
        addSuccess = null;
        adding = true;
        try {
            const added = await addMember(groupId, {username: newMember.trim()});
            addSuccess = `${added.username} was added to the group`;
            newMember = '';
            await load();
        } catch (err) {
            addError = err instanceof Error ? err.message : 'Failed to add member';
        } finally {
            adding = false;
        }
    }
</script>

<PageHeader title={group?.name ?? 'Group'}
            icon="bi-people"
            subtitle={group?.description ?? (group ? 'No description.' : undefined)}>
    {#snippet actions()}
        {#if group}
            {#if isAdmin}
                <a class="btn btn-light" href="#/groups/{group.id}/edit"><i class="bi bi-pencil me-1"></i>Edit</a>
                <button class="btn btn-outline-light" disabled={busy} onclick={() => showDelete = true}>
                    <i class="bi bi-trash me-1"></i>Delete
                </button>
            {/if}
            {#if group.myRole}
                <button class="btn btn-outline-light" disabled={busy} onclick={() => showLeave = true}>
                    <i class="bi bi-box-arrow-left me-1"></i>Leave
                </button>
            {:else}
                <button class="btn btn-light" disabled={busy} onclick={handleJoin}>
                    <i class="bi bi-box-arrow-in-right me-1"></i>Join group
                </button>
            {/if}
        {/if}
    {/snippet}
</PageHeader>

<div class="container py-4 page-fade">
    {#if error}
        <div class="alert alert-danger d-flex align-items-center">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
        </div>
    {/if}

    {#if loading}
        <div class="text-center py-5">
            <div class="spinner-border text-primary"></div>
        </div>
    {:else if !group}
        <a class="btn btn-outline-secondary" href="#/groups"><i class="bi bi-arrow-left me-1"></i>Back to groups</a>
    {:else}
        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-person-lines-fill me-2 text-primary"></i>Members
                        <span class="badge bg-primary-subtle text-primary-emphasis ms-1">{members.length}</span>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead>
                            <tr>
                                <th>Player</th>
                                <th>Role</th>
                                <th class="d-none d-sm-table-cell">Joined</th>
                                {#if isAdmin}
                                    <th><span class="visually-hidden">Actions</span></th>
                                {/if}
                            </tr>
                            </thead>
                            <tbody>
                            {#each members as member (member.userId)}
                                <tr class:row-inactive={!member.active}>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <span class="icon-circle icon-circle-xs flex-shrink-0">{member.username[0].toUpperCase()}</span>
                                            <span class="fw-semibold">{member.username}</span>
                                            {#if member.userId === myId}
                                                <span class="badge text-bg-primary">You</span>
                                            {/if}
                                            {#if !member.active}
                                                <span class="badge text-bg-secondary" title="This account is deactivated">Deactivated</span>
                                            {/if}
                                        </div>
                                    </td>
                                    <td>
                                        {#if member.roleInGroup === 'GROUP_ADMIN'}
                                            <span class="badge bg-primary-subtle text-primary-emphasis"><i class="bi bi-shield-check me-1"></i>{GROUP_ROLE_LABELS[member.roleInGroup]}</span>
                                        {:else}
                                            <span class="text-muted small">{GROUP_ROLE_LABELS[member.roleInGroup]}</span>
                                        {/if}
                                    </td>
                                    <td class="d-none d-sm-table-cell text-muted small">{formatDate(member.joinedAt)}</td>
                                    {#if isAdmin}
                                        <td class="text-end">
                                            {#if member.userId !== myId}
                                                <button class="btn btn-sm btn-outline-danger"
                                                        title="Remove from group"
                                                        aria-label="Remove {member.username} from group"
                                                        disabled={busy}
                                                        onclick={() => confirmRemove(member)}>
                                                    <i class="bi bi-person-dash"></i>
                                                </button>
                                            {/if}
                                        </td>
                                    {/if}
                                </tr>
                            {/each}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- Bočna kolona: podaci o grupi i, za admina, dodavanje člana -->
            <div class="col-lg-4">
                <div class="card mb-4">
                    <div class="card-header">
                        <i class="bi bi-info-circle me-2 text-primary"></i>About
                    </div>
                    <ul class="list-group list-group-flush">
                        <li class="list-group-item d-flex justify-content-between">
                            <span class="text-muted">Created</span><span>{formatDate(group.createdAt)}</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between">
                            <span class="text-muted">Members</span><span>{group.memberCount}</span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between">
                            <span class="text-muted">Your role</span>
                            <span>{group.myRole ? GROUP_ROLE_LABELS[group.myRole] : 'Not a member'}</span>
                        </li>
                    </ul>
                </div>

                {#if isAdmin}
                    <div class="card">
                        <div class="card-header">
                            <i class="bi bi-person-plus me-2 text-primary"></i>Add member
                        </div>
                        <div class="card-body">
                            {#if addError}
                                <div class="alert alert-danger d-flex align-items-center py-2 small">
                                    <i class="bi bi-exclamation-triangle-fill me-2"></i>{addError}
                                </div>
                            {/if}
                            {#if addSuccess}
                                <div class="alert alert-success d-flex align-items-center py-2 small">
                                    <i class="bi bi-check-circle-fill me-2"></i>{addSuccess}
                                </div>
                            {/if}
                            <form onsubmit={handleAddMember}>
                                <label class="form-label" for="newMember">Username</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-at"></i></span>
                                    <input id="newMember" class="form-control" autocomplete="off" bind:value={newMember} required/>
                                    <button type="submit" class="btn btn-primary" disabled={adding}>Add</button>
                                </div>
                                <div class="form-text">The player must already have an account in this database.</div>
                            </form>
                        </div>
                    </div>
                {/if}
            </div>
        </div>
    {/if}
</div>

<ConfirmModal
    bind:show={showLeave}
    title="Leave group"
    message={isAdmin
        ? `Leave '${group?.name}'? If you are its only admin, the longest-standing active member becomes the new admin.`
        : `Leave '${group?.name}'? Your matches stay in the group history.`}
    confirmLabel="Leave"
    onConfirm={handleLeave}
/>

<ConfirmModal
    bind:show={showDelete}
    title="Delete group"
    message="Delete '{group?.name}' permanently? This is only possible while the group has no recorded matches."
    onConfirm={handleDelete}
/>

<ConfirmModal
    bind:show={showRemove}
    title="Remove member"
    message="Remove {memberToRemove?.username} from '{group?.name}'? Their matches stay in the group history."
    confirmLabel="Remove"
    onConfirm={handleRemove}
/>
