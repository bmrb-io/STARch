/*
 * Copyright (c) 2007 Board of Regents University of Wisconsin.
 * All Rights Reserved.
 */

package edu.bmrb.starch;

/**
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Jan 29, 2007
 * Time: 1:59:46 PM
 *
 * $Id$
 */

public class Pair<T,U> {
    private T fFirst = null;
    private U fSecond = null;
    public Pair( T first, U second ) {
        fFirst = first;
        fSecond = second;
    }
    public T getFirst() {
        return fFirst;
    }
    public U getSecond() {
        return fSecond;
    }
    public void setFirst( T first ) {
        fFirst = first;
    }
    public void setSecond( U second ) {
        fSecond = second;
    }
    public String toString() {
        return fFirst.toString() + ": " + fSecond.toString();
    }
}
