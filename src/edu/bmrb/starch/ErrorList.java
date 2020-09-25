package edu.bmrb.starch;

/**
 * List of error messages.
 *
 * This is a sorted list with no duplicates and O(log n) insertion time.
 *
 * @see Error#compareTo(Error)
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 1, 2006
 * Time: 3:25:06 PM
 *
 * $Id$
 */

public class ErrorList implements Iterable<Error> {
    private final static boolean DEBUG = false;
    /** storage. */
    protected java.util.ArrayList<Error> fList = null;
//*******************************************************************************
    /**
     * constructor.
     */
    public ErrorList() {
        fList = new java.util.ArrayList<Error>();
    } //*************************************************************************
    /**
     * Add new error to the list.
     * Duplicate errors or nulls are not added.
     * @param e error to add.
     */
    public void add( Error e ) {
        if( e == null ) return;
if( DEBUG ) System.err.printf( "Add error %s\n", e.toString() );
        if( fList.size() < 1 ) {
            fList.add( e );
            return;
        }
        int pos = java.util.Collections.binarySearch( fList, e );
        if( pos >= 0 ) return;
        pos = Math.abs( pos ) - 1;
        fList.add( pos, e );
    } //*************************************************************************
    /**
     * Removes all errors from the list.
     */
    public void clear() {
if( DEBUG ) System.err.println( "Clear errors" );
        fList.clear();
    } //*************************************************************************
    /**
     * Returns error art specified index.
     * @param index error index
     * @return error
     */
    public Error get( int index ) {
        return fList.get( index );
    } //*************************************************************************
    /**
     * Returns iterator ovet this list.
     * @return iterator
     */
    public java.util.Iterator<Error> iterator() {
        return fList.iterator();
    } //*************************************************************************
    /**
     * Returns number of errors in the list.
     * @return number of errors
     */
    public int size() {
        return fList.size();
    } //*************************************************************************
}
