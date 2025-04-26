package com.tcket.implementation;


import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcket.JwtUtils;
import com.tcket.cbbuckets.BucketEnum;
import com.tcket.errorhandling.LoginException;
import com.tcket.interfaces.IUserService;
import com.tcket.model.UserRegister;
import com.tcket.utils.CouchbaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;



public class UserService implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Inject
    private CouchbaseUtils couchbaseUtils;

    @Inject
    private JwtUtils jwtUtils;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public UserRegister register(UserRegister user) {
        try {
            validateUser(user);
            user.setId(UUID.randomUUID().toString());
            user.setUsername(user.getUsername().toLowerCase());
            user.setPassword(user.getPassword());
            user.setEmail(user.getEmail().toLowerCase());
            user.setPhone(user.getPhone().toLowerCase());
            couchbaseUtils.createBucketScopeCollectionIfNotExist(BucketEnum.USER_DETAILS);
            couchbaseUtils.insertDoc(user.getId(), objectMapper.writeValueAsString(user), BucketEnum.USER_DETAILS);
        } catch(Exception e) {
            logger.error("Error occurred during user registration", e);
        }
        logger.debug("User registered successfully: {}", user.getUsername());
        return user;
    }

    @Override
    public UserRegister updateProfile(UserRegister user) {
        UserRegister existingUser = null;
        try {
            List<JsonObject>  result = couchbaseUtils.getUsernameDetails(user.getUsername()).rowsAsObject();
            if (result == null || result.isEmpty()) {
                throw new IllegalArgumentException("User not found");
            }
            String jsonDoc = result.get(0).get(BucketEnum.USER_DETAILS.getCollection()).toString();
            existingUser = objectMapper.readValue(jsonDoc, UserRegister.class);
            if(existingUser.getId() == null) {
                throw new IllegalArgumentException("User not found");
            }
            logger.debug("Updating user profile for: {}", existingUser.getUsername());
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            existingUser.setPassword(user.getPassword());
            couchbaseUtils.insertDoc(existingUser.getId(), objectMapper.writeValueAsString(existingUser), BucketEnum.USER_DETAILS);
        } catch(Exception e) {
            logger.error("Error occurred during user profile update", e);
            if(existingUser != null) {
                logger.debug("User profile update failed for: {}", existingUser.getUsername());
            }
        }
        return existingUser;
    }

    @Override
    public void deleteUserDetails(String id){
        try {
            couchbaseUtils.deleteUser(id, BucketEnum.USER_DETAILS);
        } catch(Exception ex) {
            logger.error("Error occurred during user deletion", ex);
        }
    }


    @Override
    public String login(String username, String rawPassword) {
        try {
            List<JsonObject> result = couchbaseUtils.getUsernameDetails(username).rowsAsObject();

            if (result == null || result.isEmpty()) {
                logger.info("Username Provided does not exist");
                throw new LoginException("Username does not exist, please register");
            }

            String jsonDoc = result.get(0).get(BucketEnum.USER_DETAILS.getCollection()).toString();
            UserRegister user = objectMapper.readValue(jsonDoc, UserRegister.class);

            if (!rawPassword.equals(user.getPassword())) {
                logger.info("Password does not match");
                throw new LoginException("Password does not match");
            }
            return jwtUtils.generateToken(user.getUsername());
        } catch (Exception e) {
            throw new LoginException("Login failed", e);
        }
    }


    private void validateUser(UserRegister user) {
        usernameVerification(user);

        passwordVerification(user);

        emailVerification(user);

        phoneNoVerification(user);
    }

    private void phoneNoVerification(UserRegister user) {
        if(user.getPhone() == null || user.getPhone().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        else{
            if(!user.getPhone().matches("^\\d{10}$")) {
                throw new IllegalArgumentException("Invalid phone number format");
            }
            else{
                QueryResult result = couchbaseUtils.getPhoneDetails(user.getPhone());

                if (!result.rowsAsObject().isEmpty()) {
                    throw new IllegalArgumentException("Phone number already exists");
                }
            }
        }
    }

    private void emailVerification(UserRegister user) {
        if(user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        else{
            if(!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            else {
                QueryResult result = couchbaseUtils.getEmailDetails(user.getEmail());

                if (!result.rowsAsObject().isEmpty()) {
                    throw new IllegalArgumentException("Email already exists");
                }
            }
        }
    }

    private static void passwordVerification(UserRegister user) {
        if(user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        else{
            if(user.getPassword().length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters long");
            }
            else{
                if(!user.getPassword().matches(".*[A-Z].*")) {
                    throw new IllegalArgumentException("Password must contain at least one uppercase letter");
                }
                if(!user.getPassword().matches(".*[a-z].*")) {
                    throw new IllegalArgumentException("Password must contain at least one lowercase letter");
                }
                if(!user.getPassword().matches(".*\\d.*")) {
                    throw new IllegalArgumentException("Password must contain at least one digit");
                }
            }
        }
    }

    private void usernameVerification(UserRegister user) {
        if(user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        else {
            QueryResult result = couchbaseUtils.getUsernameDetails(user.getUsername());

            if (!result.rowsAsObject().isEmpty()) {
                throw new IllegalArgumentException("Username already exists");
            }
        }
    }


}
