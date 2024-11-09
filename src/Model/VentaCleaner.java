
package Model;
import javax.swing.*;

public class VentaCleaner {

    public void limpiarCamposVenta(JTextField nitCliente, JTextField nameCliente, JTextField codigoVenta, JTextField nameVenta, JTextField cantidadVenta, JTextField precioVenta, JTextField disponibleVenta, JLabel precioPrint, JTable jTable4) {
        nitCliente.setText("");
        nameCliente.setText("");
        codigoVenta.setText("");
        nameVenta.setText("");
        cantidadVenta.setText("");
        precioVenta.setText("");
        disponibleVenta.setText("");
        precioPrint.setText("0.00");

        // Limpia todas las filas de jTable4
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable4.getModel();
        model.setRowCount(0);
    }
}