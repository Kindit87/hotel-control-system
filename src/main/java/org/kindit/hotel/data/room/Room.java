package org.kindit.hotel.data.room;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kindit.hotel.data.additionalService.AdditionalService;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer number;
    private String name;
    private Integer capacity;
    private Integer pricePerNight;
    private String description;
    private String imagePath;

    @Builder.Default
    private boolean isAvailable = true;

    @ManyToMany
    @JoinTable(
            name = "room_additional_service",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<AdditionalService> additionalServices;
}
