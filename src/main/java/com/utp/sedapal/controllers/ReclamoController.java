package com.utp.sedapal.controllers;

import com.utp.sedapal.dto.ReclamoForm;
import com.utp.sedapal.models.Reclamo;
import com.utp.sedapal.models.UsuarioServicio;
import com.utp.sedapal.services.AuthService;
import com.utp.sedapal.services.NotificacionService;
import com.utp.sedapal.services.ReclamoService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReclamoController {
    private final ReclamoService reclamoService;
    private final AuthService authService;
    private final NotificacionService notificacionService;

    public ReclamoController(ReclamoService reclamoService, AuthService authService,
                             NotificacionService notificacionService) {
        this.reclamoService = reclamoService;
        this.authService = authService;
        this.notificacionService = notificacionService;
    }

    @GetMapping("/usuario")
    public String dashboard(Authentication authentication, Model model) {
        UsuarioServicio usuario = actual(authentication);
        model.addAttribute("reclamos", reclamoService.listarReclamosDeUsuario(usuario.getId()));
        model.addAttribute("notificaciones", notificacionService.listar(usuario.getId()));
        model.addAttribute("notificacionesNoLeidas", notificacionService.contarNoLeidas(usuario.getId()));
        return "usuario/dashboard";
    }

    @GetMapping("/usuario/reclamos/nuevo")
    public String nuevo(Authentication authentication, Model model) {
        UsuarioServicio usuario = actual(authentication);
        if (!model.containsAttribute("reclamoForm")) model.addAttribute("reclamoForm", new ReclamoForm());
        model.addAttribute("suministros", reclamoService.listarSuministrosDeUsuario(usuario.getId()));
        return "usuario/registrar_reclamo";
    }

    @PostMapping("/usuario/reclamos")
    public String registrar(@Valid @ModelAttribute ReclamoForm reclamoForm, BindingResult result,
                            Authentication authentication, Model model, RedirectAttributes redirect) {
        UsuarioServicio usuario = actual(authentication);
        if (result.hasErrors()) {
            model.addAttribute("suministros", reclamoService.listarSuministrosDeUsuario(usuario.getId()));
            return "usuario/registrar_reclamo";
        }
        try {
            Reclamo reclamo = reclamoService.registrarReclamo(usuario.getId(), reclamoForm.getCodigoSuministro(),
                    reclamoForm.getTipoIncidencia(), reclamoForm.getPrioridad(), reclamoForm.getDescripcion(),
                    usuario.getDocumento());
            redirect.addFlashAttribute("mensaje", "Reclamo registrado con código " + reclamo.getCodigo() + ".");
            return "redirect:/usuario/consultar?codigo=" + reclamo.getCodigo();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("suministros", reclamoService.listarSuministrosDeUsuario(usuario.getId()));
            return "usuario/registrar_reclamo";
        }
    }

    @GetMapping("/usuario/consultar")
    public String consultar(@RequestParam(required = false) String codigo, Authentication authentication, Model model) {
        UsuarioServicio usuario = actual(authentication);
        model.addAttribute("codigoBuscado", codigo);
        model.addAttribute("estados", reclamoService.listarEstados());
        Optional<Reclamo> reclamo = reclamoService.buscarPorCodigoDeUsuario(codigo, usuario.getId());
        if (reclamo.isPresent()) {
            model.addAttribute("reclamo", reclamo.get());
            model.addAttribute("estadoIndex", reclamoService.posicionEstado(reclamo.get()));
            model.addAttribute("ordenes", reclamoService.listarOrdenesPorReclamo(reclamo.get().getId()));
            var historial = reclamoService.listarHistorial(reclamo.get().getId());
            model.addAttribute("historial", historial);
            // El historial se recupera ordenado cronológicamente en ReclamoService.
            // Si todavía no existe una transición, usar la fecha de registro.
            model.addAttribute("ultimaActualizacion", historial.isEmpty()
                    ? reclamo.get().getFechaRegistro()
                    : historial.get(historial.size() - 1).getFecha());
        } else if (codigo != null && !codigo.isBlank()) {
            model.addAttribute("error", "No se encontró un reclamo propio con el código indicado.");
        }
        return "usuario/consultar_estado";
    }

    @PostMapping("/usuario/notificaciones/leer")
    public String marcarNotificacion(@RequestParam Long id, Authentication authentication) {
        notificacionService.marcarLeida(id, actual(authentication).getId());
        return "redirect:/usuario";
    }

    private UsuarioServicio actual(Authentication authentication) {
        return authService.requerirPorDocumento(authentication.getName());
    }
}
