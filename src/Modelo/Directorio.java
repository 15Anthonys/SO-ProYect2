/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;
import EstructuraDeDatos.ListaSimple;
import EstructuraDeDatos.Nodo;

/**
 * Representa un Directorio (una carpeta).
 * Contiene una lista de hijos (Archivos u otros Directorios).
 */
public class Directorio extends EntradaSistemaArchivos {
    
    // ¡Aquí usamos tu ListaSimple!
    private final ListaSimple<EntradaSistemaArchivos> hijos;

    public Directorio(String nombre, Directorio padre) {
        super(nombre, padre);
        this.hijos = new ListaSimple<>();
    }

    public ListaSimple<EntradaSistemaArchivos> getHijos() {
        return hijos;
    }
    
    /**
     * Agrega una nueva entrada (archivo o dir) a este directorio.
     * @param entrada El Archivo o Directorio a agregar.
     */
    public void agregarHijo(EntradaSistemaArchivos entrada) {
        // Usamos el método de tu ListaSimple
        hijos.addAtTheEnd(entrada); 
        entrada.setPadre(this);
    }
    
    /**
     * Elimina una entrada de este directorio por su nombre.
     * @param nombre El nombre del archivo o dir a eliminar.
     */
    public void eliminarHijo(String nombre) {
        // Obtenemos el primer nodo usando tu método getpFirst
        Nodo<EntradaSistemaArchivos> actual = hijos.getpFirst();
        EntradaSistemaArchivos aEliminar = null;
        
        while(actual != null) {
            // Usamos getData() y getPnext()
            if (actual.getData().getNombre().equals(nombre)) {
                aEliminar = actual.getData();
                break;
            }
            actual = actual.getPnext();
        }
        
        if (aEliminar != null) {
            // Usamos el método delete() de tu ListaSimple
            hijos.delete(aEliminar); 
        }
    }
    
    /**
     * Busca un hijo por su nombre.
     * @param nombre El nombre del archivo o dir a buscar.
     * @return La EntradaSistemaArchivos si se encuentra, o null.
     */
    public EntradaSistemaArchivos buscarHijo(String nombre) {
        Nodo<EntradaSistemaArchivos> actual = hijos.getpFirst();
        while(actual != null) {
            if (actual.getData().getNombre().equals(nombre)) {
                return actual.getData();
            }
            actual = actual.getPnext();
        }
        return null; // No encontrado
    }
    
    /**
     * Revisa si este directorio es un ancestro de 'potencialDescendiente'.
     * Útil para evitar mover un directorio dentro de sí mismo (ej. mover /docs a /docs/fotos)
     */
    public boolean esAncestro(Directorio potencialDescendiente) {
        Directorio actual = potencialDescendiente.getPadre();
        while (actual != null) {
            if (actual.equals(this)) {
                return true;
            }
            actual = actual.getPadre();
        }
        return false;
    }
}
