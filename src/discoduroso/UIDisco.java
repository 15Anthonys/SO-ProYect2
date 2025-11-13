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

    private static final int TAMANO_BLOQUE_PX = 63; // Tamaño de cada cuadrado (mucho más grande)
    private static final int ESPACIO_ENTRE_BLOQUES_PX = 5; // Espacio entre cuadrados
    private static final int BLOQUES_POR_FILA = 10; // 10 bloques por fila (para 5 filas)
    private static final int MARGEN_PANEL_PX = 5;

    public UIDisco() {
        initComponents();

        // 1. Define el tamaño del disco (ej. 50 bloques)
        int TAMANO_DISCO = 50;

        // 2. Crea el cerebro
        this.miSistemaArchivos = new SistemaArchivos(TAMANO_DISCO);

        // 3. Crea el planificador
        this.miPlanificador = new PlanificadorDisco(this.miSistemaArchivos);

        arbolArchivosTree.setCellRenderer(new RenderizadorArbol());

        // --- EL PASO CLAVE ---
        // 4. Llama al método para "dibujar" el árbol por primera vez
        actualizarArbolGUI();
        actualizarEstadisticas();
        actualizarEstadisticasGUI();

        motorSimulacion = new Timer(1500, (e) -> {
            avanzarSimulacion();
        });

        motorSimulacion.start();

    }

    private void avanzarSimulacion() {
        // Le decimos al planificador: "Procesa la siguiente tarea en la cola"
        boolean huboCambios = miPlanificador.procesarSiguienteSolicitud();

        // Si el planificador hizo algo (retornó true), actualizamos la GUI
        if (huboCambios) {
            System.out.println("¡Tarea procesada! Actualizando árbol...");
            actualizarArbolGUI();     // Refresca el JTree
            actualizarEstadisticas(); // Refresca los labels de bloques
            actualizarEstadisticasGUI();
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
        java.awt.Color colorLibre = new java.awt.Color(230, 230, 230); // Gris muy claro
        java.awt.Color colorBorde = java.awt.Color.DARK_GRAY;
        java.awt.Color colorTexto = java.awt.Color.BLACK;

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
                String propietario = bloqueActual.getPropietario();
                int hash = propietario.hashCode();
                // (Esta es la lógica que genera un color "único" por archivo)
                java.awt.Color colorArchivo = java.awt.Color.getHSBColor(Math.abs(hash % 360) / 360f, 0.7f, 1.0f);
                g.setColor(colorArchivo);
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
                g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
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
                String sigStr = (siguiente == -1) ? "FIN" : "-> " + siguiente;

                g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
                g.drawString(sigStr, x + 5, y + 50); // 50px desde arriba (casi abajo)
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

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

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
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Tipo de Usuario");

        jLabel2.setText("Actualmente:");

        jButton1.setText("Cambiar");

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
                .addContainerGap(132, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(35, 35, 35)
                .addComponent(jButton1)
                .addGap(45, 45, 45))
        );

        jPanel4.setBackground(new java.awt.Color(204, 255, 204));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 692, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel5.setBackground(new java.awt.Color(255, 204, 255));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel6.setBackground(new java.awt.Color(204, 255, 255));

        jLabel3.setText("Planificador del Disco");

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
                .addContainerGap(80, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(75, 75, 75))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(106, 106, 106))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(126, 126, 126)
                        .addComponent(jLabel4))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel5))))
                .addContainerGap(128, Short.MAX_VALUE))
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addGap(16, 16, 16)
                    .addComponent(jLabel6)
                    .addContainerGap(217, Short.MAX_VALUE)))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGap(58, 58, 58)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addGap(50, 50, 50)
                    .addComponent(jLabel6)
                    .addContainerGap(112, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 460, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("tab1", jPanel1);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void arbolArchivosTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_arbolArchivosTreeMouseClicked
        // TODO add your handling code here:
        // 1. Verificar si fue un clic derecho
        if (SwingUtilities.isRightMouseButton(evt)) {

            // 2. Obtener la fila (row) del árbol donde se hizo clic
            int fila = arbolArchivosTree.getRowForLocation(evt.getX(), evt.getY());

            // Si no se hizo clic en ningún nodo (clic en el fondo), no hacemos nada
            if (fila == -1) {
                return;
            }

            // 3. Seleccionar programáticamente el nodo que se clickeó
            // Esto es CLAVE para que luego podamos obtener el nodo correcto.
            arbolArchivosTree.setSelectionRow(fila);

            // 4. Obtener el nodo y el objeto lógico (Archivo o Directorio)
            TreePath rutaSeleccionada = arbolArchivosTree.getPathForLocation(evt.getX(), evt.getY());

            // Verificación de seguridad
            if (rutaSeleccionada == null) {
                return;
            }

            DefaultMutableTreeNode nodoClickeado = (DefaultMutableTreeNode) rutaSeleccionada.getLastPathComponent();
            Object objetoClickeado = nodoClickeado.getUserObject();
            EntradaSistemaArchivos entrada = (EntradaSistemaArchivos) objetoClickeado;

            // --- AQUÍ ESTÁ LA NUEVA LÓGICA ---
            // 5. Configurar el menú según el TIPO de objeto
            if (entrada instanceof Directorio) {
                // --- Es un Directorio ---

                // Opciones de creación: SÍ se puede crear dentro de un directorio
                menuItemCrearDir.setEnabled(true);
                menuItemCrearArchivo.setEnabled(true);

                // Opción de eliminar: Cambiamos el texto
                menuItemEliminar.setText("Eliminar Directorio...");

                // Lógica de seguridad: No se puede eliminar la raíz
                if (entrada.getPadre() == null) {
                    menuItemEliminar.setEnabled(false); // Es la raíz, deshabilitar
                } else {
                    menuItemEliminar.setEnabled(true);  // No es la raíz, habilitar
                }

            } else if (entrada instanceof Archivo) {
                // --- Es un Archivo ---

                // Opciones de creación: NO se puede crear "dentro" de un archivo
                menuItemCrearDir.setEnabled(false);
                menuItemCrearArchivo.setEnabled(false);

                // Opción de eliminar: Siempre se puede eliminar un archivo
                menuItemEliminar.setText("Eliminar Archivo...");
                menuItemEliminar.setEnabled(true);
            }

            // 6. ¡Mostrar el menú en la posición del clic!
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
                miPlanificador.agregarSolicitud(solicitud);

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
            miPlanificador.agregarSolicitud(solicitud);
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

                // 5. Crear Proceso y Solicitud
                Proceso p = new Proceso();
                // Usamos el constructor de SolicitudIO para Archivos (con tamaño)
                SolicitudIO solicitud = new SolicitudIO(p, padre, nombre, tamano);

                // 6. Encolar la tarea
                miPlanificador.agregarSolicitud(solicitud);

                System.out.println("Solicitud de archivo '" + nombre + "' (" + tamano + " bloques) encolada.");

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un número válido para el tamaño.");
            }
            // --- FIN DE LA PARTE QUE FALTABA ---
        }
    }//GEN-LAST:event_menuItemCrearArchivoActionPerformed

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree arbolArchivosTree;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
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
