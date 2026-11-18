package ec.consulta.ant.client;

import ec.consulta.ant.model.Errores;
import ec.consulta.ant.model.Licencia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Consulta el portal real de la ANT y extrae los puntos del HTML. */
@Component
@ConditionalOnProperty(name = "app.ant.modo", havingValue = "REAL")
public class AntWebGateway implements AntGateway {

    private static final Pattern PUNTOS = Pattern.compile("(?i)puntos[^0-9]{0,40}(\\d{1,2})");

    private final RestClient rest;
    private final String url;

    public AntWebGateway(RestClient rest, @Value("${app.ant.url}") String url) {
        this.rest = rest;
        this.url = url;
    }

    @Override
    public Licencia consultar(String cedula, String placa) {
        try {
            String html = rest.get()
                    .uri(url + "?ps_tipo_identificacion=CED&ps_identificacion={c}&ps_placa={p}", cedula, placa)
                    .retrieve().body(String.class);

            Matcher m = PUNTOS.matcher(html == null ? "" : html);
            if (!m.find()) {
                throw new Errores.AntNoDisponible("La respuesta de la ANT no tiene el formato esperado");
            }
            int puntos = Integer.parseInt(m.group(1));
            return new Licencia(cedula, placa, puntos, 0, 0, "ANT", Instant.now());
        } catch (RestClientException e) {
            throw new Errores.AntNoDisponible("La ANT no respondió: " + e.getMessage());
        }
    }
}