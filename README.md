tutorial to use the framework:

-ajouter le sprint.jar dans le lib 
-creer web.xml
-mapper un servlet comme suis 
<servlet>
    <servlet-name>FrontController</servlet-name>
    <servlet-class>controller.FrontController</servlet-class>
    <init-param>
        <param-name>package_name</param-name>
        <param-value>wcontroller</param-value>
    </init-param>
</servlet>
<servlet-mapping>
    <servlet-name>FrontController</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>

-creer un classe dans un package wcontroller et mapper @Controller
importer les calsse annotations
-mapper les methode souhaitez avec @GetMapping retoune soit ModelView soit String
    ==>cas MOdelandView creet une fonction qui retoune model and view 
        utiliser add pour ajouter un objet au mode and view  
        utiliser seturl pour configurer l url ou sera dirige le mode and view
    ==>cas String 
        le resultat de la methode sera affivhe dams le navigateur


-les attributs de fonction doivent etre mappe avec requestParam(value="nom de l attribut") 
Rajouer -parameters dans la compilation pour eviter de necessairement mettre un mapping


-utilisation de session :
    ==>mettre la classe mysession comme attribut de la classe controller ne pas l initilaliser
    ==>utiliser les methodede mysession pour acceder a HTTPsession en back:
        -add()
        -get()
        -delete()