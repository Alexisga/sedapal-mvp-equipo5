package com.utp.sedapal.controllers;
import com.utp.sedapal.dto.RegistroForm;
import com.utp.sedapal.services.RegistroService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller public class RegistroController {
 private final RegistroService service; public RegistroController(RegistroService s){service=s;}
 @GetMapping("/registro") public String form(Model m){m.addAttribute("registroForm",new RegistroForm());return "registro";}
 @PostMapping("/registro") public String save(@Valid @ModelAttribute RegistroForm registroForm, BindingResult r, Model m, RedirectAttributes a){
  if(r.hasErrors())return "registro";
  try {service.registrar(registroForm);} catch(IllegalArgumentException|org.springframework.dao.DataIntegrityViolationException e){m.addAttribute("error","El nombre de usuario ya está registrado.");return "registro";}
  a.addFlashAttribute("mensaje","Cuenta creada. Inicie sesión.");return "redirect:/login";
 }
}
