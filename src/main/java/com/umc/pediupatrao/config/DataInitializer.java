package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.UsuarioRole;
import com.umc.pediupatrao.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${spring.security.user.name}")
    private String adminUsername;

    @Value("${spring.security.user.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo) {
        return args -> {

            if (repo.findByUsername(adminUsername).isEmpty()) {

                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                Usuario user = new Usuario();
                user.setUsername(adminUsername);
                user.setPassword(encoder.encode(adminPassword));
                user.setRole(UsuarioRole.ADMIN);

                repo.save(user);

                System.out.println("Usuário admin criado!");
            }
        };
    }
}
