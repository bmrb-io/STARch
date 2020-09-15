/*
 * XeasyWriter.java
 *
 * Created on August 25, 2004, 2:32 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/XeasyWriter.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/04/13 17:37:08 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: XeasyWriter.java,v $
 * Revision 1.1  2005/04/13 17:37:08  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.3  2004/12/15 20:55:51  dmaziuk
 * new classes
 *
 * Revision 1.2  2004/12/14 00:40:25  dmaziuk
 * more functionality
 *
 * Revision 1.1  2004/12/08 23:57:50  dmaziuk
 * added new classes
 * */

package EDU.bmrb.starch;

/**
 * Prints out LoopTable in XEASY format.
 * Prints out a single file: sequences first, then a separator line, then protons.
 * @author  dmaziuk
 */
public class XeasyWriter implements Writer {
    /** separator line */
    public static final String SEPARATOR = 
"#########     EOF RESIDUES     #########\n#########    START PROTONS     #########";
    /** tab width */
    public static final int TABWIDTH = 4;
    /** table columns */
    public static final String [] DB_COLS = {
        "ID",
        "SVALUE",
        "SERROR",
        "ATOMID",
        "SEQID"
    };
  /** primary key column */
  public static final int ID_COL = 0;
    /** DB storage */
    private LoopTable fLt = null;
//******************************************************************************
    /** Creates a new instance of XeasyWriter.
     * @param lt loop table
     */
    public XeasyWriter( LoopTable lt ) {
        fLt = lt;
    } //************************************************************************
    /** Sets "print sf id's" flag.
     * Does nothing.
     * @param flag true or false
     */
    public void setSfIdFlag(boolean flag) {
    } //************************************************************************
    /** Prints table out in XEASY format.
     * @param out output srteam
     * @throws java.sql.SQLException
     */
    public void print( java.io.PrintWriter out ) throws java.sql.SQLException {
        printResidues( fLt, out );
        out.println();
        out.println( SEPARATOR );
        out.println();
        printProtons( fLt, out );
        out.println();
    } //************************************************************************
    /** Prints table out in XEASY format.
     * @param table loop table
     * @param out output srteam
     * @throws java.sql.SQLException
     */
    public static void print( LoopTable table, java.io.PrintWriter out )
    throws java.sql.SQLException {
        printResidues( table, out );
        out.println();
        out.println( SEPARATOR );
        out.println();
        printProtons( table, out );
        out.println();
    } //************************************************************************
    /** Prints out list of residues.
     * @param table loop table
     * @param out output srteam
     * @throws java.sql.SQLException
     */
    public static void printResidues( LoopTable table, java.io.PrintWriter out )
    throws java.sql.SQLException {
        java.sql.Statement query = table.getConnection().createStatement(
        java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( 
"SELECT DISTINCT SEQID,COMPID FROM LOOP ORDER BY SEQID" );
        while( rs.next() ) {
            out.print( rs.getString( 2 ) );
            for( int i = 0; i < TABWIDTH; i++ ) out.print( ' ' );
            out.println( rs.getString( 1 ) );
        }
        rs.close();
        query.close();
        out.flush();
    } //************************************************************************
    /** Prints out list of atoms.
     * @param table loop table
     * @param out output srteam
     * @throws java.sql.SQLException
     */
    public static void printProtons( LoopTable table, java.io.PrintWriter out )
    throws java.sql.SQLException {
        int [] widths = getColumnWidths( table );
        int row = 0;
        int i, j;
        String tmp;
        java.sql.Statement query = table.getConnection().createStatement(
        java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( 
        "SELECT ID,SVALUE,SERROR,ATOMID,SEQID FROM LOOP ORDER BY ID" );
        while( rs.next() ) {
            row++;
            for( i = 0; i < DB_COLS.length; i++ ) {
                if( i == ID_COL ) tmp = Integer.toString( row );
                else {
                    tmp = rs.getString( i + 1 );
                    if( rs.wasNull() ) tmp = ".";
                    else tmp = EDU.bmrb.nmrstar.utils.QuoteString.quoteForSTAR( tmp );
                }
                for( j = 0; j < widths[i] - tmp.length(); j++ ) out.print( ' ' );
                if( i > 0 ) for( j = 0; j < TABWIDTH; j++ ) out.print( ' ' );
                out.print( tmp );
            }
            out.println();
        }
        rs.close();
        query.close();
        out.flush();
    } //************************************************************************
    /** Returns aray of column max widths.
     * @param table loop table
     * @return array of column widths
     * @throws java.sql.SQLException
     */
    public static int [] getColumnWidths( LoopTable table )
    throws java.sql.SQLException {
        int [] rc = new int[DB_COLS.length];
        java.sql.ResultSet rs = null;
        java.sql.Statement query = table.getConnection().createStatement(
        java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
        StringBuffer sql = new StringBuffer();
        for( int i = 0; i < rc.length; i++ ) {
            sql.setLength( 0 );
            sql.append( "SELECT MAX(LENGTH(" );
            sql.append( DB_COLS[i] );
            sql.append( ")) FROM LOOP" );
//System.err.println( fBuf );
            rs = query.executeQuery( sql.toString() );
            if( rs.next() ) {
                rc[i] = rs.getInt( 1 );
                if( rs.wasNull() ) rc[i] = 0;
            }
            else rc[i] = 0;
        }
        rs.close();
        query.close();
//for( int i = 0; i < rc.length; i++ )
//System.err.println( rc[i] );
        return rc;
    } //************************************************************************
}
