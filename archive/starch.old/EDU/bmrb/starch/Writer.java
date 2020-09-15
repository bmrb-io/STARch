/*
 * Writer.java
 *
 * Created on December 8, 2004, 5:13 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Writer.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/12/08 23:57:49 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Writer.java,v $
 * Revision 1.1  2004/12/08 23:57:49  dmaziuk
 * added new classes
 * */

package EDU.bmrb.starch;

/**
 * STARch output writer.
 *
 * @author  dmaziuk
 */
public interface Writer {
    /** tab width */
    public static final int TABWIDTH = 4;
//******************************************************************************
    /** Prints table out.
     * @param out output stream
     * @throws java.sql.SQLException
     */
    public void print( java.io.PrintWriter out ) throws java.sql.SQLException;
    /** Sets "print sf id's" flag.
     * @param flag true or false
     */
    public void setSfIdFlag( boolean flag );
}
