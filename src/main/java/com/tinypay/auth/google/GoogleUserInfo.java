package com.tinypay.auth.google;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleUserInfo {
    private String sub;
    private String email;
    private String name;
    private String picture;
}