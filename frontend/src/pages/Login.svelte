<script lang="ts">
    import {onMount} from 'svelte';
    import {push} from 'svelte-spa-router';
    import {login} from '../lib/auth/auth.service';
    import type {StorageType} from '../lib/types/auth.types';

    let username = $state('');
    let password = $state('');
    let storageType = $state<StorageType>('MARIADB');
    let error = $state<string | null>(null);
    let loading = $state(false);

    let usernameInput: HTMLInputElement;

    onMount(() => {
        usernameInput.focus();
    });

    async function handleSubmit(e: SubmitEvent) {
        e.preventDefault();
        error = null;
        loading = true;

        try {
            await login(username, password, storageType);
            push('/');
        } catch (err) {
            error = err instanceof Error ? err.message : 'Login failed';
        } finally {
            loading = false;
        }
    }
</script>

<div class="auth-page page-fade">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-sm-9 col-md-6 col-lg-4">
                <div class="card">
                    <div class="card-body p-4">
                        <div class="text-center mb-4">
                            <span class="icon-circle mb-2"><i class="bi bi-trophy-fill"></i></span>
                            <h4 class="mb-1">Sports Results</h4>
                            <p class="text-muted small mb-0">Sign in to track matches and rankings</p>
                        </div>

                        {#if error}
                            <div class="alert alert-danger d-flex align-items-center py-2 small">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i>{error}
                            </div>
                        {/if}

                        <form onsubmit={handleSubmit}>
                            <div class="mb-3">
                                <label class="form-label" for="username">Username</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-person"></i></span>
                                    <input id="username"
                                           class="form-control"
                                           autocomplete="username"
                                           bind:value={username}
                                           bind:this={usernameInput}
                                           required/>
                                </div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label" for="password">Password</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                    <input id="password"
                                           type="password"
                                           class="form-control"
                                           autocomplete="current-password"
                                           bind:value={password}
                                           required/>
                                </div>
                            </div>

                            <!-- Vrednosti su tačno enum stringovi koje backend očekuje u AuthRequest.storageType -->
                            <fieldset class="mb-4">
                                <legend class="form-label fs-6">Database</legend>
                                <div class="btn-group w-100">
                                    <input id="db-maria" class="btn-check" type="radio" value="MARIADB" bind:group={storageType}/>
                                    <label class="btn btn-outline-primary" for="db-maria">
                                        <i class="bi bi-database me-1"></i>MariaDB
                                    </label>
                                    <input id="db-mongo" class="btn-check" type="radio" value="MONGODB" bind:group={storageType}/>
                                    <label class="btn btn-outline-primary" for="db-mongo">
                                        <i class="bi bi-database me-1"></i>MongoDB
                                    </label>
                                </div>
                            </fieldset>

                            <button type="submit" class="btn btn-primary w-100" disabled={loading}>
                                {#if loading}
                                    <span class="spinner-border spinner-border-sm me-2"></span>Signing in...
                                {:else}
                                    <i class="bi bi-box-arrow-in-right me-2"></i>Sign in
                                {/if}
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
