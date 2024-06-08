package com.app.service;

import com.app.kafkaPayloads.UserCreatePayload;
import com.app.dto.UserDTO;
import com.app.entity.User;
import com.app.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private static final String USER_CREATED_TOPIC = "USER-CREATED";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private UserRepository userRepository;


    public Long createUser(UserDTO userDTO) throws ExecutionException, InterruptedException {

        User user = User.builder()
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .phone(userDTO.getPhone())
                .kycId(userDTO.getKycId())
                .address(userDTO.getAddress())
                .build();

        userRepository.save(user);

        UserCreatePayload userCreatePayload = new UserCreatePayload(user.getId(), user.getName(), user.getEmail(), MDC.get("requestId"));

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(USER_CREATED_TOPIC, String.valueOf(user.getId()), userCreatePayload);

        LOGGER.info("Pushed UserCratePayload to Kafka : {}", future.get());

        return user.getId();
    }


}
