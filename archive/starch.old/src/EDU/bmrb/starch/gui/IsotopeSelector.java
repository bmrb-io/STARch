/*
 * IsotopeSelector.java
 *
 * Created on December 23, 2004, 5:33 PM
 *
 * This software is copyright (c) 2004 Board of Regents, University of Wisconsin.
 * All Rights Reserved.
 *
 * FILE:        $Source: /cvs_archive/cvs/starch/src/EDU/bmrb/starch/gui/IsotopeSelector.java,v $
 * 
 * AUTHOR:      $Author: dmaziuk $
 * DATE:        $Date: 2005/04/13 17:37:08 $
 * 
 * UPDATE HISTORY:
 * ---------------
 * $Log: IsotopeSelector.java,v $
 * Revision 1.1  2005/04/13 17:37:08  dmaziuk
 * *** empty log message ***
 * */

package EDU.bmrb.starch.gui;

/**
 * Isotope selector widget
 *
 * @author  dmaziuk
 */
public class IsotopeSelector extends javax.swing.JPanel {
    private final static boolean DEBUG = false;
    /** ref. db */
    private EDU.bmrb.starch.RefDB fRefDb = null;
//******************************************************************************
    /** Creates new form IsotopeSelector.
     * @param refdb ref. DB
     */
    public IsotopeSelector( EDU.bmrb.starch.RefDB refdb ) {
        fRefDb = refdb;
        init();
    } //************************************************************************
    /** Initializes the form */
    private void init() {
        try {
            this.setLayout( new java.awt.GridBagLayout() );
            String [] types = fRefDb.getAtomTypes();
            if( types == null ) {
                this.add( new javax.swing.JLabel( "No isotopes in ref. database!" ) );
                return;
            }
            javax.swing.ButtonGroup grp = null;
            javax.swing.JCheckBox box = null;
            java.awt.GridBagConstraints constr = null;
            int col;
            int [] isotopes = null;
            StringBuffer buf = new StringBuffer();
            for( int row = 0; row < types.length; row++ ) {
                col = 0;
                isotopes = fRefDb.getAtomIsotopes( types[row] );
                buf.setLength( 0 );
                buf.append( isotopes[col] );
                buf.append( types[row] );
                box = new javax.swing.JCheckBox( buf.toString() );
if( DEBUG ) System.err.println( "type: |" + types[row] +"|, isotope: " + isotopes[col] );
// defaults
                        if( types[row].equals( "H" ) && isotopes[col] == 1 )
                            box.setSelected( true );
                        if( types[row].equals( "N" ) && isotopes[col] == 15 )
                            box.setSelected( true );
                        if( types[row].equals( "Cd" ) && isotopes[col] == 113 )
                            box.setSelected( true );
                constr = new java.awt.GridBagConstraints();
                constr.gridy = row;
                constr.gridx = col;
                constr.anchor = java.awt.GridBagConstraints.WEST;
                this.add( box, constr );
                if( isotopes.length > 1 ) {
                    if( col == 0 ) grp = new javax.swing.ButtonGroup();
                    grp.add( box );
                    for( col = 1; col < isotopes.length; col++ ) {
                        buf.setLength( 0 );
                        buf.append( isotopes[col] );
                        buf.append( types[row] );
                        box = new javax.swing.JCheckBox( buf.toString() );
if( DEBUG ) System.err.println( "type: |" + types[row] +"|, isotope: " + isotopes[col] );
// defaults
                        if( types[row].equals( "H" ) && isotopes[col] == 1 )
                            box.setSelected( true );
                        if( types[row].equals( "N" ) && isotopes[col] == 15 )
                            box.setSelected( true );
                        if( types[row].equals( "Cd" ) && isotopes[col] == 113 )
                            box.setSelected( true );
                        grp.add( box );
                        constr = new java.awt.GridBagConstraints();
                        constr.gridy = row;
                        constr.gridx = col;
                        constr.anchor = java.awt.GridBagConstraints.WEST;
                        this.add( box, constr );
                    }
                }
                else box.setSelected( true );
            } // endfor row
            this.setBorder( javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createEtchedBorder( javax.swing.border.EtchedBorder.LOWERED ),
            "select atom isotopes to insert" ) );
        }
        catch( java.sql.SQLException e ) {
            System.err.println( "Cannot initialize isotope selector" );
            System.err.println( e );
            e.printStackTrace();
        }
    } //************************************************************************
}
