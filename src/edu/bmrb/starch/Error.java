/*
 * Copyright (c) 2006 Board of Regents University of Wisconsin.
 * All Rights Reserved.
 */
package edu.bmrb.starch;

/**
 * Error object.
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Aug 29, 2006
 * Time: 2:44:36 PM
 *
 * $Id$
 */

public class Error implements Comparable<Error> {
    /** severity. */
    public enum Severity {
        /** critical. */
        CRIT,
        /** error. */
        ERR,
        /** warning. */
        WARN,
        /** info/notice. */
        INFO
    }
    /** codes. */
//    public enum Code {
        /** parser. */
//        PARSE,
        /** exception. */
//        EXCEP,
        /** unknown. */
//        UNDEF
//    }
    /** field separator for printout. */
    public static final char SEPARATOR = ':';
    /** severity. */
    private Severity fSvr = Severity.CRIT;
    /** error code. */
//    private Code fNum = Code.UNDEF;
    /** row number. */
    private int fRow = 0;
    /** seq#. */
    private String fSeq = null;
    /** label. */
    private String fComp = null;
    /** atom. */
    private String fAtom = null;
    /** error message. */
    private String fMsg = null;
//*******************************************************************************
    /**
     * Creates new Error.
     * @param svr severity
     * @param row error row number
     * @param seq residue sequence number
     * @param label residue label
     * @param atom atom name
     * @param msg error message
     */
    public Error( Severity svr, int row, String seq, String label, String atom,
                  String msg  ) {
        fSvr = svr;
        fRow = row;
        fSeq = seq;
        fComp = label;
        fAtom = atom;
        fMsg = msg;
    } //*************************************************************************
    /**
     * Returns 0 if this error is equal to <CODE>e</CODE>, positive number if
     * it's "greater", or negative number if it's "less than".
     * Errors are sorted by severity, then by row, then by sequence number:
     * numerically if possible, otherwise -- string comparison.
     * @param e error to compare to
     * @return number
     */
    public int compareTo( Error e ) {
        if( e == this ) return 0;
        if( e.fSvr.ordinal() != this.fSvr.ordinal() )
            return( this.fSvr.ordinal() - e.fSvr.ordinal() );
        if( this.fRow != e.fRow ) return( this.fRow - e.fRow );
        int s, es;
        try { es = Integer.parseInt( e.fSeq ); }
        catch( NumberFormatException nfe ) { es = -1; }
        try { s = Integer.parseInt( this.fSeq ); }
        catch( NumberFormatException nfe ) { s = -1; }
        if( (s == -1) || (es == -1) ) return this.fSeq.compareTo( e.fSeq );
        return( s - es );
    } //*************************************************************************
    /**
     * Returns true if two errors are equal
     * @param e error to compare to
     * @return true or false
     */
    public boolean equals( Error e ) {
        return( this.compareTo( e ) == 0 );
    } //*************************************************************************
    /**
     * Returns severity
     * @return severity
     */
    public Severity getSeverity() {
        return fSvr;
    } //*************************************************************************
    /**
     * Returns this error as string
     * @return "severity:number:seq.ID:comp.ID:atom.ID:message"
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append( fSvr.name() );
        buf.append( SEPARATOR );
        buf.append( fRow );
        buf.append( SEPARATOR );
        buf.append( fSeq );
        buf.append( SEPARATOR );
        buf.append( fComp );
        buf.append( SEPARATOR );
        buf.append( fAtom );
        buf.append( SEPARATOR );
        buf.append( fMsg );
        return buf.toString();
    } //*************************************************************************
} //*****************************************************************************
