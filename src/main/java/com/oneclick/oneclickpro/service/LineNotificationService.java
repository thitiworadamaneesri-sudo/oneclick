package com.oneclick.oneclickpro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class LineNotificationService {

    @Value("${line.channel-access-token}")
    private String channelAccessToken;

    @Value("${line.group-id}")
    private String groupId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendText(String text) {
        String url = "https://api.line.me/v2/bot/message/push";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(
    java.util.Objects.requireNonNull(channelAccessToken, "LINE token is null")
);

        Map<String, Object> body = Map.of(
                "to", groupId,
                "messages", List.of(
                        Map.of(
                                "type", "text",
                                "text", text
                        )
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }
}