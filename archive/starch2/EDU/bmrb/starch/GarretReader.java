/*
 * GarretReader.java
 *
 * Created on December 15, 2004, 2:54 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/GarretReader.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/12/15 21:03:32 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: GarretReader.java,v $
 * Revision 1.2  2004/12/15 21:03:32  dmaziuk
 * converted index.html to unix
 * */

package EDU.bmrb.starch;

/**
 * Reads chemical shifts in garret format.
 * <P>
 * Garret format example is available in CAMRA documentation at 
 * <A href="http://www.pence.ca/software/camra/">www.pence.ca/software/camra/</A>.
 * Record format:<pre>
   1 ALA
   HA 4.130
   C  180.000
   ; 0.0
  </pre>
 * Shift values of 999.99 and -999.99 denote an unknown value.
 *
 * @author  dmaziuk
 */
public class GarretReader implements Reader {
    private final static boolean DEBUG = false; //true;
    /** record delimiter */
    public static final char ENDREC = ';';
    /** comment char */
    public static final char COMMENT = '#';
    /** DB storage */
    private LoopTable fLt = null;
    /** error list */
    private ErrorList fErrs = null;
    /** error flag */
    private boolean fOk = true;
//******************************************************************************
    /** Creates a new instance of GarretReader.
     * @param lt loop table
     * @param errs error list
     */
    public GarretReader( LoopTable lt, ErrorList errs ) {
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
        java.sql.PreparedStatement stat = null;
        int lineno = 0;
        try {
            stat = fLt.getConnection().prepareStatement( "INSERT INTO LOOP " +
            "(ID,COMPIDXID,SEQID,ASEQCODE,ACOMPCODE,AATMCODE,SVALUE) VALUES " +
            "(?,?,?,?,?,?,?)" );
// parse
            int id = 1;
            String line;
            java.util.StringTokenizer stok;
            String tok;
            String seqno = null, label = null, atom = null, shift = null;
            int seq = -1;
            float value = Float.NaN;
            boolean start_rek = true;
            boolean have_seq = false;
            while( (line = in.readLine()) != null ) {
                lineno++;
                if( ! line.matches( ".*\\S+.*" ) ) continue;
                if( line.trim().charAt( 0 ) == COMMENT ) continue;
// EOR
                if( line.trim().charAt( 0 ) == ENDREC ) {
                    atom = null;
                    label = null;
                    seqno = null;
                    have_seq = false;
                    start_rek = true;
                    continue;
                }
                stok = new java.util.StringTokenizer( line );
                if( start_rek ) {                   // new residue
                    if( stok.countTokens() < 2 ) {
                        if( fErrs != null ) fErrs.addError( lineno, "Not enough fields" );
                        else System.err.println( lineno + ": not enough fields" );
                        continue;
                    }
                    seqno = stok.nextToken();
                    try { 
                        seq = Integer.parseInt( seqno );
                        have_seq = true;
                    }
                    catch( NumberFormatException e ) {
                        if( fErrs != null ) fErrs.addError( lineno, seqno, "?", "Invalid residue sequence code" );
                        else System.err.println( lineno + ": invalid residue sequence code " + seqno );
                        continue;
                    }
                    label = stok.nextToken();
                    start_rek = false;
                }
                else {  // atoms
                    if( stok.countTokens() < 2 ) {
                        if( fErrs != null ) fErrs.addError( lineno, seqno, (label != null ? label : "?"),
                        "Not enough fields" );
                        else System.err.println( lineno + ": not enough fields" );
                        continue;
                    }
                    atom = stok.nextToken();
                    shift = stok.nextToken();
                    if( shift.matches( "-?9{3,}\\.9{2,}" ) ) {
                        value = Float.NaN;
                        if( fErrs != null ) fErrs.addError( lineno, seqno, (label != null ? label : "?"),
                        "Invalid shift value: " + shift );
                        else System.err.println( lineno + ": invalid shift value " + shift );
                        continue;
                    }
                    else try { value = Float.parseFloat( shift ); }
                    catch( NumberFormatException e ) {
                        if( fErrs != null ) fErrs.addError( lineno, seqno, (label != null ? label : "?"),
                        "Invalid shift value: " + shift );
                        else System.err.println( lineno + ": invalid shift value " + shift );
                        continue;
                    }
// insert row
                    if( have_seq && (label != null) && (atom != null) 
                    && (! Float.isNaN( value )) ) {
                        stat.setInt( 1, id );
                        stat.setInt( 2, seq );
                        stat.setInt( 3, seq );
                        stat.setInt( 4, seq );
                        stat.setString( 5, label );
                        stat.setString( 6, atom );
                        stat.setString( 7, shift );
                        stat.executeUpdate();
                        id++;
                    }
                }
                stok = null;
            } // endwhile readline()
            in.close();
            stat.getConnection().commit();
            stat.close();
        }
        catch( java.sql.SQLException e ) {
            if( fErrs != null ) fErrs.addError( lineno, "DB exception" );
            System.err.println( "DB exception at" );
            System.err.println( stat );
            System.err.println( e );
            e.printStackTrace();
            fOk = false;
        }
        catch( java.io.IOException ie ) {
            if( fErrs != null ) fErrs.addError( lineno, "I/O exception" );
            System.err.println( "I/O exception at line " + lineno );
            System.err.println( ie.getMessage() );
            ie.printStackTrace();
            fOk = false;
        }
    } //************************************************************************
    /** Main method -- testing.
     * 1st argument, if present, is input file name. Otherwise reads stdin.
     * Dumps database table in CSV format to stdout when finished.
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        LoopTable t = new LoopTable();
        ErrorList e = new ErrorList();
        GarretReader r = new GarretReader( t, e );
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
