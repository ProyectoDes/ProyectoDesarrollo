package mx.bambuybrillo.negocio;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidarSesionTest {

    private static final Instant AHORA = IniciarSesionTest.AHORA;

    private IniciarSesionTest.SesionesFalsas sesiones;
    private String token;

    @BeforeEach
    void preparar() {
        sesiones = new IniciarSesionTest.SesionesFalsas();
        token = Contrasenas.nuevoToken();
        sesiones.guardar(Contrasenas.sha256(token), 1, AHORA.plus(IniciarSesion.DURACION));
    }

    private ValidarSesion validarEn(Instant momento) {
        return new ValidarSesion(sesiones, Clock.fixed(momento, ZoneOffset.UTC));
    }

    @Test
    void unaSesionVigenteEsValida() {
        assertTrue(validarEn(AHORA.plusSeconds(60)).esValida(token));
    }

    @Test
    void despuesDe30DiasYaNoEsValida() {
        assertFalse(validarEn(AHORA.plus(IniciarSesion.DURACION).plusSeconds(1)).esValida(token));
    }

    @Test
    void sinCookieOConTokenInventadoNoEsValida() {
        ValidarSesion validar = validarEn(AHORA);
        assertFalse(validar.esValida(null));
        assertFalse(validar.esValida(""));
        assertFalse(validar.esValida("token-inventado"));
    }

    @Test
    void alCerrarSesionDejaDeSerValida() {
        ValidarSesion validar = validarEn(AHORA);
        validar.cerrar(token);
        assertFalse(validar.esValida(token));
    }
}
