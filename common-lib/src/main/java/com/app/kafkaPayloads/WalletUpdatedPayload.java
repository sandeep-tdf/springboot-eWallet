package com.app.kafkaPayloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletUpdatedPayload {

    private String userEmail;
    private String userName;
    private Double balance;
    private String requestId;
}
