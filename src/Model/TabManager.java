
package Model;

import javax.swing.JButton;
import javax.swing.JTabbedPane;

public class TabManager {

    private final JTabbedPane tabbedPane;

    public TabManager(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        ocultarPestanas();
    }

    // Método para ocultar todas las pestañas del JTabbedPane
    private void ocultarPestanas() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setEnabledAt(i, false);
        }
    }

    // Método para mostrar una pestaña específica basada en el índice
    public void mostrarPestana(int index) {
        ocultarPestanas();
        tabbedPane.setEnabledAt(index, true);
        tabbedPane.setSelectedIndex(index);
    }

    //Método para configurar el comportamiento de los botones
    public void configurarBotones(JButton consultaButton, JButton inventarioButton, JButton ingresoDatosButton, JButton facturacionButton ) {
        consultaButton.addActionListener(e -> mostrarPestana(0));     // Pestaña de Consulta
        inventarioButton.addActionListener(e -> mostrarPestana(1));   // Pestaña de Inventario
        ingresoDatosButton.addActionListener (e -> mostrarPestana(2)); // Pestaña de Ingreso de datos
        facturacionButton.addActionListener(e -> mostrarPestana(3));  // Pestaña de Factura
        
        
    }
}