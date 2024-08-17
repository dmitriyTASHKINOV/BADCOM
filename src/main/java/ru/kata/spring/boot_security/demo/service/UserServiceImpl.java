package ru.kata.spring.boot_security.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.model.Role;

import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.repository.UsersRepository;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    @Autowired
    public UserServiceImpl(UsersRepository usersRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public User findByUsername(String username){
        return usersRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> listUsers() {
        return usersRepository.findAll();
    }

    @Transactional
    @Override
    public void add(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        usersRepository.save(user);
    }

    public User findById(Long id){
        return usersRepository.getById(id);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        if (usersRepository.findById(id).isPresent()) usersRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void update(User user) {
        User existingUser = usersRepository.findById(user.getId()).orElse(null);
        if (existingUser != null) {
            if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().equals(existingUser.getPassword())) {
                user.setPassword(existingUser.getPassword()); // Используем существующий хеш пароля
            } else {
                user.setPassword(passwordEncoder.encode(user.getPassword())); // Хешируем новый пароль
            }
            usersRepository.save(user);
        }
    }
    @PostConstruct
    @Transactional
    public void initializeAdminUser() {
        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole == null) {
            userRole = new Role("ROLE_USER");
            roleRepository.save(userRole);
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        if (adminRole == null) {
            adminRole = new Role("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }

        String encodedPassword = passwordEncoder.encode("admin");
        String encodedPassword1 = passwordEncoder.encode("user");
        String encodedPassword2 = passwordEncoder.encode("test");

        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(userRole);
        adminRoles.add(adminRole);
        User adminUser = new User("admin", encodedPassword, "admin@example.com", 30, adminRoles);

        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        User User = new User("user", encodedPassword1, "user@example.com", 18, userRoles);

        Set<Role> testRoles = new HashSet<>();
        testRoles.add(userRole);
        User test = new User("test", encodedPassword2, "test@example.com", 25, testRoles);

        usersRepository.save(adminUser);
        usersRepository.save(User);
        usersRepository.save(test);
    }
}
