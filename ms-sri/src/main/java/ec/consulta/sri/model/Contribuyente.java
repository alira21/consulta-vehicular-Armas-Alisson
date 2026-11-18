package ec.consulta.sri.model;

import java.time.Instant;

public record Contribuyente(
        String ruc, String cedula, String razonSocial, String tipoContribuyente,
        String estado, String actividadEconomica, String regimen,
        String fuente, Instant consultadoEn) {

    public Contribuyente conFuente(String f) {
        return new Contribuyente(ruc, cedula, razonSocial, tipoContribuyente,
                estado, actividadEconomica, regimen, f, consultadoEn);
    }
}