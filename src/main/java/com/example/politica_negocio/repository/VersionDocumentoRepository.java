package com.example.politica_negocio.repository;

import com.example.politica_negocio.model.VersionDocumento;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface VersionDocumentoRepository extends MongoRepository<VersionDocumento, String> {

    List<VersionDocumento> findByDocumentoIdAndDeletedAtIsNullOrderByVersionDesc(String documentoId);
    
    List<VersionDocumento> findByDocumentoIdAndDeletedAtIsNull(String documentoId);
}
