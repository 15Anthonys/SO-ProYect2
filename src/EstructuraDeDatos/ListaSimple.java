/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EstructuraDeDatos;

/**
 * Clase para implementar una lista simple enlazada.
 * @param <T> Tipo de dato a almacenar en la lista.
 */
public class ListaSimple<T> {

    private Nodo<T> pFirst;
    private Nodo<T> pLast;
    private int size;

    public ListaSimple() {
        this.pFirst = null;
        this.pLast = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return this.pFirst == null;
    }

    // Añadir al inicio
    public void addStart(T data) {
        Nodo<T> node = new Nodo<>(data);

        if (this.isEmpty()) {
            this.pFirst = node;
            this.pLast = node;
        } else {
            node.setPnext(this.pFirst);
            this.pFirst = node;
        }

        this.size++;
    }

    // Añadir al final
    public void addAtTheEnd(T data) {
        Nodo<T> node = new Nodo<>(data);
        if (this.isEmpty()) {
            this.pFirst = node;
            this.pLast = node;
        } else {
            this.pLast.setPnext(node);
            this.pLast = node;
        }
        this.size++;
    }

    // Recorrer hasta nulo
    public void printList() {
        if (this.isEmpty()) {
            System.out.println("La lista está vacía");
        } else {
            try {
                Nodo<T> pAux = this.pFirst;
                System.out.println("Lista==============================");
                while (pAux != null) {
                    System.out.println(pAux.getData());
                    pAux = pAux.getPnext();
                }
                System.out.println("===================================");
            } catch (Exception e) {
                System.out.println("Error al imprimir.");
            }
        }
    }

    // Devuelve una cadena que representa la lista
    public String printToString() {
        if (this.isEmpty()) {
            return "//";
        }
        Nodo<T> pAux = this.pFirst;
        String chain = "";
        while (pAux != null) {
            chain += pAux.getData() + "->";
            pAux = pAux.getPnext();
        }
        return chain + "//";
    }

    public T getValueByIndex(int index) {
        Nodo<T> pAux = this.pFirst;
        int count = 0;

        while (pAux != null && count != index) {
            pAux = pAux.getPnext();
            count++;
        }

        if (pAux != null) {
            return pAux.getData();
        } else {
            return null;
        }
    }

    public boolean contains(T data) {
        Nodo<T> pAux = this.pFirst;

        while (pAux != null) {
            if (pAux.getData().equals(data)) {
                return true;
            }
            pAux = pAux.getPnext();
        }

        return false;
    }

    public Nodo<T> searchByValue(T value) {
        Nodo<T> pAux = this.pFirst;

        while (pAux != null && !pAux.getData().equals(value)) {
            pAux = pAux.getPnext();
        }

        return pAux; // Retorna null si no se encuentra
    }

    // Método para retornar la posición de un elemento en la lista
    public int indexOf(T valorBuscado) {
        Nodo<T> actual = this.pFirst;
        int index = 0;

        while (actual != null) {
            if (actual.getData().equals(valorBuscado)) {
                return index;
            }

            actual = actual.getPnext();
            index++;
        }

        return -1; // Si no se encuentra el valor, devolvemos -1
    }

    public void deleteFirst() {
        if (this.isEmpty()) {
            System.out.println("Lista vacía.");
        } else {
            pFirst = pFirst.getPnext();
            this.size--;
        }
    }

    public void deleteLast() {
        if (this.pFirst == this.pLast) {
            this.pFirst = null;
            this.pLast = null;
            this.size = 0;
        } else {
            Nodo<T> pAux = this.pFirst;

            while (pAux.getPnext().getPnext() != null) {
                pAux = pAux.getPnext();
            }

            pAux.setPnext(null);
            this.pLast = pAux;
            this.size--;
        }
    }

    public boolean isValidIndex(int index) {
        return index >= 0 && index < this.size; // Verifica que el índice sea válido
    }

    public void insertAtIndex(int index, T data) {
        if (index < 0 || index > this.size) {
            System.out.println("No existe el índice");
            return;
        }

        if (index == 0) {
            addStart(data);
        } else {
            Nodo<T> pAux = this.pFirst;
            int counter = 0;

            while (counter < index - 1) {
                pAux = pAux.getPnext();
                counter++;
            }

            Nodo<T> node = new Nodo<>(data);
            node.setPnext(pAux.getPnext());
            pAux.setPnext(node);

            if (node.getPnext() == null) {
                this.pLast = node; // Actualiza pLast si se añadió al final
            }

            this.size++;
        }
    }

    // Elimina un elemento según su índice en la lista (posición)
    public void deleteByIndex(int index) {
        if (!this.isEmpty()) {
            if (index == 0) {
                deleteFirst();
            } else if (index < this.size) {
                Nodo<T> pAux = this.pFirst;
                int count = 0;
                while (count < (index - 1)) {
                    pAux = pAux.getPnext();
                    count++;
                }
                Nodo<T> temporal = pAux.getPnext();
                pAux.setPnext(temporal.getPnext());
                temporal.setPnext(null);
                this.size--;
            }
        }
    }

    public void wipeList() {
        this.pFirst = null;
        this.pLast = null;
        this.size = 0;
    }

    public Object[] toArray() {
        Object[] array = new Object[this.size];
        Nodo<T> pAux = this.pFirst;

        for (int i = 0; i < this.size; i++) {
            array[i] = pAux.getData();
            pAux = pAux.getPnext();
        }

        return array;
    }

    public void delete(T data) {
        Nodo<T> currentNode = this.pFirst;
        Nodo<T> previousNode = null;

        while (currentNode != null && !currentNode.getData().equals(data)) {
            previousNode = currentNode;
            currentNode = currentNode.getPnext();
        }

        if (currentNode != null) {
            if (previousNode == null) {
                this.pFirst = currentNode.getPnext();
            } else {
                previousNode.setPnext(currentNode.getPnext());
            }
            this.size--;
        }
    }

    /*
        Getters y Setters
     */
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

