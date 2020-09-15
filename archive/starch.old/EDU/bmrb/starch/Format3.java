/*
 * Format3.java
 *
 * Created on August 5, 2002, 2:30 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Format3.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2003/12/12 23:24:09 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Format3.java,v $
 * Revision 1.9  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.8  2003/01/06 22:45:56  dmaziuk
 * Bugfix release
 *
 * Revision 1.7  2003/01/03 01:52:09  dmaziuk
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
 * Converter for Format 3.
 * Format 3: each residue occupies one line. Field names, field offsets (start
 * column, counted from 1) and field length are listed in the header.
 * Field names: "label" and "seq_code" are mandatory (residue label and sequence
 * code, resp.). The rest are atom names. This format was designed for nucleic 
 * acids, so we don't attempt any nomenclature conversions.<P>
 * TODO: add nomenclature conversions when Nomenmap package is finished.
 *
 * @author  dmaziuk 
 * @version 1
 */
public class Format3 extends Converter {
    /** field names */
    public static final String [] fNames = { "LABEL", "SEQ_CODE" };
    /* indices */
    /** residue label */
    public static final int LABEL = 0;
    /** residue sequence code */
    public static final int SEQCODE = 1;
    /** Vector of field headers */
    private java.util.List fHeaders = null;
//*******************************************************************************
/*
 * File header
 */
    /** Format3 field header
     */
    private class Header {
        /** field name */        
        private String fName = null;
        /** field offset */        
        private int fOffset = -1;
        /** field length */        
        private int fLength = -1;
        /** Creates new Header.
         * @param name field name
         * @param pool string pool
         */
        public Header( String name, EDU.bmrb.lib.StringPool pool ) {
            fName = pool.add( name.toUpperCase() );
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
    } // class Header ***********************************************************
    /** Creates new Format3
     * @param data data store
     * @param errs error list
     * @param restype residue type
     * @param pool string pool
     */
    public Format3( Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        super( Starch.FORMAT3, data, errs, restype, pool );
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
// field names
            s = Utils.readLine( in, 2, fErrs );
            if( s == null ) {
                fErrs.addError( 2, Messages.ERR_EOF );
                return false;
            }
        }
        tok = null;
        tok = new StringTokenizer( s );
        for( int i = 0; tok.hasMoreTokens(); i++ ) 
            fHeaders.add( new Header( tok.nextToken(), fPool ) );
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
        boolean has_label = false, has_code = false;
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
// check if we have labels and sequence codes
            if( h.getName().equals( fNames[LABEL] ) ) has_label = true;
            if( h.getName().equals( fNames[SEQCODE] ) ) has_code = true;
        }
        if( ! has_code ) { // need that
            fErrs.addError( Messages.ERR_NOCODE );
            return false;
        }
        if( ! has_label ) { // and this
            fErrs.addError( Messages.ERR_NOLABEL );
            return false;
        }
        return true;
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
        Atom atom;
        Integer tmpi;
        for( int linenum = 5; s != null; linenum++ ) {
            field = null;
            label = getField( s, fNames[LABEL] );
            if( label == null ) {
                fErrs.addError( linenum, Messages.ERR_NOLABEL );
                return false;
            }
            field = getField( s, fNames[SEQCODE] );
            if( field == null ) {
                fErrs.addError( linenum, "?", label, Messages.ERR_NOCODE );
                return false;
            }
            tmpi = Utils.toInteger( field );
            if( tmpi == null ) {
                fErrs.addError( linenum, Messages.ERR_INVCODE );
                return false;
            }
            residue = new Residue( fRestype, tmpi.intValue(), label, fPool );
            fData.add( residue );
// atoms
            for( int i = 0; i < fHeaders.size(); i++ ) {
                h = (Header)fHeaders.get( i );
                if( h.getName().equals( fNames[LABEL] ) 
                || h.getName().equals( fNames[SEQCODE] ) ) continue; // skip it
                field = getField( s, h.getOffset(), h.getLength() );
//                if( field == null ) continue; // no point in adding that
// atom
                atom = new Atom( h.getName(), fPool );
                if( Float.isNaN( Utils.floatValue( field ) ) ) 
                    atom.addComment( " shift value=" + field );
                else atom.setShiftValue( field );
                fData.addAtom( residue.getSequenceCode(), atom );
            } // endfor Headers.size()
	    s = Utils.readLine( in, linenum, fErrs );
        } // endfor linenum
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
        Format3 conv = new Format3( data, errs, -1, pool );
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
