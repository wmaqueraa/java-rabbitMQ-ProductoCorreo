# Producto Service API

Microservicio REST desarrollado con Spring Boot para la gestión de productos, con integración a RabbitMQ para notificaciones por correo y base de datos H2 en memoria.

---

## Tecnologías

- Java 11
- Spring Boot 2.7.8
- Spring Data JPA
- Spring AMQP (RabbitMQ)
- Spring Mail (Mailtrap)
- H2 Database
- Hibernate

---

## Configuración

### Variables de entorno requeridas
```properties
# Base de datos
DB_URL=jdbc:h2:mem:userdb
DB_USERNAME=sa
DB_PASSWORD=password

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672

# Mail
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=tu_username
MAIL_PASSWORD=tu_password
```

### Ejecutar la aplicación
```bash
mvn spring-boot:run
```

La aplicación inicia en el puerto `9100` por defecto.

---

## API Endpoints

Base URL: `http://localhost:9100/api/productos`

---

### Listar todos los productos

**GET** `/api/productos`

Retorna la lista completa de productos registrados.

**Request:**
```http
GET http://localhost:9100/api/productos
```

**Response `200 OK`:**
```json
[
  {
    "id": "59ab6aa6-066b-4762-b969-ddf2c4c69875",
    "codigo": "ZH0001",
    "nombre": "ZAPILLAS URBANA START BELL",
    "descripcion": "PLANTA ERGONOMICA CON CUERO ORIGINAL Y COLORES MATE",
    "precio": 500.00,
    "stock": 20,
    "email": "gerencia@valtx.pe",
    "activo": true,
    "fechaCreacion": "2026-03-18T21:00:00",
    "fechaActualizacion": "2026-03-18T21:00:00"
  }
]
```

---

### Obtener producto por ID

**GET** `/api/productos/{id}`

Retorna un producto específico por su UUID.

**Request:**
```http
GET http://localhost:9100/api/productos/59ab6aa6-066b-4762-b969-ddf2c4c69875
```

**Response `200 OK`:**
```json
{
  "id": "59ab6aa6-066b-4762-b969-ddf2c4c69875",
  "codigo": "ZH0001",
  "nombre": "ZAPILLAS URBANA START BELL",
  "descripcion": "PLANTA ERGONOMICA CON CUERO ORIGINAL Y COLORES MATE",
  "precio": 500.00,
  "stock": 20,
  "email": "gerencia@valtx.pe",
  "activo": true,
  "fechaCreacion": "2026-03-18T21:00:00",
  "fechaActualizacion": "2026-03-18T21:00:00"
}
```

**Response `404 Not Found`:** producto no existe.

---

### Crear producto

**POST** `/api/productos`

Crea un nuevo producto. El `id` y las fechas son generados automáticamente.
Al crearse exitosamente, se publica una notificación en RabbitMQ que dispara el envío de un correo.

**Request:**
```http
POST http://localhost:9100/api/productos
Content-Type: application/json
```
```json
{
  "codigo": "ZH0001",
  "nombre": "ZAPILLAS URBANA START BELL",
  "descripcion": "PLANTA ERGONOMICA CON CUERO ORIGINAL Y COLORES MATE",
  "precio": 500,
  "stock": 20,
  "email": "gerencia@valtx.pe",
  "activo": true
}
```

**Response `201 Created`:**
```json
{
  "id": "59ab6aa6-066b-4762-b969-ddf2c4c69875",
  "codigo": "ZH0001",
  "nombre": "ZAPILLAS URBANA START BELL",
  "descripcion": "PLANTA ERGONOMICA CON CUERO ORIGINAL Y COLORES MATE",
  "precio": 500.00,
  "stock": 20,
  "email": "gerencia@valtx.pe",
  "activo": true,
  "fechaCreacion": "2026-03-18T21:00:00",
  "fechaActualizacion": "2026-03-18T21:00:00"
}
```

**Response `400 Bad Request`:** el código del producto ya existe.

> **Nota:** los campos `fechaCreacion` y `fechaActualizacion` no deben enviarse en el request, Spring los gestiona automáticamente con `@EnableJpaAuditing`.

---

### Actualizar producto

**PUT** `/api/productos/{id}`

Actualiza los datos de un producto existente por su UUID.

**Request:**
```http
PUT http://localhost:9100/api/productos/59ab6aa6-066b-4762-b969-ddf2c4c69875
Content-Type: application/json
```
```json
{
  "nombre": "ZAPILLAS URBANA START BELL EDICION ESPECIAL",
  "precio": 550,
  "stock": 15
}
```

**Response `200 OK`:**
```json
{
  "id": "59ab6aa6-066b-4762-b969-ddf2c4c69875",
  "codigo": "ZH0001",
  "nombre": "ZAPILLAS URBANA START BELL EDICION ESPECIAL",
  "descripcion": "PLANTA ERGONOMICA CON CUERO ORIGINAL Y COLORES MATE",
  "precio": 550.00,
  "stock": 15,
  "email": "gerencia@valtx.pe",
  "activo": true,
  "fechaCreacion": "2026-03-18T21:00:00",
  "fechaActualizacion": "2026-03-18T21:30:00"
}
```

**Response `404 Not Found`:** producto no existe.

---

### Eliminar producto

**DELETE** `/api/productos/{id}`

Elimina un producto por su UUID.

**Request:**
```http
DELETE http://localhost:9100/api/productos/59ab6aa6-066b-4762-b969-ddf2c4c69875
```

**Response `204 No Content`:** producto eliminado exitosamente.

**Response `404 Not Found`:** producto no existe.

---

## Resumen de endpoints

| Método | Endpoint | Descripción | Response |
|--------|----------|-------------|----------|
| GET | `/api/productos` | Listar todos | 200 |
| GET | `/api/productos/{id}` | Obtener por ID | 200 / 404 |
| POST | `/api/productos` | Crear producto | 201 / 400 |
| PUT | `/api/productos/{id}` | Actualizar producto | 200 / 404 |
| DELETE | `/api/productos/{id}` | Eliminar producto | 204 / 404 |

---

## Flujo de notificación
```
POST /api/productos
        ↓
ProductoController
        ↓
ProductoService → guarda en H2
        ↓
MessagePublisher → publica en RabbitMQ
        ↓
MessageConsumer → recibe el mensaje
        ↓
EmailService → envía correo via Mailtrap
```

---

## Consola H2

Disponible en desarrollo para inspeccionar la base de datos directamente:
```
URL:      http://localhost:9100/h2-console
JDBC URL: jdbc:h2:mem:userdb
Usuario:  sa
Password: password
```
