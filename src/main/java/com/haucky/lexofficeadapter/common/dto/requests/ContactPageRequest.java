package com.haucky.lexofficeadapter.common.dto.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ContactPageRequest {
    
    @PositiveOrZero
    private Integer page = 0;
    
    @Min(1)
    @Max(250)
    private Integer size = 25;
}