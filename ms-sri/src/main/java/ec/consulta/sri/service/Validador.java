package ec.consulta.sri.service;

import ec.consulta.sri.model.Errores;

public final class Validador {
    private Validador() {}

    public static String rucPersonaNatural(String ruc) {
        if (ruc == null || !ruc.matches("\\d{13}"))
            throw new Errores.DatoInvalido("El RUC debe tener 13 dígitos");
        if (!ruc.endsWith("001"))
            throw new Errores.DatoInvalido("El RUC de persona natural termina en 001");
        if (ruc.charAt(2) > '5')
            throw new Errores.ReglaNegocio("El RUC no corresponde a una persona natural");
        return ruc;
    }

    public static String placa(String placa) {
        String p = placa == null ? "" : placa.trim().toUpperCase().replace("-", "");
        if (!p.matches("^[A-Z]{3}\\d{3,4}$|^[A-Z]{2}\\d{3}[A-Z]$"))
            throw new Errores.DatoInvalido("Placa inválida (ej. PBX1234)");
        return p;
    }
}