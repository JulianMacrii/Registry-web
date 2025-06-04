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

    /**
     * 1) Listar todos los grupos (GET /grupos)
     *    Devuelve id, nombre (alias de descripcionGrupo), estado, fechas y usuarios.
     */
    @Override
    public List<Map<String, Object>> listarTodosGrupos() {
        String sql = ""
            + "SELECT id, "
            + "       descripcionGrupo AS nombre, "
            + "       estadoGrupo       AS estado, "
            + "       TO_CHAR(fechaCreacion,    'YYYY-MM-DD HH24:MI:SS') AS fechaCreacion, "
            + "       TO_CHAR(fechaModificacion,'YYYY-MM-DD HH24:MI:SS') AS fechaModificacion, "
            + "       usuarioCreacion, "
            + "       usuarioModificacion "
            + "  FROM REG_GRUPO";
        return jdbc.queryForList(sql);
    }

    /**
     * 2) Obtener un grupo por su id (GET /grupos/{id})
     */
    @Override
    public Map<String, Object> obtenerGrupoPorId(long idGrupo) {
        String sql = ""
            + "SELECT id, "
            + "       descripcionGrupo AS nombre, "
            + "       estadoGrupo       AS estado, "
            + "       TO_CHAR(fechaCreacion,    'YYYY-MM-DD HH24:MI:SS') AS fechaCreacion, "
            + "       TO_CHAR(fechaModificacion,'YYYY-MM-DD HH24:MI:SS') AS fechaModificacion, "
            + "       usuarioCreacion, "
            + "       usuarioModificacion "
            + "  FROM REG_GRUPO "
            + " WHERE id = ?";
        return jdbc.queryForMap(sql, idGrupo);
    }

    /**
     * 3) Crear un nuevo grupo (POST /grupos)
     *    Se espera un Map<String,Object> con claves:
     *      - "nombre"            → se inserta en descripcionGrupo
     *      - "estado"            → se inserta en estadoGrupo
     *      - "usuarioCreacion"   → usuario que crea el registro
     *
     *    OBSERVACIÓN: Si la tabla REG_GRUPO no tiene un campo separado para 'descripción',
     *    aquí estamos usando descripcionGrupo como el “nombre” del grupo. Si más adelante
     *    deseas almacenar un texto de descripción distinto al nombre, tendrías que agregar una
     *    columna adicional en la tabla (por ejemplo, descripcionTexto).
     */
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

    /**
     * 4) Actualizar un grupo existente (PUT /grupos/{id})
     *    Se espera un Map<String,Object> con claves:
     *      - "id"                  → id del grupo a actualizar
     *      - "nombre"              → nuevo valor para descripcionGrupo
     *      - "estado"              → nuevo valor para estadoGrupo
     *      - "usuarioModificacion" → usuario que realiza la modificación
     */
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

    /**
     * 5) Dar de baja un grupo (DELETE /grupos/{id})
     *    Aquí marcamos el grupo como INACTIVO y, opcionalmente,
     *    también podemos desactivar en cascada los ecosistemas asociados.
     */
    @Override
    public void bajaGrupo(long idGrupo) {
        // 5.1) Marcar el grupo como INACTIVO
        String sql1 = ""
            + "UPDATE REG_GRUPO SET "
            + "  estadoGrupo         = 'INACTIVO', "
            + "  fechaModificacion   = SYSDATE, "
            + "  usuarioModificacion = 'SYSTEM' "
            + "WHERE id = ?";
        jdbc.update(sql1, idGrupo);

        // 5.2) (Opcional) Desactivar todos los ecosistemas de ese grupo
        //       Si no deseas esta lógica de cascada, simplemente comenta o elimina este bloque.
        String sql2 = ""
            + "UPDATE REG_ECOSISTEMA SET "
            + "  estado             = 'INACTIVO', "
            + "  fechaModificacion   = SYSDATE, "
            + "  usuarioModificacion = 'SYSTEM' "
            + "WHERE regGrupo = ?";
        jdbc.update(sql2, idGrupo);
    }

    /**
     * 6) Dar de alta un grupo (POST /grupos/{id}/alta)
     *    Vuelve a marcar el registro como ACTIVO. No reactivamos ecosistemas en cascada.
     */
    @Override
    public void altaGrupo(long idGrupo, String usuarioModificacion) {
        String sql = ""
            + "UPDATE REG_GRUPO SET "
            + "  estadoGrupo         = 'ACTIVO', "
            + "  fechaModificacion   = SYSDATE, "
            + "  usuarioModificacion = ? "
            + "WHERE id = ?";
        jdbc.update(sql, usuarioModificacion, idGrupo);
    }
}
