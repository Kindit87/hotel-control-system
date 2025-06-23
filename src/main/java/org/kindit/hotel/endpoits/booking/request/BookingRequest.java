package org.kindit.hotel.endpoits.booking.request;

import lombok.Data;
import org.kindit.hotel.data.booking.BookingStatus;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingRequest {
    private Integer userId;
    private Integer roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private List<Integer> additionalServiceIds;
    private BookingStatus status;
}
