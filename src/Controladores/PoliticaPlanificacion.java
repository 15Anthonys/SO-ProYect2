/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;

/**
 *
 * @author luisg
 */

/**
 * Define los algoritmos de planificación de disco disponibles.
 */
public enum PoliticaPlanificacion {
    FIFO,   // First-In, First-Out
    SSTF,   // Shortest Seek Time First
    SCAN,
    CSCAN   // Circular SCAN
}