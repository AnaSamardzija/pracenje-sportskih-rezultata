export interface UserDto {
    id: string;
    username: string;
    firstName: string | null;
    lastName: string | null;
    email: string;
    roles: string[];
    active: boolean;
}
