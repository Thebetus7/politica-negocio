package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "portafolios")
@Data
public class Portafolio extends BaseEntity {

    @Id
    private String id;
    
    // Contiene los datos recopilados del cliente del mundo real en formato JSON dinámico
    private Map<String, Object> clienteData;
    
    // Qué política de negocio se va a aplicar a este portafolio
    private String politicaId;
    
    // Usuario AC responsable
    private String atencionClienteId;
}
