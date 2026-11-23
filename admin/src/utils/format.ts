export function shortTime(v?: string): string { return v ? v.slice(5, 16) : '-' }
export function isManager(role?: string): boolean { return role === 'ADMIN' || role === 'ORGANIZER' }
