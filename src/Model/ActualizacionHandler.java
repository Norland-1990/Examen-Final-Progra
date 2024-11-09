package Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.toedter.calendar.JDateChooser;
import Vista.MENU;

public class ActualizacionHandler {

    public void mostrarFormularioActualizacion(MENU menu, String codigoProducto) {
        // Crear el formulario de actualización
        JDialog dialog = new JDialog(menu, "Actualizar Producto", true);
        dialog.setLayout(new GridLayout(6, 2));

        JTextField codigoField = new JTextField(codigoProducto);
        JTextField nombreField = new JTextField();
        JTextField precioField = new JTextField();
        JTextField cantidadField = new JTextField();
        JDateChooser fechaVencimientoChooser = new JDateChooser();

        dialog.add(new JLabel("Código del Producto:"));
        dialog.add(codigoField);
        dialog.add(new JLabel("Nombre del Producto:"));
        dialog.add(nombreField);
        dialog.add(new JLabel("Precio Unitario:"));
        dialog.add(precioField);
        dialog.add(new JLabel("Cantidad del Producto:"));
        dialog.add(cantidadField);
        dialog.add(new JLabel("Fecha de Vencimiento:"));
        dialog.add(fechaVencimientoChooser);

        // Pre-cargar datos del producto seleccionado
        cargarDatosProducto(codigoProducto, nombreField, precioField, cantidadField, fechaVencimientoChooser);

        JButton guardarButton = new JButton("Guardar");
        JButton cancelarButton = new JButton("Cancelar");
        dialog.add(guardarButton);
        dialog.add(cancelarButton);

        // Acción de guardar
        guardarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try (Connection conexion = ConexionBD.getConnection()) {
                    String sql = "UPDATE producto SET nombreProducto = ?, precioUnitario = ?, cantidadProducto = ?, fechaVencimiento = ? WHERE codigoProducto = ?";
                    PreparedStatement stmt = conexion.prepareStatement(sql);
                    stmt.setString(1, nombreField.getText());
                    stmt.setDouble(2, Double.parseDouble(precioField.getText()));
                    stmt.setInt(3, Integer.parseInt(cantidadField.getText()));
                    stmt.setDate(4, new java.sql.Date(fechaVencimientoChooser.getDate().getTime()));
                    stmt.setString(5, codigoField.getText());

                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Producto actualizado exitosamente.");

                    // Refresca la tabla de inventario en MENU después de actualizar
                    menu.refrescarTablaInventario();
                    dialog.dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error al actualizar el producto: " + ex.getMessage());
                }
            }
        });

        // Acción de cancelar
        cancelarButton.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(menu);
        dialog.setVisible(true);
    }

    private void cargarDatosProducto(String codigoProducto, JTextField nombreField, JTextField precioField, JTextField cantidadField, JDateChooser fechaChooser) {
        try (Connection conexion = ConexionBD.getConnection()) {
            String sql = "SELECT nombreProducto, precioUnitario, cantidadProducto, fechaVencimiento FROM producto WHERE codigoProducto = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, codigoProducto);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nombreField.setText(rs.getString("nombreProducto"));
                precioField.setText(String.valueOf(rs.getDouble("precioUnitario")));
                cantidadField.setText(String.valueOf(rs.getInt("cantidadProducto")));
                fechaChooser.setDate(rs.getDate("fechaVencimiento"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar los datos del producto: " + e.getMessage());
        }
    }
}
