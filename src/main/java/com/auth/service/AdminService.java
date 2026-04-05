package com.auth.service;

import com.auth.dto.CreateUserRequest;
import com.auth.entities.User;
import com.auth.exeptions.AuthException;
import com.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //Create User
    public User createUser(CreateUserRequest request) {
        //Check User is already Exist or not
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthException("User already exists");
        }
        //Check for role
        if (request.getRole() == null) {
            throw new AuthException("Role is required");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);

        return userRepository.save(user);
    }
    // GET ALL USERS
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public User updataUser(UUID userId, UpdateUserRequest request){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));
        if(request.getRole() != null){
            user.setRole(request.getRole());
        }
        if(request.getActive() != null){
            user.setActive(request.getActive());
        }
        
        return userRepository.save(user);
    }

    // Toggle status
    public User toggleUserStatus(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthException("User not found"));

        user.setActive(!user.isActive());
        return userRepository.save(user);
    }


}
