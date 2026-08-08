package com.utng.AuthModule.services;

import com.utng.AuthModule.model.Usuario.Usuario;
import com.utng.AuthModule.repository.AuthRepository;
import com.utng.UserModule.UsuarioRepository;
import com.utng.util.AppException;
import com.utng.util.EmailService;
import com.utng.util.PasswordUtil;

import java.security.SecureRandom;
import java.sql.Timestamp;

public class AuthService {
    private final AuthRepository authRepository = new AuthRepository();
    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private static final SecureRandom random = new SecureRandom();
    private final EmailService emailService = new EmailService();
    private final PasswordUtil passwordUtil = new PasswordUtil();

    public boolean recuperacionDeCredenciales(String correo) {
        try {
            Usuario usuario = authRepository.obtenerPorCorreo(correo);
            if (usuario != null) {
                System.out.print("Ususario Encontrado con el correo: " + correo);
            } else {
                System.out.print("usuario No encontrado");
                return false;
            }

            String codioRecuperacion = String.valueOf(random.nextInt(900000) + 100000);
            usuario.setRecuperacionActiva(true);
            usuario.setCodigoRecuperacion(codioRecuperacion);
            int intentos = usuario.getIntentosRecuperacion();
            usuario.setIntentosRecuperacion(intentos + 1);
            usuario.setFechaCodigo(new Timestamp(System.currentTimeMillis()));
            usuarioRepository.actualizar(usuario);
            emailService.enviarCorreo(correo, "Recuperacion de Credenciales",
            emailService.codigoRecuperacion(codioRecuperacion));
            System.out.print("Usuario en recuperacion");
            return true;

        } catch (AppException e) {
            throw new AppException("Error en la recuperacion de Credenciales", e);
        }

    }

    public boolean confirmarRecuperacion(String correo, String codigo) {
        Timestamp ahora = Timestamp.from(java.time.Instant.now());

        try {
            Usuario usuario = usuarioRepository.buscarPorCorreo(correo);
            if (usuario == null){
                System.out.print("Ususario nulo ASSASASASA");
            }
            if (usuario != null && usuario.getRecuperacionActiva() == true) {
                System.out.print("Ususario encontrado y en recuperacion");
            }

            if (!usuario.getCodigoRecuperacion().equals(codigo)
                    || ahora.after(
                            new Timestamp(
                                    usuario.getFechaCodigo().getTime() + (5 * 60 * 1000)))) {
                System.out.print("El codigo es incorrecto o expirado");
                return false;
            } else {
                System.out.print("El codigo es correcto ");
                usuario.setCodigoRecuperacion(null);
                usuario.setFechaCodigo(null);
                usuario.setIntentosRecuperacion(0);

                usuarioRepository.actualizar(usuario);
                
                return true;

            }

        } catch (AppException e) {
            throw new AppException("Error al confirmar la recuperacion ", e);
        }
        
    }

    public boolean cambiaPasword(String correo, String password){
        try {
            Usuario usuario = usuarioRepository.buscarPorCorreo(correo);

            if(usuario != null && usuario.getRecuperacionActiva() == true ){
            String hash = passwordUtil.hashPassword(password);
            usuario.setPassword(hash);
            usuario.setRecuperacionActiva(false);
            usuarioRepository.actualizar(usuario);
            return true;
            }
            else{
                System.out.print("No  se encontro el User o no esta en recuperacion ");
                return false;
            }
            

        } catch (AppException e) {
            throw new AppException("Error al restablecer la password", e);
        }
        
    }

    public boolean login (String correo, String password ){
        try {
            Usuario usuario = usuarioRepository.buscarPorCorreo(correo);
             boolean res = PasswordUtil.verificarPassword(password, usuario.getPassword());
             if (res){
             System.out.print("Login exitoso");
            } else {
                System.out.print("Credenciales incorrectas");
            }
             return res;
             


        } catch (AppException e) {
            throw new AppException("Error en login",e);
        }

    }

    public boolean registro(Usuario usuario){
        try {
            usuarioRepository.guardar(usuario);
            return true;

        } catch (AppException e) {
            throw new AppException("Error al registrar el nuevo usuario", e);
        }

    }

}
