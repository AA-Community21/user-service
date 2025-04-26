package com.tcket.interfaces;

import com.tcket.model.UserRegister;

public interface IUserService {
    String login(String username, String password);

    UserRegister register(UserRegister user);

    UserRegister updateProfile(UserRegister user);

    void deleteUserDetails(String id);
}
