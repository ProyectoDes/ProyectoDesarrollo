package mx.bambuybrillo.negocio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContrasenasTest {

    @Test
    void laContrasenaCorrectaCoincide() {
        String hash = Contrasenas.cifrar("Bambu2026!");
        assertTrue(Contrasenas.coincide("Bambu2026!", hash));
    }

    @Test
    void unaContrasenaDistintaNoCoincide() {
        String hash = Contrasenas.cifrar("Bambu2026!");
        assertFalse(Contrasenas.coincide("bambu2026!", hash));
        assertFalse(Contrasenas.coincide("", hash));
    }

    @Test
    void noSeGuardaLaContrasenaTalCual() {
        String hash = Contrasenas.cifrar("Bambu2026!");
        assertFalse(hash.contains("Bambu2026!"));
        assertTrue(hash.startsWith("pbkdf2$"));
    }

    @Test
    void laMismaContrasenaDaHashesDistintosPorLaSal() {
        assertNotEquals(Contrasenas.cifrar("Bambu2026!"), Contrasenas.cifrar("Bambu2026!"));
    }

    @Test
    void unHashMalFormadoNoCoincide() {
        assertFalse(Contrasenas.coincide("Bambu2026!", "texto-cualquiera"));
        assertFalse(Contrasenas.coincide("Bambu2026!", null));
    }

    @Test
    void losTokensSonDistintos() {
        assertNotEquals(Contrasenas.nuevoToken(), Contrasenas.nuevoToken());
    }
}
