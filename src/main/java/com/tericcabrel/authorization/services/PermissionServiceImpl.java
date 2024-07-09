package com.rp.authorization.services;

import com.rp.authorization.models.entities.Permission;
import com.rp.authorization.repositories.PermissionRepository;
import com.rp.authorization.services.interfaces.PermissionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<Permission> findAll() {
        List<Permission> list = new ArrayList<>();
        permissionRepository.findAll().iterator().forEachRemaining(list::add);

        return list;
    }

    @Override
    public Optional<Permission> findByName(String name) {
        return permissionRepository.findByName(name);
    }

    @Override
    public Optional<Permission> findById(String id) {
        return permissionRepository.findById(new ObjectId(id));
    }
}
