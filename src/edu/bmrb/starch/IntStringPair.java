package edu.bmrb.starch;

/**
 * Pair class.
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 6, 2006
 * Time: 4:18:07 PM
 *
 * $Id$
 */

public class IntStringPair {
    int fNum = -1;
    String fStr = null;
    public IntStringPair( int num, String str ) {
        fNum = num;
        fStr = str;
    }
    public int getInt() {
        return fNum;
    }
    public String getString() {
        return fStr;
    }
    public void setInt( int i ) {
        fNum = i;
    }
    public void setString( String s ) {
        fStr = s;
    }
}
