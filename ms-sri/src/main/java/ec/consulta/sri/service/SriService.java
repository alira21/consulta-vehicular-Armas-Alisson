package ec.consulta.sri.service;

import tools.jackson.databind.JsonNode;
import ec.consulta.sri.client.SriGateway;
import ec.consulta.sri.model.Contribuyente;
import ec.consulta.sri.model.Errores;
import ec.consulta.sri.model.Vehiculo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class SriService {

    private final SriGateway sri;
    private final CacheService cache;
    private final Duration ttl;

    public SriService(SriGateway sri, CacheService cache,
                      @Value("${app.sri.cache-ttl-horas}") int horas) {
        this.sri = sri;
        this.cache = cache;
        this.ttl = Duration.ofHours(horas);
    }

    public Contribuyente consultarContribuyente(String rucEntrada) {
        String ruc = Validador.rucPersonaNatural(rucEntrada);
        String key = "sri:contribuyente:" + ruc;

        // 1. Busca primero en caché
        Optional<Contribuyente> enCache = cache.get(key, Contribuyente.class);
        if (enCache.isPresent()) return enCache.get().conFuente("CACHE");

        // 2. Si no hay dato, consulta al SRI
        try {
            if (!sri.existePorNumeroRuc(ruc))
                throw new Errores.NoEncontrado("El RUC no está registrado en el SRI");

            JsonNode n = sri.obtenerPorNumeroRuc(ruc);
            if (n == null) throw new Errores.NoEncontrado("El SRI no devolvió datos");

            String tipo = txt(n, "tipoContribuyente");
            if (tipo == null || !tipo.toUpperCase().contains("NATURAL"))
                throw new Errores.ReglaNegocio("No es persona natural (tipo: " + tipo + ")");

            Contribuyente c = new Contribuyente(ruc, ruc.substring(0, 10),
                    txt(n, "razonSocial"), tipo, txt(n, "estadoContribuyenteRuc"),
                    txt(n, "actividadEconomicaPrincipal"), txt(n, "regimen"),
                    "SRI", Instant.now());
            cache.put(key, c, ttl);   // 3. Guarda en caché
            return c;
        } catch (RestClientException e) {
            throw new Errores.ServicioNoDisponible("El SRI no responde, intente más tarde");
        }
    }

    public Vehiculo consultarVehiculo(String placaEntrada) {
        String placa = Validador.placa(placaEntrada);
        String key = "sri:vehiculo:" + placa;

        Optional<Vehiculo> enCache = cache.get(key, Vehiculo.class);
        if (enCache.isPresent()) return enCache.get().conFuente("CACHE");

        try {
            JsonNode n = sri.obtenerVehiculoPorPlaca(placa);
            if (n == null || txt(n, "descripcionMarca") == null)
                throw new Errores.NoEncontrado("No existe un vehículo con placa " + placa);

            Vehiculo v = new Vehiculo(placa, txt(n, "descripcionMarca"),
                    txt(n, "descripcionModelo"),
                    n.hasNonNull("anioAuto") ? n.get("anioAuto").asInt() : null,
                    txt(n, "nombreClase"), txt(n, "colorVehiculo1"),
                    txt(n, "nombreCantonMatricula"), "SRI", Instant.now());
            cache.put(key, v, ttl);
            return v;
        } catch (RestClientException e) {
            throw new Errores.ServicioNoDisponible("El servicio de matriculación no responde");
        }
    }

    private static String txt(JsonNode n, String campo) {
        JsonNode v = n.get(campo);
        return (v == null || v.isNull()) ? null : v.asString();
    }
}