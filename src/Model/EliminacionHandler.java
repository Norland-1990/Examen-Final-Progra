package Model;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import Vista.MENU;

public class EliminacionHandler {

    public void eliminarProducto(MENU menu, String codigoProducto) {
        int confirmacion = JOptionPane.showConfirmDialog(menu, "¿Está seguro de que desea eliminar este producto?", "Confirmación de eliminación", JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            try (Connection conexion = ConexionBD.getConnection()) {
                String sql = "DELETE FROM producto WHERE codigoProducto = ?";
                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setString(1, codigoProducto);

                stmt.executeUpdate();
                JOptionPane.showMessageDialog(menu, "Producto eliminado exitosamente.");

                // Refresca la tabla de inventario en MENU después de eliminar
                menu.refrescarTablaInventario();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(menu, "Error al eliminar el producto: " + e.getMessage());
            }
        }
    }
}
