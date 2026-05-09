# Decisiones de diseño

Este documento explica las principales decisiones de negocio, arquitectura y alcance tomadas para la implementación de la Payment Reconciliation API.

## 1. Alcance: payment reconciliation

La API se enfoca en `payment reconciliation`, es decir, en comparar pagos individuales registrados internamente contra pagos individuales reportados por el procesador externo.

Para este challenge, un pago se interpreta como una transacción financiera individual disponible para conciliación. No se modela como una solicitud de autorización en tiempo real ni como un flujo completo end-to-end de pagos.

## 2. InternalPaymentRecord

`InternalPaymentRecord` representa la visión interna de la fintech sobre un pago.

Conceptualmente, este registro puede haber nacido como una intención de cobro y evolucionado por distintos momentos operativos, como autorización, clearing/compensación y settlement.

Ese lifecycle completo no se modela en este challenge. La API solo utiliza el estado interno actual disponible para conciliación.

## 3. ProcessorPaymentRecord

`ProcessorPaymentRecord` representa la visión informada por el procesador externo sobre una transacción individual.

El procesador externo se interpreta como un proveedor del lado adquirente, por ejemplo un `PSP`, `gateway`, `acquiring processor` o `acquirer`, que reporta pagos individuales procesados a la fintech.

## 4. La API no llama al procesador externo en tiempo real

La API de conciliación no consulta al procesador externo en tiempo real.

El challenge permite asumir que los datos de ambas fuentes ya están disponibles. Por eso, los registros reportados por el procesador se modelan como datos ya ingeridos dentro del ecosistema de la fintech.

En un entorno real, esos datos podrían llegar mediante:

- Archivos batch.
- Webhooks.
- API synchronization.
- Procesos ETL.
- Data pipelines internos.

## 5. Payout reconciliation queda fuera de alcance

`Payout reconciliation` se excluye intencionalmente.

Un `payout` representa una liquidación agrupada y neta hacia el comercio. Puede incluir múltiples pagos, fees, refunds, ajustes, net amount, payout id e información de transferencia bancaria.

Este challenge se enfoca únicamente en conciliación a nivel de pago individual.

## 6. Hexagonal Architecture simplificada

Se utilizó una versión simplificada de Hexagonal Architecture para separar:

- Modelos y reglas de dominio.
- Casos de uso.
- Puertos de acceso a datos.
- Adaptadores de infraestructura.
- Capa REST.

El flujo principal es:

```text
Controller
  -> UseCase
      -> Ports
          -> In-memory adapters
      -> PaymentReconciliationService
          -> ReconciliationRule implementations
```

La decisión más importante es que el `UseCase` orquesta la operación de aplicación, mientras que el `PaymentReconciliationService` se limita a ejecutar reglas de conciliación con los insumos ya disponibles.

## 7. UseCase como orquestador de aplicación

`ReconcilePaymentUseCase` representa el caso de uso de aplicación: conciliar un pago.

Sus responsabilidades son:

1. Recibir el `paymentId`.
2. Buscar el registro interno mediante `InternalPaymentRecordPort`.
3. Buscar el registro del procesador mediante `ProcessorPaymentRecordPort`.
4. Delegar la decisión de conciliación al `PaymentReconciliationService`.
5. Retornar el resultado de dominio.

El use case no compara monto, moneda, estado, comercio ni referencia externa. Esa lógica pertenece al dominio.

## 8. Domain service para ejecutar reglas

`PaymentReconciliationService` se modeló como domain service porque la conciliación compara dos entidades distintas:

- `InternalPaymentRecord`.
- `ProcessorPaymentRecord`.

La decisión de conciliación no pertenece naturalmente a una sola entidad. Por eso se encapsula en un servicio de dominio.

El service no instancia reglas manualmente. Recibe `List<ReconciliationRule>` por inyección de dependencias, permitiendo agregar nuevas validaciones sin modificar el flujo principal.

## 9. Reglas de conciliación por DI

Cada implementación de `ReconciliationRule` representa una validación puntual:

- Pago no encontrado en ninguna fuente.
- Pago existente solo internamente.
- Pago existente solo en procesador.
- Diferencia de monto.
- Diferencia de moneda.
- Diferencia de estado.
- Diferencia de comercio.
- Diferencia de referencia externa.

Cada regla devuelve un `Optional<ReconciliationDifference>`.

Esto permite que el service solo ejecute reglas ordenadas por prioridad y consolide el resultado, sin crecer con condicionales de negocio.

## 10. Separación entre status final y tipo de diferencia

Se separaron dos conceptos:

```text
ReconciliationStatus
  -> resultado final de conciliación

ReconciliationDifferenceType
  -> detalle específico de la diferencia detectada
```

`ReconciliationStatus` queda limitado a estados finales:

- `RECONCILED`
- `ONLY_INTERNAL`
- `ONLY_PROCESSOR`
- `NOT_FOUND`
- `RECONCILED_WITH_DIFFERENCES`

Los detalles específicos viven en `ReconciliationDifferenceType`:

- `AMOUNT_MISMATCH`
- `CURRENCY_MISMATCH`
- `STATUS_MISMATCH`
- `MERCHANT_MISMATCH`
- `REFERENCE_MISMATCH`

Esta separación evita mezclar la clasificación general del resultado con el detalle operativo de cada diferencia.

Ejemplo:

```text
reconciliationStatus = RECONCILED_WITH_DIFFERENCES

differences:
  - type = AMOUNT_MISMATCH
  - type = CURRENCY_MISMATCH
```

## 11. RECONCILED_WITH_DIFFERENCES

`RECONCILED_WITH_DIFFERENCES` significa que ambos registros existen, pero no son equivalentes según las reglas de conciliación.

No significa necesariamente que ambos registros estén en estado `PAID`. Puede aplicar cuando ambos existen y difieren en monto, moneda, estado, comercio o referencia externa.

## 12. Optional y ausencia controlada

Los ports retornan `Optional` porque una búsqueda por `paymentId` puede no encontrar registros.

El `PaymentReconciliationService` no recibe `Optional` como parámetro público para evitar el warning y la práctica no recomendada de usar `Optional` como argumento.

En su lugar, el service recibe valores que pueden ser `null` y encapsula la ausencia inmediatamente al crear el `ReconciliationContext` mediante `Optional.ofNullable(...)`.

A partir de ese punto, las reglas trabajan con `Optional` y no con `null`.

## 13. DTOs y factory methods

Los DTOs de respuesta usan factory methods simples para evitar constructores largos con múltiples valores `null`.

Ejemplos:

- `PaymentRecordResponse.notFound()`
- `PaymentRecordResponse.fromInternal(record)`
- `PaymentRecordResponse.fromProcessor(record)`
- `ReconciliationDifferenceResponse.from(difference)`

Estos métodos no contienen lógica de negocio ni dependencias externas. Solo encapsulan la construcción de DTOs de forma legible.

## 14. Diseño de respuesta

La respuesta incluye:

- `reconciled`: indicador booleano para consumo rápido.
- `reconciliationStatus`: resultado final de la conciliación.
- `internalPayment`: visión interna de la fintech.
- `processorPayment`: visión reportada por el procesador.
- `differences`: lista explícita de diferencias.
- `message`: resumen legible para consumidores internos.

El mensaje de respuesta se resuelve en el mapper de la capa web porque forma parte del contrato de salida HTTP, no de la lógica central de conciliación.

## 15. Trade-off: solución ejecutable para challenge vs ecosistema productivo

Se priorizó una solución autocontenida, ejecutable localmente y enfocada en la lógica principal de conciliación. Esto permitió concentrar el esfuerzo en modelado de dominio, reglas de conciliación, contrato HTTP y pruebas dentro del tiempo disponible.

Como contrapartida, no se implementaron elementos habituales de un entorno productivo: versionamiento y políticas centralizadas en un API Gateway, autenticación/autorización, persistencia real, observabilidad avanzada, despliegue en Kubernetes/cloud, pipelines CI/CD con quality gates, análisis estático, pruebas que bloqueen builds y estrategia formal de release.

La decisión no implica desconocer esos componentes. En un escenario productivo, esta API debería convivir dentro de un ecosistema más completo de seguridad, operación, despliegue, monitoreo y automatización de releases. Para el challenge, se acotó el alcance para entregar una API revisable, ejecutable localmente y centrada en el dominio.

## 16. Adaptadores in-memory

Se usaron adaptadores in-memory para simular ambas fuentes:

- Registros internos de pagos.
- Registros reportados por el procesador.

Esta decisión mantiene la implementación ejecutable localmente y enfocada en la lógica de dominio, tal como pide el challenge.

En una evolución real, estos adaptadores podrían reemplazarse por persistencia en PostgreSQL, lectura de archivos de procesador, colas, jobs de ingesta o APIs internas.

## 17. Consumidores internos

La API está diseñada para equipos internos de la fintech:

- Backoffice / operaciones.
- Finanzas / conciliación.
- Soporte interno.
- Sistemas internos.

No se diseñó como API pública para clientes finales.

## 18. Evolución futura

Posibles siguientes pasos:

1. Persistencia real con PostgreSQL.
2. Flujo de ingesta de reportes del procesador.
3. Paginación y ordenamiento.
4. Autenticación/autorización.
5. Integración con API Gateway.
6. Observabilidad y trazabilidad.
7. Pipeline CI/CD con quality gates.
8. Despliegue en Kubernetes o cloud.
9. Payout reconciliation.
10. Settlement-level reconciliation.
11. Detección de duplicados.
