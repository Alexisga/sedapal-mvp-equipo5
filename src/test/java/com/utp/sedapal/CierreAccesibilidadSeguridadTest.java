package com.utp.sedapal;

import com.utp.sedapal.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class CierreAccesibilidadSeguridadTest {
    @Autowired MockMvc mvc;
    @Autowired AuthService auth;
    @Autowired PasswordEncoder encoder;
    @Test void errorServidorEstaVinculadoAlCampoYSinReexponerClave() throws Exception {
        var response=mvc.perform(post("/registro").with(csrf()).param("nombre","QA").param("documento","qaCierreError").param("password","corta"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(response).contains("aria-invalid=\"true\"","aria-describedby=\"password-error\"","id=\"password-error\"","role=\"alert\"");
        assertThat(response).doesNotContain("value=\"corta\"");
    }
    @Test void clavesDemoSonHashRobustoConfiguradoSinClaveDebilAnterior() {
        var u=auth.requerirPorDocumento("12345678");
        assertThat(u.getPassword()).startsWith("$2");
        assertThat(encoder.matches("ClaveDemoQA123",u.getPassword())).isTrue();
        assertThat(encoder.matches("1234",u.getPassword())).isFalse();
    }
}
