package com.digis01.DRosasAguilarDamianNCapasProject.ML;


import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

public class Usuario {

    private int IdUsuario;
    private String Username;
    private String Nombre;
    private String Apellidopaterno;
    private String Apellidomaterno;
    private String Email;

    private String Password;
    private String Telefono;
    private String Curp;
    // private String Direccion;
    private String Celular;
    private String Sexo;
    private String Tiposangre;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date FechaNacimiento;
    public Rol Rol;
    public List<Direccion> direcciones;
    private String Imagen;

    private Integer Status = 1;
    

    public Integer getStatus() {
        return Status;
    }

    public void setStatus(Integer status) {
        this.Status = status;
    }

    public Usuario() {
        this.direcciones = new ArrayList<>();
    }

    public Rol getRol() {
        return Rol;
    }

    public void setRol(Rol Rol) {
        this.Rol = Rol;
    }


    public Usuario(int idUsuario, String username, String nombre, String apellidopaterno, String apellidomaterno,
            String email, String password, String telefono, String direccion, String celular, String sexo,
            String tiposangre, Date fechaNacimiento, int idrol, String curp) {
        this.IdUsuario = idUsuario;
        this.Username = username;
        this.Nombre = nombre;
        this.Apellidopaterno = apellidopaterno;
        this.Apellidomaterno = apellidomaterno;
        this.Email = email;
        this.Password = password;
        this.Telefono = telefono;
        this.Curp = curp; // corregido
        // this.Direccion = direccion;
        this.Celular = celular;
        this.Sexo = sexo;
        this.Tiposangre = tiposangre;
        this.FechaNacimiento = fechaNacimiento;
        this.direcciones = new ArrayList<>();
    }

    public Usuario(Usuario usuario) {
    }



    public List<Direccion> getDirecciones() {
        return direcciones;
    }

    public void setDirecciones(List<Direccion> direcciones) {
        this.direcciones = direcciones;
    }

    public int getIdUsuario() {
        return IdUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        IdUsuario = idUsuario;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getApellidopaterno() {
        return Apellidopaterno;
    }

    public void setApellidopaterno(String apellidopaterno) {
        Apellidopaterno = apellidopaterno;
    }

    public String getApellidomaterno() {
        return Apellidomaterno;
    }

    public void setApellidomaterno(String apellidomaterno) {
        Apellidomaterno = apellidomaterno;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getTelefono() {
        return Telefono;
    }

    public void setTelefono(String telefono) {
        Telefono = telefono;
    }

    public String getCurp() {
        return Curp;
    }

    public void setCurp(String curp) {
        Curp = curp;
    }

    public String getCelular() {
        return Celular;
    }

    public void setCelular(String celular) {
        Celular = celular;
    }

    public String getSexo() {
        return Sexo;
    }

    public void setSexo(String sexo) {
        Sexo = sexo;
    }

    public String getTiposangre() {
        return Tiposangre;
    }

    public void setTiposangre(String tiposangre) {
        Tiposangre = tiposangre;
    }

    public Date getFechaNacimiento() {
        return FechaNacimiento;
    }

    public void setFechaNacimiento(Date FechaNacimiento) {
        this.FechaNacimiento = FechaNacimiento;
    }

    public String getImagen() {
        return Imagen;
    }

    public void setImagen(String Imagen) {
        this.Imagen = Imagen;
    }

    public Usuario(String Nombre, String Apellidopaterno, String Apellidomaterno, Rol Rol) {
        this.Nombre = Nombre;
        this.Apellidopaterno = Apellidopaterno;
        this.Apellidomaterno = Apellidomaterno;
        this.Rol = Rol;
    }

}
