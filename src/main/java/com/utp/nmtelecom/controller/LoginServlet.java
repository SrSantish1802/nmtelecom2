package com.utp.nmtelecom.controller;

import com.utp.nmtelecom.dao.UsuarioDAO;
import com.utp.nmtelecom.model.Usuario;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    
    // Instancia del DAO para interactuar con la BD
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Maneja la petición GET: Muestra el formulario de inicio de sesión.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Simplemente redirige a la vista JSP del formulario
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    /**
     * Maneja la petición POST: Procesa el envío de credenciales.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Obtener parámetros del formulario
        String nombreUsuario = request.getParameter("nombreUsuario");
        String password = request.getParameter("password");
        
        try {
            // 2. Intentar autenticar con la BD
            Usuario usuario = usuarioDAO.autenticar(nombreUsuario, password);

            if (usuario != null) {
                // 3. Autenticación exitosa: Guardar usuario en la sesión
                HttpSession session = request.getSession();
                session.setAttribute("usuarioLogeado", usuario); // Clave para el navbar
                
                // Redirigir al catálogo
                response.sendRedirect(request.getContextPath() + "/catalogo");
            } else {
                // 4. Autenticación fallida
                request.setAttribute("mensajeError", "Usuario o contraseña incorrectos.");
                // Volver a mostrar el formulario con el mensaje de error
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("mensajeError", "Error de base de datos durante la autenticación.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}