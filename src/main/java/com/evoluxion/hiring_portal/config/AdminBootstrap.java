package com.evoluxion.hiring_portal.config;

import com.evoluxion.hiring_portal.entity.ApprovalStatus;
import com.evoluxion.hiring_portal.entity.Role;
import com.evoluxion.hiring_portal.entity.UserAccount;
import com.evoluxion.hiring_portal.repository.UserAccountRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class AdminBootstrap {

    @Bean
    CommandLineRunner createDefaultAdmin(
            UserAccountRepository repository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            String email =
                    "admin@evoluxion.com";

            if (repository.existsByEmail(email)) {
                return;
            }

            UserAccount admin =
                    UserAccount.builder()

                    .email(email)

                    .password(
                            passwordEncoder.encode(
                                    "ChangeMe@123"
                            )
                    )

                    .role(Role.ADMIN)

                    .enabled(true)

                    .approvalStatus(
                            ApprovalStatus.APPROVED
                    )

                    .fullName(
                            "GIRISH"
                    )

                    .createdAt(
                            LocalDateTime.now()
                    )

                    .updatedAt(
                            LocalDateTime.now()
                    )

                    .build();

            repository.save(admin);

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "DEFAULT ADMIN CREATED"
            );

            System.out.println(
                    "Email: admin@evoluxion.com"
            );

            System.out.println(
                    "Password: ChangeMe@123"
            );

            System.out.println(
                    "================================="
            );
        };
    }
}