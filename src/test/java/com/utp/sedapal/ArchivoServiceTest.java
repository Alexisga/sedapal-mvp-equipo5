package com.utp.sedapal;

import com.utp.sedapal.services.ArchivoService;
import com.utp.sedapal.models.*;
import com.utp.sedapal.repository.EvidenciaRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class ArchivoServiceTest {
 @TempDir java.nio.file.Path dir;
 @ParameterizedTest @ValueSource(strings={"png","jpg"})
 void aceptaImagenRealYLaGuardaPrivada(String ext) throws Exception {
  var repo=mock(EvidenciaRepository.class);
  when(repo.save(any(Evidencia.class))).thenAnswer(i->i.getArgument(0));
  var service=new ArchivoService(repo,dir.toString());
  var r=new Reclamo();r.setId(1L);var o=new OrdenTrabajo();o.setReclamo(r);var a=new AtencionTecnica();a.setOrden(o);
  var bytes=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(8,8,BufferedImage.TYPE_INT_RGB),ext,bytes);
  var e=service.guardar(new MockMultipartFile("evidencia","qa."+ext,ext.equals("png")?"image/png":"image/jpeg",bytes.toByteArray()),"Imagen QA",a);
  assertThat(e.isVisibleCiudadano()).isFalse();
  assertThat(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(e.getRuta()))).isEqualTo(bytes.toByteArray());
  assertThat(e.getAtencion()).isSameAs(a);
 }
}
