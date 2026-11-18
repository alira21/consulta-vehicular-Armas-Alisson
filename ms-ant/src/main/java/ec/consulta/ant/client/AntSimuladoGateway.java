package ec.consulta.ant.client;

import ec.consulta.ant.model.Errores;
import ec.consulta.ant.model.Licencia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Random;

/**
 * Simula el portal de la ANT. Falla a propósito según "tasa-fallo"
 * para reproducir su baja disponibilidad.
 */
@Component
@ConditionalOnProperty(name = "app.ant.modo", havingValue = "SIMULADO", matchIfMissing = true)
public class AntSimuladoGateway implements AntGateway {

    private final double tasaFallo;
    private final Random random = new Random();

    public AntSimuladoGateway(@Value("${app.ant.tasa-fallo}") double tasaFallo) {
        this.tasaFallo = tasaFallo;
    }

    @Override
    public Licencia consultar(String cedula, String placa) {
        esperar(300); // simula la demora de la red

        if (random.nextDouble() < tasaFallo) {
            System.out.println(">>> ANT SIMULADA: FALLO (no respondio)");
            throw new Errores.AntNoDisponible("La ANT no respondió (simulado)");
        }

        // Datos fijos por cédula: la misma cédula siempre da los mismos puntos
        int semilla = Math.abs(cedula.hashCode());
        int puntos = 30 - (semilla % 11);      // entre 20 y 30 puntos
        int citaciones = semilla % 4;          // entre 0 y 3 citaciones
        double valor = citaciones * 46.00;

        System.out.println(">>> ANT SIMULADA: OK");
        return new Licencia(cedula, placa, puntos, citaciones, valor, "ANT_SIMULADO", Instant.now());
    }

    private void esperar(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}