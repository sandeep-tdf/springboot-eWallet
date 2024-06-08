package com.app.kafkaPayloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCompletePayload {

    private Long id;

    private Boolean success;

    private String reason;

    private String requestId;
}
