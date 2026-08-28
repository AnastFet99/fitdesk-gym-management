/**
 * Member API service.
 * All endpoints verified from backend MemberController.
 */

import { api } from '../lib/api';
import type { MemberReadOnlyDTO, MemberInsertDTO } from '../types/api';

/**
 * GET /api/members
 * Retrieves all members.
 */
export async function getAllMembers(): Promise<MemberReadOnlyDTO[]> {
  const response = await api.get<MemberReadOnlyDTO[]>('/members');
  return response.data;
}

/**
 * GET /api/members/{uuid}
 * Retrieves a single member by UUID.
 */
export async function getMemberByUuid(uuid: string): Promise<MemberReadOnlyDTO> {
  const response = await api.get<MemberReadOnlyDTO>(`/members/${uuid}`);
  return response.data;
}

/**
 * POST /api/members
 * Creates a new member profile.
 */
export async function createMember(data: MemberInsertDTO): Promise<MemberReadOnlyDTO> {
  const response = await api.post<MemberReadOnlyDTO>('/members', data);
  return response.data;
}

/**
 * Helper: Find member by userUuid
 */
export async function getMemberByUserUuid(userUuid: string): Promise<MemberReadOnlyDTO | null> {
  const members = await getAllMembers();
  return members.find((m) => m.userUuid === userUuid) || null;
}
