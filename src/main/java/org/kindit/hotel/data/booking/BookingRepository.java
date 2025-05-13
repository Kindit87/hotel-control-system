package org.kindit.hotel.data.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByRoomId(Integer id);
    List<Booking> findByUserId(Integer id);
    Optional<Booking> findByIdAndUserId(Integer id, Integer userId);
}
