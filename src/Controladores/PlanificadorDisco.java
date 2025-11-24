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
    private final EstructuraDeDatos.ListaSimple<SolicitudIO> colaNuevos;
    
    private final EstructuraDeDatos.ListaSimple<SolicitudIO> listaTerminados;
    
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
        this.listaTerminados = new EstructuraDeDatos.ListaSimple<>();
        this.colaNuevos = new EstructuraDeDatos.ListaSimple<>();
    }
    
    public void agregarSolicitudNUEVA(SolicitudIO solicitud) {
        solicitud.getProceso().setEstado(EstadoProceso.NUEVO); // Marcar como NUEVO
        this.colaNuevos.addAtTheEnd(solicitud);
    }
    
    /**
     * Mueve todos los procesos de la cola de NUEVOS a la cola de LISTOS.
     */
    public void transitarNuevosAListos() {
        // Mientras la cola de nuevos tenga gente...
        while (!colaNuevos.isEmpty()) {
            // 1. Sacar el primero de Nuevos
            SolicitudIO sol = colaNuevos.getpFirst().getData();
            colaNuevos.deleteFirst(); 
            
            // 2. Cambiar su estado a LISTO
            sol.getProceso().setEstado(EstadoProceso.LISTO);
            
            // 3. Meterlo en la cola principal (Listos)
            // Usamos tu método existente o accedemos a la lista directa
            colaSolicitudes.addAtTheEnd(sol);
        }
    }

    /**
     * La GUI llamará a este método para añadir una nueva "tarea"
     */
    public void agregarSolicitud(SolicitudIO solicitud) {
        //olicitud.getProceso().setEstado(EstadoProceso.BLOQUEADO);
        this.colaSolicitudes.addAtTheEnd(solicitud);
    }
    
    public EstructuraDeDatos.ListaSimple<SolicitudIO> getColaNuevos() {
        return colaNuevos;
    }

    /**
     * El método principal. Decide qué solicitud tomar, la ejecuta
     * y la elimina de la cola.
     */
    /**
     * MODIFICADO: Ahora devuelve la SolicitudIO procesada para que la GUI
     * sepa qué mostrar en la tarjeta.
     */
    public SolicitudIO procesarSiguienteSolicitud() {
        if (colaSolicitudes.isEmpty()) {
            return null; // Antes era false, ahora es null
        }

        // 1. Encontrar la siguiente solicitud según la política
        SolicitudIO solicitud = buscarSiguienteSolicitud();
        
        // Si no se encontró ninguna (ej. listas vacías en SCAN)
        if (solicitud == null) {
            return null; // Antes era false
        }

        // 2. Ejecutar la operación
        ejecutarSolicitud(solicitud);
        
        // 3. Actualizar estado del cabezal y del proceso
        this.cabezaActual = solicitud.getPosicionEnDisco();
        solicitud.getProceso().setEstado(EstadoProceso.TERMINADO);

        // 4. Eliminar la solicitud de la lista
        colaSolicitudes.delete(solicitud); 
        
        listaTerminados.addStart(solicitud);
        
        // 5. ¡AQUÍ ESTÁ LA CLAVE! Devolvemos el objeto
        return solicitud; // Antes era return true;
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
    /**
     * Ejecuta la operación solicitada.
     * @return TRUE si la operación fue exitosa, FALSE si falló.
     */
    private boolean ejecutarSolicitud(SolicitudIO sol) {
        try {
            switch (sol.getOperacion()) {
                case CREAR_ARCHIVO:
                    // Guardamos el objeto archivo que nos devuelve el sistema
                    Modelo.Archivo nuevoArchivo = sistemaArchivos.crearArchivo(sol.getNombreNuevo(), sol.getTamanoArchivo(), sol.getPadre());
                    
                    if (nuevoArchivo != null) {
                        // ¡AQUÍ GUARDAMOS QUIÉN LO CREÓ!
                        nuevoArchivo.setIdProcesoCreador(sol.getProceso().getId());
                        return true;
                    }
                    return false;

                case CREAR_DIRECTORIO:
                    return sistemaArchivos.crearDirectorio(sol.getNombreNuevo(), sol.getPadre()) != null;

                case ELIMINAR_ARCHIVO:
                    // Asumimos éxito si no lanza error. 
                    // Hacemos Cast a Archivo
                    sistemaArchivos.eliminarArchivo((Modelo.Archivo) sol.getEntradaAEliminar());
                    return true;

                case ELIMINAR_DIRECTORIO:
                    // eliminarDirectorio devuelve boolean en tu código, así que lo retornamos
                    return sistemaArchivos.eliminarDirectorio((Modelo.Directorio) sol.getEntradaAEliminar());

                case MODIFICAR_ARCHIVO:
                    // --- AQUÍ ESTABA EL ERROR, AHORA SÍ FUNCIONARÁ ---
                    return sistemaArchivos.modificarArchivo(
                            (Modelo.Archivo) sol.getEntradaAEliminar(), 
                            sol.getNombreNuevo(), 
                            sol.getTamanoArchivo()
                    );
                case LEER_ARCHIVO:
                // Leer no hace nada físico en el disco, solo mueve la aguja.
                // Simplemente retornamos true indicando éxito.
                // (Visualmente la tarjeta mostrará que se está leyendo).
                    return true;

                default:
                    return true;
            }
        } catch (Exception e) {
            System.out.println("Error ejecutando solicitud: " + e.getMessage());
            return false;
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
    
    
    public EstructuraDeDatos.ListaSimple<SolicitudIO> getListaTerminados() {
        return listaTerminados;
    }
}