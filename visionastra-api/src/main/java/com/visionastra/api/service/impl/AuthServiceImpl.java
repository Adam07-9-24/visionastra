package com.visionastra.api.service.impl;

import com.visionastra.api.dto.LoginRequest;
import com.visionastra.api.dto.LoginResponse;
import com.visionastra.api.dto.RegisterRequest;
import com.visionastra.api.dto.RegisterResponse;
import com.visionastra.api.model.RefreshToken;
import com.visionastra.api.model.Rol;
import com.visionastra.api.model.Sesion;
import com.visionastra.api.model.Usuario;
import com.visionastra.api.repository.RefreshTokenRepository;
import com.visionastra.api.repository.RolRepository;
import com.visionastra.api.repository.SesionRepository;
import com.visionastra.api.repository.UsuarioRepository;
import com.visionastra.api.service.AuditoriaLoginService;
import com.visionastra.api.service.AuthService;
import com.visionastra.api.service.JwtService;
import com.visionastra.api.service.RefreshTokenService;
import com.visionastra.api.service.SesionService;
import com.visionastra.api.util.DeviceDetector;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuditoriaLoginService auditoriaLoginService;
    private final SesionService sesionService;
    private final SesionRepository sesionRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final int MAX_SESIONES = 3;
    private static final int SESSION_TIMEOUT_MINUTES = 25;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            AuditoriaLoginService auditoriaLoginService,
            SesionService sesionService,
            SesionRepository sesionRepository,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.auditoriaLoginService = auditoriaLoginService;
        this.sesionService = sesionService;
        this.sesionRepository = sesionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (request == null) {
            throw new RuntimeException("La solicitud de registro es obligatoria.");
        }

        if (request.getNombres() == null || request.getNombres().trim().isEmpty()) {
            throw new RuntimeException("Los nombres son obligatorios.");
        }

        if (request.getApellidos() == null || request.getApellidos().trim().isEmpty()) {
            throw new RuntimeException("Los apellidos son obligatorios.");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("El correo electrónico es obligatorio.");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("La contraseña es obligatoria.");
        }

        String nombresLimpios = request.getNombres().trim();
        String apellidosLimpios = request.getApellidos().trim();
        String emailNormalizado = request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new RuntimeException("El rol CLIENTE no está configurado."));

        if (!"activo".equalsIgnoreCase(rolCliente.getEstado())) {
            throw new RuntimeException("El rol CLIENTE no está disponible.");
        }

        LocalDateTime ahora = LocalDateTime.now();

        Usuario usuario = new Usuario();
        usuario.setRol(rolCliente);
        usuario.setNombres(nombresLimpios);
        usuario.setApellidos(apellidosLimpios);
        usuario.setEmail(emailNormalizado);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setEstado("activo");
        usuario.setTelefono(null);
        usuario.setFotoPerfil(null);
        usuario.setUltimoLogin(null);
        usuario.setFechaCreacion(ahora);
        usuario.setFechaActualizacion(ahora);

        usuarioRepository.save(usuario);

        return new RegisterResponse("Usuario registrado correctamente");
    }

    @Override
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {

        Optional<Usuario> usuarioOptional =
                usuarioRepository.findByEmail(request.getEmail());

        if (usuarioOptional.isEmpty()) {
            auditoriaLoginService.registrarLoginFallido(
                    request.getEmail(),
                    "El usuario no existe",
                    ipAddress,
                    userAgent
            );

            throw new RuntimeException("El usuario no existe");
        }

        Usuario usuario = usuarioOptional.get();

        if (!"activo".equalsIgnoreCase(usuario.getEstado())) {
            throw new RuntimeException("El usuario no está activo");
        }

        boolean passwordValida = passwordEncoder.matches(
                request.getPassword(),
                usuario.getPasswordHash()
        );

        if (!passwordValida) {
            auditoriaLoginService.registrarLoginFallido(
                    request.getEmail(),
                    "Contraseña incorrecta",
                    ipAddress,
                    userAgent
            );

            throw new RuntimeException("Contraseña incorrecta");
        }

        String dispositivo = DeviceDetector.detectarDispositivo(userAgent);
        LocalDateTime fechaExpiracionSesion =
                LocalDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES);

        // 1. Limpiar sesiones vencidas antes de crear una nueva
        sesionService.expirarSesionesVencidas();

        // 2. Evitar duplicados del mismo navegador/dispositivo
        // Si el mismo usuario vuelve a logearse desde el mismo Chrome/Postman,
        // cerramos la sesión anterior de ese mismo cliente.
        sesionService.cerrarSesionesActivasDelMismoDispositivo(
                usuario,
                ipAddress,
                userAgent
        );

        // 3. Control de límite de sesiones después de limpiar duplicados
        int sesionesActivas = sesionRepository
                .countByUsuarioAndEstado(usuario, "activa");

        if (sesionesActivas >= MAX_SESIONES) {
            Optional<Sesion> sesionAntiguaOpt = sesionRepository
                    .findFirstByUsuarioAndEstadoOrderByFechaInicioAsc(usuario, "activa");

            if (sesionAntiguaOpt.isPresent()) {
                Sesion sesionAntigua = sesionAntiguaOpt.get();

                sesionAntigua.setEstado("cerrada");
                sesionRepository.save(sesionAntigua);

                revocarRefreshTokensPorSesion(sesionAntigua.getIdSesion());
            }
        }

        // 4. Crear nueva sesión limpia
        Sesion sesion = sesionService.crearSesion(
                usuario,
                dispositivo,
                ipAddress,
                userAgent,
                fechaExpiracionSesion
        );

        // 5. Crear JWT con idSesion
        String token = jwtService.generateToken(usuario, sesion.getIdSesion());

        // 6. Crear refresh token asociado a la misma sesión
        String refreshToken = refreshTokenService.createRefreshToken(
                usuario,
                sesion.getIdSesion()
        );

        auditoriaLoginService.registrarLoginExitoso(
                usuario,
                ipAddress,
                userAgent
        );

        return new LoginResponse(
                usuario.getIdUsuario(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getRol().getNombre(),
                usuario.getEstado(),
                "Login exitoso",
                token,
                refreshToken
        );
    }

    @Override
    public void logout(String token, String ipAddress, String userAgent) {

        Integer idSesion = jwtService.extractIdSesion(token);

        Sesion sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        sesion.setEstado("cerrada");
        sesionRepository.save(sesion);

        revocarRefreshTokensPorSesion(idSesion);

        auditoriaLoginService.registrarLogoutExitoso(
                sesion.getUsuario(),
                ipAddress,
                userAgent
        );
    }

    private void revocarRefreshTokensPorSesion(Integer idSesion) {
        List<RefreshToken> tokens = refreshTokenRepository.findByIdSesion(idSesion);

        for (RefreshToken token : tokens) {
            token.setRevocado(true);
            refreshTokenRepository.save(token);
        }
    }
}
