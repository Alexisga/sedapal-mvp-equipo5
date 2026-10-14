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
