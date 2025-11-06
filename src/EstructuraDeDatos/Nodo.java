package EstructuraDeDatos;

/**
 * Clase Nodo genérico para almacenar cualquier tipo de dato.
 * @param <T> Tipo de dato a almacenar en el nodo.
 */
public class Nodo<T> {
    
    private Nodo<T> pNext; // Nodo siguiente
    private T data; // Datos del nodo

    // Constructor
    public Nodo(T data) {
        this.pNext = null; // Inicializa el siguiente nodo como null
        this.data = data;  // Asigna el dato al nodo
    }

    // Método para obtener el siguiente nodo
    public Nodo<T> getPnext() {
        return pNext;
    }

    // Método para establecer el siguiente nodo
    public void setPnext(Nodo<T> pnext) {
        this.pNext = pnext;
    }

    // Método para obtener los datos del nodo
    public T getData() {
        return data;
    }

    // Método para establecer los datos del nodo
    public void setData(T data) {
        this.data = data;
    }
}
