/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

/**
 *
 * @author luisg
 */
public enum EstadoProceso {
    LISTO,      // Listo para ejecutarse o esperando en cola
    BLOQUEADO,  // Esperando a que se complete su solicitud de E/S
    TERMINADO,
    NUEVO,
    EJECUTANDO// El proceso ha finalizado
}
