// src/main/java/ar/gde/interoperabilidad/registry/service/EcosistemaService.java
package ar.gde.interoperabilidad.registry.service;

import java.util.List;
import java.util.Map;

public interface EcosistemaService {

    // REG_GRUPO
    List<Map<String, Object>> listarTodosGrupos();
    Map<String, Object> obtenerGrupoPorId(long idGrupo);
    void crearGrupo(Map<String, Object> datos);
    void actualizarGrupo(Map<String, Object> datos);
    void bajaGrupo(long idGrupo);
    void altaEcosistema(String nombreEcosistema, String usuarioModificacion);

    // REG_ECOSISTEMA
    List<Map<String, Object>> listarTodosEcosistemas();
    Map<String, Object> obtenerEcosistemaPorNombre(String nombreEcosistema);
    void crearEcosistema(Map<String, Object> datos);
    void actualizarEcosistema(Map<String, Object> datos);
    void bajaEcosistema(String nombreEcosistema);

    // REG_DOMINIO_ECOSISTEMA
    List<Map<String, Object>> listarDominiosPorEcosistema(String nombreEcosistema);
    Map<String, Object> obtenerDominioPorId(long idDominio);
    void crearDominioEcosistema(Map<String, Object> datos);
    void actualizarDominioEcosistema(Map<String, Object> datos);
    void bajaDominioEcosistema(long idDominio);

    // REG_MODULO
    List<Map<String, Object>> listarTodosModulos();
    Map<String, Object> obtenerModuloPorId(long idModulo);
    void crearModulo(Map<String, Object> datos);
    void actualizarModulo(Map<String, Object> datos);
    void bajaModulo(long idModulo);

    // REG_GENERIC_ENDPOINTS
    List<Map<String, Object>> listarGenericEndpointsPorModuloYEco(long idModulo, String nombreEcosistema);
    Map<String, Object> obtenerGenericEndpointPorId(long idEndpoint);
    void crearGenericEndpoint(Map<String, Object> datos);
    void actualizarGenericEndpoint(Map<String, Object> datos);
    void bajaGenericEndpoint(long idEndpoint);

    // REG_ENDPOINT_DOMINIO
    List<Map<String, Object>> listarEndpointDominioPorEcosistema(String nombreEcosistema);
    Map<String, Object> obtenerEndpointDominioPorId(long idEndpointDominio);
    void crearEndpointDominio(Map<String, Object> datos);
    void actualizarEndpointDominio(Map<String, Object> datos);
    void bajaEndpointDominio(long idEndpointDominio);

    // PROPERTY_CONFIGURATION
    List<Map<String, Object>> listarTodasPropiedades();
    Map<String, Object> obtenerPropiedadPorClave(String clave);
    void upsertPropiedadConfiguracion(Map<String, Object> datos);

    // PROBAR CONECTIVIDAD
    Map<String, Object> probarConectividad(String nombreEcosistema);
}
