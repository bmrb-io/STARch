/*
 * Copyright (c) 2006 Board of Regents University of Wisconsin.
 * All Rights Reserved.
 */

package edu.bmrb.starch.gui;

/**
 * Isotope selector form.
 *
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 22, 2006
 * Time: 2:33:57 PM
 *
 * $Id$
 */

public class IsotopeSelector extends javax.swing.JPanel {
    private static final boolean DEBUG = false;
    /** Dictionary. */
    private edu.bmrb.starch.Dictionary fDict = null;
    /** Instance -- no need for more than one. */
    private static IsotopeSelector fInstance = null;
    public static IsotopeSelector createSelector( edu.bmrb.starch.Dictionary dict ) {
        if( fInstance == null ) fInstance = new IsotopeSelector( dict );
        return fInstance;
    } //*************************************************************************
    private IsotopeSelector( edu.bmrb.starch.Dictionary dict ) {
        super();
        fDict = dict;
        init();
    } //*************************************************************************
    private void init() {
        try {
            edu.bmrb.starch.StringStringPair [] isotopes = fDict.getAtomIsotopes();
            this.setLayout( new java.awt.GridBagLayout() );
            if( isotopes == null ) {
                add( new javax.swing.JLabel( "No isotopes in dictionary" ) );
                return;
            }
            String [] types = getTypes( isotopes );
            javax.swing.ButtonGroup grp = null;
            javax.swing.JCheckBox box;
            java.awt.GridBagConstraints constr;
            java.util.ArrayList<String> nums;
            int col;
            StringBuilder buf = new StringBuilder();
            for( int row = 0; row < types.length; row++ ) {
                col = 0;
                nums = getIsotopes( types[row], isotopes );
                buf.setLength( 0 );
                buf.append( nums.get( 0 ) );
                buf.append( types[row] );
                box = new javax.swing.JCheckBox( buf.toString() );
if( DEBUG ) System.err.printf( "Type |%s|, row %d, col 0, isotope |%s|\n", types[row], row, nums.get( 0 ) );
// defaults
// ugh. this is for col 0.
                if( types[row].equals( "H" ) && nums.get( 0 ).equals( "1" ) )
                    box.setSelected( true );
                if( types[row].equals( "N" ) && nums.get( 0 ).equals( "15" ) )
                    box.setSelected( true );
                if( types[row].equals( "Cd" ) && nums.get( 0 ).equals( "113" ) )
                    box.setSelected( true );
                constr = new java.awt.GridBagConstraints();
                constr.gridx = col;
                constr.gridy = row;
                constr.anchor = java.awt.GridBagConstraints.WEST;
                add( box, constr );
                if( nums.size() > 1 ) {
                    grp = new javax.swing.ButtonGroup();
                    grp.add( box );
                    for( col = 1; col < nums.size(); col++ ) {
                        buf.setLength( 0 );
                        buf.append( nums.get( col ) );
                        buf.append( types[row] );
if( DEBUG ) System.err.printf( "Type |%s|, row %d, col %d, isotope |%s|\n", types[row], row, col, nums.get( col ) );
// and this is for col 1..n
                        box = new javax.swing.JCheckBox( buf.toString() );
                        if( types[row].equals( "H" ) && nums.get( col ).equals( "1" ) )
                            box.setSelected( true );
                        if( types[row].equals( "N" ) && nums.get( col ).equals( "15" ) )
                            box.setSelected( true );
                        if( types[row].equals( "Cd" ) && nums.get( col ).equals( "113" ) )
                            box.setSelected( true );
                        grp.add( box );
                        constr = new java.awt.GridBagConstraints();
                        constr.gridx = col;
                        constr.gridy = row;
                        constr.anchor = java.awt.GridBagConstraints.WEST;
                        add( box, constr );
                    }
                }
                else { // only one isotope, selected
                    box.setSelected( true );
                }
            } // endfor row
            setBorder( javax.swing.BorderFactory.createTitledBorder(
                    javax.swing.BorderFactory.createEtchedBorder(
                            javax.swing.border.EtchedBorder.LOWERED ), "select atom isotopes" ) );
        }
        catch( java.sql.SQLException e ) {
            System.err.println( "Cannot initialize isotope selector" );
            System.err.println( e );
            System.err.print( "SQL State: " );
            System.err.println( e.getSQLState() );
            e.printStackTrace();
        }
    } //*************************************************************************
    private String [] getTypes( edu.bmrb.starch.StringStringPair [] isotopes ) {
        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        boolean found;
        for( edu.bmrb.starch.StringStringPair p : isotopes ) {
            found = false;
            for( String s : list ) {
                if( s.equals( p.getFirst() ) ) {
                    found = true;
                    break;
                }
            }
            if( ! found ) list.add( p.getFirst() );
        }
        String [] rc = new String[list.size()];
        for( int i = 0; i < list.size(); i++ ) rc[i] = list.get( i );
        list.clear();
        return rc;
    } //*************************************************************************
    private java.util.ArrayList<String> getIsotopes( String type,
                                                     edu.bmrb.starch.StringStringPair [] isotopes ) {
        java.util.ArrayList<String> rc = new java.util.ArrayList<String>();
        for( edu.bmrb.starch.StringStringPair p : isotopes ) {
            if( p.getFirst().equals( type ) ) rc.add( p.getSecond() );
        }
        return rc;
    } //*************************************************************************
}
