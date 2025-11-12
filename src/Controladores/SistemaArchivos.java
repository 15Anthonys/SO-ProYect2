/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;
// Importamos tus estructuras y nuestras clases de Modelo
import EstructuraDeDatos.TablaHash;
import EstructuraDeDatos.ListaSimple;
import EstructuraDeDatos.Nodo;
import Modelo.Archivo;
import Modelo.Directorio;
import Modelo.EntradaSistemaArchivos;

/**
 * Cerebro principal del simulador.
 * Mantiene la estructura de directorios (árbol) y una tabla hash 
 * para acceso rápido por ruta.
 */
public class SistemaArchivos {
    
    private final GestorDisco gestorDisco;
    private final Directorio directorioRaiz;
    
    // ¡Aquí usamos tu TablaHash!
    // La clave será la ruta completa (String), ej: "/root/docs/mi.txt"
    // El valor será el objeto (Archivo o Directorio)
    private final TablaHash<EntradaSistemaArchivos> tablaDeRutas;

    public SistemaArchivos(int tamanoDisco) {
        this.gestorDisco = new GestorDisco(tamanoDisco);
        // Creamos el directorio raíz, no tiene padre (null)
        this.directorioRaiz = new Directorio("root", null);
        this.tablaDeRutas = new TablaHash<>();
        
        // Agregamos la raíz a la tabla hash
        // Usamos tu método 'insertar'
        this.tablaDeRutas.insertar("/root", directorioRaiz);
    }
    
    /**
     * Obtiene una entrada (Archivo o Directorio) usando su ruta completa.
     * @param ruta La ruta completa, ej: "/root/docs"
     * @return La entrada, o null si no existe.
     */
    public EntradaSistemaArchivos getEntrada(String ruta) {
        // Usamos tu método 'obtener'
        return tablaDeRutas.obtener(ruta.toLowerCase());
    }

    /**
     * Crea un nuevo archivo.
     */
    public Archivo crearArchivo(String nombre, int tamanoEnBloques, Directorio padre) {
        String nuevaRuta = padre.getRutaCompleta().equals("/root") ? 
                           "/root/" + nombre : 
                           padre.getRutaCompleta() + "/" + nombre;
        
        // Validaciones
        // Usamos tu método 'estaLlaveOcupada'
        if (tablaDeRutas.estaLlaveOcupada(nuevaRuta.toLowerCase())) {
            System.err.println("Error: Ya existe un archivo o directorio con ese nombre.");
            return null;
        }
        if (tamanoEnBloques > gestorDisco.getCantidadBloquesLibres()) {
            System.err.println("Error: Espacio insuficiente en disco.");
            return null;
        }

        // 1. Asignar bloques en disco
        int primerBloque = gestorDisco.asignarBloques(tamanoEnBloques, nuevaRuta);
        if (primerBloque == -1) {
            return null; // Falló la asignación
        }

        // 2. Crear el objeto Archivo
        Archivo nuevoArchivo = new Archivo(nombre, padre, tamanoEnBloques, primerBloque);
        
        // 3. Añadir a la jerarquía (lista del padre)
        padre.agregarHijo(nuevoArchivo);
        
        // 4. Añadir a la tabla hash para búsqueda rápida
        tablaDeRutas.insertar(nuevaRuta.toLowerCase(), nuevoArchivo);
        
        return nuevoArchivo;
    }

    /**
     * Crea un nuevo directorio.
     */
    public Directorio crearDirectorio(String nombre, Directorio padre) {
        String nuevaRuta = padre.getRutaCompleta().equals("/root") ? 
                           "/root/" + nombre : 
                           padre.getRutaCompleta() + "/" + nombre;

        // Validación
        if (tablaDeRutas.estaLlaveOcupada(nuevaRuta.toLowerCase())) {
            System.err.println("Error: Ya existe un archivo o directorio con ese nombre.");
            return null;
        }
        
        // 1. Crear el objeto Directorio
        Directorio nuevoDir = new Directorio(nombre, padre);
        
        // 2. Añadir a la jerarquía (lista del padre)
        padre.agregarHijo(nuevoDir);
        
        // 3. Añadir a la tabla hash
        tablaDeRutas.insertar(nuevaRuta.toLowerCase(), nuevoDir);
        
        return nuevoDir;
    }

    /**
     * Elimina un archivo del sistema.
     */
    public void eliminarArchivo(Archivo archivo) {
        // 1. Liberar bloques en disco
        gestorDisco.liberarBloques(archivo.getPrimerBloque());
        
        // 2. Quitar de la tabla hash
        // Usamos tu método 'eliminar'
        tablaDeRutas.eliminar(archivo.getRutaCompleta().toLowerCase());
        
        // 3. Quitar de la jerarquía (lista del padre)
        archivo.getPadre().eliminarHijo(archivo.getNombre());
    }

    /**
     * Elimina un directorio (y todo su contenido) recursivamente.
     */
    public boolean eliminarDirectorio(Directorio directorio) {
        if (directorio.equals(directorioRaiz)) {
            System.err.println("Error: No se puede eliminar el directorio raíz.");
            return false;
        }

        // 1. Hacemos una copia de la lista de hijos antes de iterar,
        // porque vamos a modificar la lista original al borrar.
        ListaSimple<EntradaSistemaArchivos> hijos = directorio.getHijos();
        ListaSimple<EntradaSistemaArchivos> copiaHijos = new ListaSimple<>();
        Nodo<EntradaSistemaArchivos> nodoActual = hijos.getpFirst();
        while(nodoActual != null) {
            copiaHijos.addAtTheEnd(nodoActual.getData());
            nodoActual = nodoActual.getPnext();
        }

        // 2. Ahora iteramos sobre la copia y eliminamos
        nodoActual = copiaHijos.getpFirst();
        while (nodoActual != null) {
            EntradaSistemaArchivos hijo = nodoActual.getData();
            if (hijo instanceof Archivo) {
                eliminarArchivo((Archivo) hijo);
            } else if (hijo instanceof Directorio) {
                eliminarDirectorio((Directorio) hijo);
            }
            nodoActual = nodoActual.getPnext();
        }

        // 3. Quitar el directorio (ya vacío) de la tabla hash
        tablaDeRutas.eliminar(directorio.getRutaCompleta().toLowerCase());
        
        // 4. Quitar de la jerarquía (lista del padre)
        directorio.getPadre().eliminarHijo(directorio.getNombre());
        
        return true;
    }
    
    // --- Getters ---

    public Directorio getDirectorioRaiz() {
        return directorioRaiz;
    }

    public GestorDisco getGestorDisco() {
        return gestorDisco;
    }
    
    public TablaHash<EntradaSistemaArchivos> getTablaDeRutas() {
        return tablaDeRutas;
    }
}
