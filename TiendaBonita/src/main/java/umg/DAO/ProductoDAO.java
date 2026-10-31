package umg.DAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos para inventario/productos. */
public class ProductoDAO {
    public record CategoriaOption(int idCategoria, String nombre) {
        @Override public String toString() { return nombre; }
    }

    public record ProductoDTO(
            int idProducto,
            String codigo,
            String nombre,
            String marca,
            String calidad,
            String tonoColor,
            String presentacion,
            BigDecimal precio,
            int existencia,
            int stockMinimo,
            LocalDate fechaVencimiento,
            int idCategoria,
            String categoria,
            Integer idProveedor) {
        @Override public String toString() { return codigo + " - " + nombre; }
    }

    public List<ProductoDTO> listarProductos() throws SQLException {
        String sql = "SELECT p.id_producto, p.codigo, p.nombre, p.marca, p.calidad, "
                + "p.tono_color, p.presentacion, p.precio, p.existencia, p.stock_minimo, "
                + "p.fecha_vencimiento, p.id_categoria, COALESCE(c.nombre, 'Sin categoría') AS categoria, "
                + "p.id_proveedor FROM producto p LEFT JOIN categoria c ON c.id_categoria = p.id_categoria "
                + "ORDER BY p.nombre";
        List<ProductoDTO> productos = new ArrayList<>();
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) productos.add(mapear(rs));
        }
        return productos;
    }

    public List<CategoriaOption> listarCategorias() throws SQLException {
        List<CategoriaOption> categorias = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre FROM categoria ORDER BY nombre";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) categorias.add(new CategoriaOption(
                    rs.getInt("id_categoria"), rs.getString("nombre")));
        }
        return categorias;
    }

    public ProductoDTO buscarProducto(String codigo) throws SQLException {
        if (codigo == null || codigo.isBlank()) return null;
        String sql = "SELECT p.id_producto, p.codigo, p.nombre, p.marca, p.calidad, "
                + "p.tono_color, p.presentacion, p.precio, p.existencia, p.stock_minimo, "
                + "p.fecha_vencimiento, p.id_categoria, COALESCE(c.nombre, 'Sin categoría') AS categoria, "
                + "p.id_proveedor FROM producto p LEFT JOIN categoria c ON c.id_categoria = p.id_categoria "
                + "WHERE p.codigo = ? LIMIT 1";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, codigo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean guardarProducto(String codigo, String nombre, String marca,
            String calidad, String tonoColor, String presentacion,
            double precio, int existencia, int stockMinimo,
            Integer idCategoria, Integer idProveedor) throws SQLException {
        validar(codigo, nombre, precio, existencia, stockMinimo, idCategoria);
        String sql = "INSERT INTO producto (codigo, nombre, marca, calidad, tono_color, "
                + "presentacion, precio, existencia, stock_minimo, id_categoria, id_proveedor) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, codigo.trim());
            ps.setString(2, nombre.trim());
            ps.setString(3, texto(marca));
            ps.setString(4, texto(calidad));
            ps.setString(5, texto(tonoColor));
            ps.setString(6, texto(presentacion));
            ps.setBigDecimal(7, BigDecimal.valueOf(precio));
            ps.setInt(8, existencia);
            ps.setInt(9, stockMinimo);
            ps.setInt(10, idCategoria);
            setNullableInt(ps, 11, idProveedor);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean actualizarProducto(String codigo, String nombre, String marca,
            String calidad, String tonoColor, String presentacion,
            double precio, int existencia, int stockMinimo,
            Integer idCategoria, Integer idProveedor) throws SQLException {
        validar(codigo, nombre, precio, existencia, stockMinimo, idCategoria);
        String sql = "UPDATE producto SET nombre = ?, marca = ?, calidad = ?, tono_color = ?, "
                + "presentacion = ?, precio = ?, existencia = ?, stock_minimo = ?, id_categoria = ?, "
                + "id_proveedor = COALESCE(?, id_proveedor) WHERE codigo = ?";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setString(2, texto(marca));
            ps.setString(3, texto(calidad));
            ps.setString(4, texto(tonoColor));
            ps.setString(5, texto(presentacion));
            ps.setBigDecimal(6, BigDecimal.valueOf(precio));
            ps.setInt(7, existencia);
            ps.setInt(8, stockMinimo);
            ps.setInt(9, idCategoria);
            setNullableInt(ps, 10, idProveedor);
            ps.setString(11, codigo.trim());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminarProducto(String codigo) throws SQLException {
        String sql = "DELETE FROM producto WHERE codigo = ?";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, codigo.trim());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarExistencia(String codigo, int nuevaExistencia) throws SQLException {
        if (nuevaExistencia < 0) throw new IllegalArgumentException("La existencia no puede ser negativa.");
        String sql = "UPDATE producto SET existencia = ? WHERE codigo = ?";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, nuevaExistencia);
            ps.setString(2, codigo.trim());
            return ps.executeUpdate() > 0;
        }
    }

    /** Conserva la salida de consola útil para pruebas manuales. */
    public void imprimirProductos() {
        try {
            System.out.println("PRODUCTOS DE TIENDA BONITA");
            for (ProductoDTO p : listarProductos()) {
                System.out.printf("%s | %s | %s | Q%s | Existencia: %d%n",
                        p.codigo(), p.nombre(), p.marca(), p.precio(), p.existencia());
            }
        } catch (SQLException e) {
            System.err.println("Error al leer productos: " + e.getMessage());
        }
    }

    private ProductoDTO mapear(ResultSet rs) throws SQLException {
        int idProveedorLeido = rs.getInt("id_proveedor");
        Integer idProveedor = rs.wasNull() ? null : idProveedorLeido;
        return new ProductoDTO(
                rs.getInt("id_producto"), rs.getString("codigo"), rs.getString("nombre"),
                rs.getString("marca"), rs.getString("calidad"), rs.getString("tono_color"),
                rs.getString("presentacion"), rs.getBigDecimal("precio"),
                rs.getInt("existencia"), rs.getInt("stock_minimo"), leerFecha(rs),
                rs.getInt("id_categoria"), rs.getString("categoria"), idProveedor);
    }

    private LocalDate leerFecha(ResultSet rs) throws SQLException {
        Date fecha = rs.getDate("fecha_vencimiento");
        return fecha == null ? null : fecha.toLocalDate();
    }

    private void validar(String codigo, String nombre, double precio, int existencia,
            int stockMinimo, Integer idCategoria) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (!Double.isFinite(precio) || precio <= 0) throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        if (existencia < 0) throw new IllegalArgumentException("La existencia no puede ser negativa.");
        if (stockMinimo < 0) throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        if (idCategoria == null || idCategoria <= 0) throw new IllegalArgumentException("Selecciona una categoría.");
    }

    private String texto(String valor) { return valor == null ? "" : valor.trim(); }

    private void setNullableInt(PreparedStatement ps, int indice, Integer valor) throws SQLException {
        if (valor == null) ps.setNull(indice, Types.INTEGER);
        else ps.setInt(indice, valor);
    }
}
