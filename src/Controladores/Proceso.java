/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

/**
 *
 * @author luisg
 */
public class Proceso {
    private final String id;
    private EstadoProceso estado;
    private static int contador = 0; // Para generar IDs únicos

    public Proceso() {
        this.id = "Proceso-" + (contador++);
        this.estado = EstadoProceso.LISTO;
    }

    public String getId() {
        return id;
    }

    public EstadoProceso getEstado() {
        return estado;
    }

    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return id + " (" + estado + ")";
    }
}
