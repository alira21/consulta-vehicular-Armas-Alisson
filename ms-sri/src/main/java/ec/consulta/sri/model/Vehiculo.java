package ec.consulta.sri.model;

import java.time.Instant;

public record Vehiculo(
        String placa, String marca, String modelo, Integer anio, String clase,
        String color, String cantonMatricula, String fuente, Instant consultadoEn) {

    public Vehiculo conFuente(String f) {
        return new Vehiculo(placa, marca, modelo, anio, clase, color,
                cantonMatricula, f, consultadoEn);
    }
}