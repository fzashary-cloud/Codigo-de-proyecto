
package modelo;

public class Rol {
    private int idRol; 
    private String nombre; 
    private String permisos; 

    public Rol(int idRol, String nombre, String permisos) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.permisos = permisos;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPermisos() {
        return permisos;
    }

    public void setPermisos(String permisos) {
        this.permisos = permisos;
    }
 
}
