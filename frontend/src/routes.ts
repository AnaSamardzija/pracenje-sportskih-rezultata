import {wrap} from 'svelte-spa-router/wrap';
import Login from './pages/Login.svelte';
import Register from './pages/Register.svelte';
import Home from './pages/Home.svelte';
import Profile from './pages/Profile.svelte';
import {requireAuth, requireGuest} from './lib/auth/auth.guard';

const guarded = (component: any) => wrap({component, conditions: [requireAuth]});
const guestOnly = (component: any) => wrap({component, conditions: [requireGuest]});

export const routes = {
    '/login': guestOnly(Login),
    '/register': guestOnly(Register),
    '/': guarded(Home),
    '/profile': guarded(Profile),
};
