package com.utp.sedapal;

import com.utp.sedapal.repository.UsuarioServicioRepository;
import com.utp.sedapal.repository.SuministroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties={"DEMO_PASSWORD=ClaveSinteticaSoloTestCloud21", "app.upload-dir=target/cloud-test-evidencias"})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "cloud-demo"})
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
class PublicDemoConfigTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioServicioRepository usuarios;
    @Autowired SuministroRepository suministros;

    @Test void datosInicialesAnonimosYDemostracionIdentificada() throws Exception {
        assertTrue(usuarios.count() > 0);
        assertTrue(usuarios.findAll().stream().allMatch(u -> u.getNombre().equals("Cuenta simulada")));
        assertTrue(suministros.findAll().stream().allMatch(s -> s.getDireccion().startsWith("Dirección simulada")));
        mvc.perform(get("/login").secure(true)).andExpect(status().isOk())
            .andExpect(content().string(containsString("DATOS SIMULADOS")))
            .andExpect(content().string(containsString("noindex,nofollow,noarchive")));
    }
    @Test void exigeHttpsYDeniegaConsola() throws Exception {
        mvc.perform(get("/login").secure(false)).andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("https://localhost/login"));
        mvc.perform(get("/h2-console/").secure(true)).andExpect(status().is3xxRedirection());
    }
}
