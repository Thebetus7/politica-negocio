package com.example.politica_negocio.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "flujos")
@Data
public class Flujo extends BaseEntity {

    @Id
    private String id;

    private String politicaId;

    private String actividadId;

    /**
     * Referencia al Trámite (Portafolio). 
     * Si es null, el flujo es la plantilla de la Política. 
     * Si tiene valor, es la instancia en ejecución para un Trámite.
     */
    private String portafolioId;

    /**
     * JSON con la lógica de rutas y flujo de ejecución.
     * Cuando un funcionario completa su FormUpdate, el sistema consulta
     * este campo para determinar cuál nodo activar a continuación.
     *
     * Estructura:
     * {
     *   "tipo": "secuencial|alternativo|iterativo_while|iterativo_dowhile|paralelo",
     *   "siguientes": [
     *     { "actividadDestinoId": "xxx", "condicion": "aprobado", "label": "Sí" }
     *   ],
     *   "condicion": "¿Es válido?",
     *   "retornoActividadId": "yyy",
     *   "estadoActual": "pendiente|en_progreso|completado",
     *   "orden": 1
     * }
     */
    private Map<String, Object> proceso;
}
