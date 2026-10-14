package com.utp.sedapal.services;

import com.utp.sedapal.models.EstadoReclamo;
import com.utp.sedapal.models.OrdenTrabajo;
import com.utp.sedapal.models.Reclamo;
import com.utp.sedapal.models.RolUsuario;
import com.utp.sedapal.models.Suministro;
import com.utp.sedapal.models.Tecnico;
import com.utp.sedapal.models.UsuarioServicio;
import com.utp.sedapal.models.Zona;
import com.utp.sedapal.repository.EstadoReclamoRepository;
import com.utp.sedapal.repository.SuministroRepository;
import com.utp.sedapal.repository.TecnicoRepository;
import com.utp.sedapal.repository.UsuarioServicioRepository;
import com.utp.sedapal.repository.ZonaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DatosInicialesService implements ApplicationRunner {
    private final UsuarioServicioRepository usuarioRepository;
    private final SuministroRepository suministroRepository;
    private final EstadoReclamoRepository estadoRepository;
    private final TecnicoRepository tecnicoRepository;
    private final ZonaRepository zonaRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReclamoService reclamoService;
    private final boolean demoData;

    public DatosInicialesService(UsuarioServicioRepository usuarioRepository,
                                 SuministroRepository suministroRepository,
                                 EstadoReclamoRepository estadoRepository,
                                 TecnicoRepository tecnicoRepository,
                                 ZonaRepository zonaRepository,
                                 PasswordEncoder passwordEncoder,
                                 ReclamoService reclamoService,
                                 @Value("${app.demo-data:true}") boolean demoData) {
        this.usuarioRepository = usuarioRepository;
        this.suministroRepository = suministroRepository;
        this.estadoRepository = estadoRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.zonaRepository = zonaRepository;
        this.passwordEncoder = passwordEncoder;
        this.reclamoService = reclamoService;
        this.demoData = demoData;
    }

    @Override
    public void run(ApplicationArguments args) {
        configurarEstados();
        if (!demoData || usuarioRepository.count() > 0) return;

        Zona limaEste = crearZona("Lima Este");
        Zona limaCentro = crearZona("Lima Centro");
        Zona callao = crearZona("Callao");

        UsuarioServicio usuario1 = crearUsuario("Jhans", "Alvarado Garayar", "12345678", RolUsuario.USUARIO);
        UsuarioServicio usuario2 = crearUsuario("Marko", "Encalada Lazo", "87654321", RolUsuario.USUARIO);
        crearUsuario("Carmen", "Rojas", "22222222", RolUsuario.OPERADOR);
        crearUsuario("Elena", "Campos", "44444444", RolUsuario.EVALUADOR);
        crearUsuario("Pedro", "Salazar", "55555555", RolUsuario.SUPERVISOR);
        UsuarioServicio tecnicoUser = crearUsuario("Luis", "Quispe", "33333333", RolUsuario.TECNICO);
        crearUsuario("Rosa", "Mendoza", "66666666", RolUsuario.CALIDAD);
        crearUsuario("Administrador", "SEDAPAL", "99999999", RolUsuario.ADMINISTRADOR);

        crearSuministro("SUM-1001", "Av. Próceres 540", "San Juan de Lurigancho", limaEste, usuario1);
        crearSuministro("SUM-1002", "Jr. Los Jardines 180", "Callao", callao, usuario1);
        crearSuministro("SUM-2001", "Av. Arequipa 1200", "Lince", limaCentro, usuario2);

        Tecnico tecnico = crearTecnico("Luis Quispe Ramos", "Redes de agua potable", true, tecnicoUser);
        crearTecnico("Ana Torres Vidal", "Fugas y presión", true, null);
        crearTecnico("Miguel Paredes Soto", "Medidores y conexiones", true, null);

        reclamoService.registrarReclamo(usuario1.getId(), "SUM-1001", "Fuga de agua", "Alta",
                "Fuga visible en la vereda desde la madrugada.", "SISTEMA");
        Reclamo asignado = reclamoService.registrarReclamo(usuario1.getId(), "SUM-1002", "Baja presión", "Media",
                "La presión del agua disminuyó de manera constante durante el día.", "SISTEMA");
        reclamoService.clasificar(asignado.getId(), "Baja presión", "Media", true,
                "Se requiere inspección de la red secundaria.", "44444444");
        OrdenTrabajo orden = reclamoService.generarOrden(asignado.getId(), "55555555");
        reclamoService.asignarTecnico(orden.getId(), tecnico.getId(), "55555555");

        Reclamo orientado = reclamoService.registrarReclamo(usuario2.getId(), "SUM-2001", "Corte de servicio", "Alta",
                "Corte temporal comunicado en el sector desde hace varias horas.", "SISTEMA");
        reclamoService.clasificar(orientado.getId(), "Corte programado", "Media", false,
                "Se informó al usuario el horario de reposición publicado para la zona.", "44444444");
    }

    private void configurarEstados() {
        String[] nombres = {ReclamoService.REGISTRADO, ReclamoService.CLASIFICADO, ReclamoService.ASIGNADO,
                ReclamoService.EN_ATENCION, ReclamoService.ATENDIDO, ReclamoService.CERRADO};
        for (int i = 0; i < nombres.length; i++) {
            int orden = i;
            EstadoReclamo estado = estadoRepository.findByNombre(nombres[i]).orElseGet(EstadoReclamo::new);
            estado.setNombre(nombres[i]);
            estado.setOrden(orden);
            estadoRepository.save(estado);
        }
    }

    private UsuarioServicio crearUsuario(String nombre, String apellido, String documento, RolUsuario rol) {
        UsuarioServicio usuario = new UsuarioServicio();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setDocumento(documento);
        usuario.setPassword(passwordEncoder.encode("1234"));
        usuario.setRol(rol);
        usuario.setCorreo(documento + "@demo.sedapal.pe");
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private Zona crearZona(String nombre) {
        Zona zona = new Zona(); zona.setNombre(nombre); return zonaRepository.save(zona);
    }

    private Suministro crearSuministro(String codigo, String direccion, String distrito, Zona zona, UsuarioServicio usuario) {
        Suministro suministro = new Suministro();
        suministro.setCodigoSuministro(codigo); suministro.setDireccion(direccion);
        suministro.setDistrito(distrito); suministro.setZona(zona); suministro.setUsuario(usuario);
        return suministroRepository.save(suministro);
    }

    private Tecnico crearTecnico(String nombres, String especialidad, boolean disponible, UsuarioServicio usuario) {
        Tecnico tecnico = new Tecnico(); tecnico.setNombres(nombres); tecnico.setEspecialidad(especialidad);
        tecnico.setDisponible(disponible); tecnico.setUsuario(usuario); return tecnicoRepository.save(tecnico);
    }
}
