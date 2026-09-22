-- =========================================================
-- VEMPA — Esquema de base de datos (MySQL / Filess.io)
--
-- NO necesitas correr esto a mano: Hibernate lo crea/actualiza
-- solo al arrancar la aplicación (application.yml → ddl-auto: update),
-- leyendo las entidades JPA en src/main/java/com/vempa/model/.
--
-- Este archivo es de REFERENCIA: muestra exactamente el SQL que
-- Hibernate genera, para que puedas revisarlo, documentarlo o
-- correrlo manualmente si alguna vez desactivas el ddl-auto.
-- =========================================================

CREATE TABLE IF NOT EXISTS admins (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS productos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  descripcion TEXT,
  precio DECIMAL(10,2) NOT NULL,
  categoria VARCHAR(50) NOT NULL,
  imagen_url VARCHAR(500),
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME
);

CREATE TABLE IF NOT EXISTS variantes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  producto_id BIGINT NOT NULL,
  color VARCHAR(100) NOT NULL,
  talla VARCHAR(50),
  stock INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_variantes_producto
    FOREIGN KEY (producto_id) REFERENCES productos(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  telefono VARCHAR(30),
  password_hash VARCHAR(255) NOT NULL,
  created_at DATETIME
);

CREATE TABLE IF NOT EXISTS favoritos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  producto_id BIGINT NOT NULL,
  created_at DATETIME,
  CONSTRAINT fk_favoritos_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_favoritos_producto
    FOREIGN KEY (producto_id) REFERENCES productos(id)
    ON DELETE CASCADE,
  CONSTRAINT uk_favoritos_usuario_producto UNIQUE (usuario_id, producto_id)
);

-- =========================================================
-- Cómo se relaciona esto con el código Java (para tu sustentación):
--
-- Admin.java          -> tabla admins
--   @Column(name = "password_hash") private String passwordHash;
--   (el atributo Java es camelCase, la columna real en MySQL es
--    snake_case — JPA traduce entre los dos mundos)
--
-- Producto.java        -> tabla productos
--   @OneToMany(mappedBy = "producto", cascade = ALL, orphanRemoval = true)
--   private List<Variante> variantes;
--   -> Por esto, al borrar un producto, sus variantes se borran solas
--      (equivalente al "ON DELETE CASCADE" de arriba)
--
-- Variante.java         -> tabla variantes
--   @ManyToOne @JoinColumn(name = "producto_id")
--   private Producto producto;
-- =========================================================
