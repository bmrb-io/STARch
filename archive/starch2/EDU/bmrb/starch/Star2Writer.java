/*
 * Star2Writer.java
 *
 * Created on December 8, 2004, 5:00 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Star2Writer.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/12/14 00:40:25 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Star2Writer.java,v $
 * Revision 1.2  2004/12/14 00:40:25  dmaziuk
 * more functionality
 *
 * Revision 1.1  2004/12/08 23:57:49  dmaziuk
 * added new classes
 * */

package EDU.bmrb.starch;

/**
 * Prints out chemical shifts loop in NMR-STAR 2.1 format.
 * @author  dmaziuk
 */
public class Star2Writer implements Writer {
    private final static boolean DEBUG = false;
    /** loop_ */
    public static final String LOOP = "loop_";
    /** stop_ */
    public static final String STOP = "stop_";
    /** table columns */
    public static final String [] DB_COLS = {
        "ID",
        "SEQID",
        "COMPID",
        "ATOMID",
        "ATOMTYPE",
        "SVALUE",
        "SERROR",
        "SAMBICODE"
    };
    /** ID column index (NMR-STAR 2.1) */
    public static final int ID_COL = 0;
    /** DB storage */
    private LoopTable fLt = null;
    /** buffer */
    private StringBuffer fBuf = null;
//******************************************************************************
    /** Creates a new instance of Star2Writer.
     * @param lt loop table
     */
    public Star2Writer( LoopTable lt ) {
        fLt = lt;
        fBuf = new StringBuffer();
    } //************************************************************************
    /** Sets "print sf id's" flag.
     * Does nothing.
     * @param flag true or false
     */
    public void setSfIdFlag( boolean flag ) {
    } //************************************************************************
    /** Returns aray of column max widths.
     * @return array of column widths
     * @throws java.sql.SQLException
     */
    protected int [] getColumnWidths() throws java.sql.SQLException {
        int [] rc = new int[DB_COLS.length];
        java.sql.Statement stat = fLt.getConnection().createStatement();
if( DEBUG ) System.err.println( fBuf );
        stat.executeUpdate( "CREATE ALIAS LENGTH FOR \"EDU.bmrb.starch.LoopTable.strlen\"" );
        stat.getConnection().commit();
        java.sql.ResultSet rs = null;
        for( int i = 0; i < rc.length; i++ ) {
            fBuf.setLength( 0 );
            fBuf.append( "SELECT MAX(LENGTH(" );
            fBuf.append( DB_COLS[i] );
            fBuf.append( ")) FROM LOOP" );
if( DEBUG ) System.err.println( fBuf );
            rs = stat.executeQuery( fBuf.toString() );
            if( rs.next() ) {
                rc[i] = rs.getInt( 1 );
                if( rs.wasNull() ) rc[i] = 0;
            }
            else rc[i] = 0;
        }
        rs.close();
        stat.close();
if( DEBUG ) for( int i = 0; i < rc.length; i++ ) System.err.println( rc[i] );
        return rc;
    } //************************************************************************
    /** Prints table out as NMR-STAR 2.1 loop.
     * @param out output stream
     * @throws java.sql.SQLException
     */
    public void print( java.io.PrintWriter out ) throws java.sql.SQLException {
        int i, j;
        for( i = 0; i < TABWIDTH; i++ ) out.print( ' ' );
        out.println( LOOP );
        for( i = 0; i < LoopTable.LOOP_TAGS2.length; i++ ) {
            for( j = 0; j < TABWIDTH * 2; j++ ) out.print( ' ' );
            out.println( LoopTable.LOOP_TAGS2[i] );
        }
        out.println();
        printBody( out );
        for( i = 0; i < TABWIDTH; i++ ) out.print( ' ' );
        out.println();
        out.println( STOP );
        out.flush();
    } //************************************************************************
    /** Prints data out as NMR-STAR 2.1 loop body.
     * @param out output stream
     * @throws java.sql.SQLException
     */
    public void printBody( java.io.PrintWriter out ) throws java.sql.SQLException {
        String tmp;
        int i, j;
        int [] widths = getColumnWidths();
        fBuf.setLength( 0 );
        fBuf.append( "SELECT " );
        for( i = 0; i < DB_COLS.length; i++ ) {
            fBuf.append( DB_COLS[i] );
            if( i < DB_COLS.length - 1 ) fBuf.append( ',' );
        }
        fBuf.append( " FROM LOOP ORDER BY " );
        fBuf.append( DB_COLS[ID_COL] );
        java.sql.Statement query = fLt.getConnection().createStatement(
        java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( fBuf.toString() );
        int rownum = 0;
        while( rs.next() ) {
            rownum++;
            for( i = 0; i < DB_COLS.length; i++ ) {
                if( i == ID_COL ) tmp = Integer.toString( rownum );
                else {
                    tmp = rs.getString( i + 1 );
                    if( rs.wasNull() ) tmp = ".";
                    else tmp = EDU.bmrb.nmrstar.utils.QuoteString.quoteForSTAR( tmp );
                }
                for( j = 0; j < widths[i] - tmp.length(); j++ ) out.print( ' ' );
                for( j = 0; j < TABWIDTH; j++ ) out.print( ' ' );
                out.print( tmp );
            }
            out.println();
        }
    } //************************************************************************
}
