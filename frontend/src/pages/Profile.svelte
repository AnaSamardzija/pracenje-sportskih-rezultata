<script lang="ts">
    import {onMount} from 'svelte';
    import {changePassword, getMe, updateMe} from '../lib/api/users.api';
    import {authStore} from '../lib/auth/auth.store';
    import {STORAGE_LABELS} from '../lib/types/auth.types';
    import {userInitials, type UserDto} from '../lib/types/user.types';

    let user = $state<UserDto | null>(null);
    let loading = $state(true);
    let loadError = $state<string | null>(null);

    // Izmena profila
    let firstName = $state('');
    let lastName = $state('');
    let email = $state('');
    let saving = $state(false);
    let profileError = $state<string | null>(null);
    let profileSuccess = $state<string | null>(null);

    // Promena lozinke
    let oldPassword = $state('');
    let newPassword = $state('');
    let confirmPassword = $state('');
    let changing = $state(false);
    let passwordError = $state<string | null>(null);
    let passwordSuccess = $state<string | null>(null);

    const fullName = $derived(user ? [user.firstName, user.lastName].filter(Boolean).join(' ') : '');

    // Dugme Save je aktivno samo kad se nešto promenilo u odnosu na sačuvan profil
    const profileChanged = $derived(
        user !== null && (firstName.trim() !== (user.firstName ?? '')
            || lastName.trim() !== (user.lastName ?? '')
            || email.trim() !== user.email)
    );

    function fillForm(u: UserDto) {
        firstName = u.firstName ?? '';
        lastName = u.lastName ?? '';
        email = u.email;
    }

    onMount(async () => {
        try {
            user = await getMe();
            fillForm(user);
        } catch (e) {
            loadError = e instanceof Error ? e.message : 'Failed to load user';
        } finally {
            loading = false;
        }
    });

    async function handleProfileSubmit(e: SubmitEvent) {
        e.preventDefault();
        profileError = null;
        profileSuccess = null;
        saving = true;

        try {
            // Odgovor je izmenjen korisnik, pa se pregled levo odmah osveži
            user = await updateMe({
                firstName: firstName.trim() || null,
                lastName: lastName.trim() || null,
                email: email.trim()
            });
            fillForm(user);
            profileSuccess = 'Profile updated';
        } catch (err) {
            profileError = err instanceof Error ? err.message : 'Failed to update profile';
        } finally {
            saving = false;
        }
    }

    async function handlePasswordSubmit(e: SubmitEvent) {
        e.preventDefault();
        passwordError = null;
        passwordSuccess = null;

        // Ponovljena lozinka se proverava samo ovde, backend dobija staru i novu
        if (newPassword !== confirmPassword) {
            passwordError = 'New passwords do not match';
            return;
        }

        changing = true;
        try {
            await changePassword({oldPassword, newPassword});
            // Token ostaje važeći; nova lozinka važi od sledeće prijave
            oldPassword = '';
            newPassword = '';
            confirmPassword = '';
            passwordSuccess = 'Password changed. Use the new password next time you sign in.';
        } catch (err) {
            passwordError = err instanceof Error ? err.message : 'Failed to change password';
        } finally {
            changing = false;
        }
    }
</script>

<div class="container py-4 page-fade">
    <h2 class="mb-4"><i class="bi bi-person-circle me-2 text-primary"></i>My profile</h2>

    {#if loadError}
        <div class="alert alert-danger">{loadError}</div>
    {/if}

    {#if loading}
        <div class="text-center py-5">
            <div class="spinner-border text-primary"></div>
        </div>
    {:else if user}
        <div class="row g-4">
            <div class="col-lg-4">
                <div class="card">
                    <div class="card-body text-center p-4">
                        <span class="icon-circle icon-circle-lg mb-3">{userInitials(user)}</span>
                        {#if fullName}
                            <h5 class="mb-0">{fullName}</h5>
                        {/if}
                        <div class="text-muted mb-3">@{user.username}</div>

                        <table class="table table-sm text-start mb-0">
                            <tbody>
                            <tr>
                                <th class="text-muted fw-normal"><i class="bi bi-envelope me-2"></i>Email</th>
                                <td class="text-break">{user.email}</td>
                            </tr>
                            <tr>
                                <th class="text-muted fw-normal"><i class="bi bi-shield-check me-2"></i>Roles</th>
                                <td>
                                    {#each user.roles as role (role)}
                                        <span class="badge text-bg-primary me-1">{role}</span>
                                    {/each}
                                </td>
                            </tr>
                            <tr>
                                <th class="text-muted fw-normal"><i class="bi bi-database me-2"></i>Database</th>
                                <td>{$authStore.storageType ? STORAGE_LABELS[$authStore.storageType] : '—'}</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div class="col-lg-8">
                <div class="card mb-4">
                    <div class="card-header">
                        <i class="bi bi-pencil-square me-2 text-primary"></i>Edit profile
                    </div>
                    <div class="card-body">
                        {#if profileError}
                            <div class="alert alert-danger d-flex align-items-center py-2 small">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i>{profileError}
                            </div>
                        {/if}
                        {#if profileSuccess}
                            <div class="alert alert-success d-flex align-items-center py-2 small">
                                <i class="bi bi-check-circle-fill me-2"></i>{profileSuccess}
                            </div>
                        {/if}

                        <form onsubmit={handleProfileSubmit}>
                            <div class="row g-3 mb-3">
                                <div class="col-sm-6">
                                    <label class="form-label" for="firstName">First name <span class="text-muted small">(optional)</span></label>
                                    <input id="firstName" class="form-control" autocomplete="given-name" bind:value={firstName}/>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label" for="lastName">Last name <span class="text-muted small">(optional)</span></label>
                                    <input id="lastName" class="form-control" autocomplete="family-name" bind:value={lastName}/>
                                </div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label" for="email">Email</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                                    <input id="email"
                                           type="email"
                                           class="form-control"
                                           autocomplete="email"
                                           bind:value={email}
                                           required/>
                                </div>
                            </div>

                            <button type="submit" class="btn btn-primary" disabled={saving || !profileChanged}>
                                {#if saving}
                                    <span class="spinner-border spinner-border-sm me-2"></span>Saving...
                                {:else}
                                    <i class="bi bi-check-lg me-2"></i>Save changes
                                {/if}
                            </button>
                        </form>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <i class="bi bi-key me-2 text-primary"></i>Change password
                    </div>
                    <div class="card-body">
                        {#if passwordError}
                            <div class="alert alert-danger d-flex align-items-center py-2 small">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i>{passwordError}
                            </div>
                        {/if}
                        {#if passwordSuccess}
                            <div class="alert alert-success d-flex align-items-center py-2 small">
                                <i class="bi bi-check-circle-fill me-2"></i>{passwordSuccess}
                            </div>
                        {/if}

                        <form onsubmit={handlePasswordSubmit}>
                            <!-- Skriveno korisničko ime pomaže menadžeru lozinki da zna za koji nalog je lozinka -->
                            <input type="text" class="d-none" autocomplete="username" value={user.username} readonly/>

                            <div class="mb-3">
                                <label class="form-label" for="oldPassword">Current password</label>
                                <input id="oldPassword"
                                       type="password"
                                       class="form-control"
                                       autocomplete="current-password"
                                       bind:value={oldPassword}
                                       required/>
                            </div>

                            <!-- minlength prati @Size(min = 4) iz ChangePasswordRequest -->
                            <div class="row g-3 mb-3">
                                <div class="col-sm-6">
                                    <label class="form-label" for="newPassword">New password</label>
                                    <input id="newPassword"
                                           type="password"
                                           class="form-control"
                                           autocomplete="new-password"
                                           minlength="4"
                                           bind:value={newPassword}
                                           required/>
                                    <div class="form-text">At least 4 characters.</div>
                                </div>
                                <div class="col-sm-6">
                                    <label class="form-label" for="confirmPassword">Confirm new password</label>
                                    <input id="confirmPassword"
                                           type="password"
                                           class="form-control"
                                           autocomplete="new-password"
                                           bind:value={confirmPassword}
                                           required/>
                                </div>
                            </div>

                            <button type="submit" class="btn btn-primary" disabled={changing}>
                                {#if changing}
                                    <span class="spinner-border spinner-border-sm me-2"></span>Changing...
                                {:else}
                                    <i class="bi bi-shield-lock me-2"></i>Change password
                                {/if}
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    {/if}
</div>
