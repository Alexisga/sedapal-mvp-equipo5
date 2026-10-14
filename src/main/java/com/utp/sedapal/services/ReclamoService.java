package com.utp.sedapal.services;

import com.utp.sedapal.dto.AtencionForm;
import com.utp.sedapal.models.AsignacionTecnica;
import com.utp.sedapal.models.AtencionTecnica;
import com.utp.sedapal.models.EstadoOrden;
import com.utp.sedapal.models.EstadoReclamo;
import com.utp.sedapal.models.HistorialEstado;
import com.utp.sedapal.models.Incidencia;
import com.utp.sedapal.models.OrdenTrabajo;
import com.utp.sedapal.models.Reclamo;
import com.utp.sedapal.models.Suministro;
import com.utp.sedapal.models.Tecnico;
import com.utp.sedapal.models.UsuarioServicio;
import com.utp.sedapal.repository.AsignacionTecnicaRepository;
import com.utp.sedapal.repository.AtencionTecnicaRepository;
import com.utp.sedapal.repository.EstadoReclamoRepository;
import com.utp.sedapal.repository.HistorialEstadoRepository;
import com.utp.sedapal.repository.OrdenTrabajoRepository;
import com.utp.sedapal.repository.ReclamoRepository;
import com.utp.sedapal.repository.SuministroRepository;
import com.utp.sedapal.repository.TecnicoRepository;
import com.utp.sedapal.repository.UsuarioServicioRepository;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReclamoService {
    public static final String REGISTRADO = "Registrado";
    public static final String CLASIFICADO = "Clasificado";
    public static final String ASIGNADO = "Asignado";
    public static final String EN_ATENCION = "En atención";
    public static final String ATENDIDO = "Atendido";
    public static final String REAPERTURA_SOLICITADA = "Reapertura solicitada";
    public static final String CERRADO = "Cerrado";

    private final ReclamoRepository reclamoRepository;
    private final SuministroRepository suministroRepository;
    private final EstadoReclamoRepository estadoRepository;
    private final UsuarioServicioRepository usuarioRepository;
    private final TecnicoRepository tecnicoRepository;
    private final OrdenTrabajoRepository ordenRepository;
    private final AsignacionTecnicaRepository asignacionRepository;
    private final AtencionTecnicaRepository atencionRepository;
    private final HistorialEstadoRepository historialRepository;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;
    private final ArchivoService archivoService;

    public ReclamoService(ReclamoRepository reclamoRepository, SuministroRepository suministroRepository,
                          EstadoReclamoRepository estadoRepository, UsuarioServicioRepository usuarioRepository,
                          TecnicoRepository tecnicoRepository, OrdenTrabajoRepository ordenRepository,
                          AsignacionTecnicaRepository asignacionRepository, AtencionTecnicaRepository atencionRepository,
                          HistorialEstadoRepository historialRepository, NotificacionService notificacionService,
                          AuditoriaService auditoriaService, ArchivoService archivoService) {
        this.reclamoRepository = reclamoRepository;
        this.suministroRepository = suministroRepository;
        this.estadoRepository = estadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.ordenRepository = ordenRepository;
        this.asignacionRepository = asignacionRepository;
        this.atencionRepository = atencionRepository;
        this.historialRepository = historialRepository;
        this.notificacionService = notificacionService;
        this.auditoriaService = auditoriaService;
        this.archivoService = archivoService;
    }

    public List<Suministro> listarSuministrosDeUsuario(Long usuarioId) {
        return suministroRepository.findByUsuarioId(usuarioId);
    }

    public List<Suministro> listarSuministros() { return suministroRepository.findAll(); }
    public List<UsuarioServicio> listarUsuariosServicio() {
        return usuarioRepository.findByRol(com.utp.sedapal.models.RolUsuario.USUARIO);
    }
    public List<Reclamo> listarReclamosDeUsuario(Long usuarioId) {
        return reclamoRepository.findByUsuarioIdOrderByFechaRegistroDesc(usuarioId);
    }
    public List<Reclamo> listarTodos() { return reclamoRepository.findAllByOrderByFechaRegistroDesc(); }
    public List<Reclamo> listarParaClasificar() { return reclamoRepository.findByEstado_NombreOrderByFechaRegistroAsc(REGISTRADO); }
    public List<Reclamo> listarClasificados() { return reclamoRepository.findByEstado_NombreOrderByFechaRegistroAsc(CLASIFICADO); }
    public List<OrdenTrabajo> listarOrdenesPendientes() {
        return ordenRepository.findByEstadoOrderByFechaCreacionAsc(EstadoOrden.PENDIENTE_ASIGNACION);
    }
    public List<EstadoReclamo> listarEstados() { return estadoRepository.findAllByOrderByOrdenAsc(); }
    public List<OrdenTrabajo> listarOrdenesPorReclamo(Long reclamoId) {
        return ordenRepository.findByReclamoIdOrderByFechaAsignacionDesc(reclamoId);
    }
    public List<HistorialEstado> listarHistorial(Long reclamoId) {
        return historialRepository.findByReclamoIdOrderByFechaAsc(reclamoId);
    }
    public Optional<Reclamo> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        return reclamoRepository.findByCodigo(codigo.trim().toUpperCase());
    }
    public Optional<Reclamo> buscarPorCodigoDeUsuario(String codigo, Long usuarioId) {
        return buscarPorCodigo(codigo).filter(r -> r.getUsuario().getId().equals(usuarioId));
    }
    public Reclamo requerir(Long id) {
        return reclamoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Reclamo no encontrado."));
    }

    public int posicionEstado(Reclamo reclamo) {
        return listarEstados().stream().map(EstadoReclamo::getNombre).toList().indexOf(reclamo.getEstado().getNombre());
    }

    @Transactional
    public Reclamo registrarCiudadano(Long usuarioId, com.utp.sedapal.dto.ReclamoForm f, String responsable) {
        String codigo=f.getCodigoSuministro();
        if(codigo==null || codigo.isBlank()) {
            validarTexto(f.getDireccion(),5,"Ingrese una ubicación de al menos cinco caracteres.");
            validarTexto(f.getDistrito(),2,"Ingrese el distrito.");
            Suministro s=new Suministro();s.setUsuario(usuarioRepository.findById(usuarioId).orElseThrow());
            s.setCodigoSuministro("LOCAL-"+java.util.UUID.randomUUID());s.setDireccion(f.getDireccion().trim());s.setDistrito(f.getDistrito().trim());
            codigo=suministroRepository.save(s).getCodigoSuministro();
        }
        return registrarReclamo(usuarioId,codigo,f.getTipoIncidencia(),f.getPrioridad(),f.getDescripcion(),responsable);
    }
    @Transactional
    public Reclamo registrarReclamo(Long usuarioId, String codigoSuministro, String tipoIncidencia,
                                    String prioridad, String descripcion, String responsable) {
        validarTexto(descripcion, 15, "La descripción debe tener al menos 15 caracteres.");
        UsuarioServicio usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        Suministro suministro = suministroRepository.findByCodigoSuministroAndUsuarioId(codigoSuministro, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El suministro no pertenece al usuario seleccionado."));
        EstadoReclamo registrado = estado(REGISTRADO);

        Reclamo reclamo = new Reclamo();
        reclamo.setCodigo(generarCodigoReclamo());
        reclamo.setFechaRegistro(LocalDateTime.now());
        reclamo.setDescripcion(descripcion.trim());
        reclamo.setTipoIncidencia(tipoIncidencia);
        reclamo.setPrioridad(prioridad);
        reclamo.setUsuario(usuario);
        reclamo.setSuministro(suministro);
        reclamo.setEstado(registrado);

        Incidencia incidencia = new Incidencia();
        incidencia.setTipo(tipoIncidencia);
        incidencia.setPrioridad(prioridad);
        incidencia.setDescripcion(descripcion.trim());
        incidencia.setReclamo(reclamo);
        reclamo.getIncidencias().add(incidencia);
        reclamo = reclamoRepository.save(reclamo);
        // Identity is allocated by the database, including concurrent registrations.
        reclamo.setCodigo(String.format("REC-%d-%04d", Year.now().getValue(), reclamo.getId()));
        registrarHistorial(reclamo, registrado, responsable, "Reclamo registrado y código generado.");
        notificacionService.crear(usuario, reclamo, "REGISTRO", "Su reclamo " + reclamo.getCodigo() + " fue registrado.");
        auditoriaService.registrar(responsable, "REGISTRAR_RECLAMO", "Reclamo", reclamo.getId(), reclamo.getCodigo());
        return reclamo;
    }

    @Transactional
    public Reclamo clasificar(Long reclamoId, String tipo, String prioridad, boolean requiereCampo,
                              String observacion, String responsable) {
        Reclamo reclamo = requerir(reclamoId);
        exigirEstado(reclamo, REGISTRADO);
        validarTexto(observacion, 10, "Ingrese una evaluación de al menos 10 caracteres.");
        reclamo.setTipoIncidencia(tipo);
        reclamo.setPrioridad(prioridad);
        reclamo.setRequiereAtencionCampo(requiereCampo);
        reclamo.getIncidencias().forEach(i -> { i.setTipo(tipo); i.setPrioridad(prioridad); });
        EstadoReclamo nuevoEstado = estado(requiereCampo ? CLASIFICADO : ATENDIDO);
        reclamo.setEstado(nuevoEstado);
        reclamoRepository.save(reclamo);
        registrarHistorial(reclamo, nuevoEstado, responsable, observacion);
        String mensaje = requiereCampo ? "fue clasificado y requiere atención en campo." : "fue atendido mediante orientación.";
        notificacionService.crear(reclamo.getUsuario(), reclamo, "CLASIFICACION",
                "Su reclamo " + reclamo.getCodigo() + " " + mensaje);
        auditoriaService.registrar(responsable, "CLASIFICAR_RECLAMO", "Reclamo", reclamoId, mensaje);
        return reclamo;
    }

    @Transactional
    public OrdenTrabajo generarOrden(Long reclamoId, String responsable) {
        Reclamo reclamo = requerir(reclamoId);
        exigirEstado(reclamo, CLASIFICADO);
        if (!reclamo.isRequiereAtencionCampo()) throw new IllegalArgumentException("El reclamo no requiere atención en campo.");
        if (ordenRepository.existsByReclamoIdAndEstadoIn(reclamoId,
                List.of(EstadoOrden.PENDIENTE_ASIGNACION, EstadoOrden.ASIGNADA, EstadoOrden.EN_ATENCION))) {
            throw new IllegalArgumentException("El reclamo ya tiene una orden activa.");
        }
        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setCodigo(generarCodigoOrden());
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setEstado(EstadoOrden.PENDIENTE_ASIGNACION);
        orden.setResultado("Pendiente de asignación técnica.");
        orden.setReclamo(reclamo);
        orden = ordenRepository.save(orden);
        registrarHistorial(reclamo, reclamo.getEstado(), responsable, "Orden " + orden.getCodigo() + " generada.");
        auditoriaService.registrar(responsable, "GENERAR_ORDEN", "OrdenTrabajo", orden.getId(), orden.getCodigo());
        return orden;
    }

    @Transactional
    public OrdenTrabajo asignarTecnico(Long ordenId, Long tecnicoId, String responsable) {
        OrdenTrabajo orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new IllegalArgumentException("Orden de trabajo no encontrada."));
        if (orden.getEstado() != EstadoOrden.PENDIENTE_ASIGNACION) {
            throw new IllegalArgumentException("La orden ya fue asignada.");
        }
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new IllegalArgumentException("Técnico no encontrado."));
        if (!tecnico.isDisponible()) throw new IllegalArgumentException("El técnico seleccionado no está disponible.");

        AsignacionTecnica asignacion = new AsignacionTecnica();
        asignacion.setOrden(orden);
        asignacion.setTecnico(tecnico);
        asignacion.setFechaAsignacion(LocalDateTime.now());
        asignacion.setActiva(true);
        asignacionRepository.save(asignacion);
        orden.setAsignacion(asignacion);
        orden.setFechaAsignacion(asignacion.getFechaAsignacion());
        orden.setEstado(EstadoOrden.ASIGNADA);
        orden.setResultado("Orden asignada; pendiente de inicio de atención.");
        tecnico.setDisponible(false);
        tecnicoRepository.save(tecnico);
        cambiarEstado(orden.getReclamo(), ASIGNADO, responsable,
                "Orden asignada al técnico " + tecnico.getNombres() + ".");
        ordenRepository.save(orden);
        notificacionService.crear(orden.getReclamo().getUsuario(), orden.getReclamo(), "ASIGNACION",
                "Se asignó un técnico al reclamo " + orden.getReclamo().getCodigo() + ".");
        auditoriaService.registrar(responsable, "ASIGNAR_TECNICO", "OrdenTrabajo", ordenId, tecnico.getNombres());
        return orden;
    }

    @Transactional
    public OrdenTrabajo iniciarAtencion(Long ordenId, Long usuarioTecnicoId, String responsable) {
        OrdenTrabajo orden = requerirOrdenTecnico(ordenId, usuarioTecnicoId);
        if (orden.getEstado() != EstadoOrden.ASIGNADA) throw new IllegalArgumentException("La orden no está disponible para iniciar.");
        orden.setEstado(EstadoOrden.EN_ATENCION);
        orden.setFechaInicio(LocalDateTime.now());
        cambiarEstado(orden.getReclamo(), EN_ATENCION, responsable, "El técnico inició la atención en campo.");
        notificacionService.crear(orden.getReclamo().getUsuario(), orden.getReclamo(), "ATENCION",
                "La atención del reclamo " + orden.getReclamo().getCodigo() + " se encuentra en curso.");
        auditoriaService.registrar(responsable, "INICIAR_ATENCION", "OrdenTrabajo", ordenId, orden.getCodigo());
        return ordenRepository.save(orden);
    }

    @Transactional
    public OrdenTrabajo registrarAtencion(Long ordenId, Long usuarioTecnicoId, AtencionForm form, String responsable) {
        OrdenTrabajo orden = requerirOrdenTecnico(ordenId, usuarioTecnicoId);
        if (orden.getEstado() == EstadoOrden.ASIGNADA) iniciarAtencionInterna(orden, responsable);
        if (orden.getEstado() != EstadoOrden.EN_ATENCION) throw new IllegalArgumentException("La orden no está en atención.");

        reclamoRepository.bloquear(orden.getReclamo().getId()).orElseThrow();
        AtencionTecnica atencion = new AtencionTecnica();
        atencion.setOrden(orden);
        atencion.setFechaRegistro(LocalDateTime.now());
        atencion.setAccionesRealizadas(form.getAccionesRealizadas().trim());
        atencion.setResultado(form.getResultado().trim());
        atencion.setObservaciones(form.getObservaciones());
        atencion = atencionRepository.save(atencion);
        archivoService.guardar(form.getEvidencia(), form.getDescripcionEvidencia(), atencion);

        orden.setAtencion(atencion);
        orden.setResultado(form.getResultado().trim());
        orden.setFechaCierre(LocalDateTime.now());
        orden.setEstado(EstadoOrden.ATENDIDA);
        orden.getAsignacion().setActiva(false);
        orden.getAsignacion().getTecnico().setDisponible(true);
        tecnicoRepository.save(orden.getAsignacion().getTecnico());
        cambiarEstado(orden.getReclamo(), ATENDIDO, responsable, "Resultado técnico registrado: " + form.getResultado());
        notificacionService.crear(orden.getReclamo().getUsuario(), orden.getReclamo(), "RESULTADO",
                "La atención del reclamo " + orden.getReclamo().getCodigo() + " fue registrada.");
        auditoriaService.registrar(responsable, "REGISTRAR_ATENCION", "OrdenTrabajo", ordenId, form.getResultado());
        return ordenRepository.save(orden);
    }

    @Transactional
    public Reclamo cerrar(Long reclamoId, String responsable) {
        Reclamo reclamo = titularBloqueado(reclamoId, responsable);
        exigirEstado(reclamo, ATENDIDO);
        reclamo.setConfirmadoPor(responsable);
        reclamo.setFechaConfirmacion(LocalDateTime.now());
        cambiarEstado(reclamo, CERRADO, responsable, "Reclamo cerrado después de comunicar el resultado.");
        notificacionService.crear(reclamo.getUsuario(), reclamo, "CIERRE", "Su reclamo " + reclamo.getCodigo() + " fue cerrado.");
        auditoriaService.registrar(responsable, "CERRAR_RECLAMO", "Reclamo", reclamoId, reclamo.getCodigo());
        return reclamo;
    }

    private Reclamo titularBloqueado(Long id, String documento) {
        Reclamo r = reclamoRepository.bloquear(id).orElseThrow(() -> new IllegalArgumentException("Caso no disponible."));
        if (!r.getUsuario().getDocumento().equals(documento)) throw new org.springframework.security.access.AccessDeniedException("Acceso denegado.");
        return r;
    }
    @Transactional public void solicitarReapertura(Long id, String motivo, String documento) {
        Reclamo r = titularBloqueado(id, documento);
        exigirEstado(r, CERRADO);
        if (motivo == null || motivo.trim().length() < 10 || motivo.trim().length() > 500)
            throw new IllegalArgumentException("El motivo debe tener entre 10 y 500 caracteres.");
        r.setMotivoReapertura(motivo.trim()); r.setFechaSolicitudReapertura(LocalDateTime.now());
        cambiarEstado(r, REAPERTURA_SOLICITADA, documento, "Solicitud de reapertura registrada; pendiente de evaluación interna.");
        notificacionService.crear(r.getUsuario(), r, "REAPERTURA", "Su reclamo " + r.getCodigo() + " tiene una solicitud de reapertura pendiente.");
        auditoriaService.registrar(documento, "SOLICITAR_REAPERTURA", "Reclamo", id, motivo.trim());
    }

    private void iniciarAtencionInterna(OrdenTrabajo orden, String responsable) {
        orden.setEstado(EstadoOrden.EN_ATENCION);
        orden.setFechaInicio(LocalDateTime.now());
        cambiarEstado(orden.getReclamo(), EN_ATENCION, responsable, "El técnico inició la atención en campo.");
    }

    private OrdenTrabajo requerirOrdenTecnico(Long ordenId, Long usuarioTecnicoId) {
        OrdenTrabajo orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new IllegalArgumentException("Orden de trabajo no encontrada."));
        if (orden.getAsignacion() == null || orden.getAsignacion().getTecnico().getUsuario() == null
                || !orden.getAsignacion().getTecnico().getUsuario().getId().equals(usuarioTecnicoId)) {
            throw new IllegalArgumentException("La orden no pertenece al técnico autenticado.");
        }
        return orden;
    }

    private void cambiarEstado(Reclamo reclamo, String nombre, String responsable, String observacion) {
        EstadoReclamo estado = estado(nombre);
        reclamo.setEstado(estado);
        reclamoRepository.save(reclamo);
        registrarHistorial(reclamo, estado, responsable, observacion);
    }

    private void registrarHistorial(Reclamo reclamo, EstadoReclamo estado, String responsable, String observacion) {
        HistorialEstado historial = new HistorialEstado();
        historial.setReclamo(reclamo);
        historial.setEstado(estado);
        historial.setFecha(LocalDateTime.now());
        historial.setResponsable(responsable == null ? "SISTEMA" : responsable);
        historial.setObservacion(observacion);
        historialRepository.save(historial);
    }

    private EstadoReclamo estado(String nombre) {
        return estadoRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Estado no configurado: " + nombre));
    }

    private void exigirEstado(Reclamo reclamo, String esperado) {
        if (!esperado.equals(reclamo.getEstado().getNombre())) {
            throw new IllegalArgumentException("El reclamo debe estar en estado " + esperado + ".");
        }
    }

    private void validarTexto(String texto, int minimo, String mensaje) {
        if (texto == null || texto.trim().length() < minimo) throw new IllegalArgumentException(mensaje);
    }

    private String generarCodigoReclamo() {
        return "PENDIENTE-" + java.util.UUID.randomUUID();
    }

    private String generarCodigoOrden() {
        return String.format("OT-%d-%04d", Year.now().getValue(), ordenRepository.count() + 1);
    }
}
