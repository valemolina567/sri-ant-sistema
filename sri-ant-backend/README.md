# Sistema de Verificación SRI/ANT
**Arquitectura C4 · React + Java Microservicios + Redis Cache-Aside**

---

## Estructura del proyecto

```
sri-ant-backend/
├── ms-sri/           # Microservicio SRI – contribuyentes y personas naturales
├── ms-vehiculos/     # Microservicio SRI – matrícula vehicular
├── ms-ant/           # Microservicio ANT – puntos de licencia + Redis Cache-Aside
└── docker-compose.yml

sri-ant-frontend/
├── src/
│   ├── api/          # Cliente HTTP → microservicios Java
│   ├── hooks/        # useVerificacion – lógica de flujo
│   ├── components/   # Stepper, Step1Ruc, Step2Vehiculo, Step3Licencia
│   └── App.jsx
├── vite.config.js
└── package.json
```

---

## Flujo completo

```
Usuario → React SPA → API Gateway (8080)
                         ├── /sri/verificar      → MS-SRI (8081)    → SRI REST API
                         ├── /vehiculos/consultar → MS-Vehículos (8082) → SRI REST API
                         └── /ant/puntos         → MS-ANT (8083)
                                                     ├── Redis (Cache HIT) → respuesta
                                                     └── ANT Portal (Cache MISS)
                                                              → guarda en Redis TTL 24h
                                                              → respuesta
```

---

## Patrón Cache-Aside (MS-ANT)

```
consultarPuntos(cedula, placa)
    │
    ▼
CacheService.get("ant:{cedula}:{placa}")
    ├── HIT  ──────────────────────────→ retorna datos (fuenteDatos: "CACHE")
    └── MISS
          │
          ▼
    AntClient.consultarPuntos()  [timeout: 3s, circuit breaker, retry x2]
          ├── OK  → CacheService.set(datos, TTL: 24h)
          │          retorna datos (fuenteDatos: "ANT")
          └── ERR → HTTP 503 (ANT no disponible)
```

---

## Levantar en desarrollo

### Opción 1 – Docker Compose (recomendado)

```bash
cd sri-ant-backend
docker-compose up --build
```

Levanta: Redis en 6379, MS-SRI en 8081, MS-Vehículos en 8082, MS-ANT en 8083.

### Opción 2 – Local con Maven

```bash
# Levantar Redis local
docker run -p 6379:6379 redis:7-alpine

# En terminales separadas:
cd ms-sri      && mvn spring-boot:run
cd ms-vehiculos && mvn spring-boot:run
cd ms-ant      && mvn spring-boot:run
```

### Frontend React

```bash
cd sri-ant-frontend
npm install
npm run dev      # http://localhost:3000
```

---

## Redis Cloud (producción)

Configura las siguientes variables de entorno en el pod del MS-ANT:

```bash
REDIS_HOST=your-endpoint.upstash.io
REDIS_PORT=6380
REDIS_PASSWORD=your-password
REDIS_SSL=true
```

Servicios recomendados: **Upstash** (free tier) o **Redis Enterprise Cloud**.

---

## APIs del SRI utilizadas

| Endpoint | Descripción | Microservicio |
|---|---|---|
| `/ConsolidadoContribuyente/existePorNumeroRuc?numeroRuc=` | Verificar si RUC existe | MS-SRI |
| `/ConsolidadoContribuyente/obtenerPorNumerosRuc?&ruc=` | Obtener datos persona natural | MS-SRI |
| `/BaseVehiculo/obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn?numeroPlacaCampvCpn=` | Consultar vehículo por placa | MS-Vehículos |

## API de la ANT utilizada

| Endpoint | Descripción | Microservicio |
|---|---|---|
| `/PortalWEB/paginas/clientes/clp_grid_citaciones.jsp?ps_tipo_identificacion=CED&ps_identificacion=XXX&ps_placa=YYY` | Puntos de licencia (HTML scraping) | MS-ANT |

---

## Tecnologías

- **Frontend**: React 18 + Vite
- **Backend**: Spring Boot 3.2 + WebFlux (reactivo)
- **Caché**: Redis Cloud (Upstash) con Spring Data Redis
- **Resiliencia**: Resilience4j (Circuit Breaker + Retry)
- **HTML parsing**: Jsoup (portal ANT)
- **Contenedores**: Docker + Docker Compose
