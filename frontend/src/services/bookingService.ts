/**
 * Booking API service.
 * All endpoints verified from backend BookingController.
 */

import { api } from '../lib/api';
import type { BookingReadOnlyDTO, BookingInsertDTO } from '../types/api';

/**
 * GET /api/bookings
 * Retrieves all bookings.
 */
export async function getAllBookings(): Promise<BookingReadOnlyDTO[]> {
  const response = await api.get<BookingReadOnlyDTO[]>('/bookings');
  return response.data;
}

/**
 * GET /api/bookings/member/{memberUuid}
 * Retrieves all bookings for a specific member.
 */
export async function getBookingsByMember(memberUuid: string): Promise<BookingReadOnlyDTO[]> {
  const response = await api.get<BookingReadOnlyDTO[]>(`/bookings/member/${memberUuid}`);
  return response.data;
}

/**
 * GET /api/bookings/{uuid}
 * Retrieves a single booking by UUID.
 */
export async function getBookingByUuid(uuid: string): Promise<BookingReadOnlyDTO> {
  const response = await api.get<BookingReadOnlyDTO>(`/bookings/${uuid}`);
  return response.data;
}

/**
 * POST /api/bookings
 * Creates a new booking.
 */
export async function createBooking(data: BookingInsertDTO): Promise<BookingReadOnlyDTO> {
  const response = await api.post<BookingReadOnlyDTO>('/bookings', data);
  return response.data;
}

/**
 * DELETE /api/bookings/{uuid}
 * Deletes a booking (cancels it).
 */
export async function deleteBooking(uuid: string): Promise<void> {
  await api.delete(`/bookings/${uuid}`);
}
