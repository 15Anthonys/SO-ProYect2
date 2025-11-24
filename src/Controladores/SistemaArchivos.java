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
    
    /**
     * Modifica nombre y/o tamaño de un archivo existente.
     * Maneja la lógica de crecer (asignar más) o encoger (liberar cola).
     */
    public boolean modificarArchivo(Modelo.Archivo archivo, String nuevoNombre, int nuevoTamano) {
        
        // 1. CAMBIO DE NOMBRE (Si es diferente)
        if (nuevoNombre != null && !nuevoNombre.isEmpty() && !nuevoNombre.equals(archivo.getNombre())) {
            // Actualizar tabla hash (Borrar entrada vieja, poner nueva)
            tablaDeRutas.eliminar(archivo.getRutaCompleta().toLowerCase());
            
            // Cambiar nombre en el objeto
            archivo.setNombre(nuevoNombre);
            
            // Insertar con nueva ruta
            tablaDeRutas.insertar(archivo.getRutaCompleta().toLowerCase(), archivo);
        }

        // 2. CAMBIO DE TAMAÑO (Si el usuario pidió cambiarlo)
        int tamanoActual = archivo.getTamanoEnBloques();
        
        if (nuevoTamano > 0 && nuevoTamano != tamanoActual) {
            
            // CASO A: EL ARCHIVO CRECE (Necesitamos más bloques)
            if (nuevoTamano > tamanoActual) {
                int bloquesNecesarios = nuevoTamano - tamanoActual;
                
                // Verificar espacio
                if (bloquesNecesarios > gestorDisco.getCantidadBloquesLibres()) {
                    System.err.println("Error: No hay espacio suficiente para agrandar el archivo.");
                    return false;
                }
                
                // Asignar los nuevos bloques (nos devuelve el inicio de la nueva cadena)
                // Usamos la ruta actual como propietario
                int inicioNuevaCadena = gestorDisco.asignarBloques(bloquesNecesarios, archivo.getRutaCompleta());
                
                if (inicioNuevaCadena != -1) {
                    // TENEMOS QUE UNIR EL FINAL ACTUAL CON EL INICIO NUEVO
                    // Recorremos el archivo hasta el último bloque actual
                    int ultimoBloque = obtenerUltimoBloque(archivo.getPrimerBloque());
                    
                    // Enlazamos
                    gestorDisco.getDisco()[ultimoBloque].setSiguienteBloque(inicioNuevaCadena);
                    
                    // Actualizamos el tamaño en el objeto
                    archivo.setTamanoEnBloques(nuevoTamano);
                }

            // CASO B: EL ARCHIVO SE ENCOGE (Liberamos sobras)
            } else {
                int bloquesAConservar = nuevoTamano;
                
                // Navegamos hasta el bloque que será el NUEVO final
                int bloqueActual = archivo.getPrimerBloque();
                for (int i = 1; i < bloquesAConservar; i++) {
                    bloqueActual = gestorDisco.getDisco()[bloqueActual].getSiguienteBloque();
                }
                
                // El siguiente de este bloque es donde empieza la basura a borrar
                int primerBloqueBorrar = gestorDisco.getDisco()[bloqueActual].getSiguienteBloque();
                
                // Cortamos la cadena (Ahora este es el fin)
                gestorDisco.getDisco()[bloqueActual].setSiguienteBloque(-1);
                
                // Liberamos el resto
                gestorDisco.liberarBloques(primerBloqueBorrar);
                
                // Actualizamos tamaño
                archivo.setTamanoEnBloques(nuevoTamano);
            }
        }
        return true;
    }

    // Método auxiliar para encontrar el final de una cadena de bloques
    private int obtenerUltimoBloque(int primerBloque) {
        int actual = primerBloque;
        while(true) {
            int siguiente = gestorDisco.getDisco()[actual].getSiguienteBloque();
            if (siguiente == -1) {
                return actual;
            }
            actual = siguiente;
        }
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
