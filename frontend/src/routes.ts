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
import {requireAuth, requireGuest, requirePermissions} from './lib/auth/auth.guard';
import type {Permission} from './lib/types/user.types';

const guarded = (component: Component) => wrap({component, conditions: [requireAuth]});
const guestOnly = (component: Component) => wrap({component, conditions: [requireGuest]});
const withPermissions = (component: Component, ...permissions: Permission[]) =>
    wrap({component, conditions: [requireAuth, requirePermissions(...permissions)]});

export const routes = {
    '/login': guestOnly(Login),
    '/register': guestOnly(Register),
    '/': guarded(Home),
    '/profile': guarded(Profile),

    '/sports': guarded(SportList),
    '/sports/new': withPermissions(SportForm, 'sports.create'),
    '/sports/:id/edit': withPermissions(SportForm, 'sports.update'),

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

    // Admin panel: upravljanje nalozima; forma izmene i učitava tuđi nalog, pa traži i users.read_all
    '/admin/users': withPermissions(UserList, 'users.read_all'),
    '/admin/users/:id/edit': withPermissions(UserForm, 'users.read_all', 'users.modify'),

    // Mora biti poslednja: sve ostale putanje
    '*': guarded(NotFound),
};
