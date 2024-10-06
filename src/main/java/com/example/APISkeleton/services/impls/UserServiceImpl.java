package com.example.APISkeleton.services.impls;

import com.example.APISkeleton.mappers.IUserMapper;
import com.example.APISkeleton.persistance.entities.User;
import com.example.APISkeleton.persistance.repositories.IUserRepository;
import com.example.APISkeleton.services.IUserService;
import com.example.APISkeleton.web.dtos.requests.CreateUserRequest;
import com.example.APISkeleton.web.dtos.responses.BaseResponse;
import com.example.APISkeleton.web.exceptions.ConflictException;
import com.example.APISkeleton.web.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements IUserService {

    private final IUserRepository repository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final IUserMapper userMapper;

    public UserServiceImpl(IUserRepository repository, IUserMapper userMapper) {
        this.repository = repository;
        this.userMapper = userMapper;
    }

    @Override
    public BaseResponse create(CreateUserRequest request) {
        Optional<User> optionalUser = getOptionalUserByEmail(request.getEmail());

        if (optionalUser.isPresent()) {
            throw new ConflictException("User already exists with this email");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);

        repository.save(user);

        return BaseResponse.builder()
                .data(userMapper.toCreateUserResponse(user))
                .message("User created successfully")
                .success(true)
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    @Override
    public User getByEmail(String email) {
        return repository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException(User.class));
    }

    @Override
    public Optional<User> getOptionalUserByEmail(String email) {
        return repository.findByEmail(email);
    }

}
