package com.example.politica_negocio.config;

import com.example.politica_negocio.model.PoliticaNegocio;
import com.example.politica_negocio.model.Usuario;
import com.example.politica_negocio.repository.PoliticaNegocioRepository;
import com.example.politica_negocio.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepository, PoliticaNegocioRepository politicaRepository) {
        return args -> {
            // Seed de Usuario para pruebas
            if (usuarioRepository.count() == 0) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword("admin123");
                usuarioRepository.save(admin);
                System.out.println("✅ Seed: Usuario admin creado.");
            }

            // Seed de Política de Negocio inicial
            if (politicaRepository.count() == 0) {
                PoliticaNegocio politica = new PoliticaNegocio();
                politica.setNombre("Política de Ventas Estándar");
                politica.setDescripcion("Diagrama base para el proceso de ventas");
                politica.setEstado("borrador");
                politicaRepository.save(politica);
                System.out.println("✅ Seed: Política inicial creada.");
            }
        };
    }
}
