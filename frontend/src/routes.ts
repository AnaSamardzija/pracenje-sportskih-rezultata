import {wrap} from 'svelte-spa-router/wrap';
import Login from './pages/Login.svelte';
import Register from './pages/Register.svelte';
import Home from './pages/Home.svelte';
import Profile from './pages/Profile.svelte';
import SportList from './pages/sports/SportList.svelte';
import SportForm from './pages/sports/SportForm.svelte';
import {requireAuth, requireGuest, requireSystemAdmin} from './lib/auth/auth.guard';

const guarded = (component: any) => wrap({component, conditions: [requireAuth]});
const guestOnly = (component: any) => wrap({component, conditions: [requireGuest]});
const adminOnly = (component: any) => wrap({component, conditions: [requireAuth, requireSystemAdmin]});

export const routes = {
    '/login': guestOnly(Login),
    '/register': guestOnly(Register),
    '/': guarded(Home),
    '/profile': guarded(Profile),

    '/sports': guarded(SportList),
    '/sports/new': adminOnly(SportForm),
    '/sports/:id/edit': adminOnly(SportForm),
};
