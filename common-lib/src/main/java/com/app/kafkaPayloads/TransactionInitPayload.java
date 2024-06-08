package com.app.kafkaPayloads;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionInitPayload {

    private Long id;

    private Long fromId;

    private Long toId;

    private Double amount;

    private Double remark;

    private String requestId;
}
