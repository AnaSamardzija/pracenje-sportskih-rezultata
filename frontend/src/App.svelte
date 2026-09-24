<script lang="ts">
    import Router from 'svelte-spa-router';
    import {routes} from './routes';
    import {authStore} from './lib/auth/auth.store';
    import Navbar from './lib/components/Navbar.svelte';
</script>

{#if $authStore.accessToken}
    <Navbar/>
{/if}

<Router {routes}/>
