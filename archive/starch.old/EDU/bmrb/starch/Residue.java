/*
 * Residue.java
 *
 * Created on December 18, 2002, 1:45 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Residue.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/07/02 19:37:33 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Residue.java,v $
 * Revision 1.6  2004/07/02 19:37:33  dmaziuk
 * changed handling of ambiguity codes
 *
 * Revision 1.5  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.4  2003/12/12 00:16:35  dmaziuk
 * Porting to Java 2
 *
 * Revision 1.3  2003/01/06 22:45:57  dmaziuk
 * Bugfix release
 *
 * Revision 1.2  2003/01/03 01:52:10  dmaziuk
 * Major bugfix/added functionality:
 * turned off "bad" nomenclature conversions -- ones that caused loss of data,
 * added a command-line parameter for residue types,
 * atom names are now quoted properly.
 *
 * Revision 1.1  2002/12/30 22:22:39  dmaziuk
 * major rewrite of data structure
 *
 *
 */

package EDU.bmrb.starch;

/**
 * Class that holds one residue.
 * Residue has a sequence code (number), label, and a list of atoms. There's
 * also residue type used by Nomenmap for nomenclature conversions.
 * @author  dmaziuk
 * @version 1
 * @see Nomenmap
 */
public class Residue implements java.io.Serializable {
    /** residue type */
    private int fType = -1;
    /** sequence number */
    private int fSeqNo = -1;
    /** label */
    private String fLabel = null;
    /** atoms */
    private java.util.List fAtoms = null;
    /** string pool */
    private EDU.bmrb.lib.StringPool fPool = null;
//*******************************************************************************
    /** Creates new Residue
     * @param seqno residue sequence number
     * @param label residue label
     * @param pool string pool
     */
    public Residue( int seqno, String label, EDU.bmrb.lib.StringPool pool ) {
        if( (label == null) || (label.length() < 1) ) 
            throw new NullPointerException( "Parameter missing" );
        fSeqNo = seqno;
        fLabel = label;
        fAtoms = new java.util.ArrayList();
        fPool = pool;
    } //*************************************************************************
    /** Creates new Residue
     * @param type residue type
     * @param seqno residue sequence number
     * @param label residue label
     * @param pool string pool
     */
    public Residue( int type, int seqno, String label, EDU.bmrb.lib.StringPool pool ) {
        if( (label == null) || (label.length() < 1) ) 
            throw new NullPointerException( "Parameter missing" );
        fType = type;
        fSeqNo = seqno;
        fLabel = label.toUpperCase();
        fAtoms = new java.util.ArrayList();
        fPool = pool;
    } //*************************************************************************
    /** Returns residue type.
     * @return residue type
     */
    public int getType() {
        return fType;
    } //*************************************************************************
    /** Sets residue type.
     * @param type residue type
     */
    public void setType( int type ) {
        fType = type;
    } //*************************************************************************
    /** Returns residue sequence code.
     * @return residue sequence code
     */
    public int getSequenceCode() {
        return fSeqNo;
    } //*************************************************************************
    /** Sets residue sequence code.
     * @param num residue sequence code
     */
    public void setSequenceCode( int num ) {
        fSeqNo = num;
    } //*************************************************************************
    /** Returns residue label.
     * @return residue label
     */
    public String getLabel() {
        return fLabel;
    } //*************************************************************************
    /** Sets residue label.
     * @param label residue label
     */
    public void setLabel( String label ) {
        if( fPool != null ) fLabel = fPool.add( label );
        else fLabel = label;
    } //*************************************************************************
    /** Returns list of atoms in this residue.
     * @return vector of atoms
     */
    public java.util.List getAtoms() {
        return fAtoms;
    } //*************************************************************************
    /** Adds list of atoms to this residue.
     * @param atoms vector of atoms
     */
    public void setAtoms( java.util.List atoms ) {
        fAtoms = atoms;
    } //*************************************************************************
    /** Adds an atom to this residue.
     * If atom with the same name already exists and has no chemical shift value,
     * copies values to existing atom. If there is no such atom, or existing
     * atom already has chemical shift value, adds atom to the list.
     * param atom atom object
     * @param atom atom to add
     */
    public void addAtom( Atom atom ) {
        Atom existing;
        for( int i = 0; i < fAtoms.size(); i++ ) {
            existing = (Atom) fAtoms.get( i );
            if( atom.getName().equals( existing.getName() ) ) {
                if( existing.getShiftValue() == null ) existing.setShiftValue( atom.getShiftValue() );
                if( existing.getShiftError() == null ) existing.setShiftError( atom.getShiftError() );
                if( existing.getType() == null ) existing.setType( atom.getType() );
                if( existing.getShiftAmbiguityCode() < 1 ) 
                    existing.setShiftAmbiguityCode( atom.getShiftAmbiguityCode() );
                    return;
            }
        }
        fAtoms.add( atom );
    } //*************************************************************************
    /** Returns number of atoms in the residue.
     * @return number of atoms
     */
    public int size() {
        return fAtoms.size();
    } //*************************************************************************
    /** Adds chemical shifts ambiguity codes to atoms that have chemical shift
     * values and no ambiguity codes.
     */
    public void addAmbiguityCodes() {
//        Atom atom;
//        for( int i = 0; i < fAtoms.size(); i++ ) {
//            atom = (Atom) fAtoms.get( i );
//            if( (atom.getShiftValue() != null ) && (atom.getShiftAmbiguityCode() < 1 ) )
//                atom.setShiftAmbiguityCode( 1 );
//        }
    } //*************************************************************************
    /** Returns atom at given index as tab-delimited string.
     * @param index atom index
     * @return tab-delimited string
     */
    public String atomSTARString( int index ) {
        if( (index < 0) || (index >= fAtoms.size()) ) return null;
        return( fSeqNo + "\t" + Utils.toNMRSTARQuotedString( fLabel ) + "\t"
        + ((Atom) fAtoms.get( index )).toSTARString() );
    } //*************************************************************************
    /** Returns atom at given index as comma-separated string.
     * @param index atom index
     * @return comma-separated string
     */
    public String atomCSVString( int index ) {
        if( (index < 0) || (index >= fAtoms.size()) ) return null;
        return( fSeqNo + "," + fLabel + "," 
        + ((Atom) fAtoms.get( index )).toCSVString() );
    } //*************************************************************************
    /** Returns this residue tab-delimited as string.
     * @return tab-delimited multi-line string
     */
    public String toSTARString() {
        StringBuffer buf = new StringBuffer();
        for( int i = 0; i < fAtoms.size(); i++ )
            buf.append( atomSTARString( i ) + "\n" );
        return buf.toString();
    } //*************************************************************************
    /** Returns this residue comma-separated as string.
     * @return comma-separated multi-line string
     */
    public String toCSVString() {
        StringBuffer buf = new StringBuffer();
        for( int i = 0; i < fAtoms.size(); i++ )
            buf.append( atomCSVString( i ) + "\n" );
        return buf.toString();
    } //*************************************************************************
    /** Returns this residue tab-delimited as string.
     * @return tab-delimited multi-line string
     */
    public String toString() {
        return toSTARString();
    } //*************************************************************************
}
