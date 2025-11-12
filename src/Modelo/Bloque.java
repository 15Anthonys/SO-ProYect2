/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author luisg
 */
public class Bloque {
    private boolean ocupado;
    private int siguienteBloque; // Índice del siguiente bloque en el array de disco
    private String propietario;  // La ruta del archivo dueño (para la GUI)

    public Bloque() {
        this.ocupado = false;
        this.siguienteBloque = -1; // -1 indica fin de cadena o bloque libre
        this.propietario = "libre";
    }

    public boolean estaOcupado() {
        return ocupado;
    }

    public void ocupar(String propietario) {
        this.ocupado = true;
        this.propietario = propietario;
    }

    public void liberar() {
        this.ocupado = false;
        this.siguienteBloque = -1;
        this.propietario = "libre";
    }

    public int getSiguienteBloque() {
        return siguienteBloque;
    }

    public void setSiguienteBloque(int siguienteBloque) {
        this.siguienteBloque = siguienteBloque;
    }

    public String getPropietario() {
        return propietario;
    }
}
