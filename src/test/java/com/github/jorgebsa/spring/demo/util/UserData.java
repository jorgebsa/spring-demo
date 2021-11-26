package com.github.jorgebsa.spring.demo.util;

import java.util.List;

import static com.github.jorgebsa.spring.demo.security.WebSecurityConfig.ADMIN_ROLE;
import static com.github.jorgebsa.spring.demo.security.WebSecurityConfig.USER_ROLE;

public record UserData(String username, String password, List<String> roles) {

    public static final UserData ADMIN = new UserData("the-admin", "123Admin", List.of(ADMIN_ROLE, USER_ROLE));
    public static final UserData SOME_USER = new UserData("some-user", "password", List.of(USER_ROLE));

}
