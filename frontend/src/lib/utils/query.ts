import {router} from 'svelte-spa-router';

// Upit iz adrese (deo posle „?“, npr. #/matches?groupId=3); pozvan unutar $derived prati promene adrese
export function queryParams(): URLSearchParams {
    return new URLSearchParams(router.querystring ?? '');
}

// Putanja sa upitom u kojoj se prazne vrednosti izostavljaju, npr. withQuery('/matches', {groupId: '3', sportId: ''}) → '/matches?groupId=3'
export function withQuery(path: string, values: Record<string, string | undefined>): string {
    const params = new URLSearchParams();
    for (const [key, value] of Object.entries(values)) {
        if (value) params.set(key, value);
    }
    return params.size ? `${path}?${params}` : path;
}
