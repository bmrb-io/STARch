/*
 * StringTable.java
 *
 * Created on May 16, 2005, 2:42 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/StringTable.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/05/20 23:07:52 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: StringTable.java,v $
 * Revision 1.1  2005/05/20 23:07:52  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch;

/**
 * A table of string values backed by pool of unique strings.
 * <P>
 * This table does not support column insertion.
 * @author  dmaziuk
 */
public class StringTable {
    private static final boolean DEBUG = true; //false;
    /** def. tab */
    public static final int TABWIDTH = 4;
    /** row sorter */
    class RowSorter implements java.util.Comparator {
        /** compares two rows.
         * @return integer
         */
        public int compare(Object obj, Object obj1) {
            if( obj == obj1 ) return 0;
            if( obj == null ) return -1;
            if( obj1 == null ) return 1;
            if( ! (obj instanceof String []) ) return -1;
            if( ! (obj1 instanceof String []) ) return 1;
            if( fIndexCol < 0 ) return 0;
            String str = ((String []) obj)[fIndexCol];
            int idx = -1;
            try { idx = Integer.parseInt( str ); }
            catch( NumberFormatException e ) { return -1; }
            str = ((String []) obj1)[fIndexCol];
            int idx1 = -1;
            try { idx1 = Integer.parseInt( str ); }
            catch( NumberFormatException e ) { return 1; }
            if( idx < idx1 ) return -1;
            else if( idx > idx1 ) return 1;
            else return 0;
        }
    }
    /** String pool */
    private EDU.bmrb.lib.StringPool fPool = null;
    /** column headers */
    private String [] fCols = null;
    /** column widths */
    private int [] fWidths = null;
    /** rows */
    private java.util.List fRows = null;
    /** index column */
    private int fIndexCol = -1;
//******************************************************************************
    /** Creates a new instance of StringTable.
     * @param cols column headers
     */
    public StringTable( String [] cols ) {
        fPool = new EDU.bmrb.lib.StringPool();
        fCols = cols;
        fWidths = new int[fCols.length];
        for( int i = 0; i < fWidths.length; i++ ) fWidths[i] = 0;
        fRows = new java.util.ArrayList();
    } //************************************************************************
    /** Flags index column.
     * @param col column name
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setIndexColumn( String col ) {
        for( int i = 0; i < fCols.length; i++ )
            if( fCols[i].equals( col ) ) {
                fIndexCol = i;
                return;
            }
        throw new ArrayIndexOutOfBoundsException( "Invalid column " + col );
    } //************************************************************************
    /** Adds row of dots */
    public void addRow() {
        String [] newrow = new String[fCols.length];
        for( int i = 0; i < newrow.length; i++ )
            newrow[i] = fPool.add( "." );
        fRows.add( newrow );
    } //************************************************************************
    /** Returns column index.
     * @param col column name
     * @return column index or -1
     */
    public int getColumnIndex( String col ) {
        for( int i = 0; i < fCols.length; i++ )
            if( fCols[i].equals( col ) )
                return i;
        return -1;
    } //************************************************************************
    /** Returns value at specified location.
     * @param row row number
     * @param col column number
     * @return value
     * @throws ArrayIndexOutOfBoundsException
     */
    public String get( int row, int col ) {
        if( (row < 0) || (row >= fRows.size()) )
            throw new ArrayIndexOutOfBoundsException( "Invalid row " + row );
        if( (col < 0) || (col >= fCols.length) )
            throw new ArrayIndexOutOfBoundsException( "Invalid column " + col );
        return ((String []) fRows.get( row ))[col];
    } //************************************************************************
    /** Returns value at specified location.
     * @param row row number
     * @param col column name
     * @return value
     * @throws ArrayIndexOutOfBoundsException
     */
    public String get( int row, String col ) {
        for( int i = 0; i < fCols.length; i++ )
            if( fCols[i].equals( col ) )
                return get( row, i );
        throw new ArrayIndexOutOfBoundsException( "Invalid column " + col );
    } //************************************************************************
    /** Stores value at specified location.
     * @param row row number
     * @param col column number
     * @param val value
     * @throws ArrayIndexOutOfBoundsException
     */
    public void put( int row, int col, String val ) {
        if( (row < 0) || (row >= fRows.size()) )
            throw new ArrayIndexOutOfBoundsException( "Invalid row " + row );
        if( (col < 0) || (col >= fCols.length) )
            throw new ArrayIndexOutOfBoundsException( "Invalid column " + col );
        String [] r = (String []) fRows.get( row );
        r[col] = fPool.add( val );
        String str = EDU.bmrb.nmrstar.utils.QuoteString.quoteForSTAR( val );
        if( str.length() > fWidths[col] ) fWidths[col] = str.length();
    } //************************************************************************
    /** Stores value at specified location.
     * @param row row number
     * @param col column name
     * @param val value
     * @throws ArrayIndexOutOfBoundsException
     */
    public void put( int row, String col, String val ) {
        for( int i = 0; i < fCols.length; i++ )
            if( fCols[i].equals( col ) ) {
                put( row, i, val );
                return;
            }
        throw new ArrayIndexOutOfBoundsException( "Invalid column " + col );
    } //************************************************************************
    /** Sorts rows. */
    public void sort() {
        java.util.Collections.sort( fRows, new RowSorter() );
    } //************************************************************************
    /** Reindex rows */
    public void reindex() {
        if( fIndexCol < 0 ) return;
        String [] r;
        int row = 1;
        for( java.util.Iterator itr = fRows.iterator(); itr.hasNext(); ) {
            r = (String []) itr.next();
            r[fIndexCol] = Integer.toString( row );
            if( Integer.toString( row ).length() > fWidths[fIndexCol] )
                fWidths[fIndexCol] = Integer.toString( row ).length();
            row++;
        }
    } //************************************************************************
    /** Removes all rows */
    public void clear() {
        fRows.clear();
        fPool.clear();
    } //************************************************************************
    /** Prints table out
     * @param out output stream
     */
    public void print( java.io.PrintStream out ) {
        print( new java.io.PrintWriter( out ) );
    } //************************************************************************
    /** Prints table out
     * @param out output stream
     */
    public void print( java.io.PrintWriter out ) {
if( DEBUG ) System.err.println( "Table.print()" );
if( DEBUG ) for( int k = 0; k < fCols.length; k++ ) System.err.println( fCols[ k ] );
if( DEBUG ) for( int k = 0; k < fWidths.length; k++ ) System.err.println( fWidths[ k ] );
        int i, j;
        for( i = 0; i < TABWIDTH; i++ ) out.print( ' ' );
        out.println( "loop_" );
        out.println();
// header
        for( i = 0; i < fCols.length; i++ ) {
            for( j = 0; j < (2 * TABWIDTH); j++ ) out.print( ' ' );
            out.println( fCols[i] );
        }
        out.println();
// body
        String [] r;
        String val;
        for( java.util.Iterator itr = fRows.iterator(); itr.hasNext(); ) {
            r = (String []) itr.next();
            for( j = 0; j < (2 * TABWIDTH); j++ ) out.print( ' ' );
            for( i = 0; i < r.length; i++ ) {
                val = EDU.bmrb.nmrstar.utils.QuoteString.quoteForSTAR( r[i] );
                out.print( val );
                if( i < (r.length - 1) ) { // trailing whitespace
                    if( val.length() < fWidths[i] )
                        for( j = val.length(); j < fWidths[i]; j++ )
                            out.print( ' ' );
                    for( j = 0; j < TABWIDTH; j++ ) out.print( ' ' );
                }
            }
            out.println();
        }
        out.println();
// end
        for( i = 0; i < TABWIDTH; i++ ) out.print( ' ' );
        out.println( "stop_" );
        out.println();
        out.flush();
    } //************************************************************************
    /** main method: testing */
    public static void main( String [] args ) {
        try {
            long start = System.currentTimeMillis();
            
            System.err.println( "Start mem total: " + Runtime.getRuntime().totalMemory()
            + ", free " +  Runtime.getRuntime().freeMemory() + ", used: " +
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) );
            StringTable tbl = null;
            java.io.BufferedReader in = new java.io.BufferedReader(
            new java.io.FileReader( args[0] ) );
            String str;
            String [] vals;
            int row = -1;
            java.util.ArrayList tags = new java.util.ArrayList();
            while( (str = in.readLine()) != null ) {
                if( str.trim().length() < 1 ) continue;
                if( str.trim().charAt( 0 ) == '#' ) continue;
                if( str.trim().equals( "loop_" ) ) continue;
                if( str.trim().equals( "stop_" ) ) continue;
                if( str.trim().charAt( 0 ) == '_' ) {
                    tags.add( str.trim() );
                    continue;
                }
                if( str.trim().charAt( 0 ) != '_' ) {
                    if( tbl == null ) {
                        String [] t = new String[tags.size()];
                        for( int i = 0; i < t.length; i++ ) t[i] = (String) tags.get( i );
                        tbl = new StringTable( t );
                    }
                    row++;
                    vals = str.trim().split( "\\s+" );
                    tbl.addRow();
                    for( int i = 0; i < vals.length; i++ )
                        tbl.put( row, i, vals[i] );
                }
            }
            in.close();
            System.err.println( "Done reading " + (System.currentTimeMillis() - start) );
            long mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            Runtime.getRuntime().gc();
            Thread.sleep( 100 );
            System.runFinalization();
            while( (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) < mem2 ) {
                Runtime.getRuntime().gc();
                System.runFinalization();
                Thread.sleep( 100 );
                mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            }
            System.err.println( "Parse mem total: " + Runtime.getRuntime().totalMemory()
            + ", free " +  Runtime.getRuntime().freeMemory() + ", used: " +
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) +
            "(" + (((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()))/1024.0/1024.0)
            + "M)" );

//            tbl.setIndexColumn( "_Coupling_constant.ID" );
            tbl.setIndexColumn( "_Atom_site.Atom_coordinate_ID" );
//            tbl.sort();
            tbl.reindex();
            tbl.print( System.out );
            tbl.clear();

            System.err.println( "Done " + (System.currentTimeMillis() - start) );
            mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            Runtime.getRuntime().gc();
            Thread.sleep( 100 );
            System.runFinalization();
            while( (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) < mem2 ) {
                Runtime.getRuntime().gc();
                System.runFinalization();
                Thread.sleep( 100 );
                mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            }
            System.err.println( "End mem total: " + Runtime.getRuntime().totalMemory()
            + ", free " +  Runtime.getRuntime().freeMemory() + ", used: " +
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) );

        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
}
