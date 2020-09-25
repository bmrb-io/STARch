package edu.bmrb.starch.gui;

/**
 * Cell renderer class.
 * The renderer stores column type, group number, and mandatory status. These are
 * fetched from the database by static factory constructor. Type and mandatoriness
 * are used to colour the cells: red for missing mandatory values, yellow where
 * author-provided value does not match BMRB one (after nomenclature conversion).
 * Group numbers are used in tables that contain more than one
 * {sequence,residue,atom} tuples, e.g. coupling constants.
 *
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 1, 2006
 * Time: 5:24:19 PM
 *
 * $Id$
 */
import edu.bmrb.starch.*;

public class CellRenderer extends javax.swing.table.DefaultTableCellRenderer {
    private static final boolean DEBUG = false;
    /** table model. */
    private edu.bmrb.starch.TableModel fModel = null;
/*******************************************************************************/
    /**
     * Constructor.
     * @param model table model
     */
    public CellRenderer( edu.bmrb.starch.TableModel model ) {
        super();
        fModel = model;
        setOpaque( true );
    } //*************************************************************************
    /**
     * Renders the cell.
     * @param table the table
     * @param value cell value
     * @param isSelected true if cell is selected
     * @param hasFocus true if cell has focus
     * @param row table row
     * @param col table column
     * @return this
     */
    public java.awt.Component getTableCellRendererComponent( javax.swing.JTable table,
                                                             Object value,
                                                             boolean isSelected,
                                                             boolean hasFocus,
                                                             int row, int col ) {
        String txt;
        if( value != null ) txt = value.toString();
        else txt = "";
        setFont( table.getFont() );
        java.util.List<edu.bmrb.starch.Column> cols = fModel.getColumns();
        edu.bmrb.starch.Column thiscol = cols.get( table.getColumnModel().getColumn( col ).getModelIndex() );
        if( isSelected ) setBackground( table.getSelectionBackground() );
        else setBackground( table.getBackground() );
        if( thiscol.isRequired() && (txt.trim().equals( "" ) || txt.trim().equals( "." )
                    || txt.trim().equals( "?" )) )
            setBackground( java.awt.Color.RED ); // missing required values
        else { // author and BMRB values don't match
            javax.swing.table.TableColumn tc;
            edu.bmrb.starch.Column mc;
            for( int i = 0; i < table.getColumnCount(); i++ ) {
                if( i == col ) continue;
                tc = table.getColumn( table.getColumnName( i ) );
                mc = cols.get( tc.getModelIndex() );
                if( ((thiscol.getType() == ColTypes.SEQID) && (mc.getType() == ColTypes.ASEQID))
                        || ((thiscol.getType() == ColTypes.ASEQID) && (mc.getType() == ColTypes.SEQID))
                        || ((thiscol.getType() == ColTypes.COMPID) && (mc.getType() == ColTypes.ACOMPID))
                        || ((thiscol.getType() == ColTypes.ACOMPID) && (mc.getType() == ColTypes.COMPID))
                        || ((thiscol.getType() == ColTypes.ATOMID) && (mc.getType() == ColTypes.AATOMID))
                        || ((thiscol.getType() == ColTypes.AATOMID) && (mc.getType() == ColTypes.ATOMID)) ) {
                    if( thiscol.getGroupId() == mc.getGroupId() ) {
                        if( table.getValueAt( row, i ) != null ) {
                            String val = table.getValueAt( row, i ).toString();
                            if( txt.trim().equals( val ) ) setBackground( table.getBackground() );
                            else setBackground( java.awt.Color.YELLOW );
                        }
                        else setBackground( java.awt.Color.YELLOW );
                        break;
                    } // endif group ID
                } // endif col. type
            } //endfor
        }
        setText( txt.trim() );
        return this;
    } //*************************************************************************
}
