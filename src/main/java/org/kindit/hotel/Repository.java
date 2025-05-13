package org.kindit.hotel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kindit.hotel.data.additionalService.AdditionalServiceRepository;
import org.kindit.hotel.data.booking.BookingRepository;
import org.kindit.hotel.data.room.RoomRepository;
import org.kindit.hotel.data.user.UserRepository;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Getter
public class Repository {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final BookingRepository bookingRepository;
}
