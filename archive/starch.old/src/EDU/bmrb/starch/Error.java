/*
 * Error.java
 *
 * Created on May 10, 2005, 1:55 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/Error.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/05/13 22:45:51 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Error.java,v $
 * Revision 1.2  2005/05/13 22:45:51  dmaziuk
 * working on better error reporting
 * */

package EDU.bmrb.starch;

/**
 * Error message.
 * @author  dmaziuk
 */
public class Error implements Comparable {
    /** separator */
    public static final char SEPARATOR = ':';
    /** severity */
    public static final String [] SVR = {
        "CRIT",
        "ERR",
        "WARN",
        "INFO"
    };
    /** severity: critcal */
    public static final int SVR_CRIT = 0;
    /** severity: error */
    public static final int SVR_ERR = 1;
    /** severity: warning */
    public static final int SVR_WARN = 2;
    /** severity: info */
    public static final int SVR_INFO = 3;
    /** error: short read */
    public static final int E_SHORTREAD = 0;
    /** error: bad residue sequence */
    public static final int E_BADSEQ = 1;
    /** error: bad residue label */
    public static final int E_BADLABEL = 2;
    /** error: bad atom name */
    public static final int E_BADATOM = 3;
    /** error: bad shift value */
    public static final int E_BADSHIFT = 4;
    /** error: bad shift error */
    public static final int E_BADSHERR = 5;
    /** error: bad ambiguity code */
    public static final int E_BADAMBI = 6;
    /** error: pseudoatom replaced */
    public static final int E_PSEUDOREPL = 10;
    /** error: methylene pseudoatom expanded */
    public static final int E_METREPL = 11;
    /** error: bad something or other */
    public static final int E_UNSPEC = 99;
    
    /** severity */
    private int fSvr = -1;
    /** code */
    private int fNum = -1;
    /** sequence (author) */
    private String fAuthorSeq = null;
    /** label (author) */
    private String fAuthorLabel = null;
    /** atom (auhtor) */
    private String fAuthorAtom = null;
    /** error message */
    private String fMsg = null;
//******************************************************************************
    /** Creates a new instance of Error.
     * @param svr severity
     * @param code error number
     * @param seq author residue sequence number
     * @param label author residue label
     * @param atom author atom name
     * @param msg error message
     */
    public Error( int svr, int code, String seq, String label, String atom, String msg ) {
        fSvr = svr;
        fNum = code;
        if( seq == null ) fAuthorSeq = "";
        else fAuthorSeq = seq;
        if( label == null ) fAuthorLabel = "";
        else fAuthorLabel = label;
        if( atom == null ) fAuthorAtom = "";
        else fAuthorAtom = atom;
        fMsg = msg;
    } //************************************************************************
    /** Returns an integer > 0 if this error is "greater" than obj, < 0 if it is
     * "less", and 0 if they are equal. If obj is null or not an Error, returns -1.
     * @param obj object to compare
     * @return integer
     */
    public int compareTo( Object obj ) {
        if( obj == null ) return -1;
        if( ! (obj instanceof Error) ) return -1;
        if( this == obj ) return 0;
        Error err = (Error) obj;
        if( fSvr < err.fSvr ) return -1;
        else if( fSvr > err.fSvr ) return 1;
        if( fNum < err.fNum ) return -1;
        else if( fNum > err.fNum ) return 1;
        int seq = -1, eseq = -1;
        try { seq = Integer.parseInt( fAuthorSeq ); }
        catch( NumberFormatException e ) { return -1; }
        try { eseq = Integer.parseInt( err.fAuthorSeq ); }
        catch( NumberFormatException e ) { return -1; }
        if( seq < eseq ) return -1;
        else if( seq > eseq ) return 1;
        return fAuthorAtom.compareTo( err.fAuthorAtom );
    } //************************************************************************
    /** Returns true if objects are equal.
     * @param obj another object
     * @return true or false
     */
    public boolean equals( Object obj ) {
        return this.compareTo( obj ) == 0;
    } //************************************************************************
    /** Returns severity
     * @return integer, one of SVR_ constants
     */
    public int getSeverity() {
        return fSvr;
    } //************************************************************************
    /** Returns error as string
     * @return string
     */
    public String toString() {
        StringBuffer buf = new StringBuffer( SVR[fSvr] );
        buf.append( SEPARATOR );
        buf.append( fNum );
        buf.append( SEPARATOR );
        buf.append( fAuthorSeq );
        buf.append( SEPARATOR );
        buf.append( fAuthorLabel );
        buf.append( SEPARATOR );
        buf.append( fAuthorAtom );
        buf.append( SEPARATOR );
        buf.append( fMsg );
        return buf.toString();
    } //************************************************************************
}
