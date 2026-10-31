package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate; 
import java.util.ArrayList; 
import umg.DAO.ConexionBD;

public class Venta {
    private int idVenta; 
    private LocalDate fecha; 
    private Cliente cliente; 
    private Usuario vendedor; 
    private ArrayList<DetalleVenta>detalles; 
    private double total; 
    private String estado; 

    public Venta(int idVenta, LocalDate fecha, Cliente cliente, Usuario vendedor) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.cliente = cliente;
        this.vendedor = vendedor;
        this.detalles = new ArrayList<>();
        this.total = 0;
        this.estado = "Pendiente"; 
    }
    
    public boolean agregarProducto(Producto producto, int cantidad){
        if (producto == null || cantidad <= 0){
            return false; 
        }  
        // no permite vender un producto vencido 
        if (producto.estaVencido()){
            return false; 
        }
        // Verifica que exista suficiente producto
        if (producto.getExistencia()< cantidad ){
            return false; 
        }
        DetalleVenta detalle = new DetalleVenta(
                detalles.size()+ 1,
                producto, 
                cantidad, 
                producto.getPrecio()
        ); 
        detalles.add(detalle);
        calcularTotal();
        return true; 
    }
    
    public double calcularTotal(){
        total = 0; 
        for(DetalleVenta detalle : detalles){
            total += detalle.getSubtotal(); 
        }
        return total;
    }
    public boolean confrimarVenta(){
        if (detalles.isEmpty()){
            return false; 
        }
        for (DetalleVenta detalle : detalles){
            if (detalle.getProducto().getExistencia()< detalle.getCantidad()){
             return false; 
        }
    }
    // descuentos 
    for (DetalleVenta detalle : detalles){
    detalle.getProducto().disminuirExistencia(
    detalle.getCantidad()
    ); 
} 
    estado = "Confirmada"; 
    return true; } 

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public void setVendedor(Usuario vendedor) {
        this.vendedor = vendedor;
    }

    public ArrayList<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(ArrayList<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}


   
