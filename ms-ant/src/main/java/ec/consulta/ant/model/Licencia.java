package ec.consulta.ant.model;

import java.time.Instant;

public record Licencia(
        String cedula,
        String placa,
        Integer puntos,
        int totalCitaciones,
        double valorPendiente,
        String fuente,
        Instant obtenidoEn) {

    public Licencia conFuente(String f) {
        return new Licencia(cedula, placa, puntos, totalCitaciones, valorPendiente, f, obtenidoEn);
    }
}