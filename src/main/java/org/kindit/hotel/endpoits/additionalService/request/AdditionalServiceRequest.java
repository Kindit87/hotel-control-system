package org.kindit.hotel.endpoits.additionalService.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalServiceRequest {
    private String name;
    private String description;
    private Integer price;
}
