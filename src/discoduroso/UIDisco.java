/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package discoduroso;

import javax.swing.tree.DefaultMutableTreeNode; // Clave para el JTree
import javax.swing.tree.DefaultTreeModel;       // Clave para el JTree
import javax.swing.tree.TreePath;               // Para expandir nodos
import Controladores.SistemaArchivos;           // Tu clase
import Controladores.PlanificadorDisco;         // Tu clase
import Modelo.Directorio;                     // Tu clase
import Modelo.Archivo;                        // Tu clase
import Modelo.EntradaSistemaArchivos;           // Tu clase
import EstructuraDeDatos.Nodo;                  // Tu clase
import EstructuraDeDatos.ListaSimple;
import javax.swing.JTree;
import javax.swing.SwingUtilities; // Para detectar el clic derecho
import javax.swing.tree.TreePath;     // Para saber qué nodo se clickeó
import javax.swing.JOptionPane;
import Controladores.Proceso;
import Controladores.SolicitudIO;
import javax.swing.Timer;

/**
 *
 * @author dugla
 */
public class UIDisco extends javax.swing.JFrame {

    /**
     * Creates new form UIDisco
     */
    private SistemaArchivos miSistemaArchivos;
    private PlanificadorDisco miPlanificador;
    private Timer motorSimulacion;
    
    private boolean esModoAdmin = false;
    
    private static UIDisco disquito;
    

    private static final int TAMANO_BLOQUE_PX = 63; // Tamaño de cada cuadrado (mucho más grande)
    private static final int ESPACIO_ENTRE_BLOQUES_PX = 5; // Espacio entre cuadrados
    private static final int BLOQUES_POR_FILA = 10; // 10 bloques por fila (para 5 filas)
    private static final int MARGEN_PANEL_PX = 5;
    
    // Componentes para la Tabla FAT y la Cola de Procesos
    private javax.swing.JTable tablaFAT;
    private javax.swing.JScrollPane scrollTabla;
    private javax.swing.JTextArea areaColaProcesos; 
    private javax.swing.JScrollPane scrollCola;
    
    // Componentes para la TARJETA DE PROCESO ACTIVO
    private javax.swing.JPanel panelTarjeta;
    private javax.swing.JLabel lblProcId;
    private javax.swing.JLabel lblProcEstado;
    private javax.swing.JLabel lblProcOperacion;
    private javax.swing.JLabel lblProcArchivo;
    
    private javax.swing.JPanel panelColas;
    
    
    // --- VARIABLES PARA LAS COLAS DE ESTADO (JList) ---
    private javax.swing.JList<String> listaNuevos;
    private javax.swing.DefaultListModel<String> modeloNuevos;
    
    private javax.swing.JList<String> listaListos;
    private javax.swing.DefaultListModel<String> modeloListos;
    
    private javax.swing.JList<String> listaTerminados;
    private javax.swing.DefaultListModel<String> modeloTerminados;
    
    private javax.swing.JMenuItem menuItemModificar;
    
    private javax.swing.JMenuItem menuItemLeer;
    
    
    private javax.swing.JLabel lblCabezal; // Nueva etiqueta para la aguja
    
    private javax.swing.JMenuBar barraMenu;
    private javax.swing.JMenu menuArchivo;
    private javax.swing.JMenuItem itemGuardar;
    private javax.swing.JMenuItem itemCargar;

    public UIDisco() {
        
       
        initComponents();
        
        
        menuItemLeer = new javax.swing.JMenuItem("Leer Archivo (Mover Cabezal)");
        menuItemLeer.addActionListener(evt -> menuItemLeerActionPerformed(evt));
        menuContextual.add(menuItemLeer);
        
        
        menuItemModificar = new javax.swing.JMenuItem("Modificar Archivo");
        menuItemModificar.addActionListener(evt -> menuItemModificarActionPerformed(evt));

        // Como initComponents() ya se ejecutó, menuContextual ya existe y no es null
        menuContextual.add(menuItemModificar);
        
        // Configuración inicial del modo usuario
        jLabel2.setText("Actualmente: Usuario (Solo Lectura)");
        jLabel2.setForeground(java.awt.Color.ORANGE); // Un color distinto para identificar
        
        // --- BARRA DE MENÚ SUPERIOR ---
        barraMenu = new javax.swing.JMenuBar();
        menuArchivo = new javax.swing.JMenu("Archivo");

        itemGuardar = new javax.swing.JMenuItem("Guardar Estado del Disco");
        itemCargar = new javax.swing.JMenuItem("Cargar Estado del Disco");

        // Iconos (Opcional, usa emojis para rápido)
        itemGuardar.setText("💾 Guardar Estado");
        itemCargar.setText("📂 Cargar Estado");

        // Acciones
        itemGuardar.addActionListener(evt -> guardarEstadoSistema());
        itemCargar.addActionListener(evt -> cargarEstadoSistema());

        menuArchivo.add(itemGuardar);
        menuArchivo.add(new javax.swing.JSeparator()); // Línea separadora
        menuArchivo.add(itemCargar);

        barraMenu.add(menuArchivo);
        
        menuArchivo.setEnabled(false);

        // Asignar la barra al JFrame
        this.setJMenuBar(barraMenu);
        
        
        
        jPanel7.setLayout(new java.awt.GridLayout(4, 1, 5, 5)); // 4 Filas, 1 Columna

    // 2. Creamos la etiqueta nueva
        lblCabezal = new javax.swing.JLabel("📍 Cabezal: 0");
        lblCabezal.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        // El color se arreglará luego con decorarInterfaz(), pero por si acaso:
        lblCabezal.setForeground(java.awt.Color.WHITE); 

        // 3. La agregamos al panel
        jPanel7.add(lblCabezal);

        // 4. Forzamos la actualización visual
        jPanel7.revalidate();
        
        
        
        
        initPanelDerecho();

        // 1. Define el tamaño del disco (ej. 50 bloques)
        int TAMANO_DISCO = 50;

        // 2. Crea el cerebro
        this.miSistemaArchivos = new SistemaArchivos(TAMANO_DISCO);

        // 3. Crea el planificador
        this.miPlanificador = new PlanificadorDisco(this.miSistemaArchivos);

        arbolArchivosTree.setCellRenderer(new RenderizadorArbol());
        
        decorarInterfaz();
        
        

        // --- EL PASO CLAVE ---
        // 4. Llama al método para "dibujar" el árbol por primera vez
        actualizarArbolGUI();
        actualizarEstadisticas();
        actualizarEstadisticasGUI();
        
        actualizarTablasGUI();
        
        

        motorSimulacion = new Timer(1500, (e) -> {
            avanzarSimulacion();
        });

        //motorSimulacion.start();
        
    }

    private void avanzarSimulacion() {
        
        if (miPlanificador.getColaSolicitudes().isEmpty()) {
            motorSimulacion.stop(); // APAGAR MOTOR
            
            // Restaurar botón
            btnIniciarSimu.setEnabled(true);
            btnIniciarSimu.setText("Iniciar Simulación");
            
            // Limpiar tarjeta de ejecución
            mostrarTarjetaProceso(null);
            
            javax.swing.JOptionPane.showMessageDialog(this, "Simulación finalizada. Todos los procesos terminaron.");
            return;
        }
        
        
        
        // Obtenemos la solicitud completada del planificador
        Controladores.SolicitudIO solicitudTerminada = miPlanificador.procesarSiguienteSolicitud();

        if (solicitudTerminada != null) {
            
            // 1. ¡MOSTRAR LA TARJETA! (El proceso ejecutando la acción)
            mostrarTarjetaProceso(solicitudTerminada);
            
            actualizarColasGUI();
            
            // 3. Actualizar Disco y Tabla
            actualizarArbolGUI();
            actualizarEstadisticasGUI();
            jPanel4.repaint();
            actualizarTablasGUI();
            
            // TRUCO VISUAL:
            // Como el timer sigue corriendo, la tarjeta se quedará mostrando el último proceso
            // hasta que llegue el siguiente. Esto es bueno para que te dé tiempo de leerlo.
            
        } else {
            // Si no hubo nada que procesar, ponemos la tarjeta en modo "Inactivo"
            // Solo si quieres que se limpie cuando no hay nada:
             mostrarTarjetaProceso(null);
             
             if (miPlanificador.getColaSolicitudes().isEmpty()) {
                motorSimulacion.stop();
                btnIniciarSimu.setEnabled(true);
                btnIniciarSimu.setText("Iniciar Simulación");
                javax.swing.JOptionPane.showMessageDialog(this, "Simulación Finalizada");
            }
        }
    }
    
    private void actualizarEstadisticas() {
        int total = miSistemaArchivos.getGestorDisco().getTamanoTotal();
        int libres = miSistemaArchivos.getGestorDisco().getCantidadBloquesLibres();
        int ocupados = total - libres;

        jLabel6.setText("Total de Bloques: " + total);
        jLabel5.setText("Total de Bloques Ocupados: " + ocupados);
        jLabel7.setText("Total de Bloques Disponibles: " + libres);
    }

    public void actualizarArbolGUI() {

        // 1. Obtén tu directorio raíz LÓGICO (el de tu clase)
        Directorio raizLogica = miSistemaArchivos.getDirectorioRaiz();

        // 2. Crea el nodo raíz para la GUI (el que JTree entiende).
        //    ¡IMPORTANTE! Almacenamos el OBJETO 'raizLogica' dentro del nodo.
        //    El JTree llamará a su método .toString() para saber qué mostrar (que es el nombre).
        DefaultMutableTreeNode raizGUI = new DefaultMutableTreeNode(raizLogica);

        // 3. (La Magia) Llama al método recursivo para poblar todo el árbol
        poblarArbolRecursivo(raizGUI, raizLogica);

        // 4. Crea un "Modelo" de árbol con tu nodo raíz
        DefaultTreeModel modeloArbol = new DefaultTreeModel(raizGUI);

        // 5. Asigna ese modelo a tu JTree (el que llamaste 'arbolArchivosTree' en el diseñador)
        arbolArchivosTree.setModel(modeloArbol);

        // (Opcional) Expande el nodo raíz para que se vea
        arbolArchivosTree.expandPath(new TreePath(raizGUI.getPath()));
    }

    private void actualizarEstadisticasGUI() {
        // 1. Obtenemos los datos frescos del Gestor de Disco
        int total = miSistemaArchivos.getGestorDisco().getTamanoTotal();
        int libres = miSistemaArchivos.getGestorDisco().getCantidadBloquesLibres();
        int ocupados = total - libres;

        // 2. Imprimimos en consola para verificar que el cálculo es correcto (Depuración)
        System.out.println("Estadísticas -> Total: " + total + ", Libres: " + libres + ", Ocupados: " + ocupados);

        // 3. Cambiamos el texto de los JLabels existentes
        // jLabel6 era el de "Total"
        jLabel6.setText("Total de Bloques: " + total);

        // jLabel5 era el de "Ocupados"
        jLabel5.setText("Total de Bloques ocupados: " + ocupados);

        // jLabel7 era el de "Disponibles"
        jLabel7.setText("Total de Bloques Disponibles: " + libres);
        
        if (miPlanificador != null) {
            int cabeza = miPlanificador.getCabezaActual();
            lblCabezal.setText("📍 Cabezal (Aguja) en: Bloque " + cabeza);
            
            // Opcional: Cambiar color si se está moviendo
            lblCabezal.setForeground(new java.awt.Color(255, 200, 0)); // Amarillo/Naranja para resaltar
        }

        // 4. Forzamos el repintado del panel (por si acaso se queda pegado)
        jPanel7.repaint();

        jPanel4.repaint();
    }

    /**
     * Dibuja la representación visual de todos los bloques del disco en
     * jPanel4. (Versión MEJORADA: con nombres de archivo y punteros)
     *
     * @param g El contexto gráfico del panel.
     */
    private void dibujarBloques(java.awt.Graphics g) {

        // 1. Verificación de seguridad
        if (miSistemaArchivos == null) {
            return;
        }

        Controladores.GestorDisco gestor = miSistemaArchivos.getGestorDisco();
        Modelo.Bloque[] disco = gestor.getDisco();
        int totalBloques = gestor.getTamanoTotal();

        // Colores
        java.awt.Color colorLibre  = new java.awt.Color(45, 45, 48); // Gris oscuro (vacío)
        java.awt.Color colorBorde  = new java.awt.Color(70, 70, 70); // Borde sutil
        java.awt.Color colorTexto  = new java.awt.Color(240, 240, 240);

        for (int i = 0; i < totalBloques; i++) {

            // Cálculo de posición 5x10
            int columna = i % BLOQUES_POR_FILA;
            int fila = i / BLOQUES_POR_FILA;

            int x = MARGEN_PANEL_PX + columna * (TAMANO_BLOQUE_PX + ESPACIO_ENTRE_BLOQUES_PX);
            int y = MARGEN_PANEL_PX + fila * (TAMANO_BLOQUE_PX + ESPACIO_ENTRE_BLOQUES_PX);

            Modelo.Bloque bloqueActual = disco[i];
            boolean estaOcupado = bloqueActual.estaOcupado();

            // Lógica de color por archivo
            if (estaOcupado) {
                String propietario = bloqueActual.getPropietario(); // Esto trae "/root/tarea.txt"
                
                // --- CORRECCIÓN DE COLOR ---
                // Extraemos solo el nombre del archivo para que el Hash coincida con la Tabla FAT
                int lastSlash = propietario.lastIndexOf('/');
                String nombreSolo = (lastSlash == -1) ? propietario : propietario.substring(lastSlash + 1);
                
                // Usamos 'nombreSolo' para el color, NO 'propietario' completo
                int hash = nombreSolo.hashCode(); 
                java.awt.Color colorArchivo = java.awt.Color.getHSBColor(Math.abs(hash % 360) / 360f, 0.8f, 0.5f);
                
                g.setColor(colorArchivo);
                // ---------------------------
            } else {
                g.setColor(colorLibre);
            }

            // Dibujar el cuadrado relleno
            g.fillRect(x, y, TAMANO_BLOQUE_PX, TAMANO_BLOQUE_PX);

            // Dibujar el borde
            g.setColor(colorBorde);
            g.drawRect(x, y, TAMANO_BLOQUE_PX, TAMANO_BLOQUE_PX);

            // --- 2. DIBUJAR EL TEXTO DENTRO DEL BLOQUE ---
            if (estaOcupado) {
                // Configurar fuente (pequeña y negrita)
                g.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
                
                
                
                g.setColor(colorTexto);

                // --- Extraer y acortar el nombre del archivo ---
                String propietario = bloqueActual.getPropietario(); // ej: "/root/docs/mi_archivo.txt"
                int lastSlash = propietario.lastIndexOf('/');
                String nombreArchivo = (lastSlash == -1) ? propietario : propietario.substring(lastSlash + 1); // ej: "mi_archivo.txt"

                // Acortar si es muy largo (ej: "mi_archi…")
                if (nombreArchivo.length() > 9) {
                    nombreArchivo = nombreArchivo.substring(0, 8) + "…";
                }

                // Dibujar Nombre (arriba)
                g.drawString(nombreArchivo, x + 5, y + 15); // 15px desde arriba

                // --- Dibujar Puntero de Asignación Encadenada (abajo) ---
                int siguiente = bloqueActual.getSiguienteBloque();
                //String sigStr = (siguiente == -1) ? "FIN" : "-> " + siguiente;
                if (siguiente != -1) {
                    String flecha = "⮕ " + siguiente; // Flecha estética

                    g.setColor(java.awt.Color.BLACK);
                    g.drawString(flecha, x + 6, y + 51);

                    g.setColor(java.awt.Color.YELLOW); // Amarillo para resaltar el puntero
                    g.drawString(flecha, x + 5, y + 50);
                }
                

                g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
                //g.drawString(sigStr, x + 5, y + 50); // 50px desde arriba (casi abajo)
            }
        }
    }

    /**
     * Método auxiliar RECURSIVO. Recorre tu ListaSimple de hijos y los añade al
     * árbol de la GUI.
     *
     * * @param nodoPadreGUI El nodo de la GUI al que le añadiremos hijos.
     * @param dirPadreLogico El Directorio (tuyo) del que leeremos los hijos.
     */
    private void poblarArbolRecursivo(DefaultMutableTreeNode nodoPadreGUI, Directorio dirPadreLogico) {

        // Obtenemos la lista de hijos usando tu método getHijos()
        ListaSimple<EntradaSistemaArchivos> listaHijos = dirPadreLogico.getHijos();

        // Caso base: Si no hay hijos, terminamos.
        if (listaHijos.isEmpty()) {
            return;
        }

        // Iteramos sobre tu ListaSimple usando getpFirst() y getPnext()
        Nodo<EntradaSistemaArchivos> nodoActual = listaHijos.getpFirst();

        while (nodoActual != null) {

            // 1. Obtenemos el dato (el Archivo o Directorio)
            EntradaSistemaArchivos entradaLogica = nodoActual.getData();

            // 2. Creamos un nuevo nodo de GUI (guardando el objeto lógico)
            DefaultMutableTreeNode nuevoNodoGUI = new DefaultMutableTreeNode(entradaLogica);

            // 3. Lo añadimos al padre en la GUI
            nodoPadreGUI.add(nuevoNodoGUI);

            // 4. RECURSIÓN: Si este hijo es un Directorio, repetimos el proceso
            if (entradaLogica instanceof Directorio) {
                poblarArbolRecursivo(nuevoNodoGUI, (Directorio) entradaLogica);
            }

            // 5. Avanzamos en tu ListaSimple
            nodoActual = nodoActual.getPnext();
        }
    }
    
    private void initPanelDerecho() {
        // Layout principal
        jPanel5.setLayout(new java.awt.BorderLayout(0, 5));

        // 1. TABLA FAT (Centro)
        String[] columnas = {"Color", "Archivo", "Tamaño", "Bloque Inicio", "Proceso"};
        javax.swing.table.DefaultTableModel modeloTabla = new javax.swing.table.DefaultTableModel(null, columnas) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaFAT = new javax.swing.JTable(modeloTabla);
        tablaFAT.getColumnModel().getColumn(0).setCellRenderer(new RenderizadorColorFAT());
        tablaFAT.getColumnModel().getColumn(0).setPreferredWidth(30);
        tablaFAT.getColumnModel().getColumn(0).setMaxWidth(30);
        scrollTabla = new javax.swing.JScrollPane(tablaFAT);
        scrollTabla.setBorder(javax.swing.BorderFactory.createTitledBorder("1. Tabla FAT"));
        
        jPanel5.add(scrollTabla, java.awt.BorderLayout.CENTER);

        // --- CONTENEDOR SUR (Tarjeta + Listas) ---
        javax.swing.JPanel panelSur = new javax.swing.JPanel();
        panelSur.setLayout(new java.awt.BorderLayout(0, 5));
        panelSur.setBackground(jPanel5.getBackground());

        // 2. TARJETA DE EJECUCIÓN
        panelTarjeta = new javax.swing.JPanel();
        panelTarjeta.setLayout(new java.awt.GridLayout(2, 2, 5, 0));
        panelTarjeta.setBorder(javax.swing.BorderFactory.createTitledBorder("2. Ejecución (CPU)"));
        panelTarjeta.setPreferredSize(new java.awt.Dimension(0, 80));
        
        // Etiquetas
        java.awt.Font fontBold = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12);
        lblProcId = new javax.swing.JLabel("ID: -"); lblProcId.setFont(fontBold);
        lblProcEstado = new javax.swing.JLabel("Est: IDLE"); lblProcEstado.setFont(fontBold);
        lblProcOperacion = new javax.swing.JLabel("Op: -"); lblProcOperacion.setFont(fontBold);
        lblProcArchivo = new javax.swing.JLabel("Ref: -"); lblProcArchivo.setFont(fontBold);
        
        panelTarjeta.add(lblProcId); panelTarjeta.add(lblProcEstado);
        panelTarjeta.add(lblProcOperacion); panelTarjeta.add(lblProcArchivo);
        
        panelSur.add(panelTarjeta, java.awt.BorderLayout.NORTH);

        // 3. LAS 3 COLAS (JLIST) - AQUÍ ESTABA EL ERROR
        // Usamos la variable de clase panelColas DIRECTAMENTE
        panelColas = new javax.swing.JPanel(); 
        panelColas.setLayout(new java.awt.GridLayout(1, 3, 5, 0)); 
        panelColas.setPreferredSize(new java.awt.Dimension(0, 150));
        
        // ¡FORZAMOS EL COLOR OSCURO AQUÍ!
        panelColas.setBackground(new java.awt.Color(45, 45, 48));

        // Inicializamos Modelos y JList
        modeloNuevos = new javax.swing.DefaultListModel<>();
        listaNuevos = new javax.swing.JList<>(modeloNuevos);

        modeloListos = new javax.swing.DefaultListModel<>();
        listaListos = new javax.swing.JList<>(modeloListos);

        modeloTerminados = new javax.swing.DefaultListModel<>();
        listaTerminados = new javax.swing.JList<>(modeloTerminados);

        // Agregamos al panel usando el método ESTILIZADO (El que pone borde oscuro)
        panelColas.add(crearScrollEstilizado(listaNuevos, "Nuevos"));
        panelColas.add(crearScrollEstilizado(listaListos, "Listos"));
        panelColas.add(crearScrollEstilizado(listaTerminados, "Terminados"));
        
        // Finalmente, agregamos panelColas (el oscuro) al panelSur
        panelSur.add(panelColas, java.awt.BorderLayout.CENTER);
        
        // Y panelSur al panel principal
        jPanel5.add(panelSur, java.awt.BorderLayout.SOUTH);
        
        jPanel5.revalidate();
    }

    // Método auxiliar para configurar JList rápidamente
    private javax.swing.JScrollPane crearScrollJList(javax.swing.JList<String> lista, String titulo) {
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(lista);
        scroll.setBorder(javax.swing.BorderFactory.createTitledBorder(titulo));
        return scroll;
    }
    
    public void mostrarTarjetaProceso(Controladores.SolicitudIO solicitud) {
        // Colores Oscuros para la tarjeta
        java.awt.Color bgInactivo = new java.awt.Color(45, 45, 55); // Gris azulado oscuro
        java.awt.Color bgActivo   = new java.awt.Color(20, 50, 20); // Verde bosque muy oscuro
        java.awt.Color textoGris  = new java.awt.Color(150, 150, 150);
        java.awt.Color textoClaro = new java.awt.Color(240, 240, 240);
        java.awt.Color textoVerde = new java.awt.Color(100, 255, 100); // Verde neón para resaltar

        if (solicitud == null) {
            // MODO INACTIVO (OSCURO)
            panelTarjeta.setBackground(bgInactivo);
            
            lblProcId.setText("ID: -");
            lblProcId.setForeground(textoGris);
            
            lblProcEstado.setText("Est: IDLE");
            lblProcEstado.setForeground(textoGris);
            
            lblProcOperacion.setText("Op: -");
            lblProcOperacion.setForeground(textoGris);
            
            lblProcArchivo.setText("Ref: -");
            lblProcArchivo.setForeground(textoGris);
            
            // Borde gris sutil
            panelTarjeta.setBorder(javax.swing.BorderFactory.createTitledBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(80,80,80)), 
                    "Proceso Ejecución", 0, 0, lblProcId.getFont(), textoGris));
            
        } else {
            // MODO ACTIVO (VERDE OSCURO)
            panelTarjeta.setBackground(bgActivo);
            
            lblProcId.setText("ID: " + solicitud.getProceso().getId());
            lblProcId.setForeground(textoClaro);
            
            lblProcEstado.setText("Est: EJECUTANDO");
            lblProcEstado.setForeground(textoVerde); // Resaltar estado
            
            lblProcOperacion.setText("Op: " + solicitud.getOperacion());
            lblProcOperacion.setForeground(textoClaro);
            
            String nombre = (solicitud.getNombreNuevo() != null) ? solicitud.getNombreNuevo() : 
                            (solicitud.getEntradaAEliminar() != null) ? solicitud.getEntradaAEliminar().getNombre() : "-";
            lblProcArchivo.setText("Ref: " + nombre);
            lblProcArchivo.setForeground(textoClaro);
            
            // Borde verde sutil
            panelTarjeta.setBorder(javax.swing.BorderFactory.createTitledBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(50, 100, 50)), 
                    "Proceso Ejecución", 0, 0, lblProcId.getFont(), textoVerde));
        }
        panelTarjeta.repaint();
    }
    
    public static synchronized UIDisco getInstance() {
        if (disquito == null) {
            setDisco(new UIDisco());
        }
        return disquito;
    }
    
    private javax.swing.JScrollPane crearScrollEstilizado(javax.swing.JList<String> lista, String titulo) {
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(lista);
        
        // Colores
        java.awt.Color fondo = new java.awt.Color(40, 40, 40);
        java.awt.Color texto = new java.awt.Color(220, 220, 220);
        java.awt.Color borde = new java.awt.Color(100, 100, 100);
        
        // Configurar la Lista interna
        lista.setBackground(fondo);
        lista.setForeground(texto);
        lista.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        
        // Configurar el Borde con Título
        scroll.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(borde),
                titulo,
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12),
                texto
        ));
        
        // Configurar el fondo del Scroll
        scroll.setBackground(fondo);
        scroll.getViewport().setBackground(fondo);
        
        return scroll;
    }
    
    public static void setDisco(UIDisco discooo) {
        disquito = discooo;
    }
    
    public void actualizarTablasGUI() {
        // --- A. ACTUALIZAR TABLA FAT (Esto SÍ va) ---
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tablaFAT.getModel();
        modelo.setRowCount(0); 
        
        if (miSistemaArchivos != null) {
            // Recuerda que corregimos getTamanoEnBloques() aquí
            llenarTablaFATRecursivo(miSistemaArchivos.getDirectorioRaiz(), modelo);
        }

        // --- B. COLA DE PROCESOS (BORRA ESTA PARTE) ---
        // El error está aquí abajo. Como quitamos el JTextArea para poner la tarjeta,
        // ya no podemos escribir texto. BORRA O COMENTA ESTAS LÍNEAS:
        
        /* StringBuilder sb = new StringBuilder();
        if (miPlanificador != null) {
             // ... código viejo ...
        }
        areaColaProcesos.setText(sb.toString()); <--- ESTA LINEA CAUSA EL ERROR
        */
    }
    
    public void actualizarColasGUI() {
        // Validación de seguridad
        if (miPlanificador == null) return;

        // --- 1. ACTUALIZAR "LISTOS" (Centro) ---
        modeloListos.clear(); // Limpiamos para no repetir
        
        // Recorremos TU ListaSimple manualmente
        EstructuraDeDatos.Nodo<Controladores.SolicitudIO> actual = miPlanificador.getColaSolicitudes().getpFirst();
        
        while (actual != null) {
            Controladores.SolicitudIO sol = actual.getData();
            // Formato: [ID] Operacion
            String texto = "[" + sol.getProceso().getId() + "] " + sol.getOperacion();
            modeloListos.addElement(texto);
            
            // Avanzamos con tu nodo
            actual = actual.getPnext();
        }

        // --- 2. ACTUALIZAR "TERMINADOS" (Derecha) ---
        modeloTerminados.clear();
        
        // Recorremos la lista de terminados del planificador
        EstructuraDeDatos.Nodo<Controladores.SolicitudIO> term = miPlanificador.getListaTerminados().getpFirst();
        
        while (term != null) {
            Controladores.SolicitudIO sol = term.getData();
            String texto = "✔ " + sol.getProceso().getId() + " (" + sol.getOperacion() + ")";
            modeloTerminados.addElement(texto);
            
            term = term.getPnext();
        }
        
        // "Nuevos" se queda vacía porque pasan directo a Listos en este simulador
        modeloNuevos.clear(); 
        
        EstructuraDeDatos.Nodo<Controladores.SolicitudIO> nuevos = miPlanificador.getColaNuevos().getpFirst();
        
        while (nuevos != null) {
            Controladores.SolicitudIO sol = nuevos.getData();
            // Mostramos algo simple indicando que es Nuevo
            String texto = "🆕 " + sol.getProceso().getId() + " (" + sol.getOperacion() + ")";
            modeloNuevos.addElement(texto);
            
            nuevos = nuevos.getPnext();
        }
    }
    // Auxiliar para recorrer carpetas y llenar la tabla
    private void llenarTablaFATRecursivo(Modelo.Directorio dir, javax.swing.table.DefaultTableModel modelo) {
        if (dir == null) return;
        
        EstructuraDeDatos.ListaSimple<Modelo.EntradaSistemaArchivos> hijos = dir.getHijos();
        if (hijos == null) return;

        EstructuraDeDatos.Nodo<Modelo.EntradaSistemaArchivos> nodo = hijos.getpFirst();

        while(nodo != null) {
            Modelo.EntradaSistemaArchivos entrada = nodo.getData();
            
            if (entrada instanceof Modelo.Archivo) {
                Modelo.Archivo arch = (Modelo.Archivo) entrada;
                
                // Agregamos fila: 
                // [0] Color (null, el renderer lo pinta)
                // [1] Nombre
                // [2] Tamaño
                // [3] Inicio
                modelo.addRow(new Object[]{
                    "", // Columna color vacía (se pinta sola)
                    arch.getNombre(), 
                    arch.getTamanoEnBloques(), 
                    arch.getPrimerBloque(),
                    arch.getIdProcesoCreador()
                        
                });
                
            } else if (entrada instanceof Modelo.Directorio) {
                llenarTablaFATRecursivo((Modelo.Directorio) entrada, modelo);
            }
            nodo = nodo.getPnext();
        }
    }
    
    /**
     * Método para embellecer la interfaz sin tocar el código generado.
     * Aplica un tema oscuro profesional (Dark Tech).
     */
    /**
     * Versión 2.0: Estilo Windows 11 Dark Mode
     */
    private void decorarInterfaz() {
        // PALETA OSCURA
        java.awt.Color bgFondo      = new java.awt.Color(30, 30, 30);
        java.awt.Color bgPanel      = new java.awt.Color(37, 37, 38); // Gris VS Code
        java.awt.Color fgTexto      = new java.awt.Color(220, 220, 220);
        java.awt.Color bordeSutil   = new java.awt.Color(60, 60, 60);
        
        java.awt.Color btnNormal    = new java.awt.Color(60, 60, 60); // Gris oscuro
        java.awt.Color btnHover     = new java.awt.Color(0, 120, 215); // Azul al pasar el mouse
        
        java.awt.Font fuenteNormal = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12);
        java.awt.Font fuenteBold   = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12);
        
        // Fuente
        java.awt.Font fuenteStd = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12);

        // 1. Paneles Generales
        this.getContentPane().setBackground(bgFondo);
        javax.swing.JPanel[] paneles = {jPanel1, jPanel2, jPanel3, jPanel4, jPanel5, jPanel6, jPanel7};
        
        for (javax.swing.JPanel p : paneles) {
            if (p != null) {
                p.setBackground(bgPanel);
                p.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, bordeSutil));
                // Actualizar labels internos
                for (java.awt.Component c : p.getComponents()) {
                    
                    if (c instanceof javax.swing.JLabel) {
                        c.setForeground(fgTexto);
                        c.setFont(fuenteNormal);
                        
                    } else if (c instanceof javax.swing.JButton) {
                        // ¡AQUÍ LLAMAMOS AL MÉTODO!
                        estilizarBoton((javax.swing.JButton) c, btnNormal, btnHover, fgTexto, fuenteBold);
                        
                    } else if (c instanceof javax.swing.JToggleButton) {
                        // TAMBIÉN PARA LOS TOGGLE BUTTONS (PAUSAR)
                        estilizarBoton((javax.swing.JToggleButton) c, btnNormal, btnHover, fgTexto, fuenteBold);
                        
                    } else if (c instanceof javax.swing.JComboBox) {
                        c.setBackground(bgFondo);
                        c.setForeground(fgTexto);
                        c.setFont(fuenteNormal);
                    }
                }    
            }
        }
        
        // El disco central más oscuro
        if (jPanel4 != null) jPanel4.setBackground(new java.awt.Color(20, 20, 20));

        // 2. ARREGLAR TABLA FAT (El área blanca que te molesta)
        if (tablaFAT != null) {
            tablaFAT.setBackground(new java.awt.Color(30, 30, 30)); // Fondo filas
            tablaFAT.setForeground(fgTexto); // Texto filas
            tablaFAT.setGridColor(bordeSutil);
            
            // Encabezado
            tablaFAT.getTableHeader().setBackground(new java.awt.Color(45, 45, 48));
            tablaFAT.getTableHeader().setForeground(fgTexto);
            
            // ¡IMPORTANTE! El fondo "sobrante" del ScrollPane que se ve blanco
            if (scrollTabla != null) {
                scrollTabla.getViewport().setBackground(new java.awt.Color(30, 30, 30));
                scrollTabla.setBackground(new java.awt.Color(30, 30, 30));
                scrollTabla.setBorder(javax.swing.BorderFactory.createTitledBorder(
                        javax.swing.BorderFactory.createLineBorder(bordeSutil), "Tabla FAT", 
                        0, 0, fuenteStd, fgTexto));
            }
        }

        // 3. ARREGLAR TARJETA DE PROCESO (Abajo a la derecha)
        if (panelTarjeta != null) {
            panelTarjeta.setBackground(new java.awt.Color(45, 45, 55)); // Azul grisáceo muy oscuro
            // Borde con título blanco
            panelTarjeta.setBorder(javax.swing.BorderFactory.createTitledBorder(
                        javax.swing.BorderFactory.createLineBorder(bordeSutil), "Proceso Ejecución", 
                        0, 0, fuenteStd, fgTexto));
            
            // Labels internos de la tarjeta
            lblProcId.setForeground(fgTexto);
            lblProcOperacion.setForeground(fgTexto);
            lblProcArchivo.setForeground(fgTexto);
            // El estado lo dejamos con color (Verde/Gris) según la lógica de mostrarTarjeta
        }
        
        // 4. ÁRBOL (JTree)
        if (arbolArchivosTree != null) {
             arbolArchivosTree.setBackground(new java.awt.Color(30, 30, 30));
             arbolArchivosTree.setCellRenderer(new RenderizadorArbolModerno()); // Tu renderizador de íconos
        }
        
        if (listaListos != null) {
        javax.swing.JList[] listas = {listaNuevos, listaListos, listaTerminados};
        java.awt.Color fondoOscuro = new java.awt.Color(40, 40, 40); // Gris oscuro
        java.awt.Color textoBlanco = new java.awt.Color(230, 230, 230);
        
        for (javax.swing.JList l : listas) {
            l.setBackground(fondoOscuro);
            l.setForeground(textoBlanco);
            l.setSelectionBackground(new java.awt.Color(0, 120, 215)); // Azul al seleccionar
            l.setSelectionForeground(java.awt.Color.WHITE);
        }
    }
        
        
        // En decorarInterfaz...
        if (barraMenu != null) {
            barraMenu.setBackground(bgPanel);
            barraMenu.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, bordeSutil));
            menuArchivo.setForeground(fgTexto);
            itemGuardar.setBackground(bgPanel); itemGuardar.setForeground(fgTexto);
            itemCargar.setBackground(bgPanel); itemCargar.setForeground(fgTexto);
        }
        
        
        
        
        
    }

    /**
     * Método auxiliar para hacer botones planos y modernos.
     */
    private void estilizarBoton(javax.swing.AbstractButton btn, java.awt.Color normal, java.awt.Color hover, java.awt.Color texto, java.awt.Font fuente) {
        btn.setBackground(normal);
        btn.setForeground(texto);
        btn.setFont(fuente);
        
        // Quitar estilos 3D viejos
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); // Sin borde, estilo Flat
        btn.setContentAreaFilled(false); // Necesario para pintar custom background
        btn.setOpaque(true);
        
        // Agregar cursor de mano
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Listener para efecto Hover (Mouse encima)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Si es un ToggleButton seleccionado, no cambiar color
                if (btn instanceof javax.swing.JToggleButton && btn.isSelected()) return;
                btn.setBackground(hover);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn instanceof javax.swing.JToggleButton && btn.isSelected()) return;
                btn.setBackground(normal);
            }
        });
    }
    
    private void menuItemModificarActionPerformed(java.awt.event.ActionEvent evt) {
    // 1. Obtener nodo seleccionado
    javax.swing.tree.DefaultMutableTreeNode nodo = (javax.swing.tree.DefaultMutableTreeNode) arbolArchivosTree.getLastSelectedPathComponent();

    // Validar que sea un Archivo
    if (nodo == null || !(nodo.getUserObject() instanceof Modelo.Archivo)) {
        javax.swing.JOptionPane.showMessageDialog(this, "Seleccione un archivo para modificar.");
        return;
    }

    Modelo.Archivo archivoActual = (Modelo.Archivo) nodo.getUserObject();

    // 2. Pedir Nuevo Nombre
    String nuevoNombre = javax.swing.JOptionPane.showInputDialog(this, 
            "Nuevo nombre (Deje igual para mantener):", 
            archivoActual.getNombre());

    if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
        nuevoNombre = archivoActual.getNombre(); 
    }

    // 3. Pedir Nuevo Tamaño
    int nuevoTamano = archivoActual.getTamanoEnBloques();
    int confirm = javax.swing.JOptionPane.showConfirmDialog(this, 
            "¿Desea cambiar el tamaño? (Actual: " + nuevoTamano + ")",
            "Modificar Tamaño", javax.swing.JOptionPane.YES_NO_OPTION);

    if (confirm == javax.swing.JOptionPane.YES_OPTION) {
        String tStr = javax.swing.JOptionPane.showInputDialog(this, "Ingrese nuevo tamaño en bloques:");
        try {
            nuevoTamano = Integer.parseInt(tStr);
            if (nuevoTamano <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Tamaño inválido.");
            return;
        }
    }

    // 4. Crear Solicitud
    Controladores.Proceso p = new Controladores.Proceso();
    Controladores.SolicitudIO solicitud = new Controladores.SolicitudIO(
            p, 
            archivoActual, 
            nuevoNombre, 
            nuevoTamano
    );

    // 5. Encolar y Actualizar Visualmente
    miPlanificador.agregarSolicitudNUEVA(solicitud);
    actualizarColasGUI(); // Reflejar en lista "Listos"
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menuContextual = new javax.swing.JPopupMenu();
        menuItemCrearDir = new javax.swing.JMenuItem();
        menuItemCrearArchivo = new javax.swing.JMenuItem();
        menuItemEliminar = new javax.swing.JMenuItem();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        arbolArchivosTree = new javax.swing.JTree();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g); // Dibuja el fondo del panel
                dibujarBloques(g);       // Llama a tu método de dibujo de bloques
            }
        };
        jLabel8 = new javax.swing.JLabel();
        btnIniciarSimu = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jPanel7 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        menuItemCrearDir.setText("Crear Directorio");
        menuItemCrearDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemCrearDirActionPerformed(evt);
            }
        });
        menuContextual.add(menuItemCrearDir);

        menuItemCrearArchivo.setText("Crear Archivo");
        menuItemCrearArchivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemCrearArchivoActionPerformed(evt);
            }
        });
        menuContextual.add(menuItemCrearArchivo);

        menuItemEliminar.setText("Eliminar");
        menuItemEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemEliminarActionPerformed(evt);
            }
        });
        menuContextual.add(menuItemEliminar);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(1600, 900));

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

        arbolArchivosTree.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Explorador", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        arbolArchivosTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                arbolArchivosTreeMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(arbolArchivosTree);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Tipo de Usuario");

        jLabel2.setText("Actualmente:");

        jButton1.setText("Cambiar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(jLabel2))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(127, 127, 127)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1)
                            .addComponent(jLabel1))))
                .addContainerGap(117, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addGap(68, 68, 68))
        );

        jPanel4.setBackground(new java.awt.Color(204, 255, 204));

        jLabel8.setText("Bloques por Almacenamiento");

        btnIniciarSimu.setText("Iniciar Simulacion");
        btnIniciarSimu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarSimuActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(278, 278, 278)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(193, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnIniciarSimu)
                .addGap(252, 252, 252))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(358, Short.MAX_VALUE)
                .addComponent(btnIniciarSimu)
                .addGap(18, 18, 18)
                .addComponent(jLabel8)
                .addGap(39, 39, 39))
        );

        jPanel5.setBackground(new java.awt.Color(51, 51, 51));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 380, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 338, Short.MAX_VALUE)
        );

        jPanel6.setBackground(new java.awt.Color(204, 255, 255));

        jLabel3.setText("Planificador del Disco");

        jComboBox1.setBackground(new java.awt.Color(51, 51, 51));
        jComboBox1.setForeground(new java.awt.Color(255, 255, 255));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FIFO", "SSTF", "SCAN", "C-SCAN" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap(96, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(119, 119, 119))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(56, 56, 56))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54))
        );

        jPanel7.setBackground(new java.awt.Color(255, 255, 204));

        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Almacenamiento");

        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Total de Bloques ocupados:");

        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("Total de Bloques: 50");

        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Total de Bloques Disponibles:");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(292, 292, 292))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(0, 103, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab1", jPanel1);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
        String algoritmoSeleccionado = (String) jComboBox1.getSelectedItem();
        
        if (miPlanificador != null) {
            // 2. Comunicar al planificador el cambio
            // Asegúrate de tener un método 'setAlgoritmo' en tu clase PlanificadorDisco
            // que acepte un String o un Enum.
            miPlanificador.setAlgoritmo(algoritmoSeleccionado);
            
            System.out.println("Algoritmo cambiado a: " + algoritmoSeleccionado);
            
            // 3. (Opcional) Si quieres reordenar la cola visualmente al instante:
            // miPlanificador.reordenarCola(); 
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void arbolArchivosTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_arbolArchivosTreeMouseClicked
        // TODO add your handling code here:
        if (javax.swing.SwingUtilities.isRightMouseButton(evt)) {
            
            // 1. Seleccionar la fila bajo el mouse
            int fila = arbolArchivosTree.getRowForLocation(evt.getX(), evt.getY());
            if (fila == -1) return;
            arbolArchivosTree.setSelectionRow(fila);
            
            // 2. Obtener el objeto seleccionado
            javax.swing.tree.TreePath ruta = arbolArchivosTree.getPathForLocation(evt.getX(), evt.getY());
            javax.swing.tree.DefaultMutableTreeNode nodo = (javax.swing.tree.DefaultMutableTreeNode) ruta.getLastPathComponent();
            Object info = nodo.getUserObject();
            
            // 3. REGLAS DE PERMISOS
            
            // A. Si es MODO USUARIO: Bloqueamos casi todo
            if (!esModoAdmin) {
                menuItemCrearDir.setEnabled(false);
                menuItemCrearArchivo.setEnabled(false);
                menuItemEliminar.setEnabled(false);
                menuItemModificar.setEnabled(false); // Asumiendo que ya creaste este
                
                // La única excepción: LEER (Si es un archivo)
                // Nota: menuItemLeer lo crearemos en el paso 2, pero ya lo dejamos listo aquí
                if (info instanceof Modelo.Archivo) {
                    if (menuItemLeer != null) menuItemLeer.setEnabled(true);
                } else {
                    if (menuItemLeer != null) menuItemLeer.setEnabled(false);
                }
                
            } else { 
                // B. Si es MODO ADMIN: Lógica normal según si es Archivo o Carpeta
                
                if (info instanceof Modelo.Directorio) {
                    menuItemCrearDir.setEnabled(true);
                    menuItemCrearArchivo.setEnabled(true);
                    
                    // No borrar la raíz
                    Modelo.Directorio dir = (Modelo.Directorio) info;
                    menuItemEliminar.setEnabled(dir.getPadre() != null);
                    
                    menuItemModificar.setEnabled(false);
                    if (menuItemLeer != null) menuItemLeer.setEnabled(false); // No se leen carpetas
                    
                } else if (info instanceof Modelo.Archivo) {
                    menuItemCrearDir.setEnabled(false);
                    menuItemCrearArchivo.setEnabled(false);
                    menuItemEliminar.setEnabled(true);
                    menuItemModificar.setEnabled(true);
                    
                    if (menuItemLeer != null) menuItemLeer.setEnabled(true);
                }
            }
            
            // Mostrar menú
            menuContextual.show(arbolArchivosTree, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_arbolArchivosTreeMouseClicked

    private void menuItemCrearDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemCrearDirActionPerformed
        // TODO add your handling code here:
        // Como el clic derecho ya seleccionó el nodo, esto funciona:
        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) arbolArchivosTree.getLastSelectedPathComponent();

        // El listener ya se aseguró de que esto es un Directorio, pero validamos por si acaso
        if (nodoSeleccionado != null && nodoSeleccionado.getUserObject() instanceof Directorio) {
            Directorio padre = (Directorio) nodoSeleccionado.getUserObject();
            String nombreNuevo = JOptionPane.showInputDialog(this, "Nombre del nuevo directorio:", "Crear Directorio", JOptionPane.PLAIN_MESSAGE);

            if (nombreNuevo != null && !nombreNuevo.trim().isEmpty()) {
                Proceso p = new Proceso();
                SolicitudIO solicitud = new SolicitudIO(p, padre, nombreNuevo);
                miPlanificador.agregarSolicitudNUEVA(solicitud);
                
                actualizarColasGUI();

                // (Recuerda que el Timer 'motorSimulacion' se encargará de actualizar el árbol)
            }
        }
    }//GEN-LAST:event_menuItemCrearDirActionPerformed

    private void menuItemEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemEliminarActionPerformed
        // TODO add your handling code here:
        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) arbolArchivosTree.getLastSelectedPathComponent();

        if (nodoSeleccionado == null) {
            return; // No debería pasar, pero por seguridad
        }
        EntradaSistemaArchivos entradaAEliminar = (EntradaSistemaArchivos) nodoSeleccionado.getUserObject();

        // El listener ya deshabilitó la raíz, así que no hay que verificarlo aquí.
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar '" + entradaAEliminar.getNombre() + "'?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Proceso p = new Proceso();
            SolicitudIO solicitud = new SolicitudIO(p, entradaAEliminar);
            miPlanificador.agregarSolicitudNUEVA(solicitud);
            actualizarColasGUI();
        }

    }//GEN-LAST:event_menuItemEliminarActionPerformed

    private void menuItemCrearArchivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemCrearArchivoActionPerformed
        DefaultMutableTreeNode nodoSeleccionado = (DefaultMutableTreeNode) arbolArchivosTree.getLastSelectedPathComponent();

        // 2. Validar que sea un directorio
        if (nodoSeleccionado != null && nodoSeleccionado.getUserObject() instanceof Directorio) {
            Directorio padre = (Directorio) nodoSeleccionado.getUserObject();

            // 3. Pedir Nombre
            String nombre = JOptionPane.showInputDialog(this, "Nombre del archivo:", "Crear Archivo", JOptionPane.PLAIN_MESSAGE);
            if (nombre == null || nombre.trim().isEmpty()) {
                return;
            }

            // 4. Pedir Tamaño (IMPORTANTE PARA LOS BLOQUES)
            String tamanoStr = JOptionPane.showInputDialog(this, "Tamaño en bloques (número entero):", "Crear Archivo", JOptionPane.QUESTION_MESSAGE);

            // --- ESTA ES LA PARTE QUE FALTABA ---
            try {
                int tamano = Integer.parseInt(tamanoStr);

                if (tamano <= 0) {
                    JOptionPane.showMessageDialog(this, "El tamaño debe ser mayor a 0.");
                    return; // Aquí terminaba tu código
                }
                
                int disponibles = miSistemaArchivos.getGestorDisco().getCantidadBloquesLibres();
                
                if (tamano > disponibles) {
                    // ALERTA DE ERROR
                    javax.swing.JOptionPane.showMessageDialog(this, 
                        "⚠️ ERROR DE ESPACIO INSUFICIENTE\n" +
                        "Solicitado: " + tamano + " bloques.\n" +
                        "Disponible: " + disponibles + " bloques.\n" +
                        "No se puede crear el archivo.", 
                        "Disco Lleno", 
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                    return; // Detenemos aquí, no se agrega a la cola
                }

                // 5. Crear Proceso y Solicitud
                Proceso p = new Proceso();
                // Usamos el constructor de SolicitudIO para Archivos (con tamaño)
                SolicitudIO solicitud = new SolicitudIO(p, padre, nombre, tamano);

                // 6. Encolar la tarea
                miPlanificador.agregarSolicitudNUEVA(solicitud);
                
                actualizarColasGUI();
                
                actualizarTablasGUI();

                System.out.println("Solicitud de archivo '" + nombre + "' (" + tamano + " bloques) encolada.");

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un número válido para el tamaño.");
            }
            // --- FIN DE LA PARTE QUE FALTABA ---
        }
    }//GEN-LAST:event_menuItemCrearArchivoActionPerformed

    private void menuItemLeerActionPerformed(java.awt.event.ActionEvent evt) {
    // 1. Obtener archivo
        javax.swing.tree.DefaultMutableTreeNode nodo = (javax.swing.tree.DefaultMutableTreeNode) arbolArchivosTree.getLastSelectedPathComponent();
        if (nodo == null || !(nodo.getUserObject() instanceof Modelo.Archivo)) return;

        Modelo.Archivo archivo = (Modelo.Archivo) nodo.getUserObject();

        // 2. Crear Solicitud de Lectura
        Controladores.Proceso p = new Controladores.Proceso();
        Controladores.SolicitudIO solicitud = new Controladores.SolicitudIO(p, archivo);

        // 3. Encolar
        miPlanificador.agregarSolicitudNUEVA(solicitud);
        actualizarColasGUI();

        System.out.println("Solicitud de LECTURA encolada para: " + archivo.getNombre());
}
    
    private void btnIniciarSimuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarSimuActionPerformed
        // TODO add your handling code here:// 1. VALIDACIÓN CORREGIDA
            // Verificamos si AMBAS colas están vacías. Si es así, no hay nada que hacer.
            // Antes seguro solo verificabas 'getColaSolicitudes()' (Listos).
            boolean noHayNuevos = miPlanificador.getColaNuevos().isEmpty();
            boolean noHayListos = miPlanificador.getColaSolicitudes().isEmpty();

            if (noHayNuevos && noHayListos) {
                javax.swing.JOptionPane.showMessageDialog(rootPane, "No hay procesos en cola. Cree archivos primero.");
                return;
            }

            // 2. TRANSICIÓN (EL PASO CLAVE)
            // Antes de arrancar el timer, movemos todo lo que está en "Nuevos" hacia "Listos"
            miPlanificador.transitarNuevosAListos();
            
            // Actualizamos la pantalla para que el usuario VEA el cambio de columna instantáneo
            actualizarColasGUI(); 

            // 3. ARRANCAR EL MOTOR
            btnIniciarSimu.setEnabled(false);
            btnIniciarSimu.setText("EJECUTANDO...");
            
            motorSimulacion.start();
        
        
    }//GEN-LAST:event_btnIniciarSimuActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        esModoAdmin = !esModoAdmin;
        
        if (esModoAdmin) {
            jLabel2.setText("Actualmente: ADMINISTRADOR");
            jLabel2.setForeground(new java.awt.Color(0, 150, 0)); // Verde
            menuArchivo.setEnabled(true);
            javax.swing.JOptionPane.showMessageDialog(this, "Modo Admin activado.\nAcceso total (Crear/Eliminar/Modificar).");
        } else {
            jLabel2.setText("Actualmente: Usuario (Solo Lectura)");
            jLabel2.setForeground(java.awt.Color.ORANGE);
            menuArchivo.setEnabled(false);
            javax.swing.JOptionPane.showMessageDialog(this, "Modo Usuario activado.\nSolo puede LEER archivos.");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void guardarEstadoSistema() {
        // 1. Selector de Archivos
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        if (fc.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivo = fc.getSelectedFile();
            // Asegurar extensión .txt
            if (!archivo.getName().endsWith(".txt")) {
                archivo = new java.io.File(archivo.getAbsolutePath() + ".txt");
            }
            
            try (java.io.PrintWriter writer = new java.io.PrintWriter(archivo)) {
                // 2. Recorrer el árbol recursivamente y escribir
                // Empezamos desde la raíz del sistema de archivos
                Modelo.Directorio raiz = miSistemaArchivos.getDirectorioRaiz();
                guardarRecursivo(raiz, writer);
                
                javax.swing.JOptionPane.showMessageDialog(this, "Sistema guardado exitosamente.");
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
            }
        }
    }

    // Método auxiliar recursivo para recorrer TU ListaSimple
    private void guardarRecursivo(Modelo.Directorio dir, java.io.PrintWriter writer) {
        // Obtenemos hijos usando TU estructura
        EstructuraDeDatos.ListaSimple<Modelo.EntradaSistemaArchivos> hijos = dir.getHijos();
        if (hijos == null) return;

        EstructuraDeDatos.Nodo<Modelo.EntradaSistemaArchivos> nodo = hijos.getpFirst();
        
        while (nodo != null) {
            Modelo.EntradaSistemaArchivos entrada = nodo.getData();
            
            // FORMATO: TIPO | RUTA_PADRE | NOMBRE | TAMAÑO
            String padreRuta = entrada.getPadre().getRutaCompleta();
            
            if (entrada instanceof Modelo.Archivo) {
                Modelo.Archivo arch = (Modelo.Archivo) entrada;
                writer.println("FILE," + padreRuta + "," + arch.getNombre() + "," + arch.getTamanoEnBloques());
            } else if (entrada instanceof Modelo.Directorio) {
                Modelo.Directorio subDir = (Modelo.Directorio) entrada;
                writer.println("DIR," + padreRuta + "," + subDir.getNombre() + ",0");
                
                // Recursión para guardar lo que hay dentro de este directorio
                guardarRecursivo(subDir, writer);
            }
            
            nodo = nodo.getPnext();
        }
    }
    
    
    private void cargarEstadoSistema() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivo = fc.getSelectedFile();
            
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(archivo))) {
                
                // 1. RESETEAR SISTEMA (Borrar todo lo actual)
                reiniciarSistemaCompleto();
                
                String linea;
                while ((linea = br.readLine()) != null) {
                    // Leemos: TIPO, RUTA_PADRE, NOMBRE, TAMAÑO
                    String[] partes = linea.split(",");
                    if (partes.length < 3) continue;
                    
                    String tipo = partes[0];
                    String rutaPadre = partes[1];
                    String nombre = partes[2];
                    int tamano = Integer.parseInt(partes[3]);
                    
                    // Buscar el objeto padre en el sistema actual
                    Modelo.EntradaSistemaArchivos objPadre = miSistemaArchivos.getEntrada(rutaPadre);
                    
                    if (objPadre instanceof Modelo.Directorio) {
                        Modelo.Directorio dirPadre = (Modelo.Directorio) objPadre;
                        
                        if (tipo.equals("DIR")) {
                            miSistemaArchivos.crearDirectorio(nombre, dirPadre);
                        } else if (tipo.equals("FILE")) {
                            miSistemaArchivos.crearArchivo(nombre, tamano, dirPadre);
                        }
                    }
                }
                
                // 2. Actualizar toda la interfaz
                actualizarArbolGUI();
                actualizarEstadisticasGUI();
                actualizarTablasGUI();
                jPanel4.repaint();
                
                javax.swing.JOptionPane.showMessageDialog(this, "Sistema cargado correctamente.");
                
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error al cargar: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Método para dejar el sistema como nuevo (Wipe)
    private void reiniciarSistemaCompleto() {
        // Simplemente creamos una instancia nueva del sistema y planificador
        // Esto borra memoria RAM y Disco virtual
        int tamanoDisco = 50; // Ojo: Debe ser el mismo tamaño
        
        this.miSistemaArchivos = new Controladores.SistemaArchivos(tamanoDisco);
        this.miPlanificador = new Controladores.PlanificadorDisco(this.miSistemaArchivos);
        
        // Limpiar listas visuales
        modeloNuevos.clear();
        modeloListos.clear();
        modeloTerminados.clear();
        mostrarTarjetaProceso(null);
    }
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
            java.util.logging.Logger.getLogger(UIDisco.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UIDisco.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UIDisco.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UIDisco.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UIDisco().setVisible(true);
            }
        });
    }

    // --- CLASE INTERNA PARA PERSONALIZAR ÍCONOS ---
    class RenderizadorArbol extends javax.swing.tree.DefaultTreeCellRenderer {

        @Override
        public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            // 1. Llamamos al comportamiento por defecto (para colores, selección, texto, etc.)
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            // 2. Obtenemos el nodo y sus datos
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
            Object userObject = nodo.getUserObject();

            // 3. VERIFICACIÓN DE TIPO (La parte clave)
            if (userObject instanceof Directorio) {
                // ES UN DIRECTORIO:
                // Forzamos el ícono de carpeta, incluso si es una "hoja" (leaf) vacía.
                if (expanded) {
                    setIcon(getDefaultOpenIcon());   // Carpeta abierta
                } else {
                    setIcon(getDefaultClosedIcon()); // Carpeta cerrada
                }
            } else if (userObject instanceof Archivo) {
                // ES UN ARCHIVO:
                setIcon(getDefaultLeafIcon());       // Ícono de archivo
            }

            return this;
        }
    }
    
    
    
    // Clase interna para pintar la columna de "Color" en la tabla
    class RenderizadorColorFAT extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Obtenemos el nombre (columna 1)
            String nombreArchivo = (String) table.getModel().getValueAt(row, 1); 
            
            if (nombreArchivo != null) {
                int hash = nombreArchivo.hashCode();
                
                // --- CORRECCIÓN AQUÍ ---
                // Usamos 0.8f (Saturación) y 0.5f (Brillo) IGUAL QUE EN 'dibujarBloques'
                java.awt.Color colorArchivo = java.awt.Color.getHSBColor(Math.abs(hash % 360) / 360f, 0.8f, 0.5f);
                
                c.setBackground(colorArchivo);
                c.setForeground(colorArchivo); 
            } else {
                // Si no hay archivo, color de fondo normal de la tabla
                c.setBackground(table.getBackground());
            }
            
            return c;
        }
    }
    
    /**
     * Renderizador que arregla el texto negro y usa Emojis de Windows 11
     * para simular íconos nativos de colores sin necesidad de imágenes externas.
     */
    class RenderizadorArbolModerno extends javax.swing.tree.DefaultTreeCellRenderer {
        
        private java.awt.Font fuenteNormal = new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 14); // Fuente Emoji para que se vean bonitos

        @Override
        public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value, 
                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            
            // 1. ARREGLAR COLORES (El problema del texto negro)
            // Si está seleccionado: Azul fondo, Blanco texto
            // Si NO está seleccionado: Transparente fondo, Blanco texto
            if (sel) {
                setForeground(new java.awt.Color(255, 255, 255)); 
                setBackgroundSelectionColor(new java.awt.Color(0, 120, 215)); // Azul Windows
            } else {
                setForeground(new java.awt.Color(230, 230, 230)); // Blanco suave
                setBackgroundNonSelectionColor(new java.awt.Color(30, 30, 30)); // Mismo que el fondo
            }
            
            setFont(fuenteNormal);

            // 2. CAMBIAR ÍCONOS POR "EMOJIS NATIVOS" (Truco visual)
            // Anulamos el ícono por defecto de Java para usar el nuestro en el texto
            setIcon(null); 
            
            javax.swing.tree.DefaultMutableTreeNode nodo = (javax.swing.tree.DefaultMutableTreeNode) value;
            Object info = nodo.getUserObject();
            String nombre = value.toString();

            // Detectamos qué es y le ponemos su ícono de Windows 11
            if (info instanceof Modelo.Directorio) {
                Modelo.Directorio dir = (Modelo.Directorio) info;
                // Si es la raíz (root)
                if (dir.getPadre() == null) {
                    setText("💻 " + nombre); // Ícono de PC para root
                } else {
                    // Carpetas abiertas o cerradas
                    if (expanded) {
                        setText("📂 " + nombre); 
                    } else {
                        setText("📁 " + nombre); // Carpeta amarilla de Windows
                    }
                }
            } else if (info instanceof Modelo.Archivo) {
                 setText("📄 " + nombre); // Hoja de papel blanca
            } else {
                // Por defecto (si hubiera nodos extraños)
                setText("🔹 " + nombre);
            }
            
            return this;
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree arbolArchivosTree;
    private javax.swing.JButton btnIniciarSimu;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPopupMenu menuContextual;
    private javax.swing.JMenuItem menuItemCrearArchivo;
    private javax.swing.JMenuItem menuItemCrearDir;
    private javax.swing.JMenuItem menuItemEliminar;
    // End of variables declaration//GEN-END:variables
}
