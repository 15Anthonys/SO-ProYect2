/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;
import Modelo.Directorio;
import Modelo.EntradaSistemaArchivos;
import Modelo.Archivo;

/**
 * Representa una única solicitud de E/S (lectura, escritura, etc.)
 * que se encolará en el Planificador de Disco.
 */
public class SolicitudIO {
    
    private final Proceso proceso;
    private final TipoOperacion operacion;
    
    // Parámetros (pueden ser null dependiendo de la operación)
    private final String nombreNuevo;
    private final Directorio padre;
    private final int tamanoArchivo;
    private final EntradaSistemaArchivos entradaAEliminar;
    
    // Campo clave para los algoritmos SSTF, SCAN, etc.
    private final int posicionEnDisco;

    // ----- Constructores -----

    /**
     * Constructor para CREAR ARCHIVO
     */
    public SolicitudIO(Proceso proceso, Directorio padre, String nombreNuevo, int tamano) {
        this.proceso = proceso;
        this.operacion = TipoOperacion.CREAR_ARCHIVO;
        this.padre = padre;
        this.nombreNuevo = nombreNuevo;
        this.tamanoArchivo = tamano;
        this.entradaAEliminar = null;
        
        // HACK DE SIMULACIÓN:
        // Una operación 'Crear' no tiene un bloque_inicio todavía.
        // Para que SSTF funcione, le asignamos una posición.
        // Usar '0' es una opción. Otra es usar el bloque del padre,
        // pero nuestro modelo de Directorio no tiene bloque.
        // Asignaremos 0 por simplicidad.
        this.posicionEnDisco = 0; 
    }

    /**
     * Constructor para CREAR DIRECTORIO
     */
    public SolicitudIO(Proceso proceso, Directorio padre, String nombreNuevo) {
        this.proceso = proceso;
        this.operacion = TipoOperacion.CREAR_DIRECTORIO;
        this.padre = padre;
        this.nombreNuevo = nombreNuevo;
        this.tamanoArchivo = 0;
        this.entradaAEliminar = null;
        
        // HACK DE SIMULACIÓN: Directorio no tiene bloque, se asigna 0.
        this.posicionEnDisco = 0;
    }

    /**
     * Constructor para ELIMINAR (Archivo o Directorio)
     */
    public SolicitudIO(Proceso proceso, EntradaSistemaArchivos entradaAEliminar) {
        this.proceso = proceso;
        this.entradaAEliminar = entradaAEliminar;
        this.padre = null;
        this.nombreNuevo = null;
        this.tamanoArchivo = 0;
        
        if (entradaAEliminar instanceof Archivo) {
            this.operacion = TipoOperacion.ELIMINAR_ARCHIVO;
            // ¡Posición real! Usamos el primer bloque del archivo.
            this.posicionEnDisco = ((Archivo) entradaAEliminar).getPrimerBloque();
        } else {
            this.operacion = TipoOperacion.ELIMINAR_DIRECTORIO;
            // HACK DE SIMULACIÓN: Directorio no tiene bloque, se asigna 0.
            this.posicionEnDisco = 0;
        }
    }

    // ----- Getters -----
    // (Necesarios para que el Planificador pueda leer los datos)

    public Proceso getProceso() {
        return proceso;
    }

    public TipoOperacion getOperacion() {
        return operacion;
    }

    public String getNombreNuevo() {
        return nombreNuevo;
    }

    public Directorio getPadre() {
        return padre;
    }

    public int getTamanoArchivo() {
        return tamanoArchivo;
    }

    public EntradaSistemaArchivos getEntradaAEliminar() {
        return entradaAEliminar;
    }

    public int getPosicionEnDisco() {
        return posicionEnDisco;
    }
}
