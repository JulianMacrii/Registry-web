// src/main/java/ar/gde/interoperabilidad/registry/service/impl/EcosistemaServiceImpl.java
package ar.gde.interoperabilidad.registry.service.impl;

import org.springframework.stereotype.Service;
import ar.gde.interoperabilidad.registry.service.EcosistemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("ecosistemaService")
public class EcosistemaServiceImpl implements EcosistemaService {

    @Autowired
    private DataSource dataSource;

    // Usaremos un único JdbcTemplate para todas las consultas/updates
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        // Inicializamos jdbcTemplate con el DataSource inyectado
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // ---------------------------------------
    // REG_GRUPO
    // ---------------------------------------
    @Override
    public List<Map<String, Object>> listarTodosGrupos() {
        String sql = ""
            + "SELECT id, descripcionGrupo AS nombre, estadoGrupo AS estado, "
            + "       TO_CHAR(fechaCreacion,'YYYY-MM-DD HH24:MI:SS') AS fechaCreacion, "
            + "       TO_CHAR(fechaModificacion,'YYYY-MM-DD HH24:MI:SS') AS fechaModificacion, "
            + "       usuarioCreacion, usuarioModificacion "
            + "FROM REG_GRUPO";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public Map<String, Object> obtenerGrupoPorId(long idGrupo) {
        String sql = ""
            + "SELECT id, descripcionGrupo AS nombre, estadoGrupo AS estado, "
            + "       TO_CHAR(fechaCreacion,'YYYY-MM-DD HH24:MI:SS') AS fechaCreacion, "
            + "       TO_CHAR(fechaModificacion,'YYYY-MM-DD HH24:MI:SS') AS fechaModificacion, "
            + "       usuarioCreacion, usuarioModificacion "
            + "FROM REG_GRUPO "
            + "WHERE id = ?";
        return jdbcTemplate.queryForMap(sql, idGrupo);
    }

    @Override
    public void crearGrupo(Map<String, Object> datos) {
        String sql = ""
            + "INSERT INTO REG_GRUPO "
            + "(descripcionGrupo, estadoGrupo, fechaCreacion, usuarioCreacion) "
            + "VALUES (?, ?, SYSDATE, ?)";
        jdbcTemplate.update(
            sql,
            datos.get("nombre"),        // descripcionGrupo
            datos.get("estado"),        // estadoGrupo
            datos.get("usuarioCreacion")
        );
    }

    @Override
    public void actualizarGrupo(Map<String, Object> datos) {
        String sql = ""
            + "UPDATE REG_GRUPO SET "
            + "  descripcionGrupo    = ?, "
            + "  estadoGrupo         = ?, "
            + "  fechaModificacion   = SYSDATE, "
            + "  usuarioModificacion = ? "
            + "WHERE id = ?";
        jdbcTemplate.update(
            sql,
            datos.get("nombre"),
            datos.get("estado"),
            datos.get("usuarioModificacion"),
            datos.get("id")
        );
    }

    @Override
    public void bajaGrupo(long idGrupo) {
        // 1) Marcar el grupo como INACTIVO
        String sql1 = ""
            + "UPDATE REG_GRUPO SET "
            + "  estadoGrupo         = 'INACTIVO', "
            + "  fechaModificacion   = SYSDATE, "
            + "  usuarioModificacion = 'SYSTEM' "
            + "WHERE id = ?";
        jdbcTemplate.update(sql1, idGrupo);

        // 2) Marcar todos los ecosistemas de ese grupo como INACTIVOS
        String sql2 = ""
            + "UPDATE REG_ECOSISTEMA SET "
            + "  estado             = 'INACTIVO', "
            + "  fechaModificacion   = SYSDATE, "
            + "  usuarioModificacion = 'SYSTEM' "
            + "WHERE regGrupo = ?";
        jdbcTemplate.update(sql2, idGrupo);
    }

    @Override
    public void altaEcosistema(String nombreEcosistema, String usuarioModificacion) {
        String sql = ""
            + "UPDATE REG_ECOSISTEMA SET "
            + "  estado             = 'ACTIVO', "
            + "  fechaModificacion   = SYSDATE, "
            + "  usuarioModificacion = ? "
            + "WHERE descripcionEcosistema = ?";
        jdbcTemplate.update(sql, usuarioModificacion, nombreEcosistema);
    }

    // ---------------------------------------
    // REG_ECOSISTEMA
    // ---------------------------------------

    @Override
    public List<Map<String, Object>> listarTodosEcosistemas() {
        String sql =
            "SELECT e.id, e.descripcionEcosistema AS nombre, e.estado, " +
            "       TO_CHAR(e.fechaCreacion,'YYYY-MM-DD HH24:MI:SS')   AS fechaCreacion, " +
            "       TO_CHAR(e.fechaModificacion,'YYYY-MM-DD HH24:MI:SS') AS fechaModificacion, " +
            "       e.usuarioCreacion, e.usuarioModificacion, " +
            "       g.descripcionGrupo AS nombreGrupo, e.version, e.certificado " +
            "  FROM REG_ECOSISTEMA e " +
            "  LEFT JOIN REG_GRUPO g ON e.regGrupo = g.id " +
            " ORDER BY e.descripcionEcosistema";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public Map<String, Object> obtenerEcosistemaPorNombre(String nombreEcosistema) {
        String sql =
            "SELECT e.id, e.descripcionEcosistema AS nombre, e.estado, " +
            "       TO_CHAR(e.fechaCreacion,'YYYY-MM-DD HH24:MI:SS')   AS fechaCreacion, " +
            "       TO_CHAR(e.fechaModificacion,'YYYY-MM-DD HH24:MI:SS') AS fechaModificacion, " +
            "       e.usuarioCreacion, e.usuarioModificacion, " +
            "       g.descripcionGrupo AS nombreGrupo, e.version, e.certificado " +
            "  FROM REG_ECOSISTEMA e " +
            "  LEFT JOIN REG_GRUPO g ON e.regGrupo = g.id " +
            " WHERE e.descripcionEcosistema = ?";
        return jdbcTemplate.queryForMap(sql, nombreEcosistema);
    }

    @Override
    public void crearEcosistema(Map<String, Object> datos) {
        // Suponiendo que datos trae "nombre", "estado", "usuarioCreacion", "certificado", "version", "grupo"
        // Primero obtenemos el ID del grupo a partir de descripciónGrupo:
        String sqlGrupo = "SELECT id FROM REG_GRUPO WHERE descripcionGrupo = ?";
        Integer idGrupo = jdbcTemplate.queryForObject(sqlGrupo, new Object[]{ datos.get("grupo") }, Integer.class);

        String insertSql =
            "INSERT INTO REG_ECOSISTEMA " +
            "   (id, descripcionEcosistema, estado, fechaCreacion, usuarioCreacion, certificado, version, regGrupo) " +
            " VALUES (REG_ECOSISTEMA_SEQ.NEXTVAL, ?, ?, SYSDATE, ?, ?, ?, ?)";
        jdbcTemplate.update(insertSql,
            datos.get("nombre"),
            datos.get("estado"),
            datos.get("usuarioCreacion"),
            datos.get("certificado"),
            datos.get("version"),
            idGrupo
        );
    }

    @Override
    public void actualizarEcosistema(Map<String, Object> datos) {
        // datos trae "id", "nombre", "estado", "usuarioModificacion", "certificado", "version", "grupo"
        String sqlGrupo = "SELECT id FROM REG_GRUPO WHERE descripcionGrupo = ?";
        Integer idGrupo = jdbcTemplate.queryForObject(sqlGrupo, new Object[]{ datos.get("grupo") }, Integer.class);

        String updateSql =
            "UPDATE REG_ECOSISTEMA " +
            "   SET descripcionEcosistema = ?, " +
            "       estado             = ?, " +
            "       fechaModificacion  = SYSDATE, " +
            "       usuarioModificacion= ?, " +
            "       certificado        = ?, " +
            "       version            = ?, " +
            "       regGrupo           = ? " +
            " WHERE id = ?";
        jdbcTemplate.update(updateSql,
            datos.get("nombre"),
            datos.get("estado"),
            datos.get("usuarioModificacion"),
            datos.get("certificado"),
            datos.get("version"),
            idGrupo,
            datos.get("id")
        );
    }

    @Override
    public void bajaEcosistema(String nombreEcosistema) {
        String sql =
            "UPDATE REG_ECOSISTEMA " +
            "   SET estado = 'INACTIVO', " +
            "       fechaModificacion = SYSDATE " +
            " WHERE descripcionEcosistema = ?";
        jdbcTemplate.update(sql, nombreEcosistema);
    }

    // ---------------------------------------
    // REG_DOMINIO_ECOSISTEMA
    // ---------------------------------------

    @Override
    public List<Map<String, Object>> listarDominiosPorEcosistema(String nombreEcosistema) {
        String sql = ""
            + "SELECT d.id, d.descripcionDominio AS nombreDominio, d.tipoDominio, "
            + "       d.valor, d.estado, "
            + "       TO_CHAR(d.fechaCreacion,'YYYY-MM-DD HH24:MI:SS')    AS fechaCreacion, "
            + "       TO_CHAR(d.fechaModificacion,'YYYY-MM-DD HH24:MI:SS')AS fechaModificacion, "
            + "       d.usuarioCreacion, d.usuarioModificacion, "
            + "       e.id   AS idEcosistema, "
            + "       e.descripcionEcosistema AS nombreEcosistema, "
            + "       m.id   AS idModulo, "
            + "       m.descripcion AS nombreModulo, "
            + "       m.prefixName AS prefixNameModulo "
            + "FROM REG_DOMINIO_ECOSISTEMA d "
            + "JOIN REG_ECOSISTEMA e ON d.idEcosistema = e.id "
            + "JOIN REG_MODULO m      ON d.idModulo     = m.id "
            + "WHERE e.descripcionEcosistema = ?";
        return jdbcTemplate.queryForList(sql, nombreEcosistema);
    }

    @Override
    public Map<String, Object> obtenerDominioPorId(long idDominio) {
        String sql = ""
            + "SELECT d.id, d.descripcionDominio AS nombreDominio, d.tipoDominio, "
            + "       d.valor, d.estado, "
            + "       TO_CHAR(d.fechaCreacion,'YYYY-MM-DD HH24:MI:SS')    AS fechaCreacion, "
            + "       TO_CHAR(d.fechaModificacion,'YYYY-MM-DD HH24:MI:SS')AS fechaModificacion, "
            + "       d.usuarioCreacion, d.usuarioModificacion, "
            + "       e.id   AS idEcosistema, "
            + "       e.descripcionEcosistema AS nombreEcosistema, "
            + "       m.id   AS idModulo, "
            + "       m.descripcion AS nombreModulo, "
            + "       m.prefixName AS prefixNameModulo "
            + "FROM REG_DOMINIO_ECOSISTEMA d "
            + "JOIN REG_ECOSISTEMA e ON d.idEcosistema = e.id "
            + "JOIN REG_MODULO m      ON d.idModulo     = m.id "
            + "WHERE d.id = ?";
        return jdbcTemplate.queryForMap(sql, idDominio);
    }

    @Override
    public void crearDominioEcosistema(Map<String, Object> datos) {
        String sql = ""
            + "INSERT INTO REG_DOMINIO_ECOSISTEMA "
            + "("
            + "  descripcionDominio, tipoDominio, valor, estado, "
            + "  idEcosistema, idModulo, fechaCreacion, usuarioCreacion"
            + ") VALUES ("
            + "  ?, ?, ?, ?, "
            + "  (SELECT id FROM REG_ECOSISTEMA WHERE descripcionEcosistema = ?), "
            + "  ?, SYSDATE, ?"
            + ")";
        jdbcTemplate.update(
            sql,
            datos.get("nombreDominio"),        // descripcionDominio
            datos.get("tipoDominio"),          // tipoDominio
            datos.get("valor"),                // valor
            datos.get("estado"),               // estado
            datos.get("nombreEcosistema"),     // buscado en REG_ECOSISTEMA
            datos.get("idModulo"),             // idModulo
            datos.get("usuarioCreacion")
        );
    }

    @Override
    public void actualizarDominioEcosistema(Map<String, Object> datos) {
        String sql = ""
            + "UPDATE REG_DOMINIO_ECOSISTEMA SET "
            + "  descripcionDominio   = ?, "
            + "  tipoDominio          = ?, "
            + "  valor                = ?, "
            + "  estado               = ?, "
            + "  fechaModificacion    = SYSDATE, "
            + "  usuarioModificacion  = ? "
            + "WHERE id = ?";
        jdbcTemplate.update(
            sql,
            datos.get("nombreDominio"),
            datos.get("tipoDominio"),
            datos.get("valor"),
            datos.get("estado"),
            datos.get("usuarioModificacion"),
            datos.get("id")
        );
    }

    @Override
    public void bajaDominioEcosistema(long idDominio) {
        String sql = ""
            + "UPDATE REG_DOMINIO_ECOSISTEMA SET "
            + "  estado               = 'INACTIVO', "
            + "  fechaModificacion    = SYSDATE, "
            + "  usuarioModificacion  = 'SYSTEM' "
            + "WHERE id = ?";
        jdbcTemplate.update(sql, idDominio);
    }

    // ---------------------------------------
    // REG_MODULO
    // ---------------------------------------

    @Override
    public List<Map<String, Object>> listarTodosModulos() {
        String sql = ""
            + "SELECT id, descripcion AS nombre, estado, "
            + "       TO_CHAR(fechaCreacion,'YYYY-MM-DD HH24:MI:SS') AS fechaCreacion, "
            + "       TO_CHAR(fechaModificacion,'YYYY-MM-DD HH24:MI:SS') AS fechaModificacion, "
            + "       usuarioCreacion, usuarioModificacion, "
            + "       componentName, prefixName, regGenericEndpoints "
            + "FROM REG_MODULO";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public Map<String, Object> obtenerModuloPorId(long idModulo) {
        String sql = ""
            + "SELECT id, descripcion AS nombre, estado, "
            + "       TO_CHAR(fechaCreacion,'YYYY-MM-DD HH24:MI:SS') AS fechaCreacion, "
            + "       TO_CHAR(fechaModificacion,'YYYY-MM-DD HH24:MI:SS') AS fechaModificacion, "
            + "       usuarioCreacion, usuarioModificacion, "
            + "       componentName, prefixName, regGenericEndpoints "
            + "FROM REG_MODULO "
            + "WHERE id = ?";
        return jdbcTemplate.queryForMap(sql, idModulo);
    }

    @Override
    public void crearModulo(Map<String, Object> datos) {
        String sql = ""
            + "INSERT INTO REG_MODULO "
            + "("
            + "  descripcion, estado, componentName, prefixName, regGenericEndpoints, "
            + "  fechaCreacion, usuarioCreacion"
            + ") VALUES ("
            + "  ?, ?, ?, ?, ?, "
            + "  SYSDATE, ?"
            + ")";
        jdbcTemplate.update(
            sql,
            datos.get("nombre"),
            datos.get("estado"),
            datos.get("componentName"),
            datos.get("prefixName"),
            datos.get("regGenericEndpoints"),  // 'S' o 'N'
            datos.get("usuarioCreacion")
        );
    }

    @Override
    public void actualizarModulo(Map<String, Object> datos) {
        String sql = ""
            + "UPDATE REG_MODULO SET "
            + "  descripcion         = ?, "
            + "  estado              = ?, "
            + "  componentName       = ?, "
            + "  prefixName          = ?, "
            + "  regGenericEndpoints = ?, "
            + "  fechaModificacion   = SYSDATE, "
            + "  usuarioModificacion = ? "
            + "WHERE id = ?";
        jdbcTemplate.update(
            sql,
            datos.get("nombre"),
            datos.get("estado"),
            datos.get("componentName"),
            datos.get("prefixName"),
            datos.get("regGenericEndpoints"),
            datos.get("usuarioModificacion"),
            datos.get("id")
        );
    }

    @Override
    public void bajaModulo(long idModulo) {
        String sql = ""
            + "UPDATE REG_MODULO SET "
            + "  estado             = 'INACTIVO', "
            + "  fechaModificacion  = SYSDATE, "
            + "  usuarioModificacion = 'SYSTEM' "
            + "WHERE id = ?";
        jdbcTemplate.update(sql, idModulo);
    }

    // ---------------------------------------
    // REG_GENERIC_ENDPOINTS
    // ---------------------------------------

    @Override
    public List<Map<String, Object>> listarGenericEndpointsPorModuloYEco(long idModulo, String nombreEcosistema) {
        String sql = ""
            + "SELECT g.id, g.descripcion, g.discriminador, g.estado, "
            + "       TO_CHAR(g.fechaCreacion,'YYYY-MM-DD HH24:MI:SS')    AS fechaCreacion, "
            + "       TO_CHAR(g.fechaModificacion,'YYYY-MM-DD HH24:MI:SS')AS fechaModificacion, "
            + "       g.usuarioCreacion, g.usuarioModificacion, "
            + "       g.valor, "
            + "       g.idEcosistema, "
            + "       g.idModulo "
            + "FROM REG_GENERIC_ENDPOINTS g "
            + "JOIN REG_ECOSISTEMA e ON g.idEcosistema = e.id "
            + "WHERE g.idModulo = ? AND e.descripcionEcosistema = ?";
        return jdbcTemplate.queryForList(sql, idModulo, nombreEcosistema);
    }

    @Override
    public Map<String, Object> obtenerGenericEndpointPorId(long idEndpoint) {
        String sql = ""
            + "SELECT g.id, g.descripcion, g.discriminador, g.estado, "
            + "       TO_CHAR(g.fechaCreacion,'YYYY-MM-DD HH24:MI:SS')    AS fechaCreacion, "
            + "       TO_CHAR(g.fechaModificacion,'YYYY-MM-DD HH24:MI:SS')AS fechaModificacion, "
            + "       g.usuarioCreacion, g.usuarioModificacion, "
            + "       g.valor, "
            + "       g.idEcosistema, "
            + "       g.idModulo "
            + "FROM REG_GENERIC_ENDPOINTS g "
            + "WHERE g.id = ?";
        return jdbcTemplate.queryForMap(sql, idEndpoint);
    }

    @Override
    public void crearGenericEndpoint(Map<String, Object> datos) {
        String sql = ""
            + "INSERT INTO REG_GENERIC_ENDPOINTS "
            + "("
            + "  descripcion, discriminador, estado, valor, "
            + "  idEcosistema, idModulo, fechaCreacion, usuarioCreacion"
            + ") VALUES ("
            + "  ?, ?, ?, ?, "
            + "  (SELECT id FROM REG_ECOSISTEMA WHERE descripcionEcosistema = ?), "
            + "  ?, SYSDATE, ?"
            + ")";
        jdbcTemplate.update(
            sql,
            datos.get("descripcion"),
            datos.get("discriminador"),
            datos.get("estado"),
            datos.get("valor"),
            datos.get("nombreEcosistema"),
            datos.get("idModulo"),
            datos.get("usuarioCreacion")
        );
    }

    @Override
    public void actualizarGenericEndpoint(Map<String, Object> datos) {
        String sql = ""
            + "UPDATE REG_GENERIC_ENDPOINTS SET "
            + "  descripcion          = ?, "
            + "  discriminador        = ?, "
            + "  estado               = ?, "
            + "  valor                = ?, "
            + "  fechaModificacion    = SYSDATE, "
            + "  usuarioModificacion  = ? "
            + "WHERE id = ?";
        jdbcTemplate.update(
            sql,
            datos.get("descripcion"),
            datos.get("discriminador"),
            datos.get("estado"),
            datos.get("valor"),
            datos.get("usuarioModificacion"),
            datos.get("id")
        );
    }

    @Override
    public void bajaGenericEndpoint(long idEndpoint) {
        String sql = ""
            + "UPDATE REG_GENERIC_ENDPOINTS SET "
            + "  estado             = 'INACTIVO', "
            + "  fechaModificacion  = SYSDATE, "
            + "  usuarioModificacion = 'SYSTEM' "
            + "WHERE id = ?";
        jdbcTemplate.update(sql, idEndpoint);
    }

    // ---------------------------------------
    // REG_ENDPOINT_DOMINIO
    // ---------------------------------------

    @Override
    public List<Map<String, Object>> listarEndpointDominioPorEcosistema(String nombreEcosistema) {
        String sql = ""
            + "SELECT ed.id, ed.estado, "
            + "       TO_CHAR(ed.fechaCreacion,'YYYY-MM-DD HH24:MI:SS')    AS fechaCreacion, "
            + "       TO_CHAR(ed.fechaModificacion,'YYYY-MM-DD HH24:MI:SS')AS fechaModificacion, "
            + "       ed.usuarioCreacion, ed.usuarioModificacion, "
            + "       ed.valorEndpoint, "
            + "       ed.idDominioEcosistema, "
            + "       d.descripcionDominio AS nombreDominio, "
            + "       ed.idModulo, "
            + "       m.descripcion AS nombreModulo, "
            + "       ed.discriminador, "
            + "       ed.regGenericEndpoints AS regGenericEndpoints "
            + "FROM REG_ENDPOINT_DOMINIO ed "
            + "JOIN REG_DOMINIO_ECOSISTEMA d ON ed.idDominioEcosistema = d.id "
            + "JOIN REG_ECOSISTEMA e      ON d.idEcosistema = e.id "
            + "JOIN REG_MODULO m          ON ed.idModulo = m.id "
            + "WHERE e.descripcionEcosistema = ?";
        return jdbcTemplate.queryForList(sql, nombreEcosistema);
    }

    @Override
    public Map<String, Object> obtenerEndpointDominioPorId(long idEndpointDominio) {
        String sql = ""
            + "SELECT ed.id, ed.estado, "
            + "       TO_CHAR(ed.fechaCreacion,'YYYY-MM-DD HH24:MI:SS')    AS fechaCreacion, "
            + "       TO_CHAR(ed.fechaModificacion,'YYYY-MM-DD HH24:MI:SS')AS fechaModificacion, "
            + "       ed.usuarioCreacion, ed.usuarioModificacion, "
            + "       ed.valorEndpoint, "
            + "       ed.idDominioEcosistema, "
            + "       d.descripcionDominio AS nombreDominio, "
            + "       ed.idModulo, "
            + "       m.descripcion AS nombreModulo, "
            + "       ed.discriminador, "
            + "       ed.regGenericEndpoints AS regGenericEndpoints "
            + "FROM REG_ENDPOINT_DOMINIO ed "
            + "JOIN REG_DOMINIO_ECOSISTEMA d ON ed.idDominioEcosistema = d.id "
            + "JOIN REG_MODULO m          ON ed.idModulo = m.id "
            + "WHERE ed.id = ?";
        return jdbcTemplate.queryForMap(sql, idEndpointDominio);
    }

    @Override
    public void crearEndpointDominio(Map<String, Object> datos) {
        String sql = ""
            + "INSERT INTO REG_ENDPOINT_DOMINIO "
            + "("
            + "  estado, valorEndpoint, idDominioEcosistema, idModulo, discriminador, "
            + "  fechaCreacion, usuarioCreacion, regGenericEndpoints"
            + ") VALUES ("
            + "  ?, ?, ?, ?, ?, "
            + "  SYSDATE, ?, ?"
            + ")";
        jdbcTemplate.update(
            sql,
            datos.get("estado"),
            datos.get("valorEndpoint"),
            datos.get("idDominioEcosistema"),
            datos.get("idModulo"),
            datos.get("discriminador"),
            datos.get("usuarioCreacion"),
            datos.get("regGenericEndpoints")
        );
    }

    @Override
    public void actualizarEndpointDominio(Map<String, Object> datos) {
        String sql = ""
            + "UPDATE REG_ENDPOINT_DOMINIO SET "
            + "  estado             = ?, "
            + "  valorEndpoint      = ?, "
            + "  fechaModificacion  = SYSDATE, "
            + "  usuarioModificacion = ?, "
            + "  regGenericEndpoints = ? "
            + "WHERE id = ?";
        jdbcTemplate.update(
            sql,
            datos.get("estado"),
            datos.get("valorEndpoint"),
            datos.get("usuarioModificacion"),
            datos.get("regGenericEndpoints"),
            datos.get("id")
        );
    }

    @Override
    public void bajaEndpointDominio(long idEndpointDominio) {
        String sql = ""
            + "UPDATE REG_ENDPOINT_DOMINIO SET "
            + "  estado             = 'INACTIVO', "
            + "  fechaModificacion  = SYSDATE, "
            + "  usuarioModificacion = 'SYSTEM' "
            + "WHERE id = ?";
        jdbcTemplate.update(sql, idEndpointDominio);
    }

    @Override
    public Map<String, Object> probarConectividad(String nombreEcosistema) {
        // 1) Recuperar el valor(baseUrl) de un dominio “PRINCIPAL” para ese ecosistema:
        String sqlDom = ""
            + "SELECT d.valor "
            + "FROM REG_DOMINIO_ECOSISTEMA d "
            + "JOIN REG_ECOSISTEMA e ON d.idEcosistema = e.id "
            + "WHERE e.descripcionEcosistema = ? "
            + "  AND d.tipoDominio = 'PRINCIPAL'";
        List<Map<String, Object>> filas = jdbcTemplate.queryForList(sqlDom, nombreEcosistema);

        Map<String, Object> resultado = new HashMap<>();
        if (filas.isEmpty()) {
            resultado.put("ok", false);
            resultado.put("mensaje", "No hay dominio PRINCIPAL configurado para " + nombreEcosistema);
            return resultado;
        }

        // Tomamos el primer dominio “PRINCIPAL”
        String baseUrl = (String) filas.get(0).get("valor");
        try {
            // Armar la URL de prueba:
            String pruebaUrl = baseUrl;
            if (!pruebaUrl.endsWith("/")) {
                pruebaUrl += "/";
            }
            pruebaUrl += "Interoperabilidad/informacion-documento";

            URL url = new URL(pruebaUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);       // 5 segundos timeout de conexión
            con.setReadTimeout(5000);
            con.setRequestMethod("GET");
            int respuesta = con.getResponseCode();

            if (respuesta >= 200 && respuesta < 300) {
                resultado.put("ok", true);
                resultado.put("mensaje", "Conectividad OK (HTTP " + respuesta + ")");
            } else {
                resultado.put("ok", false);
                resultado.put("mensaje", "HTTP " + respuesta + " al llamar a " + pruebaUrl);
            }
            con.disconnect();
        } catch (Exception ex) {
            resultado.put("ok", false);
            resultado.put("mensaje", "Error de conexión: " + ex.getMessage());
        }
        return resultado;
    }

    // ---------------------------------------
    // PROPERTY_CONFIGURATION
    // ---------------------------------------

    @Override
    public List<Map<String, Object>> listarTodasPropiedades() {
        String sql = ""
            + "SELECT clave, valor, configuracion, "
            + "       TO_CHAR(fechaCreacion,'YYYY-MM-DD HH24:MI:SS')    AS fechaCreacion, "
            + "       TO_CHAR(fechaModificacion,'YYYY-MM-DD HH24:MI:SS')AS fechaModificacion, "
            + "       usuarioCreacion, usuarioModificacion "
            + "FROM PROPERTY_CONFIGURATION";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public Map<String, Object> obtenerPropiedadPorClave(String clave) {
        String sql = ""
            + "SELECT clave, valor, configuracion, "
            + "       TO_CHAR(fechaCreacion,'YYYY-MM-DD HH24:MI:SS')    AS fechaCreacion, "
            + "       TO_CHAR(fechaModificacion,'YYYY-MM-DD HH24:MI:SS')AS fechaModificacion, "
            + "       usuarioCreacion, usuarioModificacion "
            + "FROM PROPERTY_CONFIGURATION "
            + "WHERE clave = ?";
        return jdbcTemplate.queryForMap(sql, clave);
    }

    @Override
    public void upsertPropiedadConfiguracion(Map<String, Object> datos) {
        String sqlExiste = ""
            + "SELECT COUNT(1) FROM PROPERTY_CONFIGURATION WHERE clave = ?";
        Integer count = jdbcTemplate.queryForObject(sqlExiste, new Object[]{ datos.get("clave") }, Integer.class);

        if (count != null && count > 0) {
            String sqlUpdate = ""
                + "UPDATE PROPERTY_CONFIGURATION SET "
                + "  valor             = ?, "
                + "  configuracion     = ?, "
                + "  fechaModificacion = SYSDATE, "
                + "  usuarioModificacion = ? "
                + "WHERE clave = ?";
            jdbcTemplate.update(
                sqlUpdate,
                datos.get("valor"),
                datos.get("configuracion"),
                datos.get("usuarioModificacion"),
                datos.get("clave")
            );
        } else {
            String sqlInsert = ""
                + "INSERT INTO PROPERTY_CONFIGURATION "
                + "(clave, valor, configuracion, fechaCreacion, usuarioCreacion) "
                + "VALUES (?, ?, ?, SYSDATE, ?)";
            jdbcTemplate.update(
                sqlInsert,
                datos.get("clave"),
                datos.get("valor"),
                datos.get("configuracion"),
                datos.get("usuarioCreacion")
            );
        }
    }
}
