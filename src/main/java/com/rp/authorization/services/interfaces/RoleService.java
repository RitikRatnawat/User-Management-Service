package com.rp.authorization.services.interfaces;

import com.rp.authorization.exceptions.ResourceNotFoundException;
import com.rp.authorization.models.dtos.CreateRoleDto;
import com.rp.authorization.models.entities.Role;

import java.util.List;

public interface RoleService {
    Role save(CreateRoleDto role);

    List<Role> findAll();

    void delete(String id);

    Role findByName(String name) throws ResourceNotFoundException;

    Role findById(String id) throws ResourceNotFoundException;

    Role update(String id, CreateRoleDto createRoleDto) throws ResourceNotFoundException;
    Role update(Role role);
}
