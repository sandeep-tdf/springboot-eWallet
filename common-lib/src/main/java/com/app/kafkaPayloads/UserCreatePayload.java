package com.app.kafkaPayloads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatePayload {

    private Long userId;
    private String userName;
    private String email;
    private String requestId;
}
