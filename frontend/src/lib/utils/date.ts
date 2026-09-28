// Datum sa backend-a (LocalDateTime bez zone) za prikaz, npr. „25 Sept 2026“
export function formatDate(value: string): string {
    return new Date(value).toLocaleDateString('en-GB', {day: 'numeric', month: 'short', year: 'numeric'});
}

// Datum i vreme, npr. „25 Sept 2026, 18:30“ (vreme odigravanja meča)
export function formatDateTime(value: string): string {
    return new Date(value).toLocaleString('en-GB', {day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'});
}

// Vrednost za <input type="datetime-local"> u lokalnom vremenu, npr. „2026-09-28T18:30“ (toISOString bi dao UTC)
export function toDateTimeInput(date: Date): string {
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}
