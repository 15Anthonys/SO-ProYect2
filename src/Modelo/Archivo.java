/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author luisg
 */
public class Archivo extends EntradaSistemaArchivos {
    
    private int tamanoEnBloques;
    private int primerBloque; // Índice del primer bloque en el disco
    
    private String idProcesoCreador;

    public Archivo(String nombre, Directorio padre, int tamanoEnBloques, int primerBloque) {
        super(nombre, padre);
        this.tamanoEnBloques = tamanoEnBloques;
        this.primerBloque = primerBloque;
        this.idProcesoCreador = "Sistema";
    }
    
    public String getIdProcesoCreador() {
        return idProcesoCreador;
    }
    
    public void setIdProcesoCreador(String idProcesoCreador) {
        this.idProcesoCreador = idProcesoCreador;
    }

    public int getTamanoEnBloques() {
        return tamanoEnBloques;
    }

    public void setTamanoEnBloques(int tamanoEnBloques) {
        this.tamanoEnBloques = tamanoEnBloques;
    }

    public int getPrimerBloque() {
        return primerBloque;
    }

    public void setPrimerBloque(int primerBloque) {
        this.primerBloque = primerBloque;
    }
}
