package com.sandzaksell.sandzaksell.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPublicDTO {
    private Long id;
    private String username;
    private String profileImageUrl;
    private String phone;
}