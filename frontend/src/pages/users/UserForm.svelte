<script lang="ts">
    import {push} from 'svelte-spa-router';
    import {getUser, updateUser} from '../../lib/api/users.api';
    import {authStore} from '../../lib/auth/auth.store';
    import PageHeader from '../../lib/components/PageHeader.svelte';
    import {fullNameOf, hasPermission, ROLE_HINTS, ROLE_LABELS, ROLES, type Role, userInitials, type UserDto} from '../../lib/types/user.types';

    // Izmena tuđeg naloga (/admin/users/:id/edit); username se ne menja, pa se samo prikazuje
    let {params}: {params?: {id?: string}} = $props();

    const editId = $derived(params?.id ?? null);

    // Svoj nalog admin menja u podešavanjima, gde se odmah osvežava i korisnik u navigaciji
    const isOwnAccount = $derived(editId !== null && editId === $authStore.user?.id);

    let user = $state<UserDto | null>(null);
    let firstName = $state('');
    let lastName = $state('');
    let email = $state('');
    let roles = $state<Role[]>([]);

    let loadingData = $state(false);
    let saving = $state(false);
    let error = $state<string | null>(null);

    // Uloge menja samo korisnik sa users.roles.assign; ostalima su polja zaključana, pa se uloge šalju nepromenjene
    const canAssignRoles = $derived(hasPermission($authStore.user, 'users.roles.assign'));

    // Backend traži bar jednu ulogu; bez nje se forma ne šalje
    const noRole = $derived(roles.length === 0);

    // Učitavanje prati id iz adrese: ruter ne pravi stranicu ponovo kad se promeni samo id
    $effect(() => {
        if (editId && !isOwnAccount) loadUser(editId);
    });

    // Odgovor koji stigne za id koji više nije u adresi se odbacuje
    async function loadUser(id: string) {
        loadingData = true;
        user = null;
        error = null;
        try {
            const loaded = await getUser(id);
            if (id !== editId) return;
            user = loaded;
            firstName = loaded.firstName ?? '';
            lastName = loaded.lastName ?? '';
            email = loaded.email;
            roles = ROLES.filter(r => loaded.roles.includes(r));
        } catch (e) {
            if (id !== editId) return;
            error = e instanceof Error ? e.message : 'User not found';
        } finally {
            if (id === editId) loadingData = false;
        }
    }

    async function handleSubmit(e: SubmitEvent) {
        e.preventDefault();
        if (!editId || noRole) return;
        error = null;
        saving = true;

        try {
            // Prazno ime ili prezime se šalje kao null, isto kao u podešavanjima naloga
            await updateUser(editId, {
                firstName: firstName.trim() || null,
                lastName: lastName.trim() || null,
                email: email.trim(),
                roles: ROLES.filter(r => roles.includes(r))
            });
            push('/admin/users');
        } catch (err) {
            error = err instanceof Error ? err.message : 'Failed to update user';
        } finally {
            saving = false;
        }
    }
</script>

<PageHeader title="Edit user" icon="bi-pencil-square" subtitle="Change account details and roles. The username cannot be changed."/>

<div class="container py-4 page-fade">
    <div class="row justify-content-center">
        <div class="col-lg-10 col-xl-9">

            {#if error}
                <div class="alert alert-danger d-flex align-items-center">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
                </div>
            {/if}

            {#if isOwnAccount}
                <div class="alert alert-info d-flex align-items-center">
                    <i class="bi bi-info-circle-fill me-2"></i>
                    <span>This is your own account. Change it in <a href="#/profile">Account settings</a>.</span>
                </div>
                <a class="btn btn-outline-secondary" href="#/admin/users"><i class="bi bi-arrow-left me-1"></i>Back to users</a>
            {:else if loadingData}
                <div class="text-center py-5">
                    <div class="spinner-border text-primary"></div>
                </div>
            {:else if !user}
                <a class="btn btn-outline-secondary" href="#/admin/users"><i class="bi bi-arrow-left me-1"></i>Back to users</a>
            {:else}
                <div class="row g-4">
                    <!-- Bočna kolona: nalog kakav je sada sačuvan -->
                    <div class="col-md-4 order-md-last">
                        <div class="card">
                            <div class="card-body text-center p-4">
                                <span class="icon-circle icon-circle-lg mb-3">{userInitials(user)}</span>
                                {#if fullNameOf(user)}
                                    <h5 class="mb-0">{fullNameOf(user)}</h5>
                                {/if}
                                <div class="text-muted mb-3">@{user.username}</div>
                                {#if user.active}
                                    <span class="badge text-bg-success">Active</span>
                                {:else}
                                    <span class="badge text-bg-secondary">Deactivated</span>
                                {/if}
                            </div>
                            <div class="list-group list-group-flush">
                                <a class="list-group-item list-group-item-action" href="#/players/{user.id}">
                                    <i class="bi bi-person-circle me-2 text-primary"></i>View player profile
                                </a>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-8">
                        {#if !user.active}
                            <div class="alert alert-warning d-flex align-items-center">
                                <i class="bi bi-person-slash me-2"></i>This account is deactivated. Restore it from the user list to let them sign in again.
                            </div>
                        {/if}

                        <form onsubmit={handleSubmit}>
                            <div class="card mb-4">
                                <div class="card-header">
                                    <i class="bi bi-person-vcard me-2 text-primary"></i>Account
                                </div>
                                <div class="card-body">
                                    <div class="mb-3">
                                        <label class="form-label" for="username">Username</label>
                                        <input id="username" class="form-control" value={user.username} disabled/>
                                    </div>
                                    <div class="row g-3 mb-3">
                                        <div class="col-sm-6">
                                            <label class="form-label" for="firstName">First name <span class="text-muted small">(optional)</span></label>
                                            <input id="firstName" class="form-control" bind:value={firstName}/>
                                        </div>
                                        <div class="col-sm-6">
                                            <label class="form-label" for="lastName">Last name <span class="text-muted small">(optional)</span></label>
                                            <input id="lastName" class="form-control" bind:value={lastName}/>
                                        </div>
                                    </div>
                                    <div>
                                        <label class="form-label" for="email">Email</label>
                                        <input id="email" type="email" class="form-control" bind:value={email} required/>
                                    </div>
                                </div>
                            </div>

                            <div class="card mb-4">
                                <div class="card-header">
                                    <i class="bi bi-shield-check me-2 text-primary"></i>Roles
                                </div>
                                <div class="card-body">
                                    {#each ROLES as role (role)}
                                        <div class="form-check mb-2">
                                            <input id="role-{role}" class="form-check-input" type="checkbox" value={role} bind:group={roles} disabled={!canAssignRoles}/>
                                            <label class="form-check-label" for="role-{role}">
                                                <span class="fw-semibold">{ROLE_LABELS[role]}</span>
                                                <span class="d-block text-muted small">{ROLE_HINTS[role]}</span>
                                            </label>
                                        </div>
                                    {/each}
                                    {#if !canAssignRoles}
                                        <div class="form-text">Changing roles requires the users.roles.assign permission.</div>
                                    {/if}
                                    {#if noRole}
                                        <div class="text-danger small mt-2">Select at least one role.</div>
                                    {/if}
                                </div>
                            </div>

                            <div class="d-flex gap-2">
                                <button type="submit" class="btn btn-primary" disabled={saving || noRole}>
                                    {#if saving}
                                        <span class="spinner-border spinner-border-sm me-2"></span>Saving...
                                    {:else}
                                        <i class="bi bi-check-lg me-2"></i>Save changes
                                    {/if}
                                </button>
                                <a class="btn btn-outline-secondary" href="#/admin/users">Cancel</a>
                            </div>
                        </form>
                    </div>
                </div>
            {/if}
        </div>
    </div>
</div>
