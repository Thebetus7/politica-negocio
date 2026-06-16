package com.example.politica_negocio.seeder;

import com.example.politica_negocio.model.Role;
import com.example.politica_negocio.model.Departamento;
import com.example.politica_negocio.model.Usuario;
import com.example.politica_negocio.repository.DepartamentoRepository;
import com.example.politica_negocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final DepartamentoRepository departamentoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (departamentoRepository.count() == 0) {
            seedDepartamentos();
        }
        if (usuarioRepository.count() == 0) {
            seedAdministrador();
            seedAtencionCliente();
            System.out.println("Base de datos poblada: admin, atención al cliente y departamentos.");
            System.out.println("Los funcionarios se crean manualmente en gestión de usuarios y se asignan a departamentos.");
        }
    }

    private void seedDepartamentos() {
        Departamento d1 = new Departamento();
        d1.setNombre("Recursos Humanos");
        d1.setDescripcion("Gestiona personal, permisos y procesos internos de talento humano.");
        d1.setCreatedAt(LocalDateTime.now());

        Departamento d2 = new Departamento();
        d2.setNombre("Operaciones");
        d2.setDescripcion("Coordina la ejecución operativa y el flujo diario del negocio.");
        d2.setCreatedAt(LocalDateTime.now());

        Departamento d3 = new Departamento();
        d3.setNombre("Atención al Cliente");
        d3.setDescripcion("Gestiona solicitudes, reclamos y seguimiento de clientes.");
        d3.setCreatedAt(LocalDateTime.now());

        departamentoRepository.saveAll(List.of(d1, d2, d3));
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
