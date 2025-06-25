package org.kindit.hotel.endpoits.booking;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.kindit.hotel.data.additionalService.AdditionalService;
import org.kindit.hotel.data.booking.Booking;
import org.kindit.hotel.data.booking.BookingStatus;
import org.kindit.hotel.data.room.Room;
import org.kindit.hotel.data.user.User;
import org.kindit.hotel.endpoits.ServiceController;
import org.kindit.hotel.endpoits.booking.request.BookingRequest;
import org.kindit.hotel.endpoits.booking.request.MyBookingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService extends ServiceController {

    public Page<Booking> getAll(int page, int size, String status, Long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("checkInDate").descending());

        if (status != null && userId != null) {
            return repository.getBookingRepository()
                    .findByStatusAndUserId(BookingStatus.valueOf(status), userId, pageable);
        } else if (status != null) {
            return repository.getBookingRepository()
                    .findByStatus(BookingStatus.valueOf(status), pageable);
        } else if (userId != null) {
            return repository.getBookingRepository()
                    .findByUserId(userId, pageable);
        } else {
            return repository.getBookingRepository().findAll(pageable);
        }
    }


    public Optional<Booking> get(Integer id) {
        return repository.getBookingRepository().findById(id);
    }

    public Page<Booking> getAllMy(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        Long userId = Long.valueOf(getAuthentifactedUser().getId());

        if (status != null) {
            return repository.getBookingRepository().findByStatusAndUserId(
                    BookingStatus.valueOf(status),
                    userId,
                    pageable
            );
        } else {
            return repository.getBookingRepository().findByUserId(userId, pageable);
        }
    }

    public Optional<Booking> getMy(Integer id) {
        return repository.getBookingRepository().findByIdAndUserId(id, getAuthentifactedUser().getId());
    }

    public Optional<Booking> create(BookingRequest request) {
        LocalDate today = LocalDate.now();

        if (request.getCheckInDate().isBefore(today) || request.getCheckOutDate().isBefore(today)) {
            throw new IllegalArgumentException("Cannot book for past dates");
        }

        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("The departure date must be at least 1 day after the arrival date");
        }

        User user = repository.getUserRepository().findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Room room = repository.getRoomRepository().findById(request.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN
        );

        List<Booking> existingBookings = repository.getBookingRepository()
                .findByRoomIdAndStatusIn(room.getId(), activeStatuses);

        boolean isOverlapping = existingBookings.stream().anyMatch(b ->
                !request.getCheckInDate().isAfter(b.getCheckOutDate()) &&
                        !request.getCheckOutDate().isBefore(b.getCheckInDate())
        );

        if (isOverlapping) {
            throw new IllegalStateException("Room is already booked for the selected dates");
        }

        List<AdditionalService> services = repository.getAdditionalServiceRepository()
                .findAllById(request.getAdditionalServiceIds());

        int totalPrice = room.getPricePerNight() *
                (int) ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());

        int additionalServicesPrice = services.stream()
                .mapToInt(AdditionalService::getPrice)
                .sum();

        Booking booking = Booking.builder()
                .user(user)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .additionalServices(services)
                .status(BookingStatus.PENDING)
                .totalPrice(totalPrice + additionalServicesPrice)
                .build();

        return Optional.of(repository.getBookingRepository().save(booking));
    }

    public Optional<Booking> createMy(MyBookingRequest request) {
        LocalDate today = LocalDate.now();

        if (request.getCheckInDate().isBefore(today) || request.getCheckOutDate().isBefore(today)) {
            throw new IllegalArgumentException("Cannot book for past dates");
        }

        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new IllegalArgumentException("The departure date must be at least 1 day after the arrival date.");
        }

        User thisUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Room room = repository.getRoomRepository().findById(request.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN
        );

        List<Booking> existingBookings = repository.getBookingRepository()
                .findByRoomIdAndStatusIn(room.getId(), activeStatuses);

        boolean isOverlapping = existingBookings.stream().anyMatch(b ->
                !request.getCheckInDate().isAfter(b.getCheckOutDate()) &&
                        !request.getCheckOutDate().isBefore(b.getCheckInDate())
        );

        if (isOverlapping) {
            throw new IllegalStateException("Room is already booked for the selected dates");
        }

        List<AdditionalService> services = repository.getAdditionalServiceRepository()
                .findAllById(request.getAdditionalServiceIds());

        int totalPrice = room.getPricePerNight() *
                (int) ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());

        int additionalServicesPrice = services.stream()
                .mapToInt(AdditionalService::getPrice)
                .sum();

        Booking booking = Booking.builder()
                .user(thisUser)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .additionalServices(services)
                .status(BookingStatus.PENDING)
                .totalPrice(totalPrice + additionalServicesPrice)
                .build();

        return Optional.of(repository.getBookingRepository().save(booking));
    }

    public Optional<Booking> refresh(Integer id, BookingRequest request) {
        User user = repository.getUserRepository().findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Room room = repository.getRoomRepository().findById(request.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        List<Booking> existingBookings = repository.getBookingRepository()
                .findByRoomId(room.getId());

        boolean isOverlapping = existingBookings.stream().anyMatch(b ->
                ( !request.getCheckInDate().isAfter(b.getCheckOutDate()) &&
                        !request.getCheckOutDate().isBefore(b.getCheckInDate()) )
        );

        if (isOverlapping) {
            throw new IllegalStateException("Room is already booked for the selected dates");
        }

        room.setAvailable(false);
        repository.getRoomRepository().save(room);

        List<AdditionalService> services = repository.getAdditionalServiceRepository()
                .findAllById(request.getAdditionalServiceIds());

        int totalPrice = room.getPricePerNight() *
                (int) ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());

        int additionalServicesPrice = services.stream()
                .mapToInt(AdditionalService::getPrice)
                .sum();

        return repository.getBookingRepository().findById(id).map(booking -> {
            booking.getRoom().setAvailable(true);
            repository.getRoomRepository().save(booking.getRoom());

            booking.setUser(user);
            booking.setRoom(room);
            booking.setCheckInDate(request.getCheckInDate());
            booking.setCheckOutDate(request.getCheckOutDate());
            booking.setAdditionalServices(services);
            booking.setStatus(request.getStatus() != null ? request.getStatus() : BookingStatus.PENDING);
            booking.setTotalPrice(totalPrice + additionalServicesPrice);

            return repository.getBookingRepository().save(booking);
        });
    }

    public Optional<Booking> update(Integer id, BookingRequest request) {
        return repository.getBookingRepository().findById(id).map(booking -> {

            if (request.getUserId() != null) {
                User user = repository.getUserRepository().findById(request.getUserId())
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));
                booking.setUser(user);
            }

            if (request.getRoomId() != null) {
                Room newRoom = repository.getRoomRepository().findById(request.getRoomId())
                        .orElseThrow(() -> new EntityNotFoundException("Room not found"));

                List<Booking> existingBookings = repository.getBookingRepository()
                        .findByRoomId(newRoom.getId());

                boolean isOverlapping = existingBookings.stream().anyMatch(b ->
                        ( !request.getCheckInDate().isAfter(b.getCheckOutDate()) &&
                                !request.getCheckOutDate().isBefore(b.getCheckInDate()) )
                );

                if (isOverlapping) {
                    throw new IllegalStateException("Room is already booked for the selected dates");
                }

                Room currentRoom = booking.getRoom();
                if (currentRoom != null && !currentRoom.getId().equals(newRoom.getId())) {
                    currentRoom.setAvailable(true);
                    repository.getRoomRepository().save(currentRoom);

                    newRoom.setAvailable(false);
                    repository.getRoomRepository().save(newRoom);

                    booking.setRoom(newRoom);
                }
            }

            if (request.getAdditionalServiceIds() != null) {
                List<AdditionalService> services = repository.getAdditionalServiceRepository()
                        .findAllById(request.getAdditionalServiceIds());
                booking.setAdditionalServices(services);
            }

            if (request.getCheckInDate() != null) booking.setCheckInDate(request.getCheckInDate());
            if (request.getCheckOutDate() != null) booking.setCheckOutDate(request.getCheckOutDate());

            if (booking.getRoom() != null && booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
                int totalPrice = booking.getRoom().getPricePerNight() *
                        (int) ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());

                int additionalServicesPrice = booking.getAdditionalServices().stream()
                        .mapToInt(AdditionalService::getPrice)
                        .sum();

                booking.setTotalPrice(totalPrice + additionalServicesPrice);
            }

            if (request.getStatus() != null) {
                booking.setStatus(request.getStatus());
            }

            return repository.getBookingRepository().save(booking);
        });
    }

    public Optional<Booking> cancel(Integer bookingId) {
        return repository.getBookingRepository().findById(bookingId).map(booking -> {
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                return booking; // уже отменено
            }
            booking.setStatus(BookingStatus.CANCELLED);
            booking.getRoom().setAvailable(true);
            repository.getRoomRepository().save(booking.getRoom());
            return repository.getBookingRepository().save(booking);
        });
    }

    public Optional<Booking> cancelMy(Integer bookingId) {
        User currentUser = getAuthentifactedUser();

        return repository.getBookingRepository()
                .findByIdAndUserId(bookingId, currentUser.getId())
                .map(booking -> {
                    if (booking.getStatus() == BookingStatus.CANCELLED) {
                        return booking; // уже отменено
                    }
                    booking.setStatus(BookingStatus.CANCELLED);
                    booking.getRoom().setAvailable(true);
                    repository.getRoomRepository().save(booking.getRoom());
                    return repository.getBookingRepository().save(booking);
                });
    }

    public Optional<Booking> pay(Integer bookingId) {
        User user = getAuthentifactedUser();

        return repository.getBookingRepository().findById(bookingId)
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .map(booking -> {
                    if (booking.getStatus() == BookingStatus.PENDING) {
                        booking.setStatus(BookingStatus.CONFIRMED);
                        return repository.getBookingRepository().save(booking);
                    }
                    return booking; // не меняем, если уже оплачен или отменён
                });
    }

    public void delete(Integer id) {
        Booking booking = repository.getBookingRepository().findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        booking.getRoom().setAvailable(true);
        repository.getRoomRepository().save(booking.getRoom());

        repository.getBookingRepository().delete(booking);
    }
}
