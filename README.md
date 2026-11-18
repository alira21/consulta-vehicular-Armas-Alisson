# Consulta Vehicular – SRI y ANT

Sistema web que verifica un contribuyente (persona natural) en el SRI, consulta su vehículo y sus puntos de licencia en la ANT.

## Arquitectura (C4)
- Diagramas en IcePanel: (https://s.icepanel.io/Os48xhYAeFsgfA/rEFX)
- Nivel 4 (código): [docs/c4-nivel4-codigo.md](docs/c4-nivel4-codigo.md)

| Contenedor | Tecnología | Puerto |
|---|---|---|
| frontend | React + Vite | 5173 |
| ms-sri | Java 21, Spring Boot 4 | 8081 |
| ms-ant | Java 21, Spring Boot 4 | 8082 |
| Caché | Redis Cloud | – |

## Patrones
- **ms-sri – Cache-Aside:** primero busca en Redis; si no está, consulta al SRI y guarda el resultado.
- **ms-ant – Circuit Breaker + Cache:** cada respuesta correcta de la ANT se guarda en Redis y se devuelve desde allí. Si la ANT falla, se devuelve la última copia. Tras 3 fallos seguidos el circuito se abre por 20 segundos y no se llama a la ANT.
- La ANT se **simula** (modo `SIMULADO`, 50% de fallos) por su baja disponibilidad. Se puede cambiar a `REAL` en `ms-ant/src/main/resources/application.yml`.

## Cómo ejecutar
1. Variable de entorno con la URL de Redis Cloud:
   `REDIS_URL=redis://default:PASSWORD@HOST:PUERTO`
2. ms-sri: `cd ms-sri` → `./mvnw spring-boot:run`
3. ms-ant: `cd ms-ant` → `./mvnw spring-boot:run`
4. frontend: `cd frontend` → `npm install` → `npm run dev`
5. Abrir http://localhost:5173
