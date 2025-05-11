package org.kindit.hotel.endpoits.room.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RoomRequest {
    private Integer number;
    private Integer pricePerNight;
    private Integer capacity;
    private String description;
    private MultipartFile image;
    private Boolean isAvailable;
}