package org.kindit.hotel.endpoits.booking.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MyBookingRequest {
    private Integer roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private List<Integer> additionalServiceIds;
}
