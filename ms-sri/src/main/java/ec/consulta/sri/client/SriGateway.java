package ec.consulta.sri.client;

import tools.jackson.databind.JsonNode;

public interface SriGateway {
    boolean existePorNumeroRuc(String ruc);
    JsonNode obtenerPorNumeroRuc(String ruc);
    JsonNode obtenerVehiculoPorPlaca(String placa);
}