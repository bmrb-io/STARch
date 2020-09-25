/*
 * Copyright (c) 2006 Board of Regents University of Wisconsin.
 * All Rights Reserved.
 */

package edu.bmrb.starch;

/**
 * Pair of strings.
 *
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 19, 2006
 * Time: 4:28:31 PM
 *
 * $Id$
 */

public class StringStringPair {
    private String fFirst = null;
    private String fSecond = null;
    public StringStringPair( String first, String second ) {
        fFirst = first;
        fSecond = second;
    }
    public String getFirst() {
        return fFirst;
    }
    public String getSecond() {
        return fSecond;
    }
    public void setFirst( String first ) {
        fFirst = first;
    }
    public void setSecond( String second ) {
        fSecond = second;
    }
    public String toString() {
        return fFirst + ":" + fSecond;
    }
}
