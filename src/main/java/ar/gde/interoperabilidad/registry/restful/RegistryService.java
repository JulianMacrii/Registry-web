// src/main/java/ar/gde/interoperabilidad/registry/restful/RegistryService.java
package ar.gde.interoperabilidad.registry.restful;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ar.gde.interoperabilidad.registry.service.EcosistemaService;
import ar.gde.interoperabilidad.registry.service.GrupoService;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para exponer endpoints de “grupos”, “ecosistemas” y demás.
 */
@Component
@Path("/registry-web/api/v1")
@Produces(MediaType.APPLICATION_JSON)
public class RegistryService {

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private EcosistemaService ecosistemaService;

    // ---------------------------------------
    // ENDPOINTS DE GRUPOS
    // ---------------------------------------

    /**
     * GET /registry-web/api/v1/grupos
     * Devuelve lista de todos los grupos.
     */
    @GET
    @Path("/grupos")
    public Response listarGrupos() {
        List<Map<String, Object>> lista = grupoService.listarTodosGrupos();
        return Response.ok(lista).build();
    }

    /**
     * GET /registry-web/api/v1/grupos/{id}
     * Devuelve un solo grupo por su ID.
     */
    @GET
    @Path("/grupos/{id}")
    public Response getGrupo(@PathParam("id") long idGrupo) {
        Map<String, Object> g = grupoService.obtenerGrupoPorId(idGrupo);
        return Response.ok(g).build();
    }

    /**
     * POST /registry-web/api/v1/grupos
     * Crea un nuevo grupo (recibe JSON con “nombre” y “estado” y “usuarioCreacion”)
     */
    @POST
    @Path("/grupos")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crearGrupo(Map<String, Object> payload) {
        grupoService.crearGrupo(payload);
        return Response.ok("{\"resultado\":\"Grupo creado.\"}").build();
    }

    /**
     * PUT /registry-web/api/v1/grupos/{id}
     * Actualiza un grupo existente.
     * En el payload JSON debe venir: { "nombre": "...", "estado": "...", "usuarioModificacion": "..." }
     */
    @PUT
    @Path("/grupos/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizarGrupo(
            @PathParam("id") long idGrupo,
            Map<String, Object> payload
    ) {
        payload.put("id", idGrupo);
        grupoService.actualizarGrupo(payload);
        return Response.ok("{\"resultado\":\"Grupo actualizado.\"}").build();
    }

    /**
     * DELETE /registry-web/api/v1/grupos/{id}
     * Dar de baja (soft‐delete) un grupo y sus ecosistemas asociados.
     */
    @DELETE
    @Path("/grupos/{id}")
    public Response bajaGrupo(
            @PathParam("id") long idGrupo
    ) {
        grupoService.bajaGrupo(idGrupo);
        return Response.ok("{\"resultado\":\"Grupo dado de baja.\"}").build();
    }

    /**
     * POST /registry-web/api/v1/grupos/{id}/alta
     * Reactivar un grupo (soft‐undelete).
     * Se espera un header “usuario” con el nombre de usuario que ejecuta la reactivación.
     */
    @POST
    @Path("/grupos/{id}/alta")
    public Response altaGrupo(
            @PathParam("id") long idGrupo,
            @HeaderParam("usuario") String usuario
    ) {
        grupoService.altaGrupo(idGrupo, usuario);
        return Response.ok("{\"resultado\":\"Grupo reactivado.\"}").build();
    }


    // ---------------------------------------
    // ENDPOINTS DE ECOSISTEMAS
    // ---------------------------------------

    /**
     * GET /registry-web/api/v1/ecosistemas
     * Devuelve lista de todos los ecosistemas (con columnas alias: "id", "nombre", "estado", etc.).
     */
    @GET
    @Path("/ecosistemas")
    public Response listarEcosistemas() {
        List<Map<String, Object>> lista = ecosistemaService.listarTodosEcosistemas();
        return Response.ok(lista).build();
    }

    /**
     * GET /registry-web/api/v1/ecosistemas/{nombre}
     * Obtiene un ecosistema por su “descripcionEcosistema” (alias “nombre” en el mapa).
     */
    @GET
    @Path("/ecosistemas/{nombre}")
    public Response obtenerEcosistema(@PathParam("nombre") String nombre) {
        Map<String, Object> eco = ecosistemaService.obtenerEcosistemaPorNombre(nombre);
        return Response.ok(eco).build();
    }

    /**
     * POST /registry-web/api/v1/ecosistemas
     * Crea un ecosistema nuevo. El payload JSON deberá contener:
     *   { "nombre": "...", "estado": "...", "usuarioCreacion": "...", "certificado": "...", "version": "...", "grupo": "..." }
     */
    @POST
    @Path("/ecosistemas")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crearEcosistema(Map<String, Object> payload) {
        ecosistemaService.crearEcosistema(payload);
        return Response.ok("{\"resultado\":\"Ecosistema creado.\"}").build();
    }

    /**
     * PUT /registry-web/api/v1/ecosistemas/{id}
     * Actualiza un ecosistema. Payload JSON con:
     *   { "nombre": "...", "estado": "...", "usuarioModificacion": "...", "certificado": "...", "version": "...", "grupo": "...", "id": <id> }
     */
    @PUT
    @Path("/ecosistemas/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizarEcosistema(
            @PathParam("id") Long id,
            Map<String, Object> payload
    ) {
        payload.put("id", id);
        ecosistemaService.actualizarEcosistema(payload);
        return Response.ok("{\"resultado\":\"Ecosistema actualizado.\"}").build();
    }

    /**
     * DELETE /registry-web/api/v1/ecosistemas/{nombre}
     * Dar de baja (soft‐delete) un ecosistema por su “nombre” (descripcionEcosistema).
     */
    @DELETE
    @Path("/ecosistemas/{nombre}")
    public Response bajaEcosistema(@PathParam("nombre") String nombreEcosistema) {
        ecosistemaService.bajaEcosistema(nombreEcosistema);
        return Response.ok("{\"resultado\":\"Ecosistema " + nombreEcosistema + " dado de baja.\"}").build();
    }

    /**
     * POST /registry-web/api/v1/ecosistemas/{nombre}/alta
     * Reactivar un ecosistema. Header “usuario” con nombre de usuario.
     */
    @POST
    @Path("/ecosistemas/{nombre}/alta")
    public Response altaEcosistema(
            @PathParam("nombre") String nombreEcosistema,
            @HeaderParam("usuario") String usuario
    ) {
        ecosistemaService.altaEcosistema(nombreEcosistema, usuario);
        return Response.ok("{\"resultado\":\"Ecosistema " + nombreEcosistema + " activado.\"}").build();
    }

    /**
     * GET /registry-web/api/v1/ecosistemas/{nombre}/probar
     * Llama al método probarConectividad(nombre) para verificar conectividad al dominio “PRINCIPAL”.
     */
    @GET
    @Path("/ecosistemas/{nombre}/probar")
    public Response probarConectividad(@PathParam("nombre") String nombreEcosistema) {
        Map<String, Object> result = ecosistemaService.probarConectividad(nombreEcosistema);
        return Response.ok(result).build();
    }

    // … Aquí agregarías más endpoints (dominios, módulos, endpoints, propiedades) según tu Interface EcosistemaService
}
