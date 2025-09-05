package com.digis01.DRosasAguilarDamianNCapasProject.ML;

public class Estado {

    private int IdEstado;
    private String Nombre;
    public Pais Pais;


    public Pais getPais() {

        return Pais;
    }
    
     public Estado() {}

 

    // public Estado() {}
    /*  public Estado(int IdEstado, String Nombre) {
    this.IdEstado = IdEstado;
    this.Nombre = Nombre;
    }*/
    public void setPais(Pais Pais) {
        this.Pais = Pais;
    }

    public int getIdEstado() {
        return IdEstado;
    }

    public void setIdEstado(int IdEstado) {
        this.IdEstado = IdEstado;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String Nombre) {
        this.Nombre = Nombre;

    }

}
