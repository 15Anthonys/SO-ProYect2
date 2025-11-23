/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;
import EstructuraDeDatos.ListaSimple;
import Modelo.Bloque;

/**
 * Simula el disco duro (SD).
 * Administra un array de Bloques y una lista de bloques libres.
 */
public class GestorDisco {
    
    private final Bloque[] disco;
    private final ListaSimple<Integer> listaBloquesLibres; // ¡Tu ListaSimple!
    private final int tamanoTotal;

    public GestorDisco(int tamanoTotal) {
        this.tamanoTotal = tamanoTotal;
        this.disco = new Bloque[tamanoTotal];
        this.listaBloquesLibres = new ListaSimple<>();
        
        // Inicializar el disco y la lista de libres
        for (int i = 0; i < tamanoTotal; i++) {
            this.disco[i] = new Bloque();
            // Usamos tu método addAtTheEnd
            this.listaBloquesLibres.addAtTheEnd(i); // Agregamos el *índice*
        }
    }

    /**
     * Busca, ocupa y enlaza una cantidad de bloques libres.
     * @param cantidad El número de bloques a asignar.
     * @param propietario La ruta del archivo que será dueño de estos bloques.
     * @return El índice del primer bloque asignado, o -1 si no hay espacio.
     */
    public int asignarBloques(int cantidad, String propietario) {
        // ¡¡IMPORTANTE!! Esto asume que agregaste getSize() a tu ListaSimple
        if (cantidad > listaBloquesLibres.getSize()) {
            System.err.println("Error: No hay suficiente espacio en disco.");
            return -1; // No hay espacio
        }

        int primerBloque = -1;
        int bloqueAnterior = -1;

        for (int i = 0; i < cantidad; i++) {
            // 1. "Sacamos" el primer bloque libre de la lista
            int indiceBloqueLibre = listaBloquesLibres.getpFirst().getData();
            listaBloquesLibres.delete(indiceBloqueLibre); // Usamos tu delete
            
            // 2. Obtenemos el bloque del disco y lo ocupamos
            Bloque bloqueActual = disco[indiceBloqueLibre];
            bloqueActual.ocupar(propietario);

            if (i == 0) {
                // 3. Si es el primero, guardamos su índice
                primerBloque = indiceBloqueLibre;
            } else {
                // 4. Si no, enlazamos el bloque anterior con este
                disco[bloqueAnterior].setSiguienteBloque(indiceBloqueLibre);
            }
            
            // 5. Nos preparamos para la siguiente iteración
            bloqueAnterior = indiceBloqueLibre;
        }

        // El último bloque de la cadena apunta a -1 (fin)
        if (bloqueAnterior != -1) {
            disco[bloqueAnterior].setSiguienteBloque(-1);
        }

        return primerBloque;
    }

    /**
     * Libera una cadena de bloques enlazados, comenzando por el primero.
     * @param primerBloque El índice del primer bloque de la cadena a liberar.
     */
    public void liberarBloques(int primerBloque) {
        int indiceActual = primerBloque;
        
        while (indiceActual != -1) {
            Bloque bloqueActual = disco[indiceActual];
            
            // Guardamos el siguiente antes de borrar la referencia
            int indiceSiguiente = bloqueActual.getSiguienteBloque();
            
            // Liberamos
            bloqueActual.liberar();
            
            // --- CAMBIO AQUÍ: Insertamos ordenadamente ---
            insertarBloqueLibreOrdenado(indiceActual);
            // ---------------------------------------------
            
            indiceActual = indiceSiguiente;
        }
    }
    
    private void insertarBloqueLibreOrdenado(int indiceAInsertar) {
        // 1. Si la lista está vacía o el índice es menor que el primero, va al inicio
        if (listaBloquesLibres.isEmpty() || 
            indiceAInsertar < listaBloquesLibres.getpFirst().getData()) {
            listaBloquesLibres.addStart(indiceAInsertar);
            return;
        }

        // 2. Buscar la posición correcta (antes del primer número que sea mayor que el nuestro)
        EstructuraDeDatos.Nodo<Integer> actual = listaBloquesLibres.getpFirst();
        int posicion = 0;
        
        // Recorremos mientras haya nodos y el valor sea menor al que queremos insertar
        while (actual != null && actual.getData() < indiceAInsertar) {
            actual = actual.getPnext();
            posicion++;
        }
        
        // 3. Insertamos en esa posición específica
        // Nota: Si 'actual' es null, significa que llegamos al final (es el mayor de todos)
        if (actual == null) {
            listaBloquesLibres.addAtTheEnd(indiceAInsertar);
        } else {
            listaBloquesLibres.insertAtIndex(posicion, indiceAInsertar);
        }
    }
    

    /**
     * Devuelve el array completo de bloques (para la GUI).
     */
    public Bloque[] getDisco() {
        return disco;
    }

    /**
     * Devuelve cuántos bloques libres quedan.
     * (Asume que agregaste getSize() a ListaSimple)
     */
    public int getCantidadBloquesLibres() {
        return listaBloquesLibres.getSize(); 
    }
    
    public int getTamanoTotal() {
        return tamanoTotal;
    }
}