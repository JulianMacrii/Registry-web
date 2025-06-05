// src/main/java/ar/gde/interoperabilidad/registry/zk/ABMEcosistemaComposer.java
package ar.gde.interoperabilidad.registry.zk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;
import org.zkoss.zul.Messagebox;

import ar.gde.interoperabilidad.registry.service.EcosistemaService;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ABMEcosistemaComposer {

    @WireVariable
    private EcosistemaService ecosistemaService;

    private List<Map<String, Object>> ecosistemas;
    private Map<String, Object> ecoSeleccionado;
    private Map<String, Object> nuevoEcosistema = new HashMap<>();

    @Init
    @NotifyChange("ecosistemas")
    public void init() {
        // Obtenemos todos los ecosistemas de la BD
        ecosistemas = ecosistemaService.listarTodosEcosistemas();
    }

    /**
     * Devuelve la lista de ecosistemas para enlazarla al grid del ZUL.
     */
    public List<Map<String, Object>> getEcosistemas() {
        return ecosistemas;
    }

    /**
     * Devuelve el ecosistema seleccionado (para ver o editar).
     */
    public Map<String, Object> getEcoSeleccionado() {
        return ecoSeleccionado;
    }

    /**
     * Devuelve el Map que usaremos como "modelo" para crear un nuevo ecosistema.
     */
    public Map<String, Object> getNuevoEcosistema() {
        return nuevoEcosistema;
    }

    // -----------------------------------------
    // COMANDOS PARA ABRIR/CERRAR VENTANAS MODAL
    // -----------------------------------------

    /**
     * Abre la ventana modal para agregar un nuevo ecosistema.
     * En el ZUL está ligado a: onClick="@command('agregarEcosistema')"
     */
    @Command
    @NotifyChange({"nuevoEcosistema"})
    public void agregarEcosistema(@ContextParam(ContextType.COMPONENT) Component comp) {
        // Reiniciar el Map de nuevo ecosistema
        nuevoEcosistema.clear();

        // Mostramos la ventana winAddEcosistema (ahora que está en la misma ventana padre)
        Window win = (Window) comp.getFellowIfAny("winAddEcosistema");
        if (win != null) {
            win.setVisible(true);
            win.doModal();
        }
    }

    /**
     * Cierra la ventana de agregar ecosistema sin guardar.
     */
    @Command
    public void cerrarAgregar(@ContextParam(ContextType.COMPONENT) Component comp) {
        Window win = (Window) comp.getFellowIfAny("winAddEcosistema");
        if (win != null) {
            win.setVisible(false);
            win.detach();
        }
    }

    /**
     * Cierra la ventana de ver detalles de ecosistema.
     */
    @Command
    public void cerrarVer(@ContextParam(ContextType.COMPONENT) Component comp) {
        Window win = (Window) comp.getFellowIfAny("winVerEcosistema");
        if (win != null) {
            win.setVisible(false);
            win.detach();
        }
    }

    /**
     * Cierra la ventana de editar ecosistema.
     */
    @Command
    public void cerrarEditar(@ContextParam(ContextType.COMPONENT) Component comp) {
        Window win = (Window) comp.getFellowIfAny("winEditEcosistema");
        if (win != null) {
            win.setVisible(false);
            win.detach();
        }
    }

    // -----------------------------------------
    // COMANDOS PARA CREAR / GUARDAR / REFRESCAR
    // -----------------------------------------

    /**
     * Guarda un nuevo ecosistema usando los datos que el usuario llenó
     * en winAddEcosistema. Luego refresca el listado.
     */
    @Command
    @NotifyChange("ecosistemas")
    public void guardarNuevoEcosistema() {
        // Validaciones mínimas: asegurar que venga nombre y grupo
        if (nuevoEcosistema.get("nombre") == null || nuevoEcosistema.get("grupo") == null) {
            Messagebox.show("Por favor completa al menos Nombre y Grupo.", "Error",
                    Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        // Llenar el Map con los valores que el método crearEcosistema espera en SQL:
        Map<String, Object> datosParaInsert = new HashMap<>();
        datosParaInsert.put("nombre", nuevoEcosistema.get("nombre"));                // descripcionEcosistema ⟶ alias 'nombre'
        datosParaInsert.put("estado", nuevoEcosistema.get("estado") != null
                ? nuevoEcosistema.get("estado")
                : "ACTIVO");                                                       // valor por defecto
        datosParaInsert.put("usuarioCreacion", nuevoEcosistema.get("usuarioCreacion"));
                                                                                      // por ej: zkSession.getUsuario()
        datosParaInsert.put("certificado", nuevoEcosistema.get("certificado"));      // contenido PEM
        datosParaInsert.put("version", nuevoEcosistema.get("version"));              // versión
        datosParaInsert.put("grupo", nuevoEcosistema.get("grupo"));                  // descripcionGrupo en REG_GRUPO

        // Llamada al servicio que lo inserta en tabla REG_ECOSISTEMA
        ecosistemaService.crearEcosistema(datosParaInsert);

        // Después de insertar, recargamos la lista completa
        ecosistemas = ecosistemaService.listarTodosEcosistemas();

        // Cerramos la ventana modal
        Messagebox.show("Ecosistema creado correctamente.", "Información",
                Messagebox.OK, Messagebox.INFORMATION);
        // Aquí se cierra la ventana winAddEcosistema de forma manual:
        // Puedes reutilizar cerrarAgregar(comp) si prefieres
    }

    // -----------------------------------------
    // COMANDOS PARA VISUALIZAR / PROBAR
    // -----------------------------------------

    /**
     * Abre la ventana modal que muestra los detalles de un ecosistema.
     * El parámetro "item" viene del each=each de la Grid.
     */
    @Command
    @NotifyChange("ecoSeleccionado")
    public void verEcosistema(@BindingParam("item") Map<String, Object> item,
                              @ContextParam(ContextType.COMPONENT) Component comp) {
        // Clonar los datos (si quieres evitar editar accidentalmente el listado)
        this.ecoSeleccionado = new HashMap<>(item);

        // Abrimos winVerEcosistema
        Window win = (Window) comp.getFellowIfAny("winVerEcosistema");
        if (win != null) {
            win.setVisible(true);
            win.doModal();
        }
    }

    /**
     * Llama a probarConectividad para el ecosistema enviado. Muestra un Messagebox
     * con el mensaje ok/falla.
     */
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

    // -----------------------------------------
    // COMANDOS PARA EDITAR / ACTUALIZAR
    // -----------------------------------------

    /**
     * Abre la ventana modal para editar los datos de un ecosistema.
     * Rellena ecoSeleccionado con la data actual (que luego se enlaza en winEditEcosistema).
     */
    @Command
    @NotifyChange("ecoSeleccionado")
    public void editarEcosistema(@BindingParam("item") Map<String, Object> item,
                                 @ContextParam(ContextType.COMPONENT) Component comp) {
        // Clonamos el Map (para no alterar el listado en tiempo real)
        this.ecoSeleccionado = new HashMap<>(item);

        // Abrimos winEditEcosistema
        Window win = (Window) comp.getFellowIfAny("winEditEcosistema");
        if (win != null) {
            win.setVisible(true);
            win.doModal();
        }
    }

    /**
     * Guarda los cambios de ecoSeleccionado (editando en la BD) y refresca la lista.
     */
    @Command
    @NotifyChange("ecosistemas")
    public void guardarEcosistema() {
        if (ecoSeleccionado == null || ecoSeleccionado.get("id") == null) {
            Messagebox.show("No hay ecosistema seleccionado para editar.", "Error",
                    Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        // Preparar el Map de datos para actualizar
        Map<String, Object> datosParaUpdate = new HashMap<>();
        datosParaUpdate.put("id", ecoSeleccionado.get("id"));                       // id
        datosParaUpdate.put("nombre", ecoSeleccionado.get("nombre"));               // descripcionEcosistema
        datosParaUpdate.put("estado", ecoSeleccionado.get("estado"));               // estado
        datosParaUpdate.put("usuarioModificacion", ecoSeleccionado.get("usuarioModificacion"));
                                                                                     // por ej: usuario logueado
        datosParaUpdate.put("certificado", ecoSeleccionado.get("certificado"));     // certificado PEM actualizado
        datosParaUpdate.put("version", ecoSeleccionado.get("version"));             // versión
        datosParaUpdate.put("grupo", ecoSeleccionado.get("nombreGrupo"));            // nombreGrupo actual

        // Llamada al servicio para actualizar REG_ECOSISTEMA
        ecosistemaService.actualizarEcosistema(datosParaUpdate);

        // Refrescamos el listado completo
        ecosistemas = ecosistemaService.listarTodosEcosistemas();

        Messagebox.show("Ecosistema actualizado correctamente.", "Información",
                Messagebox.OK, Messagebox.INFORMATION);
        // Aquí se cerraría la ventana winEditEcosistema
    }

    // -----------------------------------------
    // COMANDOS PARA DAR DE BAJA / DAR DE ALTA
    // -----------------------------------------

    /**
     * Cambia el estado del ecosistema a 'ACTIVO' vía servicio, y refresca la lista.
     */
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
                public void onEvent(org.zkoss.zk.ui.event.Event event) throws Exception {
                    if ("onYes".equals(event.getName())) {
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

    /**
     * Cambia el estado del ecosistema a 'INACTIVO' vía servicio, y refresca la lista.
     */
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
                public void onEvent(org.zkoss.zk.ui.event.Event event) throws Exception {
                    if ("onYes".equals(event.getName())) {
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

    // -----------------------------------------
    // Otros getters/setters si hicieran falta
    // -----------------------------------------

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
