package umg.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Centraliza la configuración de conexión de Tienda Bonita. */
public final class ConexionBD {
    private static final String URL_PREDETERMINADA =
            "jdbc:mysql://localhost:3307/tienda_bonita?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Guatemala";
    private static final String USUARIO_PREDETERMINADO = "root";
    // Para no tener que editar el código, pueden configurarse estas propiedades al ejecutar:
    // -Dtienda.db.url=... -Dtienda.db.user=... -Dtienda.db.password=...
    private static final String CLAVE_PREDETERMINADA = "TiendaBonita#2026!";

    private ConexionBD() { }

    public static Connection abrir() throws SQLException {
        String url = System.getProperty("tienda.db.url", URL_PREDETERMINADA);
        String usuario = System.getProperty("tienda.db.user", USUARIO_PREDETERMINADO);
        String clave = System.getProperty("tienda.db.password", CLAVE_PREDETERMINADA);
        return DriverManager.getConnection(url, usuario, clave);
    }
}
