/*
 * Xeasy.java
 *
 * Created on August 9, 2002, 6:53 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Xeasy.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2003/12/12 23:24:09 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Xeasy.java,v $
 * Revision 1.10  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.9  2003/12/03 20:55:59  dmaziuk
 * added converter for XEASY variant
 *
 * Revision 1.8  2003/01/06 22:45:57  dmaziuk
 * Bugfix release
 *
 * Revision 1.7  2003/01/03 01:52:11  dmaziuk
 * Major bugfix/added functionality:
 * turned off "bad" nomenclature conversions -- ones that caused loss of data,
 * added a command-line parameter for residue types,
 * atom names are now quoted properly.
 *
 *
 */

package EDU.bmrb.starch;
import java.io.*;
import java.util.*;
/**
 * Converter for XEASY format.
 * XEASY input consists of 2 files: <I>name</I>.seq file listing residue labels
 * and <I>name</I>.xeasy file that contains space-delimited values:<BR>
 * shift id, chemical shift value, value error, atom name, residue sequence code. 
 * Residue sequence code is line number in <I>name</I>.seq file.
 * <P>
 * Note that, unlike the other converters, this one requires two input files.
 * So you cannot just call read(), you need to call readLabels() and then readData().
 *
 * @author  dmaziuk
 * @version 1
 */
public class Xeasy extends Converter {
    /** Creates new Xeasy
     * @param data data store
     * @param errs error list
     * @param restype residue type
     * @param pool string pool
     */
    public Xeasy( Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        super( Starch.XEASY, data, errs, restype, pool );
    } //*************************************************************************
    /** Creates new Xeasy.
     * This constructor is for derived classes.
     * @param format format name
     * @param data data store
     * @param errs error list
     * @param restype residue type
     * @param pool string pool
     */
    protected Xeasy( String format, Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        super( format, data, errs, restype, pool );
    } //*************************************************************************
    /** Reads data from input stream.
     * This method is not to be used: XEASY input consists of two files: residue
     * labels file and data file, that cannot be easily read from the same input
     * stream (BufferedReader). Call readLabels() and readData() instead.
     * @param in BufferedReader input stream reader
     * @return always false
     * @see #readLabels( BufferedReader )
     * @see #readData( BufferedReader )
     */
    public boolean read( BufferedReader in ) {
        fErrs.addError( "Programmer error: cannot use read() on XEASY files" );
        return false;
    } //*************************************************************************
    /** Reads residue labels.
     * Input file contains residue labels, one per line.
     * @param in input stream
     * @return false on error, true otherwise
     */
    public boolean readLabels( BufferedReader in ) {
        String s = null;
        String label = null;
        Integer seqcode = null;
        s = Utils.readLine( in, 1, fErrs );
        if( s == null ) {
            fErrs.addError( 1, Messages.ERR_EOF );
            return false;
        }
        for( int linenum = 1; s != null; linenum++ ) {
            s = s.trim();
            int pos = s.indexOf( ' ' );
            if( pos < 0 ) pos = s.indexOf( '\t' );
            if( pos > 0 ) // whitespace can't be at 0 after trim()
                fData.add( new Residue( fRestype, linenum, 
                s.substring( 0, pos ), fPool ) );
            else fData.add( new Residue( fRestype, linenum, s, fPool ) );
            s = Utils.readLine( in, linenum, fErrs );
        }
        try { in.close(); }
        catch( java.io.IOException e ) { /* ignore it */ }
        return true;
    } //*************************************************************************
    /** Reads data.
     * 
     * @param in input stream
     * @return false on error, true otherwise
     */
    public boolean readData( BufferedReader in ) {
        String atom = null;
        String shift = null;
        String err = null;
        Integer tmpi = null;
        int seqcode = -1;
        java.util.StringTokenizer tok = null;
        String tmp = null;
        StringBuffer comment = new StringBuffer();
        String s = Utils.readLine( in, 1, fErrs );
        for( int linenum = 1; s != null; linenum++ ) {
            atom = null;
            shift = null;
            err = null;
            tmpi = null;
            seqcode = -1;
            tmp = null;
            comment.setLength( 0 );
            tok = null;
            tok = new java.util.StringTokenizer( s );
            if( tok.countTokens() != 5 ) {
                fErrs.addError( linenum, Messages.ERR_NOFLDS + ": " + tok.countTokens() );
                return false;
            }
// shift ID, we ignore it and generate our own
            tmp = tok.nextToken();
// shift value            
            shift = tok.nextToken();
            if( Float.isNaN( Utils.floatValue( shift ) ) ) {
                comment.append( "shift value = " + shift );
                shift = null;
            }
// shift error
            err = tok.nextToken();
            if( Float.isNaN( Utils.floatValue( err ) ) ) {
                comment.append( "shift error = " + err );
                err = null;
            }
// atom name
            atom = tok.nextToken();
// sequence code
            tmp = tok.nextToken();
            tmpi = Utils.toInteger( tmp );
            if( tmpi == null ) {
                fErrs.addError( linenum, Messages.ERR_INVCODE );
                return false;
            }
            seqcode = tmpi.intValue();
// add atom
            Atom atm = new Atom( atom, fPool );
            atm.setShiftValue( shift );
            atm.setShiftError( err );
            if( comment.length() > 0 ) atm.setComment( comment.toString() );
            fData.addAtom( seqcode, atm );
// next line
            s = Utils.readLine( in, linenum, fErrs );
        }
        try { in.close(); }
        catch( java.io.IOException e ) { /* ignore it */ }
        return true;

    } //*************************************************************************
    /** Main method.
     * Main requires one argument: input file name (without extension). Data is
     * read from <CODE>filename.seq</CODE> and <CODE>filename.prot</CODE> (if
     * <CODE>filename.prot</CODE> doesn't exist, tries <CODE>filename.xeasy</CODE>)
     * too.
     * @param args command-line arguments
     */    
    public static void main (String args[]) {
        if( args.length < 1 ) {
            System.err.println( "Usage: java EDU.bmrb.starch.Xeasy filename" );
            System.err.println( "read input data from filename.seq and filename.prot" );
            return;
        }
        ErrorList errs = new ErrorList( true );
        EDU.bmrb.lib.StringPool pool = new EDU.bmrb.lib.StringPool();
        Data data = new Data( errs );
        Xeasy conv = new Xeasy( data, errs, -1, pool );
// read data
        String fname = args[0] + ".seq";
        java.io.BufferedReader seq, prot;
        try {
             seq = new java.io.BufferedReader( new java.io.InputStreamReader( 
                      new java.io.FileInputStream( fname ) ) );
        }
        catch( Exception e ) {
            errs.addError( Messages.ERR_FOPEN + " " + fname );
            return;
        }
// protons
        fname = args[0] + ".prot";
        try {
            prot = new java.io.BufferedReader( new java.io.InputStreamReader( 
                      new java.io.FileInputStream( fname ) ) );
        }
        catch( Exception e ) {
            try { 
                fname = args[0] + ".xeasy";
                prot = new java.io.BufferedReader( new java.io.InputStreamReader( 
                      new java.io.FileInputStream( fname ) ) );
            }
            catch( Exception ex ) {
                errs.addError( Messages.ERR_FOPEN + " " + fname );
                return;
            }
        }
        conv.readLabels( seq );
        conv.readData( prot );
        data.setOutputFormat( Data.STAR2 );
        Residue r;
        int row = 1;
        data.printHeader( System.out );
        while( data.size() > 0 ) {
            r = data.get( 0 );
            row = data.printResidue( System.out, r, row );
            data.removeElementAt( 0 );
        }
        data.printFooter( System.out );
// print out errors
        if( errs.size() < 1 ) return;
        errs.printErrors( System.err );
    } //*************************************************************************
} //*****************************************************************************
