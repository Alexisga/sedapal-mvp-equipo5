package com.utp.sedapal.controllers;

import com.utp.sedapal.dto.UsuarioForm;
import com.utp.sedapal.models.RolUsuario;
import com.utp.sedapal.services.AdminService;
import com.utp.sedapal.services.AuditoriaService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    private final AdminService adminService;
    private final AuditoriaService auditoriaService;
    public AdminController(AdminService adminService, AuditoriaService auditoriaService) {
        this.adminService = adminService; this.auditoriaService = auditoriaService;
    }

    @GetMapping("/admin")
    public String panel(Model model) {
        if (!model.containsAttribute("usuarioForm")) model.addAttribute("usuarioForm", new UsuarioForm());
        model.addAttribute("usuarios", adminService.listarUsuarios());
        model.addAttribute("roles", RolUsuario.values());
        model.addAttribute("auditorias", auditoriaService.listarRecientes());
        return "admin/usuarios";
    }

    @PostMapping("/admin/usuarios")
    public String crear(@Valid @ModelAttribute UsuarioForm form, BindingResult result, Authentication auth,
                        Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("usuarios", adminService.listarUsuarios()); model.addAttribute("roles", RolUsuario.values());
            model.addAttribute("auditorias", auditoriaService.listarRecientes());
            return "admin/usuarios";
        }
        try {
            adminService.crear(form, auth.getName()); redirect.addFlashAttribute("mensaje", "Usuario creado correctamente.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin";
    }

    @PostMapping("/admin/usuarios/{id}")
    public String actualizar(@PathVariable Long id, @RequestParam RolUsuario rol,
                             @RequestParam(defaultValue = "false") boolean activo,
                             Authentication auth, RedirectAttributes redirect) {
        adminService.actualizarRolYEstado(id, rol, activo, auth.getName());
        redirect.addFlashAttribute("mensaje", "Usuario actualizado.");
        return "redirect:/admin";
    }
}
