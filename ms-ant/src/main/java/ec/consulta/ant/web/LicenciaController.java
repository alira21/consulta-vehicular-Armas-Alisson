package ec.consulta.ant.web;

import ec.consulta.ant.model.Licencia;
import ec.consulta.ant.service.LicenciaService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ant")
public class LicenciaController {

    private final LicenciaService service;

    public LicenciaController(LicenciaService service) {
        this.service = service;
    }

    /** Ej: /api/ant/licencias/1712345678?placa=PBX1234 */
    @GetMapping("/licencias/{cedula}")
    public Licencia licencia(@PathVariable String cedula,
                             @RequestParam(required = false) String placa) {
        return service.consultar(cedula, placa);
    }

    /** Para ver en qué estado está el Circuit Breaker */
    @GetMapping("/estado-circuito")
    public Map<String, Object> estado() {
        return Map.of("estado", service.estadoCircuito());
    }
}