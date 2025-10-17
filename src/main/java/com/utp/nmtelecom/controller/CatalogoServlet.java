package com.utp.nmtelecom.controller;

import com.utp.nmtelecom.dao.ProductoDAO;
import com.utp.nmtelecom.dao.IProductoDAO;
import com.utp.nmtelecom.model.Producto;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet encargado de manejar la lógica del Catálogo de Productos (Controller).
 */
@WebServlet("/catalogo")
public class CatalogoServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CatalogoServlet.class.getName());
    
    // Aplicación del Principio de Inversión de Dependencias (SOLID D)
    // Se declara la interfaz, no la implementación concreta.
    private final IProductoDAO productoDAO = new ProductoDAO(); 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // 1. Lógica de Negocio (Modelo)
            List<Producto> productos = productoDAO.listarActivos();
            
            // 2. Pasar datos a la Vista
            request.setAttribute("productos", productos);
            
            // 3. Despachar a la Vista JSP (View)
            request.getRequestDispatcher("/jsp/catalogo.jsp").forward(request, response);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en la conexión o consulta a la base de datos.", e);
            // Manejo de errores
            request.setAttribute("error", "Error al cargar el catálogo: " + e.getMessage());
            request.getRequestDispatcher("/jsp/error.jsp").forward(request, response);
        }
    }
}