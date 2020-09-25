/*
 * AtomNameEditor.java
 *
 * Created on February 17, 2005, 3:08 PM
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

package EDU.bmrb.starch.gui;

/**
 *
 * @author  dmaziuk
 */
public class AtomNameEditor extends javax.swing.DefaultCellEditor {
    /** parent form */
    private Main fForm;
//******************************************************************************
    /** Creates a new instance of AtomNameEditor.
     * @param form parent form
     */
    public AtomNameEditor( Main form ) {
        super( new javax.swing.JTextField() );
        fForm = form;
    } //************************************************************************
    public boolean stopCellEditing() {
        String val = ((javax.swing.JTextField) getComponent()).getText();
        javax.swing.JTable grid = fForm.getGrid();
        String atom = (String) grid.getValueAt( grid.getEditingRow(), 11 );
        String label = (String) grid.getValueAt( grid.getEditingRow(), 5 );
// FIXME: exception
        int seqno = Integer.parseInt((String) grid.getValueAt( grid.getEditingRow(), 4 ));
        StringBuffer buf = new StringBuffer( "Replace " );
        buf.append( atom );
        buf.append( " with " );
        buf.append( val );
        buf.append( " in all " );
        buf.append( label );
        buf.append( " residues?" );
        int rc = javax.swing.JOptionPane.showConfirmDialog( fForm, buf.toString(),
        "Confirm editing", javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
        javax.swing.JOptionPane.QUESTION_MESSAGE );
        Actions a = fForm.getActions();
        EDU.bmrb.starch.LoopTable table = fForm.getTable();
        switch( rc ) {
            case javax.swing.JOptionPane.CANCEL_OPTION:
                return super.stopCellEditing();
            case javax.swing.JOptionPane.YES_OPTION :
                int [] seq = EDU.bmrb.starch.TableUtils.getSequenceNumbers( table, 
                fForm.getRefdb(), label, atom );
                if( seq == null ) return  super.stopCellEditing();
                for( int i = 0; i < seq.length; i++ )
                    a.convertAtom( seq[i], label, atom, val );
                break;
            default :
                a.convertAtom( seqno, label, atom, val );
        }
/*
        try {
            EDU.bmrb.starch.TableUtils.reindexResidues( table );
            EDU.bmrb.starch.TableUtils.sort( table );
            EDU.bmrb.starch.TableUtils.reindexRows( table );
        }
        catch( java.sql.SQLException e ) {
            System.err.println( e );
            e.printStackTrace();
        }
 */
        table.fireTableDataChanged();
        return super.stopCellEditing();
    } //************************************************************************
}
