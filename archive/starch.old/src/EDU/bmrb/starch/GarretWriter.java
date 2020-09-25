/*
 * GarretWriter.java
 *
 * Created on December 15, 2004, 6:02 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/GarretWriter.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/04/13 17:37:06 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: GarretWriter.java,v $
 * Revision 1.1  2005/04/13 17:37:06  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch;

/**
 * Prints out chemical shift loop in Garret format.
 * <P>
 * Garret format example is available in CAMRA documentation at 
 * <A href="http://www.pence.ca/software/camra/">www.pence.ca/software/camra/</A>.
 * Record format:<pre>
   1 ALA
   HA 4.130
   C  180.000
   ; 0.0
  </pre>
 * Shift values of 999.99 and -999.99 denote an unknown value.
 *
 * @author  dmaziuk
 */
public class GarretWriter implements Writer {
    private final static boolean DEBUG = false; //true;
    /** DB storage */
    private LoopTable fLt = null;
//******************************************************************************
    /** Creates a new instance of GarretWriter.
     * @param lt loop table
     */
    public GarretWriter( LoopTable lt ) {
        fLt = lt;
    } //************************************************************************
    /** No-op */
    public void setSfIdFlag(boolean flag) {
    } //************************************************************************
    public void print(java.io.PrintWriter out) throws java.sql.SQLException {
        int seq = -1;
        java.sql.Statement query = fLt.getConnection().createStatement(
        java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( "SELECT SEQID,COMPID,ATOMID," +
        "SVALUE,ID FROM LOOP ORDER BY ID" );
        while( rs.next() ) {
            if( seq != rs.getInt( 1 ) ) { // next residue
                if( seq != -1 ) out.println( GarretReader.ENDREC );
                seq = rs.getInt( 1 );
                out.print( seq );
                out.print( ' ' );
                out.println( rs.getString( 2 ) );
            }
            out.print( rs.getString( 3 ) );
            out.print( ' ' );
            out.println( rs.getString( 4 ) );
        }
        rs.close();
        query.close();
        out.flush();
    } //************************************************************************
}
