
package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    // Método para obtener la conexión a la base de datos
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Valores según la configuración de MySQL
            String url = "jdbc:mysql://localhost:3306/bdvalvecorporation";  // URL de la base de datos
            String usuario = "root";  // Usuario de MySQL
            String contraseña = "dinosaurio0214";  // Contraseña de MySQL
            
            // Conexión con MySQL
            connection = DriverManager.getConnection(url, usuario, contraseña);
            System.out.println("Conexión exitosa a la base de datos.");
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos.");
            e.printStackTrace();
        }
        return connection;
    }
    public static void main(String[] args) {

    }
}