/*
 * Nomenclmap.java
 *
 * Created on February 11, 2005, 5:13 PM
 *
 * This software is copyright (c) 2005 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source$
 * 
 * AUTHOR:      $Author$
 * DATE:        $Date$
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log$ */

package EDU.bmrb.starch;

/**
 * Nomenclature mapper
 * @author  dmaziuk
 */
public class Nomenclmap {
    /** refdb */
    private RefDB fRefDb = null;
//******************************************************************************
    /** Creates a new instance of Nomenmap.
     * @param refdb reference DB
     */
    public Nomenclmap( RefDB refdb ) {
        fRefDb = refdb;
    } //************************************************************************
    /** Convert residue to BMRB nomenclature.
     * @param nomid nomenclature id
     * @param typeid residue type id
     * @param residue residue label or code
     * @param islabel if true, residue paramemter is residue label, else residue code
     * @param errs error list
     * @return list of BMRB atom IDs
     */
    public int [] convertResidue( int nomid, int typeid, String residue,
    boolean islabel, ErrorList errs ) {
        try {
            int bmrbnomid = fRefDb.getNomenclatureId( "BMRB" );
            StringBuffer sql = new StringBuffer( "SELECT" );
            if( nomid == bmrbnomid ) sql.append( " r.ID FROM RESIDUES r" );
            else sql.append( " n.BMRBID FROM RESIDUES r,RESNOM n" );
            sql.append( " WHERE r.TYPEID=" );
            sql.append( typeid );
            sql.append( " AND r.NOMID=" );
            sql.append( nomid );
            if( islabel ) sql.append( " AND r.LABEL='" );
            else sql.append( " AND r.CODE='" );
            sql.append( residue.toUpperCase() );
            sql.append( '\'' );
            if( nomid != bmrbnomid ) sql.append( " AND n.RESID=r.ID" );
            java.util.List ids = new java.util.ArrayList();
            java.sql.Statement query = fRefDb.getConnection().createStatement(
            java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
            java.sql.ResultSet rs = query.executeQuery( sql.toString() );
            while( rs.next() ) {
                ids.add( new Integer( rs.getInt( 1 ) ) );
            }
            rs.close();
            query.close();
            if( ids.size() < 1 ) return null;
            int [] rc = new int[ids.size()];
            for( int i = 0; i < rc.length; i++ ) rc[i] = ((Integer) ids.get( i )).intValue();
            ids.clear();
            return rc;
        }
        catch( java.sql.SQLException e ) {
            errs.addError( "DB exception, check error output" );
            System.err.println( e );
            e.printStackTrace();
            return null;
        }
    } //************************************************************************
    /** Convert residue to BMRB nomenclature.
     * @param nomid nomenclature id
     * @param typeid residue type id
     * @param label residue label
     * @param errs error list
     * @return list of BMRB atom IDs
     */
    public int [] convertResidueLabel( int nomid, int typeid, String label,
    ErrorList errs ) {
        return convertResidue( nomid, typeid, label, true, errs );
    } //************************************************************************
    /** Convert residue to BMRB nomenclature.
     * @param nomid nomenclature id
     * @param typeid residue type id
     * @param code residue code
     * @param errs error list
     * @return list of BMRB atom IDs
     */
    public int [] convertResidueCode( int nomid, int typeid, String code,
    ErrorList errs ) {
        return convertResidue( nomid, typeid, code, false, errs );
    } //************************************************************************
    /** Convert atom to BMRB nomenclature.
     * @param nomid nomenclature id
     * @param resid residue id
     * @param seqno residue sequence number
     * @param residue residue label
     * @param atom atom name
     * @param errs error list
     * @return list of BMRB atom IDs
     */
    public int [] convertAtom( int nomid, int resid, String seqno, String residue,
    String atom, ErrorList errs ) {
        try {
            int [] atoms = null;
            int [] pseudo = null;
            int bmrbnomid = fRefDb.getNomenclatureId( "BMRB" );
            String str = atom.toUpperCase();
            atom = fRefDb.convertAnyAtom( residue, str );
            if( ! atom.equals( str ) ) {
                errs.addWarning( seqno, residue, str, "Replacing with " + atom );
            }
            if( bmrbnomid != nomid ) {
                atoms = fRefDb.getBMRBAtomIds( nomid, resid, atom );
                if( atoms == null ) {
                    errs.addError( seqno, residue, atom, "Unknown atom" );
                    return null;
                }
            }
            else {
                atoms = new int [1];
                atoms[0] = fRefDb.getAtomId( nomid, resid, atom );
                if( atoms[0] < 0 ) {
                    errs.addError( seqno, residue, atom, "Unknown atom" );
                    return null;
                }
            }
            java.util.List l = new java.util.ArrayList();
            for( int i = 0; i < atoms.length; i++ ) {
                if( fRefDb.isPseudoAtom( atoms[i] ) ) {
                    pseudo = fRefDb.expandPseudoAtom( atoms[i] );
                    for( int j = 0; j < pseudo.length; j++ )
                        l.add( new Integer( pseudo[j] ) );
                }
                else l.add( new Integer( atoms[i] ) );
            }
            if( l.size() < 1 ) {
                errs.addError( seqno, residue, atom, "Unknown atom" );
                l.clear();
                return null;
            }
            atoms = new int[l.size()];
            for( int i = 0; i < atoms.length; i++ )
                atoms[i] = ((Integer) l.get( i )).intValue();
            l.clear();
            return atoms;
        }
        catch( java.sql.SQLException e ) {
            errs.addError( "DB exception, check error output" );
            System.err.println( e );
            e.printStackTrace();
            return null;
        }
    } //************************************************************************
}
