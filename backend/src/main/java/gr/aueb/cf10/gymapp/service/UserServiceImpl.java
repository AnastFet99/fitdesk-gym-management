package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf10.gymapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf10.gymapp.core.mapper.Mapper;
import gr.aueb.cf10.gymapp.dto.UserInsertDTO;
import gr.aueb.cf10.gymapp.dto.UserReadOnlyDTO;
import gr.aueb.cf10.gymapp.model.User;
import gr.aueb.cf10.gymapp.model.enums.Role;
import gr.aueb.cf10.gymapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserReadOnlyDTO createUser(UserInsertDTO insertDTO) {
        log.info("Creating user: {}", insertDTO.email());

        if (userRepository.existsByEmail(insertDTO.email())) {
            log.error("User with email {} already exists", insertDTO.email());
            throw new EntityAlreadyExistsException("User", "email", insertDTO.email());
        }

        User user = mapper.mapToUser(insertDTO);
        // Hash password with BCrypt
        user.setPassword(passwordEncoder.encode(insertDTO.password()));
        User savedUser = userRepository.save(user);

        log.info("Successfully created user with uuid: {}", savedUser.getUuid());
        return mapper.mapToReadOnlyDTO(savedUser);
    }

    @Override
    @Transactional
    public UserReadOnlyDTO updateUser(UUID uuid, UserInsertDTO insertDTO) {
        log.info("Updating user with uuid: {}", uuid);

        User existingUser = userRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("User with uuid {} not found", uuid);
                    return new EntityNotFoundException("User", uuid);
                });

        if (!existingUser.getEmail().equals(insertDTO.email()) &&
                userRepository.existsByEmail(insertDTO.email())) {
            log.error("Email {} is already taken", insertDTO.email());
            throw new EntityAlreadyExistsException("User", "email", insertDTO.email());
        }

        existingUser.setName(insertDTO.name());
        existingUser.setEmail(insertDTO.email());
        existingUser.setPassword(passwordEncoder.encode(insertDTO.password()));
        existingUser.setRole(insertDTO.role());

        User updatedUser = userRepository.save(existingUser);
        log.info("Successfully updated user with uuid: {}", uuid);

        return mapper.mapToReadOnlyDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID uuid) {
        log.info("Deleting user with uuid: {}", uuid);

        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("User with uuid {} not found", uuid);
                    return new EntityNotFoundException("User", uuid);
                });

        userRepository.delete(user);
        log.info("Successfully deleted user with uuid: {}", uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserByUuid(UUID uuid) {
        log.info("Fetching user with uuid: {}", uuid);

        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("User with uuid {} not found", uuid);
                    return new EntityNotFoundException("User", uuid);
                });

        return mapper.mapToReadOnlyDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserReadOnlyDTO getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User with email {} not found", email);
                    return new EntityNotFoundException("User with email '" + email + "' was not found");
                });

        return mapper.mapToReadOnlyDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReadOnlyDTO> getAllUsers() {
        log.info("Fetching all users");

        return userRepository.findAll()
                .stream()
                .map(mapper::mapToReadOnlyDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReadOnlyDTO> getUsersByRole(Role role) {
        log.info("Fetching users with role: {}", role);

        return userRepository.findByRole(role)
                .stream()
                .map(mapper::mapToReadOnlyDTO)
                .toList();
    }
}
