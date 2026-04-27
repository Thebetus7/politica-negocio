package com.example.politica_negocio.seeder;

import com.example.politica_negocio.model.Role;
import com.example.politica_negocio.model.Usuario;
import com.example.politica_negocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            seedAdministrador();
            seedFuncionarios();
            seedAtencionCliente();
            System.out.println("Base de datos poblada exitosamente con usuarios por defecto.");
        }
    }

    private void seedAdministrador() {
        Usuario admin = new Usuario();
        admin.setNombre("Administrador Root");
        admin.setCorreo("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRol(Role.ADMINISTRADOR);
        admin.setCreatedAt(LocalDateTime.now());
        usuarioRepository.save(admin);
    }

    private void seedFuncionarios() {
        for (int i = 1; i <= 2; i++) {
            Usuario funcionario = new Usuario();
            funcionario.setNombre("Funcionario " + i);
            funcionario.setCorreo("funcionario" + i + "@example.com");
            funcionario.setPassword(passwordEncoder.encode("password"));
            funcionario.setRol(Role.FUNCIONARIO);
            funcionario.setCreatedAt(LocalDateTime.now());
            usuarioRepository.save(funcionario);
        }
    }

    private void seedAtencionCliente() {
        for (int i = 1; i <= 2; i++) {
            Usuario atencion = new Usuario();
            atencion.setNombre("Atencion Cliente " + i);
            atencion.setCorreo("atencion" + i + "@example.com");
            atencion.setPassword(passwordEncoder.encode("password"));
            atencion.setRol(Role.ATENCION_CLIENTE);
            atencion.setCreatedAt(LocalDateTime.now());
            usuarioRepository.save(atencion);
        }
    }
}
