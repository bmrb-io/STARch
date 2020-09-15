/*
 * InsertLabels.java
 *
 * Created on April 7, 2005, 2:35 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/InsertLabels.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2006/01/25 18:56:38 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: InsertLabels.java,v $
 * Revision 1.4  2006/01/25 18:56:38  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.3  2005/06/20 22:29:31  dmaziuk
 * vacation
 *
 * Revision 1.2  2005/05/20 23:07:52  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.1  2005/04/13 17:37:06  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch;

/**
 * Insert residue labels into data file.
 * Inserts a column of residue labels into a space (tab) -delimited file.
 * <P>
 * Command line arguments
 * <UL>
 *  <LI>-d FILE: data file</LI>
 *  <LI>-l FILE: residue label file</LI>
 *  <LI>-r NUM: residue sequence column number in data file</LI>
 *  <LI>-c NUM: insert residue labels into column NUM</LI>
 *  <LI>-o FILE: output file (default: stdout)</LI>
 * </UL>
 * <P>
 * File format: space or tab-delimited tables. Residue label file must have
 * residue labels in the first column, one label per line. It may have a second
 * column: residue sequence numbers. If no sequence numbers are given, program
 * uses residue count (counting from one).
 *
 * @author  dmaziuk
 */
public class InsertLabels {
    private final static boolean DEBUG = false;
//******************************************************************************
    /** Creates a new instance of InsertLabels */
    public InsertLabels() {
    } //************************************************************************
    /** Prints out usage sumary */
    public static void usage() {
        System.err.println( "Usage: java EDU.bmrb.starch.InsertLabels <-d FILE> " +
        "<-l FILE> <-r NUM> [-c NUM] [-o FILE]" );
        System.err.println( "-d FILE: data file" );
        System.err.println( "-l FILE: residue label file" );
        System.err.println( "-r NUM: residue sequence column number in data file" );
        System.err.println( "-c NUM: insert residue labels into column NUM (default: last column)" );
        System.err.println( "-o FILE: output file (default: stdout)" );
    } //************************************************************************
    /** Main method.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            gnu.getopt.Getopt g = new gnu.getopt.Getopt( "InsertLabels", args, "c:d:l:o:r:h" );
            int opt;
            int target_col = -1;
            int seq_col = -1;
            String datafile = null;
            String labelfile = null;
            String outfile = null;
            while( (opt = g.getopt()) != -1 ) {
                switch( opt ) {
                    case 'c' : // target column
                        try { target_col = Integer.parseInt( g.getOptarg() ); }
                        catch( NumberFormatException e ) {
                            System.err.println( "Not a number (-c): " + g.getOptarg() );
                            usage();
                            System.exit( 2 );
                        }
                        break;
                    case 'd' : // data table
                        datafile = g.getOptarg();
                        break;
                    case  'l' : // label file
                        labelfile = g.getOptarg();
                        break;
                    case 'o' : // output file
                        outfile = g.getOptarg();
                        break;
                    case 'r' : // sequence column
                        try { seq_col = Integer.parseInt( g.getOptarg() ); }
                        catch( NumberFormatException e ) {
                            System.err.println( "Not a number (-r): " + g.getOptarg() );
                            usage();
                            System.exit( 3 );
                        }
                        break;
                    default : 
                        usage();
                        System.exit( 4 );
                }
            }
            if( (datafile == null) || (labelfile == null) || (seq_col < 1) ) {
                usage();
                System.exit( 5 );
            }
            String str;
            Integer key;
            String [] fields;
            int lineno = 1;
            java.util.HashMap map = new java.util.HashMap();
            java.io.BufferedReader in = new java.io.BufferedReader(
            new java.io.FileReader( labelfile ) );
            while( (str = in.readLine()) != null ) {
	        str = str.trim();
                if( str.length() < 1 ) continue;
                fields = str.split( "\\s+" );
if( DEBUG ) {
    for( int i = 0; i < fields.length; i++ ) System.err.print( fields[i] + "\t" );
    System.err.println();
}
                if( fields.length > 1 ) {
                    try {
                        key = new Integer( fields[1] );
                        map.put( key, fields[0] );
                    }
                    catch( NumberFormatException e ) {
                        System.err.print( "Invalid residue sequence number: " );
                        System.err.print( fields[1] );
                        System.err.print( " in line " );
                        System.err.print( lineno );
                        System.err.println( " of residue file" );
                        System.exit( 6 );
                    }
                }
                else {
                    key = new Integer( lineno );
                    map.put( key, fields[0] );
                }
                lineno++;
            } // endwhile labels
            in.close();
            java.io.PrintWriter out;
            if( outfile == null ) out = new java.io.PrintWriter( System.out );
            else out = new java.io.PrintWriter( new java.io.FileWriter( outfile ) );
            lineno = 1;
            String val;
            in = new java.io.BufferedReader( new java.io.FileReader( datafile ) );
            while( (str = in.readLine()) != null ) {
	        str = str.trim();
                if( str.length() < 1 ) continue;
                fields = str.trim().split( "\\s+" );
if( DEBUG ) {
    for( int i = 0; i < fields.length; i++ ) System.err.print( fields[i] + "\t" );
    System.err.println();
}
                if( fields.length < seq_col ) {
                    System.err.print( "Not enough columns in data file: " );
                    System.err.print( fields.length );
                    System.err.print( " in line " );
                    System.err.print( lineno );
                    System.err.print( " (residue sequence column = " );
                    System.err.print( seq_col );
                    System.err.println( ')' );
                    System.exit( 7 );
                }
                if( seq_col == 0 ) seq_col = 1; // just in case
                key = new Integer( fields[seq_col - 1] );
                val = (String) map.get( key );
                if( val == null ) {
                    System.err.print( "No such residue: " );
                    System.err.print( key );
                    System.err.print( " in data file line " );
                    System.err.println( lineno );
                    System.exit( 8 );
                }
                if( target_col == 0 ) target_col = 1;
                if( fields.length <= (target_col - 1) ) target_col = -1;
                for( int i = 0; i < fields.length; i++ ) {
                    if( (target_col > -1) && ((target_col - 1) == i) ) {
                        out.print( val );
                        out.print( "    " );
                    }
                    out.print( fields[i] );
                    out.print( "    " );
                }
                if( target_col < 0 ) out.println( val );
                else out.println();
                lineno++;
            } // endwhile data
            in.close();
            out.flush();
            out.close();
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
            System.exit( 1 );
        }
    } //************************************************************************
}
