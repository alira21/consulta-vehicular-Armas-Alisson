package ec.consulta.ant.client;

import ec.consulta.ant.model.Licencia;

/** Conexión con la ANT. Si no obtiene una respuesta adecuada, lanza AntNoDisponible. */
public interface AntGateway {
    Licencia consultar(String cedula, String placa);
}