package umg.DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Registra venta y detalle en una sola transacción y descuenta inventario en MySQL. */
public class VentaDAO {
    public record DetalleVentaInput(int idProducto, int cantidad) { }
    public record VentaRegistrada(int idVenta, BigDecimal total) { }
    private record ProductoBloqueado(String codigo, String nombre, BigDecimal precio,
                                     int existencia, LocalDate fechaVencimiento) { }

    public VentaRegistrada confirmarVenta(int idCliente, int idUsuario, LocalDate fecha,
                                          List<DetalleVentaInput> detalles) throws SQLException {
        if (idCliente <= 0) throw new IllegalArgumentException("Selecciona un cliente.");
        if (idUsuario <= 0) throw new IllegalArgumentException("No se pudo identificar al vendedor. Cierra sesión e ingresa nuevamente.");
        if (fecha == null) throw new IllegalArgumentException("La fecha de venta es obligatoria.");
        if (detalles == null || detalles.isEmpty()) throw new IllegalArgumentException("Agrega al menos un producto a la venta.");
        for (DetalleVentaInput d : detalles) {
            if (d.idProducto() <= 0 || d.cantidad() <= 0) throw new IllegalArgumentException("Todas las cantidades deben ser mayores que cero.");
        }

        try (Connection c = ConexionBD.abrir()) {
            boolean autoCommitAnterior = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                Map<Integer, ProductoBloqueado> productos = new HashMap<>();
                BigDecimal total = BigDecimal.ZERO;
                for (DetalleVentaInput d : detalles) {
                    ProductoBloqueado p = productos.get(d.idProducto());
                    if (p == null) {
                        p = bloquearProducto(c, d.idProducto());
                        productos.put(d.idProducto(), p);
                    }
                    int solicitadoAcumulado = detalles.stream()
                            .filter(x -> x.idProducto() == d.idProducto())
                            .mapToInt(DetalleVentaInput::cantidad).sum();
                    if (p.existencia() < solicitadoAcumulado) {
                        throw new SQLException("Existencia insuficiente para " + p.nombre()
                                + ". Disponible: " + p.existencia() + ".");
                    }
                    if (p.fechaVencimiento() != null && p.fechaVencimiento().isBefore(fecha)) {
                        throw new SQLException("No se puede vender el producto vencido: " + p.nombre() + ".");
                    }
                }
                for (DetalleVentaInput d : detalles) {
                    ProductoBloqueado p = productos.get(d.idProducto());
                    total = total.add(p.precio().multiply(BigDecimal.valueOf(d.cantidad())));
                }

                int idVenta;
                String sqlVenta = "INSERT INTO venta (fecha, id_cliente, id_usuario, total, estado) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(sqlVenta, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setDate(1, Date.valueOf(fecha));
                    ps.setInt(2, idCliente);
                    ps.setInt(3, idUsuario);
                    ps.setBigDecimal(4, total);
                    ps.setString(5, "Confirmada");
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("MySQL no devolvió el número de venta.");
                        idVenta = keys.getInt(1);
                    }
                }

                String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio, subtotal) VALUES (?, ?, ?, ?, ?)";
                String sqlStock = "UPDATE producto SET existencia = existencia - ? WHERE id_producto = ? AND existencia >= ?";
                try (PreparedStatement psDetalle = c.prepareStatement(sqlDetalle);
                     PreparedStatement psStock = c.prepareStatement(sqlStock)) {
                    for (DetalleVentaInput d : detalles) {
                        ProductoBloqueado p = productos.get(d.idProducto());
                        BigDecimal subtotal = p.precio().multiply(BigDecimal.valueOf(d.cantidad()));
                        psDetalle.setInt(1, idVenta);
                        psDetalle.setInt(2, d.idProducto());
                        psDetalle.setInt(3, d.cantidad());
                        psDetalle.setBigDecimal(4, p.precio());
                        psDetalle.setBigDecimal(5, subtotal);
                        psDetalle.executeUpdate();

                        psStock.setInt(1, d.cantidad());
                        psStock.setInt(2, d.idProducto());
                        psStock.setInt(3, d.cantidad());
                        if (psStock.executeUpdate() != 1) {
                            throw new SQLException("No se pudo actualizar el inventario de " + p.nombre() + ". Intenta nuevamente.");
                        }
                    }
                }
                c.commit();
                return new VentaRegistrada(idVenta, total);
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                throw e;
            } finally {
                try { c.setAutoCommit(autoCommitAnterior); } catch (SQLException ignored) { }
            }
        }
    }

    private ProductoBloqueado bloquearProducto(Connection c, int idProducto) throws SQLException {
        String sql = "SELECT codigo, nombre, precio, existencia, fecha_vencimiento FROM producto WHERE id_producto = ? FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Uno de los productos seleccionados ya no existe.");
                Date fecha = rs.getDate("fecha_vencimiento");
                return new ProductoBloqueado(rs.getString("codigo"), rs.getString("nombre"),
                        rs.getBigDecimal("precio"), rs.getInt("existencia"),
                        fecha == null ? null : fecha.toLocalDate());
            }
        }
    }
}
