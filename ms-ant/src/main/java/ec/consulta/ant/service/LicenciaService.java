package ec.consulta.ant.service;

import ec.consulta.ant.client.AntGateway;
import ec.consulta.ant.model.Errores;
import ec.consulta.ant.model.Licencia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Patrón Circuit Breaker + Cache:
 * - Cada vez que la ANT responde bien, se guarda en caché y se devuelve desde allí.
 * - Si la ANT falla (o el circuito está abierto), se devuelve la última copia de la caché.
 */
@Service
public class LicenciaService {

    private final AntGateway ant;
    private final CacheService cache;
    private final AntCircuitBreaker circuitBreaker;
    private final Duration ttl;

    public LicenciaService(AntGateway ant, CacheService cache, AntCircuitBreaker circuitBreaker,
                           @Value("${app.ant.cache-ttl-dias}") int dias) {
        this.ant = ant;
        this.cache = cache;
        this.circuitBreaker = circuitBreaker;
        this.ttl = Duration.ofDays(dias);
    }

    public Licencia consultar(String cedulaEntrada, String placaEntrada) {
        String cedula = Validador.cedula(cedulaEntrada);
        String placa = Validador.placaOpcional(placaEntrada);
        String key = "ant:licencia:" + cedula;

        return circuitBreaker.ejecutar(
                // Llamada normal: consulta la ANT, guarda en caché y devuelve desde la caché
                () -> {
                    Licencia respuesta = ant.consultar(cedula, placa);
                    cache.put(key, respuesta, ttl);
                    return cache.get(key, Licencia.class).orElse(respuesta);
                },
                // Fallback: la ANT falló -> se devuelve lo último guardado
                error -> cache.get(key, Licencia.class)
                        .map(l -> l.conFuente("CACHE"))
                        .orElseThrow(() -> new Errores.SinDatos(
                                "La ANT no está disponible y aún no hay datos guardados para esta cédula. Intente de nuevo."))
        );
    }

    public AntCircuitBreaker.Estado estadoCircuito() {
        return circuitBreaker.getEstado();
    }
}