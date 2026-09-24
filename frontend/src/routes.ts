import {wrap} from 'svelte-spa-router/wrap';
import Login from './pages/Login.svelte';
import Home from './pages/Home.svelte';
import {requireAuth} from './lib/auth/auth.guard';

export const routes = {
    '/login': Login,
    '/': wrap({component: Home, conditions: [requireAuth]}),
};
