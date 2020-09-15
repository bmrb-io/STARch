/*
 * ErrorSelector.java
 *
 * Created on December 30, 2004, 5:07 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
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

package EDU.bmrb.starch.gui;

/**
 * Panel for selecting chemical shift error.
 * @author  dmaziuk
 */
public class ErrorSelector extends javax.swing.JPanel {
    /** isotopes */
    // this list is from NMR-STAR 3.0 dictionary tags:
    // _Assigned_chem_shift_list.Chem_shift_XXX_err
    public static final String [] ATOMS = { "all", "1H", "2H", "13C", "15N", "19F", "31P" };
    /** ref. db */
    private EDU.bmrb.starch.RefDB fRefDb = null;
    /** atom selector */
    private javax.swing.JComboBox fAtomBox = null;
    /** value */
    private javax.swing.JTextField fErrBox = null;
//******************************************************************************
    /** Creates a new instance of ErrorSelector.
     * @param refdb ref. DB
     */
    public ErrorSelector( EDU.bmrb.starch.RefDB refdb ) {
        fRefDb = refdb;
        this.setLayout( new javax.swing.BoxLayout( this, javax.swing.BoxLayout.Y_AXIS ) );
        this.add( new javax.swing.JLabel( "Atom type/isotope:" ) );
        try { fAtomBox = new javax.swing.JComboBox( fRefDb.getAtomIsotopes() ); }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
            fAtomBox = new javax.swing.JComboBox( ATOMS );
        }
        this.add( fAtomBox );
        this.add( new javax.swing.JLabel( "Chemical shift value error:" ) );
        fErrBox = new javax.swing.JTextField();
        this.add( fErrBox );
    } //************************************************************************
    /** returns selected atom.
     * @return selected atom, one of <CODE>ATOMS</CODE>
     */
    public String getAtom() {
        return (String) fAtomBox.getSelectedItem();
    } //************************************************************************
    /** returns value.
     * @return value or null
     */
    public String getValue() {
        if( fErrBox.getText().trim().length() < 1 ) return null;
        else return fErrBox.getText().trim();
    } //************************************************************************
}
