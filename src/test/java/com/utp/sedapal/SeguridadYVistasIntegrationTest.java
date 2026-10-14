package com.utp.sedapal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadYVistasIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void visitanteVeLoginYEsRedirigidoDesdeAreaPrivada() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("SEDAPAL Reclamos")));
        mvc.perform(get("/usuario"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "12345678", roles = "USUARIO")
    void usuarioSoloAccedeASuPortalYCsrfProtegeOperaciones() throws Exception {
        mvc.perform(get("/usuario"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuario/dashboard"));
        mvc.perform(get("/usuario/reclamos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuario/registrar_reclamo"));
        mvc.perform(get("/usuario/consultar").param("codigo", "REC-2026-0001"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuario/consultar_estado"));
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
        mvc.perform(post("/usuario/notificaciones/leer").param("id", "1"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/usuario/notificaciones/leer").with(csrf()).param("id", "1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "22222222", roles = "OPERADOR")
    void cargaVistaOperador() throws Exception {
        mvc.perform(get("/operador")).andExpect(status().isOk()).andExpect(view().name("operador/dashboard"));
        mvc.perform(get("/operador/reclamos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("operador/registrar_reclamo"));
    }

    @Test
    @WithMockUser(username = "44444444", roles = "EVALUADOR")
    void cargaVistaEvaluador() throws Exception {
        mvc.perform(get("/evaluador")).andExpect(status().isOk()).andExpect(view().name("evaluador/clasificar"));
    }

    @Test
    @WithMockUser(username = "55555555", roles = "SUPERVISOR")
    void cargaVistaSupervisor() throws Exception {
        mvc.perform(get("/supervisor")).andExpect(status().isOk()).andExpect(view().name("supervisor/ordenes"));
    }

    @Test
    @WithMockUser(username = "33333333", roles = "TECNICO")
    void cargaVistaTecnico() throws Exception {
        mvc.perform(get("/tecnico")).andExpect(status().isOk()).andExpect(view().name("tecnico/atencion"));
    }

    @Test
    @WithMockUser(username = "66666666", roles = "CALIDAD")
    void cargaVistaCalidadYExportaCsv() throws Exception {
        mvc.perform(get("/calidad/reportes")).andExpect(status().isOk()).andExpect(view().name("calidad/reportes"));
        mvc.perform(get("/calidad/reportes.csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"));
    }

    @Test
    @WithMockUser(username = "99999999", roles = "ADMINISTRADOR")
    void cargaVistaAdministrador() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isOk()).andExpect(view().name("admin/usuarios"));
    }
}
