/*
 * Format1.java
 *
 * Created on May 24, 2002, 3:57 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Format1.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2003/12/12 23:24:09 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Format1.java,v $
 * Revision 1.15  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.14  2003/06/16 19:54:44  dmaziuk
 * fixed a bug in format1
 *
 * Revision 1.13  2003/05/01 01:28:10  dmaziuk
 * added float validation method
 *
 * Revision 1.12  2003/04/29 23:00:39  dmaziuk
 * documentation fix
 *
 * Revision 1.11  2003/04/15 21:26:52  dmaziuk
 * Fixed bugs with comment handling in formats 1 and 2, javadoc warning in
 * Converter. Added another format to Starch.
 *
 * Revision 1.10  2003/01/06 22:45:55  dmaziuk
 * Bugfix release
 *
 * Revision 1.9  2003/01/03 01:52:09  dmaziuk
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
 * Converter for Format 1.
 * Format 1 is space-separated values, one record per line. First line contains
 * optional column count, second (or first, if column count is omitted) line: 
 * space-separated list of column (field) names. Valid field names are listed
 * in fNames array.
 * @author  dmaziuk
 * @version 1
 */
public class Format1 extends Converter {
    /** field names */
    private static final String fNames[] = { "SHIFT_ID", "SEQ_CODE", 
        "LABEL", "NAME", "TYPE", "VALUE", 
        "VALUE_ERROR", "AMBIGUITY_CODE", "SEQCODE_LABEL", "NOTHING" };
    /** indices of corresp. tags */
    private static final int fTags[] = { Data.SHIFT_ID, Data.SEQ_CODE, 
        Data.LABEL, Data.ATOM_NAME, Data.ATOM_TYPE, Data.SHIFT_VAL, 
        Data.SHIFT_ERR, Data.SHIFT_AMB, Data.NOSUCHTAG, Data.COMMENTS };
    /** field indices lookup table 
     * @see #addField( String, int )
     */
    private int fPresent[] = { -1, -1, -1, -1, -1, -1, -1, -1, -1 };
//*******************************************************************************
    /** Creates new Format1
     * @param data data store
     * @param errs error list
     * @param restype residue type
     * @param pool string pool
     */
    public Format1( Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        super( Starch.FORMAT1, data, errs, restype, pool );
    } //*************************************************************************
    /** Reads data from input stream.
     * @param in BufferedReader input stream
     * @return false on critical error, true otherwise
     */
    public boolean read( BufferedReader in ) {
        if( in == null ) return false;
        if( ! parseHeader( in ) ) return false;
        if( ! parseData( in ) ) return false;
        return true;
    } //*************************************************************************
    /** Parses file header.
     * @param in BufferedReader input stream
     * @return false on error, true otherwise
     */
    private boolean parseHeader( BufferedReader in ) {
        String s = Utils.readLine( in, 1, fErrs );
        if( s == null ) {
            fErrs.addError( 1, Messages.ERR_EOF );
            return false;
        }
        StringTokenizer tok = new StringTokenizer( s );
        Integer tmpi = null;
// field count is optional, it's here for backwards compatibility only
        if( tok.countTokens() == 1 ) { // make sure it's not a field name
            s = tok.nextToken(); // strip blanks
            tmpi = Utils.toInteger( s );
            if( tmpi == null ) {
// assume there cannot be just one field
                fErrs.addError( 1, s + Messages.ERR_FCNT );
                return false;
            }
// now read field names
            s = Utils.readLine( in, 2, fErrs );
            if( s == null ) {
                fErrs.addError( 2, Messages.ERR_EOF );
                return false;
            }
            tok = null; // Grrr, stupid StringTokenizer.
            tok = new StringTokenizer( s );
        }
// parse field names
        for( int i = 0; tok.hasMoreTokens(); i++ ) {
            s = tok.nextToken();
            if( ! addField( s, i ) ) return false;
        }
        return true;
    } //*************************************************************************
    /** Adds field name to lookup table.
     * Table is an array of integers. Array index is the field (column) number.
     * Value is the index of corresponding NMR-STAR tag. NOSUCHTAG is a special
     * case: SEQCODE_LABEL field that has residue label and sequence code 
     * concatenated together.
     * @param name String field name
     * @param index int field index
     * @return true if field was successfully added, false otherwise
     */
    private boolean addField( String name, int index ) {
        int pos = -1;
        for( int i = 0; i < fNames.length; i++ )
            if( name.toUpperCase().equals( fNames[i] ) ) {
                pos = i;
                break;
            }
        if( pos == -1 ) {
            fErrs.addError( 2, Messages.ERR_FLDNAME + ": " + name );
            return false;
        }
        fPresent[index] = fTags[pos];
        return true;
    } //*************************************************************************
    /** Parses input data.
     * @param in BufferedReader input stream
     * @return false on critical error, true otherwise
     */
    private boolean parseData( BufferedReader in ) {
        StringTokenizer st = null;
        String tok;
        StringBuffer tmpbuf = new StringBuffer();
        String label, atom_name, atom_type, shift_value, shift_error;
        Integer shift_id, seq_code, shift_amb;
        String [] atom_names = null, atom_types = null;
        String s = Utils.readLine( in, 3, fErrs );
        for( int linenum = 3; s != null; linenum++ ) {
          
            label = null; 
            atom_name = null; 
            atom_type = null; 
            shift_id = null; 
            seq_code = null; 
            shift_amb = null;
            shift_value = null;
            shift_error = null;
            tmpbuf.setLength( 0 );
            st = null;
            st = new StringTokenizer( s );
            for( int fieldnum = 0; st.hasMoreTokens(); fieldnum++ ) {
                tok = st.nextToken();
                switch( fPresent[fieldnum] ) {
                    case Data.SHIFT_ID : // shift ID -- ignore it
                        break;
// residue code and sequence code may be encoded in one field (SEQCODE_LABEL), 
// or they may be in separate fields. It is an error to have both.
                    case Data.SEQ_CODE : // residue sequence code
                        if( seq_code != null ) { // error: we already have code
                            fErrs.addError( linenum, seq_code.toString(), label, Messages.ERR_DUPCODE );
                            return false;
                        }                            
                        seq_code = Utils.toInteger( tok );
// sequence code is part of primary key. Can't continue without it
                        if( seq_code == null ) {
                            fErrs.addError( linenum, "?", label, Messages.ERR_NOCODE + " " + tok );
                            return false;
                        }
                        break;
                    case Data.LABEL : // residue label
                        if( label != null ) { // error: we already have a label
                            fErrs.addError( linenum, seq_code.toString(), label, Messages.ERR_DUPLABEL );
                            return false;
                        }
                        label = tok;
                        break;
                    case Data.ATOM_NAME : // atom name
                        atom_name = tok; // since we may not have residue label 
                        break;   // yet, we cannot do nomenclature mapping here
                    case Data.ATOM_TYPE : // atom type
                        atom_type = tok; // ditto
                        break;
                    case Data.SHIFT_VAL : // chemical shift value
                        shift_value = tok;
                        break;
                    case Data.SHIFT_ERR : // chemical shift value error
                        shift_error = tok;
                        break;
                    case Data.SHIFT_AMB : // shift ambiguity code
                        shift_amb = Utils.toInteger( tok );
                        if( shift_amb == null ) tmpbuf.append( " shift amb. code=" + tok );
                        break;
                    case Data.NOSUCHTAG : // sequence code and residue code in one
                        if( (label != null) || (seq_code != null) ) { // error
                            fErrs.addError( linenum, seq_code.toString(), label, Messages.ERR_DUPCODE );
                            fErrs.addError( linenum, seq_code.toString(), label, Messages.ERR_DUPLABEL );
                            return false;
                        }
//                        seq_code = Utils.toInteger( tok.substring( 0, 1 ) );
                        seq_code = Utils.toInteger( tok.substring( 1 ) );
                        if( seq_code == null ) {
                            fErrs.addError( linenum, "?", label, Messages.ERR_NOCODE + " " + tok );
                            return false;
                        }
//                        label = tok.substring( 1 );
                        label = tok.substring( 0, 1 );
                        break;
                    case Data.COMMENTS : // extras
                        tmpbuf.append( " " + tok );
                        break;
                } // endswitch
            } // endfor fieldnum
// Finished with record. 
// check that we have label and sequence code, once again
            if( seq_code == null ) {
                fErrs.addError( linenum, "?", label, Messages.ERR_NOCODE );
                return false;
            }
            if( label == null ) {
                fErrs.addError( linenum, seq_code.toString(), "?", Messages.ERR_NOLABEL );
                return false;
            }
// add data                
            fData.add( new Residue( fRestype, seq_code.intValue(), label, fPool ) );
            Atom atom = new Atom( atom_name, fPool );
// it's common to put '-' or "n.a." for missing values. Don't generate warning
// message if cast fails, but put original string in comment.
            if( Utils.isFloat( shift_value ) ) atom.setShiftValue( shift_value );
            else atom.addComment( "shift val. = " + shift_value );
            if( Utils.isFloat( shift_error ) ) atom.setShiftError( shift_error );
            else atom.addComment( "shift err. = " + shift_error );
            if( shift_amb != null ) atom.setShiftAmbiguityCode( shift_amb.intValue() );
            atom.setType( atom_type );
            if( tmpbuf.length() > 0 ) atom.addComment( tmpbuf.toString() );
            fData.addAtom( seq_code.intValue(), atom );
            s = Utils.readLine( in, linenum, fErrs );
        } //endfor linenum
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
        Format1 conv = new Format1( data, errs, -1, pool );
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
