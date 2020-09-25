/*
 * ErrorList.java
 *
 * Created on December 20, 2002, 3:08 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/ErrorList.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/05/13 22:45:51 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: ErrorList.java,v $
 * Revision 1.2  2005/05/13 22:45:51  dmaziuk
 * working on better error reporting
 *
 * Revision 1.1  2005/04/13 17:37:06  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.3  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.2  2003/01/06 22:45:55  dmaziuk
 * Bugfix release
 *
 * Revision 1.1  2002/12/30 22:22:38  dmaziuk
 * major rewrite of data structure
 *
 *
 */

package EDU.bmrb.starch;

/**
 * List of error messages.
 * @author  dmaziuk
 * @version 1
 */
public class ErrorList {
    /** vector of messages */
    private java.util.List fErrs = null;
    /** verbose flag */
    private boolean fVerbose = true; //false;
    /** message buffer */
    private StringBuffer fBuf = null;
//******************************************************************************
    /** comparator for sorting */
    private class Sorter implements java.util.Comparator {
        /** Sorts errors by residue number and atom name,
         *  regardless of severity.
         */
        public int compare(Object obj, Object obj1) {
            if( obj == obj1 || obj.equals( obj1 ) ) return 0;
            if( obj == null ) return -1; // nulls first
            if( obj1 == null ) return 1;
            String msg1 = (String) obj;
            if( msg1.trim().length() < 2 ) return -1;
            String msg2 = (String) obj1;
            if( msg2.trim().length() < 2 ) return 1;
//System.err.println( "Compare " + msg1 + " and " + msg2 );
            int start = msg1.indexOf( ':' );
            if( start < 0 ) return -1;
            int end = msg1.indexOf( ':', start + 1 );
            if( end < 0 ) return -1;
            String seq1 = msg1.substring( start + 1, end );
            start = msg2.indexOf( ':' );
            if( start < 0 ) return 1;
            end = msg2.indexOf( ':', start + 1 );
            if( end < 0 ) return 1;
            String seq2 = msg2.substring( start + 1, end );
//System.err.println( "Compare " + seq1 + " and " + seq2 );
            int s1 = -1, s2 = -1;
            try { 
                s1 = Integer.parseInt( seq1 );
            }
            catch( NumberFormatException e ) {
                return -1;
            }
            try {
                s2 = Integer.parseInt( seq2 );
            }
            catch( NumberFormatException e ) {
                return 1;
            }
            if( s1 < s2 ) return -1;
            else if( s1 > s2 ) return 1;
            else {
// compare atoms
                start = msg1.indexOf( seq1 );
                start = msg1.indexOf( ':', start + 1 );
                if( start < 0 ) return -1;
                end = msg1.indexOf( ':', start + 1 );
                if( end < 0 ) return -1; // no atom name
                seq1 = msg1.substring( start + 1, end );
                start = msg2.indexOf( seq2 );
                start = msg2.indexOf( ':', start + 1 );
                if( start < 0 ) return 1;
                end = msg2.indexOf( ':', start + 1 );
                if( end < 0 ) return 1; // no atom name
                seq2 = msg2.substring( start + 1, end );
                return seq1.compareTo( seq2 );
            }
        }
    }
//******************************************************************************
    /** Creates new ErrorList */
    public ErrorList() {
        fErrs = new java.util.ArrayList();
        fBuf = new StringBuffer();
    } //*************************************************************************
    /** Creates new ErrorList.
     * @param verbose verbose flag
     */
    public ErrorList( boolean verbose ) {
        fErrs = new java.util.ArrayList();
        fVerbose = verbose;
        fBuf = new StringBuffer();
    } //*************************************************************************
    /** Sets verbose flag.
     * @param verbose boolean
     * @see #addWarning( String )
     */
    public void setVerbose( boolean verbose ) {
        fVerbose = verbose;
    } //*************************************************************************
    /** Returns verbose flag.
     * @return boolean verbose flag
     * @see #addWarning( String )
     */
    public boolean getVerbose() {
        return fVerbose;
    } //*************************************************************************
    /** Adds error message.
     * This method adds String to the list of conversion error/warning messages.
     * Unlike addWarning(), this method ignores verbose flag.
     * @param msg String message
     * @see #addWarning( String )
     * @see Converter#read( BufferedReader )
     */
    public void addError(String msg) {
        fErrs.add( new Error( Error.SVR_ERR, Error.E_UNSPEC, "", "", "", msg ) );
    } //*************************************************************************
    /** Adds warning message.
     * This method adds String to the list of conversion error/warning messages.
     * If verbose flag is false, message is not added.
     * @param msg String message
     */
    public void addWarning(String msg) {
        if( ! fVerbose ) return;
        fErrs.add( new Error( Error.SVR_WARN, Error.E_UNSPEC, "", "", "", msg ) );
    } //*************************************************************************
    /** Adds error message.
     * This method adds String to the list of conversion error/warning messages.
     * Unlike addWarning(), this method ignores verbose flag.
     * @param linenum line number
     * @param msg String message
     * @see #addWarning( String )
     */
    public void addError(int linenum, String msg) {
        fErrs.add( new Error( Error.SVR_ERR, Error.E_UNSPEC, "", "",
        Integer.toString( linenum ), msg ) );
    } //*************************************************************************
    /** Adds warning message.
     * This method adds String to the list of conversion error/warning messages.
     * If verbose flag is false, message is not added.
     * @param linenum line number
     * @param msg String message
     */
    public void addWarning(int linenum, String msg) {
        if( ! fVerbose ) return;
        fErrs.add( new Error( Error.SVR_WARN, Error.E_UNSPEC, "", "",
        Integer.toString( linenum ), msg ) );
    } //*************************************************************************
    /** Adds error message.
     * This method adds formatted error message to the list of conversion
     * error/warning messages. Unlike addWarning(), this method ignores verbose
     * flag.
     * @param seqcode sequence code
     * @param label residue label
     * @param msg String message
     * @see #addWarning( String )
     */
    public void addError(String seqcode, String label, String msg) {
        fErrs.add( new Error( Error.SVR_ERR, Error.E_UNSPEC, seqcode, label, "", msg ) );
    } //*************************************************************************
    /** Adds warning message.
     * This method adds formatted message to the list of conversion error/warning
     * messages. If verbose flag is false, message is not added.
     * @param seqcode sequence code
     * @param label residue label
     * @param msg String message
     */
    public void addWarning(String seqcode, String label, String msg) {
        if( ! fVerbose ) return;
        fErrs.add( new Error( Error.SVR_WARN, Error.E_UNSPEC, seqcode, label, "", msg ) );
    } //*************************************************************************
    /** Adds error message.
     * This method adds formatted error message to the list of conversion
     * error/warning messages. Unlike addWarning(), this method ignores verbose
     * flag.
     * @param linenum line number
     * @param seqcode sequence code
     * @param label residue label
     * @param msg String message
     * @see #addWarning( String )
     */
    public void addError(int linenum, String seqcode, String label, String msg) {
        fErrs.add( new Error( Error.SVR_ERR, Error.E_UNSPEC, seqcode, label,
        Integer.toString( linenum ), msg ) );
    } //*************************************************************************
    /** Adds warning message.
     * This method adds formatted message to the list of conversion error/warning
     * messages. If verbose flag is false, message is not added.
     * @param linenum line number
     * @param seqcode sequence code
     * @param label residue label
     * @param msg String message
     */
    public void addWarning(int linenum, String seqcode, String label, String msg) {
        if( ! fVerbose ) return;
        fErrs.add( new Error( Error.SVR_ERR, Error.E_UNSPEC, seqcode, label,
        Integer.toString( linenum ), msg ) );
    } //*************************************************************************
    /** Adds error message.
     * This method adds formatted error message to the list of conversion
     * error/warning messages. Unlike addWarning(), this method ignores verbose
     * flag.
     * @param seqcode sequence code
     * @param label residue label
     * @param atom atom name
     * @param msg String message
     * @see #addWarning( String )
     */
    public void addError( String seqcode, String label, String atom, String msg) {
        fErrs.add( new Error( Error.SVR_ERR, Error.E_UNSPEC, seqcode, label, atom, msg ) );
    } //*************************************************************************
    /** Adds warning message.
     * @param seqcode sequence code
     * @param label residue label
     * @param atom atom name
     * @param msg String message
     * @see #addWarning( String )
     */
    public void addWarning( String seqcode, String label, String atom, String msg) {
        if( ! fVerbose ) return;
        fErrs.add( new Error( Error.SVR_WARN, Error.E_UNSPEC, seqcode, label, atom, msg ) );
    } //*************************************************************************
    /** Returns error message at specified index.
     * Returns message from the list of conversion errors/warnings.
     * @param index int message index
     * @return String message or null
     */
    public String errorAt(int index) {
        if( (index < 0) || (index >= fErrs.size()) ) return null;
        return (String) fErrs.get( index );
    } //************************************************************************
    /** Returns error count.
     * Returns number of messages in the list of conversion errors/warnings.
     * Application should check this number after Converter.read() has returned,
     * regardless of the return value: Converter.read() returns false on critical
     * errors only.
     * @return int message count
     */
    public int size() {
        return fErrs.size();
    } //************************************************************************
    /** Adds error to the list
     * @param err error to add
     */
    public void add( Error err ) {
        fErrs.add( err );
    } //************************************************************************
    /** Returns error message at specified index.
     * Returns message from the list of conversion errors/warnings.
     * @param index int message index
     * @return Error object
     * @throws IndexOutOfBoundsException
     */
    public Error get( int index ) {
        return (Error) fErrs.get( index );
    } //************************************************************************
    /** Clears list of conversion errors/warnings. */
    public void clear() {
        fErrs.clear();
    } //************************************************************************
    /** Sorts the list */
    public void sort() {
//        java.util.Collections.sort( fErrs, new Sorter() );
        java.util.Collections.sort( fErrs );
    } //************************************************************************
    /** Returns iterator.
     * @return iterator
     */
    public java.util.Iterator iterator() {
        return fErrs.iterator();
    } //************************************************************************
    /** Prints out error messages.
     * @param out output stream
     */
    public void printErrors(java.io.PrintStream out) {
        if( out == null ) return;
        for( int i = 0; i < fErrs.size(); i++ ) 
            out.println( fErrs.get( i ) );
        out.flush();
    } //************************************************************************
    /** Prints out error messages.
     * @param out output stream
     */
    public void printErrors(java.io.PrintWriter out) {
        if( out == null ) return;
        for( int i = 0; i < fErrs.size(); i++ ) 
            out.println( fErrs.get( i ) );
        out.flush();
    } //************************************************************************
    /** Returns error messages as one (possibly huge) String.
     * Use printErrors() instead.
     * @return String error messages
     */
    public String toString() {
        fBuf.setLength( 0 );
        for( int i = 0; i < fErrs.size(); i++ )
            fBuf.append( fErrs.get( i ).toString() + "\n" );
        return fBuf.toString();
    } //************************************************************************
}
