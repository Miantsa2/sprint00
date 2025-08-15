package com.framework.object;

import java.util.Set;


public class Mapping {
    private String className;
   // private String methodName;
    private Set<VerbMethod> verbmethods;

    // public Mapping(String className, String methodName) {
    //     this.className = className;
    //     this.methodName = methodName;
    // }


    public Mapping() {
    }
    public Mapping(String className, Set<VerbMethod> verbmethods) {
        setClassName(className);
        setVerbmethods(verbmethods);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }


    public Set<VerbMethod> getVerbmethods() {
        return verbmethods;
    }
    public void setVerbmethods(Set<VerbMethod> verbmethods) {
        this.verbmethods = verbmethods;
    }
   

    // public String getMethodName() {
    //     return methodName;
    // }

    // public void setMethodName(String methodName) {
    //     this.methodName = methodName;
    // }

   
}
