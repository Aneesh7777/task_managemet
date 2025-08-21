package com.task_management.org;

import com.task_management.org.entity.User;
import com.task_management.org.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (userRepository.count() == 0) {

            User admin = new User();
            admin.setUsername("admin");


            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@test.com");
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);


            User user = new User();
            user.setUsername("user");


            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@test.com");
            user.setRole("ROLE_USER");
            userRepository.save(user);




        }
        if (userRepository.count() == 2)
        {
            User user2 = new User();
            user2.setUsername("user2");


            user2.setPassword(passwordEncoder.encode("user123"));
            user2.setEmail("user2@test.com");
            user2.setRole("ROLE_USER");
            userRepository.save(user2);
        }
    }
}

