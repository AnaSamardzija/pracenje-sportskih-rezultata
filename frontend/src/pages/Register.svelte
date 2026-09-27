<script lang="ts">
    import {onMount} from 'svelte';
    import {push} from 'svelte-spa-router';
    import {register} from '../lib/auth/auth.service';
    import DatabasePicker from '../lib/components/DatabasePicker.svelte';
    import type {StorageType} from '../lib/types/auth.types';

    let username = $state('');
    let firstName = $state('');
    let lastName = $state('');
    let email = $state('');
    let password = $state('');
    let confirmPassword = $state('');
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

        // Ponovljena lozinka se proverava samo ovde, backend dobija jednu lozinku
        if (password !== confirmPassword) {
            error = 'Passwords do not match';
            return;
        }

        loading = true;
        try {
            await register({
                username,
                password,
                firstName: firstName.trim() || null,
                lastName: lastName.trim() || null,
                email,
                storageType
            });
            push('/');
        } catch (err) {
            error = err instanceof Error ? err.message : 'Registration failed';
        } finally {
            loading = false;
        }
    }
</script>

<div class="auth-page page-fade">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-sm-10 col-md-8 col-lg-5">
                <div class="card">
                    <div class="card-body p-4">
                        <div class="text-center mb-4">
                            <span class="icon-circle mb-2"><i class="bi bi-person-plus-fill"></i></span>
                            <h4 class="mb-1">Create an account</h4>
                            <p class="text-muted small mb-0">Join groups, record matches and follow rankings</p>
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

                            <!-- minlength prati @Size(min = 4) iz RegisterRequest -->
                            <div class="mb-3">
                                <label class="form-label" for="password">Password</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                    <input id="password"
                                           type="password"
                                           class="form-control"
                                           autocomplete="new-password"
                                           minlength="4"
                                           bind:value={password}
                                           required/>
                                </div>
                                <div class="form-text">At least 4 characters.</div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label" for="confirmPassword">Confirm password</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                                    <input id="confirmPassword"
                                           type="password"
                                           class="form-control"
                                           autocomplete="new-password"
                                           bind:value={confirmPassword}
                                           required/>
                                </div>
                            </div>

                            <DatabasePicker bind:value={storageType}/>

                            <button type="submit" class="btn btn-primary w-100" disabled={loading}>
                                {#if loading}
                                    <span class="spinner-border spinner-border-sm me-2"></span>Creating account...
                                {:else}
                                    <i class="bi bi-person-plus me-2"></i>Create account
                                {/if}
                            </button>
                        </form>

                        <p class="text-center text-muted small mt-3 mb-0">
                            Already have an account? <a href="#/login">Sign in</a>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
