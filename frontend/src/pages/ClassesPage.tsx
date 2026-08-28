import { useQuery } from '@tanstack/react-query';
import { getAllGymClasses } from '../services/gymClassService';
import { getAllBookings } from '../services/bookingService';
import type { BookingStatus } from '../types/api';

export function ClassesPage() {
  const { data: classes, isLoading: classesLoading } = useQuery({
    queryKey: ['gym-classes'],
    queryFn: getAllGymClasses,
  });

  const { data: bookings, isLoading: bookingsLoading } = useQuery({
    queryKey: ['bookings'],
    queryFn: getAllBookings,
  });

  const isActiveBooking = (status: BookingStatus) =>
    status === 'CONFIRMED' || status === 'PENDING';

  const getBookedCount = (classUuid: string) => {
    if (!bookings) return 0;
    return bookings.filter(
      (b) => b.gymClassUuid === classUuid && isActiveBooking(b.status)
    ).length;
  };

  if (classesLoading || bookingsLoading) {
    return (
      <div className="main-content">
        <div className="loading">Loading classes...</div>
      </div>
    );
  }

  if (!classes || classes.length === 0) {
    return (
      <div className="main-content">
        <h1 style={{ marginBottom: '1rem' }}>Browse Classes</h1>
        <div className="empty-state">
          <p>No gym classes available yet.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="main-content">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 600, marginBottom: '0.5rem' }}>
          Browse Classes
        </h1>
        <p className="text-muted" style={{ fontSize: '0.875rem' }}>
          Explore all available gym classes
        </p>
      </div>

      <div className="grid grid-2">
        {classes.map((gymClass) => {
          const booked = getBookedCount(gymClass.uuid);
          const isFull = booked >= gymClass.capacity;
          const availableSpots = gymClass.capacity - booked;

          return (
            <div key={gymClass.uuid} className="card">
              <div className="flex-between" style={{ marginBottom: '0.625rem' }}>
                <h3 className="card-title" style={{ margin: 0, fontSize: '1.0625rem' }}>
                  {gymClass.name}
                </h3>
                <span
                  className={`badge ${isFull ? 'badge-error' : 'badge-secondary'}`}
                  style={{ flexShrink: 0 }}
                >
                  {booked}/{gymClass.capacity}
                </span>
              </div>

              <div style={{ fontSize: '0.8125rem', color: 'var(--color-muted)', lineHeight: 1.5 }}>
                <div style={{ marginBottom: '0.25rem' }}>
                  <strong style={{ color: 'var(--color-fg)' }}>{gymClass.trainerName}</strong>
                  {' · '}
                  <span>{gymClass.trainerSpecialty}</span>
                </div>
                <div style={{ marginBottom: '0.25rem' }}>
                  {new Date(gymClass.dateTime).toLocaleDateString([], {
                    weekday: 'short',
                    month: 'short',
                    day: 'numeric',
                  })}
                  {' · '}
                  {new Date(gymClass.dateTime).toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </div>
                <div style={{ color: isFull ? 'var(--color-error)' : 'var(--color-success)', fontWeight: 500 }}>
                  {isFull ? 'Class is full' : `${availableSpots} spots available`}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
