# Prices API

REST API desarrollada con Spring Boot 3 para consultar el precio aplicable a un producto en una fecha dada, con desambiguación por prioridad cuando varios rangos se solapan.

## Stack

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.3.5 | Framework de aplicación |
| Spring Data JPA | — | Acceso a datos |
| H2 | — | Base de datos en memoria |
| Maven | — | Gestión de dependencias y build |
| JUnit 5 + MockMvc | — | Tests de integración |
| Mockito + AssertJ | — | Tests unitarios |

## Arrancar la aplicación

```bash
mvn spring-boot:run
```

La aplicación levanta en `http://localhost:8080` e inicializa la base de datos H2 con los datos de ejemplo al arrancar.

Consola H2 disponible en `http://localhost:8080/h2-console`:
- JDBC URL: `jdbc:h2:mem:pricesdb`
- User: `sa` / Password: *(vacío)*

---

## Arquitectura

La aplicación sigue **arquitectura hexagonal (Ports & Adapters)**. El principio central es que el núcleo de negocio (dominio) no conoce ningún detalle de infraestructura: ni Spring, ni JPA, ni HTTP. Todo lo externo se conecta mediante interfaces (puertos) que el dominio define y que la infraestructura implementa.

```
┌──────────────────────────────────────────────────────────┐
│  Infrastructure                                          │
│                                                          │
│   ┌─────────────┐              ┌──────────────────────┐  │
│   │ REST        │              │ Persistence          │  │
│   │ Controller  │              │ Adapter              │  │
│   │ (in-adapter)│              │ (out-adapter)        │  │
│   └──────┬──────┘              └──────────┬───────────┘  │
│          │                               │               │
│   ┌──────▼───────────────────────────────▼───────────┐   │
│   │  Application                                     │   │
│   │  GetApplicablePriceService                       │   │
│   └──────┬───────────────────────────────┬───────────┘   │
│          │                               │               │
│   ┌──────▼──────────┐         ┌──────────▼────────────┐  │
│   │ Port IN         │         │ Port OUT              │  │
│   │ GetApplicable   │         │ PriceRepository       │  │
│   │ PriceUseCase    │         │ (interfaz)            │  │
│   └─────────────────┘         └───────────────────────┘  │
│          Domain (sin dependencias externas)               │
└──────────────────────────────────────────────────────────┘
```

### Capas

**Domain** — contiene el modelo de negocio (`Price`) y las interfaces de los puertos. No importa ninguna clase de Spring ni JPA; puede compilar y testearse de forma completamente aislada.

**Application** — `GetApplicablePriceService` implementa el puerto de entrada y orquesta la operación: delega en el puerto de salida y lanza `PriceNotFoundException` si no hay resultado. Está anotado con `@Transactional(readOnly = true)` para evitar el dirty checking de Hibernate en operaciones de solo lectura.

**Infrastructure** — dos adaptadores:
- *Adaptador de entrada* (`PriceController`): recibe la petición HTTP, valida los parámetros y devuelve el DTO de respuesta.
- *Adaptador de salida* (`PriceRepositoryAdapter`): implementa el puerto de repositorio usando Spring Data JPA y mapea la entidad al modelo de dominio.


---

## Modelo de datos

Tabla `PRICES` inicializada con los datos del enunciado:

| BRAND_ID | START_DATE | END_DATE | PRICE_LIST | PRODUCT_ID | PRIORITY | PRICE | CURR |
|---|---|---|---|---|---|---|---|
| 1 | 2020-06-14 00:00:00 | 2020-12-31 23:59:59 | 1 | 35455 | 0 | 35.50 | EUR |
| 1 | 2020-06-14 15:00:00 | 2020-06-14 18:30:00 | 2 | 35455 | 1 | 25.45 | EUR |
| 1 | 2020-06-15 00:00:00 | 2020-06-15 11:00:00 | 3 | 35455 | 1 | 30.50 | EUR |
| 1 | 2020-06-15 16:00:00 | 2020-12-31 23:59:59 | 4 | 35455 | 1 | 38.95 | EUR |

Cuando dos tarifas se solapan, se aplica la de mayor `PRIORITY` (columna). La desambiguación ocurre en base de datos mediante `ORDER BY priority DESC LIMIT 1`, sin lógica adicional en Java.

---

## Endpoint

### `GET /api/v1/prices`

**Parámetros:**

| Parámetro | Tipo | Descripción | Ejemplo |
|---|---|---|---|
| `applicationDate` | ISO 8601 | Fecha y hora de consulta | `2020-06-14T10:00:00` |
| `productId` | Long | Identificador del producto | `35455` |
| `brandId` | Long | Identificador de la cadena | `1` |

**Respuesta `200 OK`:**

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 1,
  "startDate": "2020-06-14T00:00:00",
  "endDate": "2020-12-31T23:59:59",
  "price": 35.50,
  "currency": "EUR"
}
```

**Errores:**

| Código | Causa |
|---|---|
| `400` | Parámetros ausentes o con formato inválido |
| `404` | No existe precio para los parámetros dados |

**Ejemplo:**

```bash
curl "http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"
```

---

## Decisiones técnicas

**`BigDecimal` para el precio** — evita los errores de representación en punto flotante inherentes a `double`/`float`, mejor para valores de dinero

**Desambiguación en base de datos** — la query usa `ORDER BY priority DESC LIMIT 1` directamente en SQL. Traer todos los registros coincidentes y ordenarlos en Java sería menos eficiente

**Índice compuesto** — `@Index(columnList = "PRODUCT_ID, BRAND_ID, START_DATE, END_DATE, PRIORITY")` declarado en `PriceEntity`. Con H2 en memoria el beneficio es marginal, pero el índice se genera automáticamente en cualquier base de datos de producción que soporte JPA.

**`@Transactional(readOnly = true)`** — desactiva el dirty checking de Hibernate (snapshot de entidades para detectar cambios al cerrar la sesión). En una operación de solo lectura ese trabajo es completamente innecesario.

**Mappers manuales en lugar de MapStruct** — las conversiones son simples y el número de campos es reducido. Añadir MapStruct introduce un procesador de anotaciones y una dependencia de compilación que no aporta valor en este contexto.

---

## Tests

```bash
mvn test
```

