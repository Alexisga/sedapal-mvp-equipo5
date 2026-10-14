package com.utp.sedapal.services;
import com.utp.sedapal.models.*;
import com.utp.sedapal.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
@Service @Transactional(readOnly=true)
public class EvidenciaService {
 private final EvidenciaRepository repo; private final AuthService auth; private final ReclamoRepository reclamos;
 public EvidenciaService(EvidenciaRepository r,AuthService a,ReclamoRepository c){repo=r;auth=a;reclamos=c;}
 public record Adjunto(Long id,String nombre,boolean visible){}
 public List<Adjunto> listar(Long id,String doc){
  var u=auth.requerirPorDocumento(doc);var r=reclamos.findById(id).orElseThrow();
  if(u.getRol()!=RolUsuario.OPERADOR && !r.getUsuario().getId().equals(u.getId()))throw new AccessDeniedException("Acceso denegado.");
  return repo.findByAtencion_Orden_Reclamo_Id(id).stream().filter(e->u.getRol()==RolUsuario.OPERADOR||e.isVisibleCiudadano()).map(e->new Adjunto(e.getId(),e.getNombreOriginal(),e.isVisibleCiudadano())).toList();
 }
 private Evidencia requerir(Long id){return repo.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));}
 @Transactional public void publicar(Long id,String doc){
  if(auth.requerirPorDocumento(doc).getRol()!=RolUsuario.OPERADOR)throw new AccessDeniedException("Acceso denegado.");
  requerir(id).setVisibleCiudadano(true);
 }
 public record Archivo(String nombre,String tipo,byte[] contenido){}
 public Archivo abrir(Long id,String doc) throws java.io.IOException {
  var e=requerir(id);var u=auth.requerirPorDocumento(doc);var r=e.getAtencion().getOrden().getReclamo();
  boolean titular=u.getRol()==RolUsuario.USUARIO && e.isVisibleCiudadano() && r.getUsuario().getId().equals(u.getId());
  boolean tecnico=u.getRol()==RolUsuario.TECNICO && e.getAtencion().getOrden().getAsignacion().getTecnico().getUsuario()!=null && e.getAtencion().getOrden().getAsignacion().getTecnico().getUsuario().getId().equals(u.getId());
  if(!titular && !tecnico && u.getRol()!=RolUsuario.OPERADOR)throw new AccessDeniedException("Acceso denegado.");
  return new Archivo(e.getNombreOriginal(),e.getTipoContenido(),java.nio.file.Files.readAllBytes(java.nio.file.Path.of(e.getRuta())));
 }
}
