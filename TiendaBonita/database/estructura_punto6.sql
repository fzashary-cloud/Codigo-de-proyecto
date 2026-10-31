-- ESQUEMA DE REFERENCIA PARA PUNTO 6
-- Úsalo solo si faltan tablas en tienda_bonita. CREATE TABLE IF NOT EXISTS no modifica
-- tablas preexistentes; por eso primero ejecuta diagnostico_esquema.sql y revisa columnas.
CREATE DATABASE IF NOT EXISTS tienda_bonita
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tienda_bonita;

CREATE TABLE IF NOT EXISTS rol (
    id_rol INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    permisos VARCHAR(255) NULL,
    PRIMARY KEY (id_rol),
    UNIQUE KEY uq_rol_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    usuario VARCHAR(80) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    estado TINYINT(1) NOT NULL DEFAULT 1,
    id_rol INT NOT NULL,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uq_usuario_usuario (usuario),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categoria (
    id_categoria INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    estado TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id_categoria),
    UNIQUE KEY uq_categoria_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS proveedor (
    id_proveedor INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    nit VARCHAR(30) NULL,
    telefono VARCHAR(40) NULL,
    correo VARCHAR(150) NULL,
    direccion VARCHAR(255) NULL,
    PRIMARY KEY (id_proveedor)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cliente (
    id_cliente INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    nit VARCHAR(30) NULL,
    telefono VARCHAR(40) NULL,
    correo VARCHAR(150) NULL,
    direccion VARCHAR(255) NULL,
    PRIMARY KEY (id_cliente),
    KEY ix_cliente_nombre (nombre),
    KEY ix_cliente_nit (nit)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS producto (
    id_producto INT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(180) NOT NULL,
    marca VARCHAR(100) NULL,
    calidad VARCHAR(100) NULL,
    tono_color VARCHAR(100) NULL,
    presentacion VARCHAR(100) NULL,
    precio DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    existencia INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    fecha_vencimiento DATE NULL,
    id_categoria INT NOT NULL,
    id_proveedor INT NULL,
    PRIMARY KEY (id_producto),
    UNIQUE KEY uq_producto_codigo (codigo),
    KEY ix_producto_nombre (nombre),
    KEY ix_producto_vencimiento (fecha_vencimiento),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria),
    CONSTRAINT fk_producto_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor),
    CONSTRAINT chk_producto_precio CHECK (precio >= 0),
    CONSTRAINT chk_producto_existencia CHECK (existencia >= 0),
    CONSTRAINT chk_producto_stock_minimo CHECK (stock_minimo >= 0)
) ENGINE=InnoDB;

-- Tabla temporal opcional para cargar el CSV antes de normalizar sus fechas.
CREATE TABLE IF NOT EXISTS producto_importacion (
    codigo VARCHAR(50) NULL,
    nombre VARCHAR(180) NULL,
    marca VARCHAR(100) NULL,
    calidad VARCHAR(100) NULL,
    tono_color VARCHAR(100) NULL,
    presentacion VARCHAR(100) NULL,
    precio DECIMAL(10,2) NULL,
    existencia INT NULL,
    stock_minimo INT NULL,
    fecha_vencimiento VARCHAR(50) NULL,
    id_categoria INT NULL,
    id_proveedor INT NULL,
    extra1 VARCHAR(255) NULL,
    extra2 VARCHAR(255) NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS venta (
    id_venta INT NOT NULL AUTO_INCREMENT,
    fecha DATE NOT NULL,
    id_cliente INT NOT NULL,
    id_usuario INT NOT NULL,
    total DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(30) NOT NULL DEFAULT 'Confirmada',
    PRIMARY KEY (id_venta),
    KEY ix_venta_fecha (fecha),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalle_venta (
    id_detalle INT NOT NULL AUTO_INCREMENT,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id_detalle),
    KEY ix_detalle_venta (id_venta),
    KEY ix_detalle_producto (id_producto),
    CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

-- No se crea un usuario fijo automáticamente. El login valida la tabla usuario.
-- Ejemplo opcional para una base de datos vacía; ejecuta y cambia la contraseña antes de usar:
-- INSERT INTO rol (nombre, permisos) VALUES ('Administrador', 'ADMINISTRACION')
--   ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);
-- INSERT INTO usuario (nombre, usuario, contrasena, estado, id_rol)
-- SELECT 'Administradora', 'admin', 'CAMBIA_ESTA_CLAVE', 1, r.id_rol
-- FROM rol r WHERE r.nombre = 'Administrador'
--   AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.usuario = 'admin');
