package ec.consulta.sri.web;

import ec.consulta.sri.model.Contribuyente;
import ec.consulta.sri.model.Vehiculo;
import ec.consulta.sri.service.SriService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sri")
public class SriController {

    private final SriService service;

    public SriController(SriService service) {
        this.service = service;
    }

    @GetMapping("/contribuyentes/{ruc}")
    public Contribuyente contribuyente(@PathVariable String ruc) {
        return service.consultarContribuyente(ruc);
    }

    @GetMapping("/vehiculos/{placa}")
    public Vehiculo vehiculo(@PathVariable String placa) {
        return service.consultarVehiculo(placa);
    }
}