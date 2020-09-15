/*
 * Nomenmap.java
 *
 * Created on December 27, 2002, 1:11 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/Nomenmap_old.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/04/13 17:37:07 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Nomenmap_old.java,v $
 * Revision 1.1  2005/04/13 17:37:07  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/18 18:44:13  dmaziuk
 * typo in nomenmap: 'C' instead of 'S'
 *
 * Revision 1.7  2004/10/18 18:34:25  dmaziuk
 * typo in nomenmap: 'C' instead of 'S'
 *
 * Revision 1.6  2004/07/02 19:37:33  dmaziuk
 * changed handling of ambiguity codes
 *
 * Revision 1.5  2004/01/08 19:38:18  dmaziuk
 * Bugfix
 *
 * Revision 1.4  2003/12/12 23:24:09  dmaziuk
 * Updated to Java 2 collections, added pool of unique string as backing storage
 *
 * Revision 1.3  2003/01/06 22:45:56  dmaziuk
 * Bugfix release
 *
 * Revision 1.2  2003/01/03 01:52:10  dmaziuk
 * Major bugfix/added functionality:
 * turned off "bad" nomenclature conversions -- ones that caused loss of data,
 * added a command-line parameter for residue types,
 * atom names are now quoted properly.
 *
 * Revision 1.1  2002/12/30 22:22:38  dmaziuk
 * major rewrite of data structure
 *
 *
 */

package EDU.bmrb.starch;
import java.util.*;
/**
 * Simple nomenclature mapper.
 * @author  dmaziuk
 * @version 1
 */
public class Nomenmap_old {
    private static final boolean DEBUG = true;
    /** 20 common amino acids */
    public static final String AA_NAME = "20 common amino acids";
    /** amino acids -- number */
    public static final int AA_NUMBER = 0;
    /** nucleic acids */
    public static final String NA_NAME = "nucleic acids";
    /** nucleic acids -- number */
    public static final int NA_NUMBER = 1;
    /** nucleic acid atom types */
    public static final String NA_TYPES = "CHNP";
    /** residue types */
    public static final String [] RESTYPES = { AA_NAME, NA_NAME }; 
    /* amino acids */
    /** Carbon */
    private static final String C = "C";
    /** Carbon - oxygen; gets converted to C */
    private static final String CO = "CO";
    /** Hydrogen */
    private static final String H = "H";
    /** Nitrogen proton; gets converted to H */
    private static final String HN = "HN";
    /** Nitrogen proton; gets converted to H */
    private static final String NH = "NH";
    /** Nitrogen is commonly written as NN */
    private static final String NN = "NN";
    /** Nitrogen */
    private static final String N = "N";
    /** HA */
    private static final String HA = "HA";
    /** HA1 */
    private static final String HA1 = "HA1";
    /** HA2 */
    private static final String HA2 = "HA2";
    /** HA3 */
    private static final String HA3 = "HA3";
    /** HB */
    private static final String HB = "HB";
    /** HB1 */
    private static final String HB1 = "HB1";
    /** HB2 */
    private static final String HB2 = "HB2";
    /** HB3 */
    private static final String HB3 = "HB3";
    /** HG */
    private static final String HG = "HG";
    /** HG1 */
    private static final String HG1 = "HG1";
    /** HG2 */
    private static final String HG2 = "HG2";
    /** HG3 */
    private static final String HG3 = "HG3";
    /** HG11 */
    private static final String HG11 = "HG11";
    /** HG12 */
    private static final String HG12 = "HG12";
    /** HG13 */
    private static final String HG13 = "HG13";
    /** HG21 */
    private static final String HG21 = "HG21";
    /** HD */
    private static final String HD = "HD";
    /** HD1 */
    private static final String HD1 = "HD1";
    /** HD2 */
    private static final String HD2 = "HD2";
    /** HD3 */
    private static final String HD3 = "HD3";
    /** HE */
    private static final String HE = "HE";
    /** HE1 */
    private static final String HE1 = "HE1";
    /** HE2 */
    private static final String HE2 = "HE2";
    /** HE3 */
    private static final String HE3 = "HE3";
    /** amino acid numbers */
    private static final int ALA = 0;
    private static final int ARG = 1;
    private static final int ASN = 2;
    private static final int ASP = 3;
    private static final int CYS = 4;
    private static final int GLN = 5;
    private static final int GLU = 6;
    private static final int GLY = 7;
    private static final int HIS = 8;
    private static final int ILE = 9;
    private static final int LEU = 10;
    private static final int LYS = 11;
    private static final int MET = 12;
    private static final int PHE = 13;
    private static final int PRO = 14;
    private static final int SER = 15;
    private static final int THR = 16;
    private static final int TRP = 17;
    private static final int TYR = 18;
    private static final int VAL = 19;
    /** amino acid symbols */
    private static final String [] fAAcodes = { "A", "R", "N", "D", "C", "Q",
    "E", "G", "H", "I", "L", "K", "M", "F", "P", "S", "T", "W", "Y", "V" };
    /** amino acid labels */
    private static final String [] fAAlabels = { "ALA", "ARG", "ASN", "ASP", "CYS",
    "GLN", "GLU", "GLY", "HIS", "ILE", "LEU", "LYS", "MET", "PHE", "PRO", "SER", 
    "THR", "TRP", "TYR", "VAL" };
    /* atom names */
    /** PROLINE */
    private static final String [] fPro = { "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "HG2", "HG3", "CD", "HD2", "HD3" };
    /** GLYCINE */
    private static final String [] fGly = { "H", "N", "CA", "HA2", "HA3", "C" };
    /**  ALANINE */
    // HN (NH) should get converted to H in alanine & the rest
    private static final String [] fAla = { "H", "N", "CA", "HA", "C", "CB", "HB" };
    // the rest have alanine sans CB, HB hanging off their CB
    /** ARGININE */
    private static final String [] fArg = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "HG2", "HG3", "CD", "HD2", "HD3", "NE", "HE", "CZ", "NH1", "HH11", 
    "HH12", "NH2", "HH21", "HH22" };
    /** ASPARAGINE */
    private static final String [] fAsn = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "ND2", "HD21", "HD22" };
    /** ASPARTIC ACID */
    private static final String [] fAsp = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "HD2" };
    /** CYSTEINE */ 
    private static final String [] fCys = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "SG", "HG" };
    /** GLUTAMINE */
    private static final String [] fGln = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "HG2", "HG3", "CD", "NE2", "HE21", "HE22" };
    /** GLUTAMIC ACID */
    private static final String [] fGlu = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "HG2", "HG3", "CD", "HE2" };
    /** HISTIDINE */
    private static final String [] fHis = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "ND1", "HD1", "CD2", "HD2", "CE1", "HE1", "NE2", "HE2" };
    /** ISOLEUCINE */
    private static final String [] fIle = { "H", "N", "CA", "HA", "C", "CB", "HB",
    "CG2", "HG2", "CG1", "HG12", "HG13", "CD1", "HD1" };
    /** LEUCINE */
    private static final String [] fLeu = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "CD1", "HD1", "CD2", "HD2", "HG" };
    /** LYSINE */
    private static final String [] fLys = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "HG2", "HG3", "CD", "HD2","HD3", "CE", "HE2", "HE3", "NZ", "HZ" };
    /** METHIONINE */
    private static final String [] fMet = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "HG2", "HG3", "CE", "HE" };
    /** PHENYLALANINE */
    private static final String [] fPhe = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "CD1", "HD1", "CD2", "HD2", "CE1", "HE1", "CE2", "HE2", "CZ", "HZ" };
    /** SERINE */
    private static final String [] fSer = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "HG" };
    /** THREONINE */
    private static final String [] fThr = { "H", "N", "CA", "HA", "C", "CB", "HB",
    "HG1", "CG2", "HG2" };
    /** TRYPTOPHAN */
    private static final String [] fTrp = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "CD1", "HD1", "CD2", "NE1", "HE1", "CE2", "CE3", "HE3", "CZ2",
    "HZ2", "CZ3", "HZ3", "CH2", "HH2" };
    /** TYROSINE */
    private static final String [] fTyr = { "H", "N", "CA", "HA", "C", "CB", "HB2",
    "HB3", "CG", "CD1", "HD1", "CD2", "HD2", "CE1", "HE1", "CE2", "HE2", "CZ", "HH" };
    /** VALINE */
    private static final String [] fVal = { "H", "N", "CA", "HA", "C", "CB", "HB", 
    "CG1", "HG1", "CG2", "HG2" };
    /** amino acid to atom names map */
    // note: these must be in the same order as in fAAlabels array
    private static final String [][] fAtoms = { fAla, fArg, fAsn, fAsp, fCys, 
    fGln, fGlu, fGly, fHis, fIle, fLeu, fLys, fMet, fPhe, fPro, fSer, fThr, fTrp,
    fTyr, fVal };
    /** all atom names */
    private static final String [] fAllAtoms = { "C", "N", "H", "CA", "HA", "HA2", 
    "HA3", "CB", "HB", "HB2", "HB3", "CG", "HG", "CG1", "HG1", "HG12", "HG13", 
    "CG2", "HG2", "HG3", "CD", "CD1", "ND1", "HD1", "CD2", "ND2", "HD2", "HD21", 
    "HD22", "HD3", "CD3", "CE", "NE", "HE", "CE1", "NE1", "HE1", "CE2", "NE2", 
    "HE2", "HE21", "HE22", "CE3", "HE3", "CZ", "NZ", "HZ", "CZ2", "HZ2", "CZ3", 
    "HZ3", "HH", "NH1", "HH11", "HH12", "CH2", "NH2", "HH2", "HH21", "HH22", "SG" };
    
    /** amino acid pseudoatoms map */
    private java.util.Map fAApseudos = null;
    
    /** string pool */
    private EDU.bmrb.lib.StringPool fPool = null;
//******************************************************************************
    /** Creates new Nomenmap.
     * @param pool string pool
     */
    public Nomenmap_old( EDU.bmrb.lib.StringPool pool ) {
        fPool = pool;
        fAApseudos = new java.util.HashMap();
// glycine
        java.util.Map glymap = new java.util.HashMap();
        String [] ha2ha3 = { "HA2", "HA3" };
        glymap.put( "QA", ha2ha3 );
        fAApseudos.put( fAAlabels[GLY], glymap );
// alanine
        java.util.Map alamap = new java.util.HashMap();
        String [] hb = { "HB" };
        alamap.put( "MB", hb );
        alamap.put( "QB", hb );
        fAApseudos.put( fAAlabels[ALA], alamap );
// valine
        java.util.Map valmap = new java.util.HashMap();
        String [] hg1 = { "HG1" };
        valmap.put( "MG1", hg1 );
        valmap.put( "QG1", hg1 );
        String [] hg2 = { "HG2" };
        valmap.put( "MG2", hg2 );
        valmap.put( "QG2", hg2 );
        String [] vals3 = { "HG1", "HG2" };
        valmap.put( "QG", vals3 );
        fAApseudos.put( fAAlabels[VAL], valmap );
// isoleucine
        java.util.Map ilemap = new java.util.HashMap();
        ilemap.put( "MG", hg2 );
        String [] hd1 = { "HD1" };
        ilemap.put( "MD", hd1 );
        ilemap.put( "QD1", hd1 );
        String [] hg12hg13 = { "HG12", "HG13" };
        ilemap.put( "QG", hg12hg13 );
        fAApseudos.put( fAAlabels[ILE], ilemap );
// leucine
        java.util.Map leumap = new java.util.HashMap();
        leumap.put( "MD1", hd1 );
        leumap.put( "QD1", hd1 );
        String [] hd2 = { "HD2" };
        leumap.put( "MD2", hd2 );
        leumap.put( "QD2", hd2 );
        String [] hb2hb3 = { "HB2", "HB3" };
        leumap.put( "QB", hb2hb3 );
        String [] hd1hd2 = { "HD1", "HD2" };
        leumap.put( "QD", hd1hd2 );
        fAApseudos.put( fAAlabels[LEU], leumap );
// proline
        java.util.Map promap = new java.util.HashMap();
        promap.put( "QB", hb2hb3 );
        String [] hg2hg3 = { "HG2", "HG3" };
        promap.put( "QG", hg2hg3 );
        String [] hd2hd3 = { "HD2", "HD3" };
        promap.put( "QD", hd2hd3 );
        fAApseudos.put( fAAlabels[PRO], promap );
// serine, aspartic asid, cysteine, histidine, tryptophan
        java.util.Map sermap = new java.util.HashMap();
        sermap.put( "QB", hb2hb3 );
        fAApseudos.put( fAAlabels[SER], sermap );
        fAApseudos.put( fAAlabels[ASP], sermap );
        fAApseudos.put( fAAlabels[CYS], sermap );
        fAApseudos.put( fAAlabels[HIS], sermap );
        fAApseudos.put( fAAlabels[TRP], sermap );
// threonine
        java.util.Map thrmap = new java.util.HashMap();
        thrmap.put( "MG", hg2 );
        thrmap.put( "MG2", hg2 );
        thrmap.put( "QG2", hg2 );
        fAApseudos.put( fAAlabels[THR], thrmap );
// asparagine
        java.util.Map asnmap = new java.util.HashMap();
        asnmap.put( "QB", hb2hb3 );
        String [] hd21hd22 = { "HD21", "HD22" };
        promap.put( "QD", hd21hd22 );
        fAApseudos.put( fAAlabels[ASN], asnmap );
// glutamic acid
        java.util.Map glumap = new java.util.HashMap();
        glumap.put( "QB", hb2hb3 );
        glumap.put( "QG", hg2hg3 );
        fAApseudos.put( fAAlabels[GLU], glumap );
// glutamine
        java.util.Map glnmap = new java.util.HashMap();
        glnmap.put( "QB", hb2hb3 );
        glnmap.put( "QG", hg2hg3 );
        String [] he21he22 = { "HE21", "HE22" };
        glnmap.put( "QE", he21he22 );
        fAApseudos.put( fAAlabels[GLN], glnmap );
// lysine
        java.util.Map lysmap = new java.util.HashMap();
        lysmap.put( "QB", hb2hb3 );
        lysmap.put( "QG", hg2hg3 );
        lysmap.put( "QD", hd2hd3 );
        String [] he2he3 = { "HE2", "HE3" };
        lysmap.put( "QE", he2he3 );
        String [] hz = { "HZ" };
        lysmap.put( "QE", hz );
        fAApseudos.put( fAAlabels[LYS], lysmap );
// arginine
        java.util.Map argmap = new java.util.HashMap();
        argmap.put( "QB", hb2hb3 );
        argmap.put( "QG", hg2hg3 );
        argmap.put( "QD", hd2hd3 );
        String [] hh11hh12 = { "HH11", "HH12" };
        argmap.put( "QH1", hh11hh12 );
        String [] hh21hh22 = { "HH21", "HH22" };
        argmap.put( "QH2", hh21hh22 );
        String [] hh11hh12hh21hh22 = { "HH11", "HH12", "HH21", "HH22" };
        argmap.put( "QH", hh11hh12hh21hh22 );
        fAApseudos.put( fAAlabels[ARG], argmap );
// methionine
        java.util.Map metmap = new java.util.HashMap();
        metmap.put( "QB", hb2hb3 );
        metmap.put( "QG", hg2hg3 );
        String [] he = { "HE" };
        metmap.put( "ME", he );
        metmap.put( "QE", he );
        fAApseudos.put( fAAlabels[MET], metmap );
// phenylalanine, tyrosine
        java.util.Map phemap = new java.util.HashMap();
        phemap.put( "QB", hb2hb3 );
        phemap.put( "QD", hd1hd2 );
        String [] he1he2 = { "HE1", "HE2" };
        phemap.put( "QE", he1he2 );
        fAApseudos.put( fAAlabels[PHE], phemap );
        fAApseudos.put( fAAlabels[TYR], phemap );
    } //************************************************************************
    /** Converts residue label.
     * If <I>label</I> is a 1-letter code, try to translate it into 3-letter
     * label. If that fails, check if it's a known label and add a warning message
     * if it isn't.
     * @param errs error list
     * @param residue residue
     */
    public void convertLabel( ErrorList errs, Residue residue ) {
        if( residue.getType() != AA_NUMBER ) return; // no mapings for non-AAs
// see if this is a valid label
        for( int i = 0; i < fAAlabels.length; i++ )
            if( fAAlabels[i].equals( residue.getLabel() ) )
                return;
// check if this is 1-letter code
        for( int i = 0; i < fAAcodes.length; i++ )
            if( fAAcodes[i].equals( residue.getLabel() ) ) {
                residue.setLabel( fAAlabels[i] );
                return;
            }
// if we get here, something's wrong
        errs.addWarning( Integer.toString( residue.getSequenceCode() ), 
        residue.getLabel(), Messages.ERR_RESLABEL );
    } //************************************************************************
    /** returns list of atoms in a residue.
     * @param errs error list
     * @param residue residue
     * @return list of atom names or null
     */
    public java.util.List getAtomNames( ErrorList errs, Residue residue ) {
        if( residue.getType() != AA_NUMBER ) return null;
        for( int i = 0; i < fAAlabels.length; i++ )
            if( fAAlabels[i].equals( residue.getLabel() ) ) {
                java.util.List rc = new java.util.ArrayList();
                for( int j = 0; j < fAtoms[i].length; j++ )
                    rc.add( new Atom( fAtoms[i][j], fPool ) );
                addAtomTypes( errs, residue, rc );
                return rc;
            }
        return null;
    } //************************************************************************
    /** Translates pseudo atom name into list of atom names.
     * @param atom atom name
     * @param residue residue
     * @return list of atom names or null
     */
    public String[] getAtomNames( String atom, Residue residue ) {
        if( residue.getType() != AA_NUMBER ) return null; // no mappings for !AAs
        String [] rc = null;
        java.util.Map h = (java.util.Map) fAApseudos.get( residue.getLabel() );
        if( h != null ) rc = (String []) h.get( atom );
        return rc;
    } //************************************************************************
    /** Adds atom types to all atoms in residue.
     * @param errs error list
     * @param residue residue
     */
    public void addAtomTypes( ErrorList errs, Residue residue ) {
        java.util.List atoms = residue.getAtoms();
        Atom a;
        String type;
        for( int i = 0; i < atoms.size(); i++ ) {
            a = (Atom) atoms.get( i );
            type = getAtomType( errs, residue, a.getName() );
            a.setType( type );
        }
    } //************************************************************************
    /** Adds atom types to all atoms in a vector.
     * @param errs error list
     * @param residue residue
     * @param atoms vector of atoms
     */
    public void addAtomTypes( ErrorList errs, Residue residue, java.util.List atoms ) {
        Atom a;
        String type;
        for( int i = 0; i < atoms.size(); i++ ) {
            a = (Atom) atoms.get( i );
            type = getAtomType( errs, residue, a.getName() );
            a.setType( type );
        }
    } //************************************************************************
    /** returns atom type.
     * @param errs error list
     * @param residue residue
     * @param atom atom name
     * @return atom type or null
     */
    public String getAtomType( ErrorList errs, Residue residue, String atom ) {
        if( residue.getType() == AA_NUMBER ) {
            for( int i = 0; i < fAllAtoms.length; i++ ) {
                if( atom.toUpperCase().equals( fAllAtoms[i] ) )
                    return atom.substring( 0, 1 ).toUpperCase();
            }
            errs.addError( Integer.toString( residue.getSequenceCode() ),
            residue.getLabel(), Messages.ERR_INVALIDATOM + ": " + atom );
            return null;
        }
        else if( residue.getType() == NA_NUMBER ) { 
// add warning if atom type is not in [CNHP]
            if( NA_TYPES.indexOf( atom.charAt( 0 ) ) < 0 )
                errs.addWarning( Integer.toString( residue.getSequenceCode() ),
                residue.getLabel(), Messages.ERR_CHKATOMTYPE + ": " + atom.charAt( 0 ) );
            return atom.substring( 0, 1 );
        }
        return null;
    } //************************************************************************
    /** Expands pseudo atoms in a residue.
     * @param errs error list
     * @param residue residue
     */
    public void expandPseudoAtoms( ErrorList errs, Residue residue ) {
        if( residue.getType() != AA_NUMBER ) return; // no mappings for non-AAs
        java.util.List newatoms = new java.util.ArrayList();
        java.util.List atoms = residue.getAtoms();
        Atom a;
        String [] names = null;
        for( int i = 0; i < atoms.size(); i++ ) {
            a = (Atom) atoms.get( i );
            names = getAtomNames( a.getName(), residue );
            if( names == null ) {
                newatoms.add( a );
                continue;
            }
            a.setName( names[0] );
            if( a.getShiftValue() == null ) { // no chemical shift
                newatoms.add( a );
                for( int j = 1; j < names.length; j++ ) 
                    newatoms.add( new Atom( names[j], fPool ) );
            }
            else {
//                if( a.getShiftAmbiguityCode() < 1 ) { // no ambiguity code -- add it
                if( a.getShiftAmbiguityCode() == 0 ) { // no ambiguity code from author
//                    a.setShiftAmbiguityCode( names.length );
                    a.setShiftAmbiguityCode( -1 );
                    newatoms.add( a );
                    for( int j = 1; j < names.length; j++ ) 
                        newatoms.add( new Atom( names[j], fPool ) );
                }
                else { // copy shift value/error and ambiguity code to all atoms
                    newatoms.add( a );
                    for( int j = 1; j < names.length; j++ ) {
                        Atom b = new Atom( names[j], fPool );
                        b.setShiftValue( a.getShiftValue() );
                        b.setShiftError( a.getShiftError() );
                        b.setShiftAmbiguityCode( a.getShiftAmbiguityCode() );
                        newatoms.add( b );
                    }
                }
            }
        }
        residue.setAtoms( newatoms );
        atoms = null;
    } //************************************************************************
    /** Converts atom names in a residue.
     * This method converts "NH" or "HN" to "H", "CO" to "C", "NN" to "N".
     * @param errs error list
     * @param residue residue
     */
    public void convertAtomNames( ErrorList errs, Residue residue ) {
        if( residue.getType() != AA_NUMBER ) return; // no mappings for non-AAs
        java.util.List atoms = residue.getAtoms();
        Atom a;
        for( int i = 0; i < atoms.size(); i++ ) {
            a = (Atom) atoms.get( i );
            if( a.getName().equals( CO ) ) a.setName( C );
            else if( a.getName().equals( HN ) || a.getName().equals( NH ) ) 
                a.setName( H );
            else if( a.getName().equals( NN ) ) a.setName( N );
        }
    } //************************************************************************
    /** Adds atoms to a residue.
     * This method generates "proper" list of atoms for the residue and merges
     * it with the existing list. "Proper" list contains atom names, types, and
     * chemical shift ambiguity codes. Existing list (hopefully) contains 
     * chemical shift values; they're copied to "proper" list.
     * @param errs error list
     * @param residue residue
     */
    public void addAtoms( ErrorList errs, Residue residue ) {
        if( residue.getType() == NA_NUMBER ) { // nucleic acids: add atom types
            addAtomTypes( errs, residue );
            return;
        }
        else if( residue.getType() != AA_NUMBER ) return; // no mappings for non-AAs
        java.util.List newatoms = getAtomNames( errs, residue );
        if( newatoms == null )
            throw new NullPointerException( "Invalid residue " + residue.getLabel() );
        java.util.List atoms = residue.getAtoms();
        Atom src, tgt;
        boolean found;
        boolean duplicate;
        for( int i = 0; i < atoms.size(); i++ ) {
            found = false;
            duplicate = false;
            src = (Atom) atoms.get( i );
//System.err.println( "Atom: " + src );
//System.err.println( ", newatoms.size = " + newatoms.size() );
            for( int j = 0; j < newatoms.size(); j++ ) {
               tgt = (Atom) newatoms.get( j );
//System.err.println( "New atom: " + tgt );
               if( tgt.getName().equals( src.getName() ) ) { // duplicate atom?
                   if( tgt.getShiftValue() != null ) duplicate = true;
                   else { // copy it over
                       tgt.setShiftValue( src.getShiftValue() );
                       tgt.setShiftError( src.getShiftError() );
                       tgt.setShiftAmbiguityCode( src.getShiftAmbiguityCode() );
                       tgt.addComment( src.getComment() );
                   }
                   found = true;
               }
            }
            if( ! found ) {
                src.addComment( " " + Messages.ERR_INVALIDATOM );
                newatoms.add( src );
            }
            if( duplicate ) {
                errs.addError( Integer.toString( residue.getSequenceCode() ),
                residue.getLabel(), Messages.ERR_DUPLICATEATOM + " " + src.getName() );
                src.addComment( " " + Messages.ERR_DUPLICATEATOM );
                newatoms.add( src );
            }
        }
        residue.setAtoms( newatoms );
        atoms.clear();
        atoms = null;
    } //************************************************************************
    /** Adds chemical shift ambiguity code to all atoms in residue.
     * Adds ambiguity code of 1 to those atoms that have chemical shift value and
     * no ambiguity code.
     * @param errs error list
     * @param residue residue
     */
    public void addAmbiguityCodes( ErrorList errs, Residue residue ) {
//        if( residue.getType() != AA_NUMBER ) return; // no mappings for non-AAs
        java.util.List atoms = residue.getAtoms();
        Atom a;
        for( int i = 0; i < atoms.size(); i++ ) {
            a = (Atom) atoms.get( i );
            if( (a.getShiftValue() != null) && (a.getShiftAmbiguityCode() < 1) )
                a.setShiftAmbiguityCode( 1 );
        }
    } //************************************************************************
    /** lists known residue types.
     * @return list of residue types
     */
    public String[] listResidueTypes() {
        return RESTYPES;
    } //************************************************************************
//************************* NEW CODE *******************************************
     /** Converts residue labels.
      * Converts non-BMRB names to BMRB, 1-letter codes to 3-letter labels.
      * @param table LoopTable
      * @param refdb reference DB
      * @param nomid nomenclature id
      * @param restype residue type id
      * @param haslabels true if author supplied 3-letter residue labels, false
      *                  if 1-letter codes
     * @param errs error list
      * @throws java.sql.SQLException
      */
     public static void convertResidues( LoopTable table, RefDB refdb, int nomid,
     int restype, boolean haslabels, ErrorList errs ) throws java.sql.SQLException {
         java.sql.ResultSet rs1 = null;
         int resid = -1;
// update statement
         java.sql.PreparedStatement stat = table.getConnection().prepareStatement( 
         "UPDATE LOOP SET COMPID=? WHERE ACOMPCODE=?" );
// residue ID
         StringBuffer sql = new StringBuffer( "SELECT ID,LABEL FROM RESIDUES" +
         " WHERE TYPEID=" );
         sql.append( restype );
         sql.append( " AND NOMID=" );
         sql.append( nomid );
         if( haslabels ) sql.append( " AND LABEL=?" );
         else sql.append( " AND CODE=?" );
         java.sql.PreparedStatement resquery = refdb.getConnection().prepareStatement(
         sql.toString(), java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
// residue map
         java.sql.PreparedStatement mapquery = refdb.getConnection().prepareStatement(
         "SELECT ID,LABEL FROM RESIDUES r,RESMAP m WHERE m.RESID=? AND" +
         " r.ID=m.BMRBID", java.sql.ResultSet.TYPE_FORWARD_ONLY,
         java.sql.ResultSet.CONCUR_READ_ONLY );
// main
         java.sql.Statement query = table.getConnection().createStatement(
         java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
         java.sql.ResultSet rs = query.executeQuery( "SELECT DISTINCT ACOMPCODE FROM LOOP" +
         " ORDER BY ACOMPCODE" );
         while( rs.next() ) {
             resquery.setString( 1, rs.getString( 1 ) );
             if( rs.wasNull() ) continue;
             rs1 = resquery.executeQuery();
             if( ! rs1.next() ) {
                 errs.addError( "Unknown residue: " + rs.getString( 1 ) );
                 continue;
             }
// map to BMRB residue
// FIXME: use a constant instead of hard-coded 0 or better fetch ID for "BMRB" from DB
             if( nomid != 0 ) {
                 mapquery.setInt( 1, rs1.getInt( 1 ) );
                 rs1 = mapquery.executeQuery();
                 if( ! rs1.next() ) {
                     errs.addError( "No BMRB residue for: " + rs.getString( 1 ) );
                     continue;
                 }
             }
// update
             stat.setString( 1, rs1.getString( 2 ) );
             stat.setString( 2, rs.getString( 1 ) );
             stat.executeUpdate();
         }
         stat.getConnection().commit();
         stat.close();
         if( rs1 != null ) rs1.close();
         rs.close();
         resquery.close();
         mapquery.close();
         query.close();
     } //***********************************************************************
    /** Expands pseudoatoms.
     * @param table LoopTable
     * @param refdb reference DB
     * @param restype residue type id
     * @param nomid nomenclature id
     * @param errs error list
     * @throws java.sql.SQLException
     */
    public static void convertAtoms( LoopTable table, RefDB refdb, int restype,
    int nomid, ErrorList errs ) throws java.sql.SQLException {
        int atomid, nextid;
        int [] atoms = null;
        String atomname = null;
        java.sql.Statement query = table.getConnection().createStatement(
        java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.PreparedStatement ustat = table.getConnection().prepareStatement(
        "UPDATE LOOP SET ATOMID=? WHERE COMPID=? AND AATMCODE=? AND ID=?" );
        java.sql.PreparedStatement istat = table.getConnection().prepareStatement(
        "INSERT INTO LOOP(ID,COMPID,ATOMID,ASEQCODE,ACOMPCODE,AATMCODE,SVALUE,SERROR," +
        "SFOMERIT,SAMBICODE,SOCCID,SDERIVID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)" );
        java.sql.ResultSet rs = query.executeQuery( "SELECT ID,COMPID,ATOMID,ASEQCODE," +
        "ACOMPCODE,AATMCODE,SVALUE,SERROR,SFOMERIT,SAMBICODE,SOCCID,SDERIVID FROM LOOP" );
        while( rs.next() ) {
// fetch atom id
            atomname = rs.getString( 6 ).toUpperCase();
            atomid = refdb.getAtomId( nomid, restype, rs.getString( 5 ), atomname );
            if( atomid < 0 ) {
                errs.addError( rs.getString( 4 ), rs.getString( 5 ), rs.getString( 6 ),
                "Unknown atom" );
if( DEBUG ) System.err.println( "Copy (unknown) row " + rs.getString( 4 ) + "," + rs.getString( 5 )
+ "," + refdb.getAtomName( atomid ) );
                ustat.setString( 1, atomname );
                ustat.setString( 2, rs.getString( 2 ) );
                ustat.setString( 3, rs.getString( 6 ) );
                ustat.setInt( 4, rs.getInt( 1 ) );
if( DEBUG ) System.err.println( ustat );
                ustat.executeUpdate();
                continue;
            }
            if( refdb.isPseudoAtom( atomid ) ) {
                atoms = refdb.expandPseudoAtom( atomid );
                if( (atoms == null) || (atoms.length == 0) ) {
                    errs.addError( rs.getString( 4 ), rs.getString( 5 ), rs.getString( 6 ),
                    "No data for pseudoatom" );
                    continue;
                }
// update existing row
if( DEBUG ) System.err.println( "Update row " + rs.getString( 4 ) + "," + rs.getString( 5 )
+ "," + rs.getString( 6 ) );
                ustat.setString( 1, refdb.getAtomName( atoms[0] ) );
                ustat.setString( 2, rs.getString( 2 ) );
                ustat.setString( 3, rs.getString( 6 ) );
                ustat.setInt( 4, rs.getInt( 1 ) );
if( DEBUG ) System.err.println( ustat );
                ustat.executeUpdate();
                nextid = TableUtils.getNextId( table );
                for( int i = 1; i < atoms.length; i++ ) {
// insert rows
if( DEBUG ) System.err.println( "Insert row " + rs.getString( 4 ) + "," + rs.getString( 5 )
+ "," + refdb.getAtomName( atoms[i] ) );
                    istat.setInt( 1, nextid );
                    istat.setString( 2, rs.getString( 2 ) );
                    istat.setString( 3, refdb.getAtomName( atoms[i] ) );
                    istat.setInt( 4, rs.getInt( 4 ) );
                    istat.setString( 5, rs.getString( 5 ) );
                    istat.setString( 6, rs.getString( 6 ) );
                    istat.setString( 7, rs.getString( 7 ) );
                    istat.setString( 8, rs.getString( 8 ) );
                    istat.setString( 9, rs.getString( 9 ) );
                    istat.setInt( 10, rs.getInt( 10 ) );
                    istat.setString( 11, rs.getString( 11 ) );
                    istat.setString( 12, rs.getString( 12 ) );
if( DEBUG ) System.err.println( istat );
                    istat.executeUpdate();
                    nextid++;
                }
            }
            else {
// if not BMRB atom
                if( ! refdb.getAtomNomenclature( atomid ).equals( "BMRB" ) ) {
if( DEBUG ) System.err.println( "Convert to BMRB atom " + rs.getString( 4 ) + "," + rs.getString( 5 )
+ "," + refdb.getAtomName( atomid ) );
                    int [] atomids = refdb.getBMRBAtomIds( atomid );
                    if( atomids == null ) {
                        errs.addError( rs.getString( 4 ), rs.getString( 5 ),
                        rs.getString( 6 ), "No BMRB atom" );
if( DEBUG ) System.err.println( "Copy (non-BMRB) row " + rs.getString( 4 ) + "," + rs.getString( 5 )
+ "," + refdb.getAtomName( atomid ) );
                        ustat.setString( 1, atomname );
                        ustat.setString( 2, rs.getString( 2 ) );
                        ustat.setString( 3, rs.getString( 6 ) );
                        ustat.setInt( 4, rs.getInt( 1 ) );
if( DEBUG ) System.err.println( ustat );
                        ustat.executeUpdate();
                        continue;
                    }
                    else atomid = atomids[0];
                }
if( DEBUG ) System.err.println( "Update (only) row " + rs.getString( 4 ) + "," + rs.getString( 5 )
+ "," + refdb.getAtomName( atomid ) );
                ustat.setString( 1, atomname );
                ustat.setString( 2, rs.getString( 2 ) );
                ustat.setString( 3, rs.getString( 6 ) );
                ustat.setInt( 4, rs.getInt( 1 ) );
if( DEBUG ) System.err.println( ustat );
                ustat.executeUpdate();
            }
        } // endwhile
        istat.getConnection().commit();
        ustat.getConnection().commit();
        istat.close();
        ustat.close();
        rs.close();
        query.close();
    } //************************************************************************
    /** Convert single atom to BMRB nomenclature.
     * @param table LoopTable
     * @param refdb reference DB
     * @param restype residue type id
     * @param nomid nomenclature id
     * @param seqno residue sequence number (Seq_ID)
     * @param atom atom name (Atom_ID)
     * @param errs error list
     * @throws java.sql.SQLException
     */
    public static void convertAtom( LoopTable table, RefDB refdb, int restype,
    int nomid, int seqno, String atom, ErrorList errs ) throws java.sql.SQLException {
        java.sql.PreparedStatement ustat = table.getConnection().prepareStatement(
        "UPDATE LOOP SET ATOMID=? WHERE COMPID=? AND AATMCODE=? AND PK=?" );
        StringBuffer sql = new StringBuffer( "SELECT PK,COMPID,ATOMID,ASEQCODE," +
        "ACOMPCODE,AATMCODE,SVALUE,SERROR,SFOMERIT,SAMBICODE,SOCCID,SDERIVID " +
        "FROM LOOP WHERE SEQID=" );
        sql.append( seqno );
        sql.append( " AND ATOMID='" );
        sql.append( atom );
        sql.append( '\'' );
        java.sql.Statement query = table.getConnection().createStatement(
        java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG ) System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        if( ! rs.next() ) {
            errs.addError( seqno, "?", atom, "Invalid atom" );
            rs.close();
            query.close();
            return;
        }
        java.util.List bmrbnames = new java.util.ArrayList();
// Java goto
        String atomname = null;
        end: {
// fetch atom id
            atomname = rs.getString( 6 ).toUpperCase();
            int atomid = refdb.getAtomId( nomid, restype, rs.getString( 5 ), atomname );
            if( atomid < 0 ) {
// not in DB, copy over
                errs.addError( rs.getString( 4 ), rs.getString( 5 ), atomname, "Unknown atom" );
if( DEBUG ) System.err.println( "Copy (unknown) row " + rs.getString( 4 ) + "," + rs.getString( 5 )
+ "," + refdb.getAtomName( atomid ) );
                bmrbnames.add( atomname );
                break end;
            }
// see if BMRB atom
            int [] atoms = null;
            if( refdb.getNomenclatureId( "BMRB" ) != nomid ) {
                atoms = refdb.getBMRBAtomIds( atomid );
                if( atoms == null ) {
// no mapping -- copy atom
                    errs.addError( rs.getString( 4 ), rs.getString( 5 ), atomname, "No mapping for atom" );
if( DEBUG ) System.err.println( "Copy (no mapping) row " + rs.getString( 4 ) +
"," + rs.getString( 5 ) + "," + refdb.getAtomName( atomid ) );
                    bmrbnames.add( atomname );
                    break end;
                }
            }
            else {
                atoms = new int[1];
                atoms[0] = atomid;
            }
if( DEBUG ) for( int k = 0; k < atoms.length; k++ ) System.err.println( "* " + atoms[k] );
            int [] pseudos;
            for( int i = 0; i < atoms.length; i++ ) {
                if( refdb.isPseudoAtom( atoms[i] ) ) {
if( DEBUG ) System.err.println( "** pseudoatom: " + atoms[i] );
                    pseudos = refdb.expandPseudoAtom( atoms[i] );
                    if( pseudos == null ) {
// no mapping -- copy atom
                        errs.addError( rs.getString( 4 ), rs.getString( 5 ), atomname,
                        "No mapping for pseudoatom" );
if( DEBUG ) System.err.println( "Copy (no pseudo map) row " + rs.getString( 4 ) +
"," + rs.getString( 5 ) + "," + refdb.getAtomName( atomid ) );
                        bmrbnames.add( refdb.getAtomName( atoms[i] ) );
                    }
                    else
                        for( int j = 0; j < pseudos.length; j++ )
                            bmrbnames.add( refdb.getAtomName( pseudos[j] ) );
                }
            } // endfor
        } // end:
if( DEBUG ) for( int k = 0; k < bmrbnames.size(); k++ ) System.err.println( "** " + bmrbnames.get(k) );
        if( bmrbnames.size() > 0 ) {
// copy over 1st row, insert additional ones
if( DEBUG ) System.err.println( "Copy 1st pseudo row " + rs.getString( 4 ) +
"," + rs.getString( 5 ) + "," + bmrbnames.get( 0 ) );
            ustat.setString( 1, (String) bmrbnames.get( 0 ) );
            ustat.setString( 2, rs.getString( 2 ) );
            ustat.setString( 3, rs.getString( 6 ) );
            ustat.setInt( 4, rs.getInt( 1 ) );
if( DEBUG ) System.err.println( ustat );
            ustat.executeUpdate();
            ustat.getConnection().commit();
            ustat.close();
        }
        if( bmrbnames.size() > 1 ) {
            java.sql.PreparedStatement istat = table.getConnection().prepareStatement(
            "INSERT INTO LOOP(ID,COMPID,ATOMID,ASEQCODE,ACOMPCODE,AATMCODE,SVALUE,SERROR," +
            "SFOMERIT,SAMBICODE,SOCCID,SDERIVID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)" );
            int nextid = TableUtils.getNextId( table );
            for( int i = 1; i < bmrbnames.size(); i++ ) {
// insert rows
if( DEBUG ) System.err.println( "Insert row " + rs.getString( 4 ) + "," + rs.getString( 5 )
+ "," + bmrbnames.get( i ) );
                istat.setInt( 1, nextid );
                istat.setString( 2, rs.getString( 2 ) );
                istat.setString( 3, (String) bmrbnames.get( i ) );
                istat.setInt( 4, rs.getInt( 4 ) );
                istat.setString( 5, rs.getString( 5 ) );
                istat.setString( 6, rs.getString( 6 ) );
                istat.setString( 7, rs.getString( 7 ) );
                istat.setString( 8, rs.getString( 8 ) );
                istat.setString( 9, rs.getString( 9 ) );
                istat.setInt( 10, rs.getInt( 10 ) );
                istat.setString( 11, rs.getString( 11 ) );
                istat.setString( 12, rs.getString( 12 ) );
if( DEBUG ) System.err.println( istat );
                istat.executeUpdate();
                nextid++;
            }
            istat.getConnection().commit();
            istat.close();
        }
        rs.close();
        query.close();
        bmrbnames.clear();
    } //************************************************************************
//******************************************************************************
    /** Convert residue to BMRB nomenclature.
     * @param table LoopTable
     * @param refdb reference DB
     * @param nomid nomenclature id
     * @param typeid residue type id
     * @param label residue label
     * @param errs error list
     * @return list of BMRB atom IDs
     */
    public static int [] convertResidueLabel( LoopTable table, RefDB refdb,
    int nomid, int typeid, String label, ErrorList errs ) {
        return convertResidue( table, refdb, nomid, typeid, label, true, errs );
    } //************************************************************************
    /** Convert residue to BMRB nomenclature.
     * @param table LoopTable
     * @param refdb reference DB
     * @param nomid nomenclature id
     * @param typeid residue type id
     * @param code residue code
     * @param errs error list
     * @return list of BMRB atom IDs
     */
    public static int [] convertResidueCode( LoopTable table, RefDB refdb,
    int nomid, int typeid, String code, ErrorList errs ) {
        return convertResidue( table, refdb, nomid, typeid, code, false, errs );
    } //************************************************************************
    /** Convert residue to BMRB nomenclature.
     * @param table LoopTable
     * @param refdb reference DB
     * @param nomid nomenclature id
     * @param typeid residue type id
     * @param residue residue label or code
     * @param islabel if true, residue paramemter is residue label, else residue code
     * @param errs error list
     * @return list of BMRB atom IDs
     */
    public static int [] convertResidue( LoopTable table, RefDB refdb, int nomid,
    int typeid, String residue, boolean islabel, ErrorList errs ) {
        try {
            int bmrbnomid = refdb.getNomenclatureId( "BMRB" );
            StringBuffer sql = new StringBuffer( "SELECT" );
            if( nomid == bmrbnomid ) sql.append( " r.ID FROM RESIDUES r" );
            else sql.append( " n.BMRBID FROM RESIDUES r,RESNOM n" );
            sql.append( " WHERE r.TYPEID=" );
            sql.append( typeid );
            sql.append( " AND r.NOMID=" );
            sql.append( nomid );
            if( islabel ) sql.append( " AND r.LABEL='" );
            else sql.append( " AND r.CODE='" );
            sql.append( residue );
            sql.append( '\'' );
            if( nomid != bmrbnomid ) sql.append( " AND n.RESID=r.ID" );
            java.util.List ids = new java.util.ArrayList();
            java.sql.Statement query = refdb.getConnection().createStatement(
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
}
