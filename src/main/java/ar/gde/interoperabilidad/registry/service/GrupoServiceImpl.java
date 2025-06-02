// src/main/java/ar/gde/interoperabilidad/registry/service/impl/GrupoServiceImpl.java
package ar.gde.interoperabilidad.registry.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import ar.gde.interoperabilidad.registry.service.GrupoService;

@Service("grupoService")
public class GrupoServiceImpl implements GrupoService {

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbc;

    @PostConstruct
    public void init() {
        this.jdbc = new JdbcTemplate(dataSource);
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
        return jdbc.queryForList(sql);
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
        return jdbc.queryForMap(sql, idGrupo);
    }

    @Override
    public void crearGrupo(Map<String, Object> datos) {
        String sql = ""
            + "INSERT INTO REG_GRUPO "
            + "(descripcionGrupo, estadoGrupo, fechaCreacion, usuarioCreacion) "
            + "VALUES (?, ?, SYSDATE, ?)";
        jdbc.update(
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
        jdbc.update(
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
        jdbc.update(sql1, idGrupo);

        // 2) Marcar todos los ecosistemas de ese grupo como INACTIVOS
        String sql2 = ""
            + "UPDATE REG_ECOSISTEMA SET "
            + "  estado             = 'INACTIVO', "
            + "  fechaModificacion   = SYSDATE, "
            + "  usuarioModificacion = 'SYSTEM' "
            + "WHERE regGrupo = ?";
        jdbc.update(sql2, idGrupo);
    }

    @Override
    public void altaGrupo(long idGrupo, String usuarioModificacion) {
        // Al “dar de alta” solo reactivamos el grupo; los ecosistemas quedan tal cual.
        String sql = ""
            + "UPDATE REG_GRUPO SET "
            + "  estadoGrupo         = 'ACTIVO', "
            + "  fechaModificacion   = SYSDATE, "
            + "  usuarioModificacion = ? "
            + "WHERE id = ?";
        jdbc.update(sql, usuarioModificacion, idGrupo);
    }

    @Override
    public void altaEcosistema(String nombreEcosistema, String usuarioModificacion) {
        String sql = ""
            + "UPDATE REG_ECOSISTEMA SET "
            + "  estado             = 'ACTIVO', "
            + "  fechaModificacion   = SYSDATE, "
            + "  usuarioModificacion = ? "
            + "WHERE descripcionEcosistema = ?";
        jdbc.update(sql, usuarioModificacion, nombreEcosistema);
    }
}
