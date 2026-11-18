package ec.consulta.ant.service;

import ec.consulta.ant.model.Errores;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Circuit Breaker:
 *  CERRADO     -> las llamadas pasan normalmente a la ANT.
 *  ABIERTO     -> tras N fallos seguidos, NO se llama a la ANT; se usa el fallback (caché).
 *  SEMIABIERTO -> pasado el tiempo de espera, se deja pasar una llamada de prueba:
 *                 si funciona vuelve a CERRADO, si falla vuelve a ABIERTO.
 */
@Component
public class AntCircuitBreaker {

    public enum Estado { CERRADO, ABIERTO, SEMIABIERTO }

    private final int fallosParaAbrir;
    private final Duration tiempoAbierto;

    private Estado estado = Estado.CERRADO;
    private int fallosSeguidos = 0;
    private Instant abiertoDesde;

    public AntCircuitBreaker(@Value("${app.ant.cb.fallos-para-abrir}") int fallosParaAbrir,
                             @Value("${app.ant.cb.segundos-abierto}") long segundosAbierto) {
        this.fallosParaAbrir = fallosParaAbrir;
        this.tiempoAbierto = Duration.ofSeconds(segundosAbierto);
    }

    public <T> T ejecutar(Supplier<T> llamada, Function<Throwable, T> fallback) {
        if (!permiteLlamada()) {
            System.out.println(">>> CIRCUITO ABIERTO: no se llama a la ANT, se usa la cache");
            return fallback.apply(new Errores.AntNoDisponible("Circuito abierto"));
        }
        try {
            T resultado = llamada.get();
            registrarExito();
            return resultado;
        } catch (RuntimeException e) {
            registrarFallo();
            return fallback.apply(e);
        }
    }

    private synchronized boolean permiteLlamada() {
        if (estado == Estado.ABIERTO) {
            if (Instant.now().isAfter(abiertoDesde.plus(tiempoAbierto))) {
                estado = Estado.SEMIABIERTO;
                System.out.println(">>> CIRCUITO SEMIABIERTO: se prueba una llamada a la ANT");
                return true;
            }
            return false;
        }
        return true;
    }

    private synchronized void registrarExito() {
        if (estado != Estado.CERRADO) System.out.println(">>> CIRCUITO CERRADO: la ANT volvio a responder");
        estado = Estado.CERRADO;
        fallosSeguidos = 0;
    }

    private synchronized void registrarFallo() {
        fallosSeguidos++;
        if (estado == Estado.SEMIABIERTO || fallosSeguidos >= fallosParaAbrir) {
            estado = Estado.ABIERTO;
            abiertoDesde = Instant.now();
            System.out.println(">>> CIRCUITO ABIERTO por " + tiempoAbierto.getSeconds() + " segundos");
        }
    }

    public synchronized Estado getEstado() {
        return estado;
    }

    public synchronized int getFallosSeguidos() {
        return fallosSeguidos;
    }
}