package com.example.politica_negocio.seeder;

import com.example.politica_negocio.repository.DepartamentoRepository;
import com.example.politica_negocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final DepartamentoRepository departamentoRepository;
    private final SeederService seederService;

    @Override
    public void run(String... args) throws Exception {
        if (departamentoRepository.count() == 0) {
            seederService.seedDepartamentos();
        }
        if (usuarioRepository.count() == 0) {
            seederService.seedAdministrador();
            seederService.seedAtencionCliente();
            System.out.println("Base de datos poblada: admin, atención al cliente y departamentos.");
            System.out.println("Los funcionarios se crean manualmente en gestión de usuarios y se asignan a departamentos.");
        }
    }
}

