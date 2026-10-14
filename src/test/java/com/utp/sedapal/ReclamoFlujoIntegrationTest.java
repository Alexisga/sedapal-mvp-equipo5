package com.utp.sedapal;

import com.utp.sedapal.dto.AtencionForm;
import com.utp.sedapal.models.EstadoOrden;
import com.utp.sedapal.models.Reclamo;
import com.utp.sedapal.models.UsuarioServicio;
import com.utp.sedapal.services.AuthService;
import com.utp.sedapal.services.NotificacionService;
import com.utp.sedapal.services.ReclamoService;
import com.utp.sedapal.services.ReporteService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReclamoFlujoIntegrationTest {

    @Autowired ReclamoService reclamoService;
    @Autowired AuthService authService;
    @Autowired NotificacionService notificacionService;
    @Autowired ReporteService reporteService;

    @Test
    void registraReclamoConCodigoHistorialYNotificacion() {
        UsuarioServicio usuario = authService.requerirPorDocumento("87654321");

        Reclamo reclamo = reclamoService.registrarReclamo(usuario.getId(), "SUM-2001",
                "Fuga de agua", "Alta", "Existe una fuga continua frente al domicilio del usuario.",
                usuario.getDocumento());

        assertThat(reclamo.getCodigo()).startsWith("REC-");
        assertThat(reclamo.getEstado().getNombre()).isEqualTo(ReclamoService.REGISTRADO);
        assertThat(reclamoService.listarHistorial(reclamo.getId()))
                .extracting(h -> h.getEstado().getNombre())
                .containsExactly(ReclamoService.REGISTRADO);
        assertThat(notificacionService.listar(usuario.getId()))
                .anyMatch(n -> n.getMensaje().contains(reclamo.getCodigo()));
    }

    @Test
    void rechazaSuministroQueNoPerteneceAlUsuario() {
        UsuarioServicio usuario = authService.requerirPorDocumento("87654321");

        assertThatThrownBy(() -> reclamoService.registrarReclamo(usuario.getId(), "SUM-1001",
                "Fuga de agua", "Alta", "Descripción suficientemente extensa para validar.", usuario.getDocumento()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece");
    }

    @Test
    void completaFlujoAsignadoHastaCerradoConTrazabilidad() {
        Reclamo reclamo = reclamoService.listarTodos().stream()
                .filter(r -> ReclamoService.ASIGNADO.equals(r.getEstado().getNombre()))
                .findFirst().orElseThrow();
        var orden = reclamoService.listarOrdenesPorReclamo(reclamo.getId()).getFirst();
        UsuarioServicio tecnico = authService.requerirPorDocumento("33333333");

        reclamoService.iniciarAtencion(orden.getId(), tecnico.getId(), tecnico.getDocumento());
        AtencionForm form = new AtencionForm();
        form.setAccionesRealizadas("Se inspeccionó la conexión y se reguló la válvula de control.");
        form.setResultado("La presión quedó restablecida y se verificó el suministro.");
        form.setObservaciones("El usuario confirmó la normalización del servicio.");
        var atendida = reclamoService.registrarAtencion(orden.getId(), tecnico.getId(), form,
                tecnico.getDocumento());
        reclamoService.cerrar(reclamo.getId(), reclamo.getUsuario().getDocumento());

        assertThat(atendida.getEstado()).isEqualTo(EstadoOrden.ATENDIDA);
        assertThat(reclamoService.requerir(reclamo.getId()).getEstado().getNombre())
                .isEqualTo(ReclamoService.CERRADO);
        assertThat(reclamoService.listarHistorial(reclamo.getId()))
                .extracting(h -> h.getEstado().getNombre())
                .containsSubsequence(ReclamoService.REGISTRADO, ReclamoService.CLASIFICADO,
                        ReclamoService.ASIGNADO, ReclamoService.EN_ATENCION,
                        ReclamoService.ATENDIDO, ReclamoService.CERRADO);
    }

    @Test
    void generaResumenYCsvSinDependerDeSesionJpaAbierta() {
        List<Reclamo> reclamos = reporteService.filtrar(null, null, null, null, null);
        var resumen = reporteService.resumir(reclamos);
        String csv = reporteService.generarCsv(reclamos);

        assertThat(resumen.total()).isEqualTo(reclamos.size());
        assertThat(resumen.enProceso() + resumen.atendidos() + resumen.cerrados()).isEqualTo(resumen.total());
        assertThat(csv).startsWith("Código,Fecha,Usuario,Suministro,Zona,Tipo,Prioridad,Estado");
        assertThat(csv).contains("REC-");
    }
}
