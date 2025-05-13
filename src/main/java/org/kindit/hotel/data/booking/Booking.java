package org.kindit.hotel.data.booking;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kindit.hotel.data.additionalService.AdditionalService;
import org.kindit.hotel.data.room.Room;
import org.kindit.hotel.data.user.User;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Room room;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    @ManyToMany
    @JoinTable(
            name = "booking_additional_services",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<AdditionalService> additionalServices;

    private Integer totalPrice;
}
