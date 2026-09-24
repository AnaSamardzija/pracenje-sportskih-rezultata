<script lang="ts">
    import {push} from 'svelte-spa-router';
    import {login} from '../lib/auth/auth.service';
    import type {StorageType} from '../lib/auth/auth.types';

    let username = $state('');
    let password = $state('');
    let storageType = $state<StorageType>('MARIADB');
    let error = $state<string | null>(null);
    let loading = $state(false);

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

<div class="container mt-5" style="max-width: 400px">
    <h4 class="mb-4">Sports results</h4>

    {#if error}
        <div class="alert alert-danger">{error}</div>
    {/if}

    <form onsubmit={handleSubmit}>
        <div class="mb-3">
            <label class="form-label" for="username">Username</label>
            <input id="username" class="form-control" bind:value={username} required/>
        </div>

        <div class="mb-3">
            <label class="form-label" for="password">Password</label>
            <input id="password" type="password" class="form-control" bind:value={password} required/>
        </div>

        <!-- Vrednosti su tačno enum stringovi koje backend očekuje u AuthRequest.storageType -->
        <fieldset class="mb-3">
            <legend class="form-label fs-6">Database</legend>
            <div class="form-check form-check-inline">
                <input id="db-maria" class="form-check-input" type="radio" value="MARIADB" bind:group={storageType}/>
                <label class="form-check-label" for="db-maria">MariaDB</label>
            </div>
            <div class="form-check form-check-inline">
                <input id="db-mongo" class="form-check-input" type="radio" value="MONGODB" bind:group={storageType}/>
                <label class="form-check-label" for="db-mongo">MongoDB</label>
            </div>
        </fieldset>

        <button class="btn btn-primary w-100" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign in'}
        </button>
    </form>
</div>
