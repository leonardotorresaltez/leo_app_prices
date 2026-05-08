# APP de Prices 

REST API desarrollada con Spring Boot 3.3.5 para consulta de precios aplicables según fecha, producto y cadena.

## Usa

- Java 21
- Spring Boot 3.3.5
- Spring Data JPA
- H2 (base de datos en memoria)
- Maven
- JUnit 5 + MockMvc (tests de integración)


## Principios aplicados

- **SOLID**: cada clase tiene responsabilidad única; dependencias por interfaces (puertos)
- **Hexagonal Architecture**: el dominio no conoce Spring, JPA ni HTTP
- **Clean Code**: nombres expresivos, sin lógica en controladores, mappers dedicados
- **Eficiencia**: una sola query SQL con `ORDER BY priority DESC LIMIT 1` — sin lógica de desambiguación en Java

## Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La aplicación arranca en `http://localhost:8080` e inicializa automáticamente la base de datos H2 con los datos de ejemplo.

H2 Console disponible en: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:pricesdb`
- User: `sa`
- Password: *(vacío)*

## Endpoint

### GET /api/v1/prices

Devuelve el precio aplicable para una fecha, producto y cadena dados.

**Parámetros de entrada:**

| Parámetro         | Tipo            | Descripción                          | Ejemplo                  |
|-------------------|-----------------|--------------------------------------|--------------------------|
| `applicationDate` | LocalDateTime   | Fecha y hora de consulta (ISO 8601)  | `2020-06-14T10:00:00`    |
| `productId`       | Long            | Identificador del producto           | `35455`                  |
| `brandId`         | Long            | Identificador de la cadena           | `1`                      |

**Respuesta exitosa (200 OK):**

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

| Código | Descripción                                 |
|--------|---------------------------------------------|
| `400`  | Parámetros inválidos o ausentes             |
| `404`  | No existe precio para los parámetros dados  |

**Ejemplo de uso:**

```bash
curl "http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1"
```

## Tests de integración

-          Test 1: petición a las 10:00 del día 14 del producto 35455   para la brand 1 (ZARA)
-          Test 2: petición a las 16:00 del día 14 del producto 35455   para la brand 1 (ZARA)
-          Test 3: petición a las 21:00 del día 14 del producto 35455   para la brand 1 (ZARA)
-          Test 4: petición a las 10:00 del día 15 del producto 35455   para la brand 1 (ZARA)
-          Test 5: petición a las 21:00 del día 16 del producto 35455   para la brand 1 (ZARA)

```bash
mvn test
```
