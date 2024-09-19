package com.group10.koiauction.entity.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginAccountRequest {
    private String username;
    private String password;
}
