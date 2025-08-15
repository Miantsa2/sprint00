package com.framework.controller;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ModuleLayer.Controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.framework.annotations.*;
import com.framework.object.*;


import com.framework.*;
@MultipartConfig

public class FrontController extends HttpServlet {

        private List<String> controllers;   
        private HashMap<String,Mapping> map;
        private String authVarName;
        private String authRoleVarName;


    @Override
    public void init() throws ServletException {

        String packageToScan = this.getInitParameter("controllersPackage");
        
        if (packageToScan == null || packageToScan.isEmpty()) {
            throw new ServletException("Le paramètre 'controllersPackage' est manquant ou vide.");
        }
        
        try {
            //sprint 1 : Récupérer toutes les classes annotées
            this.controllers = new Utils().getAllClassesStringAnnotation(packageToScan, AnnotationController.class);

            if (this.controllers.isEmpty()) {
                throw new ServletException("Aucune classe trouvée dans le package spécifié : " + packageToScan);
            }

            //sprint 2 : Récupérer les méthodes annotées  
            this.map = new Utils().scanControllersMethods(this.controllers);

            if (Utils.hasDuplicateKeys(map)) {
                throw new ServletException("vous avez deux get mapping similaires dans votre classe");
            }
            

          
        } catch (ServletException e) {
            throw e;  // Relancer l'exception ServletException
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Une erreur est survenue lors de l'initialisation des contrôleurs.", e);
        }

        this.authVarName = this.getInitParameter("auth_name");
        this.authRoleVarName = this.getInitParameter("auth_role_name");
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // sprint 0 : Récupérer l'URL actuelle
        String currentURL = request.getRequestURL().toString();
        PrintWriter out= response.getWriter();
        // Répondre avec l'URL actuelle
        response.setContentType("text/plain");
      //  response.getWriter().println("Vous êtes actuellement sur : " + currentURL);


        
      // response.getWriter().println("Liste de tous vos controllers : ");


        // for (String class1 : this.controllers) {
        //    response.getWriter().println(class1);
        // }

        String path =new Utils().getURIWithoutContextPath(request);
        
       // response.getWriter().println("L'URL a chercher dans le map : " + path);



        // response.getWriter().println("Résultats des mappings :");

        // for (Map.Entry<String, Mapping> entry : map.entrySet()) {
        // response.getWriter().println("URL : " + entry.getKey());
        // response.getWriter().println("Mapping : " + entry.getValue().getClassName());
        // response.getWriter().println("Mapping : " + entry.getValue().getVerbmethods());


        
        // }
        //sprint 3 : Appeler la méthode correspondante
        new Utils().ProcessMethod(map,path,request,response,out,authRoleVarName,authVarName);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
