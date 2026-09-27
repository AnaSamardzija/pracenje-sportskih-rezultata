// Datum sa backend-a (LocalDateTime bez zone) za prikaz, npr. „25 Sept 2026“
export function formatDate(value: string): string {
    return new Date(value).toLocaleDateString('en-GB', {day: 'numeric', month: 'short', year: 'numeric'});
}
