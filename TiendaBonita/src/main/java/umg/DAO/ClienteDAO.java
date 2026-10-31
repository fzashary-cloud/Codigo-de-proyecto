package umg.DAO;

import modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** CRUD para clientes. */
public class ClienteDAO {
    public List<Cliente> listarClientes() throws SQLException {
        String sql = "SELECT id_cliente, nombre, nit, telefono, correo, direccion FROM cliente ORDER BY nombre";
        List<Cliente> clientes = new ArrayList<>();
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) clientes.add(mapear(rs));
        }
        return clientes;
    }

    public List<Cliente> buscarClientes(String criterio) throws SQLException {
        String sql = "SELECT id_cliente, nombre, nit, telefono, correo, direccion FROM cliente "
                + "WHERE nombre LIKE ? OR nit LIKE ? ORDER BY nombre";
        List<Cliente> clientes = new ArrayList<>();
        String filtro = "%" + (criterio == null ? "" : criterio.trim()) + "%";
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) clientes.add(mapear(rs));
            }
        }
        return clientes;
    }

    public boolean guardarCliente(Cliente cliente) throws SQLException {
        validar(cliente);
        String sql = "INSERT INTO cliente (nombre, nit, telefono, correo, direccion) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            asignarCampos(ps, cliente, false);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean actualizarCliente(Cliente cliente) throws SQLException {
        validar(cliente);
        if (cliente.getIdCliente() <= 0) throw new IllegalArgumentException("Selecciona un cliente de la tabla.");
        String sql = "UPDATE cliente SET nombre = ?, nit = ?, telefono = ?, correo = ?, direccion = ? WHERE id_cliente = ?";
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            asignarCampos(ps, cliente, true);
            ps.setInt(6, cliente.getIdCliente());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminarCliente(int idCliente) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id_cliente = ?";
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            return ps.executeUpdate() > 0;
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id_cliente"), rs.getString("nombre"),
                rs.getString("nit"), rs.getString("telefono"), rs.getString("correo"),
                rs.getString("direccion"));
    }

    private void asignarCampos(PreparedStatement ps, Cliente cliente, boolean actualizacion) throws SQLException {
        ps.setString(1, normalizar(cliente.getNombre()));
        ps.setString(2, normalizar(cliente.getNit()));
        ps.setString(3, normalizar(cliente.getTelefono()));
        ps.setString(4, normalizar(cliente.getCorreo()));
        ps.setString(5, normalizar(cliente.getDireccion()));
    }

    private void validar(Cliente cliente) {
        if (cliente == null || cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
    }

    private String normalizar(String s) { return s == null ? "" : s.trim(); }
}
