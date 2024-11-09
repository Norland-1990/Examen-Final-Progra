
package Vista;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.util.regex.Pattern;
import java.net.URL;

import Model.TabManager;
import Model.InventarioLoader;

import Model.ActualizacionHandler;
import Model.RegistroHandler;
import Model.EliminacionHandler;

import Model.VentaHandler;
import com.toedter.calendar.JDateChooser;

import Model.FacturaPDFGenerator;
import Model.InventarioUpdater;
import Model.VentaCleaner;

public class MENU extends javax.swing.JFrame {
    
    private TabManager tabManager;
    private InventarioLoader inventarioLoader;
    
    private RegistroHandler registroHandler = new RegistroHandler();
    private ActualizacionHandler actualizacionHandler = new ActualizacionHandler();
    private EliminacionHandler eliminacionHandler = new EliminacionHandler();
    
    private VentaHandler ventaHandler;
    private FacturaPDFGenerator facturaPDFGenerator;
    private InventarioUpdater inventarioUpdater;
    private VentaCleaner ventaCleaner;
    
    
    public MENU() {
        initComponents();
        redimensionarImagenTitulo();
        redimensionarImagenLogo();
        configurarPlaceholders();
        configurarPlaceholders2();
        inicializarTabla();
        inicializarTabManager();
        cargarInventario();
        setLocationRelativeTo(null); // Centra la ventana al iniciar        
        configurarTablaVentas();
        configurarListenerTablaVentas();
       
        // Cambiar el título de la ventana
        setTitle("Menú Principal");

        // Establecer el ícono de la ventana (NO se redimensiona, es el ícono)
        setIconImage(new ImageIcon(getClass().getResource("/Img/Valve_logo.png")).getImage());
        
        facturaPDFGenerator = new FacturaPDFGenerator(this);
        inventarioUpdater = new InventarioUpdater();
        ventaCleaner = new VentaCleaner();
        
        // Inicializar handlers
        registroHandler = new RegistroHandler();
        actualizacionHandler = new ActualizacionHandler();
        eliminacionHandler = new EliminacionHandler();
        
        ventaHandler = new VentaHandler(CodigoVenta, NameVenta, PrecioVenta, DisponibleVenta, CantidadVenta, jTable4);
        
        
        cleanVenta.addActionListener(evt -> ventaHandler.limpiarCamposVentaManual());
        
        // Configurar botones
        configurarBotones();
    }
    
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
    private void configurarListenerTablaVentas() {
        jTable4.getModel().addTableModelListener(e -> actualizarPrecioTotal());
    }    
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
    // Método para configurar el JTable4 como no editable
    private void configurarTablaVentas() {
    javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
        new Object[][]{},
        new String[]{"CÓDIGO", "NOMBRE DE PRODUCTO", "Cantidad", "PRECIO UNITARIO", "PRECIO TOTAL"}
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Hace que todas las celdas no sean editables
        }
    };

    jTable4.setModel(model);
    }
    
    private void agregarFilaVenta(Object[] filaDatos) {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable4.getModel();
        model.addRow(filaDatos);
        actualizarPrecioTotal(); // Llama a la suma después de agregar la fila
    }
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
    // Metodo para imprimir precio en el Jleable
    private void actualizarPrecioTotal() {
        double total = 0.0;
        javax.swing.table.TableModel model = jTable4.getModel();

    for (int i = 0; i < model.getRowCount(); i++) {
        Object precioTotalObj = model.getValueAt(i, 4); // Columna "Precio Total"
        
        if (precioTotalObj != null) {
            try {
                double precioTotal = Double.parseDouble(precioTotalObj.toString());
                total += precioTotal;
            } catch (NumberFormatException e) {
                System.out.println("Error al convertir el valor: " + precioTotalObj);
                JOptionPane.showMessageDialog(this, "Error al convertir el valor de la tabla a número: " + e.getMessage());
            }
        } else {
            System.out.println("Valor nulo en la fila " + i);
        }
    }

        // Formatear el total a dos decimales y actualizar el JLabel
        PrecioPrint.setText(String.format("%.2f", total));
        System.out.println("Total actualizado: " + total);
    }
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------      
    // Configura los botones de acción después de inicializar los handlers
    private void configurarBotones() {
        // Desactivar botones actualizar y eliminar hasta que se seleccione una fila
        actualizarRegistro.setEnabled(false);
        eliminarRegistro.setEnabled(false);

        // Mantener el botón de ingreso habilitado siempre
        ingresoRegistro.setEnabled(true);

        // Agregar el listener de selección para habilitar/deshabilitar botones de actualizar y eliminar
        jTable3.getSelectionModel().addListSelectionListener(event -> {
            boolean seleccionado = jTable3.getSelectedRow() != -1;
            actualizarRegistro.setEnabled(seleccionado);
            eliminarRegistro.setEnabled(seleccionado);
        });

        // Acción para el botón ingresoRegistro
        ingresoRegistro.addActionListener(evt -> registroHandler.mostrarFormularioRegistro(this));

        // Acción para el botón actualizarRegistro
        actualizarRegistro.addActionListener(evt -> {
            int selectedRow = jTable3.getSelectedRow();
            if (selectedRow != -1) {
                String codigoProducto = jTable3.getValueAt(selectedRow, 0).toString();
                actualizacionHandler.mostrarFormularioActualizacion(this, codigoProducto);
            }
        });

        // Acción para el botón eliminarRegistro
        eliminarRegistro.addActionListener(evt -> {
            int selectedRow = jTable3.getSelectedRow();
            if (selectedRow != -1) {
                String codigoProducto = jTable3.getValueAt(selectedRow, 0).toString();
                eliminacionHandler.eliminarProducto(this, codigoProducto);
            }
        });
    }
    
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------        
    // Método para refrescar la tabla de inventario (jTable2)
    public void refrescarTablaInventario() {
        // Limpia las filas actuales de jTable2
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);
    
        // Recarga los datos en jTable2 usando el InventarioLoader
        inventarioLoader.cargarInventario(jTable2);
    }
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------       
    // Método para cargar el inventario en jTable2 que esta dentro del paquete Model
    private void cargarInventario() {
        inventarioLoader = new InventarioLoader();
        inventarioLoader.cargarInventario(jTable2); // Cargar datos en la tabla de inventario
    }    
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------       
    // Método para inicializar y configurar el TabManager que esta dentro del paquete Model
    private void inicializarTabManager() {
        tabManager = new TabManager(jTabbedPane1);
        tabManager.configurarBotones (Consulta, Inventario, ingresoDatos, Facturacion);
    }   
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------   
    // Método para redimensionar la imagen de título y asignarla a jLabel1
    private void redimensionarImagenTitulo() {
        ImageIcon iconoOriginal = new ImageIcon(getClass().getResource("/Img/Titulo.png"));
        java.awt.Image imagenRedimensionada = iconoOriginal.getImage().getScaledInstance(850, 175, java.awt.Image.SCALE_SMOOTH);
        ImageIcon iconoRedimensionado = new ImageIcon(imagenRedimensionada);
        jLabel1.setIcon(iconoRedimensionado); // Asigna la imagen redimensionada a jLabel1
    }
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------      
    // Método para redimensionar la imagen de título y asignarla a jLabel1
    private void redimensionarImagenLogo() {
        ImageIcon iconoOriginal = new ImageIcon(getClass().getResource("/Img/Escudo_de_la_universidad_Mariano_Gálvez_Guatemala.svg.png"));
        java.awt.Image imagenRedimensionada = iconoOriginal.getImage().getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH);
        ImageIcon iconoRedimensionado = new ImageIcon(imagenRedimensionada);
        jLabel2.setIcon(iconoRedimensionado); // Asigna la imagen redimensionada a jLabel1
    }
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
    // Método para inicializar jTable1 con un modelo vacío sin filas en blanco
    private void inicializarTabla() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
        new Object[]{"Código", "Nombre del Producto", "Precio Unitario", "Cantidad del Producto", "Fecha de Vencimiento"}, 0
        );
        jTable1.setModel(model); // Asigna el modelo vacío a jTable1
    }    
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
    // Método para manejar la acción del botón Search
    private void realizarBusqueda() {
        // Verificar si alguno de los campos de búsqueda está lleno
        boolean campoCodigoLleno = !jTextField1.getText().equals("VC000") && !jTextField1.getText().isEmpty();
        boolean campoNombreLleno = !jTextField2.getText().equals("Product Name") && !jTextField2.getText().isEmpty();
        boolean fechaInicioSeleccionada = jDateChooser1.getDate() != null;
        boolean fechaFinSeleccionada = jDateChooser2.getDate() != null;

        // Si ningún campo está lleno, mostrar un mensaje de advertencia
        if (!campoCodigoLleno && !campoNombreLleno && !fechaInicioSeleccionada && !fechaFinSeleccionada) {
            JOptionPane.showMessageDialog(this, "Debe llenar al menos uno de los campos de búsqueda.", 
                                      "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return; // Detener el método si no hay campos llenos
        }

        // Realizar la búsqueda según los criterios de los campos
        buscarProducto();
    }

    private void realizarBusqueda2() {
        // Verificar si alguno de los campos de búsqueda está lleno
        boolean campoCodigoLleno = !jTextField3.getText().equals("VC000") && !jTextField3.getText().isEmpty();
        boolean campoNombreLleno = !jTextField4.getText().equals("Product Name") && !jTextField4.getText().isEmpty();
        boolean fechaInicioSeleccionada = jDateChooser3.getDate() != null;
        boolean fechaFinSeleccionada = jDateChooser4.getDate() != null;

        // Si ningún campo está lleno, mostrar un mensaje de advertencia
        if (!campoCodigoLleno && !campoNombreLleno && !fechaInicioSeleccionada && !fechaFinSeleccionada) {
            JOptionPane.showMessageDialog(this, "Debe llenar al menos uno de los campos de búsqueda.", 
                                      "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return; // Detener el método si no hay campos llenos
        }

        // Realizar la búsqueda según los criterios de los campos
        buscarProducto2();
    }
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------    
    // Método para configurar los placeholders en jTextField1 y jTextField2
    private void configurarPlaceholders() {
            jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (jTextField1.getText().equals("VC000")) {
                        jTextField1.setText("");
                        jTextField1.setForeground(Color.BLACK);
                    }
                }
                
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (jTextField1.getText().isEmpty()) {
                        jTextField1.setForeground(Color.GRAY);
                        jTextField1.setText("VC000");
                    }
                }
            });

        jTextField2.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (jTextField2.getText().equals("Product Name")) {
                        jTextField2.setText("");
                        jTextField2.setForeground(Color.BLACK);
                    }
                }
                
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (jTextField2.getText().isEmpty()) {
                        jTextField2.setForeground(Color.GRAY);
                        jTextField2.setText("Product Name");
                    }
                }
            });
    }

    private void configurarPlaceholders2() {
        jTextField3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (jTextField3.getText().equals("VC000")) {
                    jTextField3.setText("");
                    jTextField3.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (jTextField3.getText().isEmpty()) {
                    jTextField3.setForeground(Color.GRAY);
                    jTextField3.setText("VC000");
                }
            }
        });

        jTextField4.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (jTextField4.getText().equals("Product Name")) {
                    jTextField4.setText("");
                    jTextField4.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (jTextField4.getText().isEmpty()) {
                    jTextField4.setForeground(Color.GRAY);
                    jTextField4.setText("Product Name");
                }
            }
        });
    }
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------   
    // Método principal que se llama al hacer clic en el botón de búsqueda
    private void buscarProducto() {
    
        // Validación para búsqueda por rango de fechas si ambas fechas están seleccionadas
        if (jDateChooser1.getDate() != null && jDateChooser2.getDate() != null) {
            if (validarRangoDeFechas()) { // Valida que las fechas sean correctas
                buscarPorRangoDeFechas(); // Realiza la búsqueda por rango de fechas
            } else {
                JOptionPane.showMessageDialog(this, "La fecha de inicio debe ser menor o igual a la fecha de fin.", 
                                          "Error de rango de fechas", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Validamos y realizamos la búsqueda en base a los campos
        if (!jTextField1.getText().equals("VC000") && !jTextField1.getText().isEmpty()) {
            buscarPorCodigo(); // Ejecuta la búsqueda solo por código si cumple con el criterio
        }
        if (!jTextField2.getText().equals("Product Name") && !jTextField2.getText().isEmpty()) {
            buscarPorNombre(); // Ejecuta la búsqueda solo por nombre
        }
    }   

private void buscarProducto2() {
    
    // Validación para búsqueda por rango de fechas si ambas fechas están seleccionadas
    if (jDateChooser3.getDate() != null && jDateChooser4.getDate() != null) {
        if (validarRangoDeFechas2()) { // Valida que las fechas sean correctas
            buscarPorRangoDeFechas2(); // Realiza la búsqueda por rango de fechas
        } else {
            JOptionPane.showMessageDialog(this, "La fecha de inicio debe ser menor o igual a la fecha de fin.", 
                                          "Error de rango de fechas", JOptionPane.WARNING_MESSAGE);
            return;
        }
    }

    // Validamos y realizamos la búsqueda en base a los campos
    if (!jTextField3.getText().equals("VC000") && !jTextField3.getText().isEmpty()) {
        buscarPorCodigo2(); // Ejecuta la búsqueda solo por código si cumple con el criterio
    }
    if (!jTextField4.getText().equals("Product Name") && !jTextField4.getText().isEmpty()) {
        buscarPorNombre2(); // Ejecuta la búsqueda solo por nombre
    }
}

//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
// Método para validar el rango de fechas seleccionadas en los JDateChooser
private boolean validarRangoDeFechas() {
    java.util.Date fechaInicio = jDateChooser1.getDate();
    java.util.Date fechaFin = jDateChooser2.getDate();
    
    // Verifica que la fecha de inicio sea menor o igual que la fecha de fin
    return fechaInicio.compareTo(fechaFin) <= 0;
}

private boolean validarRangoDeFechas2() {
    java.util.Date fechaInicio = jDateChooser3.getDate();
    java.util.Date fechaFin = jDateChooser4.getDate();
    
    // Verifica que la fecha de inicio sea menor o igual que la fecha de fin
    return fechaInicio.compareTo(fechaFin) <= 0;
}

//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
// Método para buscar productos por rango de fechas de vencimiento
private void buscarPorRangoDeFechas() {
    // Consulta SQL para buscar productos dentro del rango de fechas
    String sql = "SELECT codigoProducto, nombreProducto, precioUnitario, cantidadProducto, fechaVencimiento " +
                 "FROM producto WHERE fechaVencimiento BETWEEN ? AND ?";

    try (Connection conexion = ConexionBD.getConnection(); 
         PreparedStatement stmt = conexion.prepareStatement(sql)) {

        if (conexion == null) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos.");
            return;
        }

        // Configurar las fechas como parámetros en la consulta
        stmt.setDate(1, new java.sql.Date(jDateChooser1.getDate().getTime()));
        stmt.setDate(2, new java.sql.Date(jDateChooser2.getDate().getTime()));

        // Ejecutar la consulta y obtener los resultados
        ResultSet rs = stmt.executeQuery();

        // Obtener el modelo actual de jTable1 para agregar resultados acumulativos
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();

        // Rellenar el modelo de la tabla con los resultados
        boolean tieneResultados = false;
        while (rs.next()) {
            tieneResultados = true;
            model.addRow(new Object[]{
                rs.getString("codigoProducto"),
                rs.getString("nombreProducto"),
                rs.getDouble("precioUnitario"),
                rs.getInt("cantidadProducto"),
                rs.getDate("fechaVencimiento")
            });
        }

        // Si no hubo resultados, mostrar un mensaje
        if (!tieneResultados) {
            JOptionPane.showMessageDialog(this, "No se encontraron productos en el rango de fechas especificado.", 
                                          "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
        }
        resetPlaceholders();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al buscar productos por rango de fechas: " + e.getMessage());
    }
}

private void buscarPorRangoDeFechas2() {
    // Consulta SQL para buscar productos dentro del rango de fechas
    String sql = "SELECT codigoProducto, nombreProducto, precioUnitario, cantidadProducto, fechaVencimiento " +
                 "FROM producto WHERE fechaVencimiento BETWEEN ? AND ?";

    try (Connection conexion = ConexionBD.getConnection(); 
         PreparedStatement stmt = conexion.prepareStatement(sql)) {

        if (conexion == null) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos.");
            return;
        }

        // Configurar las fechas como parámetros en la consulta
        stmt.setDate(1, new java.sql.Date(jDateChooser3.getDate().getTime()));
        stmt.setDate(2, new java.sql.Date(jDateChooser4.getDate().getTime()));

        // Ejecutar la consulta y obtener los resultados
        ResultSet rs = stmt.executeQuery();

        // Obtener el modelo actual de jTable1 para agregar resultados acumulativos
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable3.getModel();

        // Rellenar el modelo de la tabla con los resultados
        boolean tieneResultados = false;
        while (rs.next()) {
            tieneResultados = true;
            model.addRow(new Object[]{
                rs.getString("codigoProducto"),
                rs.getString("nombreProducto"),
                rs.getDouble("precioUnitario"),
                rs.getInt("cantidadProducto"),
                rs.getDate("fechaVencimiento")
            });
        }

        // Si no hubo resultados, mostrar un mensaje
        if (!tieneResultados) {
            JOptionPane.showMessageDialog(this, "No se encontraron productos en el rango de fechas especificado.", 
                                          "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
        }
        resetPlaceholders();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al buscar productos por rango de fechas: " + e.getMessage());
    }
}

//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
// Método para buscar productos por código
private void buscarPorCodigo() {
    String codigo = jTextField1.getText();

    // Validación del formato de código: debe ser 'VC' seguido de tres dígitos
    if (!codigo.matches("^VC\\d{3}$")) {
        JOptionPane.showMessageDialog(this, "El valor del código no es válido. Debe ingresar 'VC' seguido de tres dígitos.", 
                                      "Advertencia", JOptionPane.WARNING_MESSAGE);
        resetPlaceholders(); // Restablecer el placeholder
        return;
    }

    // Consulta SQL para buscar productos por código
    String sql = "SELECT codigoProducto, nombreProducto, precioUnitario, cantidadProducto, fechaVencimiento " +
                 "FROM producto WHERE codigoProducto = ?";

    try (Connection conexion = ConexionBD.getConnection(); 
         PreparedStatement stmt = conexion.prepareStatement(sql)) {

        if (conexion == null) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos.");
            return;
        }

        // Asignar el código como parámetro en la consulta
        stmt.setString(1, codigo);

        // Ejecutar la consulta y obtener los resultados
        ResultSet rs = stmt.executeQuery();

        // Verificar si hubo resultados; si no, mostrar mensaje y restablecer campo
        if (!rs.isBeforeFirst()) { // No hay resultados
            JOptionPane.showMessageDialog(this, "El código ingresado no está registrado en la base de datos.", 
                                          "Código no encontrado", JOptionPane.WARNING_MESSAGE);
            resetPlaceholders();
            return;
        }

        // Obtener el modelo actual de jTable1
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();

        // Rellenar el modelo de la tabla con los resultados acumulativos
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
        JOptionPane.showMessageDialog(this, "Error al buscar el producto: " + e.getMessage());
    }
}

private void buscarPorCodigo2() {
    String codigo = jTextField3.getText();

    // Validación del formato de código: debe ser 'VC' seguido de tres dígitos
    if (!codigo.matches("^VC\\d{3}$")) {
        JOptionPane.showMessageDialog(this, "El valor del código no es válido. Debe ingresar 'VC' seguido de tres dígitos.", 
                                      "Advertencia", JOptionPane.WARNING_MESSAGE);
        resetPlaceholders(); // Restablecer el placeholder
        return;
    }

    // Consulta SQL para buscar productos por código
    String sql = "SELECT codigoProducto, nombreProducto, precioUnitario, cantidadProducto, fechaVencimiento " +
                 "FROM producto WHERE codigoProducto = ?";

    try (Connection conexion = ConexionBD.getConnection(); 
         PreparedStatement stmt = conexion.prepareStatement(sql)) {

        if (conexion == null) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos.");
            return;
        }

        // Asignar el código como parámetro en la consulta
        stmt.setString(1, codigo);

        // Ejecutar la consulta y obtener los resultados
        ResultSet rs = stmt.executeQuery();

        // Verificar si hubo resultados; si no, mostrar mensaje y restablecer campo
        if (!rs.isBeforeFirst()) { // No hay resultados
            JOptionPane.showMessageDialog(this, "El código ingresado no está registrado en la base de datos.", 
                                          "Código no encontrado", JOptionPane.WARNING_MESSAGE);
            resetPlaceholders();
            return;
        }

        // Obtener el modelo actual de jTable1
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable3.getModel();

        // Rellenar el modelo de la tabla con los resultados acumulativos
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
        JOptionPane.showMessageDialog(this, "Error al buscar el producto: " + e.getMessage());
    }
}

//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
// Método para buscar productos por nombre
private void buscarPorNombre() {
    String nombre = jTextField2.getText();

    // Validación para asegurarse de que el campo solo contiene letras y espacios
    if (!nombre.matches("^[a-zA-Z\\s]*$")) {
        JOptionPane.showMessageDialog(this, "El nombre del producto solo puede contener letras. Ingrese un nombre válido.", 
                                      "Advertencia", JOptionPane.WARNING_MESSAGE);
        resetPlaceholders();
        return;
    }

    // Consulta SQL para buscar productos cuyo nombre comience con la letra o palabra ingresada
    String sql = "SELECT codigoProducto, nombreProducto, precioUnitario, cantidadProducto, fechaVencimiento " +
                 "FROM producto WHERE nombreProducto LIKE ?";

    try (Connection conexion = ConexionBD.getConnection(); 
         PreparedStatement stmt = conexion.prepareStatement(sql)) {

        if (conexion == null) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos.");
            return;
        }

        // Configurar el nombre como parámetro en la consulta
        stmt.setString(1, nombre + "%");

        // Ejecutar la consulta y obtener los resultados
        ResultSet rs = stmt.executeQuery();

        // Obtener el modelo actual de jTable1
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();

        // Rellenar el modelo de la tabla con los resultados acumulativos
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
        JOptionPane.showMessageDialog(this, "Error al buscar el producto: " + e.getMessage());
    }
}

private void buscarPorNombre2() {
    String nombre = jTextField4.getText();

    // Validación para asegurarse de que el campo solo contiene letras y espacios
    if (!nombre.matches("^[a-zA-Z\\s]*$")) {
        JOptionPane.showMessageDialog(this, "El nombre del producto solo puede contener letras. Ingrese un nombre válido.", 
                                      "Advertencia", JOptionPane.WARNING_MESSAGE);
        resetPlaceholders();
        return;
    }

    // Consulta SQL para buscar productos cuyo nombre comience con la letra o palabra ingresada
    String sql = "SELECT codigoProducto, nombreProducto, precioUnitario, cantidadProducto, fechaVencimiento " +
                 "FROM producto WHERE nombreProducto LIKE ?";

    try (Connection conexion = ConexionBD.getConnection(); 
         PreparedStatement stmt = conexion.prepareStatement(sql)) {

        if (conexion == null) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos.");
            return;
        }

        // Configurar el nombre como parámetro en la consulta
        stmt.setString(1, nombre + "%");

        // Ejecutar la consulta y obtener los resultados
        ResultSet rs = stmt.executeQuery();

        // Obtener el modelo actual de jTable1
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable3.getModel();

        // Rellenar el modelo de la tabla con los resultados acumulativos
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
        JOptionPane.showMessageDialog(this, "Error al buscar el producto: " + e.getMessage());
    }
}

//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
// Método para restablecer los placeholders en jTextField
private void resetPlaceholders() {
    jTextField1.setText("VC000");
    jTextField1.setForeground(Color.GRAY);
    
    jTextField2.setText("Product Name");
    jTextField2.setForeground(Color.GRAY);
    
    jDateChooser1.setDate(null);
    jDateChooser2.setDate(null);
}

private void resetPlaceholders2() {
    jTextField3.setText("VC000");
    jTextField3.setForeground(Color.GRAY);
    
    jTextField4.setText("Product Name");
    jTextField4.setForeground(Color.GRAY);
    
    jDateChooser3.setDate(null);
    jDateChooser4.setDate(null);
}
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
// Método para el botón CleanC que limpia todos los campos y reinicia jTable1
private void limpiarCamposCompleto() {
    resetPlaceholders(); // Restablece los placeholders de los TextFields
    
    // Limpia todos los datos de jTable1
    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
    model.setRowCount(0); // Elimina todas las filas de la tabla
}

private void limpiarCamposCompleto2() {
    resetPlaceholders2(); // Restablece los placeholders de los TextFields
    
    // Limpia todos los datos de jTable1
    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable3.getModel();
    model.setRowCount(0); // Elimina todas las filas de la tabla
}

//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------  
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        Consulta = new javax.swing.JButton();
        Inventario = new javax.swing.JButton();
        Facturacion = new javax.swing.JButton();
        ingresoDatos = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        Search = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        CleanC = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        Search2 = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jDateChooser3 = new com.toedter.calendar.JDateChooser();
        jDateChooser4 = new com.toedter.calendar.JDateChooser();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        ingresoRegistro = new javax.swing.JButton();
        actualizarRegistro = new javax.swing.JButton();
        eliminarRegistro = new javax.swing.JButton();
        cleanC2 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        CodigoVenta = new javax.swing.JTextField();
        NameVenta = new javax.swing.JTextField();
        CantidadVenta = new javax.swing.JTextField();
        PrecioVenta = new javax.swing.JTextField();
        DisponibleVenta = new javax.swing.JTextField();
        nitCliente = new javax.swing.JTextField();
        nameCliente = new javax.swing.JTextField();
        cleanVenta = new javax.swing.JButton();
        printVenta = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        PrecioPrint = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Escudo_de_la_universidad_Mariano_Gálvez_Guatemala.svg.png"))); // NOI18N

        Consulta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/lupa.png"))); // NOI18N
        Consulta.setText("Consulta");
        Consulta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConsultaActionPerformed(evt);
            }
        });

        Inventario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/lista-de-verificacion.png"))); // NOI18N
        Inventario.setText("Inventario");
        Inventario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InventarioActionPerformed(evt);
            }
        });

        Facturacion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/factura.png"))); // NOI18N
        Facturacion.setText("Venta");
        Facturacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FacturacionActionPerformed(evt);
            }
        });

        ingresoDatos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Editar.png"))); // NOI18N
        ingresoDatos.setText("Modificar Datos");

        jLabel16.setFont(new java.awt.Font("Arial Narrow", 3, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(204, 204, 204));
        jLabel16.setText("Norland E. Per Cali 1990-23-9137");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Inventario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ingresoDatos, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Consulta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(Facturacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jLabel16)))
                .addContainerGap(52, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(71, 71, 71)
                .addComponent(Consulta)
                .addGap(33, 33, 33)
                .addComponent(Inventario)
                .addGap(31, 31, 31)
                .addComponent(ingresoDatos)
                .addGap(32, 32, 32)
                .addComponent(Facturacion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 154, Short.MAX_VALUE)
                .addComponent(jLabel16)
                .addGap(30, 30, 30))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 270, 710));

        jPanel3.setBackground(new java.awt.Color(51, 51, 51));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Titulo.png"))); // NOI18N

        jTabbedPane1.setBackground(new java.awt.Color(102, 102, 102));

        jPanel2.setBackground(new java.awt.Color(51, 51, 51));

        jLabel3.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Codigo: ");

        jLabel4.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Nombre del Producto:");

        jLabel5.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Rango de Fecha en la que expira la garantia:");

        jTextField1.setForeground(new java.awt.Color(153, 153, 153));
        jTextField1.setText("VC000");
        jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField1FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField1FocusLost(evt);
            }
        });
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField2.setForeground(new java.awt.Color(153, 153, 153));
        jTextField2.setText("Product Name");
        jTextField2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField2FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField2FocusLost(evt);
            }
        });
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        Search.setText("Buscar");
        Search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Código", "Nombre del Producto", "Precio Unitario", "Cantidad de Producto", "Fecha de Vencimiento"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        CleanC.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Limpiar.png"))); // NOI18N
        CleanC.setText("Limpiar Campos");
        CleanC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CleanCActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane1)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jTextField1)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
                            .addGap(52, 52, 52)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel4)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(66, 66, 66)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGap(18, 18, 18)
                            .addComponent(Search, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(CleanC, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(Search, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField2)
                    .addComponent(jTextField1))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(CleanC, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Consulta", jPanel2);

        jPanel4.setBackground(new java.awt.Color(51, 51, 51));

        jLabel7.setFont(new java.awt.Font("Candara", 1, 24)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/inventario.png"))); // NOI18N
        jLabel7.setText("PRODUCTOS");

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Nombre del producto", "Precio unitario", "Cantidad de producto", "Fecha de vencimiento"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(354, 354, 354))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 993, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );

        jTabbedPane1.addTab("Inventario", jPanel4);

        jPanel5.setBackground(new java.awt.Color(51, 51, 51));

        Search2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/lupa.png"))); // NOI18N
        Search2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Search2ActionPerformed(evt);
            }
        });

        jTextField3.setForeground(new java.awt.Color(204, 204, 204));
        jTextField3.setText("VC000");
        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jTextField4.setForeground(new java.awt.Color(204, 204, 204));
        jTextField4.setText("Product Name");
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Nombre del producto", "Precio unitario", "Cantidad de producto", "Fecha de vencimiento"
            }
        ));
        jScrollPane3.setViewportView(jTable3);

        jLabel6.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Codigo:");

        jLabel8.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Nombre del Producto");

        jLabel9.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Rango de fecha en que expira la garantia:");

        ingresoRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Agregar.png"))); // NOI18N
        ingresoRegistro.setText("Ingresar");
        ingresoRegistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ingresoRegistroActionPerformed(evt);
            }
        });

        actualizarRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/actualizarre.png"))); // NOI18N
        actualizarRegistro.setText("Actualizar");
        actualizarRegistro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actualizarRegistroActionPerformed(evt);
            }
        });

        eliminarRegistro.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Eliminar.png"))); // NOI18N
        eliminarRegistro.setText("Eliminar");

        cleanC2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/Limpiar.png"))); // NOI18N
        cleanC2.setText("Limpiar campos");
        cleanC2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cleanC2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(45, 45, 45)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jDateChooser3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jDateChooser4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(32, 32, 32)
                        .addComponent(Search2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 955, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(ingresoRegistro)
                            .addGap(31, 31, 31)
                            .addComponent(actualizarRegistro)
                            .addGap(31, 31, 31)
                            .addComponent(eliminarRegistro)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cleanC2, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(Search2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                    .addComponent(jTextField3)
                    .addComponent(jDateChooser3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jDateChooser4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cleanC2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ingresoRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(actualizarRegistro)
                    .addComponent(eliminarRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Modificar datos", jPanel5);

        jPanel6.setBackground(new java.awt.Color(51, 51, 51));

        CodigoVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CodigoVentaActionPerformed(evt);
            }
        });

        cleanVenta.setBackground(new java.awt.Color(51, 51, 51));
        cleanVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/cancelar.png"))); // NOI18N
        cleanVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cleanVentaActionPerformed(evt);
            }
        });

        printVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/impresora.png"))); // NOI18N
        printVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printVentaActionPerformed(evt);
            }
        });

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "CÓDIGO", "NOMBRE DE PRODUCTO", "CANTIDAD", "PRECIO UNITARIO", "PRECIO TOTAL"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane4.setViewportView(jTable4);

        jLabel10.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Codigo del Producto:");

        jLabel11.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Nombre del producro:");

        jLabel12.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("Cantidad:");

        jLabel13.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("Precio Unitario:");
        jLabel13.setToolTipText("");

        jLabel14.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 102));
        jLabel14.setText("Disponible");
        jLabel14.setToolTipText("");

        jLabel15.setFont(new java.awt.Font("Bahnschrift", 1, 12)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/dinero.png"))); // NOI18N
        jLabel15.setText("TOTAL A PAGAR");

        PrecioPrint.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        PrecioPrint.setForeground(new java.awt.Color(255, 255, 255));
        PrecioPrint.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        PrecioPrint.setText("-----");
        PrecioPrint.setToolTipText("");
        PrecioPrint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel17.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("NIT:");

        jLabel18.setFont(new java.awt.Font("Bahnschrift", 1, 14)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setText("Nombre del cliente:");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(nitCliente)
                            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                        .addGap(43, 43, 43)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(nameCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(printVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(108, 108, 108)))
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(PrecioPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(77, 77, 77))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 960, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(CodigoVenta)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                                .addGap(31, 31, 31)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(NameVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel11))
                                .addGap(30, 30, 30)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                                    .addComponent(CantidadVenta))
                                .addGap(45, 45, 45)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(PrecioVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(47, 47, 47)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(DisponibleVenta)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE))
                                .addGap(40, 40, 40)
                                .addComponent(cleanVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(35, Short.MAX_VALUE))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(NameVenta)
                    .addComponent(CodigoVenta)
                    .addComponent(CantidadVenta)
                    .addComponent(PrecioVenta, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(DisponibleVenta)
                    .addComponent(cleanVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(PrecioPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(77, 77, 77))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nitCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nameCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(printVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(67, 67, 67))))
        );

        jTabbedPane1.addTab("Venta", jPanel6);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(jTabbedPane1)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 538, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("Consulta");

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 0, 1030, 710));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ConsultaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConsultaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ConsultaActionPerformed

    private void InventarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InventarioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_InventarioActionPerformed

    private void FacturacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FacturacionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FacturacionActionPerformed

    private void SearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchActionPerformed
    realizarBusqueda();
    }//GEN-LAST:event_SearchActionPerformed

    private void jTextField1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1FocusGained

    private void jTextField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1FocusLost

    private void jTextField2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField2FocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2FocusGained

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField2FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2FocusLost

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void CleanCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CleanCActionPerformed
        limpiarCamposCompleto();
    }//GEN-LAST:event_CleanCActionPerformed

    private void Search2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Search2ActionPerformed
        realizarBusqueda2();
    }//GEN-LAST:event_Search2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void ingresoRegistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ingresoRegistroActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ingresoRegistroActionPerformed

    private void actualizarRegistroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actualizarRegistroActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_actualizarRegistroActionPerformed

    private void cleanC2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cleanC2ActionPerformed
        limpiarCamposCompleto2();
    }//GEN-LAST:event_cleanC2ActionPerformed

    private void CodigoVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CodigoVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CodigoVentaActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void cleanVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cleanVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cleanVentaActionPerformed

    private void printVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printVentaActionPerformed
        String nit = nitCliente.getText();
        String nombre = nameCliente.getText();
        String precioFinal = PrecioPrint.getText();

        facturaPDFGenerator.generarFactura(nit, nombre, precioFinal, jTable4); // Genera el PDF
        inventarioUpdater.actualizarInventario(jTable4); // Actualiza la base de datos
        ventaCleaner.limpiarCamposVenta(nitCliente, nameCliente, CodigoVenta, NameVenta, CantidadVenta, PrecioVenta, DisponibleVenta, PrecioPrint, jTable4); // Limpia los campos

    }//GEN-LAST:event_printVentaActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MENU.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MENU.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MENU.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MENU.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MENU().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField CantidadVenta;
    private javax.swing.JButton CleanC;
    private javax.swing.JTextField CodigoVenta;
    private javax.swing.JButton Consulta;
    private javax.swing.JTextField DisponibleVenta;
    private javax.swing.JButton Facturacion;
    private javax.swing.JButton Inventario;
    private javax.swing.JTextField NameVenta;
    private javax.swing.JLabel PrecioPrint;
    private javax.swing.JTextField PrecioVenta;
    private javax.swing.JButton Search;
    private javax.swing.JButton Search2;
    private javax.swing.JButton actualizarRegistro;
    private javax.swing.JButton cleanC2;
    private javax.swing.JButton cleanVenta;
    private javax.swing.JButton eliminarRegistro;
    private javax.swing.JButton ingresoDatos;
    private javax.swing.JButton ingresoRegistro;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private com.toedter.calendar.JDateChooser jDateChooser3;
    private com.toedter.calendar.JDateChooser jDateChooser4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField nameCliente;
    private javax.swing.JTextField nitCliente;
    private javax.swing.JButton printVenta;
    // End of variables declaration//GEN-END:variables

}

