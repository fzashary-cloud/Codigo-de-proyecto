package umg.DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Consultas disponibles en el módulo de reportes. */
public class ReporteDAO {
    public record Resultado(String[] columnas, List<Object[]> filas) { }

    public Resultado consultar(String tipo, LocalDate desde, LocalDate hasta) throws SQLException {
        String sql;
        boolean usaFechas = false;
        switch (tipo) {
            case "Todos los productos" -> sql = "SELECT p.codigo AS Código, p.nombre AS Producto, p.marca AS Marca, "
                    + "c.nombre AS Categoría, p.precio AS Precio, p.existencia AS Existencia, p.stock_minimo AS `Stock mínimo` "
                    + "FROM producto p LEFT JOIN categoria c ON c.id_categoria = p.id_categoria ORDER BY p.nombre";
            case "Productos con stock bajo" -> sql = "SELECT p.codigo AS Código, p.nombre AS Producto, p.existencia AS Existencia, "
                    + "p.stock_minimo AS `Stock mínimo`, c.nombre AS Categoría FROM producto p "
                    + "LEFT JOIN categoria c ON c.id_categoria = p.id_categoria WHERE p.existencia <= p.stock_minimo ORDER BY p.existencia";
            case "Productos por categoría" -> sql = "SELECT c.nombre AS Categoría, p.codigo AS Código, p.nombre AS Producto, "
                    + "p.precio AS Precio, p.existencia AS Existencia FROM producto p "
                    + "LEFT JOIN categoria c ON c.id_categoria = p.id_categoria ORDER BY c.nombre, p.nombre";
            case "Productos vencidos" -> sql = "SELECT p.codigo AS Código, p.nombre AS Producto, p.fecha_vencimiento AS `Fecha de vencimiento`, "
                    + "p.existencia AS Existencia FROM producto p WHERE p.fecha_vencimiento IS NOT NULL "
                    + "AND p.fecha_vencimiento < CURDATE() ORDER BY p.fecha_vencimiento";
            case "Ventas realizadas" -> sql = "SELECT v.id_venta AS `No. venta`, v.fecha AS Fecha, c.nombre AS Cliente, "
                    + "u.nombre AS Vendedor, v.total AS Total, v.estado AS Estado FROM venta v "
                    + "LEFT JOIN cliente c ON c.id_cliente = v.id_cliente "
                    + "LEFT JOIN usuario u ON u.id_usuario = v.id_usuario ORDER BY v.fecha DESC, v.id_venta DESC";
            case "Ventas por fecha" -> {
                sql = "SELECT v.id_venta AS `No. venta`, v.fecha AS Fecha, c.nombre AS Cliente, "
                        + "u.nombre AS Vendedor, v.total AS Total, v.estado AS Estado FROM venta v "
                        + "LEFT JOIN cliente c ON c.id_cliente = v.id_cliente "
                        + "LEFT JOIN usuario u ON u.id_usuario = v.id_usuario "
                        + "WHERE v.fecha BETWEEN ? AND ? ORDER BY v.fecha DESC, v.id_venta DESC";
                usaFechas = true;
            }
            case "Clientes registrados" -> sql = "SELECT id_cliente AS ID, nombre AS Nombre, nit AS NIT, "
                    + "telefono AS Teléfono, correo AS Correo, direccion AS Dirección FROM cliente ORDER BY nombre";
            default -> throw new IllegalArgumentException("Selecciona un tipo de reporte válido.");
        }

        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (usaFechas) {
                LocalDate inicio = desde == null ? LocalDate.now().withDayOfMonth(1) : desde;
                LocalDate fin = hasta == null ? LocalDate.now() : hasta;
                if (inicio.isAfter(fin)) throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
                ps.setDate(1, Date.valueOf(inicio));
                ps.setDate(2, Date.valueOf(fin));
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int cantidadColumnas = md.getColumnCount();
                String[] columnas = new String[cantidadColumnas];
                for (int i = 0; i < cantidadColumnas; i++) columnas[i] = md.getColumnLabel(i + 1);
                List<Object[]> filas = new ArrayList<>();
                while (rs.next()) {
                    Object[] fila = new Object[cantidadColumnas];
                    for (int i = 0; i < cantidadColumnas; i++) fila[i] = rs.getObject(i + 1);
                    filas.add(fila);
                }
                return new Resultado(columnas, filas);
            }
        }
    }
}
