package com.utp.sedapal.controllers;
import com.utp.sedapal.services.ReclamoService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller public class ConsultaPublicaController {
 private final ReclamoService s; public ConsultaPublicaController(ReclamoService s){this.s=s;}
 @GetMapping("/consulta") @Transactional(readOnly=true)
 public String consulta(@RequestParam(required=false) String codigo, Model m){
  var r=s.buscarPorCodigo(codigo);
  r.ifPresent(v->{m.addAttribute("codigo",v.getCodigo());m.addAttribute("estado",v.getEstado().getNombre());
   m.addAttribute("eventos",s.listarHistorial(v.getId()).stream().map(h->new Evento(h.getEstado().getNombre(),h.getFecha())).toList());});
  if(codigo!=null && r.isEmpty())m.addAttribute("error","No se encontró una incidencia con ese código.");
  return "consulta_publica";
 }
 public record Evento(String estado, java.time.LocalDateTime fecha){}
}
