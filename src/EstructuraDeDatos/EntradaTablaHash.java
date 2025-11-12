/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EstructuraDeDatos;

/**
 *
 * @author luisg
 */
public class EntradaTablaHash<T> {
    private final String llave ; // La llave original (ej: "/root/docs/mi.txt")
    private final int hash;
    private T valor;    // El valor (ej: el objeto Archivo)
    private EntradaTablaHash<T> siguiente; // Para encadenamiento

    public EntradaTablaHash(String llave, int hash, T valor) {
        this.llave = llave;
        this.hash = hash;
        this.valor = valor;
        this.siguiente = null;
    }

    // --- Getters y Setters ---
    
    public String getLlave() {
        return llave;
    }

    public int getHash() {
        return hash;
    }

    public T getValor() {
        return valor;
    }

    public void setValor(T valor) {
        this.valor = valor;
    }

    public EntradaTablaHash<T> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(EntradaTablaHash<T> siguiente) {
        this.siguiente = siguiente;
    }
}
