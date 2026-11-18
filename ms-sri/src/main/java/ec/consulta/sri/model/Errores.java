package ec.consulta.sri.model;

public final class Errores {
    private Errores() {}

    public static class DatoInvalido extends RuntimeException {
        public DatoInvalido(String m) { super(m); }
    }
    public static class NoEncontrado extends RuntimeException {
        public NoEncontrado(String m) { super(m); }
    }
    public static class ReglaNegocio extends RuntimeException {
        public ReglaNegocio(String m) { super(m); }
    }
    public static class ServicioNoDisponible extends RuntimeException {
        public ServicioNoDisponible(String m) { super(m); }
    }
}