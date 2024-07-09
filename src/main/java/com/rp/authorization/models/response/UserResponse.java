package com.rp.authorization.models.response;

import com.rp.authorization.models.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class UserResponse {
    private User data;
}
