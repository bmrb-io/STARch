/*
 * Actions.java
 *
 * Created on December 23, 2004, 1:56 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/gui/Actions.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/10/20 22:20:21 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: Actions.java,v $
 * Revision 1.5  2005/10/20 22:20:21  dmaziuk
 * typo, chmod fix
 *
 * Revision 1.4  2005/06/20 22:29:31  dmaziuk
 * vacation
 *
 * Revision 1.3  2005/05/20 23:07:53  dmaziuk
 * *** empty log message ***
 *
 * Revision 1.2  2005/05/10 18:56:42  dmaziuk
 * added functionality
 *
 * Revision 1.1  2005/04/13 17:37:08  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch.gui;

/**
 * Actions for STARch gui.
 * @author  dmaziuk
 *
 */
public class Actions {
    private static final boolean DEBUG = false;
    /** Loop table */
    private EDU.bmrb.starch.LoopTable fLt = null;
    /** Reference DB */
    private EDU.bmrb.starch.RefDB fRefDb = null;
    /** Main form */
    private Main fForm = null;
    /** Read file action */
    private ReadFile fReadAction = null;
    /** Save file action */
    private SaveFile fSaveAction = null;
//******************************************************************************    
    /** Creates a new instance of Actions.
     * @param form parent form
     * @param table loop table
     * @param refdb reference DB
     */
    public Actions( Main form, EDU.bmrb.starch.LoopTable table,
    EDU.bmrb.starch.RefDB refdb ) {
        fForm = form;
        fLt = table;
        fRefDb = refdb;
    } //************************************************************************
    /** Returns ReadFile action.
     * @return action or null
     */
    public ReadFile getReadFile() {
        return fReadAction;
    } //************************************************************************
    /** Changes ReadFile action.
     * @param action ReadFile action
     */
    public void setReadFile( ReadFile action ) {
        fReadAction = action;
    } //************************************************************************
    /** Returns SaveFile action.
     * @return action or null
     */
    public SaveFile getSaveFile() {
        return fSaveAction;
    } //************************************************************************
    /** Changes SaveFile action.
     * @param action SaveFile action
     */
    public void setSaveFile( SaveFile action ) {
        fSaveAction = action;
    } //************************************************************************
    /** Deletes column 
     * @param col column index
     */
    public void clearColumn( int col ) {
        try {
            int rc = javax.swing.JOptionPane.showConfirmDialog( fForm,
            "Clear column?", "Confirm deletion", javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE );
            if( rc == javax.swing.JOptionPane.YES_OPTION ) {
                EDU.bmrb.starch.TableUtils.clearColumn( fLt, EDU.bmrb.starch.LoopTable.DB_COLS[col] );
            }
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Inserts all defaults */
    public void insertDefaultValues() {
        try {
            addDefaultIsotopes();
            EDU.bmrb.starch.TableUtils.addEntityId( fLt, 1 );
            EDU.bmrb.starch.TableUtils.replaceAssemblyId( fLt, 1 );
            
            FileChooser chooser = FileChooser.getInstance( fLt, null );
            chooser.initMetadataFileOpen();
            int i = chooser.showOpenDialog( fForm );
            if( i != javax.swing.JFileChooser.APPROVE_OPTION ) {
                EDU.bmrb.starch.TableUtils.addEntryId( fLt, "NEED_ACC_NUM" );
                EDU.bmrb.starch.TableUtils.addListId( fLt, 1 );
            }
            else {
                EDU.bmrb.starch.TableUtils.addMetadata( fLt,
                chooser.getSelectedFile().getAbsolutePath(), fLt.getDataFilename(),
                fForm.getErrorList(), false );
//                fForm.showErrors();
            }
            EDU.bmrb.starch.TableUtils.addSaveframeId( fLt, 1 );
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Edit atom name.
     * @param row row number
     * @param atom new atom name
     */
    public void editAtom( int row, String atom ) {
        int restype = fForm.getResidueTypeId();
        if( restype < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select residue type first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return;
        }
        int nomid = fForm.getNomenclatureId();
        if( nomid < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select nomenclature first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return;
        }
        try {
if( DEBUG ) System.err.println( "Edit atom in row " + row + ": " + atom + "(" + fLt.getById( row + 1, 5 ) + ")" );
            int atomid = fRefDb.getAtomId( nomid, restype, fLt.getById( row + 1, 5 ), atom );
            if( atomid < 0 ) {
                javax.swing.JOptionPane.showMessageDialog( fForm,
                "Unknown atom: " + atom, "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE );
                return;
            }
            if( fRefDb.isPseudoAtom( atomid ) ) {
// expand pseudo atom
if( DEBUG ) System.err.println( "Pseudo atom: " + atomid );
                int [] atoms = fRefDb.expandPseudoAtom( atomid );
                if( atoms == null || atoms.length < 1 ) {
                    fForm.getErrorList().addError( fLt.getById( row + 1, fLt.SEQ_COL ),
                    fLt.getById( row + 1, fLt.LABEL_COL ), fLt.getById( row + 1, fLt.ATOM_COL ),
                    "No data for pseudoatom" );
                    return;
                }
                EDU.bmrb.starch.TableUtils.expandPseudoAtom( fLt, fRefDb, row + 1, atoms );
                EDU.bmrb.starch.TableUtils.addAtomTypes( fLt, fRefDb, restype );
                EDU.bmrb.starch.TableUtils.sort( fLt, fForm.getResidueTypeId() );
                EDU.bmrb.starch.TableUtils.reindexRows( fLt );
                fLt.fireTableDataChanged();
                fForm.showErrors();
            }
            else {
if( DEBUG ) System.err.println( "Change atom: " + atomid );
                fLt.set( row + 1, fLt.ATOM_COL, atom );
                fLt.fireTableCellUpdated( row, fLt.ATOM_COL );
            }
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Convert atom name.
     * this function is called when editing atom name in Atom_ID field.
     * @param seqno residue sequence number (Seq_ID)
     * @param residue residue label (Comp_ID)
     * @param atom original atom name (_Author_atom_code)
     * @param newatom atom name (Atom_ID)
     */
    public void convertAtom( int seqno, String residue, String atom, String newatom ) {
        int restype = fForm.getResidueTypeId();
        if( restype < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select residue type first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return;
        }
        int nomid = fForm.getNomenclatureId();
        if( nomid < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select nomenclature first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return;
        }
        try {
            int resid = fRefDb.getResidueId( nomid, restype, residue );
            if( resid < 0 )
// try BMRB nomenclature
                resid = fRefDb.getResidueId( fRefDb.getNomenclatureId( "BMRB" ), restype, residue );
            if( resid < 0 ) {
                javax.swing.JOptionPane.showMessageDialog( fForm,
                "Unknown residue", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
                return;
            }
//if( DEBUG )    System.err.println( "Convert atom in residue " + residue + ": " + atom + " to " + newatom );
            int [] ids = fForm.getNomenmap().convertAtom( nomid, resid, Integer.toString( seqno ),
            residue, newatom, fForm.getErrorList() );
//if( DEBUG )    System.err.println( "* IDs = " + ids );
            if( ids == null ) {
                javax.swing.JOptionPane.showMessageDialog( fForm,
                "Unknown atom: " + newatom, "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE );
                return;
            }
            EDU.bmrb.starch.TableUtils.replaceAtom( fLt, fRefDb, seqno, atom, ids, true );
            EDU.bmrb.starch.TableUtils.addAtomTypes( fLt, fRefDb, restype );
//            EDU.bmrb.starch.TableUtils.reindexResidues( fLt );
//            EDU.bmrb.starch.TableUtils.sort( fLt );
//            EDU.bmrb.starch.TableUtils.reindexRows( fLt );
        }
        catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Convert selected atoms.
     * @param seqno residue sequence numbers (Seq_ID)
     * @param residues residue labels (Comp_ID)
     * @param atoms atom names (Author_atom_code)
     */
    public void convertSelectedAtoms( int [] seqno, String [] residues, String [] atoms ) {
if( DEBUG ) System.err.println( "convertSelectedAtoms()" );
        int restype = fForm.getResidueTypeId();
        if( restype < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select residue type first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return;
        }
        int nomid = fForm.getNomenclatureId();
        if( nomid < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select nomenclature first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return;
        }
        try {
            int [] ids = null;
            int resid = -1;
            for( int i = 0; i < seqno.length; i++ ) {
if( DEBUG ) System.err.println( "Convert atom in residue " + seqno[i] + ": " + atoms[i] );
                resid = fRefDb.getResidueId( nomid, restype, residues[i] );
                if( resid < 0 )
// try BMRB nomenclature
                    resid = fRefDb.getResidueId( fRefDb.getNomenclatureId( "BMRB" ), restype, residues[i] );
                if( resid < 0 ) {
                    fForm.getErrorList().addError( Integer.toString( seqno[i] ), residues[i], "Unknown residue" );
                    EDU.bmrb.starch.TableUtils.copyResidue( fLt, fRefDb, seqno[i] );
                    continue;
                }
                ids = fForm.getNomenmap().convertAtom( nomid, resid, Integer.toString( seqno[i] ),
                residues[i], atoms[i], fForm.getErrorList() );
                if( ids != null ) EDU.bmrb.starch.TableUtils.replaceAtom( fLt, fRefDb,
                seqno[i], atoms[i], ids, true );
//                seqno[i], atoms[i], ids, false );
                else EDU.bmrb.starch.TableUtils.copyAtom( fLt, fRefDb, seqno[i],  atoms[i] );
            }
            EDU.bmrb.starch.TableUtils.addAtomTypes( fLt, fRefDb, restype );
//            EDU.bmrb.starch.TableUtils.reindexResidues( fLt );
//            EDU.bmrb.starch.TableUtils.sort( fLt );
//            EDU.bmrb.starch.TableUtils.reindexRows( fLt );
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Convert all atoms. */
    public void convertAtoms() {
if( DEBUG ) System.err.println( "convertAtoms()" );
        int restype = fForm.getResidueTypeId();
        if( restype < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select residue type first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return;
        }
        int nomid = fForm.getNomenclatureId();
        if( nomid < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select nomenclature first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return;
        }
        try {
            EDU.bmrb.starch.TableUtils.convertAtoms( fLt, fRefDb, fForm.getNomenmap(),
            nomid, restype, fForm.getErrorList() );
//            EDU.bmrb.starch.TableUtils.reindexResidues( fLt );
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Convert selected residues.
     * @param residues list of residue labels or codes (_Author_comp_code)
     */
    public void convertResidues( String [] residues ) {
        int restype = fForm.getResidueTypeId();
        if( restype < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select residue type first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return;
        }
        int nomid = fForm.getNomenclatureId();
        if( nomid < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select nomenclature first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return;
        }
        int rc = javax.swing.JOptionPane.showConfirmDialog( fForm,
        "Does Author_comp_code column contain 3-letter residue labels?",
        "Residue label mapping", javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
        javax.swing.JOptionPane.QUESTION_MESSAGE );
        if( rc == javax.swing.JOptionPane.CANCEL_OPTION ) return;
        boolean islabel = (rc == javax.swing.JOptionPane.YES_OPTION);
        int [] resids = null;
        try {
            for( int i = 0; i < residues.length; i++ ) {
                resids = fForm.getNomenmap().convertResidue( nomid, restype,
                residues[i], islabel,fForm.getErrorList() );
                if( resids == null ) {
                    fForm.getErrorList().addError( "Unknown residue: " + residues[i] );
                    continue;
                }
// FIXME: this assumes there's only one ID in resids
                EDU.bmrb.starch.TableUtils.replaceResidue( fLt, fRefDb, residues[i], 
                resids[0] );
            }
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Convert all residues.
     * @param residues list of residue labels or codes (_Author_comp_code)
     */
    public void convertResidues() {
        int restype = fForm.getResidueTypeId();
        if( restype < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select residue type first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return;
        }
        int nomid = fForm.getNomenclatureId();
        if( nomid < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select nomenclature first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return;
        }
        int rc = javax.swing.JOptionPane.showConfirmDialog( fForm,
        "Does Author_comp_code column contain 3-letter residue labels?",
        "Residue label mapping", javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
        javax.swing.JOptionPane.QUESTION_MESSAGE );
        if( rc == javax.swing.JOptionPane.CANCEL_OPTION ) return;
        boolean islabel = (rc == javax.swing.JOptionPane.YES_OPTION);
        try {
            EDU.bmrb.starch.TableUtils.convertResidues( fLt, fRefDb, fForm.getNomenmap(),
            nomid, restype, islabel, fForm.getErrorList() );
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Convert all author data.
     */
    public boolean convertData() {
        int restype = fForm.getResidueTypeId();
        if( restype < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select residue type first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return false;
        }
        int nomid = fForm.getNomenclatureId();
        if( nomid < 0 ) {
            javax.swing.JOptionPane.showMessageDialog( fForm,
            "Select nomenclature first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
            return false;
        }
        java.awt.Cursor curs = fForm.getCursor();
        try {
            fForm.setCursor( new java.awt.Cursor( java.awt.Cursor.WAIT_CURSOR ) );
            EDU.bmrb.starch.TableUtils.deleteEmptyRows( fLt );
            EDU.bmrb.starch.TableUtils.reindexRows( fLt );
            if( ! checkAuthorData() ) return false;
            EDU.bmrb.starch.TableUtils.reindexRows( fLt );
            convertResidues();
            EDU.bmrb.starch.TableUtils.reindexResidues( fLt );
            EDU.bmrb.starch.TableUtils.convertAtoms( fLt, fRefDb, fForm.getNomenmap(),
            nomid, restype, fForm.getErrorList() );
//            EDU.bmrb.starch.TableUtils.reindexResidues( fLt );
            EDU.bmrb.starch.TableUtils.fixMethylenes( fLt, fRefDb, fForm.getErrorList() );
            EDU.bmrb.starch.TableUtils.addAtomTypes( fLt, fRefDb, restype );
            EDU.bmrb.starch.TableUtils.sort( fLt, fForm.getResidueTypeId() );
            EDU.bmrb.starch.TableUtils.reindexRows( fLt );
            EDU.bmrb.starch.TableUtils.convertAmbicodes( fLt, fRefDb, fForm.getErrorList() );
//
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
        finally {
            fForm.setCursor( curs );
        }
        return true;
    } //************************************************************************
    /** Add atom types */
    public void addAtomTypes() {
        try {
            int restype = fForm.getResidueTypeId();
            if( restype < 0 ) {
                javax.swing.JOptionPane.showMessageDialog( fForm,
                "Select residue type first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
                return;
            }
            EDU.bmrb.starch.TableUtils.addAtomTypes( fLt, fRefDb, restype );
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Add atom isotopes */
    public void addAtomIsotopes() {
        try {
            IsotopeSelector isl = new IsotopeSelector( fRefDb );
            int rc = javax.swing.JOptionPane.showConfirmDialog( fForm, isl,
            "Insert atom isotopes", javax.swing.JOptionPane.OK_CANCEL_OPTION );
            if( rc != javax.swing.JOptionPane.OK_OPTION ) return;
            javax.swing.JCheckBox box;
            String iso, type;
            for( int i = 0; i < isl.getComponentCount(); i++ ) {
                if( isl.getComponent( i ) instanceof javax.swing.JCheckBox ) {
                    box = (javax.swing.JCheckBox) isl.getComponent( i );
                    if( box.isSelected() ) {
                        iso = box.getText().split( "\\p{Alpha}" )[0];
                        type = box.getText().substring( box.getText().indexOf( iso ) + iso.length() );
                        EDU.bmrb.starch.TableUtils.addIsotopes( fLt, type, iso );
                    }
                }
            }
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Add default atom isotopes */
    public void addDefaultIsotopes() {
        try {
            IsotopeSelector isl = new IsotopeSelector( fRefDb );
            javax.swing.JCheckBox box;
            String iso, type;
            for( int i = 0; i < isl.getComponentCount(); i++ ) {
                if( isl.getComponent( i ) instanceof javax.swing.JCheckBox ) {
                    box = (javax.swing.JCheckBox) isl.getComponent( i );
                    if( box.isSelected() ) {
                        iso = box.getText().split( "\\p{Alpha}" )[0];
                        type = box.getText().substring( box.getText().indexOf( iso ) + iso.length() );
                        EDU.bmrb.starch.TableUtils.addIsotopes( fLt, type, iso );
                    }
                }
            }
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Add shift errors */
    public void addShiftErrors() {
        ErrorSelector esl = new ErrorSelector( fRefDb );
        int rc = javax.swing.JOptionPane.showConfirmDialog( fForm, esl,
        "Insert shift errors", javax.swing.JOptionPane.OK_CANCEL_OPTION );
        if( rc != javax.swing.JOptionPane.OK_OPTION ) return;
        String val = esl.getValue();
        if( val == null || val.trim().length() < 1 ) return;
        String iso, type;
        iso = esl.getAtom().split( "\\p{Alpha}" )[0];
        type = esl.getAtom().substring( esl.getAtom().indexOf( iso ) + iso.length() );
        int isotope = -1;
        float tmp;
        try { isotope = Integer.parseInt( iso ); }
        catch( NumberFormatException e ) {
            System.err.println( "Not a number: " + iso );
            System.err.println( e );
            e.printStackTrace();
            return;
        }
        try { tmp = Float.parseFloat( val ); }
        catch( NumberFormatException ne ) {
            System.err.println( "Not a number: " + val );
            System.err.println( ne );
            ne.printStackTrace();
            return;
        }
        try {
            EDU.bmrb.starch.TableUtils.addShiftError( fLt, type, isotope, val );
        }
        catch( java.sql.SQLException se ) {
            System.err.println( se );
            se.printStackTrace();
        }
    } //************************************************************************
    /** Add accession number */
    public void addAccessionNumber() {
        try {
            String eid = (String) javax.swing.JOptionPane.showInputDialog( fForm,
            "BMRB accession number: ", "Insert entry ID",
            javax.swing.JOptionPane.QUESTION_MESSAGE, null, null, "NEED_ACC_NUM" );
            if( eid != null && eid.trim().length() > 0 ) {
                EDU.bmrb.starch.TableUtils.addEntryId( fLt, eid );
            }
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Add entity ID */
    public void addEntityID() {
        try {
            int id = -1;
            String eid = (String) javax.swing.JOptionPane.showInputDialog( fForm,
            "Entity ID (value of corresp. _Entity.ID tag): ", "Insert entity ID",
            javax.swing.JOptionPane.QUESTION_MESSAGE, null, null, "1" );
            if( eid != null && eid.trim().length() > 0 ) {
                try { id = Integer.parseInt( eid ); }
                catch( NumberFormatException ne ) {
                    javax.swing.JOptionPane.showMessageDialog( fForm, "Not a number: "
                    + eid, "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
                }
                if( id >= 0 ) EDU.bmrb.starch.TableUtils.replaceEntityId( fLt, id );
            }
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Add entity assembly ID */
    public void addEntityAssemblyID() {
        try {
            int id = -1;
            String eid = (String) javax.swing.JOptionPane.showInputDialog( fForm,
            "Entity assembly ID (value of corresp. _Entity_assembly.ID tag): ",
            "Insert entity assembly ID",
            javax.swing.JOptionPane.QUESTION_MESSAGE, null, null, "1" );
            if( eid != null && eid.trim().length() > 0 ) {
                try { id = Integer.parseInt( eid ); }
                catch( NumberFormatException ne ) {
                    javax.swing.JOptionPane.showMessageDialog( fForm, "Not a number: "
                    + eid, "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
                }
                if( id >= 0 ) EDU.bmrb.starch.TableUtils.replaceAssemblyId( fLt, id );
            }
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Add shift list ID */
    public void addListID() {
        try {
            String lid = (String) javax.swing.JOptionPane.showInputDialog( fForm,
            "CS list ID (value of _Assigned_chem_shift_list.ID tag): ",
            "Insert list ID", javax.swing.JOptionPane.QUESTION_MESSAGE, null,
            null, "1" );
            if( lid != null && lid.trim().length() > 0 ) {
                try {
                    int id = Integer.parseInt( lid );
                    EDU.bmrb.starch.TableUtils.replaceListId( fLt, id );
                    fLt.fireTableRowsUpdated( 0, fLt.getRowCount() );
                }
                catch( NumberFormatException ne ) {
                    javax.swing.JOptionPane.showMessageDialog( fForm, "Not a number: "
                    + lid, "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
                }
            }
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Adds saveframe IDs */
    public void addSaveframeID() {
        try {
            String sfid = (String) javax.swing.JOptionPane.showInputDialog( fForm,
            "Saveframe ID (value of _Assigned_chemical_shifts.Sf_ID tag): ",
            "Insert saveframe ID", javax.swing.JOptionPane.QUESTION_MESSAGE );
            if( sfid != null && sfid.trim().length() > 0 ) {
                try {
                    int id = Integer.parseInt( sfid );
                    EDU.bmrb.starch.TableUtils.replaceSaveframeId( fLt, id );
                    fLt.fireTableRowsUpdated( 0, fLt.getRowCount() );
                }
                catch( NumberFormatException ne ) {
                    javax.swing.JOptionPane.showMessageDialog( fForm, "Not a number: "
                    + sfid, "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
                }
            }
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Fixes methylene protons */
    public void fixMethylenes() {
        try {
            int restype = fForm.getResidueTypeId();
            if( restype < 0 ) {
                javax.swing.JOptionPane.showMessageDialog( fForm,
                "Select residue type first", "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
                return;
            }
            EDU.bmrb.starch.TableUtils.fixMethylenes( fLt, fRefDb, fForm.getErrorList() );
            EDU.bmrb.starch.TableUtils.addAtomTypes( fLt, fRefDb, restype );
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Reindex residues */
    public void reindexResidues() {
        try {
            int seq = -1;
            String str = (String) javax.swing.JOptionPane.showInputDialog( fForm,
            "Starting sequence number: ", "Renumber residues",
            javax.swing.JOptionPane.QUESTION_MESSAGE, null, null, "1" );
            if( str != null && str.trim().length() > 0 ) {
                try { seq = Integer.parseInt( str ); }
                catch( NumberFormatException ne ) {
                    javax.swing.JOptionPane.showMessageDialog( fForm, "Not a number: "
                    + str, "Error", javax.swing.JOptionPane.ERROR_MESSAGE );
                }
                if( seq >= 0 ) EDU.bmrb.starch.TableUtils.reindexResidues( fLt, seq );
            }
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Check for duplicate atoms */
    public void checkDuplicates() {
        try {
            EDU.bmrb.starch.TableUtils.checkDuplicateAtoms( fLt, fForm.getErrorList() );
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Check for invalid atoms */
    public void checkAtoms() {
        try {
            EDU.bmrb.starch.TableUtils.checkInvalidAtoms( fForm.getResidueTypeId(),
            fLt, fRefDb, fForm.getErrorList() );
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
    /** Check author data for invalid atoms.
     * @return false if check fails
     */
    public boolean checkAuthorData() {
        try {
            return EDU.bmrb.starch.TableUtils.checkAuthorData( fForm.getNomenclatureId(),
            fForm.getResidueTypeId(), fLt, fRefDb, fForm.getErrorList() );
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
            return false;
        }
    } //************************************************************************
}
