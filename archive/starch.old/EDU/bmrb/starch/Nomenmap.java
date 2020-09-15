/*
 * Nomenmap.java
 *
 * Created on December 27, 2002, 1:11 PM
 *
 * This software is copyright (c) 2002 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/EDU/bmrb/starch/Nomenmap.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2004/10/18 18:44:13 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Nomenmap.java,v $
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
public class Nomenmap {
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
    public Nomenmap( EDU.bmrb.lib.StringPool pool ) {
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
}
