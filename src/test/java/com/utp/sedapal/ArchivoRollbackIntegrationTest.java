package com.utp.sedapal;

import com.utp.sedapal.models.AtencionTecnica;
import com.utp.sedapal.repository.AtencionTecnicaRepository;
import com.utp.sedapal.repository.OrdenTrabajoRepository;
import com.utp.sedapal.services.ArchivoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.mock.web.MockMultipartFile;
import java.util.concurrent.atomic.AtomicReference;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test")
class ArchivoRollbackIntegrationTest {
    @Autowired ArchivoService archivos;
    @Autowired OrdenTrabajoRepository ordenes;
    @Autowired AtencionTecnicaRepository atenciones;
    @Autowired PlatformTransactionManager manager;

    @Test void eliminaSoloArchivoNuevoCuandoRevierteLaBaseDeDatos() throws Exception {
        var bytes=new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(8,8,1),"png",bytes);
        AtomicReference<Path> creado=new AtomicReference<>();
        new TransactionTemplate(manager).executeWithoutResult(status -> {
            var a=new AtencionTecnica();a.setOrden(ordenes.findAll().getFirst());
            a.setAccionesRealizadas("Prueba rollback ficticia");a.setResultado("Prueba rollback");atenciones.save(a);
            var e=archivos.guardar(new MockMultipartFile("evidencia","rollback.png","image/png",bytes.toByteArray()),"QA",a);
            creado.set(Path.of(e.getRuta()));assertThat(creado.get()).exists();status.setRollbackOnly();
        });
        assertThat(creado.get()).doesNotExist();
    }
}
