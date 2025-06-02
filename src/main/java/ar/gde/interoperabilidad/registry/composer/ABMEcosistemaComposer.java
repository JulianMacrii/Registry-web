// src/main/java/ar/gde/interoperabilidad/registry/composer/ABMEcosistemaComposer.java
package ar.gde.interoperabilidad.registry.composer;

import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelList;

import ar.gde.interoperabilidad.registry.service.EcosistemaService;

public class ABMEcosistemaComposer extends SelectorComposer<Component> {

    @Wire("#gridEcosistemas")
    private Grid gridEcosistemas;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // 1) obtener el servicio desde Spring
        EcosistemaService svc = (EcosistemaService) SpringUtil.getBean("ecosistemaService");

        // 2) listar y poblar la grilla
        // Nota: en el Service, listarTodosEcosistemas() devuelve un List<Map<...>>
        // con claves: "id", "nombre", "estado", "fechaCreacion", "fechaModificacion",
        // "usuarioCreacion", "usuarioModificacion", "certificado", "version", 
        // "idGrupo", "nombreGrupo".
        List<Map<String, Object>> lista = svc.listarTodosEcosistemas();
        gridEcosistemas.setModel(new ListModelList<>(lista));
    }

    /**
     * Ejemplo de método para mostrar un dialogo de alta.
     * Deberás enlazarlo a tu ZUL (ej. onClick de un botón “Agregar”).
     */
    public void showAddDialog() {
        Clients.showNotification("Aquí abrirías el diálogo de alta", "info", null, null, 2000);
    }

    // Los demás comandos (ver, editar, baja, alta, probar, etc.) irían aquí,
    // siempre consumiendo las mismas claves que devuelve el Map:
    //
    // Por ejemplo:
    // @Listen("onClick = button#btnVer")
    // public void verEcosistema(ForwardEvent evt) { … usar datos.get("nombre"), datos.get("estado"), datos.get("nombreGrupo"), etc. }
    //
    // Asegúrate de NO usar datos.get("descripcionEcosistema") ni datos.get("estadoGrupo"),
    // sino “nombre” y “estado” tal como los alias SQL.
}
