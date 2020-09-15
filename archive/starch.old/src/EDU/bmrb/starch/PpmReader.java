/*
 * PpmReader.java
 *
 * Created on December 15, 2004, 6:36 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/PpmReader.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/04/13 17:37:07 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: PpmReader.java,v $
 * Revision 1.1  2005/04/13 17:37:07  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch;

/**
 * Converter for PPM format.
 * <P>
 * PPM format description is available in CAMRA documentation at 
 * <A href="http://www.pence.ca/software/camra/">www.pence.ca/software/camra/</A>.
 * Record format:<pre>
  molNum:Residue_ResId:atom  shift_value [ shift_value]
  </pre>
 * Lines starting with '!' are comments. Shift values of ***.**, 999.99,
 * and -999.99 denote an unknown value.
 *
 * @author  dmaziuk
 */
public class PpmReader implements Reader {
    /** comment char */
    public static final char COMMENT = '!';
    /** DB storage */
    private LoopTable fLt = null;
    /** error list */
    private ErrorList fErrs = null;
    /** insert statement */
    private java.sql.PreparedStatement fStat = null;
    /** atom id */
    private int fId = 1;
    /** error flag */
    private boolean fOk = true;
    /** error message */
    private String fMessage = null;
//******************************************************************************
    /** Creates a new instance of PpmReader.
     * @param lt loop table
     * @param errs error list
     */
    public PpmReader( LoopTable lt, ErrorList errs ) {
        fLt = lt;
        fErrs = errs;
    } //************************************************************************
    /** Returns parse result: success or failure.
     * @return true or false
     */
    public boolean parsedOk() {
        return fOk;
    } //************************************************************************
     /** Parses input.
     * @param in input stream
     */
    public void parse( java.io.BufferedReader in ) {
        int lineno = 0;
        try {
            fStat = fLt.getConnection().prepareStatement( "INSERT INTO LOOP " +
            "(ID,COMPIDXID,SEQID,ASEQCODE,ACOMPCODE,AATMCODE,SVALUE) VALUES (?,?,?,?,?,?,?)" );
            String line;
            java.util.StringTokenizer stok;
            String tok;
            int i;
            String label = null, seqno = null, atom = null, shift = null;
            boolean firstline = true;
// parse
             Atom atm;
             while( (line = in.readLine()) != null ) {
                 lineno++;
                 if( line.trim().charAt( 0 ) == COMMENT ) continue;
                 if( ! line.matches( ".*\\S+.*" ) ) continue;
                 stok = new java.util.StringTokenizer( line, " \t\n\r\f:_" );
                 i = 0;
                 while( stok.hasMoreTokens() ) {
                     tok = stok.nextToken();
                     switch( i ) {
                         case 0 : // insert row
                             if( firstline ) {
                                 firstline = false;
                                 break;
                             }
                             if( label == null || seqno == null || atom == null || shift == null ) {
                                 if( fErrs != null ) fErrs.addError( lineno, "Not enough fields" );
                                 else System.err.println( lineno + ": not enough fields" );
                             }
                             else insertRow( lineno, seqno, label, atom, shift );
                             label = null;
                             seqno = null;
                             atom = null;
                             shift = null;
                             break;
                         case 1 : // label
                             label = tok;
                             break;
                         case 2 : // sequence num
                             seqno = tok;
                             break;
                         case 3 : // atom name
                             atom = tok;
                             break;
                         case 4 : // shift value
                             shift = tok;
                             break;
                         default : // ignore everything else
                             break;
                     } // endswitch
                     i++;
                 } // endwhile moreTokens()
                 stok = null;
             } // endwhile readline()
             in.close();
            fStat.getConnection().commit();
            fStat.close();
            fStat = null;
        }
        catch( java.sql.SQLException e ) {
            if( fErrs != null ) fErrs.addError( lineno, "DB exception" );
            System.err.println( "DB exception at" );
            System.err.println( fStat );
            System.err.println( e );
            e.printStackTrace();
            fOk = false;
            fMessage = "DB error at line " + lineno + " (" + fStat + ") " + e.getMessage();
        }
        catch( java.io.IOException ie ) {
            if( fErrs != null ) fErrs.addError( lineno, "I/O exception" );
            System.err.println( "I/O exception at line " + lineno );
            System.err.println( ie.getMessage() );
            ie.printStackTrace();
            fOk = false;
            fMessage = "I/O error at line " + lineno + " (" + fStat + ") " + ie.getMessage();
        }
    } //************************************************************************
    /** Inserts one row.
     * @param line line number
     * @param seqno sequence number
     * @param label residue label
     * @param atom atom
     * @param shift value
     */
    private void insertRow( int line, String seqno, String label, String atom,
    String shift ) throws java.sql.SQLException {
        int seq;
        try { seq = Integer.parseInt( seqno ); }
        catch( NumberFormatException e ) {
            if( fErrs != null ) fErrs.addError( line, seqno, "?", "Invalid residue sequence code" );
            else System.err.println( line + ": invalid residue sequence code " + seqno );
            return;
        }
        float value;
        try { value = Float.parseFloat( shift ); }
        catch( NumberFormatException e ) {
            if( fErrs != null ) fErrs.addError( line, seqno, (label != null ? label : "?"),
            "Invalid shift value: " + shift );
            else System.err.println( line + ": invalid shift value " + shift );
            return;
        }
        if( shift.matches( "-?9{3,}\\.9{2,}" ) ) {
            if( fErrs != null ) fErrs.addError( line, seqno, (label != null ? label : "?"),
            "Invalid shift value: " + shift );
            else System.err.println( line + ": invalid shift value " + shift );
            return;
        }
        fStat.setInt( 1, fId );
        fStat.setInt( 2, seq );
        fStat.setInt( 3, seq );
        fStat.setInt( 4, seq );
        fStat.setString( 5, label );
        fStat.setString( 6, atom );
        fStat.setString( 7, shift );
        fStat.executeUpdate();
        fId++;
    } //************************************************************************
    /** Returns error message.
     * @return error message or null.
     */
    public String getMessage() {
        return fMessage;
    } //************************************************************************
    /** Main method -- testing.
     * 1st argument, if present, is input file name. Otherwise reads stdin.
     * Dumps database table in CSV format to stdout when finished.
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        LoopTable t = new LoopTable();
        ErrorList e = new ErrorList();
        PpmReader r = new PpmReader( t, e );
        try {
            t.connect();
            t.createTable();
            java.io.BufferedReader in;
            if( args.length < 1 ) 
                in = new java.io.BufferedReader( new java.io.InputStreamReader( 
                System.in, "ISO-8859-15" ) );
            else in = new java.io.BufferedReader( new java.io.InputStreamReader( 
            new java.io.FileInputStream( args[0] ), "ISO-8859-15" ) );
            r.parse( in );
            in.close();
            t.print( System.out );
            t.disconnect();
            if( e.size() > 0 ) e.printErrors( System.out );
        }
        catch( Exception ex ) {
            System.err.println( ex );
            ex.printStackTrace();
        }
    } //************************************************************************
}
