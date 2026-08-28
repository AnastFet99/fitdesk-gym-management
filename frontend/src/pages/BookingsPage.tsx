import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../contexts/AuthContext';
import { getAllGymClasses } from '../services/gymClassService';
import { getBookingsByMember, createBooking, deleteBooking } from '../services/bookingService';
import { getMemberByUserUuid, createMember } from '../services/memberService';
import { handleApiError } from '../lib/api';
import type { BookingStatus, BookingReadOnlyDTO } from '../types/api';
import { useState } from 'react';

const PHONE_PATTERN = /^[+]?[0-9]{10,15}$/;

export function BookingsPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');
  const [showProfileForm, setShowProfileForm] = useState(false);
  const [phone, setPhone] = useState('');
  const [phoneError, setPhoneError] = useState('');
  const [profileError, setProfileError] = useState('');
  const [isCreatingProfile, setIsCreatingProfile] = useState(false);

  // Get member profile for current user
  const { data: member, isLoading: memberLoading } = useQuery({
    queryKey: ['member', user?.uuid],
    queryFn: () => getMemberByUserUuid(user!.uuid),
    enabled: !!user,
  });

  // Get all gym classes
  const { data: classes, isLoading: classesLoading } = useQuery({
    queryKey: ['gym-classes'],
    queryFn: getAllGymClasses,
  });

  // Get bookings for this member
  const { data: bookings, isLoading: bookingsLoading } = useQuery({
    queryKey: ['member-bookings', member?.uuid],
    queryFn: () => getBookingsByMember(member!.uuid),
    enabled: !!member,
  });

  // Create booking mutation
  const bookMutation = useMutation({
    mutationFn: (gymClassUuid: string) => {
      if (!member) throw new Error('Member profile required');
      return createBooking({
        memberUuid: member.uuid,
        gymClassUuid,
        status: 'CONFIRMED',
      });
    },
    onSuccess: () => {
      setSuccess('Class booked successfully!');
      setError('');
      queryClient.invalidateQueries({ queryKey: ['member-bookings'] });
      queryClient.invalidateQueries({ queryKey: ['bookings'] });
    },
    onError: (err) => {
      const apiError = handleApiError(err);
      setError(apiError.message);
      setSuccess('');
    },
  });

  // Cancel booking mutation
  const cancelMutation = useMutation({
    mutationFn: (bookingUuid: string) => deleteBooking(bookingUuid),
    onSuccess: () => {
      setSuccess('Booking cancelled successfully');
      setError('');
      queryClient.invalidateQueries({ queryKey: ['member-bookings'] });
      queryClient.invalidateQueries({ queryKey: ['bookings'] });
    },
    onError: (err) => {
      const apiError = handleApiError(err);
      setError(apiError.message);
      setSuccess('');
    },
  });

  const isActiveBooking = (status: BookingStatus) =>
    status === 'CONFIRMED' || status === 'PENDING';

  // Find active booking for a specific gym class
  const getActiveBookingForClass = (gymClassUuid: string): BookingReadOnlyDTO | undefined => {
    if (!bookings) return undefined;
    return bookings.find(
      (b) => b.gymClassUuid === gymClassUuid && isActiveBooking(b.status)
    );
  };

  const handleBook = (gymClassUuid: string) => {
    setError('');
    setSuccess('');
    bookMutation.mutate(gymClassUuid);
  };

  const handleCancel = (gymClassUuid: string) => {
    const booking = getActiveBookingForClass(gymClassUuid);
    if (!booking) {
      setError('Booking not found');
      return;
    }

    if (confirm('Are you sure you want to cancel this booking?')) {
      setError('');
      setSuccess('');
      cancelMutation.mutate(booking.uuid);
    }
  };

  const validatePhone = (value: string): string => {
    const trimmed = value.trim();
    if (!trimmed) return 'Phone number is required';
    if (!PHONE_PATTERN.test(trimmed)) {
      return 'Enter a valid phone number (10–15 digits, optional + prefix)';
    }
    return '';
  };

  const handleCreateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    setProfileError('');
    const phoneValidation = validatePhone(phone);
    setPhoneError(phoneValidation);
    if (phoneValidation) return;

    setIsCreatingProfile(true);
    try {
      await createMember({
        userUuid: user.uuid,
        phone: phone.trim(),
      });
      await queryClient.invalidateQueries({ queryKey: ['member', user.uuid] });
      setShowProfileForm(false);
      setPhone('');
      setSuccess('Member profile created successfully. You can now book classes.');
      setError('');
    } catch (err) {
      const apiError = handleApiError(err);
      if (apiError.status === 409) {
        setProfileError('A member profile already exists for this account.');
        await queryClient.invalidateQueries({ queryKey: ['member', user.uuid] });
      } else if (apiError.status === 400) {
        setProfileError(apiError.message || 'Please check your phone number and try again.');
      } else if (apiError.status === 0) {
        setProfileError('Unable to reach the server. Please check that the backend is running.');
      } else {
        setProfileError(apiError.message || 'Failed to create member profile. Please try again.');
      }
    } finally {
      setIsCreatingProfile(false);
    }
  };

  if (memberLoading || classesLoading || bookingsLoading) {
    return (
      <div className="main-content">
        <div className="loading">Loading bookings...</div>
      </div>
    );
  }

  if (!member) {
    return (
      <div className="main-content">
        <div style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 600, marginBottom: '0.5rem' }}>
            My Bookings
          </h1>
          <p className="text-muted" style={{ fontSize: '0.875rem' }}>
            Book a spot or cancel your reservation
          </p>
        </div>

        <div className="card" style={{ maxWidth: '480px' }}>
          <h2 className="card-title">Member Profile Required</h2>
          <p className="card-subtitle" style={{ marginBottom: '1rem' }}>
            You need a member profile before you can book gym classes.
          </p>

          {profileError && <div className="error-message">{profileError}</div>}

          {!showProfileForm ? (
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => {
                setShowProfileForm(true);
                setProfileError('');
                setPhoneError('');
              }}
            >
              Create Member Profile
            </button>
          ) : (
            <form onSubmit={handleCreateProfile}>
              <div className="form-group">
                <label className="form-label" htmlFor="phone">
                  Phone Number
                </label>
                <input
                  id="phone"
                  type="tel"
                  className="form-input"
                  placeholder="e.g. 6912345678"
                  value={phone}
                  onChange={(e) => {
                    setPhone(e.target.value);
                    if (phoneError) setPhoneError('');
                  }}
                  autoComplete="tel"
                />
                {phoneError && <p className="form-error">{phoneError}</p>}
                <p style={{ fontSize: '0.75rem', color: 'var(--color-muted)', marginTop: '0.375rem' }}>
                  10–15 digits. You may include a leading +.
                </p>
              </div>

              <div style={{ display: 'flex', gap: '0.75rem' }}>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={isCreatingProfile}
                >
                  {isCreatingProfile ? 'Creating...' : 'Save Profile'}
                </button>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => {
                    setShowProfileForm(false);
                    setPhone('');
                    setPhoneError('');
                    setProfileError('');
                  }}
                  disabled={isCreatingProfile}
                >
                  Cancel
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    );
  }

  if (!classes || classes.length === 0) {
    return (
      <div className="main-content">
        <h1 style={{ marginBottom: '1rem' }}>My Bookings</h1>
        <div className="empty-state">
          <p>No gym classes available to book.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="main-content">
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 600, marginBottom: '0.5rem' }}>
          My Bookings
        </h1>
        <p className="text-muted" style={{ fontSize: '0.875rem' }}>
          Book a spot or cancel your reservation
        </p>
      </div>

      {error && <div className="error-message">{error}</div>}
      {success && (
        <div style={{
          background: '#c6f6d5',
          color: '#22543d',
          padding: '0.875rem 1rem',
          borderRadius: 'var(--radius)',
          marginBottom: '1.5rem',
          fontSize: '0.875rem',
          fontWeight: 500,
        }}>
          {success}
        </div>
      )}

      <div className="grid grid-2">
        {classes.map((gymClass) => {
          const activeBooking = getActiveBookingForClass(gymClass.uuid);
          const isBooked = !!activeBooking;

          return (
            <div key={gymClass.uuid} className="card">
              <div style={{ marginBottom: '0.625rem' }}>
                <div className="flex-between" style={{ alignItems: 'flex-start', marginBottom: '0.25rem' }}>
                  <h3 className="card-title" style={{ margin: 0, fontSize: '1.0625rem' }}>
                    {gymClass.name}
                  </h3>
                  <span
                    className={`badge ${isBooked ? 'badge-default' : 'badge-secondary'}`}
                    style={{ flexShrink: 0 }}
                  >
                    {isBooked ? 'Booked' : 'Available'}
                  </span>
                </div>
                <div style={{ fontSize: '0.8125rem', color: 'var(--color-muted)' }}>
                  {gymClass.trainerName}
                </div>
              </div>

              <div className="flex-between" style={{ gap: '1rem', alignItems: 'flex-end' }}>
                <div style={{ fontSize: '0.8125rem', color: 'var(--color-muted)', lineHeight: 1.5 }}>
                  <div>
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
                  {activeBooking && (
                    <div style={{ marginTop: '0.375rem', fontSize: '0.75rem', opacity: 0.7 }}>
                      Booked {new Date(activeBooking.createdAt).toLocaleDateString()}
                    </div>
                  )}
                </div>

                {isBooked ? (
                  <button
                    className="btn btn-secondary"
                    onClick={() => handleCancel(gymClass.uuid)}
                    disabled={cancelMutation.isPending}
                    style={{ flexShrink: 0 }}
                  >
                    {cancelMutation.isPending ? 'Cancelling...' : 'Cancel'}
                  </button>
                ) : (
                  <button
                    className="btn btn-primary"
                    onClick={() => handleBook(gymClass.uuid)}
                    disabled={bookMutation.isPending}
                    style={{ flexShrink: 0 }}
                  >
                    {bookMutation.isPending ? 'Booking...' : 'Book'}
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
