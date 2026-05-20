package com.tinypay.auth.google;

import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoogleIdTokenVerifier {

    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    @Value("${google.client-id}")
    private String clientId;

    private final RestTemplate restTemplate;

    public GoogleUserInfo verify(String idToken) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> payload = restTemplate.getForObject(TOKEN_INFO_URL + idToken, Map.class);

            if (payload == null || !clientId.equals(payload.get("aud"))) {
                throw new CustomException(ErrorType.INVALID_ID_TOKEN);
            }

            GoogleUserInfo userInfo = new GoogleUserInfo();
            userInfo.setSub(payload.get("sub"));
            userInfo.setEmail(payload.get("email"));
            userInfo.setName(payload.get("name"));
            userInfo.setPicture(payload.get("picture"));
            return userInfo;

        } catch (HttpClientErrorException e) {
            throw new CustomException(ErrorType.INVALID_ID_TOKEN);
        } catch (ResourceAccessException e) {
            throw new CustomException(ErrorType.INTERNAL_SERVER_ERROR);
        }
    }
}