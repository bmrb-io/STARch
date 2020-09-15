/*
 * TableToLoop.java
 *
 * Created on May 20, 2005, 5:38 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/conv/TableToLoop.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/06/20 22:29:31 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: TableToLoop.java,v $
 * Revision 1.2  2005/06/20 22:29:31  dmaziuk
 * vacation
 *
 * Revision 1.1  2005/05/20 23:07:53  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch.conv;
import EDU.bmrb.starch.*;
/**
 * Converts tab or space-delimited table to NMR-STAR 3 loop.
 * @author  dmaziuk
 */
public class TableToLoop {
    private static final boolean DEBUG = true; //false;
    /** Loop table */
    private StringTable fTbl = null;
//******************************************************************************
    /** Creates a new instance of TableToLoop */
    public TableToLoop() {
    } //************************************************************************
    /** Parses input file.
     * Closes input stream when done.
     * @param tags dictionary tags
     * @param in input stream.
     */
    public void read( String [] tags, java.io.BufferedReader in ) 
    throws java.io.IOException {
        String str;
        int lineno = 0;
        int i;
        String [] cols = null;
// read header
        while( (str = in.readLine()) != null ) {
            lineno++;
            str = str.trim();
            if( str.length() < 1 ) continue;
            cols = str.split( "\\s+" );
            break;
        }
if( DEBUG ) {
    System.err.println( "Dictionary tags: " );
    for( int k = 0; k < tags.length; k++ ) System.err.println( tags[k] );
    System.err.println( "File tags: " );
    for( int k = 0; k < cols.length; k++ ) System.err.println( cols[k] );
}
// sanity checks
        if( cols.length > tags.length )
            throw new IllegalArgumentException( "Too many columns" );
        boolean found;
        for( i = 0; i < cols.length; i++ ) {
if( DEBUG ) System.err.println( "Looking for " + cols[i] );
            found = false;
            for( int j = 0; j < tags.length; j++ ) {
// if( DEBUG ) System.err.println( "* checking " + tags[j] );
                if( tags[j].equals( cols[i] ) ) {
if( DEBUG ) System.err.println( "** got it" );
                    found = true;
                    break;
                }
            }
            if( ! found )
                throw new IllegalArgumentException( "Invalid tag: " + cols[i] );
        }
        fTbl = new StringTable( tags );
        String [] data;
        int row = -1;
// read data
        while( (str = in.readLine()) != null ) {
            lineno++;
            fTbl.addRow();
            row++;
            str = str.trim();
            if( str.length() < 1 ) continue;
            data = str.split( "\\s+" );
            if( data.length != cols.length )
                throw new java.io.IOException( "Wrong number of values in line " + lineno );
            for( i = 0; i < cols.length; i++ )
                fTbl.put( row, cols[i], data[i] );
if( DEBUG ) {
    System.err.println( "Loop row: " );
    for( int k = 0; k < data.length; k++ )
        System.err.println( data[k] );
}
        } // endwhile
        in.close();
    } //************************************************************************
    /** Prints table.
     * @param out output stream
     */
    public void print( java.io.PrintStream out ) {
        fTbl.print( out );
    } //************************************************************************
    /** Prints table.
     * @param out output stream
     */
    public void print( java.io.PrintWriter out ) {
        fTbl.print( out );
    } //************************************************************************
    /** Prints usage summary */
    public static void usage() {
        System.err.println( "Usage: java EDU.bmrb.starch.conv.TableToLoop [-c FILE] " +
        "<-i FILE> [-o FILE]" );
        System.err.print( "-c FILE: config file (default: " );
        System.err.print( System.getProperty( "user.home" ) );
        System.err.print( java.io.File.separator );
        System.err.print( Dictionary.DEF_PROPFILE );
        System.err.println( ')' );
        System.err.println( "-i FILE: input file (required)" );
        System.err.println( "-o FILE: output file (default: stdout)" );
    } //************************************************************************
    /** Main method
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String propfile = null;
            String infile = null;
            String outfile = null;
            int opt;
            gnu.getopt.Getopt g = new gnu.getopt.Getopt( "TableToLoop", args, "c:i:o:h" );
            while( (opt = g.getopt()) != -1 ) {
                switch( opt ) {
                    case 'c' : // config file
                        propfile = g.getOptarg();
                        break;
                    case 'i' : // input file
                        infile = g.getOptarg();
                        break;
                    case 'o' : // output file
                        outfile = g.getOptarg();
                        break;
                    default : 
                        usage();
                        System.exit( 2 );
                }
            }
            if( infile == null ) {
                usage();
                System.exit( 2 );
            }
// init properties
            if( propfile == null ) propfile = System.getProperty( "user.home" ) +
            java.io.File.separator + Dictionary.DEF_PROPFILE;
            java.util.Properties props = new java.util.Properties();
            java.io.InputStream pin = new java.io.FileInputStream( propfile );
            props.load( pin );
            pin.close();
            pin = null;
// determine data type
            java.io.BufferedReader in = new java.io.BufferedReader(
            new java.io.FileReader( infile ) );
            in.mark( 4096 );
            String str = in.readLine();
            while( (str != null) && (str.trim().length() < 1) )
                str = in.readLine();
            String [] tags = str.trim().split( "\\s+" );
            int pos = tags[0].indexOf( '.' );
            if( pos < 0 ) {
                System.err.println( "Invalid tag: " + tags[0] );
                System.exit( 3 );
            }
            str = tags[0].substring( 0, pos + 1 );
if( DEBUG ) System.err.println( "tag category: " + str );
            in.reset();
// fetch loop tags
            tags = Dictionary.getLoopTags( props, str );
            if( tags == null ) {
                System.err.println( "Tag not in dictionary: " + tags[0] );
                System.exit( 4 );
            }
            props.clear();
            props = null;
// convert
            TableToLoop conv = new TableToLoop();
            conv.read( tags, in );
            in.close();
// print
            java.io.PrintWriter out;
            if( outfile == null ) out = new java.io.PrintWriter( System.out );
            else out = new java.io.PrintWriter( new java.io.FileOutputStream( outfile ) );
            conv.print( out );
            out.flush();
            if( outfile != null ) out.close();
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
            System.exit( 1 );
        }
    } //************************************************************************
}
