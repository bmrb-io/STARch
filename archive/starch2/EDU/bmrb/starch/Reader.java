/*
 * Reader.java
 *
 * Created on December 22, 2004, 5:43 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source$
 * 
 * AUTHOR:      $Author$
 * DATE:        $Date$
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log$ */

package EDU.bmrb.starch;

/**
 * STARch input reader.
 *
 * @author  dmaziuk
 */
public interface Reader {
    public void parse( java.io.BufferedReader in );
    public boolean parsedOk();
}
