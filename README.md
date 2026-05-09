# Payment Reconciliation API

API REST para consultar la conciliación de pagos entre la visión interna de una fintech y la visión informada por un procesador externo.

## Contexto

Una fintech necesita que sus equipos internos puedan consultar si los pagos registrados internamente coinciden con los pagos reportados por un procesador externo.

El foco de esta API es **payment reconciliation** a nivel de pago individual. No implementa procesamiento de pagos, autorización en tiempo real, clearing detallado, settlement detallado ni payout reconciliation.

## Alcance

### Implementado

- Consultar conciliación por `paymentId`.
- Listar conciliaciones.
- Filtrar conciliaciones por `merchantId`, `reconciliationStatus` o `reconciled`.
- Detectar pagos existentes solo internamente.
- Detectar pagos existentes solo en los registros reportados por el procesador.
- Detectar diferencias de monto, moneda, estado, comercio y referencia externa.
- Separar el estado final de conciliación (`ReconciliationStatus`) del detalle de la diferencia (`ReconciliationDifferenceType`).
- Adaptadores in-memory para simular datos internos y datos reportados por el procesador.
- Pruebas unitarias y pruebas del controller.
- Swagger/OpenAPI.
- Ejecución local con Maven, JAR y Docker.

### Fuera de alcance

- Autorización de pagos en tiempo real.
- Integración real con el procesador externo.
- Flujo detallado de clearing.
- Flujo detallado de settlement.
- Payout reconciliation.
- Fees, net amount, payoutId y validación de transferencia bancaria.
- Ingesta real mediante Kafka/RabbitMQ.
- Persistencia real en PostgreSQL.
- Autenticación/autorización.
- Despliegue en Kubernetes o cloud.

Para más detalle sobre decisiones de alcance y diseño, revisar [`docs/design-decisions.md`](docs/design-decisions.md).

## Arquitectura

El proyecto usa una versión simplificada de Hexagonal Architecture:

```text
Controller
  -> UseCase
      -> Ports
          -> In-memory adapters
      -> PaymentReconciliationService
          -> ReconciliationRule implementations
```

La separación principal es:

- `Controller`: recibe la solicitud HTTP y devuelve DTOs de respuesta.
- `UseCase`: orquesta el caso de uso, obtiene los insumos mediante ports y delega la decisión al dominio.
- `PaymentReconciliationService`: ejecuta reglas de conciliación inyectadas por Spring.
- `ReconciliationRule`: encapsula una validación puntual de conciliación.
- `PaymentReconciliationResult`: consolida el resultado final de dominio.
- `Mapper`: adapta el resultado de dominio al contrato HTTP.

Estructura principal:

```text
src/main/java/com/oraex/reconciliation
 ├── application
 │   └── usecase
 ├── domain
 │   ├── model
 │   ├── port
 │   ├── rule
 │   └── service
 └── infrastructure
     ├── persistence
     └── web
```

## Consumidores de la API

Esta API está diseñada para consumo interno dentro de la fintech:

- Backoffice / operaciones.
- Finanzas / conciliación.
- Soporte interno.
- Sistemas internos.

Estos consumidores necesitan saber si un pago está conciliado y, si no lo está, necesitan ver claramente la razón: en qué fuente falta, qué campo difiere y cuáles son los valores de cada fuente.

## Endpoints

### Consultar conciliación por pago

```http
GET /api/v1/reconciliations/payments/{paymentId}
```

Ejemplo:

```bash
curl http://localhost:8080/api/v1/reconciliations/payments/PAY-1001
```

### Listar conciliaciones

```http
GET /api/v1/reconciliations/payments
```

Filtros opcionales:

```bash
curl "http://localhost:8080/api/v1/reconciliations/payments?reconciled=false"
curl "http://localhost:8080/api/v1/reconciliations/payments?merchantId=MERCHANT-001"
curl "http://localhost:8080/api/v1/reconciliations/payments?reconciliationStatus=RECONCILED_WITH_DIFFERENCES"
```

> Nota: el filtro `reconciliationStatus` usa estados finales de conciliación, no tipos específicos de diferencia. Para conocer el detalle de la diferencia se debe revisar el arreglo `differences` de cada respuesta.

## Reconciliation statuses

`ReconciliationStatus` representa el resultado final de la conciliación.

| Status | Descripción |
|---|---|
| `RECONCILED` | Ambos registros existen y coinciden según las reglas de conciliación. |
| `ONLY_INTERNAL` | El pago existe internamente, pero no fue reportado por el procesador. |
| `ONLY_PROCESSOR` | El pago fue reportado por el procesador, pero no existe internamente. |
| `NOT_FOUND` | El pago no existe en ninguna fuente. |
| `RECONCILED_WITH_DIFFERENCES` | Ambos registros existen, pero se detectó una o más diferencias relevantes. |

## Reconciliation difference types

`ReconciliationDifferenceType` representa el detalle específico detectado por una regla.

| Difference type | Descripción |
|---|---|
| `ONLY_INTERNAL` | Existe registro interno, pero no existe registro del procesador. |
| `ONLY_PROCESSOR` | Existe registro del procesador, pero no existe registro interno. |
| `NOT_FOUND` | No existe registro en ninguna fuente. |
| `AMOUNT_MISMATCH` | Ambos registros existen, pero el monto difiere. |
| `CURRENCY_MISMATCH` | Ambos registros existen, pero la moneda difiere. |
| `STATUS_MISMATCH` | Ambos registros existen, pero el estado del pago difiere. |
| `MERCHANT_MISMATCH` | Ambos registros existen, pero el comercio difiere. |
| `REFERENCE_MISMATCH` | Ambos registros existen, pero la referencia externa difiere. |

## Datos de ejemplo

| paymentId | Escenario | Status final | Difference type |
|---|---|---|---|
| `PAY-1001` | Pago conciliado. | `RECONCILED` | N/A |
| `PAY-1002` | Existe solo internamente. | `ONLY_INTERNAL` | `ONLY_INTERNAL` |
| `PAY-1003` | Existe solo en registros reportados por el procesador. | `ONLY_PROCESSOR` | `ONLY_PROCESSOR` |
| `PAY-1004` | Diferencia de monto. | `RECONCILED_WITH_DIFFERENCES` | `AMOUNT_MISMATCH` |
| `PAY-1005` | Diferencia de moneda. | `RECONCILED_WITH_DIFFERENCES` | `CURRENCY_MISMATCH` |
| `PAY-1006` | Diferencia de estado. | `RECONCILED_WITH_DIFFERENCES` | `STATUS_MISMATCH` |
| `PAY-1007` | Diferencia de comercio. | `RECONCILED_WITH_DIFFERENCES` | `MERCHANT_MISMATCH` |
| `PAY-1008` | Diferencia de referencia externa. | `RECONCILED_WITH_DIFFERENCES` | `REFERENCE_MISMATCH` |
| `PAY-9999` | No existe en ninguna fuente. | `NOT_FOUND` | `NOT_FOUND` |

## Ejemplo de respuesta

```json
{
  "paymentId": "PAY-1004",
  "merchantId": "MERCHANT-001",
  "reconciled": false,
  "reconciliationStatus": "RECONCILED_WITH_DIFFERENCES",
  "internalPayment": {
    "exists": true,
    "paymentId": "PAY-1004",
    "externalReference": "EXT-9004",
    "merchantId": "MERCHANT-001",
    "amount": 10.00,
    "currency": "USD",
    "status": "PAID",
    "processedAt": "2026-05-07T10:20:00",
    "reportedAt": null
  },
  "processorPayment": {
    "exists": true,
    "paymentId": "PAY-1004",
    "externalReference": "EXT-9004",
    "merchantId": "MERCHANT-001",
    "amount": 9.99,
    "currency": "USD",
    "status": "PAID",
    "processedAt": null,
    "reportedAt": "2026-05-08T02:10:00"
  },
  "differences": [
    {
      "field": "amount",
      "internalValue": "10.00",
      "processorValue": "9.99",
      "type": "AMOUNT_MISMATCH"
    }
  ],
  "message": "Payment exists in both sources but contains reconciliation differences"
}
```

## Ejecución local

### Requisitos

- Java 17+
- Maven 3.9+

### Ejecutar pruebas

```bash
mvn clean test
```

### Levantar la aplicación

```bash
mvn spring-boot:run
```

La API estará disponible en:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Ejecución con JAR

```bash
mvn clean package
java -jar target/payment-reconciliation-api-0.0.1-SNAPSHOT.jar
```

## Ejecución con Docker

```bash
docker build -t payment-reconciliation-api .
docker run -p 8080:8080 payment-reconciliation-api
```

## Trade-offs principales

Esta implementación prioriza claridad de dominio, ejecución local y cobertura del flujo principal del challenge.

Por practicidad, se usan adaptadores in-memory y no se implementan componentes productivos como API Gateway, autenticación/autorización, persistencia real, observabilidad avanzada, despliegue en Kubernetes/cloud ni pipeline CI/CD con quality gates.

Estas decisiones permiten entregar una API ejecutable y revisable en poco tiempo, aunque en un entorno productivo la API debería integrarse a un ecosistema más completo de seguridad, despliegue, monitoreo y operación.
