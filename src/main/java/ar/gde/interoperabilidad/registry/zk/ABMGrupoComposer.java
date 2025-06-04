// src/main/java/ar/gde/interoperabilidad/registry/zk/ABMGrupoComposer.java
package ar.gde.interoperabilidad.registry.zk;

import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zul.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ABMGrupoComposer extends SelectorComposer<Component> {

    @Wire
    private Grid gridGrupos;
    private ListModelList<Map<String, Object>> gruposModel;

    // Ventanas y campos para “Agregar” y “Editar”
    @Wire
    private Window winAddGrupo;
    @Wire
    private Window winEditGrupo;

    // Campos “Agregar Grupo”
    @Wire
    private Textbox txtNombreAddG;
    @Wire
    private Textbox txtDescAddG;
    @Wire
    private Combobox cmbEstadoAddG;

    // Campos “Editar Grupo”
    @Wire
    private Textbox txtNombreEditG;
    @Wire
    private Textbox txtDescEditG;
    @Wire
    private Combobox cmbEstadoEditG;

    // Id del grupo que se está editando
    private Long idGrupoEnEdicion;

    // URL base del servicio REST
    private final String BASE_URL = "http://localhost:8080/registry-web/api/v1";
    private final Client jaxClient = ClientBuilder.newClient();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        cargarGrillaGrupos();
    }

    /**
     * 1) Cargar la grilla con GET /grupos
     */
    private void cargarGrillaGrupos() {
        WebTarget t = jaxClient.target(BASE_URL).path("grupos");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lista = t.request().get(List.class);
        gruposModel = new ListModelList<>(lista);
        gridGrupos.setModel(gruposModel);
    }

    /**
     * 2) Abrir ventana “Agregar Grupo”
     */
    @Listen("onClick = button#btnAbrirAddG")
    public void abrirAgregarGrupo() {
        // Limpiar campos antes de mostrar
        txtNombreAddG.setValue("");
        txtDescAddG.setValue("");
        cmbEstadoAddG.setValue("");
        winAddGrupo.doModal();
    }

    /**
     * 3) Guardar nuevo grupo → POST /grupos
     */
    @Listen("onClick = button#btnGuardarAddG")
    public void guardarNuevoGrupo() {
        String nombre = txtNombreAddG.getValue().trim();
        String descripcion = txtDescAddG.getValue().trim();
        String estado = cmbEstadoAddG.getValue();

        if (nombre.isEmpty() || estado == null || estado.isEmpty()) {
            Clients.showNotification("El nombre y estado son obligatorios.", "warning", null, "top_center", 2000);
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("nombre", nombre);
        payload.put("descripcionGrupo", descripcion);
        payload.put("estado", estado);
        payload.put("usuarioCreacion", "USUARIO_LOGUEADO");

        WebTarget t = jaxClient.target(BASE_URL).path("grupos");
        t.request()
         .post(Entity.json(payload));

        Clients.showNotification("Grupo creado correctamente.", "success", null, "top_center", 2000);
        winAddGrupo.onClose();
        cargarGrillaGrupos();
    }

    /**
     * 4) Dar de baja un grupo → DELETE /grupos/{id}
     */
    @Listen("onClick = button#btnBajaG")
    public void bajaGrupo(ForwardEvent evt) {
        Row fila = (Row) evt.getOrigin().getTarget().getParent().getParent();
        @SuppressWarnings("unchecked")
        Map<String, Object> datos = (Map<String, Object>) fila.getValue();
        Long idGrupo = ((Number) datos.get("id")).longValue();

        WebTarget t = jaxClient.target(BASE_URL).path("grupos").path(String.valueOf(idGrupo));
        t.request().delete();

        Clients.showNotification("Grupo dado de baja.", "info", null, "top_center", 2000);
        cargarGrillaGrupos();
    }

    /**
     * 5) Dar de alta un grupo → POST /grupos/{id}/alta
     */
    @Listen("onClick = button#btnAltaG")
    public void altaGrupo(ForwardEvent evt) {
        Row fila = (Row) evt.getOrigin().getTarget().getParent().getParent();
        @SuppressWarnings("unchecked")
        Map<String, Object> datos = (Map<String, Object>) fila.getValue();
        Long idGrupo = ((Number) datos.get("id")).longValue();

        WebTarget t = jaxClient.target(BASE_URL)
                               .path("grupos")
                               .path(String.valueOf(idGrupo))
                               .path("alta");
        t.request()
         .header("usuario", "USUARIO_LOGUEADO")
         .post(null);

        Clients.showNotification("Grupo reactivado.", "info", null, "top_center", 2000);
        cargarGrillaGrupos();
    }

    /**
     * 6) Abrir ventana “Editar Grupo” con datos cargados
     */
    @Listen("onClick = button#btnEditarG")
    public void editarGrupo(ForwardEvent evt) {
        Row fila = (Row) evt.getOrigin().getTarget().getParent().getParent();
        @SuppressWarnings("unchecked")
        Map<String, Object> datos = (Map<String, Object>) fila.getValue();
        idGrupoEnEdicion = ((Number) datos.get("id")).longValue();

        txtNombreEditG.setValue((String) datos.get("nombre"));
        txtDescEditG.setValue((String) datos.get("descripcionGrupo"));
        cmbEstadoEditG.setValue((String) datos.get("estado"));

        winEditGrupo.doModal();
    }

    /**
     * 7) Guardar cambios edición → PUT /grupos/{id}
     */
    @Listen("onClick = button#btnGuardarEditG")
    public void guardarEditGrupo() {
        String nombre = txtNombreEditG.getValue().trim();
        String descripcion = txtDescEditG.getValue().trim();
        String estado = cmbEstadoEditG.getValue();

        if (nombre.isEmpty() || estado == null || estado.isEmpty()) {
            Clients.showNotification("El nombre y estado son obligatorios.", "warning", null, "top_center", 2000);
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", idGrupoEnEdicion);
        payload.put("nombre", nombre);
        payload.put("descripcionGrupo", descripcion);
        payload.put("estado", estado);
        payload.put("usuarioModificacion", "USUARIO_LOGUEADO");

        WebTarget t = jaxClient.target(BASE_URL).path("grupos").path(String.valueOf(idGrupoEnEdicion));
        t.request().put(Entity.json(payload));

        Clients.showNotification("Datos actualizados.", "success", null, "top_center", 2000);
        winEditGrupo.onClose();
        cargarGrillaGrupos();
    }
}
