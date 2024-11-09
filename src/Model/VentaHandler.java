
package Model;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VentaHandler {
    private JTextField codigoVenta;
    private JTextField nameVenta;
    private JTextField precioVenta;
    private JTextField disponibleVenta;
    private JTextField cantidadVenta;
    private JTable tablaVentas;
    
    // Constructor que recibe los componentes relevantes de la venta
    public VentaHandler(JTextField codigoVenta, JTextField nameVenta, JTextField precioVenta, 
                        JTextField disponibleVenta, JTextField cantidadVenta, JTable tablaVentas) {
        this.codigoVenta = codigoVenta;
        this.nameVenta = nameVenta;
        this.precioVenta = precioVenta;
        this.disponibleVenta = disponibleVenta;
        this.cantidadVenta = cantidadVenta;
        this.tablaVentas = tablaVentas;

        configurarListeners();
    }
    // Configura los listeners para los campos necesarios
    private void configurarListeners() {
        codigoVenta.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                validarYCargarProducto();
            }
        });

        cantidadVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    agregarVentaATabla();
                }
            }
        });
    }
    // Método para validar el código de producto y cargar los datos correspondientes
    private void validarYCargarProducto() {
        String codigo = codigoVenta.getText().trim();
        if (!codigo.matches("^VC\\d{3}$")) {
            JOptionPane.showMessageDialog(null, "Código inválido. Debe cumplir el formato VC000.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            limpiarCamposVenta(); // Limpia campos si el código es inválido
        } else {
            cargarDatosProducto(codigo);
        }
    }

    // Método para cargar datos del producto desde la base de datos
    private void cargarDatosProducto(String codigo) {
        String sql = "SELECT nombreProducto, precioUnitario, cantidadProducto FROM producto WHERE codigoProducto = ?";
        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameVenta.setText(rs.getString("nombreProducto"));
                precioVenta.setText(String.valueOf(rs.getDouble("precioUnitario")));
                disponibleVenta.setText(String.valueOf(rs.getInt("cantidadProducto")));
                
                // Hacer que los campos llenados no sean editables
                nameVenta.setEditable(false);
                precioVenta.setEditable(false);
                disponibleVenta.setEditable(false);
            } else {
                JOptionPane.showMessageDialog(null, "Código no encontrado en la base de datos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                limpiarCamposVenta(); // Limpia campos si no se encuentra el código
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al buscar el producto: " + e.getMessage());
        }
    }

    // Método para limpiar los campos de venta si el código es inválido o no encontrado
    private void limpiarCamposVenta() {
        nameVenta.setText("");
        precioVenta.setText("");
        disponibleVenta.setText("");
        nameVenta.setEditable(true);
        precioVenta.setEditable(true);
        disponibleVenta.setEditable(true);
    }
    
    // Método para limpiar manualmente los campos cuando se presiona el botón "cleanVenta"
    public void limpiarCamposVentaManual() {
        codigoVenta.setText("");
        nameVenta.setText("");
        precioVenta.setText("");
        disponibleVenta.setText("");
        cantidadVenta.setText("");

        nameVenta.setEditable(true);
        precioVenta.setEditable(true);
        disponibleVenta.setEditable(true);
    }

    // Método para agregar los datos de la venta a jTable4 y calcular el precio total
    private void agregarVentaATabla() {
        try {
            int cantidad = Integer.parseInt(cantidadVenta.getText().trim());
            double precioUnitario = Double.parseDouble(precioVenta.getText());
            double precioTotal = cantidad * precioUnitario;

            // Agregar los datos a la tabla de ventas
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tablaVentas.getModel();
            model.addRow(new Object[]{
                codigoVenta.getText(),  // Código del producto
                nameVenta.getText(),    // Nombre del producto
                cantidad,               // Cantidad
                precioUnitario,         // Precio unitario
                precioTotal             // Precio total
            });

            // Limpiar el campo de cantidad para otra entrada
            cantidadVenta.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Ingrese una cantidad válida.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
