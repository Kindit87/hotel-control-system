package org.kindit.hotel.endpoits.booking;

import lombok.RequiredArgsConstructor;
import org.kindit.hotel.data.booking.Booking;
import org.kindit.hotel.endpoits.ApiController;
import org.kindit.hotel.endpoits.booking.request.BookingRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingController extends ApiController<BookingService> {

    private final BookingService bookingService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<Booking> getBooking(@PathVariable Integer id) {
        return bookingService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me/all")
    @PreAuthorize("hasAnyRole('USER', MODERATOR', 'ADMIN')")
    public ResponseEntity<List<Booking>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getAllMy());
    }

    @GetMapping("/me/{id}")
    @PreAuthorize("hasAnyRole('USER', MODERATOR', 'ADMIN')")
    public ResponseEntity<Booking> getMyBookings(@PathVariable Integer id) {
        return bookingService.getMy(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request) {
        return bookingService.create(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/me")
    @PreAuthorize("hasAnyRole('USER', MODERATOR', 'ADMIN')")
    public ResponseEntity<Booking> createMyBooking(@RequestBody BookingRequest request) {
        return bookingService.createMy(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> refreshBooking(@PathVariable Integer id, @RequestBody BookingRequest request) {
        return bookingService.refresh(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Integer id, @RequestBody BookingRequest request) {
        return bookingService.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Integer id) {
        bookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
