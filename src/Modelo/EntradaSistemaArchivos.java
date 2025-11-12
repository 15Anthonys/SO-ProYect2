/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author luisg
 */
public abstract class EntradaSistemaArchivos {
    protected String nombre;
    protected Directorio padre; // Referencia al directorio que lo contiene

    public EntradaSistemaArchivos(String nombre, Directorio padre) {
        this.nombre = nombre;
        this.padre = padre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Directorio getPadre() {
        return padre;
    }

    public void setPadre(Directorio padre) {
        this.padre = padre;
    }
    
    /**
     * Devuelve la ruta completa, ej: /root/docs/miArchivo.txt
     */
    public String getRutaCompleta() {
        if (padre == null) {
            // Asumimos que la raíz se llama "root"
            return "/root"; 
        }
        
        String rutaPadre = padre.getRutaCompleta();
        
        // Evita doble barra // si el padre es la raíz
        if (rutaPadre.equals("/root")) {
             return "/root/" + nombre;
        }
        
        return rutaPadre + "/" + nombre;
    }
    
    // Sobrescribimos toString() para que el JTree muestre el nombre
    @Override
    public String toString() {
        return nombre;
    }
    
    // Sobrescribimos equals() para que ListaSimple.delete() funcione
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EntradaSistemaArchivos eso = (EntradaSistemaArchivos) obj;
        // Dos entradas son "iguales" si están en el mismo padre y tienen el mismo nombre
        return nombre.equals(eso.nombre) && padre.equals(eso.padre);
    }
}
