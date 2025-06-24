package org.kindit.hotel.endpoits.room.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomRequest {
    private Integer number;
    private String name;
    private Integer pricePerNight;
    private Integer capacity;
    private String description;
    private MultipartFile image;
    private List<Integer> additionalServiceIds;
}