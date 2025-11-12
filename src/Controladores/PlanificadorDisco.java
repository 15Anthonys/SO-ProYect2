/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;
import EstructuraDeDatos.ListaSimple;
import EstructuraDeDatos.Nodo;
import Modelo.Archivo;
/**
 *
 * @author luisg
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
        this.direccionSCAN = DireccionSCAN.ASCENDENTE;
    }

    /**
     * La GUI llamará a este método para añadir una nueva "tarea"
     */
    public void agregarSolicitud(SolicitudIO solicitud) {
        solicitud.getProceso().setEstado(EstadoProceso.BLOQUEADO);
        this.colaSolicitudes.addAtTheEnd(solicitud);
        
        // (En una simulación real, un hilo separado llamaría a procesarSiguiente())
        // Por ahora, lo podemos llamar manualmente desde la GUI.
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

        // 2. Ejecutar la operación
        ejecutarSolicitud(solicitud);
        
        // 3. Actualizar estado del cabezal y del proceso
        this.cabezaActual = solicitud.getPosicionEnDisco();
        solicitud.getProceso().setEstado(EstadoProceso.TERMINADO);

        // 4. Eliminar la solicitud de la lista
        colaSolicitudes.delete(solicitud);
        
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
                sistemaArchivos.eliminarDirectorio((Modelo.Directorio) sol.getEntradaAEliminar());
                break;
        }
    }

    // ----- ALGORITMOS DE PLANIFICACIÓN -----

    /**
     * FIFO: Simplemente toma el primero de la lista.
     */
    private SolicitudIO buscarSiguienteFIFO() {
        if (colaSolicitudes.isEmpty()) return null;
        // El primero de la lista es el primero que entró
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
            actual = actual.getPnext();
        }
        return mejorSolicitud;
    }

    /**
     * SCAN (Elevator): El cabezal se mueve en una dirección, atendiendo
     * solicitudes, hasta llegar al final, donde invierte la dirección.
     */
    private SolicitudIO buscarSiguienteSCAN() {
        // Esta lógica es compleja. Requiere buscar la más cercana
        // EN LA DIRECCIÓN ACTUAL. Si no hay, invierte la dirección.
        
        // TODO: Implementar lógica de SCAN
        // Por ahora, usamos SSTF como sustituto
        System.err.println("Advertencia: SCAN no implementado, usando SSTF.");
        return buscarSiguienteSSTF();
    }

    /**
     * C-SCAN (Circular SCAN): Igual que SCAN, pero al llegar al final,
     * salta al inicio (0) y sigue en la misma dirección.
     */
    private SolicitudIO buscarSiguienteCSCAN() {
        // TODO: Implementar lógica de C-SCAN
        // Por ahora, usamos SSTF como sustituto
        System.err.println("Advertencia: C-SCAN no implementado, usando SSTF.");
        return buscarSiguienteSSTF();
    }
    
    
    // --- Getters y Setters ---
    
    public void setPolitica(PoliticaPlanificacion politica) {
        this.politicaActual = politica;
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
}
