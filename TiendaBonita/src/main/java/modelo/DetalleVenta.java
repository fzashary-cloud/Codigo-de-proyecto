package modelo;

public class DetalleVenta {
    private int idDetalle; 
    private Producto producto; 
    private int cantidad; 
    private double precio;
    private double subtotal;

    public DetalleVenta(int idDetalle, Producto producto, int cantidad, double precio) {
        this.idDetalle = idDetalle;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precio;
        this.subtotal = calcularSubtotal();
    }
    public double calcularSubtotal(){
        return cantidad * precio; 
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
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
        this.subtotal = calcularSubtotal();
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
        this.subtotal = calcularSubtotal(); 
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
