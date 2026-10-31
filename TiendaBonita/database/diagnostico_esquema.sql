-- Ejecutar en MySQL Workbench para confirmar que el esquema coincide con el proyecto.
USE tienda_bonita;
SHOW TABLES;

SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'tienda_bonita'
  AND TABLE_NAME IN ('usuario', 'rol', 'categoria', 'producto', 'cliente', 'venta', 'detalle_venta')
ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- Verifica que el nombre de la columna sea stock_minimo, no stok_minimo.
SHOW CREATE TABLE producto;
SHOW CREATE TABLE usuario;
SHOW CREATE TABLE cliente;
SHOW CREATE TABLE venta;
SHOW CREATE TABLE detalle_venta;
