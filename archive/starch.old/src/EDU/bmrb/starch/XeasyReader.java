/*
 * XeasyReader.java
 *
 * Created on March 2, 2005, 1:50 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/XeasyReader.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/04/13 17:37:07 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: XeasyReader.java,v $
 * Revision 1.1  2005/04/13 17:37:07  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch;

/**
 * XEASY parser.
 * This parser does not implement Reader since XEASY comes in two files.
 *
 * @author  dmaziuk
 */
public class XeasyReader {
    private final static boolean DEBUG = true;
    /** DB storage */
    private LoopTable fLt = null;
    /** error list */
    private ErrorList fErrs = null;
    /** author-defined sequences flag */
    private boolean fHasSeq = false;
    /** error flag */
    private boolean fOk = true;
    /** error message */
    private String fMessage = null;
//******************************************************************************
    /** Creates a new instance of XeasyReader.
     * @param table loop table
     * @param errs error list
     */
    public XeasyReader( LoopTable table, ErrorList errs ) {
        fLt = table;
        fErrs = errs;
    } //************************************************************************
    /** Returns parse result: success or failure.
     * @return true or false
     */
    public boolean parsedOk() {
        return fOk;
    } //************************************************************************
    /** Returns error message.
     * @return error message or null.
     */
    public String getMessage() {
        return fMessage;
    } //************************************************************************
    /** Sets "has sequence numbers" flag.
     * Set true if sequence file has residue sequence numbers in 2nd column.
     * Otherwise sequence numbers will be generated automatically.
     * @param flag true or false
     */
    public void setHasSequenceNumbers( boolean flag ) {
        fHasSeq = flag;
    } //************************************************************************
    /** Returns "has sequence numbers" flag.
     * @return true or false
     */
    public boolean hasSequenceNumbers() {
        return fHasSeq;
    } //************************************************************************
     /** Parses protons file.
     * @param in input stream
     */
    public void parseProtons( java.io.BufferedReader in ) {
        java.sql.PreparedStatement stat = null;
        int lineno = 0;
        int seq;
        float val;
        try {
            String line = null;
            String [] fields;
            stat = fLt.getConnection().prepareStatement( "INSERT INTO LOOP (ID," +
            "COMPIDXID,SEQID,ASEQCODE,AATMCODE,SVALUE,SERROR) VALUES (?,?,?,?,?,?,?)" );
            while( (line = in.readLine()) != null ) {
                lineno++;
                line = line.trim();
                if( line.length() < 1 ) continue; // ignore blank lines
                fields = line.split( "\\s+" );
if( DEBUG ) {
    System.err.print( "Line " + lineno );
    for( int i = 0; i < fields.length; i++ ) System.err.print( " |" + fields[i] + "|" );
    System.err.println( "\n***" );
}
                if( fields.length < 5 ) {
                    System.err.println( "Not enough fields in line " + lineno );
                    in.close();
                    stat.close();
                    return;
                }
                if( fields.length > 5 )
                    System.err.println( "Ignoring extra fields in line " + lineno );
                stat.setInt( 1, lineno ); // replace original atom numbers
 // check values, bad ones will cause DB exception at INSERT. Give user a meaningful error message.
                try { val = Float.parseFloat( fields[1] ); }
                catch( NumberFormatException e ) {
                    val = Float.NaN;
                    if( fErrs != null ) fErrs.addError( lineno, "Shift value is not" +
                    " a number: " + fields[1] );
                    System.err.print( "Shift value is not a number at line " );
                    System.err.print( lineno );
                    System.err.print( ": " );
                    System.err.println( fields[1] );
                }
                if( Float.isNaN( val ) ) stat.setNull( 6, java.sql.Types.VARCHAR );
                else stat.setString( 6, fields[1] );
                try { val = Float.parseFloat( fields[2] ); }
                catch( NumberFormatException e ) {
                    val = Float.NaN;
                    if( fErrs != null ) fErrs.addError( lineno, "Shift error is not" +
                    " a number: " + fields[2] );
                    System.err.print( "Shift error is not a number at line " );
                    System.err.print( lineno );
                    System.err.print( ": " );
                    System.err.println( fields[2] );
                }
                if( Float.isNaN( val ) ) stat.setNull( 7, java.sql.Types.VARCHAR );
                else stat.setString( 7, fields[2] );
                if( fields[3].equals( "." ) || fields[3].equals( "?" ) )
                    stat.setNull( 5, java.sql.Types.VARCHAR );
                else stat.setString( 5, fields[3].toUpperCase() );
                try { 
                    seq = Integer.parseInt( fields[4] );
                    stat.setInt( 2, seq );
                    stat.setInt( 3, seq );
                    stat.setInt( 4, seq );
                }
                catch( NumberFormatException e ) { 
                    stat.setNull( 2, java.sql.Types.VARCHAR );
                    stat.setNull( 3, java.sql.Types.VARCHAR );
                    stat.setNull( 4, java.sql.Types.VARCHAR );
                    if( fErrs != null ) fErrs.addError( lineno, "Sequence code is not" +
                    " a number: " + fields[4] );
                    System.err.print( "Sequence code is not a number at line " );
                    System.err.print( lineno );
                    System.err.print( ": " );
                    System.err.println( fields[4] );
                }
                stat.executeUpdate();
            }
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
            fMessage = "Protons: DB error at line " + lineno + " (" + stat + "): " + e.getMessage();
        }
        catch( java.io.IOException ie ) {
            if( fErrs != null ) fErrs.addError( lineno, "I/O exception" );
            System.err.println( "I/O exception at line " + lineno );
            System.err.println( ie.getMessage() );
            ie.printStackTrace();
            fOk = false;
            fMessage = "Protons: I/O error at line " + lineno + ": " + ie.getMessage();
        }
    } //************************************************************************
    /** Parses residue file.
     * @param in input stream
     */
    public void parseResudies( java.io.BufferedReader in ) {
        java.sql.PreparedStatement stat = null;
        int lineno = 0;
        try {
            int seq = 0;
            String line = null;
            String [] fields;
            stat = fLt.getConnection().prepareStatement( "UPDATE LOOP SET " +
            "ACOMPCODE=? WHERE ASEQCODE=?" );
            while( (line = in.readLine()) != null ) {
                lineno++;
                line = line.trim();
                if( line.length() < 1 ) continue; // ignore blank lines
                fields = line.split( "\\s+" );
if( DEBUG ) {
    System.err.print( "Line " + lineno + " seq#=" + fHasSeq );
    for( int i = 0; i < fields.length; i++ ) System.err.print( " |" + fields[i] + "|" );
    System.err.println( "\n***" );
}
                if( (fHasSeq && fields.length > 2) || (! fHasSeq && fields.length > 1) )
                    System.err.println( "Ignoring extra fields in line " + lineno );
                if( fHasSeq && fields.length < 2 ) {
                    System.err.println( "Not enough fields in line " + lineno + 
                    " (found " + fields.length + ", expected 2)" );
//                    fHasSeq = false;
                    in.close();
                    stat.close();
                    return;
                }
                if( fHasSeq && fields.length > 1 ) {
                    try { seq = Integer.parseInt( fields[1] ); }
                    catch( NumberFormatException e ) {
                        System.err.println( "Residue sequence code is not a number in line " +
                        lineno );
//                        fHasSeq = false;
                        in.close();
                        stat.close();
                        return;
                    }
                }
                if( ! fHasSeq ) seq++;
                stat.setString( 1, fields[0].toUpperCase() );
                stat.setInt( 2, seq );
                stat.executeUpdate();
            }
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
            fMessage = "Residues: DB error at line " + lineno + " (" + stat + "): " + e.getMessage();
        }
        catch( java.io.IOException ie ) {
            if( fErrs != null ) fErrs.addError( lineno, "I/O exception" );
            System.err.println( "I/O exception at line " + lineno );
            System.err.println( ie.getMessage() );
            ie.printStackTrace();
            fOk = false;
            fMessage = "Residues: I/O error at line " + lineno + ": " + ie.getMessage();
        }
    } //************************************************************************
    /** Main method.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    } //************************************************************************
}
