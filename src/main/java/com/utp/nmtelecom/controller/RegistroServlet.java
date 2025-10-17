package com.utp.nmtelecom.controller;

import com.utp.nmtelecom.dao.UsuarioDAO;
import com.utp.nmtelecom.model.Usuario;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.SQLException;

@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Cuando el usuario entra por la URL, simplemente muestra el formulario JSP.
        request.getRequestDispatcher("/registro.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Obtener parámetros
        String nombreUsuario = request.getParameter("nombreUsuario");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        // Establecemos el rol por defecto como CLIENTE
        String rol = "CLIENTE";

        if (nombreUsuario == null || email == null || password == null
                || nombreUsuario.isEmpty() || email.isEmpty() || password.isEmpty()) {

            request.setAttribute("mensaje", "Todos los campos son obligatorios.");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
            return;
        }

        try {
            // 2. Crear objeto Usuario y registrar
            Usuario nuevoUsuario = new Usuario(nombreUsuario, email, password, rol);
            Long idGenerado = usuarioDAO.registrarUsuario(nuevoUsuario);

            if (idGenerado != null) {
                // 3. Simular Autenticación y Redirección
                request.getSession().setAttribute("usuarioLogeado", nuevoUsuario);
                response.sendRedirect(request.getContextPath() + "/catalogo");

            } else {
                request.setAttribute("mensaje", "Error al registrar el usuario. El nombre de usuario o email ya existen.");
                request.getRequestDispatcher("/registro.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("mensaje", "Error de base de datos: " + e.getMessage());
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
            request.setAttribute("mensaje", e.getMessage() + " Por favor, implementa la clase de conexión a la BD.");
            request.getRequestDispatcher("/registro.jsp").forward(request, response);
        }
    }
}
