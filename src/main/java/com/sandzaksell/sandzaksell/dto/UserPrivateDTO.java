package com.sandzaksell.sandzaksell.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrivateDTO {
    private Long id;
    private String username;
    private String email;
    private String profileImageUrl;
    private String phone;
    private Integer tokenBalance;
    private Boolean enabled;
}