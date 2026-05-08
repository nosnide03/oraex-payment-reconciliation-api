# Decisiones de diseño

## 1. Alcance: payment reconciliation

La API se enfoca en `payment reconciliation`, es decir, en comparar pagos individuales registrados internamente contra pagos individuales reportados por el procesador externo.

Para este challenge, un pago se interpreta como una transacción financiera individual disponible para conciliación. No se modela como una solicitud de autorización en tiempo real.

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

Un `payout` representa una liquidación agrupada y neta hacia el comercio. Puede incluir:

- Múltiples pagos.
- Fees.
- Refunds.
- Ajustes.
- Net amount.
- Payout id.
- Información de transferencia bancaria.

Este challenge se enfoca únicamente en conciliación a nivel de pago individual.

## 6. Adaptadores in-memory

Se usaron adaptadores in-memory para simular ambas fuentes:

- Registros internos de pagos.
- Registros reportados por el procesador.

Esta decisión mantiene la implementación ejecutable localmente y enfocada en la lógica de dominio, tal como pide el challenge.

## 7. Hexagonal Architecture simplificada

Se utilizó una versión simplificada de Hexagonal Architecture para separar:

- Modelos y reglas de dominio.
- Casos de uso.
- Adaptadores de infraestructura.
- Capa REST.

Esto permite reemplazar los adaptadores in-memory por persistencia real o fuentes de ingesta reales sin modificar la lógica de conciliación del dominio.

## 8. Diseño de respuesta

La respuesta incluye:

- `reconciled`: indicador booleano para consumo rápido.
- `reconciliationStatus`: clasificación detallada del resultado.
- `internalPayment`: visión interna de la fintech.
- `processorPayment`: visión reportada por el procesador.
- `differences`: lista explícita de diferencias.
- `message`: resumen legible para consumidores internos.

Este diseño soporta necesidades de backoffice, operaciones, finanzas/conciliación, soporte interno y sistemas internos.

## 9. Consumidores internos

La API está diseñada para equipos internos de la fintech:

- Backoffice / operaciones.
- Finanzas / conciliación.
- Soporte interno.
- Sistemas internos.

No se diseñó como API pública para clientes finales.

## 10. Evolución futura

Posibles siguientes pasos:

1. Persistencia real con PostgreSQL.
2. Flujo de ingesta de reportes del procesador.
3. Paginación y ordenamiento.
4. Autenticación/autorización.
5. Payout reconciliation.
6. Settlement-level reconciliation.
7. Detección de duplicados.
8. Observabilidad.
9. Pipeline CI/CD.
