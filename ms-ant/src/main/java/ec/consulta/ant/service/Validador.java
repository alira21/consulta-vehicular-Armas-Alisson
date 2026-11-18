package ec.consulta.ant.service;

import ec.consulta.ant.model.Errores;

public final class Validador {
    private Validador() {}

    public static String cedula(String cedula) {
        if (cedula == null || !cedula.matches("\\d{10}"))
            throw new Errores.DatoInvalido("La cédula debe tener 10 dígitos");
        if (cedula.charAt(2) > '5')
            throw new Errores.DatoInvalido("La cédula no corresponde a una persona natural");
        return cedula;
    }

    /** La placa es opcional: si viene vacía se consulta solo por cédula */
    public static String placaOpcional(String placa) {
        if (placa == null || placa.isBlank()) return "";
        String p = placa.trim().toUpperCase().replace("-", "");
        if (!p.matches("^[A-Z]{3}\\d{3,4}$|^[A-Z]{2}\\d{3}[A-Z]$"))
            throw new Errores.DatoInvalido("Placa inválida (ej. PBX1234)");
        return p;
    }
}