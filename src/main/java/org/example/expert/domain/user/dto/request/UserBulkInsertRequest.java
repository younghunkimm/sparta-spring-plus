package org.example.expert.domain.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class UserBulkInsertRequest {

    @Min(1)
    @Max(5_000_000)
    private int count;
}
