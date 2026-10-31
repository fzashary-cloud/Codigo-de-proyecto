
package modelo;
import java.time.LocalDate; 
public class Producto {
    private int idProducto; 
    private String codigo; 
    private String nombre; 
    private String marca; 
    private String calidad; 
    private String tonoColor; 
    private String presentacion;  
    private double precio; 
    private int existencia; 
    private int stockMinimo; 
    private LocalDate fechaVencimiento; 
    
    private Categoria categoria; 
    private Proveedor proveedor; 

    public Producto(int idProducto, String codigo, String nombre, String marca, String calidad, String tonoColor, String presentacion, double precio, int existencia, int stockMinimo, LocalDate fechaVencimiento, Categoria categoria, Proveedor proveedor) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.marca = marca;
        this.calidad = calidad;
        this.tonoColor = tonoColor;
        this.presentacion = presentacion;
        this.precio = precio;
        this.existencia = existencia;
        this.stockMinimo = stockMinimo;
        this.fechaVencimiento = fechaVencimiento;
        this.categoria = categoria;
        this.proveedor = proveedor;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getCalidad() {
        return calidad;
    }

    public void setCalidad(String calidad) {
        this.calidad = calidad;
    }

    public String getTonoColor() {
        return tonoColor;
    }

    public void setTonoColor(String tonoColor) {
        this.tonoColor = tonoColor;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getExistencia() {
        return existencia;
    }

    public void setExistencia(int existencia) {
        this.existencia = existencia;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
    
    public boolean stockBajo(){
        return existencia <= stockMinimo;
    }
    public boolean estaVencido(){
        return fechaVencimiento != null
                && fechaVencimiento.isBefore(LocalDate.now()); 
    }
    public void aumentarExistencia(int cantidad){
        if (cantidad > 0){
            existencia += cantidad; 
        }
    }
    public boolean disminuirExistencia(int cantidad){
        if (cantidad > 0 && cantidad <= existencia){
        existencia -= cantidad; 
        return true; 
        }
        return false;
    }
    private boolean validarDatosProducto(
            String codigo,
            String nombre, 
            double precio,
            int existencia,
            int stockMinimo){
        
        if(codigo == null || codigo.trim().isEmpty()){
            System.out.println("Error: el codigo es obligatorio.");
            return false; 
        }
        if(nombre == null || nombre.trim().isEmpty()){
            System.out.println("Error: el nombre es obligatorio.");
            return false; 
        }
        if(precio <= 0){
            System.out.println("Error: el precio debe ser mayor que cero.");
            return false; 
        }
        if(existencia < 0){
            System.out.println("Error: la existencia no puede ser negativa.");
            return false; 
        }
        if (stockMinimo < 0){
            System.out.println("Error: el stock minimo no puede ser negativa.");
            return false;
        }
        return true; 
    }
}
