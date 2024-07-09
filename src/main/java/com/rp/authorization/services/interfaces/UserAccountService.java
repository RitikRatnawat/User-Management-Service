package com.rp.authorization.services.interfaces;

import com.rp.authorization.exceptions.ResourceNotFoundException;
import com.rp.authorization.models.entities.UserAccount;
import com.rp.authorization.models.entities.User;

import java.util.List;

public interface UserAccountService {
    UserAccount save(User user, String token);

    List<UserAccount> findAll();

    void delete(String id);

    UserAccount findByToken(String token) throws ResourceNotFoundException;

    UserAccount findById(String id) throws ResourceNotFoundException;
}
