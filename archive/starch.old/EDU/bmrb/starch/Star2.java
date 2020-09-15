/*
 * Star2.java
 *
 * Created on August 27, 2002, 3:40 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Star2.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2003/12/12 23:24:09 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Star2.java,v $
 * Revision 1.6  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.5  2003/01/06 22:45:57  dmaziuk
 * Bugfix release
 *
 * Revision 1.4  2003/01/03 01:52:10  dmaziuk
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
 * Converter for NMR-STAR 2.1 chemical shift loops.
 * NMR-STAR 2.1 to NMR-STAR 2.1 conversion can be used for re-numbering 
 * _Atom_shift_assigned_ID's, validating/nomenclature conversions etc.
 * 
 * @author  dmaziuk
 * @version 1
 */
public class Star2 extends Converter {
    /** indices of NMR-STAR 2.1 tags */
    private static final int fTags[] = { Data.SHIFT_ID, Data.SEQ_CODE, 
        Data.LABEL, Data.ATOM_NAME, Data.ATOM_TYPE, Data.SHIFT_VAL, 
        Data.SHIFT_ERR, Data.SHIFT_AMB };
    /** field indices lookup table 
     * @see #addField( String, int )
     */
    private int fPresent[] = { -1, -1, -1, -1, -1, -1, -1, -1 };
//*******************************************************************************
    /** Creates new Star2
     * @param data data store
     * @param errs error list
     * @param restype residue type
     * @param pool string pool
     */
    public Star2( Data data, ErrorList errs, int restype, EDU.bmrb.lib.StringPool pool ) {
        super( Starch.STAR2, data, errs, restype, pool );
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
        int i = parseHeader( in );
        if( i < 0 ) return false;
        if( ! parseData( in, i ) ) return false;
        return true;
    } //*************************************************************************
    /** Adds field name to lookup table.
     * Table is an array of integers. Array index is the field (column) number.
     * Value is the index of corresponding NMR-STAR tag. 
     *
     * @param name String field name
     * @param index int field index
     * @return true if field was successfully added, false otherwise
     */
    private boolean addField( String name, int index ) {
        int pos = -1;
        for( int i = 0; i < Data.fTags.length; i++ )
            if( name.equals( Data.fTags[i] ) ) {
                pos = i;
                break;
            }
        if( pos == -1 ) {
            fErrs.addError( Messages.ERR_FLDNAME + ": " + name );
            return false;
        }
        fPresent[index] = fTags[pos];
        return true;
    } //*************************************************************************
    /** Reads file header.
     * This is a little ugly since NMR-STAR syntax doesn't care about whitespace:
     * you could have all loop tags in one line, followed by first row of data
     * values in the same line. This code won't handle this case; it assumes
     * that data rows start at new line. It also assumes that a non-tag is
     * the start of data row -- hopefully, we'll run into errors later on
     * if it isn't.
     *
     * @param in input stream
     * @return number of lines read, negative value on error
     */
    private int parseHeader( BufferedReader in ) {
        int line = 1;
        String s = Utils.readLine( in, line, fErrs );
        if( s == null ) {
            fErrs.addError( line, Messages.ERR_EOF );
            return -1;
        }
        while( (s != null) && (s.trim().charAt( 0 ) == '#') ) // skip comments
            s = Utils.readLine( in, line, fErrs );
        if( ! s.trim().equals( Data.LOOP_START ) ) {
            fErrs.addError( line, Messages.ERR_NOLOOP );
            return -1;
        }
        StringTokenizer st;
        String tok;
        int fieldnum = -1;
        lines: {
            for( line = 2; (s != null) && (line < 10); line++ ) {
                st = null;
                tok = null;
                try { in.mark( 1024 ); }
                catch( Exception e ) {
                    System.err.println( e.getMessage() );
                    e.printStackTrace();
                }
                s = Utils.readLine( in, line, fErrs );
                st = new StringTokenizer( s );
                while( st.hasMoreTokens() ) {
                    tok = st.nextToken();
                    if( tok.charAt( 0 ) == '#' ) // skip comments
                        break lines;
                    fieldnum++;
                    if( ! addField( tok, fieldnum ) ) { // assume it's the end of tags
                        try { in.reset(); }
                        catch( Exception e ) {
                            System.err.println( e.getMessage() );
                            e.printStackTrace();
                        }
                        break lines;
                    }
                } // endwhile
            } // endfor
        } // end lines:
// Sanity checks
        boolean has_label = false, has_code = false;
        for( int i = 0; i < fPresent.length; i++ ) {
            if( fPresent[i] == Data.SEQ_CODE ) has_code = true;
            if( fPresent[i] == Data.LABEL ) has_label = true;
        }
        if( ! has_code ) {
            fErrs.addError( Messages.ERR_NOCODE );
            return -1;
        }
        if( ! has_label ) {
            fErrs.addError( Messages.ERR_NOLABEL );
            return -1;
        }
        return (line - 1);
    } //*************************************************************************
    /** Parses input data.
     * @param in BufferedReader input stream
     * @return false on critical error, true otherwise
     * @param line line number
     */
    private boolean parseData( BufferedReader in, int line ) {
        StringTokenizer st = null;
        String tok;
        StringBuffer tmpbuf = new StringBuffer();
        String label, atom_name, atom_type, shift_val, shift_err;
        Integer seq_code, shift_amb;
        String [] atom_names = null;
        String s = Utils.readLine( in, line, fErrs );
        for( int linenum = line; s != null; linenum++ ) {
            if( s.trim().equals( Data.LOOP_END ) ) {
                try { in.close(); }
                catch( IOException e ) { /* ignore it */ }
                return true; // end of loop
            }
            if( s.trim().charAt( 0 ) != '#' ) { // ignore comments
                label = null; 
                atom_name = null; 
                atom_type = null; 
                seq_code = null; 
                shift_amb = null;
                shift_val = null;
                shift_err = null;
                tmpbuf.setLength( 0 );
                st = null;
                st = new StringTokenizer( s );
                for( int fieldnum = 0; st.hasMoreTokens(); fieldnum++ ) {
                    tok = st.nextToken();
                    if( fieldnum >= fPresent.length ) //  must be comment
                        tmpbuf.append( " " + tok );
                    else {
                        switch( fPresent[fieldnum] ) {
                            case Data.SHIFT_ID : // shift ID -- ignore it
                                break;
                            case Data.SEQ_CODE : // residue sequence code
                                seq_code = Utils.toInteger( tok );
                                if( seq_code == null ) {
                                    fErrs.addError( linenum, "?", label, Messages.ERR_NOCODE + " " + tok );
                                    return false;
                                }
                                break;
                            case Data.LABEL : // residue label
                                label = tok;
                                break;
                            case Data.ATOM_NAME : // atom anme
                                atom_name = tok;
                                break;
                            case Data.ATOM_TYPE : // atom type
                                atom_type = tok;
                                break;
                            case Data.SHIFT_VAL : // chemical shift value
                                if( Float.isNaN( Utils.floatValue( tok ) ) )
                                    tmpbuf.append( " shift value= " + tok );
                                else shift_val = tok;
                                break;
                            case Data.SHIFT_ERR : // chemical shift value error
                                if( Float.isNaN( Utils.floatValue( tok ) ) )
                                    tmpbuf.append( " shift error= " + tok );
                                else shift_err = tok;
                                break;
                            case Data.SHIFT_AMB : // chemical shift ambiguity code
                                shift_amb = Utils.toInteger( tok );
                                if( shift_amb == null ) tmpbuf.append( " amb. code= " + tok );
                                break;
                        } // endswitch
                    } // end else
                } // endfor fieldnum
// Finished with record. 
// check that we have label and sequence code
                if( seq_code == null ) {
                    fErrs.addError( linenum, "?", label, Messages.ERR_NOCODE );
                    return false;
                }
                if( label == null ) {
                    fErrs.addError( linenum, seq_code.toString(), "?", Messages.ERR_NOLABEL );
                    return false;
                }
                fData.add( new Residue( fRestype, seq_code.intValue(), label,  fPool ) );
                Atom atom = new Atom( atom_name, fPool );
                atom.setType( atom_type );
                atom.setShiftValue( shift_val );
                atom.setShiftError( shift_err );
                if( shift_amb != null ) atom.setShiftAmbiguityCode( shift_amb.intValue() );
                if( tmpbuf.length() > 0 ) atom.addComment( tmpbuf.toString() );
                fData.addAtom( seq_code.intValue(), atom );
            } // endif not a comment
            s = Utils.readLine( in, linenum, fErrs );
        } //endfor linenum
        try { in.close(); }
        catch( IOException e ) { /* ignore it */ }
        return true;
    } //*************************************************************************
    /**
     * Main does nothing.
     * @param args the command line arguments
     */
    public static void main (String args[]) {
        ErrorList errs = new ErrorList( true );
        Data data = new Data( errs );
        EDU.bmrb.lib.StringPool pool = new EDU.bmrb.lib.StringPool();
        Star2 conv = new Star2( data, errs, -1, pool );
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
