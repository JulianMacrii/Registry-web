// src/main/java/ar/gde/interoperabilidad/registry/zk/ABMEcosistemaComposer.java
package ar.gde.interoperabilidad.registry.zk;
import java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;

import ar.gde.interoperabilidad.registry.service.EcosistemaService;
import ar.gde.interoperabilidad.registry.service.GrupoService;
import ar.gde.interoperabilidad.registry.service.impl.EcosistemaServiceImpl;
import ar.gde.interoperabilidad.registry.service.impl.GrupoServiceImpl;

/**
 * ViewModel / Composer MVVM para la pantalla ABM de Ecosistemas.
 *
 * Este Composer:
 *   - Carga la lista de ecosistemas a mostrar en la grilla.
 *   - Controla 3 ventanas modales: Ver, Editar y Agregar.
 *   - Invoca a los métodos crearEcosistema(...) y actualizarEcosistema(...) de su service.
 */
@VariableResolver(DelegatingVariableResolver.class)
public class ABMEcosistemaComposer {

    // --------------------------------------------------------
    // 1) Servicios (aquí inicializados "a mano", pero podrías usar @WireVariable si tuvieras Spring)
    // --------------------------------------------------------
    private EcosistemaService ecosistemaService = new EcosistemaServiceImpl();
    private GrupoService      grupoService      = new GrupoServiceImpl();

    // --------------------------------------------------------
    // 2) Datos para la grilla
    //    Cada elemento de la lista es un Map<String,Object> con las claves definidas en tu interfaz.
    // --------------------------------------------------------
    private List<Map<String, Object>> listaEcosistemas;

    // --------------------------------------------------------
    // 3) Bean temporal para “Ver” y “Editar”
    // --------------------------------------------------------
    private Map<String, Object> ecoSeleccionado;

    // --------------------------------------------------------
    // 4) Bean temporal para “Agregar”
    // --------------------------------------------------------
    private Map<String, Object> nuevoEcosistema;

    // --------------------------------------------------------
    // 5) Listas auxiliares para combo/combobox (estados y grupos)
    // --------------------------------------------------------
    private List<String> listaEstados;
    private List<Map<String, Object>> listaGrupos;

    // --------------------------------------------------------
    // 6) Booleans para controlar la visibilidad de los popups
    // --------------------------------------------------------
    private boolean mostrarVer     = false;
    private boolean mostrarEditar  = false;
    private boolean mostrarAgregar = false;

    // --------------------------------------------------------
    // 7) Init: se ejecuta una sola vez al cargar el ViewModel
    // --------------------------------------------------------
    @Init
    @NotifyChange({"listaEcosistemas", "listaEstados", "listaGrupos"})
    public void init() {
        // 7.1) Cargar lista de ecosistemas
        listaEcosistemas = ecosistemaService.listarTodosEcosistemas();

        // 7.2) Cargar estados posibles (ACTIVO / INACTIVO)
        listaEstados = Arrays.asList("ACTIVO", "INACTIVO");

        // 7.3) Cargar lista de grupos (ejemplo: el servicio devuelve List<Map<"id","nombreGrupo">>)
        listaGrupos = grupoService.listarTodosGrupos();
    }

    // --------------------------------------------------------
    // 8) GETTERS (MVVM usa estos getters para enlazar con el ZUL)
    // --------------------------------------------------------
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

    public boolean isMostrarVer() {
        return mostrarVer;
    }

    public boolean isMostrarEditar() {
        return mostrarEditar;
    }

    public boolean isMostrarAgregar() {
        return mostrarAgregar;
    }

    // --------------------------------------------------------
    // 9) COMANDOS MVVM PARA ABRIR/CIERRE DE POPUPS y operaciones
    // --------------------------------------------------------

    /**
     * Comando vinculado a: onClick="@command('agregarEcosistema')"
     * 
     * Inicializa un Map vacío (nuevoEcosistema) y abre el popup de alta.
     */
    @Command("agregarEcosistema")
    @NotifyChange({"mostrarAgregar", "nuevoEcosistema"})
    public void agregarEcosistema() {
        // 9.1) Crear un Map vacío para que el usuario complete los campos
        nuevoEcosistema = new HashMap<>();
        // Asignar valores por defecto
        nuevoEcosistema.put("estado", "ACTIVO");
        nuevoEcosistema.put("version", 1);
        // Si quieres precargar el primer grupo, podrías hacer algo como:
        // nuevoEcosistema.put("idGrupo", listaGrupos.get(0).get("id"));

        // 9.2) Mostrar la ventana modal de “Agregar Ecosistema”
        mostrarAgregar = true;
    }

    /**
     * Comando vinculado a: onClick="@command('guardarNuevoEcosistema')"
     * 
     * Valida y llama a listarTodosEcosistemas(), crearEcosistema(...), recarga la lista y cierra el popup.
     */
    @Command("guardarNuevoEcosistema")
    @NotifyChange({"listaEcosistemas", "mostrarAgregar"})
    public void guardarNuevoEcosistema() {
        // 9.3) Validaciones mínimas (por ejemplo, que exista un nombre)
        String nombre = (String) nuevoEcosistema.get("nombre");
        if (nombre == null || nombre.trim().isEmpty()) {
            org.zkoss.zk.ui.util.Clients.showNotification(
                "El campo Nombre es obligatorio.", "warning", null, null, 2000);
            return;
        }

        // 9.4) Invocar al servicio para crear (usar el método crearEcosistema)
        ecosistemaService.crearEcosistema(nuevoEcosistema);

        // 9.5) Volver a cargar la lista completa (para que se vea el recién creado)
        listaEcosistemas = ecosistemaService.listarTodosEcosistemas();

        // 9.6) Cerrar el popup y limpiar el bean
        mostrarAgregar = false;
        nuevoEcosistema = null;
    }

    /**
     * Comando vinculado a: onClick="@command('cerrarAgregar')"
     * 
     * Sólo cierra el popup de “Agregar Ecosistema” sin guardar.
     */
    @Command("cerrarAgregar")
    @NotifyChange("mostrarAgregar")
    public void cerrarAgregar() {
        mostrarAgregar = false;
    }

    /**
     * Comando vinculado a: onClick="@command('verEcosistema', item=each)"
     * 
     * Recibe la fila seleccionada (Map<String,Object> “each”) y abre el popup de solo‐lectura.
     */
    @Command("verEcosistema")
    @NotifyChange("mostrarVer")
    public void verEcosistema(@BindingParam("item") Map<String, Object> each) {
        ecoSeleccionado = each;
        mostrarVer = true;
    }

    /**
     * Comando vinculado a: onClick="@command('cerrarVer')"
     * 
     * Cierra el popup de “Ver Detalle” sin cambios.
     */
    @Command("cerrarVer")
    @NotifyChange("mostrarVer")
    public void cerrarVer() {
        mostrarVer = false;
    }

    /**
     * Comando vinculado a: onClick="@command('editarEcosistema', item=each)"
     * 
     * Recibe la fila seleccionada, la clona para no modificar la lista original hasta guardar,
     * y abre el popup de edición.
     */
    @Command("editarEcosistema")
    @NotifyChange("mostrarEditar")
    public void editarEcosistema(@BindingParam("item") Map<String, Object> each) {
        // 9.7) Clonar el Map (para no sobrescribir “en caliente” la lista original)
        ecoSeleccionado = new HashMap<>(each);
        mostrarEditar = true;
    }

    /**
     * Comando vinculado a: onClick="@command('guardarEcosistema')"
     * 
     * Toma los valores de ecoSeleccionado, valida, llama a servicio.update y recarga lista.
     */
    @Command("guardarEcosistema")
    @NotifyChange({"listaEcosistemas", "mostrarEditar"})
    public void guardarEcosistema() {
        // 9.8) Validaciones mínimas (por ejemplo, que nombre no esté vacío)
        String nombre = (String) ecoSeleccionado.get("nombre");
        if (nombre == null || nombre.trim().isEmpty()) {
            org.zkoss.zk.ui.util.Clients.showNotification(
                "El campo Nombre es obligatorio.", "warning", null, null, 2000);
            return;
        }

        // 9.9) Invocar al servicio para actualizar (usar actualizarEcosistema)
        ecosistemaService.actualizarEcosistema(ecoSeleccionado);

        // 9.10) Volver a recargar la lista completa para refrescar la grilla
        listaEcosistemas = ecosistemaService.listarTodosEcosistemas();

        // 9.11) Cerrar popup y limpiar bean
        mostrarEditar = false;
        ecoSeleccionado = null;
    }

    /**
     * Comando vinculado a: onClick="@command('cerrarEditar')"
     * 
     * Sólo cierra el popup de edición sin guardar cambios.
     */
    @Command("cerrarEditar")
    @NotifyChange("mostrarEditar")
    public void cerrarEditar() {
        mostrarEditar = false;
    }

    /**
     * Comando vinculado a: onClick="@command('bajaEcosistema', item=each)"
     * 
     * Cambia el estado a "INACTIVO", invoca actualizarEcosistema(...) y recarga lista.
     */
    @Command("bajaEcosistema")
    @NotifyChange("listaEcosistemas")
    public void bajaEcosistema(@BindingParam("item") Map<String, Object> each) {
        each.put("estado", "INACTIVO");
        ecosistemaService.actualizarEcosistema(each);
        listaEcosistemas = ecosistemaService.listarTodosEcosistemas();
    }

    /**
     * Comando vinculado a: onClick="@command('altaEcosistema', item=each)"
     * 
     * Cambia el estado a "ACTIVO", invoca actualizarEcosistema(...) y recarga lista.
     */
    @Command("altaEcosistema")
    @NotifyChange("listaEcosistemas")
    public void altaEcosistema(@BindingParam("item") Map<String, Object> each) {
        each.put("estado", "ACTIVO");
        ecosistemaService.actualizarEcosistema(each);
        listaEcosistemas = ecosistemaService.listarTodosEcosistemas();
    }

    /**
     * Comando vinculado a: onClick="@command('probarEcosistema', item=each)"
     * 
     * Placeholder temporal: muestra notificación de “en desarrollo”.
     */
    @Command("probarEcosistema")
    public void probarEcosistema(@BindingParam("item") Map<String, Object> each) {
        org.zkoss.zk.ui.util.Clients.showNotification(
            "Funcionalidad 'Probar' en desarrollo.", "info", null, null, 1500);
    }
}
