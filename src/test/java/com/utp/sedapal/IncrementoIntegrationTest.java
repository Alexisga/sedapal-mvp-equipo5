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
 private byte[] pdfValido() {
  try(var d=new org.apache.pdfbox.pdmodel.PDDocument();var out=new java.io.ByteArrayOutputStream()){
   d.addPage(new org.apache.pdfbox.pdmodel.PDPage());d.save(out);return out.toByteArray();
  }catch(java.io.IOException e){throw new IllegalStateException(e);}
 }
 @Test void RNF06RechazaPdfConFirmaPeroSinEstructura() {
  assertThatThrownBy(()->archivos.guardar(new MockMultipartFile("evidencia","qa.pdf","application/pdf","%PDF-1.4\ncontenido falso\n%%EOF".getBytes()),"QA",new AtencionTecnica())).isInstanceOf(IllegalArgumentException.class);
 }

 @ParameterizedTest @ValueSource(ints={10,500})
 void HU06AceptaLimitesMotivo(int longitud) {
  var r=reclamos.buscarPorCodigo("REC-2026-0003").orElseThrow();reclamos.cerrar(r.getId(),"87654321");
  reclamos.solicitarReapertura(r.getId(),"a".repeat(longitud),"87654321");
  assertThat(r.getMotivoReapertura()).hasSize(longitud);
 }
 @ParameterizedTest @ValueSource(ints={0,9,501})
 void HU06RechazaFueraDeLimites(int longitud) {
  var r=reclamos.buscarPorCodigo("REC-2026-0003").orElseThrow();reclamos.cerrar(r.getId(),"87654321");
  assertThatThrownBy(()->reclamos.solicitarReapertura(r.getId(),"a".repeat(longitud),"87654321")).isInstanceOf(IllegalArgumentException.class);
  assertThat(r.getEstado().getNombre()).isEqualTo(ReclamoService.CERRADO);
 }
 @Test void HU05RechazaCierreNoAtendido() {
  var r=reclamos.buscarPorCodigo("REC-2026-0001").orElseThrow();
  assertThatThrownBy(()->reclamos.cerrar(r.getId(),"12345678")).isInstanceOf(IllegalArgumentException.class);
 }
 @Test void HU01RechazaDatosObligatoriosVacios() throws Exception {
  var u=auth.requerirPorDocumento("12345678");int n=reclamos.listarReclamosDeUsuario(u.getId()).size();
  mvc.perform(post("/usuario/reclamos").with(user("12345678").roles("USUARIO")).with(csrf())).andExpect(status().isOk());
  assertThat(reclamos.listarReclamosDeUsuario(u.getId())).hasSize(n);
 }
 @ParameterizedTest @ValueSource(strings={"nombre","documento","password"})
 void HU08RechazaCadaDatoObligatorioAusente(String campo) throws Exception {
  var req=post("/registro").with(csrf());
  if(!campo.equals("nombre"))req.param("nombre","QA");
  if(!campo.equals("documento"))req.param("documento","qaFaltante");
  if(!campo.equals("password"))req.param("password","ClaveQA123");
  mvc.perform(req).andExpect(status().isOk());assertThat(auth.buscarPorDocumento("qaFaltante")).isEmpty();
 }
 @Test void HU08RechazaClaveUnicodeExcedidaEnBytes() throws Exception {
  mvc.perform(post("/registro").with(csrf()).param("nombre","QA").param("documento","qaUnicode").param("password","é".repeat(40))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("72 bytes")));
  assertThat(auth.buscarPorDocumento("qaUnicode")).isEmpty();
 }
 @Test void HU04CadaTransicionNotificaCodigoDestinatarioYFecha() {
  var r=reclamos.buscarPorCodigo("REC-2026-0002").orElseThrow();var o=reclamos.listarOrdenesPorReclamo(r.getId()).getFirst();
  var tecnico=auth.requerirPorDocumento("33333333");reclamos.iniciarAtencion(o.getId(),tecnico.getId(),tecnico.getDocumento());
  var f=new AtencionForm();f.setAccionesRealizadas("Acciones ficticias para comprobar los avisos.");f.setResultado("Resultado ficticio de una atención de QA.");
  reclamos.registrarAtencion(o.getId(),tecnico.getId(),f,tecnico.getDocumento());reclamos.cerrar(r.getId(),"12345678");
  reclamos.solicitarReapertura(r.getId(),"El problema ficticio continúa sin solución.","12345678");
  var avisos=notificaciones.listar(r.getUsuario().getId()).stream().filter(n->n.getReclamo().getId().equals(r.getId())).toList();
  assertThat(avisos).extracting(Notificacion::getTipo).containsExactlyInAnyOrder("REGISTRO","CLASIFICACION","ASIGNACION","ATENCION","RESULTADO","CIERRE","REAPERTURA");
  assertThat(avisos).allMatch(n->n.getFecha()!=null && n.getMensaje().contains(r.getCodigo()) && n.getUsuario().getId().equals(r.getUsuario().getId()));
 }
 @Test void RNF06AceptaPdfCifradoReconocidoSinPublicarlo() throws Exception {
  byte[] bytes;
  try(var d=new org.apache.pdfbox.pdmodel.PDDocument();var out=new java.io.ByteArrayOutputStream()) {
   d.addPage(new org.apache.pdfbox.pdmodel.PDPage());
   var policy=new org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy("OwnerQA123","UserQA123",new org.apache.pdfbox.pdmodel.encryption.AccessPermission());
   policy.setEncryptionKeyLength(128);d.protect(policy);d.save(out);bytes=out.toByteArray();
  }
  var a=new AtencionTecnica();a.setOrden(ordenes.findAll().getFirst());a.setAccionesRealizadas("QA");a.setResultado("QA");ar.save(a);
  var e=archivos.guardar(new MockMultipartFile("evidencia","cifrado.pdf","application/pdf",bytes),"QA",a);
  assertThat(e.isVisibleCiudadano()).isFalse();assertThat(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(e.getRuta()))).isEqualTo(bytes);
 }
 @Test void HU08AceptaNombreDisponibleDeUnCaracterYNormalizaEspacios() throws Exception {
  mvc.perform(post("/registro").with(csrf()).param("nombre","QA").param("documento"," x ").param("password","ClaveQA123")).andExpect(status().is3xxRedirection());
  assertThat(auth.buscarPorDocumento("x")).isPresent();
 }
 @Test void HU02ProgresoNoMarcaEtapasQueNuncaOcurrieron() throws Exception {
  var r=mvc.perform(get("/usuario/consultar").param("codigo","REC-2026-0003").with(user("87654321").roles("USUARIO"))).andExpect(status().isOk()).andReturn();
  assertThat(java.util.regex.Pattern.compile("timeline-step\\s+active").matcher(r.getResponse().getContentAsString()).results().count()).isEqualTo(2);
 }
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
  var e=archivos.guardar(new MockMultipartFile("evidencia","qa.pdf","application/pdf",pdfValido()),"QA",a);
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
  for(int n=0;n<5;n++)archivos.guardar(new MockMultipartFile("evidencia","qa.pdf","application/pdf",pdfValido()),"QA",a);
  assertThatThrownBy(()->archivos.guardar(new MockMultipartFile("evidencia","qa.pdf","application/pdf",pdfValido()),"QA",a)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("cinco");
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
