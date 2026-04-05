package com.auth.service;

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



}
