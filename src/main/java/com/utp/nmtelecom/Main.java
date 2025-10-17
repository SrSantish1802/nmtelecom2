package com.utp.nmtelecom;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import java.io.File;

public class Main {

    public static void main(String[] args) throws LifecycleException {
        // Crear instancia del servidor embebido
        Tomcat tomcat = new Tomcat();

        // Puerto HTTP
        String port = System.getenv("PORT");

        if (port == null) {
            port = "9090"; // modo local
            System.out.println("🌍 Ejecutando en modo LOCAL en http://localhost:" + port);
        } else {
            System.out.println("🚀 Ejecutando en RAILWAY en puerto asignado: " + port);
        }

        tomcat.setPort(Integer.parseInt(port));

        // Crear conector (necesario antes de start)
        tomcat.getConnector();

        // Directorio raíz del proyecto (carpeta webapp)
        File webappDir = new File("target/classes/webapp");
        if (!webappDir.exists()) {
            // fallback para ejecución dentro del JAR
            webappDir = new File(".");
        }
        tomcat.addWebapp("", webappDir.getAbsolutePath());
        System.out.println("📂 Cargando webapp desde: " + webappDir.getAbsolutePath());


        // Agregar la aplicación web
        Context ctx = tomcat.addWebapp("", webAppPath);

        // 👇 Asegurar que el classpath apunte a las clases compiladas (servlets anotados)
        File classesDir = new File("target/classes");
        if (classesDir.exists()) {
            ctx.setResources(new org.apache.catalina.webresources.StandardRoot(ctx));
            ctx.getResources().createWebResourceSet(
                org.apache.catalina.WebResourceRoot.ResourceSetType.PRE,
                "/WEB-INF/classes",
                classesDir.getAbsolutePath(),
                null,
                "/"
            );
        }

        // Iniciar el servidor
        tomcat.start();
        System.out.println("Servidor embebido iniciado en http://localhost:9090");
        tomcat.getServer().await();
    }
}

