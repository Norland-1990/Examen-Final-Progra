package Model;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import Vista.MENU;

public class FacturaPDFGenerator {

    private MENU menu;

    // Constructor para recibir la instancia de MENU
    public FacturaPDFGenerator(MENU menu) {
        this.menu = menu;
    }

    public void generarFactura(String nitCliente, String nombreCliente, String precioFinal, JTable jTable4) {
        String pdfPath = "src/pdfDate/factura_" + nitCliente + ".pdf";
        File file = new File(pdfPath);
        file.getParentFile().mkdirs();

        try {
            PdfWriter writer = new PdfWriter(pdfPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Factura de Venta"));
            document.add(new Paragraph("NIT Cliente: " + nitCliente));
            document.add(new Paragraph("Nombre Cliente: " + nombreCliente));

            Table table = new Table(new float[]{2, 2, 2, 2});
            table.addCell(new Cell().add(new Paragraph("Producto")));
            table.addCell(new Cell().add(new Paragraph("Cantidad")));
            table.addCell(new Cell().add(new Paragraph("Precio Unitario")));
            table.addCell(new Cell().add(new Paragraph("Precio Total")));

            javax.swing.table.TableModel model = jTable4.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                table.addCell(new Cell().add(new Paragraph(model.getValueAt(i, 1).toString())));
                table.addCell(new Cell().add(new Paragraph(model.getValueAt(i, 2).toString())));
                table.addCell(new Cell().add(new Paragraph(model.getValueAt(i, 3).toString())));
                table.addCell(new Cell().add(new Paragraph(model.getValueAt(i, 4).toString())));

                // Actualizar la cantidad de producto en la base de datos
                String codigoProducto = model.getValueAt(i, 0).toString();
                int cantidadVendida = Integer.parseInt(model.getValueAt(i, 2).toString());
                actualizarCantidadProducto(codigoProducto, cantidadVendida);
            }

            document.add(table);
            document.add(new Paragraph("Precio Final: Q" + precioFinal));
            document.close();
            JOptionPane.showMessageDialog(null, "Factura generada con éxito en: " + pdfPath);

            // Refrescar el inventario después de la actualización
            menu.refrescarTablaInventario();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al generar el PDF: " + e.getMessage());
        }
    }

    private void actualizarCantidadProducto(String codigoProducto, int cantidadVendida) {
        String sql = "UPDATE producto SET cantidadProducto = cantidadProducto - ? WHERE codigoProducto = ?";
        try (Connection conexion = ConexionBD.getConnection();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {

            stmt.setInt(1, cantidadVendida);
            stmt.setString(2, codigoProducto);
            stmt.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar la cantidad de producto: " + e.getMessage());
        }
    }
}
