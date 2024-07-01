tutorial to use the framework:

-ajouter le Sprint1.jar dans le lib 
-creer web.xml
-mapper un servlet comme suis 
<servlet>
    <servlet-name>FrontController</servlet-name>
    <servlet-class>mg.itu.prom16.controller.FrontController</servlet-class>
    <init-param>
        <param-name>package_name</param-name>
        <param-value>mg.itu.prom16.controllers</param-value>
    </init-param>
</servlet>
<servlet-mapping>
    <servlet-name>FrontController</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>

-creer un classe dans un package mg.itu.prom16.controllers et mapper @Controller
importer les classes annotatees
-mapper les methodes souhaitez avec @GetMapping retournant soit ModelView soit String
    ==>cas ModelView creer une fonction qui retoune modelview 
        utiliser add pour ajouter un objet au mode and view  
        utiliser seturl pour configurer l url ou sera dirige le modelview
    ==>cas String 
        le resultat de la methode sera affiche  directementb dans le navigateur
-creer erreur.jsp pour afficher les erreurs dans le cas ou les parametres d'une fonction ne sont pas annoter 