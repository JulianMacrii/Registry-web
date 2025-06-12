// src/main/java/ar/gde/interoperabilidad/registry/zk/ABMGrupoComposer.java
package ar.gde.interoperabilidad.registry.zk;

import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zul.*;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.BindingParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ABMGrupoComposer extends SelectorComposer<Component> {

    @Wire
    private Grid gridGrupos;
    private ListModelList<Map<String, Object>> gruposModel;

    public ListModelList<Map<String, Object>> getGruposModel() {
        return gruposModel;
    }
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
    private List<Map<String, Object>> grupos;
    private Map<String, Object> nuevoGrupo = new HashMap<>();
    private Map<String, Object> grupoEnEdicion = new HashMap<>();
    // URL base del servicio REST
    private final String BASE_URL = "http://localhost:8080/registry-web/api/v1";
    private final Client jaxClient = ClientBuilder.newClient();
    private List<String> listaEstados = Arrays.asList("ACTIVO", "INACTIVO");

    public List<Map<String, Object>> getGrupos() {
        return grupos;
    }

    public Map<String, Object> getNuevoGrupo() {
        return nuevoGrupo;
    }

    public Map<String, Object> getGrupoEnEdicion() {
        return grupoEnEdicion;
    }
    public List<String> getListaEstados() {
        return listaEstados;
    }
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        org.zkoss.zk.ui.select.Selectors.wireComponents(comp, this, false);
        grupos = jaxClient.target(BASE_URL).path("grupos").request().get(List.class);

    }

    /**
     * 1) Cargar la grilla con GET /grupos
     */
        // --- Abrir Popup Agregar ---
    @Command
    @NotifyChange("nuevoGrupo")
    public void abrirAgregarGrupo(@ContextParam(ContextType.VIEW) Component view) {
        nuevoGrupo.clear();
        Window win = (Window) view.getFellow("winAddGrupo");
        win.setVisible(true);
        win.doModal();
    }

    // --- Guardar Nuevo Grupo ---
    @Command
    @NotifyChange({"grupos"})
    public void guardarNuevoGrupo(@ContextParam(ContextType.VIEW) Component view) {
        if (nuevoGrupo.get("nombre") == null || nuevoGrupo.get("estado") == null) {
            Messagebox.show("El nombre y estado son obligatorios.", "Advertencia", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        nuevoGrupo.put("usuarioCreacion", "USUARIO_LOGUEADO");

        WebTarget t = jaxClient.target(BASE_URL).path("grupos");
        t.request().post(Entity.json(nuevoGrupo));

        grupos = jaxClient.target(BASE_URL).path("grupos").request().get(List.class);

        Clients.showNotification("Grupo creado correctamente.", "info", null, "top_center", 2000);

        Window win = (Window) view.getFellow("winAddGrupo");
        win.setVisible(false);
    }

    // --- Abrir Edición ---
    @Command
    @NotifyChange("grupoEnEdicion")
    public void editarGrupo(@BindingParam("item") Map<String, Object> item,
                            @ContextParam(ContextType.COMPONENT) Component comp) {
        grupoEnEdicion = new HashMap<>(item);
        comp.getPage().getFellow("winEditGrupo").setVisible(true);
    }

    // --- Guardar Edición ---
    @Command
    @NotifyChange("grupos")
    public void guardarEditGrupo(@ContextParam(ContextType.COMPONENT) Component comp) {
        if (grupoEnEdicion.get("nombre") == null || grupoEnEdicion.get("estado") == null) {
            Messagebox.show("El nombre y estado son obligatorios.", "Advertencia", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        grupoEnEdicion.put("usuarioModificacion", "USUARIO_LOGUEADO");
        Long id = ((Number) grupoEnEdicion.get("id")).longValue();
        WebTarget t = jaxClient.target(BASE_URL).path("grupos").path(String.valueOf(id));
        t.request().put(Entity.json(grupoEnEdicion));

        grupos = jaxClient.target(BASE_URL).path("grupos").request().get(List.class);
        Clients.showNotification("Datos actualizados.", "success", null, "top_center", 2000);
        comp.getPage().getFellow("winEditGrupo").setVisible(false);
    }

    // --- Dar de baja ---
    @Command
    @NotifyChange("grupos")
    public void bajaGrupo(@BindingParam("item") Map<String, Object> item) {
        Long id = ((Number) item.get("id")).longValue();
        jaxClient.target(BASE_URL).path("grupos").path(String.valueOf(id)).request().delete();
        grupos = jaxClient.target(BASE_URL).path("grupos").request().get(List.class);
        Clients.showNotification("Grupo dado de baja.", "info", null, "top_center", 2000);
    }

    // --- Dar de alta ---
    @Command
    @NotifyChange("grupos")
    public void altaGrupo(@BindingParam("item") Map<String, Object> item) {
        Long id = ((Number) item.get("id")).longValue();
        jaxClient.target(BASE_URL).path("grupos").path(String.valueOf(id)).path("alta")
                .request().header("usuario", "USUARIO_LOGUEADO").post(null);
        grupos = jaxClient.target(BASE_URL).path("grupos").request().get(List.class);
        Clients.showNotification("Grupo reactivado.", "info", null, "top_center", 2000);
    }

    
}
