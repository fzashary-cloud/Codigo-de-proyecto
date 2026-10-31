package umg.DAO;

import modelo.Rol;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/** Autenticación contra la tabla usuario; no mantiene usuarios fijos en Java. */
public class UsuarioDAO {
    public Usuario autenticar(String nombreUsuario, String contrasena) throws SQLException {
        if (nombreUsuario == null || nombreUsuario.isBlank()
                || contrasena == null || contrasena.isEmpty()) {
            return null;
        }

        String sql = "SELECT id_usuario, nombre, usuario, contrasena, estado, id_rol "
                + "FROM usuario WHERE usuario = ? LIMIT 1";

        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String claveGuardada = rs.getString("contrasena");
                String estado = rs.getString("estado");
                if (claveGuardada == null || !claveGuardada.equals(contrasena)
                        || !estaActivo(estado)) {
                    return null;
                }

                int idUsuario = rs.getInt("id_usuario");
                String nombre = rs.getString("nombre");
                String usuario = rs.getString("usuario");
                int idRol = rs.getInt("id_rol");
                Rol rol = cargarRol(conexion, idRol);
                return new Usuario(idUsuario, nombre, usuario, claveGuardada, true, rol);
            }
        }
    }

    private Rol cargarRol(Connection conexion, int idRol) {
        String sql = "SELECT nombre, permisos FROM rol WHERE id_rol = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Rol(idRol, rs.getString("nombre"), rs.getString("permisos"));
                }
            }
        } catch (SQLException ignored) {
            // La autenticación se puede completar aunque la tabla rol no tenga permisos cargados.
        }
        return new Rol(idRol, "Rol " + idRol, "");
    }

    private boolean estaActivo(String valor) {
        if (valor == null) return false;
        String normalizado = valor.trim().toLowerCase(Locale.ROOT);
        return normalizado.equals("1") || normalizado.equals("true")
                || normalizado.equals("activo") || normalizado.equals("activa")
                || normalizado.equals("si") || normalizado.equals("sí")
                || normalizado.equals("yes");
    }
}
