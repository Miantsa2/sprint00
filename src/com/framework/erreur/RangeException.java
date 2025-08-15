package com.framework.erreur;

import com.framework.annotations.*;


public class RangeException extends Exception {
    public RangeException(String paramName, Range range) {
        super("Le parametre " + paramName + " doit etre dans la plage [" + range.min()
        + "," + range.max() + "]");
    }
}
