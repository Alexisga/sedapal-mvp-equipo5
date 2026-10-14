package com.utp.sedapal.controllers;
import com.utp.sedapal.services.EvidenciaService;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.ui.Model;
@Controller public class EvidenciaController {
 private final EvidenciaService s;public EvidenciaController(EvidenciaService s){this.s=s;}
 @GetMapping("/evidencias/{id}") @ResponseBody
 public ResponseEntity<byte[]> abrir(@PathVariable Long id,Authentication a) throws java.io.IOException {
  var f=s.abrir(id,a.getName());return ResponseEntity.ok().header("Cache-Control","no-store").header("X-Content-Type-Options","nosniff").header("Content-Disposition",ContentDisposition.attachment().filename(f.nombre(),java.nio.charset.StandardCharsets.UTF_8).build().toString()).contentType(MediaType.parseMediaType(f.tipo())).body(f.contenido());
 }
 @GetMapping("/operador/evidencias") public String lista(@RequestParam Long reclamoId,Authentication a,Model m){m.addAttribute("evidencias",s.listar(reclamoId,a.getName()));return "operador/evidencias";}
 @PostMapping("/operador/evidencias/{id}/publicar") public String publicar(@PathVariable Long id,@RequestParam Long reclamoId,Authentication a){s.publicar(id,a.getName());return "redirect:/operador/evidencias?reclamoId="+reclamoId;}
}
