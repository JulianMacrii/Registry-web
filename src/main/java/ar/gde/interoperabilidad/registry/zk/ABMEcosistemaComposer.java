// src/main/java/ar/gde/interoperabilidad/registry/zk/ABMEcosistemaComposer.java
package ar.gde.interoperabilidad.registry.zk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import ar.gde.interoperabilidad.registry.service.EcosistemaService;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ABMEcosistemaComposer {

    @WireVariable
    private EcosistemaService ecosistemaService;

    private List<Map<String, Object>> ecosistemas;
    private Map<String, Object> ecoSeleccionado;
    private Map<String, Object> nuevoEcosistema = new HashMap<>();

    // Propiedades para poblar los combobox
    private List<String> listaEstados;
    private List<Map<String, Object>> listaGrupos;

    /**
     * Se ejecuta al iniciar el zul:
     * 1) Recupera todos los ecosistemas
     * 2) Llena listaEstados con “ACTIVO” / “INACTIVO”
     * 3) Recupera todos los grupos y agrega al inicio “Sin Asignar”
     */
    @Init
    @NotifyChange("ecosistemas")
    public void init() {
        ecosistemas = ecosistemaService.listarTodosEcosistemas();
        listaEstados = Arrays.asList("ACTIVO", "INACTIVO");
        listaGrupos = ecosistemaService.listarTodosGrupos();
        Map<String, Object> sinAsignar = new HashMap<>();
        sinAsignar.put("id", null);
        sinAsignar.put("descripcionGrupo", "Sin Asignar");
        listaGrupos.add(0, sinAsignar);
    }

    // ---------- Getters para data‐binding ----------
    public List<Map<String, Object>> getEcosistemas() {
        return ecosistemas;
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

    // ---------- Abrir/Cerrar pop‐ups ----------
    @Command
    @NotifyChange("nuevoEcosistema")
    public void agregarEcosistema(@ContextParam(ContextType.COMPONENT) Component comp) {
        // Limpiamos el Map para el nuevo ecosistema
        nuevoEcosistema.clear();
        // Mostramos el popup “winAddEcosistema”
        Window win = (Window) comp.getFellowIfAny("winAddEcosistema");
        if (win != null) {
            win.setVisible(true);
            win.doModal();
        }
    }
    @Command
    public void cerrarAgregar(@ContextParam(ContextType.COMPONENT) Component comp) {
        Window win = (Window) comp.getFellowIfAny("winAddEcosistema");
        if (win != null) {
            win.setVisible(false);
        }
    }
    @Command
    public void cerrarVer(@ContextParam(ContextType.COMPONENT) Component comp) {
        Window win = (Window) comp.getFellowIfAny("winVerEcosistema");
        if (win != null) {
            win.setVisible(false);
        }
    }
    @Command
    public void cerrarEditar(@ContextParam(ContextType.COMPONENT) Component comp) {
        Window win = (Window) comp.getFellowIfAny("winEditEcosistema");
        if (win != null) {
            win.setVisible(false);
        }
    }

    // ---------- Guardar nuevo ecosistema ----------
    @Command
    @NotifyChange("ecosistemas")
    public void guardarNuevoEcosistema() {
        // Validación mínima: el campo “nombre” no puede quedar vacío
        if (nuevoEcosistema.get("nombre") == null
                || nuevoEcosistema.get("nombre").toString().trim().isEmpty()) {
            Messagebox.show("Por favor completa al menos Nombre.", "Error",
                    Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        // Map con los datos que espera el servicio
        Map<String, Object> datosParaInsert = new HashMap<>();
        datosParaInsert.put("nombre", nuevoEcosistema.get("nombre"));
        datosParaInsert.put("descripcionEcosistema", nuevoEcosistema.get("descripcionEcosistema"));
        datosParaInsert.put("estado", nuevoEcosistema.get("estado") != null
                ? nuevoEcosistema.get("estado")
                : "ACTIVO");

        // ← Asegurarnos de que nunca sea null. Si no existe, uso "SYSTEM" como valor por defecto.
        Object usr = nuevoEcosistema.get("usuarioCreacion");
        if (usr == null || usr.toString().trim().isEmpty()) {
            datosParaInsert.put("usuarioCreacion", "SYSTEM");
        } else {
            datosParaInsert.put("usuarioCreacion", usr);
        }

        datosParaInsert.put("certificado", nuevoEcosistema.get("certificado"));
        datosParaInsert.put("version", nuevoEcosistema.get("version"));

        Object seleccion = nuevoEcosistema.get("grupo");
        if (seleccion != null && !"Sin Asignar".equals(seleccion.toString())) {
            datosParaInsert.put("grupo", seleccion);
        } else {
            datosParaInsert.put("grupo", null);
        }

        // Llamamos al servicio para insertar en BD
        ecosistemaService.crearEcosistema(datosParaInsert);

        // Refrescamos la lista completa de ecosistemas
        ecosistemas = ecosistemaService.listarTodosEcosistemas();

        Messagebox.show("Ecosistema creado correctamente.", "Información",
                Messagebox.OK, Messagebox.INFORMATION);
    }

    /**
     * NUEVO COMANDO: llama a guardarNuevoEcosistema(), y luego cierra el popup “Agregar”
     */
    @Command
    @NotifyChange("ecosistemas")
    public void guardarYCerrar(@ContextParam(ContextType.COMPONENT) Component comp) {
        guardarNuevoEcosistema();
        Window win = (Window) comp.getFellowIfAny("winAddEcosistema");
        if (win != null) {
            win.setVisible(false);
        }
    }

    // ---------- Ver detalles ----------
    @Command
    @NotifyChange("ecoSeleccionado")
    public void verEcosistema(@BindingParam("item") Map<String, Object> item,
                              @ContextParam(ContextType.COMPONENT) Component comp) {
        // Clonamos el Map para no alterar la lista original
        this.ecoSeleccionado = new HashMap<>(item);
        Window win = (Window) comp.getFellowIfAny("winVerEcosistema");
        if (win != null) {
            win.setVisible(true);
            win.doModal();
        }
    }

    // ---------- Probar conectividad ----------
    @Command
    public void probarEcosistema(@BindingParam("item") Map<String, Object> item) {
        String nombreEco = (String) item.get("nombre");
        Map<String, Object> resultado = ecosistemaService.probarConectividad(nombreEco);

        Boolean ok = (Boolean) resultado.get("ok");
        String mensaje = (String) resultado.get("mensaje");
        if (ok != null && ok) {
            Messagebox.show(mensaje, "Conectividad OK", Messagebox.OK, Messagebox.INFORMATION);
        } else {
            Messagebox.show(mensaje, "Error de Conectividad", Messagebox.OK, Messagebox.ERROR);
        }
    }

    // ---------- Editar ecosistema ----------
    @Command
    @NotifyChange("ecoSeleccionado")
    public void editarEcosistema(@BindingParam("item") Map<String, Object> item,
                                 @ContextParam(ContextType.COMPONENT) Component comp) {
        this.ecoSeleccionado = new HashMap<>(item);
        Window win = (Window) comp.getFellowIfAny("winEditEcosistema");
        if (win != null) {
            win.setVisible(true);
            win.doModal();
        }
    }

    @Command
    @NotifyChange("ecosistemas")
    public void guardarEcosistema() {
        if (ecoSeleccionado == null || ecoSeleccionado.get("ID") == null) {
            Messagebox.show("No hay ecosistema seleccionado para editar.", "Error",
                            Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        Map<String, Object> datosParaUpdate = new HashMap<>();
        datosParaUpdate.put("id", ecoSeleccionado.get("ID"));
        datosParaUpdate.put("nombre", ecoSeleccionado.get("NOMBRE"));
        datosParaUpdate.put("descripcionEcosistema", ecoSeleccionado.get("DESCRIPCIONECOSISTEMA"));
        datosParaUpdate.put("estado", ecoSeleccionado.get("ESTADO"));
        datosParaUpdate.put("usuarioModificacion", ecoSeleccionado.get("USUARIOMODIFICACION"));
        datosParaUpdate.put("certificado", ecoSeleccionado.get("CERTIFICADO"));
        datosParaUpdate.put("version", ecoSeleccionado.get("VERSION"));
        datosParaUpdate.put("grupo", ecoSeleccionado.get("NOMBREGRUPO"));

        ecosistemaService.actualizarEcosistema(datosParaUpdate);
        ecosistemas = ecosistemaService.listarTodosEcosistemas();

        Messagebox.show("Ecosistema actualizado correctamente.", "Información",
                        Messagebox.OK, Messagebox.INFORMATION);
    }


    /**
     * NUEVO COMANDO: llama a guardarEcosistema(), y luego cierra el popup “Editar”
     */
    @Command
    @NotifyChange("ecosistemas")
    public void guardarEditarYCerrar(@ContextParam(ContextType.COMPONENT) Component comp) {
        guardarEcosistema();
        Window win = (Window) comp.getFellowIfAny("winEditEcosistema");
        if (win != null) {
            win.setVisible(false);
        }
    }

    // ---------- Dar de alta / dar de baja ----------
    @Command
    @NotifyChange("ecosistemas")
    public void altaEcosistema(@BindingParam("item") final Map<String, Object> item) {
        final String nombreEco = (String) item.get("nombre");
        Messagebox.show(
            "¿Confirma dar de alta el ecosistema \"" + nombreEco + "\"?",
            "Confirmar",
            Messagebox.YES | Messagebox.NO,
            Messagebox.QUESTION,
            new org.zkoss.zk.ui.event.EventListener<org.zkoss.zk.ui.event.Event>() {
                @Override
                public void onEvent(org.zkoss.zk.ui.event.Event evt) throws Exception {
                    if ("onYes".equals(evt.getName())) {
                        ecosistemaService.altaEcosistema(
                            nombreEco,
                            (String) item.get("usuarioModificacion")
                        );
                        ecosistemas = ecosistemaService.listarTodosEcosistemas();
                        Messagebox.show(
                            "Ecosistema dado de alta.",
                            "Información",
                            Messagebox.OK,
                            Messagebox.INFORMATION
                        );
                    }
                }
            }
        );
    }

    @Command
    @NotifyChange("ecosistemas")
    public void bajaEcosistema(@BindingParam("item") final Map<String, Object> item) {
        final String nombreEco = (String) item.get("nombre");
        Messagebox.show(
            "¿Confirma dar de baja el ecosistema \"" + nombreEco + "\"?",
            "Confirmar",
            Messagebox.YES | Messagebox.NO,
            Messagebox.QUESTION,
            new org.zkoss.zk.ui.event.EventListener<org.zkoss.zk.ui.event.Event>() {
                @Override
                public void onEvent(org.zkoss.zk.ui.event.Event evt) throws Exception {
                    if ("onYes".equals(evt.getName())) {
                        ecosistemaService.bajaEcosistema(nombreEco);
                        ecosistemas = ecosistemaService.listarTodosEcosistemas();
                        Messagebox.show(
                            "Ecosistema dado de baja.",
                            "Información",
                            Messagebox.OK,
                            Messagebox.INFORMATION
                        );
                    }
                }
            }
        );
    }

    // ---------- Setters (opcionales) ----------
    public void setEcosistemas(List<Map<String, Object>> ecosistemas) {
        this.ecosistemas = ecosistemas;
    }

    public void setEcoSeleccionado(Map<String, Object> ecoSeleccionado) {
        this.ecoSeleccionado = ecoSeleccionado;
    }

    public void setNuevoEcosistema(Map<String, Object> nuevoEcosistema) {
        this.nuevoEcosistema = nuevoEcosistema;
    }
}
