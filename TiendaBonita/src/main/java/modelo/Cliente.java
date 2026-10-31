
package modelo;

public class Cliente {
    private int idCliente; 
    private String nombre; 
    private String nit; 
    private String telefono; 
    private String correo; 
    private String direccio; 

    public Cliente(int idCliente, String nombre, String nit, String telefono, String correo, String direccio) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.nit = nit;
        this.telefono = telefono;
        this.correo = correo;
        this.direccio = direccio;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccio() {
        return direccio;
    }

    public void setDireccio(String direccio) {
        this.direccio = direccio;
    }

    // Alias con la ortografía habitual para usar el modelo desde el formulario y DAO.
    public String getDireccion() {
        return direccio;
    }

    public void setDireccion(String direccion) {
        this.direccio = direccion;
    }
}
