package ar.gde.interoperabilidad.registry.zk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Window;
import ar.gde.interoperabilidad.registry.service.EcosistemaService;
import ar.gde.interoperabilidad.registry.service.GrupoService;
import ar.gde.interoperabilidad.registry.service.impl.EcosistemaServiceImpl;
import ar.gde.interoperabilidad.registry.service.impl.GrupoServiceImpl;

public class ABMEcosistemaComposer extends SelectorComposer<Component> {

    private EcosistemaService ecosistemaService = new EcosistemaServiceImpl();
    private GrupoService      grupoService      = new GrupoServiceImpl();

    private List<Map<String, Object>> listaEcosistemas;
    private Map<String, Object> ecoSeleccionado;
    private Map<String, Object> nuevoEcosistema;
    private List<String> listaEstados;
    private List<Map<String, Object>> listaGrupos;

    @Wire("#winVerEcosistema")
    private Window winVerEcosistema;

    @Wire("#winEditEcosistema")
    private Window winEditEcosistema;

    @Wire("#winAddEcosistema")
    private Window winAddEcosistema;

    @Init
    @NotifyChange({ "listaEcosistemas", "listaEstados", "listaGrupos" })
    public void init() {
        listaEcosistemas = ecosistemaService.listarTodosEcosistemas();
        listaEstados     = Arrays.asList("ACTIVO", "INACTIVO");
        listaGrupos      = grupoService.listarTodosGrupos();
    }

    public List<Map<String, Object>> getListaEcosistemas() {
        return listaEcosistemas;
    }

    public Map<String, Object> getEcoSeleccionado() {
        return ecoSeleccionado;
    }

    public Map<String, Object> getNuevoEcosistema() {
        return nuevoEcosistema;
    }

    public List<String> getListaEstados() {
        return listaEstados;
    }

    public List<Map<String, Object>> getListaGrupos() {
        return listaGrupos;
    }

    // -----------------------  
    // AGREGAR ECSITEMA (pop-up)
    // -----------------------
    @Command("agregarEcosistema")
    @NotifyChange("nuevoEcosistema")
    public void agregarEcosistema() {
        nuevoEcosistema = new HashMap<>();
        nuevoEcosistema.put("estado", "ACTIVO");
        nuevoEcosistema.put("version", 1);
        winAddEcosistema.doModal();
    }

    @Command("guardarNuevoEcosistema")
    @NotifyChange("listaEcosistemas")
    public void guardarNuevoEcosistema() {
        String nombre = (String) nuevoEcosistema.get("nombre");
        if (nombre == null || nombre.trim().isEmpty()) {
            Clients.showNotification("El campo Nombre es obligatorio.",
                                     "warning", null, null, 2000);
            return;
        }
        ecosistemaService.crearEcosistema(nuevoEcosistema);
        listaEcosistemas = ecosistemaService.listarTodosEcosistemas();
        winAddEcosistema.setVisible(false);
    }

    @Command("cerrarAgregar")
    public void cerrarAgregar() {
        winAddEcosistema.setVisible(false);
    }

    // -----------------------  
    // VER ECSITEMA (pop-up)
    // -----------------------
    @Command("verEcosistema")
    public void verEcosistema(@BindingParam("item") Map<String, Object> each) {
        ecoSeleccionado = each;
        winVerEcosistema.doModal();
    }

    @Command("cerrarVer")
    public void cerrarVer() {
        winVerEcosistema.setVisible(false);
    }

    // -----------------------  
    // EDITAR ECSITEMA (pop-up)
    // -----------------------
    @Command("editarEcosistema")
    public void editarEcosistema(@BindingParam("item") Map<String, Object> each) {
        ecoSeleccionado = new HashMap<>(each);
        winEditEcosistema.doModal();
    }

    @Command("guardarEcosistema")
    @NotifyChange("listaEcosistemas")
    public void guardarEcosistema() {
        String nombre = (String) ecoSeleccionado.get("nombre");
        if (nombre == null || nombre.trim().isEmpty()) {
            Clients.showNotification("El campo Nombre es obligatorio.",
                                     "warning", null, null, 2000);
            return;
        }
        ecosistemaService.actualizarEcosistema(ecoSeleccionado);
        listaEcosistemas = ecosistemaService.listarTodosEcosistemas();
        winEditEcosistema.setVisible(false);
    }

    @Command("cerrarEditar")
    public void cerrarEditar() {
        winEditEcosistema.setVisible(false);
    }

    // -----------------------  
    // DAR DE BAJA / ALTA
    // -----------------------
    @Command("bajaEcosistema")
    @NotifyChange("listaEcosistemas")
    public void bajaEcosistema(@BindingParam("item") Map<String, Object> each) {
        each.put("estado", "INACTIVO");
        ecosistemaService.actualizarEcosistema(each);
        listaEcosistemas = ecosistemaService.listarTodosEcosistemas();
    }

    @Command("altaEcosistema")
    @NotifyChange("listaEcosistemas")
    public void altaEcosistema(@BindingParam("item") Map<String, Object> each) {
        each.put("estado", "ACTIVO");
        ecosistemaService.actualizarEcosistema(each);
        listaEcosistemas = ecosistemaService.listarTodosEcosistemas();
    }

    // -----------------------  
    // PROBAR ECSITEMA (placeholder)
    // -----------------------
    @Command("probarEcosistema")
    public void probarEcosistema(@BindingParam("item") Map<String, Object> each) {
        Clients.showNotification("Funcionalidad 'Probar' en desarrollo.",
                                 "info", null, null, 1500);
    }
}
