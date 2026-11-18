package ec.consulta.sri.client;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SriRestGateway implements SriGateway {

    private final RestClient rest;
    @Value("${app.sri.catastro-url}") private String catastroUrl;
    @Value("${app.sri.vehiculo-url}") private String vehiculoUrl;

    public SriRestGateway(RestClient rest) {
        this.rest = rest;
    }

    @Override
    public boolean existePorNumeroRuc(String ruc) {
        String body = rest.get()
                .uri(catastroUrl + "/existePorNumeroRuc?numeroRuc={ruc}", ruc)
                .retrieve().body(String.class);
        return body != null && body.trim().equalsIgnoreCase("true");
    }

    @Override
    public JsonNode obtenerPorNumeroRuc(String ruc) {
        JsonNode body = rest.get()
                .uri(catastroUrl + "/obtenerPorNumerosRuc?&ruc={ruc}", ruc)
                .retrieve().body(JsonNode.class);
        return primero(body);
    }

    @Override
    public JsonNode obtenerVehiculoPorPlaca(String placa) {
        JsonNode body = rest.get()
                .uri(vehiculoUrl + "/obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn?numeroPlacaCampvCpn={p}", placa)
                .retrieve().body(JsonNode.class);
        return primero(body);
    }

    private static JsonNode primero(JsonNode body) {
        if (body == null || body.isNull()) return null;
        if (body.isArray()) return body.isEmpty() ? null : body.get(0);
        return body;
    }
}