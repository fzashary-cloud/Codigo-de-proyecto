USE tienda_bonita;
show tables;

show variables like 'local_infile';
alter table producto_importacion 
modify fecha_vencimiento varchar(50); 

TRUNCATE TABLE producto_importacion;

LOAD DATA LOCAL INFILE 'C:/Users/Usuario/Documents/productos_tienda_bonita.csv'
INTO TABLE producto_importacion
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
(codigo, nombre, marca, calidad, tono_color, presentacion,
 precio, existencia, stock_minimo, fecha_vencimiento,
 id_categoria, id_proveedor, @extra1, @extra2)
 set 
 precio = nullif(TRIM(@precio), ''),
 existencia = nullif(TRIM(@existencia), ''),
stock_minimo= nullif(TRIM(@stock_minimo), ''),
 id_categoria = nullif(TRIM(@id_categoria), ''),
 id_producto = nullif(TRIM(@id_producto), '');
 ; 
 
 
 show variables like 'secure_file_priv';
 
 TRUNCATE TABLE producto_importacion;

LOAD DATA INFILE 'C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/productos_tienda_bonita.csv'
INTO TABLE producto_importacion
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
(codigo, nombre, marca, calidad, tono_color, presentacion,
 precio, existencia, stock_minimo, fecha_vencimiento,
 id_categoria, @id_proveedor, @extra1, @extra2)
SET id_proveedor = NULLIF(@id_proveedor, '');


TRUNCATE TABLE producto_importacion;

LOAD DATA INFILE 'C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/productos_tienda_bonita.csv'
INTO TABLE producto_importacion
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\r\n'
IGNORE 1 LINES
(codigo, nombre, marca, calidad, tono_color, presentacion,
 @precio, @existencia, @stock_minimo, fecha_vencimiento,
 @id_categoria, @id_proveedor, @extra1, @extra2)
SET
precio = NULLIF(TRIM(@precio), ''),
existencia = NULLIF(TRIM(@existencia), ''),
stock_minimo = NULLIF(TRIM(@stock_minimo), ''),
id_categoria = NULLIF(TRIM(@id_categoria), ''),
id_proveedor = NULLIF(TRIM(@id_proveedor), '');

select count(*) as registros_importados
from producto_importacion;

select *
from producto_importacion
limit 10; 

select codigo, count(*) as cantidad 
from producto_importacion
group by codigo
having count(*) > 1;

select *
from producto_importacion
where codigo is null or trim(codigo) = ''
or nombre is null or trim(nombre) = ''
or precio is null
or existencia is null
or stock_minimo is null
or id_categoria is null;

select pi.codigo, pi.nombre, pi.id_categoria
from producto_importacion pi
left join categoria c
on pi.id_categoria = c.id_categoria
where c.id_categoria is null;

update producto_importacion
set precio = 60
where codigo = 'SHAM003';

update producto_importacion
set existencia = 3
where codigo = 'tin0001';

update producto_importacion
set precio = 30
where codigo = 'AM024';

update producto_importacion
set precio = 25
where codigo = AM025;

update producto_importacion
set precio = 25
where codigo = 'AM026'; 

set sql_safe_updates = 1;

SET SQL_SAFE_UPDATES = 0;

UPDATE producto_importacion SET precio = 60
WHERE codigo = 'SHAM003';

UPDATE producto_importacion SET existencia = 3
WHERE codigo = 'tin0001';

UPDATE producto_importacion SET precio = 30
WHERE codigo = 'AM024';

UPDATE producto_importacion SET precio = 25
WHERE codigo = 'AM025';

UPDATE producto_importacion SET precio = 25
WHERE codigo = 'AM026';

SET SQL_SAFE_UPDATES = 1;

select codigo, nombre, precio, existencia, stock_minimo
from producto_importacion
where codigo in ('SHAM003', 'tin0001', 'AM024', 'AM025', 'AM026'); 

select distinct fecha_vencimiento
from producto_importacion
order by fecha_vencimiento;

select distinct fecha_vencimiento
from producto_importacion
where fecha_vencimiento is not null
and trim(fecha_vencimiento)<>''
order by fecha_vencimiento; 

INSERT INTO producto
(
    codigo,
    nombre,
    marca,
    calidad,
    tono_color,
    presentacion,
    precio,
    existencia,
    stock_minimo,
    fecha_vencimiento,
    id_categoria,
    id_proveedor
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
        -- No perecederos
        WHEN LOWER(TRIM(pi.fecha_vencimiento)) LIKE 'no pereced%'
            THEN NULL

        -- Ejemplo: 09/11/2028
        WHEN TRIM(pi.fecha_vencimiento)
             REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'
            THEN STR_TO_DATE(
                TRIM(pi.fecha_vencimiento),
                '%d/%m/%Y'
            )

        -- Ejemplo: 2028-03
        WHEN TRIM(pi.fecha_vencimiento)
             REGEXP '^[0-9]{4}-[0-9]{2}$'
            THEN LAST_DAY(
                STR_TO_DATE(
                    CONCAT(TRIM(pi.fecha_vencimiento), '-01'),
                    '%Y-%m-%d'
                )
            )

        -- Ejemplo: 2030
        WHEN TRIM(pi.fecha_vencimiento)
             REGEXP '^[0-9]{4}$'
            THEN STR_TO_DATE(
                CONCAT(TRIM(pi.fecha_vencimiento), '-12-31'),
                '%Y-%m-%d'
            )

        -- Ejemplos: ene-30, feb-27, mar-29, etc.
        WHEN LOWER(LEFT(TRIM(pi.fecha_vencimiento),3))
             IN ('ene','feb','mar','abr','may','jun',
                 'jul','ago','sep','oct','nov','dic')
            THEN LAST_DAY(
                STR_TO_DATE(
                    CONCAT(
                        '20',
                        RIGHT(TRIM(pi.fecha_vencimiento),2),
                        '-',
                        CASE LOWER(LEFT(TRIM(pi.fecha_vencimiento),3))
                            WHEN 'ene' THEN '01'
                            WHEN 'feb' THEN '02'
                            WHEN 'mar' THEN '03'
                            WHEN 'abr' THEN '04'
                            WHEN 'may' THEN '05'
                            WHEN 'jun' THEN '06'
                            WHEN 'jul' THEN '07'
                            WHEN 'ago' THEN '08'
                            WHEN 'sep' THEN '09'
                            WHEN 'oct' THEN '10'
                            WHEN 'nov' THEN '11'
                            WHEN 'dic' THEN '12'
                        END,
                        '-01'
                    ),
                    '%Y-%m-%d'
                )
            )

        ELSE NULL
    END,

    pi.id_categoria,
    pi.id_proveedor

FROM producto_importacion pi

WHERE NOT EXISTS (
    SELECT 1
    FROM producto p
    WHERE p.codigo = pi.codigo
);

describe producto; 

alter table producto 
change column stok_minimo stock_minimo int not null default 0;