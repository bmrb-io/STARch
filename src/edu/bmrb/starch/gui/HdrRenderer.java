package edu.bmrb.starch.gui;

/**
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 12, 2006
 * Time: 5:46:22 PM
 *
 * $Id$
 */

public class HdrRenderer extends javax.swing.JList
        implements javax.swing.table.TableCellRenderer {
    private static final boolean DEBUG = false;
/*******************************************************************************/
    /**
     * Constructor.
     */
    public HdrRenderer() {
        super();
        setOpaque( true );
        setBorder( javax.swing.UIManager.getBorder( "TableHeader.cellBorder" ) );
        javax.swing.ListCellRenderer r = getCellRenderer();
        ((javax.swing.JLabel) r).setHorizontalAlignment( javax.swing.JLabel.CENTER );
        setCellRenderer( r );
    } //*************************************************************************
    /**
     * Returns renderer.
     * @param table parent table
     * @param value header value
     * @param isSelected true if selected
     * @param hasFocus true if has focus
     * @param row row number
     * @param col column number
     * @return renderer
     */
    public java.awt.Component getTableCellRendererComponent(
            javax.swing.JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int col ) {
        String str;
        if( value == null ) str = "";
        else str = value.toString();
        String [] lines = str.split( "\n" );
        setListData( lines );
        setFont( table.getFont() );
        return this;
    } //*************************************************************************
}
