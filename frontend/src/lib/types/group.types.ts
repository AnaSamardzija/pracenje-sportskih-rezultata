// Vrednosti su tačno enum GroupRole sa backend-a
export type GroupRole = 'MEMBER' | 'GROUP_ADMIN';

// memberCount i myRole backend računa za prijavljenog korisnika; myRole je null ako nije član
export interface GroupResponse {
    id: string;
    name: string;
    description: string | null;
    createdBy: string;
    createdAt: string;
    memberCount: number;
    myRole: GroupRole | null;
}

// Prazan opis se šalje kao null
export interface GroupRequest {
    name: string;
    description: string | null;
}

// active = false znači da je nalog deaktiviran; takav član ostaje u listi, ali ne može u nove mečeve
export interface MemberResponse {
    userId: string;
    username: string;
    active: boolean;
    roleInGroup: GroupRole;
    joinedAt: string;
}

export interface AddMemberRequest {
    username: string;
}

// Kratak prikaz grupe unutar drugog odgovora (npr. u meču)
export interface GroupSummaryDto {
    id: string;
    name: string;
}

export const GROUP_ROLE_LABELS: Record<GroupRole, string> = {
    MEMBER: 'Member',
    GROUP_ADMIN: 'Admin'
};
