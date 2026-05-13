package com.allobank.test.dto.billing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingCreateRequest {
    @NotBlank(message = "Group name is required")
    @Size(max = 100)
    private String name;

    @NotEmpty(message = "At least one participant is required")
    @Size(min = 2, message = "A group requires at least 2 participants")
    private List<@NotBlank(message = "Participant name must not be blank") String> participants;
}
