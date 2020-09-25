/*
 * PpmWriter.java
 *
 * Created on December 16, 2004, 1:08 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/PpmWriter.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/04/13 17:37:07 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: PpmWriter.java,v $
 * Revision 1.1  2005/04/13 17:37:07  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch;

/**
 * Prints out chemical shift loop in PPM format.
 * <P>
 * PPM format example is available in CAMRA documentation at 
 * <A href="http://www.pence.ca/software/camra/">www.pence.ca/software/camra/</A>.
 * Record format:<pre>
  molNum:Residue_ResId:atom  shift_value [ shift_value]
  </pre>
 * Lines starting with '!' are comments. Shift values of ***.**, 999.99,
 * and -999.99 denote an unknown value.
 *
 * @author  dmaziuk
 */
public class PpmWriter implements Writer {
    /** DB storage */
    private LoopTable fLt = null;
//******************************************************************************
    /** Creates a new instance of PpmWriter.
     * @param lt loop table
     */
    public PpmWriter( LoopTable lt ) {
        fLt = lt;
    } //************************************************************************
    /** No-op */
    public void setSfIdFlag(boolean flag) {
    } //************************************************************************
    public void print(java.io.PrintWriter out) throws java.sql.SQLException {
        java.sql.Statement query = fLt.getConnection().createStatement(
        java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT SEQID,COMPID,ATOMID," +
        "SVALUE,ID FROM LOOP ORDER BY ID" );
        while( rs.next() ) {
            out.print( "1:" );
            out.print( rs.getString( 2 ) );
            out.print( '_' );
            out.print( rs.getString( 1 ) );
            out.print( ':' );
            out.print( rs.getString( 3 ) );
            out.print( " \t\t" );
            out.println( rs.getString( 4 ) );
        }
        rs.close();
        query.close();
        out.flush();
    } //************************************************************************
}
