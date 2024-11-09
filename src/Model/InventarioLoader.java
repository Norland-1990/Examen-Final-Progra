
package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class InventarioLoader {

    // Método para cargar los datos de inventario en la tabla proporcionada
    public void cargarInventario(JTable tablaInventario) {
        // Definir el modelo de tabla y bloquear edición de celdas
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Código", "Nombre del producto", "Precio unitario", "Cantidad de producto", "Fecha de vencimiento"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Bloquea la edición de las celdas
            }
        };

        // Establecer el modelo en la tabla
        tablaInventario.setModel(model);

        // Consulta SQL para obtener los datos del inventario
        String sql = "SELECT codigoProducto, nombreProducto, precioUnitario, cantidadProducto, fechaVencimiento FROM producto";

        // Conectar con la base de datos y cargar los datos
        try (Connection conexion = ConexionBD.getConnection(); 
             PreparedStatement stmt = conexion.prepareStatement(sql); 
             ResultSet rs = stmt.executeQuery()) {

            if (conexion == null) {
                JOptionPane.showMessageDialog(null, "Error al conectar con la base de datos.");
                return;
            }

            // Rellenar el modelo de la tabla con los datos obtenidos
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("codigoProducto"),
                    rs.getString("nombreProducto"),
                    rs.getDouble("precioUnitario"),
                    rs.getInt("cantidadProducto"),
                    rs.getDate("fechaVencimiento")
                    
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar el inventario: " + e.getMessage());
        }
    }
}