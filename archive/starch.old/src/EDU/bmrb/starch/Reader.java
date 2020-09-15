/*
 * Reader.java
 *
 * Created on December 22, 2004, 5:43 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/Reader.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/04/13 17:37:07 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Reader.java,v $
 * Revision 1.1  2005/04/13 17:37:07  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch;

/**
 * STARch input reader.
 *
 * @author  dmaziuk
 */
public interface Reader {
    public void parse( java.io.BufferedReader in );
    public boolean parsedOk();
    public String getMessage();
}
