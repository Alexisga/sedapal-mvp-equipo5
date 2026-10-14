package com.utp.sedapal;

import com.utp.sedapal.dto.AtencionForm;
import com.utp.sedapal.models.*;
import com.utp.sedapal.repository.*;
import com.utp.sedapal.services.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

/** Cohorte fijada antes de medir; incluye los casos incompletos y no modifica reglas RF. */
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class CompletitudIP05IntegrationTest {
    @Autowired ReclamoService reclamos;
    @Autowired AuthService auth;
    @Autowired EvidenciaService evidencias;
    @Autowired EvidenciaRepository adjuntos;
    @Autowired TecnicoRepository tecnicos;
    @Autowired MockMvc mvc;
    private byte[] archivo(String tipo) throws Exception {
        var out=new java.io.ByteArrayOutputStream();
        if(tipo.equals("pdf")) {
            try(var d=new org.apache.pdfbox.pdmodel.PDDocument()) {
                d.addPage(new org.apache.pdfbox.pdmodel.PDPage());d.save(out);
            }
        } else javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(12,12,1),tipo,out);
        return out.toByteArray();
    }
    private Reclamo atender(Reclamo r,String extension,boolean publicar) throws Exception {
        var orden=reclamos.listarOrdenesPorReclamo(r.getId()).getFirst();
        var tecnico=auth.requerirPorDocumento("33333333");
        var form=new AtencionForm();form.setAccionesRealizadas("Acciones SIMULADAS para medir IP05.");
        form.setResultado("Resultado SIMULADO para una medición académica.");
        if(extension!=null)form.setEvidencia(new MockMultipartFile("evidencia","simulada."+extension,
                extension.equals("pdf")?"application/pdf":extension.equals("jpg")?"image/jpeg":"image/png",archivo(extension)));
        reclamos.registrarAtencion(orden.getId(),tecnico.getId(),form,tecnico.getDocumento());
        if(publicar)for(var e:adjuntos.findByAtencion_Orden_Reclamo_Id(r.getId()))evidencias.publicar(e.getId(),"22222222");
        return reclamos.requerir(r.getId());
    }
    private Reclamo caso(String extension,boolean publicar,String etapa) throws Exception {
        var u=auth.requerirPorDocumento("12345678");
        var r=reclamos.registrarReclamo(u.getId(),"SUM-1001","Fuga de agua","Alta",
                "Caso SIMULADO fijado previamente para IP05: "+etapa,"12345678");
        reclamos.clasificar(r.getId(),"Fuga de agua","Alta",true,"Evaluación SIMULADA de la cohorte IP05.","44444444");
        var o=reclamos.generarOrden(r.getId(),"55555555");
        var t=tecnicos.findByUsuarioId(auth.requerirPorDocumento("33333333").getId()).orElseThrow();
        reclamos.asignarTecnico(o.getId(),t.getId(),"55555555");r=atender(r,extension,publicar);
        if(etapa.equals("Cerrado")||etapa.equals("Reapertura solicitada"))reclamos.cerrar(r.getId(),"12345678");
        if(etapa.equals("Reapertura solicitada"))reclamos.solicitarReapertura(r.getId(),"Motivo SIMULADO de la cohorte IP05.","12345678");
        return reclamos.requerir(r.getId());
    }
    @Test void mideTodosLosExpedientesElegiblesSinDescartarIncompletos() throws Exception {
        // La orientación atendida del seed no tiene adjunto: permanece en el denominador.
        atender(reclamos.buscarPorCodigo("REC-2026-0002").orElseThrow(),"pdf",true);
        caso("png",false,"Atendido");
        caso("jpg",true,"Cerrado");
        caso("pdf",true,"Reapertura solicitada");
        caso(null,false,"Atendido");
        var etapas=Set.of("Atendido","Cerrado","Reapertura solicitada");
        var out=Path.of(Optional.ofNullable(System.getenv("SEDAPAL_CIERRE_EVIDENCIAS")).orElse("target/cierre-evidencias")).resolve("IP05");
        Files.createDirectories(out.resolve("adjuntos"));
        List<Map<String,Object>> evaluados=new ArrayList<>(),excluidos=new ArrayList<>();
        for(var r:reclamos.listarTodos()) {
            Map<String,Object> row=new LinkedHashMap<>();row.put("id",r.getId());row.put("codigo",r.getCodigo());
            row.put("estado",r.getEstado().getNombre());row.put("datos","SIMULADOS");
            if(!etapas.contains(r.getEstado().getNombre())) {row.put("causa","La etapa no requiere evidencia final de atención");excluidos.add(row);continue;}
            var hist=reclamos.listarHistorial(r.getId());
            boolean fecha=r.getFechaRegistro()!=null&&!hist.isEmpty()&&hist.getLast().getFecha()!=null;
            row.put("fecha_registro",String.valueOf(r.getFechaRegistro()));row.put("fecha_ultima",hist.isEmpty()?"":String.valueOf(hist.getLast().getFecha()));
            row.put("fecha_completa",fecha);String titular=r.getUsuario().getDocumento();String ajeno=titular.equals("12345678")?"87654321":"12345678";
            List<Map<String,Object>> files=new ArrayList<>();boolean consultable=false;
            for(var e:adjuntos.findByAtencion_Orden_Reclamo_Id(r.getId())) {
                var propietario=mvc.perform(get("/evidencias/"+e.getId()).with(user(titular).roles("USUARIO"))).andReturn().getResponse();
                var externo=mvc.perform(get("/evidencias/"+e.getId()).with(user(ajeno).roles("USUARIO"))).andReturn().getResponse();
                var interno=mvc.perform(get("/evidencias/"+e.getId()).with(user("22222222").roles("OPERADOR"))).andReturn().getResponse();
                assertThat(externo.getStatus()).isEqualTo(403);assertThat(interno.getStatus()).isEqualTo(200);
                assertThat(propietario.getStatus()).isEqualTo(e.isVisibleCiudadano()?200:403);
                byte[] bytes=Files.readAllBytes(Path.of(e.getRuta()));assertThat(interno.getContentAsByteArray()).isEqualTo(bytes);
                String file=r.getCodigo()+"-"+e.getId()+"-"+e.getNombreOriginal();Files.write(out.resolve("adjuntos").resolve(file),bytes);
                Map<String,Object> f=new LinkedHashMap<>();f.put("evidencia_id",e.getId());f.put("archivo",file);f.put("visible_ciudadano",e.isVisibleCiudadano());f.put("http_titular",propietario.getStatus());f.put("http_ajeno",externo.getStatus());f.put("http_operador",interno.getStatus());f.put("bytes",bytes.length);
                f.put("sha256",java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes)));files.add(f);
                consultable|=e.isVisibleCiudadano()&&propietario.getStatus()==200&&Arrays.equals(propietario.getContentAsByteArray(),bytes);
            }
            row.put("evidencias",files);row.put("evidencia_consultable_titular",consultable);
            row.put("completo",fecha&&!r.getEstado().getNombre().isBlank()&&consultable);
            row.put("causa",consultable?"Elementos completos":files.isEmpty()?"Sin evidencia asociada":"Evidencia restringida pendiente de revisión/publicación");evaluados.add(row);
        }
        long completos=evaluados.stream().filter(r->Boolean.TRUE.equals(r.get("completo"))).count();
        Map<String,Object> result=new LinkedHashMap<>();result.put("fecha",ZonedDateTime.now(ZoneId.of("America/Lima")).toString());result.put("version",Optional.ofNullable(System.getenv("SEDAPAL_QA_COMMIT")).orElse("Consultar log Maven"));
        result.put("responsable","Codex agente automatizado");result.put("protocolo","Cohorte sintética prefijada; TODOS los casos Atendido/Cerrado/Reapertura solicitada de esta BD H2. Evidencia consultable por titular, sin excluir faltantes ni borradores.");
        result.put("evaluados",evaluados);result.put("excluidos",excluidos);result.put("numerador",completos);result.put("denominador",evaluados.size());result.put("IP05_porcentaje",evaluados.isEmpty()?null:100.0*completos/evaluados.size());result.put("meta_porcentaje",90);result.put("cumple_meta",!evaluados.isEmpty()&&100.0*completos/evaluados.size()>=90);
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out.resolve("medicion.json").toFile(),result);
        assertThat(evaluados).hasSize(6);assertThat(completos).isEqualTo(3);assertThat(excluidos).hasSize(1);
        System.out.println("IP05_MEDIDO="+result.get("IP05_porcentaje")+"; CASOS="+evaluados.size()+"; META="+result.get("cumple_meta"));
    }
}
