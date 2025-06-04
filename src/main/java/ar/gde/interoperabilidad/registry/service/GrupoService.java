// src/main/java/ar/gde/interoperabilidad/registry/service/GrupoService.java
package ar.gde.interoperabilidad.registry.service;

import java.util.List;
import java.util.Map;

public interface GrupoService {
    List<Map<String, Object>> listarTodosGrupos();
    Map<String, Object> obtenerGrupoPorId(long idGrupo);
    void crearGrupo(Map<String, Object> datos);
    void actualizarGrupo(Map<String, Object> datos);
    void bajaGrupo(long idGrupo);
    void altaGrupo(long idGrupo, String usuarioModificacion);
}
