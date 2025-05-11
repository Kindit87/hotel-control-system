package org.kindit.hotel.data.room;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Integer> {

    Optional<Room> findByNumber(Integer number);

    // Если захочешь искать все доступные комнаты
    List<Room> findByIsAvailableTrue();
}
