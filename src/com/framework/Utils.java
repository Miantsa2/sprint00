package com.framework;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.rmi.ServerException;
import java.security.spec.ECFieldF2m;
import java.lang.ModuleLayer.Controller;
import java.lang.annotation.Annotation;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



import com.framework.object.*;
import com.framework.erreur.*;

import com.framework.annotations.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpServletRequestWrapper;
import com.google.gson.Gson;

public class Utils {
    boolean isController(Class<?> c) {
        return c.isAnnotationPresent(AnnotationController.class);
    }

    public Object parse(Object o, Class<?> typage) {
        if (typage.equals(int.class)) {
            return o != null ? Integer.parseInt((String) o) : 0;
        } else if (typage.equals(double.class)) {
            return o != null ? Double.parseDouble((String) o) : 0;
        } else if (typage.equals(boolean.class)) {
            return o != null ? Boolean.parseBoolean((String) o) : false;

        } else if (typage.equals(byte.class)) {
            return o != null ? Byte.parseByte((String) o) : 0;

        } else if (typage.equals(float.class)) {
            return o != null ? Float.parseFloat((String) o) : 0;

        } else if (typage.equals(short.class)) {
            return o != null ? Short.parseShort((String) o) : 0;

        } else if (typage.equals(long.class)) {
            return o != null ? Long.parseLong((String) o) : 0;

        }
        return typage.cast(o);
    }


    static public String getCatMethodName(String attributeName) {
        String get = "get";
        String firstLetter = attributeName.substring(0, 1).toUpperCase();
        String rest = attributeName.substring(1);
        String res = firstLetter.concat(rest);
        String methodName = get.concat(res);
        return methodName;
    }

    static public String setCatMethodName(String attributeName) {
        String set = "set";
        String firstLetter = attributeName.substring(0, 1).toUpperCase();
        String rest = attributeName.substring(1);
        String res = firstLetter.concat(rest);
        String methodName = set.concat(res);
        return methodName;
    }

    public List<String> getAllClassesStringAnnotation(String packageName,Class annotation) throws Exception {
        List<String> res=new ArrayList<String>();

        //répertoire racine du package
        String path = this.getClass().getClassLoader().getResource(packageName.replace('.', '/')).getPath();
        String decodedPath = URLDecoder.decode(path, "UTF-8");
        File packageDir = new File(decodedPath);

        // parcourir tous les fichiers dans le répertoire du package
        File[] files = packageDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().replace(".class", "");
                    Class<?> classe = Class.forName(className);
                    if (classe.isAnnotationPresent(annotation)) {
                        res.add(classe.getName());
                    }
                }
            }
        }
        return res;
    }


    public static boolean hasDuplicateKeys(HashMap<String, Mapping> map) {
        Set<String> keysSet = new HashSet<>();
        for (String key : map.keySet()) {
            if (!keysSet.add(key)) {
                return true;
            }
        }
        return false;
    }



    public HashMap<String,Mapping> scanControllersMethods(List<String> controllers) throws Exception,ServletException{

        HashMap<String,Mapping> res=new HashMap<>();
        
        for (String c : controllers) {
                Class classe=Class.forName(c);
                /* Prendre toutes les méthodes de cette classe */
                Method[] meths=classe.getDeclaredMethods();
                for (Method method : meths) {
                    if(method.isAnnotationPresent(GetMapping.class)){
                        
                        // if (method.getReturnType()!=String.class && method.getReturnType()!=ModelView.class) {

                        //     throw new ServletException("le type de retour de la fonction"+method.getName()+"est different de String ou ModelView");
                        
                        // }

                        String url=method.getAnnotation(GetMapping.class).url();
                        
                        String valeurAnnotationUrl = Get.value;
                        if (method.isAnnotationPresent(Get.class)) {
                            valeurAnnotationUrl = Get.value;
                        } else if (method.isAnnotationPresent(Post.class)) {
                            valeurAnnotationUrl = Post.value;
                        }

                        if(res.containsKey(url)){
                        
                            // String existant=res.get(url).getClassName()+":"+res.get(url).getMethodName() ;
                            // String nouveau=classe.getName()+":"+method.getName();
                            // throw new ServletException("L'url "+url+" est déja mappé sur "+existant+" et ne peut plus l'être sur "+nouveau);

                            if (!res.get(url).getVerbmethods().add(new VerbMethod(valeurAnnotationUrl, method))) {
                                System.out.println("tsy mety scan");
                                throw new Exception(
                                        "Il ya deja un verb " + valeurAnnotationUrl + " sur l'url " + url);
                            }
                        }
                        /* Prendre l'annotation */

                        Set<VerbMethod> set = new HashSet<VerbMethod>();
                        set.add(new VerbMethod(valeurAnnotationUrl, method));
                        res.put(url, new Mapping(c, set));
                       // res.put(url,new Mapping(c,method.getName()));
                    }
                }
            }
        return res;
    }



    public  Method findMethod(String className,VerbMethod verbmethode,String path) throws ServletException,Exception{
        Class<?> laclasse=Class.forName(className);

        Method method=null;
        for (Method m : laclasse.getMethods()) {
            if (m.getName().equals(verbmethode.getMethode().getName())) {
                    Annotation[] annotations =  m.getAnnotations();
                    if (annotations[0] instanceof GetMapping) {
                        String name = ((GetMapping) annotations[0]).url();
                        if (name.equals(path)) {
                            method = m;
                        }
                   }
            }
        }
        if (method == null) {
            throw new ServletException("No such method" + verbmethode.getMethode().getName());
        }
        return  method;
    }

    //sprint 6
    public String getURIWithoutContextPath(HttpServletRequest request){
        return  request.getRequestURI().substring(request.getContextPath().length()).split("\\?")[0];
    }

    //sprint 6
    // public static Object[] getParameters(Method method,HttpServletRequest request ,HttpServletResponse response) throws ServletException, Exception{

    //     // Get parameter types and values from the request using annotations
    //     Parameter[] parameters = method.getParameters();
    //     Object[] parameterValues = new Object[parameters.length];
    
    //     for (int i = 0; i < parameters.length; i++) {

    //         if (parameters[i].getType().equals(MySession.class)) {
    //             parameterValues[i]=new MySession(request.getSession());
    //         }

    //          else{
    //             String paramName = "";
    //             Annotation[] annotations =  parameters[i].getAnnotations();
                
    //              if (annotations.length>0) {
    //                 for (Annotation annotation : annotations) {
    //                     if (annotation instanceof RequestParam) {
    //                         paramName = ((RequestParam) annotation).value();
    //                     }
    //                 }
    //             }
    //             else{

    //                 paramName=parameters[i].getName();
    //                 // throw new Exception("pas d annotation ETU002453");

    //            }
    //                 if (parameters[i].getType() == String.class||
    //                 parameters[i].getType() == int.class ||
    //                 parameters[i].getType() == double.class) {

    //                 String paramValue = request.getParameter(paramName);

    //                 if (parameters[i].getType() == String.class) {
    //                     parameterValues[i] = paramValue;
    //                 } else if (parameters[i].getType() == int.class || parameters[i].getType() == Integer.class) {
    //                     parameterValues[i] = Integer.parseInt(paramValue);
    //                 } else if (parameters[i].getType() == double.class || parameters[i].getType() == Double.class) {
    //                     parameterValues[i] = Double.parseDouble(paramValue);
    //                 }

    //                 } //sprint7
    //                 else {


    //                     Class<?> laclasse=Class.forName(parameters[i].getType().getName());
    //                     Object newInstance = laclasse.getDeclaredConstructor().newInstance();
    //                     Field[] attributs=(laclasse).getDeclaredFields();
    //                     Object[] attributsvalue=new Object[attributs.length];

    //                     for (int j=0 ; j<attributs.length ;j++) {
    //                         attributs[j].setAccessible(true);
    //                         String attvalue=request.getParameter(paramName+"."+attributs[j].getName());
    //                         System.out.println("tonga eto");
                            
    //                         if (attributs[j].getType() == String.class) {
    //                             attributsvalue[j] = attvalue;
    //                         } else if (attributs[j].getType() == int.class || attributs[j].getType() == Integer.class) {
    //                             attributsvalue[j] = Integer.parseInt(attvalue);
    //                         } else if (attributs[j].getType() == double.class || attributs[j].getType() == Double.class) {
    //                             attributsvalue[j] = Double.parseDouble(attvalue);
    //                         }
    //                         else {
                                
    //                             throw new ServletException("l objet ne peut pas avoir d objet en tant que parametre");
    //                        }
    //                        attributs[j].set(newInstance, attributsvalue[j]);
    //                     }
    //                    parameterValues[i] =newInstance;
    //                 }
    //          }    
    //     }   
    //     return parameterValues;
    // }

 

    


    public void processObject(Map<String, String[]> params, Parameter param, List<Object> ls) throws Exception {
        String key = null;
        Map<String, List<String>> errorMap = new HashMap<>();
        Class<?> c = param.getType();
        String nomObjet = null;
        nomObjet = param.isAnnotationPresent(ObjectParam.class) ? param.getAnnotation(ObjectParam.class).value()
                : param.getName();
        Object o = c.getConstructor((Class[]) null).newInstance((Object[]) null);
        /// prendre les attributs
        Field[] f = c.getDeclaredFields();
      

        for (Field field : f) {
            String attributObjet = null;
            attributObjet = field.isAnnotationPresent(FieldParam.class)
                    ? field.getAnnotation(FieldParam.class).paramName()
                    : field.getName();
            key = nomObjet + "." + attributObjet;
           
            Method setters = c.getDeclaredMethod(setCatMethodName(attributObjet), field.getType());
            if (key == null || params.get(key) == null) {
                setters.invoke(o, this.parse(null, field.getType()));
            } else if (params.get(key).length == 1) {
                setters.invoke(o, this.parse(params.get(key)[0], field.getType()));
            } else if (params.get(key).length > 1) {
                setters.invoke(o, this.parse(params.get(key), field.getType()));
            }
        }
        ls.add(o);
    }


    

    //sprint 6
    public  Object[] getParameters(Method method, HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, Exception {

        Parameter[] parameters = method.getParameters();
        Object[] parameterValues = new Object[parameters.length];

        Map<String, String[]> params = request.getParameterMap();
        List<Object> objectsList = new ArrayList<>();

        for (int i = 0; i < parameters.length; i++) {


            if (parameters[i].getType().equals(MySession.class)) {
                parameterValues[i] = new MySession(request.getSession());
            } 
            //ajouttt
            else if (parameters[i].getType().equals(Map.class)) {
               // parameterValues[i] = new HashMap<>();
                Map<String, String> map = new HashMap<>();
                Enumeration<String> paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String paramName = paramNames.nextElement();
                    String paramVal = request.getParameter(paramName);
                    map.put(paramName, paramVal);
                }
                parameterValues[i]  = map;
            } 
            //sprint 12
            
            else if (parameters[i].getType().equals(MyMultiPart.class)) { 
                String paramName = parameters[i].getAnnotation(RequestParam.class).value();
                Part part = request.getPart(paramName);
                parameterValues[i] = new MyMultiPart(part);  // Convertit en MyMultiPart
            }

           
            //sprint7
             else {
                String paramName = "";
                Annotation[] annotations = parameters[i].getAnnotations();

                if (annotations.length > 0) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof RequestParam) {
                            paramName = ((RequestParam) annotation).value();
                        }
                    }
                } else {
                    paramName = parameters[i].getName();
                }

                if (parameters[i].getType() == String.class || parameters[i].getType() == int.class || parameters[i].getType() == double.class) {
                    String paramValue = request.getParameter(paramName);
                    if (parameters[i].getType() == String.class) {
                        parameterValues[i] = paramValue;
                    } else if (parameters[i].getType() == int.class || parameters[i].getType() == Integer.class) {
                        parameterValues[i] = Integer.parseInt(paramValue);
                    } else if (parameters[i].getType() == double.class || parameters[i].getType() == Double.class) {
                        parameterValues[i] = Double.parseDouble(paramValue);
                    }
                } else {
                    throw new ServletException("Type de paramètre non pris en charge : " + parameters[i].getType());
                }
            }
        }
        return parameterValues;
    }



    // public static Object callMethod(String className,String methodName,String path,HttpServletRequest request) throws ServletException,Exception{
    //     Class<?> laclasse=Class.forName(className);

    //     Method method=null;
    //     for (Method m : laclasse.getMethods()) {
    //         if (m.getName().equals(methodName)) {
    //                 Annotation[] annotations =  m.getAnnotations();
    //                 if (annotations[0] instanceof GetMapping) {
    //                     String name = ((GetMapping) annotations[0]).url();
    //                     System.out.println(name);
    //                     System.out.println(path);
    //                     if (name.equals(path)) {
    //                         method = m;
    //                     }
    //                }
    //         }
    //     }
    //     if (method == null) {
    //         throw new ServletException("No such method" + methodName);
    //     }
    //     Object[] paramValues=getParameters(method,request, null);

    //     Object controller=laclasse.getConstructor().newInstance();

    //     setSessionAttribut(controller, laclasse, request);

    //     Object objet=method.invoke(controller,paramValues);
    //     return objet;

    // }



    public  Object callMethod(String className,VerbMethod verbmethode,String path,HttpServletRequest request) throws ServletException,Exception{
        Class<?> laclasse=Class.forName(className);

        Method method=null;
        for (Method m : laclasse.getMethods()) {
            if (m.getName().equals(verbmethode.getMethode().getName())) {
                if(request.getMethod().equals(verbmethode.getVerb())){
                    method=verbmethode.getMethode();
                 }
                //     Annotation[] annotations =  m.getAnnotations();
                //     if (annotations[0] instanceof GetMapping) {
                //         String name = ((GetMapping) annotations[0]).url();
                //         System.out.println(name);
                //         System.out.println(path);
                //         if (name.equals(path)) {
                //             method = m;
                //         }
                //    }


                else {
                    throw new Exception(
                            "La requete est de type " + request.getMethod() + " alors que la methode est de type " + verbmethode.getVerb());
                }
            }
        }
        if (method == null) {
            throw new ServletException("No such method" + verbmethode.getMethode().getName());
        }
        Object[] paramValues=null;
        Map<String, String[]> params = request.getParameterMap();
        try{
           paramValues=getParameters(method,request, null);


        }
        catch (Exception e) {
            throw new ServletException("Erreur lors de la récupération des paramètres : " + e.getMessage(), e);
        }
        
        Object controller=laclasse.getConstructor().newInstance();

        setSessionAttribut(controller, laclasse, request);

        Object objet=method.invoke(controller,paramValues);
        return objet;

    }

    public static void setSessionAttribut(Object obje,Class<?> laclasse, HttpServletRequest request) throws Exception, ServerException{

        Field[] fld=laclasse.getDeclaredFields();
        for (Field field : fld) {
            if (field.getType().equals(MySession.class)) {
                field.setAccessible(true);
                field.set(obje,new MySession(request.getSession(true)));
            }
        }

    }

    

    public VerbMethod searchVerbMethod(HttpServletRequest req, HashMap<String, Mapping> map, String path,
            String authVar, String authRole)
            throws Exception {
        if (map.containsKey(path)) {
            boolean did=verifAuthController(map.get(path), req, authVar, authRole);
            VerbMethod[] verb_meths = (VerbMethod[]) map.get(path).getVerbmethods().toArray(new VerbMethod[0]);
            VerbMethod m = null;
            for (VerbMethod verbMethod : verb_meths) {
                if (verbMethod.getVerb().equals(req.getMethod())) {
                    m = verbMethod;
                    break;
                }
            }
            if (m == null) {
                throw new ResourceNotFound("L'url ne supporte pas la méthode " + req.getMethod());
            }
            if(!did){
                verifAuthMethode(m, req, authVar, authRole);
            }
            return m;
        } else {
            throw new Exception("Aucune méthode associé a cette url");
        }
    }

    public  Object findAndCallMethod(HashMap<String,Mapping> map,String path,HttpServletRequest request,
    String authVar, String authRole)throws ServletException,Exception{
        if(map.containsKey(path)){
            VerbMethod  verb=new Utils().searchVerbMethod(request,  map,  path, authVar, authRole);
            Mapping m=map.get(path);
            return callMethod(m.getClassName(),verb,path,request);
        }
        else{
            throw new ServletException("No Such method "+path);
        }

    }



  



    public  void ProcessMethod(HashMap<String,Mapping> map,String path,HttpServletRequest request,HttpServletResponse response,PrintWriter out,
    String authRoleVarName, String authVarName )
    throws ServletException,IOException
    {
        try {
            Object objet=findAndCallMethod(map, path,request,authVarName,authRoleVarName);       
            VerbMethod  verb=  new Utils().searchVerbMethod(request,  map,  path, authVarName,authRoleVarName);
            Method method=findMethod(map.get(path).getClassName(),verb, path);


            //sprint9
            if (method.isAnnotationPresent(RestApi.class)) {

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                Gson gson=new Gson();
                if (!(objet instanceof ModelView)) {
                    gson.toJson(objet,out);
                }
                else{
                    ModelView mv =(ModelView) objet;
                    gson.toJson(mv.getData(),out);
                }
                   
            }
            else{ 

                if (objet instanceof String) {
                    out.println(objet.toString());
                }
                //sprint 4 
                else if (objet instanceof ModelView) {

                    HashMap<String,Object> hash=((ModelView)objet).getData();
                    if (hash != null) {
                        if (!hash.isEmpty()) {
                            for (String string : hash.keySet()) {
                                request.setAttribute(string, hash.get(string));
                                out.println(string);
                            }
                        }
                    }
                    String view=((ModelView)objet).getUrl();
                    out.println(view);
                    request.getRequestDispatcher(view).forward(request, response);
                }
            
                else{
                    //sprint 5

                    if (!(objet instanceof String) && !(objet instanceof ModelView)) {
                        throw new Exception("La méthode " + method.getName() + " ne retourne ni String ni ModelView");
                    }
                }
            }

            }
            
            // catch(ResourceNotFound e){
            //     response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            //     response.getWriter().write(e.getMessage());
            //     e.printStackTrace();
            // }
            // catch (ResourceNotFound e) {
            //     response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            //     response.setContentType("text/html");
            //     out = response.getWriter();
            //     out.println("<html><head><title>État HTTP 404 – Non trouvé</title></head><body>");
            //     out.println("<h1>État HTTP 404 – Non trouvé</h1>");
            //     out.println("<p><strong>Type :</strong> Rapport d'état</p>");
            //     out.println("<p><strong>Message :</strong> " + e.getMessage() + "</p>");
            //     out.println("<p><strong>Description :</strong> La ressource demandée n'est pas disponible.</p>");
            //     out.println("</body></html>");
            // }


           

           //sprint 11
            catch (ResourceNotFound e) {
                out.println("ressource");

                response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            } 



            catch (Exception e) {
                out.println("exception");

                // TODO Auto-generated catch block
                /* throw new ServletException(e); */
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(e.getMessage());
                e.printStackTrace();

                 /* throw new ServletException(e); */
    
            }
            
        //     catch (Exception e) {

        //         out.print(e.getLocalizedMessage());                    
                
        //         try {
                        
        //         request.setAttribute("exception",e.getLocalizedMessage());
        //         // request.getRequestDispatcher("exception.jsp").forward(request, response);
            
        //         } catch (Exception ex) {
        //         System.err.println(ex.getLocalizedMessage());    
        //         }
            

        // }
        }
    


    



  

    
}
