/*
 * Pipp.java
 *
 * Created on August 7, 2002, 6:26 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Pipp.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/07/02 19:37:33 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Pipp.java,v $
 * Revision 1.11  2004/07/02 19:37:33  dmaziuk
 * changed handling of ambiguity codes
 *
 * Revision 1.10  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.9  2003/01/06 22:45:56  dmaziuk
 * Bugfix release
 *
 * Revision 1.8  2003/01/03 01:52:10  dmaziuk
 * Major bugfix/added functionality:
 * turned off "bad" nomenclature conversions -- ones that caused loss of data,
 * added a command-line parameter for residue types,
 * atom names are now quoted properly.
 *
 *
 */

package EDU.bmrb.starch;
import java.util.*;
import java.io.*;
/**
 * Converter for PIPP format.
 * PIPP has no header that we're interested in: residue blocks are of the form
 * <PRE>
   RES_ID           residue sequence code
   RES_TYPE         residue label
   SPIN_SYSTEM_ID   usually same as RES_ID
   HETEROGENEITY    number
       atom name    shift value
       atom name    shift value
       ...
   END_RES_DEF
 * </PRE>
 * Lines containing atom names start with whitespace.
 *
 * @author  dmaziuk
 * @version 1
 */
public class Pipp extends Converter {
    /** start of record */
    public static final String REC_START = "RES_ID";
    /** residue label */
    public static final String LABEL = "RES_TYPE";
    /** end of record */
    public static final String REC_END = "END_RES_DEF";
    /** HG21|HG11 */
    private static final String HG21HG11 = "HG21|HG11";
//    /** HG11 */
//    private static final String HG11 = "HG11";
    /** HG12 */
    private static final String HG12 = "HG12";
//*******************************************************************************
    /** Creates new Pipp
     * @param data data store
     * @param errs error list
     * @param restype residue type
     * @param pool string pool
     */
    public Pipp( Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        super( Starch.PIPP, data, errs, restype, pool );
    } //*************************************************************************
    /** Reads data from input stream.
     * Note that this method returns false on "critical" errors, e.g. read
     * errors. These should be logged with Starch.addError(). Non-critical
     * errors, e.g. missing values do not result in "false" return value; they
     * should be logged with Starch.addWarning(). Calling code should check
     * Starch.numErrors() after read()
     * @param in BufferedReader input stream reader
     * @return false on critical error, true otherwise
     */
    public boolean read( BufferedReader in ) {
        if( ! parseData( in ) ) return false;
        return true;
    } //*************************************************************************
    /** Does atom name conversions specific to PIPP format.
     * Ambiguous shifts are often written as X|Y. We change that to X with
     * ambiguity code of 2. PIPP also uses # at the end of atom name to denote
     * methyl groups. We drop the # and generate a warning.
     *
     * @param seqcode residue sequence code
     * @param label residue label
     * @param atom atom name
     * @return array of atom names or null
     */
    private String [] getAtomNames( Integer seqcode, String label, String atom ) 
    {
        String name;
// corner case: HG21|HG11 is converted to HG12
        if( atom.toUpperCase().trim().equals( HG21HG11 ) ) {
            String [] rc = new String[2];
            rc[0] = HG12;
            fErrs.addWarning( seqcode.toString(), label, "Replacing " 
            + atom + " with " + rc[0] + ", ambiguity code ?" );
//            + atom + " with " + rc[0] + ", ambiguity code " + rc.length );
            return rc;
        }   
// see if this is ambiguous shift (e.g. "HB1|HB2")
        int count = 1;
        int index = atom.indexOf( '|' );
        if( index == 0 ) { // hopefully, this will never happen
            fErrs.addError( seqcode.toString(), label, Messages.ERR_INVALIDATOM );
            return null;
        }
// we're only interested in the first substring
        if( index > 0 ) name = atom.substring( 0, index );
        else name = atom;
// but we want to know how many there are
        while( index >= 0 ) {
            count++;
            index = atom.indexOf( '|', index + 1 );
        }
        String [] rc = new String[count];
// ok, now see if it has a #
        index = name.indexOf( '#' );
        if( index == 0 ) { // hopefully, this will never happen
            fErrs.addError( seqcode.toString(), label, Messages.ERR_INVALIDATOM );
            return null;
        }
        if( index > 0 ) name = name.substring( 0, index );
        rc[0] = name.toUpperCase();
        if( ! atom.toUpperCase().equals( rc[0] ) )
            fErrs.addWarning( seqcode.toString(), label, "Replacing " 
            + atom + " with " + rc[0] + ", ambiguity code ?" ) ;
//            + atom + " with " + rc[0] + ", ambiguity code " + rc.length ) ;
        return rc;
    } //*************************************************************************
    /** Parses input data.
     * @param in input stream
     * @return false on error or EOF, true otherwise
     */
    private boolean parseData( BufferedReader in ) {
        StringTokenizer tok = null;
        Integer seqcode = null;
        String label = null;
        Residue residue = null;
        Atom atom;
        Integer ambiguity = null;
        String tmp = null;
        String [] names = null;
        boolean in_rec = false; 
        String s = Utils.readLine( in, 1, fErrs );
        for( int linenum = 1; s != null; linenum++ ) {
            tok = null;
            tok = new StringTokenizer( s );
            tmp = tok.nextToken().toUpperCase();
            if( tmp.equals( REC_START ) ) {
                in_rec = true;
                tmp = tok.nextToken();
                seqcode = Utils.toInteger( tmp );
// actually, if there are no atoms in this record we could just ignore it and continue
                if( seqcode == null ) {
                    fErrs.addError( linenum, Messages.ERR_INVCODE + ": " + tmp );
                    return false;
                }
            }
            else if( tmp.equals( LABEL ) ) {
                label = tok.nextToken();
//FIXME: this ass-u-me-s that label is always after RES_ID
                residue = new Residue( fRestype, seqcode.intValue(), label, fPool );
                fData.add( residue );
            }
            else if( tmp.equals( REC_END ) ) {
                label = null;
                seqcode = null;
                ambiguity = null;
                in_rec = false;
                names = null;
            }
            else if( in_rec && ((s.charAt( 0 ) == ' ') || (s.charAt( 0 ) == '\t')) ) {
// check atom name
// PIPP uses # to denote methyl groups. It also uses X|Y notation for
// ambiguous shifts. Check for that first.
                if( fRestype == Nomenmap.AA_NUMBER ) {
                    names = getAtomNames( seqcode, label, tmp );
//                    if( names != null ) ambiguity = new Integer( names.length ); 
                }
                else {
                    names = new String[1];
                    names[0] = tmp;
                }
                if( tok.hasMoreTokens() ) tmp = tok.nextToken();
                else tmp = null;
                if( (tmp != null) && tmp.equals( "\\" ) ) {
                    s = Utils.readLine( in, ++linenum, fErrs );
                    if( s == null ) {
                        fErrs.addError( linenum, Messages.ERR_EOF );
                        return false;
                    }
                    tok = null;
                    tok = new StringTokenizer( s );
                    tmp = tok.nextToken();
                }
                atom = new Atom( names[0], fPool );
                if( Float.isNaN( Utils.floatValue( tmp ) ) ) 
                    atom.addComment( "shift value = " + tmp );
                else atom.setShiftValue( tmp );
//                if( (ambiguity != null ) && (ambiguity.intValue() > 0) ) 
//                    atom.setShiftAmbiguityCode( ambiguity.intValue() );
                atom.setShiftAmbiguityCode( -1 );
                fData.addAtom( seqcode.intValue(), atom );
            } // endif s[0] == ' '
            s = Utils.readLine( in, linenum, fErrs );
        } // endfor
        try { in.close(); }
        catch( IOException e ) { /* ignore it */ }
        return true;
    } //*************************************************************************
    /** Main method.
     * Accepts an optional parameter: input filename. Without that, reads from
     * stdin.
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        ErrorList errs = new ErrorList( true );
        Data data = new Data( errs );
        EDU.bmrb.lib.StringPool pool = new EDU.bmrb.lib.StringPool();
        Pipp conv = new Pipp( data, errs, -1, pool );
        java.io.BufferedReader in;
        try {
            if( args.length > 0 ) {
                in = new java.io.BufferedReader( new java.io.InputStreamReader( 
                      new java.io.FileInputStream( args[0] ) ) );
            }
            else in = new java.io.BufferedReader( new java.io.InputStreamReader( System.in ) );
        }
        catch( Exception e ) {
            errs.addError( Messages.ERR_FOPEN + " " + args[0] );
            return;
        }
        conv.read( in );
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
}
