/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EstructuraDeDatos;

/**
 *
 * @author dugla
 */
public class Cola <T>{
    
    private Nodo<T> pFirst;
    private Nodo<T> pLast;
    private int size;

    public Cola() {
        this.pFirst = null;
        this.pLast = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return this.pFirst == null;
    }

    public void clearQueue() {
        this.pFirst = null;
        this.pLast = null;
        this.size = 0;
    }
    
    

    public void insert(T data) {

        Nodo<T> newNode = new Nodo(data);

        if (this.isEmpty()) {
            this.pFirst = newNode;
            this.pLast = newNode;

        } else {
            this.pLast.setPnext(newNode);
            this.pLast = newNode;

        }

        this.size++;

    }

    public T pop() {
    
        // --- ¡ARREGLO AQUÍ! ---
        // Añade esta comprobación de 3 líneas
        if (isEmpty()) {
            return null; // Si la cola está vacía, devuelve null
        }
        // --- FIN DEL ARREGLO ---

        Nodo<T> popped = this.pFirst;
        this.pFirst = this.pFirst.getPnext(); // <-- Esta línea era el error cuando pFirst era null
        this.size--;

        // --- ARREGLO 2 (MUY IMPORTANTE) ---
        // Si la cola quedó vacía, pLast también debe ser null
        if (isEmpty()) {
            this.pLast = null; 
        }
        // --- FIN DEL ARREGLO 2 ---

        return popped.getData();
}

    public boolean remove(T data) {
        if (isEmpty()) {
            return false; // La cola está vacía
        }

        // Si el primer elemento es el que queremos eliminar
        if (pFirst.getData().equals(data)) {
            pop(); // Si es el primer elemento, simplemente lo eliminamos
            return true;
        }

        // Buscamos el nodo que contiene el dato
        Nodo<T> current = pFirst;
        while (current.getPnext() != null) {
            if (current.getPnext().getData().equals(data)) {
                // Elimina el nodo
                current.setPnext(current.getPnext().getPnext());
                if (current.getPnext() == null) { // Si estamos eliminando el último nodo
                    pLast = current; // Actualiza pLast
                }
                size--;
                return true; // Elemento eliminado
            }
            current = current.getPnext();
        }
        return false; // No se encontró el elemento
    }

    // STILL A WIP
    public ListaSimple<T> getListSortedFromQueue(Cola<T> queue) {
        ListaSimple<T> dataList = new ListaSimple<>();

        if (queue == null || queue.isEmpty()) {
            System.out.println("La cola esta vacia o no se pudo acceder a ella.");
            return dataList;
        }

        // Recorremos la cola desde el primer nodo
        Nodo<T> currentNode = queue.getpFirst();

        while (currentNode != null) {
            dataList.addAtTheEnd(currentNode.getData());
            currentNode = currentNode.getPnext();
        }

        return dataList;
    }

    public Nodo<T> getpFirst() {
        return pFirst;
    }

    public void setpFirst(Nodo<T> pFirst) {
        this.pFirst = pFirst;
    }

    public Nodo<T> getpLast() {
        return pLast;
    }

    public void setpLast(Nodo<T> pLast) {
        this.pLast = pLast;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
