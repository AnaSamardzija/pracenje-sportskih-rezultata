import type {Component} from 'svelte';
import {wrap} from 'svelte-spa-router/wrap';
import Login from './pages/Login.svelte';
import Register from './pages/Register.svelte';
import Home from './pages/Home.svelte';
import Profile from './pages/Profile.svelte';
import SportList from './pages/sports/SportList.svelte';
import SportForm from './pages/sports/SportForm.svelte';
import GroupList from './pages/groups/GroupList.svelte';
import GroupForm from './pages/groups/GroupForm.svelte';
import GroupDetail from './pages/groups/GroupDetail.svelte';
import MatchList from './pages/matches/MatchList.svelte';
import MatchDetail from './pages/matches/MatchDetail.svelte';
import MatchForm from './pages/matches/MatchForm.svelte';
import RankingList from './pages/rankings/RankingList.svelte';
import PlayerProfile from './pages/players/PlayerProfile.svelte';
import UserList from './pages/users/UserList.svelte';
import UserForm from './pages/users/UserForm.svelte';
import NotFound from './pages/NotFound.svelte';
import {requireAuth, requireGuest, requireSystemAdmin} from './lib/auth/auth.guard';

const guarded = (component: Component) => wrap({component, conditions: [requireAuth]});
const guestOnly = (component: Component) => wrap({component, conditions: [requireGuest]});
const adminOnly = (component: Component) => wrap({component, conditions: [requireAuth, requireSystemAdmin]});

export const routes = {
    '/login': guestOnly(Login),
    '/register': guestOnly(Register),
    '/': guarded(Home),
    '/profile': guarded(Profile),

    '/sports': guarded(SportList),
    '/sports/new': adminOnly(SportForm),
    '/sports/:id/edit': adminOnly(SportForm),

    '/groups': guarded(GroupList),
    '/groups/new': guarded(GroupForm),
    '/groups/:id/edit': guarded(GroupForm),
    '/groups/:id': guarded(GroupDetail),

    '/matches': guarded(MatchList),
    // Mora biti pre /matches/:id, inače bi „new“ bio id meča
    '/matches/new': guarded(MatchForm),
    '/matches/:id/edit': guarded(MatchForm),
    '/matches/:id': guarded(MatchDetail),

    '/rankings': guarded(RankingList),

    // Profil igrača, i sopstveni (My profile u meniju) i tuđi; /profile su podešavanja naloga
    '/players/:id': guarded(PlayerProfile),

    // Admin panel: upravljanje nalozima, samo SYSTEM_ADMIN
    '/admin/users': adminOnly(UserList),
    '/admin/users/:id/edit': adminOnly(UserForm),

    // Mora biti poslednja: sve ostale putanje
    '*': guarded(NotFound),
};
