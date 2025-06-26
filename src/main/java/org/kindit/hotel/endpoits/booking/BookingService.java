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
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BookingService extends ServiceController {

    public Page<Booking> getAll(
            int page, int size, String status, String email,
            String firstname, String lastname,
            LocalDate checkInFrom, LocalDate checkInTo)
    {
        List<Booking> bookings;

        if (status != null) {
            try {
                BookingStatus parsedStatus = BookingStatus.valueOf(status);
                bookings = repository.getBookingRepository().findByStatus(parsedStatus, Pageable.unpaged()).getContent();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid booking status");
            }
        } else {
            bookings = repository.getBookingRepository().findAll();
        }

        Stream<Booking> stream = bookings.stream();

        if (email != null && !email.isBlank()) {
            stream = stream.filter(b -> b.getUser().getEmail().toLowerCase().contains(email.toLowerCase()));
        }
        if (firstname != null && !firstname.isBlank()) {
            stream = stream.filter(b -> b.getUser().getFirstname().toLowerCase().contains(firstname.toLowerCase()));
        }
        if (lastname != null && !lastname.isBlank()) {
            stream = stream.filter(b -> b.getUser().getLastname().toLowerCase().contains(lastname.toLowerCase()));
        }
        if (checkInFrom != null) {
            stream = stream.filter(b -> !b.getCheckInDate().isBefore(checkInFrom));
        }
        if (checkInTo != null) {
            stream = stream.filter(b -> !b.getCheckInDate().isAfter(checkInTo));
        }

        List<Booking> filtered = stream.toList();

        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        List<Booking> paged = (start <= end) ? filtered.subList(start, end) : List.of();

        return new PageImpl<>(paged, PageRequest.of(page, size), filtered.size());
    }


    public Optional<Booking> get(Integer id) {
        return repository.getBookingRepository().findById(id);
    }

    public Page<Booking> getAllMy(int page, int size, String statusStr, LocalDate checkInFrom, LocalDate checkInTo) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("checkInDate").descending());

        Page<Booking> allBookings = repository.getBookingRepository().findByUserId(Long.valueOf(user.getId()), pageable);

        final BookingStatus status;
        try {
            status = (statusStr != null) ? BookingStatus.valueOf(statusStr) : null;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid booking status: " + statusStr);
        }

        List<Booking> filtered = allBookings.stream()
                .filter(b -> status == null || b.getStatus() == status)
                .filter(b -> checkInFrom == null || !b.getCheckInDate().isBefore(checkInFrom))
                .filter(b -> checkInTo == null || !b.getCheckInDate().isAfter(checkInTo))
                .collect(Collectors.toList());

        int start = Math.min(page * size, filtered.size());
        int end = Math.min(start + size, filtered.size());
        List<Booking> pageContent = filtered.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filtered.size());
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
