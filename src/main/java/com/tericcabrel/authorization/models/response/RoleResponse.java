package com.rp.authorization.models.response;

import com.rp.authorization.models.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class RoleResponse {
    private Role data;
}
