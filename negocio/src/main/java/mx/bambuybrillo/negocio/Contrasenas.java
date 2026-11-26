package mx.bambuybrillo.negocio;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Cifra y revisa contraseñas con PBKDF2 (viene incluido en Java).
// Formato guardado: pbkdf2$<iteraciones>$<sal en Base64>$<hash en Base64>
public final class Contrasenas {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 210_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Contrasenas() {
    }

    public static String cifrar(String contrasena) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(contrasena, sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIJO + "$" + ITERACIONES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    public static boolean coincide(String contrasena, String guardado) {
        if (contrasena == null || guardado == null) {
            return false;
        }
        String[] partes = guardado.split("\\$");
        if (partes.length != 4 || !PREFIJO.equals(partes[0])) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(contrasena, sal, iteraciones);
            // Comparación en tiempo constante
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // SHA-256 en hexadecimal; se usa para guardar el token de sesión sin guardarlo tal cual
    public static String sha256(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 no está disponible.", e);
        }
    }

    // Token aleatorio para la cookie de sesión
    public static String nuevoToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] derivar(String contrasena, byte[] sal, int iteraciones) {
        try {
            KeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, BITS_HASH);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo cifrar la contraseña.", e);
        }
    }
}
