/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.digis01.DRosasAguilarDamianNCapasProject.ML;

/**
 *
 * @author digis
 */
public class ErrorCM {
    
    public int linea;
    public String dato;
    public String mensaje;

    public ErrorCM(){}
    
    public ErrorCM(int linea, String dato, String mensaje) {
        this.linea = linea;
        this.dato = dato;
        this.mensaje = mensaje;
    }
}
