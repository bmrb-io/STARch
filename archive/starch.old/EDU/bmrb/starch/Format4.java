/*
 * Format4.java
 *
 * Created on April 15, 2003, 1:55 PM
 *
 * This software is copyright (c) 2002-2003 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Format4.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2003/12/12 23:24:09 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Format4.java,v $
 * Revision 1.3  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.2  2003/04/29 23:00:39  dmaziuk
 * documentation fix
 *
 * Revision 1.1  2003/04/15 21:53:45  dmaziuk
 * added new format
 *
 */

package EDU.bmrb.starch;

/**
 * Converter for "format 4".
 * Format 4 is a whitespace-delimited format where each residue occupies on line.
 * First line contains columin headers, the rest is data. Mandatory headers are
 * SEQ_CODE (residue sequence code) and LABEL (residue label), they must be the
 * first two columns of input file. Other headers are atom names.
 * <P>
 * Unlike format 1 where user can specify atom type, chemical shift value error, 
 * and ambiguity code, this format allows only chemical shift value.
 *
 * @author  dmaziuk
 * @version 1
 */
public class Format4 extends Converter {
    /** field names -- sequence code */
    private static final String SEQCODE = "SEQ_CODE";
    /** field names -- label */
    private static final String LABEL = "LABEL";
    /** indices of corresp. tags */
    private static final int fTags[] = { Data.SEQ_CODE, Data.LABEL}; 
    /** code column index */
    private int fCodeIdx = -1;
    /** label column index */
    private int fLabelIdx = -1;
    /** atom names */
    java.util.ArrayList atoms = null;
//*******************************************************************************
    /** Creates new Format4
     * @param data data store
     * @param errs error list
     * @param restype residue type
     * @param pool string pool
     */
    public Format4( Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        super( Starch.FORMAT4, data, errs, restype, pool );
        atoms = new java.util.ArrayList();
    } //*************************************************************************
    /** Reads data from input stream.
     * @param in BufferedReader input stream
     * @return false on critical error, true otherwise
     */
    public boolean read( java.io.BufferedReader in ) {
        if( in == null ) return false;
        if( ! parseHeader( in ) ) return false;
        if( ! parseData( in ) ) return false;
        return true;
    } //*************************************************************************
    /** Parses file header.
     * @param in BufferedReader input stream
     * @return false on error, true otherwise
     */
    private boolean parseHeader( java.io.BufferedReader in ) {
        String s = Utils.readLine( in, 1, fErrs );
        if( s == null ) {
            fErrs.addError( 1, Messages.ERR_EOF );
            return false;
        }
        java.util.StringTokenizer tok = new java.util.StringTokenizer( s );
// parse field names
        for( int i = 0; tok.hasMoreTokens(); i++ ) {
            s = tok.nextToken();
            if( s.toUpperCase().equals( SEQCODE ) ) fCodeIdx = i;
            else if( s.toUpperCase().equals( LABEL ) ) fLabelIdx = i;
            else atoms.add( s.toUpperCase() );
        }
        return true;
    } //*************************************************************************
    /** Parses input data.
     * @param in BufferedReader input stream
     * @return false on critical error, true otherwise
     */
    private boolean parseData( java.io.BufferedReader in ) {
        java.util.StringTokenizer st = null;
        String tok, label;
        int seqcode, atomindex;
        String s = Utils.readLine( in, 3, fErrs );
        for( int linenum = 2; s != null; linenum++ ) {
            seqcode = -1;
            atomindex = -1;
            label = null;
            st = null;
            st = new java.util.StringTokenizer( s );
            for( int fieldnum = 0; st.hasMoreTokens(); fieldnum++ ) {
                tok = st.nextToken();
                if( fieldnum == fCodeIdx ) {
                    try { seqcode = Integer.parseInt( tok ); }
                    catch( Exception e ) {
                        fErrs.addError( linenum, Messages.ERR_INVCODE + ": " + tok );
                        return false;
                    }
                }
                else if( fieldnum == fLabelIdx ) label = tok;
                else {
                    atomindex++;
//                    if( seqcode < 0 ) {
//                        fErrs.addError( linenum, "", label, Messages.ERR_NOCODE );
//                        return false;
//                    }
                    if( (label == null) || label.equals( "" ) ) {
                        fErrs.addError( linenum, Integer.toString( seqcode ), "", Messages.ERR_NOLABEL );
                        return false;
                    }
                    // this will not add duplicate residues
                    fData.add( new Residue( fRestype, seqcode, label, fPool ) );
                    Atom a = new Atom( (String) atoms.get( atomindex ), fPool );
                    if( Utils.isFloat( tok ) ) a.setShiftValue( tok );
                    else a.addComment( "shift.val = " + tok );
                    fData.addAtom( seqcode, a );
                }
            } // endfor hasMoreTokens()
            s = Utils.readLine( in, linenum, fErrs );
        } // endfor
        try { in.close(); }
        catch( java.io.IOException e ) { /* ignore it */ }
        return true;
    } //*************************************************************************
    /** Main method.
     * Accepts one optional argument: input file name. Without arguments reads
     * from stdin.
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        ErrorList errs = new ErrorList( true );
        Data data = new Data( errs );
        EDU.bmrb.lib.StringPool pool = new EDU.bmrb.lib.StringPool();
        Format4 conv = new Format4( data, errs, -1, pool );
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
