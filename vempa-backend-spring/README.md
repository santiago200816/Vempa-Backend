# VEMPA — Backend (Spring Boot + JPA + JWT + Docker)

Reescritura del backend de VEMPA en **Java 21 + Spring Boot 3** cumpliendo:
- ✅ Seguridad en contraseñas (BCrypt) y manejo de JWT
- ✅ Manejo de base de datos con **JPA** (Hibernate)
- ✅ Backend **dockerizado**
- ✅ Mismo contrato de API que el backend Node/Express anterior — el frontend casi no cambia

---

## Requisitos previos

- **JDK 21** (verifica con `java -version`)
- **Maven** (verifica con `mvn -version`) — o usa el wrapper si lo agregas con `mvn wrapper:wrapper`
- **Docker Desktop** (para la parte dockerizada)
- Tu base de datos de Filess.io y cuenta de Cloudinary (las que ya configuraste)

## 1. Configurar variables de entorno

```bash
cp .env.example .env
```

Completa `.env` con tus datos reales (los mismos de Filess.io/Cloudinary que ya tienes, más un correo/contraseña para el admin inicial):

```
DB_HOST=myhbfx.h.filess.io
DB_PORT=3306
DB_USER=santiago_bellwiseam
DB_PASSWORD=...
DB_NAME=santiago_bellwiseam

CLOUDINARY_CLOUD_NAME=...
CLOUDINARY_API_KEY=...
CLOUDINARY_API_SECRET=...

JWT_SECRET=...
JWT_EXPIRACION_MINUTOS=60
BCRYPT_STRENGTH=12

FRONTEND_URL=http://localhost:5173

ADMIN_EMAIL=admin@vempa.com
ADMIN_PASSWORD=una-contraseña-segura
```

⚠️ No necesitas correr el `schema.sql` de MySQL manualmente esta vez: **Hibernate crea/ajusta las tablas automáticamente** al arrancar (`ddl-auto: update` en `application.yml`), a partir de las entidades `Producto`, `Variante`, `Admin`. El usuario admin también se crea solo, al arrancar, leyendo `ADMIN_EMAIL`/`ADMIN_PASSWORD`.

El archivo `sql/schema.sql` queda como **referencia** (el SQL exacto que Hibernate genera) — útil para revisar/documentar la estructura o mostrarla en tu sustentación, no hace falta ejecutarlo.

## 2. Correr en local (sin Docker, para desarrollar rápido)

Como Spring Boot lee `application.yml` con variables de entorno del sistema (no lee `.env` automáticamente fuera de Docker), expórtalas antes de correr:

**En PowerShell (Windows):**
```powershell
Get-Content .env | ForEach-Object {
  if ($_ -match '^\s*([^#][^=]*)=(.*)$') {
    [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim())
  }
}
mvn spring-boot:run
```

**En Mac/Linux:**
```bash
export $(grep -v '^#' .env | xargs)
mvn spring-boot:run
```

Debe arrancar en el puerto 3000. Prueba: http://localhost:3000/api/health → `{"ok":true}`.

## 3. Correr con Docker (el requisito de "backend dockerizado")

Con Docker Desktop abierto:

```bash
docker compose up --build
```

Esto:
1. Construye la imagen con el `Dockerfile` (compila con Maven en una etapa, y en la etapa final solo deja el `.jar` corriendo sobre un JRE liviano — así la imagen final pesa mucho menos que si incluyera todo Maven).
2. Levanta el contenedor, leyendo las variables desde tu `.env` (`env_file` en `docker-compose.yml`).
3. Expone el puerto 3000.

Para pararlo: `docker compose down`.

## 4. Ver la base de datos en un gestor (MySQL Workbench) conectada a la nube

1. Abre **MySQL Workbench** (gratis) → **Database → Connect to Database**.
2. Host: el mismo `DB_HOST` de tu `.env` (`myhbfx.h.filess.io`). Puerto: `3306`. Usuario/contraseña: los mismos.
3. Al conectar, veas el esquema `santiago_bellwiseam` con las tablas `admins`, `productos`, `variantes` — ya creadas por Hibernate la primera vez que corriste la app.
4. Esto demuestra que tu app en la nube (o local) está escribiendo sobre la misma base de datos gestionada en Filess.io.

## 5. Conectar el frontend

En `vempa-app/.env`:
```
VITE_API_URL=http://localhost:3000
```
El contrato de la API es idéntico al backend anterior (mismas rutas, mismo formato JSON), así que el frontend funciona sin cambios de lógica — solo se agregan 2 vistas nuevas (ver el README del frontend).

---

## Guía para tu sustentación oral

### Hashing vs. encriptación
- **Encriptar** es reversible: con la clave correcta se puede volver a obtener el texto original (útil para datos que necesitas leer después, como un número de tarjeta).
- **Hashear** es de un solo sentido: no existe una función para "deshacer" un hash. Las contraseñas se **hashean**, nunca se encriptan, porque ni siquiera el propio sistema necesita saber la contraseña real — solo necesita poder *verificar* si una contraseña coincide.

### BCrypt y el factor de costo
- BCrypt es un algoritmo de hashing diseñado específicamente para contraseñas (a diferencia de SHA-256, que es rápido y por eso es malo para este uso — un atacante puede probar miles de millones de combinaciones por segundo).
- El **factor de costo** (`BCRYPT_STRENGTH`, en el código `strength`) define cuántas rondas internas (2^strength) se ejecutan. Con `strength=12` son 2¹² = 4096 rondas — cada +1 al factor **duplica** el tiempo de cómputo.
- Usamos `12` porque es el estándar recomendado en 2026: toma ~250-300ms por hash, lo cual es imperceptible para un login legítimo pero vuelve inviable un ataque de fuerza bruta a gran escala.
- BCrypt genera y guarda una **sal (salt)** distinta para cada contraseña, incrustada en el mismo hash resultante — por eso dos usuarios con la misma contraseña obtienen hashes completamente distintos, y por eso no hace falta guardar la sal en una columna aparte.
- Código: `SecurityConfig.passwordEncoder()` crea el `BCryptPasswordEncoder(strength)`. `AuthService.login()` usa `passwordEncoder.matches(passwordEnTextoPlano, hashGuardado)` para verificar — nunca se "desencripta" nada.

### Sistema de login
1. El usuario envía `POST /api/auth/login` con `{ email, password }`.
2. `AuthService` busca el admin por email, y compara la contraseña con `passwordEncoder.matches(...)`.
3. Si coincide, genera un JWT (`JwtService.generarToken`) y lo devuelve.
4. El frontend guarda ese token y lo manda en cada petición protegida como header `Authorization: Bearer <token>`.

### JWT: estructura y expiración
- Un JWT tiene 3 partes separadas por puntos: `header.payload.signature`.
  - **header**: algoritmo usado (aquí, HS256).
  - **payload**: los "claims" — en este caso, el email (`subject`), fecha de emisión (`iat`) y fecha de expiración (`exp`).
  - **signature**: firma HMAC-SHA256 calculada con `JWT_SECRET`. Garantiza que nadie pueda alterar el payload sin invalidar la firma.
- Importante para la sustentación: el payload **no está encriptado**, solo codificado en Base64 — cualquiera puede leerlo (pruébalo en https://jwt.io). Por eso nunca se debe poner información sensible (como la contraseña) dentro del token.
- **Tiempo de expiración**: configurado en `JWT_EXPIRACION_MINUTOS` (60 minutos por defecto). Pasado ese tiempo, `JwtService.validarYObtenerEmail()` lanza una excepción al intentar parsear el token (la librería `jjwt` valida la fecha `exp` automáticamente), y `JwtAuthFilter` responde como no autenticado → el usuario debe loguearse de nuevo.
- Esto es **autenticación stateless**: el servidor no guarda sesiones en memoria ni en base de datos — toda la prueba de identidad vive en el propio token. Por eso `SecurityConfig` usa `SessionCreationPolicy.STATELESS`.

### JPA / Hibernate
- Las clases en `model/` (`Producto`, `Variante`, `Admin`) son **entidades JPA**: cada una mapea a una tabla, cada atributo a una columna (anotaciones `@Entity`, `@Table`, `@Column`, `@Id`).
- La relación `Producto` 1-a-muchos `Variante` se modela con `@OneToMany`/`@ManyToOne`, y `cascade = ALL, orphanRemoval = true` significa: si borras un producto, sus variantes se borran solas; si quitas una variante de la lista en memoria, JPA la borra de la base de datos al guardar.
- Los `Repository` (`ProductoRepository`, etc.) extienden `JpaRepository`: Spring Data JPA genera automáticamente las consultas SQL a partir del nombre del método (ej. `findByActivoTrueOrderByCreatedAtDesc`), sin que tengamos que escribir SQL a mano.
- `ddl-auto: update` le dice a Hibernate que cree/ajuste las tablas automáticamente comparando las entidades contra la base de datos real.

### Docker
- El `Dockerfile` usa **multi-stage build**: una primera imagen con Maven completo compila el proyecto; la imagen final solo copia el `.jar` resultante sobre un JRE mínimo (`eclipse-temurin:21-jre-alpine`). Esto reduce el tamaño final de la imagen (no queda Maven, código fuente ni dependencias de build en la imagen que se despliega).
- `docker-compose.yml` simplifica correr el contenedor pasándole las variables de `.env` sin escribir un comando largo de `docker run`.

---

## Endpoints (idénticos al backend anterior)

| Método | Ruta | Auth | Rol |
|---|---|---|---|
| POST | `/api/auth/login` | No | — |
| GET | `/api/productos` | No | — |
| GET | `/api/productos/{id}` | No | — |
| GET | `/api/admin/productos` | Sí | ADMIN |
| POST | `/api/admin/productos` | Sí | ADMIN |
| PUT | `/api/admin/productos/{id}` | Sí | ADMIN |
| DELETE | `/api/admin/productos/{id}` | Sí | ADMIN |
| POST | `/api/admin/upload` | Sí | ADMIN |
| POST | `/api/clientes/registro` | No | — |
| POST | `/api/clientes/login` | No | — |
| GET | `/api/clientes/favoritos` | Sí | CLIENTE |
| GET | `/api/clientes/favoritos/ids` | Sí | CLIENTE |
| POST | `/api/clientes/favoritos/{productoId}` | Sí | CLIENTE |
| DELETE | `/api/clientes/favoritos/{productoId}` | Sí | CLIENTE |

### Dos tipos de cuenta (ADMIN y CLIENTE)

El JWT ahora lleva un claim `role` (`ADMIN` o `CLIENTE`), y `SecurityConfig` exige el rol correcto por prefijo de ruta (`/api/admin/**` → `hasRole("ADMIN")`, `/api/clientes/**` → `hasRole("CLIENTE")`). Un token de cliente no puede usar rutas de admin y viceversa, aunque ambos sean JWT válidos firmados con la misma clave.

Los clientes (tabla `usuarios`) pueden registrarse libremente desde `/registro` en el frontend — a diferencia del admin, que se crea solo por variables de entorno. Guardan productos en `favoritos` (tabla puente `usuario_id` + `producto_id`), y al pedir por WhatsApp, el frontend agrega automáticamente su nombre y teléfono al mensaje si están logueados.

## ⚠️ Nota importante

No pude compilar este proyecto dentro de este entorno (no tengo acceso de red a Maven Central desde aquí para descargar las dependencias de Spring). Revisé el código cuidadosamente a mano, pero **corre `mvn clean package` en tu máquina apenas lo descargues** y, si sale algún error de compilación, pégamelo aquí y lo corregimos de inmediato.
