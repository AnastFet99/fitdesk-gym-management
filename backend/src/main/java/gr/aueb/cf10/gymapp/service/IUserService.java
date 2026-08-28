package gr.aueb.cf10.gymapp.service;

import gr.aueb.cf10.gymapp.dto.UserInsertDTO;
import gr.aueb.cf10.gymapp.dto.UserReadOnlyDTO;
import gr.aueb.cf10.gymapp.model.enums.Role;

import java.util.List;
import java.util.UUID;

public interface IUserService {

    UserReadOnlyDTO createUser(UserInsertDTO insertDTO);

    UserReadOnlyDTO updateUser(UUID uuid, UserInsertDTO insertDTO);

    void deleteUser(UUID uuid);

    UserReadOnlyDTO getUserByUuid(UUID uuid);

    UserReadOnlyDTO getUserByEmail(String email);

    List<UserReadOnlyDTO> getAllUsers();

    List<UserReadOnlyDTO> getUsersByRole(Role role);
}
