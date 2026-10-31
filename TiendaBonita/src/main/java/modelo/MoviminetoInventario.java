package modelo;
import java.time.LocalDate;

public class MoviminetoInventario {
    private int idMovimiento; 
    private String tipo; 
    private Producto producto; 
    private int cantidad; 
    private LocalDate fecha; 
    private String motivo; 
    private Usuario usuario; 

    public MoviminetoInventario(int idMovimiento, String tipo, Producto producto, int cantidad, LocalDate fecha, String motivo, Usuario usuario) {
        this.idMovimiento = idMovimiento;
        this.tipo = tipo;
        this.producto = producto;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.motivo = motivo;
        this.usuario = usuario;
    }
    public boolean aplicarMovimiento(){
        if(producto == null || cantidad <= 0){
            return false; 
        }
        if(tipo.equalsIgnoreCase("ENTRADA")
                || tipo.equalsIgnoreCase("DEVOLICON")){
            producto.aumentarExistencia(cantidad); 
            return true;
        }
        if(tipo.equalsIgnoreCase("BAJA")){
            return producto.disminuirExistencia(cantidad);
        }
        return false; 
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
}
