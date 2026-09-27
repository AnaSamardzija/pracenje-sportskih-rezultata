<script lang="ts">
    import {onMount} from 'svelte';
    import {getMe} from '../lib/api/users.api';
    import {authStore} from '../lib/auth/auth.store';
    import {STORAGE_LABELS} from '../lib/types/auth.types';
    import {userInitials, type UserDto} from '../lib/types/user.types';

    let user = $state<UserDto | null>(null);
    let loading = $state(true);
    let error = $state<string | null>(null);

    // Zaštićena ruta: dokaz da token radi i da backend čita iz baze izabrane pri prijavi
    onMount(async () => {
        try {
            user = await getMe();
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to load user';
        } finally {
            loading = false;
        }
    });
</script>

<div class="container py-4 page-fade">
    {#if error}
        <div class="alert alert-danger">{error}</div>
    {/if}

    {#if loading}
        <div class="text-center py-5">
            <div class="spinner-border text-primary"></div>
        </div>
    {:else if user}
        <h2 class="mb-1">Welcome, {user.firstName || user.username}</h2>
        <p class="text-muted mb-4">
            You are signed in to <b>{$authStore.storageType ? STORAGE_LABELS[$authStore.storageType] : '—'}</b>.
        </p>

        <div class="row g-4">
            <div class="col-lg-6">
                <div class="card card-hover h-100">
                    <div class="card-header">
                        <i class="bi bi-person-badge me-2 text-primary"></i>Account
                    </div>
                    <div class="card-body">
                        <div class="d-flex align-items-center gap-3 mb-3">
                            <span class="icon-circle">{userInitials(user)}</span>
                            <div>
                                <div class="fw-semibold">{user.firstName} {user.lastName}</div>
                                <div class="text-muted small">@{user.username}</div>
                            </div>
                        </div>

                        <table class="table table-sm mb-0">
                            <tbody>
                            <tr>
                                <th class="text-muted fw-normal"><i class="bi bi-envelope me-2"></i>Email</th>
                                <td>{user.email}</td>
                            </tr>
                            <tr>
                                <th class="text-muted fw-normal"><i class="bi bi-shield-check me-2"></i>Roles</th>
                                <td>
                                    {#each user.roles as role (role)}
                                        <span class="badge text-bg-primary me-1">{role}</span>
                                    {/each}
                                </td>
                            </tr>
                            </tbody>
                        </table>

                        <a class="btn btn-outline-primary btn-sm mt-3" href="#/profile">
                            <i class="bi bi-pencil me-1"></i>Edit profile
                        </a>
                    </div>
                </div>
            </div>

            <div class="col-lg-6">
                <div class="card card-hover h-100">
                    <div class="card-header">
                        <i class="bi bi-bullseye me-2 text-primary"></i>Sports
                    </div>
                    <div class="card-body d-flex flex-column">
                        <p class="text-muted">Browse the sports you can record matches in and see how each one is scored.</p>
                        <a class="btn btn-outline-primary btn-sm mt-auto align-self-start" href="#/sports">
                            <i class="bi bi-arrow-right me-1"></i>View sports
                        </a>
                    </div>
                </div>
            </div>
        </div>
    {/if}
</div>
