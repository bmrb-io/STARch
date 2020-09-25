/*
 * Star3Writer.java
 *
 * Created on December 8, 2004, 4:47 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Star3Writer.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/12/15 20:55:51 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Star3Writer.java,v $
 * Revision 1.3  2004/12/15 20:55:51  dmaziuk
 * new classes
 *
 * Revision 1.1  2004/12/08 23:57:49  dmaziuk
 * added new classes
 * */

package EDU.bmrb.starch;

/**
 * Prints out chemical shifts loop in NMR-STAR 3.0 format.
 * @author  dmaziuk
 */
public class Star3Writer implements Writer {
    private final static boolean DEBUG = false; //true;
    /** loop_ */
    public static final String LOOP = "loop_";
    /** stop_ */
    public static final String STOP = "stop_";
    /** DB storage */
    private LoopTable fLt = null;
    /** buffer */
    private StringBuffer fBuf = null;
    /** if true, print out .Sf_ID tags */
    private boolean fPrintSfids = false;
//******************************************************************************
    /** Creates a new instance of Star3Writer.
     * @param lt loop table
     */
    public Star3Writer( LoopTable lt ) {
        fLt = lt;
        fBuf = new StringBuffer();
    } //************************************************************************
    /** Sets "print sf id's" flag.
     * @param flag true or false
     */
    public void setSfIdFlag( boolean flag ) {
        fPrintSfids = flag;
if( DEBUG ) System.err.println( "***** Sf ID flag is now " + fPrintSfids );
    } //************************************************************************
    /** Returns aray of column max widths.
     * @return array of column widths
     * @throws java.sql.SQLException
     */
    protected int [] getColumnWidths() throws java.sql.SQLException {
        int [] rc;
        if( fPrintSfids ) rc = new int[LoopTable.DB_COLS.length];
        else rc = new int[LoopTable.DB_COLS.length - 1];
if( DEBUG ) System.err.println( "*** Array size is " + rc.length );
        java.sql.Statement stat = fLt.getConnection().createStatement();
if( DEBUG ) System.err.println( fBuf );
        stat.executeUpdate( "CREATE ALIAS LENGTH FOR \"EDU.bmrb.starch.LoopTable.strlen\"" );
        stat.getConnection().commit();
        java.sql.ResultSet rs = null;
        for( int i = 0, j = 0; i < LoopTable.DB_COLS.length; i++ ) {
            if( (! fPrintSfids ) && (i == LoopTable.SFID_COL ) ) continue;
            fBuf.setLength( 0 );
            fBuf.append( "SELECT MAX(LENGTH(CAST(" );
            fBuf.append( LoopTable.DB_COLS[i] );
            fBuf.append( " AS VARCHAR))) FROM LOOP" );
if( DEBUG ) System.err.println( fBuf );
            rs = stat.executeQuery( fBuf.toString() );
            if( rs.next() ) {
                rc[j] = rs.getInt( 1 );
                if( rs.wasNull() ) rc[j] = 0;
if( DEBUG ) System.err.println( "MAX length of " + LoopTable.DB_COLS[i] + "(" + i + ") = " + rc[j] );
            }
            else rc[j] = 0;
            j++;
        }
        rs.close();
        stat.close();
if( DEBUG ) for( int i = 0; i < rc.length; i++ ) System.err.println( rc[i] );
        return rc;
    } //************************************************************************
    /** Prints table out as NMR-STAR 3.0 loop.
     * @param out output stream
     * @throws java.sql.SQLException
     */
    public void print( java.io.PrintWriter out ) throws java.sql.SQLException {
        int i, j;
        for( i = 0; i < TABWIDTH; i++ ) out.print( ' ' );
        out.println( LOOP );
        for( i = 0; i < LoopTable.LOOP_TAGS3.length; i++ ) {
            if( (! fPrintSfids ) && (i == LoopTable.SFID_COL) ) continue;
            for( j = 0; j < TABWIDTH * 2; j++ ) out.print( ' ' );
            out.println( LoopTable.LOOP_TAGS3[i] );
        }
        out.println();
        printBody( out );
        for( i = 0; i < TABWIDTH; i++ ) out.print( ' ' );
        out.println();
        out.println( STOP );
        out.flush();
    } //************************************************************************
    /** Prints data out as NMR-STAR 3.0 loop body.
     * @param out output stream
     * @throws java.sql.SQLException
     */
    public void printBody( java.io.PrintWriter out ) throws java.sql.SQLException {
        String tmp;
        int i, j;
        int [] widths = getColumnWidths();
        fBuf.setLength( 0 );
        fBuf.append( "SELECT " );
        for( i = 0; i < LoopTable.DB_COLS.length; i++ ) {
            if( (! fPrintSfids ) && (i == LoopTable.SFID_COL ) ) continue;
            fBuf.append( LoopTable.DB_COLS[i] );
            if( i < LoopTable.DB_COLS.length - 1 ) fBuf.append( ',' );
        }
        fBuf.append( " FROM LOOP ORDER BY " );
        fBuf.append( LoopTable.DB_COLS[LoopTable.ID_COL] );
if( DEBUG ) System.out.println( fBuf );
        java.sql.Statement query = fLt.getConnection().createStatement(
        java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( fBuf.toString() );
        java.sql.ResultSetMetaData rmd = rs.getMetaData();
        while( rs.next() ) {
            for( j = 0; j < TABWIDTH; j++ ) out.print( ' ' );
            for( i = 0; i < rmd.getColumnCount(); i++ ) {
                tmp = rs.getString( i + 1 );
if( DEBUG ) System.out.print( "rs.getString( " + (i + 1) + ") = " + rs.getString( i + 1 ) );
                if( rs.wasNull() ) tmp = ".";
                else tmp = EDU.bmrb.nmrstar.utils.QuoteString.quoteForSTAR( tmp );
if( DEBUG ) System.out.println( ", now tmp = " + tmp + ": " + tmp.length() + " max: " + widths[i] );
                for( j = 0; j < widths[i] - tmp.length(); j++ ) out.print( ' ' );
if( DEBUG ) System.out.println( "Padding from 0 to " + (widths[i] - tmp.length()) );
                out.print( tmp );
                for( j = 0; j < TABWIDTH; j++ ) out.print( ' ' );
            }
            out.println();
        }
    } //************************************************************************
}
