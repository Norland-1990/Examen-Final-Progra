
package Model;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InventarioUpdater {

    public void actualizarInventario(JTable jTable4) {
        javax.swing.table.TableModel model = jTable4.getModel();

        try (Connection conexion = ConexionBD.getConnection()) {
            String sql = "UPDATE producto SET cantidadProducto = cantidadProducto - ? WHERE codigoProducto = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);

            for (int i = 0; i < model.getRowCount(); i++) {
                String codigoProducto = model.getValueAt(i, 0).toString();
                int cantidadVendida = Integer.parseInt(model.getValueAt(i, 2).toString());

                stmt.setInt(1, cantidadVendida);
                stmt.setString(2, codigoProducto);
                stmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(null, "Inventario actualizado correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar el inventario: " + e.getMessage());
        }
    }
}