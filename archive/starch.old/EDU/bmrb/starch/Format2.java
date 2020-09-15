/*
 * Format2.java
 *
 * Created on July 18, 2002, 6:35 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Format2.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/01/08 19:38:18 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Format2.java,v $
 * Revision 1.12  2004/01/08 19:38:18  dmaziuk
 * Bugfix
 *
 * Revision 1.11  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.10  2003/04/15 21:26:52  dmaziuk
 * Fixed bugs with comment handling in formats 1 and 2, javadoc warning in
 * Converter. Added another format to Starch.
 *
 * Revision 1.9  2003/01/06 22:45:56  dmaziuk
 * Bugfix release
 *
 * Revision 1.8  2003/01/03 01:52:09  dmaziuk
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
 * Converter for format 2.
 * Format 2: each amino-acid occupies one or two lines. Field names, field offsets
 * (start column counting from 1) and field length are listed in the header. 
 * Field name prefixed with plus (+) means this field is stored on next line.
 * Field names: 
 * <DL>
 * <DT>syn  <DD>the symbol of amino acid
 * <DT>lab  <DD>the three-letter label of amino acid
 * <DT>id   <DD>residue sequence code (note: not shift id, that's added by code)
 * <DT>n    <DD>nitrogen
 * <DT>nh   <DD>the nitrogen proton
 * <DT>co   <DD>the carbon
 * <DT>ca   <DD>ca
 * <DT>cb   <DD>cb
 * <DT>ha1  <DD>ca proton 1 
 * <DT>ha2  <DD>ca proton 2 if it is stored in the same line as ha1.
 * <DT>+ha2 <DD>ca proton 2 if it is stored in a separate line.
 * <DT>hb1  <DD>cb proton 1
 * <DT>hb2  <DD>cb proton 2 if it is stored in the same line as hb1.
 * <DT>+hb2 <DD>cb proton 2 if it is stored in a separate line.
 * <DT>other<DD>other atoms
 * </DL>
 * @author  dmaziuk
 * @version 1
 */
public class Format2 extends Converter {
    /** field names */
    public static final String fNames [] = { "SYN", "LAB", "ID", "N", "NH", "CO", 
               "CA", "CB", "HA1", "HA2", "+HA2", "HB1", "HB2", "+HB2", "OTHER" };
    /** number of fields */
    public static final int NUMFIELDS = 15;
    /** residue symbol */
    public static final int SYMBOL = 0;
    /** residue label */
    public static final int LABEL = 1;
    /** residue sequence code */
    public static final int SEQCODE = 2;
    /** nitrogen */
    public static final int N = 3;
    /** nitrogen proton */
    public static final int NH = 4;
    /** carbon */
    public static final int CO = 5;
    /** ca */
    public static final int CA = 6;
    /** cb */
    public static final int CB = 7;
    /** ca proton 1 */
    public static final int HA1 = 8;
    /** ca proton 2 */
    public static final int HA2 = 9;
    /** ca proton 1 on separate line */
    public static final int PLUSHA2 = 10;
    /** cb proton 1 */
    public static final int HB1 = 11;
    /** cb proton 2 */
    public static final int HB2 = 12;
    /** cb proton 2 on separate line */
    public static final int PLUSHB2 = 13;
    /** other */
    public static final int OTHER = 14;
    /** proper name for carbon */
    public static final String CARBON = "C";
    /** proper name for proton */
    public static final String PROTON = "H";
    /** proper name for A-proton */
    public static final String HA = "HA";
    /** proper name for B-protons */
    public static final String HB = "HB";
//*******************************************************************************
/*
 * File header
 */
    /** Format2 field header
     */
    private class Header {
        /** field name */        
        private String fName = null;
        /** field offset */        
        private int fOffset = -1;
        /** field length */        
        private int fLength = -1;
        /** true if the field is on the next line */
        private boolean fNextLine = false;
        /** Creates new Header
         * @param name field name
         * @param pool string pool
         */
        public Header( String name, EDU.bmrb.lib.StringPool pool ) {
            if( (name == null) || name.trim().equals( "" ) ) 
                throw new NullPointerException( "Missing field name" );
            if( name.equals( fNames[PLUSHA2] ) ) {
                fName = pool.add( fNames[HA2] );
                fNextLine = true;
            }
            else if( name.equals( fNames[PLUSHB2] ) ) {
                fName = pool.add( fNames[HB2] );
                fNextLine = true;
            }
            else fName = pool.add( name.toUpperCase() );
        } //*********************************************************************
        /* traditional Java accessors */
        /** Sets field offset.
         * @param offset int field starting column
         */
        public void setOffset( int offset ) {
            fOffset = offset;
        } //*********************************************************************
        /** Sets field length
         * @param length int field length
         */
        public void setLength( int length ) {
            fLength = length;
        } //*********************************************************************
        /** Returns field name.
         * @return String field name
         */
        public String getName() {
            return fName;
        } //*********************************************************************
        /** Returns field offset.
         * @return int offset
         */
        public int getOffset() {
            return fOffset;
        } //*********************************************************************
        /** Returns field length.
         * @return int length
         */
        public int getLength() {
            return fLength;
        } //*********************************************************************
        /** Returns true if field is on next line.
         * @return true or false
         */
        public boolean isNewLine() {
            return fNextLine;
        } //*********************************************************************
    } // class Header ***********************************************************
    /** Vector of Headers */
    private java.util.List fHeaders = null; // Vector of at most 15 elements; should be
                                    // more efficient than hashmap
//*******************************************************************************
    /** Creates new Format2
     * @param data data store
     * @param errs error list
     * @param restype residue type
     * @param pool string pool
     */
    public Format2( Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        super( Starch.FORMAT2, data, errs, restype, pool );
        fHeaders = new java.util.ArrayList();
    } //*************************************************************************
    /** Reads a field.
     * Length = 0 is a special case, it means "everything to the end of line"
     * @param line String input line
     * @param offset int starting column, counting from 0
     * @param length int field length
     * @return String field or null
     */
    public static String getField( String line, int offset, int length ) {
        if( (line == null) || line.equals( "" ) ) return null;
        if( (offset < 0) || (length < 0) ) return null;
        if( offset > line.length() ) return null;
        String tmps = null;
        if( (length == 0) || ((offset + length) > line.length()) ) 
            tmps = line.substring( offset );
        else tmps = line.substring( offset, offset + length );
        if( tmps.trim().equals( "" ) ) return null;
        return tmps.trim();
    } //*************************************************************************
    /** Reads a field.
     * @param line String input line
     * @param name field name
     * @return String field or null
     */
    public String getField( String line, String name ) {
        Header h = null;
        for( int i = 0; i < fHeaders.size(); i++ ) {
            h = (Header)fHeaders.get( i );
            if( h.getName().equals( name ) )
                return getField( line, h.getOffset(), h.getLength() );
        }
        return null;
    } //*************************************************************************
    /** Reads data from input stream.
     * @param in BufferedReader input stream reader
     * @return false on critical error, true otherwise
     */
    public boolean read( BufferedReader in ) {
        if( ! parseHeader( in ) ) return false;
        if( ! parseData( in ) ) return false;
        return true;
    } //*************************************************************************
    /** Reads file header from input stream.
     * @param in BufferedReader input stream reader
     * @return false on critical error, true otherwise.
     */
    private boolean parseHeader( BufferedReader in ) {
// 1st line may be field count -- it is not required anymore as we can count
// field names, but supported for backwards compatibility
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
// read field names
            s = Utils.readLine( in, 2, fErrs );
            if( s == null ) {
                fErrs.addError( 2, Messages.ERR_EOF );
                return false;
            }
        }
        tok = null;
        tok = new StringTokenizer( s );
        for( int i = 0; tok.hasMoreTokens(); i++ )
            if( ! addField( tok.nextToken() ) ) return false;
// field offsets -- counted from 1 in the file, from 0 in the program
        s = Utils.readLine( in, 3, fErrs );
        if( s == null ) {
            fErrs.addError( 3, Messages.ERR_EOF );
            return false;
        }
        tok = null;
        tok = new StringTokenizer( s );
        for( int i = 0; tok.hasMoreTokens(); i++ ) {
            tmpi = Utils.toInteger( tok.nextToken() );
            if( tmpi == null ) {
                fErrs.addError( 3, Messages.ERR_FLDOFF );
                return false;
            }
            if( ! setFieldOffset( i, tmpi.intValue() - 1 ) ) return false;
        }
// field lengths
        s = Utils.readLine( in, 4, fErrs );
        if( s == null ) {
            fErrs.addError( 4, Messages.ERR_EOF );
            return false;
        }
        tok = null;
        tok = new StringTokenizer( s );
        for( int i = 0; tok.hasMoreTokens(); i++ ) {
            tmpi = Utils.toInteger( tok.nextToken() );
            if( tmpi == null ) {
                fErrs.addError( 4, Messages.ERR_FLDLEN );
                return false;
            }
            if( ! setFieldLength( i, tmpi.intValue() ) ) return false;
        }
// sanity checks
        Header h;
        boolean has_label = false, has_symbol = false, has_code = false;
        for( int i = 0; i < fHeaders.size(); i++ ) {
            h = (Header)fHeaders.get( i );
// check if headers are consistent
            if( h.getName() == null ) { // should never happen 
                fErrs.addError( 4, Messages.ERR_CANTBE );
                return false;
            }
            if( (h.getOffset() < 0) || (h.getLength() < 0) ) {
                fErrs.addError( 4, Messages.ERR_FLDDAT + ": field " + h.getName() );
                return false;
            }
// check if we have symbols, labels, sequence codes
            if( h.getName().equals( fNames[SYMBOL] ) ) has_symbol = true;
            if( h.getName().equals( fNames[LABEL] ) ) has_label = true;
            if( h.getName().equals( fNames[SEQCODE] ) ) has_code = true;
        }
        if( ! has_code ) { // need that
            fErrs.addError( Messages.ERR_NOCODE );
            return false;
        }
        if( ! (has_symbol || has_label) ) { // and one of those
            fErrs.addError( Messages.ERR_NOLABEL );
            return false;
        }
        if( has_symbol && has_label ) { // but not both
            fErrs.addError( Messages.ERR_DUPLABEL );
            return false;
        }
        return true;
    } //*************************************************************************
    /** Adds field name to Vector of Headers.
     * @param name String field name
     * @return true if field was successfully added, false otherwise
     */
    private boolean addField( String name ) {
        for( int i = 0; i < fNames.length; i++ )
            if( name.toUpperCase().equals( fNames[i] ) ) {
                fHeaders.add( new Header( fNames[i], fPool ) );
                return true;
            }
        fErrs.addError( Messages.ERR_FLDNAME + ": " + name );
        return false;
    } //*************************************************************************
    /** Sets field offset in Vector of Headers.
     * @param index int field index
     * @param offset int field offset
     * @return false if there is no such name in the Vector
     */
    private boolean setFieldOffset( int index, int offset ) {
        if( offset < 0 ) {
            fErrs.addError( Messages.ERR_FLDOFF + ": " + offset );
            return false;
        }
        if( index >= fHeaders.size() ) {
            fErrs.addError( Messages.ERR_CANTBE + ": no field at index " + index );
            return false;
        }    
        Header h = (Header)fHeaders.get( index );
        h.setOffset( offset );
        return true;
    } //*************************************************************************
    /** Sets field length in Vector of Headers.
     * @param index int field index
     * @param length int field length
     * @return false if there is no such name in the Vector
     */
    private boolean setFieldLength( int index, int length ) {
        if( length < 0 ) {
            fErrs.addError( Messages.ERR_FLDLEN + ": " + length );
            return false;
        }
        if( index >= fHeaders.size() ) {
            fErrs.addError( Messages.ERR_CANTBE + ": no field at index " + index );
            return false;
        }    
        Header h = (Header)fHeaders.get( index );
        h.setLength( length );
        return true;
    } //*************************************************************************
    /** Parses input data.
     * @param in BufferedReader input stream
     * @return false on critical error, true otherwise
     */
    private boolean parseData( BufferedReader in ) {
        String s = Utils.readLine( in, 5, fErrs );
        if( s == null ) {
            fErrs.addError( 5, Messages.ERR_EOF );
            return false;
        }
        Header h;
        String field;
        String label;
        Residue residue = null;
        Atom atom = null;
        Integer tmpi;
        for( int linenum = 5; s != null; linenum++ ) {
            field = null;
// start of record is when 1st field is not null            
            h = (Header)fHeaders.get( 0 );
            field = getField( s, h.getOffset(), h.getLength() );
            if( field != null ) { // new record
// residue
                label = getField( s, fNames[LABEL] );
                if( label == null ) label = getField( s, fNames[SYMBOL] );
                if( label == null ) {
                    fErrs.addError( linenum, Messages.ERR_NOLABEL );
                    return false;
                }
                field = getField( s, fNames[SEQCODE] );
                if( field == null ) {
                    fErrs.addError( linenum, Messages.ERR_NOCODE );
                    return false;
                }
                tmpi = Utils.toInteger( field );
                if( tmpi == null ) {
                    fErrs.addError( linenum, Messages.ERR_INVCODE + ": " + field );
                    return false;
                }
                residue = new Residue( fRestype, tmpi.intValue(), label, fPool );
                fData.add( residue );
// atoms
                for( int i = 0; i < fHeaders.size(); i++ ) {
                    h = (Header)fHeaders.get( i );
// skip residue label, seq.code, and fields on next line
                    if( h.isNewLine() ) continue;
                    if( h.getName().equals( fNames[LABEL] )
                    || h.getName().equals( fNames[SYMBOL] )
                    || h.getName().equals( fNames[SEQCODE] ) ) continue;
                    field = getField( s, h.getOffset(), h.getLength() );
                    if( field == null ) continue; // no point in adding that
// atom
                    if( h.getName().equals( fNames[OTHER] ) && (atom != null) ) 
                        atom.addComment( field );
                    else {
                        atom = new Atom( h.getName(), fPool );
                        atom.setShiftValue( field );
                    }
                    fData.addAtom( residue.getSequenceCode(), atom );
                }
            }
            else { // continuation line
// look for +ha2 and +hb2
                field = getField( s, fNames[HA2] );
                if( field != null ) {
                    atom = new Atom( fNames[HA2], fPool );
                    atom.setShiftValue( field );
                    fData.addAtom( residue.getSequenceCode(), atom );
                }
                else {
                    field = getField( s, fNames[HB2] );
                    if( field != null ) {
                        atom = new Atom( fNames[HB2], fPool );
                        atom.setShiftValue( field );
                        fData.addAtom( residue.getSequenceCode(), atom );
                    }
                }
            }
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
        Format2 conv = new Format2( data, errs, -1, pool );
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
