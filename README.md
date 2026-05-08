# Payment Reconciliation API

API REST para consultar la conciliación de pagos entre la visión interna de una fintech y la visión informada por un procesador externo.

## Contexto

Una fintech necesita exponer una API para que sus equipos internos consulten si los pagos registrados internamente coinciden con los pagos informados por el procesador externo.

El foco de esta API es **payment reconciliation**. No implementa procesamiento de pagos, autorización en tiempo real, clearing detallado, settlement detallado ni payout reconciliation.

## Comprensión de negocio

### InternalPaymentRecord

`InternalPaymentRecord` representa la visión interna de la fintech sobre un pago.

Conceptualmente, este registro puede nacer como una intención de cobro y evolucionar según distintos momentos operativos del ciclo de pago, como autorización, clearing/compensación y settlement. Para este challenge no se modela el lifecycle completo del pago; la API solo utiliza el estado interno actual disponible para conciliación.

### ProcessorPaymentRecord

`ProcessorPaymentRecord` representa la visión informada por el procesador externo sobre una transacción individual.

El procesador externo se interpreta como un proveedor del lado adquirente, por ejemplo un `PSP`, `gateway`, `acquiring processor` o `acquirer`, que reporta pagos individuales procesados a la fintech.

La API no llama al procesador externo en tiempo real. Se asume que los registros informados por el procesador ya fueron ingeridos previamente dentro del ecosistema de la fintech mediante un archivo batch, webhook, API synchronization, proceso ETL o cualquier otro mecanismo de ingesta.

### Reconciliation

El proceso de `reconciliation` compara ambos registros para determinar si la visión interna de la fintech coincide con la visión reportada por el procesador externo.

La API responde no solo si el pago está conciliado, sino también qué diferencia existe cuando no lo está.

## Alcance

### Implementado

- Consultar conciliación por `paymentId`.
- Listar conciliaciones.
- Filtrar conciliaciones por `merchantId`, `reconciliationStatus` o `reconciled`.
- Detectar pagos existentes solo internamente.
- Detectar pagos existentes solo en los registros reportados por el procesador.
- Detectar diferencias de monto, moneda, estado, comercio y referencia externa.
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

## Arquitectura

El proyecto usa una versión simplificada de Hexagonal Architecture:

```text
controller
  -> use case
    -> domain service
      -> ports
        -> in-memory adapters
```

Estructura principal:

```text
src/main/java/com/oraex/reconciliation
 ├── domain
 │   ├── model
 │   ├── port
 │   └── service
 ├── application
 │   └── usecase
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
curl "http://localhost:8080/api/v1/reconciliations/payments?reconciliationStatus=AMOUNT_MISMATCH"
```

## Reconciliation statuses

| Status | Descripción |
|---|---|
| `RECONCILED` | Ambos registros existen y coinciden. |
| `ONLY_INTERNAL` | El pago existe internamente, pero no fue reportado por el procesador. |
| `ONLY_PROCESSOR` | El pago fue reportado por el procesador, pero no existe internamente. |
| `AMOUNT_MISMATCH` | Ambos registros existen, pero el monto difiere. |
| `CURRENCY_MISMATCH` | Ambos registros existen, pero la moneda difiere. |
| `STATUS_MISMATCH` | Ambos registros existen, pero el estado del pago difiere. |
| `MERCHANT_MISMATCH` | Ambos registros existen, pero el comercio difiere. |
| `REFERENCE_MISMATCH` | Ambos registros existen, pero la referencia externa difiere. |
| `MULTIPLE_MISMATCHES` | Existe más de una diferencia. |
| `NOT_FOUND` | El pago no existe en ninguna fuente. |

## Datos de ejemplo

| paymentId | Escenario |
|---|---|
| `PAY-1001` | Pago conciliado. |
| `PAY-1002` | Existe solo internamente. |
| `PAY-1003` | Existe solo en registros reportados por el procesador. |
| `PAY-1004` | Diferencia de monto. |
| `PAY-1005` | Diferencia de moneda. |
| `PAY-1006` | Diferencia de estado. |
| `PAY-1007` | Diferencia de comercio. |
| `PAY-1008` | Diferencia de referencia externa. |
| `PAY-9999` | No existe en ninguna fuente. |

## Ejemplo de respuesta

```json
{
  "paymentId": "PAY-1001",
  "merchantId": "MERCHANT-001",
  "reconciled": true,
  "reconciliationStatus": "RECONCILED",
  "internalPayment": {
    "exists": true,
    "paymentId": "PAY-1001",
    "externalReference": "EXT-9001",
    "merchantId": "MERCHANT-001",
    "amount": 10.00,
    "currency": "USD",
    "status": "PAID",
    "processedAt": "2026-05-07T10:00:00",
    "reportedAt": null
  },
  "processorPayment": {
    "exists": true,
    "paymentId": "PAY-1001",
    "externalReference": "EXT-9001",
    "merchantId": "MERCHANT-001",
    "amount": 10.00,
    "currency": "USD",
    "status": "PAID",
    "processedAt": null,
    "reportedAt": "2026-05-08T02:00:00"
  },
  "differences": [],
  "message": "Payment is reconciled successfully"
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

- Se usaron adaptadores in-memory para mantener el foco en el dominio y permitir ejecución local simple.
- No se implementó integración real con el procesador externo porque el challenge permite asumir que los datos de ambas fuentes ya están disponibles.
- No se modeló el lifecycle completo del pago. `InternalPaymentRecord` puede haber nacido como intención de cobro, pero la API usa únicamente el estado interno actual disponible para conciliación.
- `Payout reconciliation` queda fuera de alcance porque representa una liquidación agrupada hacia el comercio, con múltiples pagos, fees, refunds, ajustes, net amount y bank transfer.
- Se mantuvo una arquitectura hexagonal simplificada para separar dominio, casos de uso e infraestructura sin sobreingenierizar el challenge.

## Próximos pasos

- Reemplazar adaptadores in-memory por persistencia real, por ejemplo PostgreSQL.
- Implementar ingesta de reportes del procesador externo.
- Agregar paginación y ordenamiento.
- Agregar autenticación/autorización para consumidores internos.
- Agregar observabilidad: structured logs, metrics y tracing.
- Extender el modelo para payout reconciliation si el negocio lo requiere.
