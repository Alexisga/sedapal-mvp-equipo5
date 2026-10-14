package com.utp.sedapal;
import com.utp.sedapal.services.*;
import com.utp.sedapal.repository.*;
import com.utp.sedapal.models.*;
import com.utp.sedapal.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class IncrementoIntegrationTest {
 @Autowired MockMvc mvc; @Autowired ReclamoService reclamos; @Autowired AuthService auth;
 @Autowired RegistroService registro; @Autowired PasswordEncoder encoder; @Autowired EvidenciaService evidencias;
 @Autowired ArchivoService archivos; @Autowired EvidenciaRepository er;
 @Autowired AtencionTecnicaRepository ar; @Autowired OrdenTrabajoRepository ordenes;
 @Test void HU08AltaHashTitularPersistenteSinSuplantacion() throws Exception {
  RegistroForm f=new RegistroForm();f.setNombre("Ciudadano QA");f.setDocumento("qaNuevo");f.setPassword("ClaveQA123");registro.registrar(f);
  var u=auth.requerirPorDocumento("qaNuevo");assertThat(u.getRol()).isEqualTo(RolUsuario.USUARIO);assertThat(encoder.matches("ClaveQA123",u.getPassword())).isTrue();
  assertThatThrownBy(()->registro.registrar(f)).isInstanceOf(IllegalArgumentException.class);
  mvc.perform(post("/usuario/reclamos").with(user("qaNuevo").roles("USUARIO")).with(csrf()).param("usuarioId","1").param("direccion","Ubicación ficticia QA").param("distrito","Lima").param("tipoIncidencia","Fuga de agua").param("prioridad","Alta").param("descripcion","Fuga ficticia registrada exclusivamente para QA."))
   .andExpect(status().is3xxRedirection());
  assertThat(reclamos.listarReclamosDeUsuario(u.getId())).hasSize(1).allMatch(r->r.getUsuario().getId().equals(u.getId()));
 }
 @ParameterizedTest @ValueSource(strings={"", "1234567"}) void HU08RechazaClaveInsuficiente(String clave)throws Exception {
  mvc.perform(post("/registro").with(csrf()).param("nombre","QA").param("documento","qaPrueba").param("password",clave)).andExpect(status().isOk());
  assertThat(auth.buscarPorDocumento("qaPrueba")).isEmpty();
 }
 @Test void HU02ConsultaPublicaNoExponeIdentidadNiObservaciones()throws Exception {
  mvc.perform(get("/consulta").param("codigo","REC-2026-0002")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Asignado"))).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("12345678")))).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Jr. Los Jardines"))));
 }
 @Test void HU05HU06CierreExplicitoReaperturaHistorialYNotificacion()throws Exception {
  var r=reclamos.buscarPorCodigo("REC-2026-0003").orElseThrow();var antes=reclamos.listarHistorial(r.getId()).size();
  mvc.perform(post("/usuario/reclamos/"+r.getId()+"/cerrar").with(user("87654321").roles("USUARIO")).with(csrf())).andExpect(status().is3xxRedirection());
  assertThat(r.getEstado().getNombre()).isEqualTo(ReclamoService.ATENDIDO);
  reclamos.cerrar(r.getId(),"87654321");assertThat(r.getConfirmadoPor()).isEqualTo("87654321");assertThat(r.getFechaConfirmacion()).isNotNull();
  assertThatThrownBy(()->reclamos.solicitarReapertura(r.getId(),"corto","87654321")).isInstanceOf(IllegalArgumentException.class);
  reclamos.solicitarReapertura(r.getId(),"El problema ficticio persiste sin solución.","87654321");
  assertThat(r.getEstado().getNombre()).isEqualTo(ReclamoService.REAPERTURA_SOLICITADA);
  assertThat(r.getMotivoReapertura()).contains("persiste");assertThat(reclamos.listarHistorial(r.getId())).hasSize(antes+2);
  assertThatThrownBy(()->reclamos.solicitarReapertura(r.getId(),"Segunda solicitud ficticia.","87654321")).isInstanceOf(IllegalArgumentException.class);
 }
 @Test void HU05HU06AjenoYOperadorNoPuedenCerrar() {
  var r=reclamos.buscarPorCodigo("REC-2026-0003").orElseThrow();
  for(String doc:new String[]{"12345678","22222222"}) assertThatThrownBy(()->reclamos.cerrar(r.getId(),doc)).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  assertThatThrownBy(()->reclamos.solicitarReapertura(r.getId(),"Motivo de un usuario ajeno.","12345678")).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
 }
 @Test void HU03HU07RevisionYDescargaExigeTitular()throws Exception {
  var o=ordenes.findAll().getFirst();var a=new AtencionTecnica();a.setOrden(o);a.setAccionesRealizadas("QA ficticia");a.setResultado("QA ficticia");ar.save(a);
  var e=archivos.guardar(new MockMultipartFile("evidencia","qa.pdf","application/pdf","%PDF-1.4\nQA ficticia\n%%EOF".getBytes()),"QA",a);
  assertThat(evidencias.listar(o.getReclamo().getId(),"12345678")).isEmpty();
  assertThatThrownBy(()->evidencias.abrir(e.getId(),"12345678")).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  evidencias.publicar(e.getId(),"22222222");assertThat(evidencias.abrir(e.getId(),"12345678").contenido()).isNotEmpty();
  assertThatThrownBy(()->evidencias.abrir(e.getId(),"87654321")).isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
  mvc.perform(get("/evidencias/"+e.getId()).with(user("87654321").roles("USUARIO"))).andExpect(status().isForbidden());
 }
 @ParameterizedTest @ValueSource(strings={"qa.exe","qa.jpg","qa.png","../qa.pdf"}) void RNF06RechazaExtensionOContenidoDiscrepante(String nombre) {
  assertThatThrownBy(()->archivos.guardar(new MockMultipartFile("evidencia",nombre,"application/pdf","contenido falso".getBytes()),"QA",new AtencionTecnica())).isInstanceOf(IllegalArgumentException.class);
 }
 @Test void RNF06RechazaExcesoTamanoYConteo() {
  assertThatThrownBy(()->archivos.guardar(new MockMultipartFile("evidencia","qa.pdf","application/pdf",new byte[5*1024*1024+1]),"QA",new AtencionTecnica())).isInstanceOf(IllegalArgumentException.class);
  var a=new AtencionTecnica();a.setOrden(ordenes.findAll().getFirst());a.setAccionesRealizadas("QA");a.setResultado("QA");ar.save(a);
  for(int n=0;n<5;n++)archivos.guardar(new MockMultipartFile("evidencia","qa.pdf","application/pdf","%PDF-1.4\n%%EOF".getBytes()),"QA",a);
  assertThatThrownBy(()->archivos.guardar(new MockMultipartFile("evidencia","qa.pdf","application/pdf","%PDF-1.4\n%%EOF".getBytes()),"QA",a)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("cinco");
 }
 @Test void HU04AvisosNoSeMezclan() {
  var u=auth.requerirPorDocumento("12345678");var ns=notificaciones.listar(u.getId());assertThat(ns).isNotEmpty().allMatch(n->n.getUsuario().getId().equals(u.getId()) && n.getFecha()!=null && n.getMensaje().contains(n.getReclamo().getCodigo()));
 }
 @Autowired NotificacionService notificaciones;
 @Test void SeguridadCsrfRegistroCierreYH2()throws Exception {
  mvc.perform(post("/registro")).andExpect(status().isForbidden());
  mvc.perform(post("/usuario/reclamos/3/cerrar").with(user("87654321").roles("USUARIO"))).andExpect(status().isForbidden());
  mvc.perform(get("/h2-console/").with(user("99999999").roles("ADMINISTRADOR"))).andExpect(status().isForbidden());
 }
}
