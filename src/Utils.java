package mg.itu.prom16.utils;
import jakarta.servlet.ServletException;
import java.lang.reflect.Field;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.lang.annotation.Annotation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotations.Controller;
import mg.itu.prom16.annotations.GetMapping;
import mg.itu.prom16.annotations.RequestParam;
import utils.ModelView;
import utils.MySession;

public class Utils {
    boolean isController(Class<?> c) {
        return c.isAnnotationPresent(Controller.class);
    }
    //toutes les class Controller annoter ex: SampleController
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

    //method dans un controlleur annoter getMapping
    public HashMap<String,Mapping> scanControllersMethods(List<String> controllers) throws Exception{
        HashMap<String,Mapping> res=new HashMap<>();
        for (String c : controllers) {
            //avadika class ilay string c controlleur
                Class classe=Class.forName(c);
                /* Prendre toutes les méthodes de cette classe */
                Method[] meths=classe.getDeclaredMethods();
                for (Method method : meths) {
                    if(method.isAnnotationPresent(GetMapping.class)){
                        String url=method.getAnnotation(GetMapping.class).url();
                        if(res.containsKey(url)){
                            String existant=res.get(url).className+":"+res.get(url).methodName;
                            String nouveau=classe.getName()+":"+method.getName();
                            throw new Exception("L'url "+url+" est déja mappé sur "+existant+" et ne peut plus l'être sur "+nouveau);
                        }
                        /* Prendre l'annotation */
                        
                        res.put(url,new Mapping(c,method.getName()));
                    }
                }
            }
        return res;
    }

    public static Object[] getParameters(Method method, HttpServletRequest request, PrintWriter out) throws ServletException, Exception {
        // Get parameter types and values from the request using annotations
        Parameter[] parameters = method.getParameters();
        Object[] parameterValues = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            String paramName = "";
            Annotation[] annotations = parameters[i].getAnnotations();
            if (annotations.length > 0) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof RequestParam) {
                        paramName = ((RequestParam) annotation).value();
                    }
                }
            } else if(annotations.length == 0 && parameters[i].getType() != MySession.class) {
                paramName = parameters[i].getName();
                ModelView modelView = new ModelView();
                modelView.setUrl("/erreur.jsp");
                modelView.add("message", "ETU002557 SANS ANNOTATION");
                parameterValues[0] = modelView;
                return parameterValues;
            }

            if (parameters[i].getType() == String.class ||
                parameters[i].getType() == int.class ||
                parameters[i].getType() == double.class) {

                String paramValue = request.getParameter(paramName);
                if (parameters[i].getType() == String.class) {
                    parameterValues[i] = paramValue;
                } else if (parameters[i].getType() == int.class || parameters[i].getType() == Integer.class) {
                    parameterValues[i] = Integer.parseInt(paramValue);
                } else if (parameters[i].getType() == double.class || parameters[i].getType() == Double.class) {
                    parameterValues[i] = Double.parseDouble(paramValue);
                }

            } else if (parameters[i].getType() == MySession.class) {
                parameterValues[i] = new MySession(request.getSession());
            } else {
                Class<?> laclasse = Class.forName(parameters[i].getType().getName());
                Object newInstance = laclasse.getDeclaredConstructor().newInstance();
                Field[] attributs = laclasse.getDeclaredFields();
                for (Field field : attributs) {
                    if (field.getType().equals(MySession.class)) {
                        field.setAccessible(true);
                        field.set(newInstance,new MySession(request.getSession(true)));
                    }
                }
                Object[] attributsvalue = new Object[attributs.length];

                for (int j = 0; j < attributs.length; j++) {
                    attributs[j].setAccessible(true);

                    out.println(paramName + "." + attributs[j].getName());

                    String attvalue = request.getParameter(paramName + "." + attributs[j].getName());
                    out.println(attvalue);

                    if (attributs[j].getType() == String.class) {
                        attributsvalue[j] = attvalue;
                    } else if (attributs[j].getType() == int.class || attributs[j].getType() == Integer.class) {
                        attributsvalue[j] = Integer.parseInt(attvalue);
                    } else if (attributs[j].getType() == double.class || attributs[j].getType() == Double.class) {
                        attributsvalue[j] = Double.parseDouble(attvalue);
                    } 
                    else {
                        throw new ServletException("L'objet ne peut pas avoir d'objet en tant que paramètre");
                    }
                    attributs[j].set(newInstance, attributsvalue[j]);
                }
                parameterValues[i] = newInstance;
            }
        }
        return parameterValues;
    }
    
    public static Object callMethod(String className,String methodName,String path,HttpServletRequest request,PrintWriter out) throws ServletException,Exception{
        Class<?> laclasse=Class.forName(className);
        Method method=null;
        for (Method m : laclasse.getMethods()) {
            if (m.getName().equals(methodName)) {
                    Annotation[] annotations =  m.getAnnotations();
                    if (annotations[0] instanceof GetMapping) {
                        String name = ((GetMapping) annotations[0]).url();
                        System.out.println(name);
                        System.out.println(path);
                        if (name.equals(path)) {
                            method = m;
                        }
                   }
            }
        }
        if (method == null) {
            throw new ServletException("No such method" + methodName);
        }
        Object[] paramValues=getParameters(method,request,out);
        if(paramValues[0] instanceof ModelView){
            return paramValues[0];
        }

        Object objet=method.invoke(laclasse.getConstructor().newInstance(), paramValues );
        return objet;

    }

    
       public static Object findAndCallMethod(HashMap<String,Mapping> map,String path,HttpServletRequest request,PrintWriter out)throws ServletException,Exception{
        if(map.containsKey(path)){
            Mapping m=map.get(path);
            return Utils.callMethod(m.getClassName(),m.getMethodName(),path,request,out);
        }
        else{
            throw new ServletException("No Such method "+path);
        }

    }

    public String getURIWithoutContextPath(HttpServletRequest request){
        return  request.getRequestURI().substring(request.getContextPath().length());
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
    
   


        public static void ProcessMethod(HashMap<String,Mapping> map,String path,HttpServletRequest request,HttpServletResponse response,PrintWriter out)
    {
        try {
            Object objet=Utils.findAndCallMethod(map, path,request,out);       
            if (objet instanceof String) {
                out.println(objet.toString());
            }
            else if (objet instanceof ModelView) {
                HashMap<String,Object> hash=((ModelView)objet).getData();
                for (String string : hash.keySet()) {
                    request.setAttribute(string, hash.get(string));
                    out.println(string);
                }
                String view=((ModelView)objet).getUrl();
                out.println(view);
                request.getRequestDispatcher(view).forward(request, response);
            }
            else{
                throw new ServletException("type de retour non reconnu");
            }
            } catch (Exception e) {
            out.println(e.getLocalizedMessage());
        }
    }
}
