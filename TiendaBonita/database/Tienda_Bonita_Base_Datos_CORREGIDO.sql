-- TIENDA BONITA - importación y validación de productos (versión ordenada)
-- IMPORTANTE: confirmar primero que tienda_bonita y sus tablas ya existen.
-- Ejecutar con permiso para LOAD DATA LOCAL INFILE y editar la ruta del CSV si es necesario.

USE tienda_bonita;
SHOW TABLES;
SHOW CREATE TABLE producto;
SHOW CREATE TABLE producto_importacion;

-- Corrige el nombre de la columna solo si existe el nombre antiguo y aún no existe el nuevo.
SET @existe_stok_minimo := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'producto' AND COLUMN_NAME = 'stok_minimo'
);
SET @existe_stock_minimo := (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'producto' AND COLUMN_NAME = 'stock_minimo'
);
SET @sql_renombrar := IF(
    @existe_stok_minimo > 0 AND @existe_stock_minimo = 0,
    'ALTER TABLE producto CHANGE COLUMN stok_minimo stock_minimo INT NOT NULL DEFAULT 0',
    'SELECT ''La columna de stock mínimo ya está normalizada; no se modifica.'' AS mensaje'
);
PREPARE stmt_renombrar FROM @sql_renombrar;
EXECUTE stmt_renombrar;
DEALLOCATE PREPARE stmt_renombrar;

-- La tabla temporal conserva la fecha como texto porque el CSV contiene varios formatos.
ALTER TABLE producto_importacion MODIFY fecha_vencimiento VARCHAR(50);

-- Vacía solamente la tabla temporal y carga el CSV UNA sola vez.
-- Si no quieres reemplazar la importación temporal previa, comenta la línea TRUNCATE.
TRUNCATE TABLE producto_importacion;

LOAD DATA LOCAL INFILE 'C:/Users/Usuario/Documents/productos_tienda_bonita.csv'
INTO TABLE producto_importacion
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
(
    codigo, nombre, marca, calidad, tono_color, presentacion,
    @precio, @existencia, @stock_minimo, @fecha_vencimiento,
    @id_categoria, @id_proveedor, @extra1, @extra2
)
SET
    precio = NULLIF(TRIM(@precio), ''),
    existencia = NULLIF(TRIM(@existencia), ''),
    stock_minimo = NULLIF(TRIM(@stock_minimo), ''),
    fecha_vencimiento = NULLIF(TRIM(@fecha_vencimiento), ''),
    id_categoria = NULLIF(TRIM(@id_categoria), ''),
    id_proveedor = NULLIF(TRIM(@id_proveedor), '');

-- Revisión del resultado de la importación.
SELECT COUNT(*) AS registros_importados FROM producto_importacion;
SELECT * FROM producto_importacion LIMIT 10;

-- Revisa códigos duplicados: la consulta debe devolver cero filas.
SELECT codigo, COUNT(*) AS cantidad
FROM producto_importacion
GROUP BY codigo
HAVING COUNT(*) > 1;

-- Revisa campos obligatorios vacíos.
SELECT *
FROM producto_importacion
WHERE codigo IS NULL OR TRIM(codigo) = ''
   OR nombre IS NULL OR TRIM(nombre) = ''
   OR precio IS NULL
   OR existencia IS NULL
   OR stock_minimo IS NULL
   OR id_categoria IS NULL;

-- Revisa categorías inexistentes antes de copiar los productos.
SELECT pi.codigo, pi.nombre, pi.id_categoria
FROM producto_importacion pi
LEFT JOIN categoria c ON pi.id_categoria = c.id_categoria
WHERE c.id_categoria IS NULL;

-- Correcciones que aparecían en el SQL original (los códigos van entre comillas).
SET SQL_SAFE_UPDATES = 0;
UPDATE producto_importacion SET precio = 60 WHERE codigo = 'SHAM003';
UPDATE producto_importacion SET existencia = 3 WHERE codigo = 'tin0001';
UPDATE producto_importacion SET precio = 30 WHERE codigo = 'AM024';
UPDATE producto_importacion SET precio = 25 WHERE codigo = 'AM025';
UPDATE producto_importacion SET precio = 25 WHERE codigo = 'AM026';
SET SQL_SAFE_UPDATES = 1;

SELECT codigo, nombre, precio, existencia, stock_minimo
FROM producto_importacion
WHERE codigo IN ('SHAM003', 'tin0001', 'AM024', 'AM025', 'AM026');

-- Vista previa de los formatos de fecha recibidos.
SELECT DISTINCT fecha_vencimiento
FROM producto_importacion
WHERE fecha_vencimiento IS NOT NULL AND TRIM(fecha_vencimiento) <> ''
ORDER BY fecha_vencimiento;

-- Inserta solo códigos que aún no existen en producto.
-- Formatos de fecha contemplados: dd/mm/aaaa, aaaa-mm, aaaa, ene-30, feb-27, etc.
INSERT INTO producto
(
    codigo, nombre, marca, calidad, tono_color, presentacion,
    precio, existencia, stock_minimo, fecha_vencimiento,
    id_categoria, id_proveedor
)
SELECT
    pi.codigo,
    pi.nombre,
    pi.marca,
    pi.calidad,
    pi.tono_color,
    pi.presentacion,
    pi.precio,
    pi.existencia,
    pi.stock_minimo,
    CASE
        WHEN LOWER(TRIM(pi.fecha_vencimiento)) LIKE 'no pereced%' THEN NULL
        WHEN TRIM(pi.fecha_vencimiento) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
            THEN STR_TO_DATE(TRIM(pi.fecha_vencimiento), '%d/%m/%Y')
        WHEN TRIM(pi.fecha_vencimiento) REGEXP '^[0-9]{4}-[0-9]{2}$'
            THEN LAST_DAY(STR_TO_DATE(CONCAT(TRIM(pi.fecha_vencimiento), '-01'), '%Y-%m-%d'))
        WHEN TRIM(pi.fecha_vencimiento) REGEXP '^[0-9]{4}$'
            THEN STR_TO_DATE(CONCAT(TRIM(pi.fecha_vencimiento), '-12-31'), '%Y-%m-%d')
        WHEN LOWER(LEFT(TRIM(pi.fecha_vencimiento), 3)) IN
             ('ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic')
            THEN LAST_DAY(STR_TO_DATE(CONCAT(
                '20', RIGHT(TRIM(pi.fecha_vencimiento), 2), '-',
                CASE LOWER(LEFT(TRIM(pi.fecha_vencimiento), 3))
                    WHEN 'ene' THEN '01' WHEN 'feb' THEN '02' WHEN 'mar' THEN '03'
                    WHEN 'abr' THEN '04' WHEN 'may' THEN '05' WHEN 'jun' THEN '06'
                    WHEN 'jul' THEN '07' WHEN 'ago' THEN '08' WHEN 'sep' THEN '09'
                    WHEN 'oct' THEN '10' WHEN 'nov' THEN '11' WHEN 'dic' THEN '12'
                END,
                '-01'
            ), '%Y-%m-%d'))
        ELSE NULL
    END AS fecha_vencimiento,
    pi.id_categoria,
    pi.id_proveedor
FROM producto_importacion pi
WHERE pi.codigo IS NOT NULL AND TRIM(pi.codigo) <> ''
  AND NOT EXISTS (SELECT 1 FROM producto p WHERE p.codigo = pi.codigo);

-- Comprobaciones finales.
SELECT COUNT(*) AS total_productos FROM producto;
SELECT codigo, nombre, precio, existencia, stock_minimo, fecha_vencimiento
FROM producto
WHERE existencia <= stock_minimo
ORDER BY existencia, nombre;
SHOW CREATE TABLE producto;
