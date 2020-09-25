/*
 * Main.java
 *
 * Created on May 22, 2002, 6:47 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Main.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/09/01 22:15:17 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Main.java,v $
 * Revision 1.16  2004/09/01 22:15:17  dmaziuk
 * makefile fix
 *
 * Revision 1.15  2004/07/02 19:37:33  dmaziuk
 * changed handling of ambiguity codes
 *
 * Revision 1.14  2004/01/08 19:38:18  dmaziuk
 * Bugfix
 *
 * Revision 1.13  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.12  2003/01/06 23:54:05  dmaziuk
 * removed obsolete 'import'
 *
 * Revision 1.11  2003/01/06 22:45:56  dmaziuk
 * Bugfix release
 *
 *
 */

package EDU.bmrb.starch;
import java.io.*;
import gnu.getopt.*;
/**
 * Command-line interface to STARch
 * @author  dmaziuk
 * @version 1 
 */
public class Main {
    /** STARch object */
    public Starch fStarch = null;
//*******************************************************************************
    /** Creates new Main */
    public Main () {
    } //*************************************************************************
    /** Prints out usage sumary */
    private static void usage() {
        System.err.print( "Usage: java EDU.bmrb.starch.Main -f <input format>" );
        System.err.print( " [-r <residue type>] [-i <input filename>]" );
        System.err.print( " [-o <output filename>] [-e <error filename>] " );
        System.err.print( " [-c] [-3] [-v] [-h ] [-hformats]" );
        System.err.println( " [-hresidues]" );
        System.err.println( " -h: print usage summary" );
        System.err.println( " -hformats: list known input formats" );
        System.err.println( " input format: one of formats from the above list" );
        System.err.println( " -hresidues: list known residue types" );
        System.err.println( " residue type: one of the types from the above list" );
        System.err.println( "   (if omitted, use built-in nomenclature conversions)" );
        System.err.println( " input filename: input file in specified format" );
        System.err.println( "   (if omitted, read from standard input)" );
        System.err.println( " output filename: if missing, write to standard out " );
        System.err.println( " -c: CSV (comma-separated values) output" );
        System.err.println( " -x: XEASY output" );
        System.err.println( " -3: NMR-STAR 3.0 output" );
        System.err.println( "   (default is NMR-STAR 2.1)" );
        System.err.println( " -v: verbose output: print conversion warnings" );
        System.err.println( " error filename: file for error/warning messages " );
        System.err.println( "   (if omitted, write to standard error, if same as" );
        System.err.println( "   output filename, print messages before data loop" );
        System.err.println( "   in output file)" );
    } //*************************************************************************
    /** Main method.
     * @param args the command line arguments
     */
    public static void main (String args[]) {
        if( args.length < 1 ) {
            usage();
            return;
        }
        gnu.getopt.Getopt g = new gnu.getopt.Getopt( "STARch", args, "h::f:i:o:e:r:c3xv" );
        int i;
        String arg = null;
        boolean help_formats = false, help_residues = false;
        Integer tmpi;
        int restype = -1;
        String informat = null;
        String infile = null;
        String outfile = null;
        String errfile = null;
        int outformat = Data.STAR2;
        boolean verbose = false;
        while( (i = g.getopt()) != -1 ) {
            if( help_formats || help_residues ) break;
            switch( i ) {
                case 'h' : // help
                    arg = g.getOptarg();
                    if( arg == null ) {
                        usage();
                        return;
                    }
                    else if( arg.toLowerCase().equals( "formats" ) ) {
                        help_formats = true;
                        break;
                    }
                    else if( arg.toLowerCase().equals( "residues" ) ) {
                        help_residues = true;
                        break;
                    }
                case 'r' : // residue type
                    arg = g.getOptarg();
                    try { 
                        tmpi = new Integer( arg );
                        restype = tmpi.intValue();
                        tmpi = null;
                    }
                    catch( Exception e ) { /* ignore it */ }
                    break;
                case 'f' : // input format
                    informat = g.getOptarg();
                    break;
                case 'i' : // input file
                    infile = g.getOptarg();
                    break;
                case 'o' : // output file
                    outfile = g.getOptarg();
                    break;
                case 'e' : // error file
                    errfile = g.getOptarg();
                    break;
                case 'c' : // CSV output
                    outformat = Data.CSV;
                    break;
                case '3' : // NMR-STAR 3.0 output
                    outformat = Data.STAR3;
                    break;
                case 'x' : // Xeasy output
                    outformat = Data.XEASY;
                    break;
                case 'v' : // verbose
                    verbose = true;
                    break;
                default :
                    usage();
                    return;
            } // endswitch
        } // endwhile
// check arguments
        EDU.bmrb.lib.StringPool pool = new EDU.bmrb.lib.StringPool();
        Nomenmap nomenmap = new Nomenmap( pool );
        String [] res = nomenmap.listResidueTypes();
        if( help_residues ) {
            System.out.println( "Known residue types:" );
            for( i = 0; i < res.length; i++ ) 
                System.out.println( i + ": " + res[i] );
            return;
        }
        ErrorList errs = new ErrorList( verbose );
        Data data = new Data( errs );
        Starch starch = new Starch( data, errs, restype, pool );
        res = starch.listInputFormats();
        if( ! help_formats ) {
            boolean found = false;
            for( i = 0; i < res.length; i++ )
                if( res[i].equals( informat.toUpperCase() ) ) {
                    found = true;
                    break;
                }
            if( ! found ) help_formats = true;
        }
        if( help_formats ) {
            System.out.println( "Input formats:" );
            for( i = 0; i < res.length; i++ ) System.out.println( res[i] );
            return;
        }
        if( infile == null ) {
            usage();
            return;
        }
// set up output
        java.io.PrintStream out;
        if( outfile != null )
            try {
                out = new java.io.PrintStream( new java.io.FileOutputStream( outfile ) );
            }
            catch( Exception e ) {
                errs.addError( Messages.ERR_FOPEN + outfile );
                return;
            }
        else out = System.out;
        data.setOutputFormat( outformat );
// read data
        starch.setInputFormat( informat.toUpperCase() );
        try {
            starch.convert( infile );
            Residue r;
            int row = 1;
            if( outformat != Data.XEASY ) {
                data.printHeader( out );
                while( data.size() > 0 ) {
                    r = data.get( 0 );
// fix residue labels
                    nomenmap.convertLabel( errs, r );
// fix atom names
                    nomenmap.convertAtomNames( errs, r );
// expand pseudo atoms
                    nomenmap.expandPseudoAtoms( errs, r );
// add missing atoms
                    nomenmap.addAtoms( errs, r );
// add ambiguity codes
//                    nomenmap.addAmbiguityCodes( errs, r );
// print residue out
                    row = data.printResidue( out, r, row );
                    data.removeElementAt( 0 );
                }
                data.printFooter( out );
            }
            else {
                for( i = 0; i < data.size(); i++ ) {
                    r = data.get( i );
                    nomenmap.convertLabel( errs, r );
                    nomenmap.convertAtomNames( errs, r );
                    nomenmap.expandPseudoAtoms( errs, r );
                    nomenmap.addAtoms( errs, r );
                }
                data.printXeasy( out );
                data.clear();
            }
        }
        catch( Exception e ) {
            System.err.println();
            System.err.println( e.getMessage() );
            e.printStackTrace();
            System.err.println();
        }
// print out errors
        if( errs.size() < 1 ) return;
        java.io.PrintStream err;
        if( errfile == null ) err = System.err;
        else if( ! errfile.equals( outfile ) )
            try {
                err = new java.io.PrintStream( new java.io.FileOutputStream( errfile ) );
            }
            catch( Exception e ) {
                errs.addError( Messages.ERR_FOPEN + errfile );
                return;
            }
        else err = out;
        errs.printErrors( err );
        return;
    } //*************************************************************************
}
