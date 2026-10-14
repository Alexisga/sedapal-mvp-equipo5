package com.utp.sedapal.services;

import com.utp.sedapal.models.AtencionTecnica;
import com.utp.sedapal.models.Evidencia;
import com.utp.sedapal.repository.EvidenciaRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ArchivoService {
    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png", "application/pdf");
    private final EvidenciaRepository evidenciaRepository;
    private final Path directorio;

    public ArchivoService(EvidenciaRepository evidenciaRepository,
                          @Value("${app.upload-dir}") String directorio) {
        this.evidenciaRepository = evidenciaRepository;
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    private void validarContenido(MultipartFile f, String ext) {
        if (f.getSize() > 5L * 1024 * 1024) throw new IllegalArgumentException("El archivo excede 5 MiB.");
        try {
            byte[] b=f.getBytes();
            boolean pdf=ext.equals("pdf") && "application/pdf".equals(f.getContentType()) && b.length>8 && new String(b,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-") && new String(b,java.nio.charset.StandardCharsets.ISO_8859_1).contains("%%EOF");
            boolean png=ext.equals("png") && "image/png".equals(f.getContentType()) && b.length>8 && java.util.Arrays.equals(java.util.Arrays.copyOf(b,8),new byte[]{(byte)137,80,78,71,13,10,26,10});
            boolean jpg=(ext.equals("jpg")||ext.equals("jpeg")) && "image/jpeg".equals(f.getContentType()) && b.length>3 && (b[0]&255)==255 && (b[1]&255)==216 && (b[2]&255)==255;
            if (!(pdf || ((png || jpg) && javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(b))!=null)))
                throw new IllegalArgumentException("La extensión, el tipo y el contenido deben corresponder a JPG, PNG o PDF.");
        } catch(IOException e){throw new IllegalArgumentException("No se pudo validar el archivo.");}
    }

    public Evidencia guardar(MultipartFile archivo, String descripcion, AtencionTecnica atencion) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        if (!TIPOS_PERMITIDOS.contains(archivo.getContentType())) {
            throw new IllegalArgumentException("La evidencia debe ser una imagen JPG/PNG o un archivo PDF.");
        }
        String nombreOriginal = StringUtils.cleanPath(
                archivo.getOriginalFilename() == null ? "evidencia" : archivo.getOriginalFilename());
        if (nombreOriginal.contains("..")) {
            throw new IllegalArgumentException("Nombre de archivo no válido.");
        }
        String extension = StringUtils.getFilenameExtension(nombreOriginal);
        extension = extension == null ? "bin" : extension.toLowerCase(Locale.ROOT);
        validarContenido(archivo, extension);
        if (evidenciaRepository.countByAtencion_Orden_Reclamo_Id(atencion.getOrden().getReclamo().getId()) >= 5)
            throw new IllegalArgumentException("Se permiten hasta cinco evidencias por incidencia.");
        String almacenado = UUID.randomUUID() + "." + extension;
        Path destino = directorio.resolve(almacenado).normalize();
        if (!destino.startsWith(directorio)) {
            throw new IllegalArgumentException("Nombre de archivo no válido.");
        }
        try {
            Files.createDirectories(directorio);
            archivo.transferTo(destino);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar la evidencia técnica.", ex);
        }

        Evidencia evidencia = new Evidencia();
        evidencia.setNombreOriginal(Path.of(nombreOriginal).getFileName().toString());
        evidencia.setNombreAlmacenado(almacenado);
        evidencia.setTipoContenido(archivo.getContentType());
        evidencia.setRuta(destino.toString());
        evidencia.setFechaCarga(LocalDateTime.now());
        evidencia.setDescripcion(descripcion);
        evidencia.setAtencion(atencion);
        return evidenciaRepository.save(evidencia);
    }
}
