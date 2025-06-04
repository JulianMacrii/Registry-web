// src/main/java/ar/gde/interoperabilidad/registry/zk/ABMGrupoComposer.java
package ar.gde.interoperabilidad.registry.zk;

import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;
import org.zkoss.zk.ui.event.ForwardEvent;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ABMGrupoComposer extends SelectorComposer<Component> {

    @Wire
    private Grid gridGrupos;
    private ListModelList<Map<String, Object>> gruposModel;

    // Ventanas / Campos
    @Wire
    private Window winVerGrupo;
    @Wire
    private Window winEditGrupo;
    @Wire
    private Textbox txtNombreVerG, txtDescripcionVerG, txtEstadoVerG,
                    txtFechaCreacionVerG, txtFechaModVerG,
                    txtUserCrearVerG, txtUserModVerG;

    @Wire
    private Textbox txtNombreEditG, txtDescripcionEditG;
    @Wire
    private Combobox cmbEstadoEditG;
    @Wire
    private Button btnGuardarEditG;

    // URL base del servicio REST
    private final String BASE_URL = "http://localhost:8080/registry-web/api/v1";
    private final Client jaxClient = ClientBuilder.newClient();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        cargarGrillaGrupos();
    }

    /**
     * Llama al REST GET /grupos y carga la grilla.
     */
    private void cargarGrillaGrupos() {
        WebTarget t = jaxClient.target(BASE_URL).path("grupos");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lista = t.request().get(List.class);
        gruposModel = new ListModelList<>(lista);
        gridGrupos.setModel(gruposModel);
    }

    /**
     * Mostrar ventana “Ver Grupo” y cargar los datos.
     */
    @Listen("onClick = button#btnVerG")
    public void verGrupo(ForwardEvent evt) {
        Row fila = (Row) evt.getOrigin().getTarget().getParent().getParent();
        @SuppressWarnings("unchecked")
        Map<String, Object> datos = (Map<String, Object>) fila.getValue();

        txtNombreVerG.setValue((String) datos.get("nombre"));
        txtDescripcionVerG.setValue((String) datos.get("descripcionGrupo"));
        txtEstadoVerG.setValue((String) datos.get("estado"));
        txtFechaCreacionVerG.setValue((String) datos.get("fechaCreacion"));
        txtFechaModVerG.setValue((String) datos.get("fechaModificacion"));
        txtUserCrearVerG.setValue((String) datos.get("usuarioCreacion"));
        txtUserModVerG.setValue((String) datos.get("usuarioModificacion"));

        winVerGrupo.doModal();
    }

    /**
     * Botón “Dar de baja” → DELETE /grupos/{id}
     */
    @Listen("onClick = button#btnBajaG")
    public void bajaGrupo(ForwardEvent evt) {
        Row fila = (Row) evt.getOrigin().getTarget().getParent().getParent();
        @SuppressWarnings("unchecked")
        Map<String, Object> datos = (Map<String, Object>) fila.getValue();
        Long idGrupo = ((Number) datos.get("id")).longValue();

        WebTarget t = jaxClient.target(BASE_URL)
                               .path("grupos")
                               .path(String.valueOf(idGrupo));
        t.request().delete();

        Clients.showNotification("Grupo dado de baja.", "info", null, "top_center", 2000);
        cargarGrillaGrupos();
    }

    /**
     * Botón “Dar de alta” → POST /grupos/{id}/alta
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
         .header("usuario", "USUARIO_LOGUEADO")  // para pruebas
         .post(null);

        Clients.showNotification("Grupo reactivado.", "info", null, "top_center", 2000);
        cargarGrillaGrupos();
    }

    /**
     * Botón “Editar” → abre la ventana y completa campos con los valores del mapa
     */
    @Listen("onClick = button#btnEditarG")
    public void editarGrupo(ForwardEvent evt) {
        Row fila = (Row) evt.getOrigin().getTarget().getParent().getParent();
        @SuppressWarnings("unchecked")
        Map<String, Object> datos = (Map<String, Object>) fila.getValue();

        txtNombreEditG.setValue((String) datos.get("nombre"));
        txtDescripcionEditG.setValue((String) datos.get("descripcionGrupo"));
        cmbEstadoEditG.setValue((String) datos.get("estado"));

        winEditGrupo.setAttribute("idGrupo", datos.get("id"));
        winEditGrupo.doModal();
    }

    /**
     * Botón “Guardar” de la ventana de edición → hace PUT /grupos/{id}
     */
    @Listen("onClick = button#btnGuardarEditG")
    public void guardarEditGrupo() {
        Long idGrupo = (Long) winEditGrupo.getAttribute("idGrupo");

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", idGrupo);
        // La REST espera “descripcionGrupo” en BD, pero en nuestro servicio lo abrimos como “nombre”
        payload.put("nombre", txtNombreEditG.getValue());
        payload.put("estado", cmbEstadoEditG.getValue());
        payload.put("usuarioModificacion", "USUARIO_LOGUEADO");

        WebTarget t = jaxClient.target(BASE_URL)
                               .path("grupos")
                               .path(String.valueOf(idGrupo));
        t.request()
         .put(javax.ws.rs.client.Entity.json(payload));

        Clients.showNotification("Datos guardados.", "success", null, "top_center", 2000);
        winEditGrupo.onClose();
        cargarGrillaGrupos();
    }
}
