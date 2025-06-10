package ar.gob.gde.interoperabilidad.registry.restful;

import ar.gde.interoperabilidad.registry.service.EcosistemaService;
import ar.gde.interoperabilidad.registry.service.GrupoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST unificado del Registry usando JAX-RS.
 * Base path: /registry-web/api/v1
 */
@Component
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RegistryService {

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private EcosistemaService ecosistemaService;

    // ---------------------------------------
    // ENDPOINTS DE GRUPOS
    // ---------------------------------------

    @GET
    @Path("/grupos")
    public Response listarGrupos() {
        List<Map<String, Object>> lista = grupoService.listarTodosGrupos();
        return Response.ok(lista).build();
    }

    @GET
    @Path("/grupos/{id}")
    public Response obtenerGrupo(@PathParam("id") long id) {
        Map<String, Object> grupo = grupoService.obtenerGrupoPorId(id);
        return Response.ok(grupo).build();
    }

    @POST
    @Path("/grupos")
    public Response crearGrupo(Map<String, Object> payload) {
        grupoService.crearGrupo(payload);
        return Response.ok().build();
    }

    @PUT
    @Path("/grupos/{id}")
    public Response actualizarGrupo(@PathParam("id") long id,
                                    Map<String, Object> payload) {
        payload.put("id", id);
        grupoService.actualizarGrupo(payload);
        return Response.ok().build();
    }

    @DELETE
    @Path("/grupos/{id}")
    public Response bajaGrupo(@PathParam("id") long id) {
        grupoService.bajaGrupo(id);
        return Response.ok().build();
    }

    @POST
    @Path("/grupos/{id}/alta")
    public Response altaGrupo(@PathParam("id") long id,
                              @HeaderParam("usuario") String usuario) {
        grupoService.altaGrupo(id, usuario);
        return Response.ok().build();
    }

    // ---------------------------------------
    // ENDPOINTS DE ECOSISTEMAS
    // ---------------------------------------

    @GET
    @Path("/ecosistemas")
    public Response listarEcosistemas() {
        List<Map<String, Object>> lista = ecosistemaService.listarTodosEcosistemas();
        return Response.ok(lista).build();
    }

    @GET
    @Path("/ecosistemas/{nombre}")
    public Response obtenerEcosistema(@PathParam("nombre") String nombre) {
        Map<String, Object> eco = ecosistemaService.obtenerEcosistemaPorNombre(nombre);
        return Response.ok(eco).build();
    }

    @GET
    @Path("/ecosistemas/grupo/{grupo}")
    public Response listarEcosistemasPorGrupo(@PathParam("grupo") String grupo) {
        List<Map<String, Object>> lista = ecosistemaService.listarEcosistemasPorGrupo(grupo);
        return Response.ok(lista).build();
    }

    @GET
    @Path("/ecosistemas/mismo-grupo/{ecosistema}")
    public Response listarEcosistemasMismoGrupo(@PathParam("ecosistema") String ecosistema) {
        List<Map<String, Object>> lista = ecosistemaService.listarEcosistemasMismoGrupo(ecosistema);
        return Response.ok(lista).build();
    }

    @GET
    @Path("/ecosistemas/descripcion/{ecosistema}")
    public Response obtenerDescripcion(@PathParam("ecosistema") String ecosistema) {
        Map<String, Object> eco = ecosistemaService.obtenerEcosistemaPorNombre(ecosistema);
        return Response.ok(eco.get("descripcionEcosistema")).build();
    }

    @GET
    @Path("/clave/ecosistema/{ecosistema}")
    public Response obtenerCertificado(@PathParam("ecosistema") String ecosistema) {
        Map<String, Object> eco = ecosistemaService.obtenerEcosistemaPorNombre(ecosistema);
        return Response.ok(eco.get("certificado")).build();
    }

    @POST
    @Path("/ecosistemas")
    public Response crearEcosistema(Map<String, Object> payload) {
        ecosistemaService.crearEcosistema(payload);
        return Response.ok().build();
    }

    @PUT
    @Path("/ecosistemas/{id}")
    public Response actualizarEcosistema(@PathParam("id") long id,
                                         Map<String, Object> payload) {
        payload.put("id", id);
        ecosistemaService.actualizarEcosistema(payload);
        return Response.ok().build();
    }

    @DELETE
    @Path("/ecosistemas/{nombre}")
    public Response bajaEcosistema(@PathParam("nombre") String nombre) {
        ecosistemaService.bajaEcosistema(nombre);
        return Response.ok().build();
    }

    @POST
    @Path("/ecosistemas/{nombre}/alta")
    public Response altaEcosistema(@PathParam("nombre") String nombre,
                                   @HeaderParam("usuario") String usuario) {
        ecosistemaService.altaEcosistema(nombre, usuario);
        return Response.ok().build();
    }

    @GET
    @Path("/ecosistemas/{nombre}/probar")
    public Response probarConectividad(@PathParam("nombre") String nombre) {
        Map<String, Object> resultado = ecosistemaService.probarConectividad(nombre);
        return Response.ok(resultado).build();
    }
}
