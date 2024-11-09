package Model;

import javax.swing.*;
import Vista.MENU;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistroHandler {

    public void mostrarFormularioRegistro(MENU menu) {
        // Crear el panel para el formulario de ingreso de nuevo producto
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Campos de texto para ingresar el nuevo producto
        JTextField codigoProductoField = new JTextField(10);
        JTextField nombreProductoField = new JTextField(10);
        JTextField precioUnitarioField = new JTextField(10);
        JTextField cantidadProductoField = new JTextField(10);
        JTextField fechaVencimientoField = new JTextField(10); // Puedes usar JDateChooser si prefieres
        
        // Agregar los campos al panel
        panel.add(new JLabel("Código del Producto:"));
        panel.add(codigoProductoField);
        panel.add(new JLabel("Nombre del Producto:"));
        panel.add(nombreProductoField);
        panel.add(new JLabel("Precio Unitario:"));
        panel.add(precioUnitarioField);
        panel.add(new JLabel("Cantidad del Producto:"));
        panel.add(cantidadProductoField);
        panel.add(new JLabel("Fecha de Vencimiento (YYYY-MM-DD):"));
        panel.add(fechaVencimientoField);

        // Crear opciones de botones para la ventana emergente
        int result = JOptionPane.showOptionDialog(
            menu, 
            panel, 
            "Ingresar Nuevo Producto", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE, 
            null, 
            new String[]{"Guardar", "Cancelar"}, 
            "Guardar"
        );

        // Si el usuario elige "Guardar", realiza la acción de guardar en la base de datos
        if (result == JOptionPane.OK_OPTION) {
            String codigoProducto = codigoProductoField.getText();
            String nombreProducto = nombreProductoField.getText();
            String precioUnitario = precioUnitarioField.getText();
            String cantidadProducto = cantidadProductoField.getText();
            String fechaVencimiento = fechaVencimientoField.getText();

            // Validación simple de los datos ingresados
            if (codigoProducto.isEmpty() || nombreProducto.isEmpty() || precioUnitario.isEmpty() || cantidadProducto.isEmpty() || fechaVencimiento.isEmpty()) {
                JOptionPane.showMessageDialog(menu, "Por favor, complete todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Llamar a un método para insertar estos datos en la base de datos y refrescar la tabla
            guardarProductoEnBaseDatos(menu, codigoProducto, nombreProducto, precioUnitario, cantidadProducto, fechaVencimiento);
        }
    }

    // Método para guardar el producto en la base de datos
    private void guardarProductoEnBaseDatos(MENU menu, String codigoProducto, String nombreProducto, String precioUnitario, String cantidadProducto, String fechaVencimiento) {
        try (Connection conexion = ConexionBD.getConnection()) {
            String sql = "INSERT INTO producto (codigoProducto, nombreProducto, precioUnitario, cantidadProducto, fechaVencimiento) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
                stmt.setString(1, codigoProducto);
                stmt.setString(2, nombreProducto);
                stmt.setDouble(3, Double.parseDouble(precioUnitario));
                stmt.setInt(4, Integer.parseInt(cantidadProducto));
                stmt.setString(5, fechaVencimiento);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Producto ingresado exitosamente.");

                // Llama al método para refrescar la tabla en MENU después de guardar el producto
                menu.refrescarTablaInventario();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al guardar el producto: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Formato de precio o cantidad inválido: " + e.getMessage());
        }
    }
}