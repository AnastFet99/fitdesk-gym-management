/**
 * Gym Class API service.
 * All endpoints verified from backend GymClassController.
 */

import { api } from '../lib/api';
import type { GymClassReadOnlyDTO, GymClassInsertDTO } from '../types/api';

/**
 * GET /api/gym-classes
 * Retrieves all gym classes.
 */
export async function getAllGymClasses(): Promise<GymClassReadOnlyDTO[]> {
  const response = await api.get<GymClassReadOnlyDTO[]>('/gym-classes');
  return response.data;
}

/**
 * GET /api/gym-classes/{uuid}
 * Retrieves a single gym class by UUID.
 */
export async function getGymClassByUuid(uuid: string): Promise<GymClassReadOnlyDTO> {
  const response = await api.get<GymClassReadOnlyDTO>(`/gym-classes/${uuid}`);
  return response.data;
}

/**
 * POST /api/gym-classes
 * Creates a new gym class.
 */
export async function createGymClass(data: GymClassInsertDTO): Promise<GymClassReadOnlyDTO> {
  const response = await api.post<GymClassReadOnlyDTO>('/gym-classes', data);
  return response.data;
}

/**
 * DELETE /api/gym-classes/{uuid}
 * Deletes a gym class.
 */
export async function deleteGymClass(uuid: string): Promise<void> {
  await api.delete(`/gym-classes/${uuid}`);
}
