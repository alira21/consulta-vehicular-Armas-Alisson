package ec.consulta.ant.model;

public final class Errores {
    private Errores() {}

    /** Cédula o placa mal escritas (HTTP 400) */
    public static class DatoInvalido extends RuntimeException {
        public DatoInvalido(String m) { super(m); }
    }

    /** La ANT no respondió o respondió algo inválido */
    public static class AntNoDisponible extends RuntimeException {
        public AntNoDisponible(String m) { super(m); }
    }

    /** La ANT falló y tampoco hay nada guardado en caché (HTTP 503) */
    public static class SinDatos extends RuntimeException {
        public SinDatos(String m) { super(m); }
    }
}