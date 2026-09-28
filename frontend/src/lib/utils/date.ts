// Datum sa backend-a (LocalDateTime bez zone) za prikaz, npr. „25 Sept 2026“
export function formatDate(value: string): string {
    return new Date(value).toLocaleDateString('en-GB', {day: 'numeric', month: 'short', year: 'numeric'});
}

// Datum i vreme, npr. „25 Sept 2026, 18:30“ (vreme odigravanja meča)
export function formatDateTime(value: string): string {
    return new Date(value).toLocaleString('en-GB', {day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'});
}
