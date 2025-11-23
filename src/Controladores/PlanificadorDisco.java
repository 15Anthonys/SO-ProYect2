/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

import EstructuraDeDatos.ListaSimple;
import EstructuraDeDatos.Nodo;
import Modelo.Archivo;
import Modelo.Directorio; // Asegúrate de importar Directorio

/**
 * Administra la cola de solicitudes de E/S y decide cuál ejecutar
 * según la política de planificación seleccionada.
 * (Versión con SCAN y C-SCAN implementados)
 */
public class PlanificadorDisco {

    // Usamos ListaSimple para poder iterar y buscar (necesario para SSTF, SCAN)
    private final ListaSimple<SolicitudIO> colaSolicitudes;
    
    private final SistemaArchivos sistemaArchivos; // Para ejecutar la operación
    private final GestorDisco gestorDisco;
    
    private PoliticaPlanificacion politicaActual;
    private int cabezaActual; // Posición actual del cabezal del disco
    private DireccionSCAN direccionSCAN; // Para los algoritmos SCAN

    // Enum interno para la dirección de SCAN
    private enum DireccionSCAN { ASCENDENTE, DESCENDENTE }

    public PlanificadorDisco(SistemaArchivos sistemaArchivos) {
        this.sistemaArchivos = sistemaArchivos;
        this.gestorDisco = sistemaArchivos.getGestorDisco();
        this.colaSolicitudes = new ListaSimple<>();
        this.politicaActual = PoliticaPlanificacion.FIFO; // Por defecto
        this.cabezaActual = 0;
        this.direccionSCAN = DireccionSCAN.ASCENDENTE; // Empezamos subiendo
    }

    /**
     * La GUI llamará a este método para añadir una nueva "tarea"
     */
    public void agregarSolicitud(SolicitudIO solicitud) {
        solicitud.getProceso().setEstado(EstadoProceso.BLOQUEADO);
        this.colaSolicitudes.addAtTheEnd(solicitud);
    }

    /**
     * El método principal. Decide qué solicitud tomar, la ejecuta
     * y la elimina de la cola.
     */
    public boolean procesarSiguienteSolicitud() {
        if (colaSolicitudes.isEmpty()) {
            return false; // No hay nada que procesar
        }

        // 1. Encontrar la siguiente solicitud según la política
        SolicitudIO solicitud = buscarSiguienteSolicitud();
        
        // Si no se encontró ninguna solicitud (ej. listas vacías en SCAN),
        // no hacer nada.
        if (solicitud == null) {
            return false;
        }

        // 2. Ejecutar la operación
        ejecutarSolicitud(solicitud);
        
        // 3. Actualizar estado del cabezal y del proceso
        this.cabezaActual = solicitud.getPosicionEnDisco();
        solicitud.getProceso().setEstado(EstadoProceso.TERMINADO);

        // 4. Eliminar la solicitud de la lista
        colaSolicitudes.delete(solicitud); // Usamos tu método delete()
        
        return true;
    }
    
    /**
     * Método "cerebro" que elige la solicitud correcta
     */
    private SolicitudIO buscarSiguienteSolicitud() {
        switch (politicaActual) {
            case FIFO:
                return buscarSiguienteFIFO();
            case SSTF:
                return buscarSiguienteSSTF();
            case SCAN:
                return buscarSiguienteSCAN();
            case CSCAN:
                return buscarSiguienteCSCAN();
            default:
                return buscarSiguienteFIFO();
        }
    }

    /**
     * Ejecuta la operación llamando al Sistema de Archivos
     */
    private void ejecutarSolicitud(SolicitudIO sol) {
        switch (sol.getOperacion()) {
            case CREAR_ARCHIVO:
                sistemaArchivos.crearArchivo(sol.getNombreNuevo(), sol.getTamanoArchivo(), sol.getPadre());
                break;
            case ELIMINAR_ARCHIVO:
                sistemaArchivos.eliminarArchivo((Archivo) sol.getEntradaAEliminar());
                break;
            case CREAR_DIRECTORIO:
                sistemaArchivos.crearDirectorio(sol.getNombreNuevo(), sol.getPadre());
                break;
            case ELIMINAR_DIRECTORIO:
                // Hacemos el cast a Modelo.Directorio
                sistemaArchivos.eliminarDirectorio((Directorio) sol.getEntradaAEliminar());
                break;
        }
    }

    // ----- ALGORITMOS DE PLANIFICACIÓN -----

    /**
     * FIFO: Simplemente toma el primero de la lista.
     */
    private SolicitudIO buscarSiguienteFIFO() {
        if (colaSolicitudes.isEmpty()) return null;
        // El primero de la lista es el primero que entró (usando tu getpFirst)
        return colaSolicitudes.getpFirst().getData();
    }

    /**
     * SSTF: Busca en TODA la lista la solicitud con la 'posicionEnDisco'
     * más cercana a 'cabezaActual'.
     */
    private SolicitudIO buscarSiguienteSSTF() {
        if (colaSolicitudes.isEmpty()) return null;

        Nodo<SolicitudIO> actual = colaSolicitudes.getpFirst();
        SolicitudIO mejorSolicitud = actual.getData();
        int distanciaMinima = Math.abs(mejorSolicitud.getPosicionEnDisco() - cabezaActual);

        // Recorremos la ListaSimple
        while (actual != null) {
            SolicitudIO solicitudActual = actual.getData();
            int distanciaActual = Math.abs(solicitudActual.getPosicionEnDisco() - cabezaActual);

            if (distanciaActual < distanciaMinima) {
                distanciaMinima = distanciaActual;
                mejorSolicitud = solicitudActual;
            }
            actual = actual.getPnext(); // Usamos tu método getPnext
        }
        return mejorSolicitud;
    }

    /**
     * SCAN (Elevator): El cabezal se mueve en una dirección, atendiendo
     * solicitudes, hasta llegar al final, donde invierte la dirección.
     */
    private SolicitudIO buscarSiguienteSCAN() {
        if (colaSolicitudes.isEmpty()) return null;

        // 1. Separar solicitudes en dos listas:
        //    - ascendentes: las que están en la posición actual o más adelante
        //    - descendentes: las que están detrás
        ListaSimple<SolicitudIO> ascendentes = new ListaSimple<>();
        ListaSimple<SolicitudIO> descendentes = new ListaSimple<>();
        
        Nodo<SolicitudIO> actual = colaSolicitudes.getpFirst();
        while (actual != null) {
            SolicitudIO sol = actual.getData();
            if (sol.getPosicionEnDisco() >= cabezaActual) {
                ascendentes.addAtTheEnd(sol);
            } else {
                descendentes.addAtTheEnd(sol);
            }
            actual = actual.getPnext();
        }

        // 2. Decidir cuál tomar
        SolicitudIO elegida = null;
        
        if (direccionSCAN == DireccionSCAN.ASCENDENTE) {
            // Buscamos la más cercana hacia "arriba"
            elegida = encontrarMinimo(ascendentes);
            if (elegida == null) {
                // No hay más hacia arriba, cambiamos dirección
                direccionSCAN = DireccionSCAN.DESCENDENTE;
                // Buscamos la más cercana hacia "abajo"
                elegida = encontrarMaximo(descendentes);
            }
        } else { // DIRECCION_DESCENDENTE
            // Buscamos la más cercana hacia "abajo"
            elegida = encontrarMaximo(descendentes);
            if (elegida == null) {
                // No hay más hacia abajo, cambiamos dirección
                direccionSCAN = DireccionSCAN.ASCENDENTE;
                // Buscamos la más cercana hacia "arriba"
                elegida = encontrarMinimo(ascendentes);
            }
        }
        
        return elegida;
    }

    /**
     * C-SCAN (Circular SCAN): Igual que SCAN, pero al llegar al final,
     * salta al inicio (0) y sigue en la misma dirección (siempre ascendente).
     */
    private SolicitudIO buscarSiguienteCSCAN() {
        if (colaSolicitudes.isEmpty()) return null;

        // 1. Separar en ascendentes y descendentes (igual que SCAN)
        ListaSimple<SolicitudIO> ascendentes = new ListaSimple<>();
        ListaSimple<SolicitudIO> descendentes = new ListaSimple<>();
        
        Nodo<SolicitudIO> actual = colaSolicitudes.getpFirst();
        while (actual != null) {
            SolicitudIO sol = actual.getData();
            if (sol.getPosicionEnDisco() >= cabezaActual) {
                ascendentes.addAtTheEnd(sol);
            } else {
                descendentes.addAtTheEnd(sol);
            }
            actual = actual.getPnext();
        }

        // 2. Decidir
        // C-SCAN siempre sube. Primero busca la más cercana hacia arriba.
        SolicitudIO elegida = encontrarMinimo(ascendentes);
        
        if (elegida == null) {
            // Si no hay más hacia arriba, "salta" al inicio (0)
            // y toma la más cercana al inicio (la mínima de las descendentes).
            elegida = encontrarMinimo(descendentes);
        }
        
        return elegida;
    }
    
    // ----- MÉTODOS DE AYUDA -----
    // (Helpers para buscar en tu ListaSimple)

    /**
     * Encuentra la solicitud con la posición MÍNIMA en una lista.
     * @return La SolicitudIO, o null si la lista está vacía.
     */
    private SolicitudIO encontrarMinimo(ListaSimple<SolicitudIO> lista) {
        if (lista.isEmpty()) {
            return null;
        }
        
        Nodo<SolicitudIO> actual = lista.getpFirst();
        SolicitudIO minSolicitud = actual.getData();
        int minPos = minSolicitud.getPosicionEnDisco();
        
        while (actual != null) {
            SolicitudIO solActual = actual.getData();
            if (solActual.getPosicionEnDisco() < minPos) {
                minPos = solActual.getPosicionEnDisco();
                minSolicitud = solActual;
            }
            actual = actual.getPnext();
        }
        return minSolicitud;
    }

    /**
     * Encuentra la solicitud con la posición MÁXIMA en una lista.
     * @return La SolicitudIO, o null si la lista está vacía.
     */
    private SolicitudIO encontrarMaximo(ListaSimple<SolicitudIO> lista) {
        if (lista.isEmpty()) {
            return null;
        }
        
        Nodo<SolicitudIO> actual = lista.getpFirst();
        SolicitudIO maxSolicitud = actual.getData();
        int maxPos = maxSolicitud.getPosicionEnDisco();
        
        while (actual != null) {
            SolicitudIO solActual = actual.getData();
            if (solActual.getPosicionEnDisco() > maxPos) {
                maxPos = solActual.getPosicionEnDisco();
                maxSolicitud = solActual;
            }
            actual = actual.getPnext();
        }
        return maxSolicitud;
    }
    
    
    // --- Getters y Setters ---
    
    public void setPolitica(PoliticaPlanificacion politica) {
        this.politicaActual = politica;
        // Reiniciamos la dirección de SCAN si se cambia
        this.direccionSCAN = DireccionSCAN.ASCENDENTE;
    }
    
    public PoliticaPlanificacion getPoliticaActual() {
        return politicaActual;
    }
    
    public ListaSimple<SolicitudIO> getColaSolicitudes() {
        return colaSolicitudes;
    }
    
    public int getCabezaActual() {
        return cabezaActual;
    }
    /**
     * Método puente para recibir el String del ComboBox de la GUI
     * y convertirlo al Enum interno.
     */
    public void setAlgoritmo(String nombreAlgoritmo) {
        switch (nombreAlgoritmo) {
            case "FIFO":
                this.politicaActual = PoliticaPlanificacion.FIFO;
                break;
            case "SSTF":
                this.politicaActual = PoliticaPlanificacion.SSTF;
                break;
            case "SCAN":
                this.politicaActual = PoliticaPlanificacion.SCAN;
                break;
            case "C-SCAN":
                this.politicaActual = PoliticaPlanificacion.CSCAN;
                break;
            default:
                System.out.println("Algoritmo no reconocido: " + nombreAlgoritmo);
                break;
        }
        // Reiniciamos dirección por si acaso
        this.direccionSCAN = DireccionSCAN.ASCENDENTE; 
    }
}