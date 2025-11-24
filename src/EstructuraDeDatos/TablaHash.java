/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EstructuraDeDatos;

/**
 *
 * @author luisg
 */
public class TablaHash <T> {
    private final int TAMANO_TABLA_DEFECTO = 256;
    private int tamanoTabla;
    
    // Array de entradas (buckets)
    // ¡¡¡ARREGLO AQUÍ!!! Se quitó la palabra 'final'
    @SuppressWarnings("unchecked")
    private EntradaTablaHash<T>[] tabla;

    // Lista de todas las entradas (útil para iterar todo)
    private final ListaSimple <T> listaDeEntradas;


    @SuppressWarnings("unchecked")
    public TablaHash() {
        // No se puede crear un array de genéricos directamente
        this.tabla = new EntradaTablaHash[this.TAMANO_TABLA_DEFECTO];
        this.listaDeEntradas = new ListaSimple<>();
        this.actualizarTamanoTabla();
    }

    public ListaSimple<T> getListaDeEntradas() {
        return this.listaDeEntradas;
    }

    /**
     * Inserta un par (llave, valor) en la tabla.
     * @param llave La llave (String), ej: "/root/docs"
     * @param valor El valor (T), ej: el objeto Directorio
     */
    public void insertar(String llave, T valor) {
        if (this.estaLlaveOcupada(llave)) {
            System.out.println("La llave " + llave + " ya esta tomada, por favor escoja otra.");
            return;
        }
        
        String llaveMinuscula = llave.toLowerCase();
        int hashCalculado = llaveMinuscula.hashCode();
        int indice = Math.abs(funcionHash(hashCalculado));
        
        // Constructor de EntradaTablaHash: (String llave, int hash, T valor)
        EntradaTablaHash<T> nuevaEntrada = new EntradaTablaHash<>(llaveMinuscula, hashCalculado, valor);

        if (tabla[indice] == null) {
            // No hay colisión
            tabla[indice] = nuevaEntrada;
        } else {
            // Hay colisión, vamos al final
            EntradaTablaHash<T> actual = this.tabla[indice];
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevaEntrada);
        }
        
        // Método de ListaSimple: addAtTheEnd(T data)
        this.listaDeEntradas.addAtTheEnd(valor);
    }

    /**
     * Obtiene un valor usando su llave.
     * @param llave La llave (String) a buscar.
     * @return El valor (T) asociado, o null si no se encuentra.
     */
    public T obtener(String llave) {
        String llaveMinuscula = llave.toLowerCase();
        int hashCalculado = llaveMinuscula.hashCode();
        int indice = Math.abs(funcionHash(hashCalculado));

        EntradaTablaHash<T> entrada = this.tabla[indice];
        while (entrada != null) {
            // Métodos de EntradaTablaHash: getHash() y getLlave()
            if (entrada.getHash() == hashCalculado && entrada.getLlave().equals(llaveMinuscula)) {
                return entrada.getValor();
            }
            entrada = entrada.getSiguiente();
        }
        return null; // No se encontró
    }

    /**
     * Elimina una entrada de la tabla usando su llave.
     * @param llave La llave (String) a eliminar.
     */
    public void eliminar(String llave) {
        String llaveMinuscula = llave.toLowerCase();
        int hashCalculado = llaveMinuscula.hashCode();
        int indice = Math.abs(funcionHash(hashCalculado));

        if (this.tabla[indice] == null) {
            System.out.println("No hay elemento asociado con la llave: " + llaveMinuscula);
            return;
        }

        EntradaTablaHash<T> actual = this.tabla[indice];
        EntradaTablaHash<T> anterior = null;

        while (actual != null) {
            if (actual.getLlave().equals(llaveMinuscula)) {
                // Encontrado.
                if (anterior == null) {
                    this.tabla[indice] = actual.getSiguiente();
                } else {
                    anterior.setSiguiente(actual.getSiguiente());
                }

                // Método de ListaSimple: delete(T data)
                this.listaDeEntradas.delete(actual.getValor());
                return;
            }
            anterior = actual;
            actual = actual.getSiguiente();
        }
        
        System.out.println("No hay elemento asociado con la llave: " + llaveMinuscula);
    }

    /**
     * Función hash simple.
     */
    private int funcionHash(int llave) {
        return llave % this.tamanoTabla;
    }

    private void actualizarTamanoTabla() {
        this.tamanoTabla = this.tabla.length;
    }

    /**
     * Verifica si una llave ya existe en la tabla.
     * @param llave La llave (String) a verificar.
     * @return true si ya existe, false si no.
     */
    public boolean estaLlaveOcupada(String llave) {
        // Reutilizamos el método 'obtener' que ya está corregido
        return obtener(llave) != null;
    }

    /**
     * Vacía la tabla hash.
     * (Ahora funciona porque 'tabla' ya no es 'final')
     */
    @SuppressWarnings("unchecked")
    public void limpiar() {
        this.tabla = new EntradaTablaHash[this.TAMANO_TABLA_DEFECTO];
        
        // Método de ListaSimple: wipeList()
        this.listaDeEntradas.wipeList();
        this.actualizarTamanoTabla();
    }
}
