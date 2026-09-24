import type {StorageType} from './auth.types';

export async function login(username: string, password: string, storageType: StorageType) {
    const res = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password, storageType})
    });

    if (!res.ok) {
        // Backend vraća ErrorDto {timestamp, message}
        const body = await res.json().catch(() => null);
        throw new Error(body?.message ?? 'Login failed');
    }

    return res.json() as Promise<{accessToken: string}>;
}
