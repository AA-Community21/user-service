package com.tcket;


import com.tcket.interfaces.IUserService;
import com.tcket.model.UserRegister;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Inject
    private IUserService userService;

    @PostMapping("/register")
    public UserRegister register(@RequestBody UserRegister user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestParam String username, @RequestParam String password) {
        try {
            String token = userService.login(username, password);
            return new AuthResponse(token, username);
        } catch (RuntimeException e) {
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/updateProfile")
    public UserRegister updateProfile(@RequestBody UserRegister user) {
        return userService.updateProfile(user);
    }

    @PostMapping("/deleteUser")
    public void deleteUser(@RequestParam String id) {
        userService.deleteUserDetails(id);
    }
}