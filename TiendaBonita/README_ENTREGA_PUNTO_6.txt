TIENDA BONITA — PUNTO 6: INTERFAZ GRÁFICA
================================================

Incluye las seis ventanas pedidas:
6.1 FrmLogin          — valida usuario, contraseña y estado desde MySQL.
6.2 FrmMenuPrincipal  — abre Productos, Clientes, Ventas y Reportes; permite cerrar sesión.
6.3 FrmProductos      — alta, búsqueda, actualización y eliminación de productos.
6.4 FrmClientes       — alta, búsqueda, actualización y eliminación de clientes.
6.5 FrmVentas         — agrega productos, valida cantidades y vencimiento, registra venta/detalle
                         y descuenta existencias dentro de una transacción MySQL.
6.6 FrmReportes       — todos los productos, stock bajo, categoría, vencidos, ventas, ventas por
                         fechas y clientes registrados.

REQUISITOS
----------
- JDK 21 o superior.
- MySQL Server iniciado.
- Base de datos llamada tienda_bonita.
- El conector MySQL está declarado como dependencia Maven en pom.xml.
- Las tablas deben tener los nombres de las tablas/columnas indicados en
  database/diagnostico_esquema.sql. El archivo SQL recibido es principalmente un script de
  importación/limpieza de productos, no el DDL completo que crea todas las tablas.
- Si faltan tablas, consulta database/estructura_punto6.sql (esquema de referencia; revisa antes
  de aplicarlo a una base que ya tenga información).

ABRIR EN NETBEANS
-----------------
1. Descomprime TiendaBonita_entrega_punto6.zip.
2. En NetBeans elige File > Open Project y selecciona la carpeta TiendaBonita.
3. Espera que Maven resuelva las dependencias del proyecto.
4. Inicia MySQL Server y verifica que la base de datos tienda_bonita exista.
5. La configuración local utilizada durante las pruebas fue localhost, puerto 3307, base de datos tienda_bonita.
6. Revisa la configuración de conexión en src/main/java/umg/DAO/ConexionBD.java. Ajusta el puerto, usuario y contraseña según la instalación local de MySQL. No compartas contraseñas reales en la documentación.
7. Ejecuta la clase app.Principal.TiendaBonita.
8. Inicia sesión con un usuario activo existente en la tabla usuario. El programa no crea automáticamente un usuario fijo.

CONFIGURAR CREDENCIALES (OPCIONAL)
La conexión a MySQL debe utilizar los datos válidos del equipo donde se ejecuta la aplicación.
Si se cambia el puerto, usuario o contraseña de MySQL, revisa la configuración de conexión en src/main/java/umg/DAO/ConexionBD.java y comprueba que la aplicación pueda conectarse correctamente.
No publiques contraseñas reales ni las incluyas en documentación compartida.

BASE DE DATOS Y RESPALDO

La base de datos utilizada por la aplicación se llama tienda_bonita.
El archivo database/estructura_punto6.sql contiene el esquema de referencia.
El archivo database/diagnostico_esquema.sql permite revisar la estructura requerida.
El archivo database/tienda_bonita_respaldo.sql contiene el respaldo de la base de datos y sus registros exportados.
Antes de restaurar un respaldo, realiza una copia de seguridad de cualquier base de datos existente. Verifica el contenido y el entorno antes de ejecutar scripts SQL.