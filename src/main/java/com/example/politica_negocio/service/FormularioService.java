package com.example.politica_negocio.service;

import com.example.politica_negocio.model.CampoFormulario;
import com.example.politica_negocio.model.Formulario;
import com.example.politica_negocio.repository.FormularioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FormularioService {

    private final FormularioRepository repository;

    public List<Formulario> getAll() {
        return repository.findAllActive();
    }

    public Formulario getById(String id) {
        return repository.findById(id)
                .filter(f -> f.getDeletedAt() == null)
                .orElse(null);
    }

    public Formulario create(Formulario formulario) {
        formulario.setCampos(normalizarCampos(formulario.getCampos()));
        formulario.setCreatedAt(LocalDateTime.now());
        formulario.setUpdatedAt(LocalDateTime.now());
        return repository.save(formulario);
    }

    public Formulario update(String id, Formulario datos) {
        Formulario existente = getById(id);
        if (existente == null) return null;
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setCampos(normalizarCampos(datos.getCampos()));
        existente.setUpdatedAt(LocalDateTime.now());
        return repository.save(existente);
    }

    private List<CampoFormulario> normalizarCampos(List<CampoFormulario> campos) {
        if (campos == null) {
            return new ArrayList<>();
        }
        List<CampoFormulario> copia = new ArrayList<>();
        for (int i = 0; i < campos.size(); i++) {
            CampoFormulario campo = campos.get(i);
            if (campo == null) continue;
            if (campo.getOrden() == null) {
                campo.setOrden(i);
            }
            if (campo.getRequerido() == null) {
                campo.setRequerido(false);
            }
            if (campo.getOpciones() == null) {
                campo.setOpciones(new ArrayList<>());
            }
            copia.add(campo);
        }
        return copia;
    }

    public boolean softDelete(String id) {
        Formulario existente = getById(id);
        if (existente == null) return false;
        existente.setDeletedAt(LocalDateTime.now());
        repository.save(existente);
        return true;
    }
}
