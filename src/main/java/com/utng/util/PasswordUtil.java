package com.utng.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {


    // Hashear contraseña antes de guardarla
    public static String hashPassword(String password) {

        return BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );
    }


    // Comparar contraseña ingresada con hash almacenado
    public static boolean verificarPassword(
            String passwordIngresada,
            String passwordHash) {

        return BCrypt.checkpw(
                passwordIngresada,
                passwordHash
        );
    }

}