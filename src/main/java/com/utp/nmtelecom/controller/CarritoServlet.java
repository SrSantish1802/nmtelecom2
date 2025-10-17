package com.utp.nmtelecom.controller;

import com.google.gson.Gson;
import com.utp.nmtelecom.dao.Conexion;
import com.utp.nmtelecom.model.Usuario;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/carrito")
public class CarritoServlet extends HttpServlet {
    
    private final Gson gson = new Gson();
    
    /**
     * Maneja POST: Agregar producto al carrito (AJAX)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Configurar respuesta JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogeado");
        
        Map<String, Object> jsonResponse = new HashMap<>();
        
        // Verificar autenticación
        if (usuario == null) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Debe iniciar sesión para agregar productos al carrito.");
            jsonResponse.put("requireLogin", true);
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }
        
        String accion = request.getParameter("accion");
        String idProductoStr = request.getParameter("idProducto");
        
        if (!"agregar".equals(accion) || idProductoStr == null || idProductoStr.isEmpty()) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Parámetros inválidos.");
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }
        
        try {
            long idProducto = Long.parseLong(idProductoStr);
            long usuarioId = usuario.getIdUsuario();
            
            // Agregar al carrito
            boolean agregado = agregarAlCarrito(usuarioId, idProducto);
            
            if (agregado) {
                // Contar items en el carrito
                int count = contarItemsCarrito(usuarioId);
                session.setAttribute("carritoCount", count);
                
                jsonResponse.put("success", true);
                jsonResponse.put("message", "Producto agregado al carrito.");
                jsonResponse.put("count", count);
            } else {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "No se pudo agregar el producto al carrito.");
            }
            
        } catch (NumberFormatException e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "ID de producto inválido.");
        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error de base de datos.");
        }
        
        response.getWriter().write(gson.toJson(jsonResponse));
    }
    
    /**
     * Obtiene o crea un carrito para el usuario
     */
    private Long obtenerOCrearCarrito(long usuarioId) throws SQLException {
        String sqlSelect = "SELECT idCarrito FROM carrito_compra WHERE usuario_id = ?";
        String sqlInsert = "INSERT INTO carrito_compra (usuario_id, fechaCreacion, total) VALUES (?, ?, 0.00)";
        
        try (Connection conn = Conexion.getConexion()) {
            // Buscar carrito existente
            try (PreparedStatement ps = conn.prepareStatement(sqlSelect)) {
                ps.setLong(1, usuarioId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getLong("idCarrito");
                }
            }
            
            // Crear nuevo carrito
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, usuarioId);
                ps.setDate(2, Date.valueOf(LocalDate.now()));
                ps.executeUpdate();
                
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }
    
    /**
     * Agrega un producto al carrito o incrementa la cantidad si ya existe
     */
    private boolean agregarAlCarrito(long usuarioId, long idProducto) throws SQLException {
        // 1. Verificar que el producto existe y tiene stock
        String sqlProducto = "SELECT precio, stock FROM producto WHERE idProducto = ? AND activo = 1";
        double precio = 0;
        int stock = 0;
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sqlProducto)) {
            ps.setLong(1, idProducto);
            ResultSet rs = ps.executeQuery();
            
            if (!rs.next()) {
                return false; // Producto no existe
            }
            
            precio = rs.getDouble("precio");
            stock = rs.getInt("stock");
            
            if (stock <= 0) {
                return false; // Sin stock
            }
        }
        
        // 2. Obtener o crear carrito
        Long carritoId = obtenerOCrearCarrito(usuarioId);
        if (carritoId == null) {
            return false;
        }
        
        // 3. Verificar si el producto ya está en el carrito
        String sqlCheck = "SELECT idItem, cantidad FROM item_carrito WHERE carrito_id = ? AND producto_id = ?";
        String sqlUpdate = "UPDATE item_carrito SET cantidad = cantidad + 1 WHERE idItem = ?";
        String sqlInsert = "INSERT INTO item_carrito (carrito_id, producto_id, cantidad, precioUnitario) VALUES (?, ?, 1, ?)";
        
        try (Connection conn = Conexion.getConexion()) {
            // Verificar si existe
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setLong(1, carritoId);
                ps.setLong(2, idProducto);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    // Ya existe, incrementar cantidad
                    long idItem = rs.getLong("idItem");
                    int cantidadActual = rs.getInt("cantidad");
                    
                    // Verificar que no exceda el stock
                    if (cantidadActual + 1 > stock) {
                        return false;
                    }
                    
                    try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                        psUpdate.setLong(1, idItem);
                        return psUpdate.executeUpdate() > 0;
                    }
                } else {
                    // No existe, insertar nuevo
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                        psInsert.setLong(1, carritoId);
                        psInsert.setLong(2, idProducto);
                        psInsert.setDouble(3, precio);
                        return psInsert.executeUpdate() > 0;
                    }
                }
            }
        }
    }
    
    /**
     * Cuenta el número de items diferentes en el carrito
     */
    private int contarItemsCarrito(long usuarioId) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM item_carrito ic " +
                    "INNER JOIN carrito_compra cc ON ic.carrito_id = cc.idCarrito " +
                    "WHERE cc.usuario_id = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }
}