package mg.itu.prom16;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class FrontController extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.processRequest(request, response);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.processRequest(request, response);
    }
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException{
        StringBuffer url = request.getRequestURL();
        try {
            PrintWriter out = response.getWriter();
            out.println("votre url :"+url); 
        } catch (IOException e) {
            throw e;
        }
    }
}