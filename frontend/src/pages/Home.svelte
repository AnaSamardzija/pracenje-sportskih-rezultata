<script lang="ts">
    import {onMount} from 'svelte';
    import {push} from 'svelte-spa-router';
    import {apiFetch} from '../lib/api/client';
    import {authStore} from '../lib/auth/auth.store';
    import {logout} from '../lib/auth/auth.service';

    let user = $state<{username: string; firstName: string; lastName: string} | null>(null);

    // Zaštićena ruta: dokaz da token radi i da backend čita iz baze izabrane pri prijavi
    onMount(async () => {
        const res = await apiFetch('/api/v1/users/me');
        if (res.ok) {
            user = await res.json();
        }
    });

    function handleLogout() {
        logout();
        push('/login');
    }
</script>

<div class="container mt-5">
    {#if user}
        <p>Signed in as <b>{user.username}</b> ({user.firstName} {user.lastName})
            on <b>{$authStore.storageType}</b>.</p>
    {/if}
    <button class="btn btn-outline-secondary" onclick={handleLogout}>Sign out</button>
</div>
