/*
 * Copyright (c) 2007 Board of Regents University of Wisconsin.
 * All Rights Reserved.
 */

package edu.bmrb.starch.gui;

/**
 * Cell editor for atom names.
 *
 * Expands pseudo-atom, if necessry.
 *
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Apr 12, 2007
 * Time: 2:00:07 PM
 *
 * $Id$
 */

public class AtomNameEditor extends javax.swing.DefaultCellEditor {
    private static final boolean DEBUG = false;
    /** parent gui form. */
    private MainFrame fForm;
/*******************************************************************************/
    /** Creates new editor.
     * @param form parent gui form
     */
    public AtomNameEditor( MainFrame form ) {
        super( new javax.swing.JTextField() );
        fForm = form;
    } //*************************************************************************
    /** Returns true
     * @param eventObject event source
     * @return true
     */
    public boolean isCellEditable( java.util.EventObject eventObject ) {
        return ((edu.bmrb.starch.TableModel) fForm.getGrid().getModel()).isConverted();
    } //*************************************************************************
    public boolean stopCellEditing() {
if( DEBUG ) System.err.println( "stopCellEditing()" );
        String val = ((javax.swing.JTextField) getComponent()).getText();
        javax.swing.JTable grid = fForm.getGrid();
        edu.bmrb.starch.TableModel mdl = (edu.bmrb.starch.TableModel) grid.getModel();
        edu.bmrb.starch.Column c = mdl.getColumn( grid.getEditingColumn() );
        int group = c.getGroupId();
if( DEBUG ) System.err.printf( "Editing col %d, %s, group %d\n", grid.getEditingColumn(), c.getLabel(), group );
        String atom = (String) grid.getValueAt( grid.getEditingRow(), mdl.getAtomColIdx( group ) );
        String label = (String) grid.getValueAt( grid.getEditingRow(), mdl.getCompColIdx( group ) );
        int seqno = ((Integer) grid.getValueAt( grid.getEditingRow(), mdl.getSeqColIdx( group ) )).intValue();
if( DEBUG ) System.err.printf( "seq %d, label %s, atom %s, newval %s, row %s\n", seqno, label, atom, val, grid.getValueAt( grid.getEditingRow(), 0 ) );
        if( val.equals( atom ) )
            return super.stopCellEditing();
        boolean change_all;
        StringBuilder buf = new StringBuilder( "Replace " );
        buf.append( atom );
        buf.append( " with " );
        buf.append( val );
        buf.append( " in all " );
        buf.append( label );
        buf.append( " residues?" );
        int rc = javax.swing.JOptionPane.showConfirmDialog( fForm, buf.toString(),
                                                            "Confirm editing",
                                                            javax.swing.JOptionPane.YES_NO_CANCEL_OPTION );
        switch( rc ) {
            case javax.swing.JOptionPane.YES_OPTION :
                change_all = true;
                break;
//            default :
            case javax.swing.JOptionPane.CANCEL_OPTION :
                ((javax.swing.JTextField) getComponent()).setText( atom );
                return super.stopCellEditing();
            default :
                change_all = false;
        }
        try {
            editAtom( group, Integer.toString( seqno ), label, atom, val, change_all );
            fForm.getNomenmap().sort();
            fForm.getNomenmap().reindexRows(); // also fires updateTable()
        }
        catch( java.sql.SQLException e ) {
            fForm.getErrorList().add( new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.CRIT,
                                                                 0, Integer.toString( seqno ), label, atom,
                                                                 "DB exception" ) );
            ((javax.swing.JTextField) getComponent()).setText( atom );
            System.err.println( e );
            e.printStackTrace();
        }
        return super.stopCellEditing();
    } //*************************************************************************
    private void editAtom( int group, String seq, String label, String atom,
                           String newval, boolean change_all ) throws java.sql.SQLException {
//todo: expand pseudo atom
//edit row/add rows
//renumber rows
        edu.bmrb.starch.TableModel mdl = (edu.bmrb.starch.TableModel) fForm.getGrid().getModel();
        edu.bmrb.starch.IntStringPair [] atoms = mdl.getDictionary().getAtoms( mdl.getNomenclatureId(),
                                                                               mdl.getRestypeId( group ),
                                                                               label,
                                                                               newval );
        if( (atoms == null) || (atoms.length == 0) ) {
            mdl.getErrorList().add(  new edu.bmrb.starch.Error( edu.bmrb.starch.Error.Severity.ERR, -1,
                                                   seq, label, atom, "Unknown atom" ) );
            return;
        }
        String eidcol = null;
        String seqcol = null;
        String labelcol = null;
        String atomcol = null;
        for( edu.bmrb.starch.Column c : mdl.getColumns() ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == edu.bmrb.starch.ColTypes.ENTITYID ) eidcol = c.getDbName();
                if( c.getType() == edu.bmrb.starch.ColTypes.COMPIDX ) seqcol = c.getDbName();
                if( c.getType() == edu.bmrb.starch.ColTypes.COMPID ) labelcol = c.getDbName();
                if( c.getType() == edu.bmrb.starch.ColTypes.ATOMID ) atomcol = c.getDbName();
            }
        }
        int eid = mdl.getEntityId( group );
        StringBuilder sql = new StringBuilder();
        sql.append( "UPDATE ");
        sql.append( mdl.getName() );
        sql.append( " SET \"" );
        sql.append( atomcol );
        sql.append( "\"='" );
        sql.append( edu.bmrb.starch.Utils.quoteForDB( atoms[0].getString() ) );
        sql.append( "' WHERE \"");
        sql.append( eidcol );
        sql.append( "\"=" );
        sql.append( eid );
        sql.append( " AND \"" );
        sql.append( atomcol );
        sql.append( "\"='" );
        sql.append( edu.bmrb.starch.Utils.quoteForDB( atom ) );
        sql.append( "' AND \"" );
        sql.append( labelcol );
        sql.append( "\"='" );
        sql.append( edu.bmrb.starch.Utils.quoteForDB( label ) );
        if( ! change_all ) {
            sql.append( "' AND \"" );
            sql.append( seqcol );
            sql.append( "\"=" );
            sql.append( seq );
        }
        else sql.append( '\'' );
        java.sql.Statement stat = mdl.getConnection().createStatement();
if( DEBUG ) System.err.println( sql );
        stat.executeUpdate( sql.toString() );
if( DEBUG ) System.err.printf( "Got %d atoms\n", atoms.length );
        if( atoms.length > 1 ) {
            int pk = mdl.getMaxRow() + 1;
            java.util.ArrayList<String> seqs = new java.util.ArrayList<String>();
            if( change_all ) {
                java.sql.Statement query = mdl.getConnection().createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                                                java.sql.ResultSet.CONCUR_READ_ONLY );
                sql.setLength( 0 );
                sql.append( "SELECT DISTINCT \"" );
                sql.append( seqcol );
                sql.append( "\" FROM " );
                sql.append( mdl.getName() );
                sql.append( " WHERE \"" );
                sql.append( eidcol );
                sql.append( "\"=" );
                sql.append( eid );
                sql.append( " AND \"" );
                sql.append( atomcol );
                sql.append( "\"='" );
                sql.append( edu.bmrb.starch.Utils.quoteForDB( atoms[0].getString() ) );
                sql.append( "' AND \"" );
                sql.append( labelcol );
                sql.append( "\"='" );
                sql.append( edu.bmrb.starch.Utils.quoteForDB( label ) );
                sql.append( '\'' );
                java.sql.ResultSet rs = query.executeQuery( sql.toString() );
                while( rs.next() ) seqs.add( rs.getString( 1 ) );
                rs.close();
                query.close();
            }
            else seqs.add( seq );
            boolean create_table = true;
            for( String s : seqs ) {
                for( int i = 1; i < atoms.length; i++ ) {
if( DEBUG ) System.err.printf( "Seq: %s, label %s, atom %s\n", s,label,  atoms[i].getString() );
                    sql.setLength( 0 );
                    if( create_table ) {
                        sql.append( "CREATE TEMPORARY TABLE TMPTABLE AS SELECT * FROM " );
                        sql.append( mdl.getName() );
                        sql.append( " WHERE \"" );
                        sql.append( eidcol );
                        sql.append( "\"=" );
                        sql.append( eid );
                        sql.append( " AND \"" );
                        sql.append( atomcol );
                        sql.append( "\"='" );
                        sql.append( edu.bmrb.starch.Utils.quoteForDB( atoms[0].getString() ) );
                        sql.append( "' AND \"" );
                        sql.append( labelcol );
                        sql.append( "\"='" );
                        sql.append( edu.bmrb.starch.Utils.quoteForDB( label ) );
                        sql.append( "' AND \"" );
                        sql.append( seqcol );
                        sql.append( "\"=" );
                        sql.append( s );
                        create_table = false;
                    }
                    else {
                        sql.append( "INSERT INTO TMPTABLE SELECT * FROM " );
                        sql.append( mdl.getName() );
                        sql.append( " WHERE \"" );
                        sql.append( eidcol );
                        sql.append( "\"=" );
                        sql.append( eid );
                        sql.append( " AND \"" );
                        sql.append( atomcol );
                        sql.append( "\"='" );
                        sql.append( edu.bmrb.starch.Utils.quoteForDB( atoms[0].getString() ) );
                        sql.append( "' AND \"" );
                        sql.append( labelcol );
                        sql.append( "\"='" );
                        sql.append( edu.bmrb.starch.Utils.quoteForDB( label ) );
                        sql.append( "' AND \"" );
                        sql.append( seqcol );
                        sql.append( "\"=" );
                        sql.append( s );
                    }
if( DEBUG ) System.err.println( sql );
                    stat.executeUpdate( sql.toString() );
                    sql.setLength( 0 );
                    sql.append( "UPDATE TMPTABLE SET \"" );
                    sql.append( atomcol );
                    sql.append( "\"='" );
                    sql.append( edu.bmrb.starch.Utils.quoteForDB( atoms[i].getString() ) );
                    sql.append( '\'' );
if( DEBUG ) System.err.println( sql );
                    stat.executeUpdate( sql.toString() );
                    sql.setLength( 0 );
                    sql.append( "UPDATE TMPTABLE SET ROW=" );
                    sql.append( pk );
if( DEBUG ) System.err.println( sql );
                    stat.executeUpdate( sql.toString() );
                    pk++;
                    sql.setLength( 0 );
                    sql.append( "INSERT INTO " );
                    sql.append( mdl.getName() );
                    sql.append( " SELECT * FROM TMPTABLE" );
if( DEBUG ) System.err.println( sql );
                    stat.executeUpdate( sql.toString() );
if( DEBUG ) System.err.println( "DELETE FROM TMPTABLE" );
                    stat.executeUpdate( "DELETE FROM TMPTABLE" );
                } // endfor atoms
            } // endfor sequence numbers
            seqs.clear();
        } // endif
if( DEBUG ) System.err.println( "DROP TMPTABLE" );
        stat.executeUpdate( "DROP TABLE TMPTABLE" );
        if( ! stat.getConnection().getAutoCommit() ) stat.getConnection().commit();
        stat.close();
    } //*************************************************************************
/*
    sql.setLength( 0 );
    for( edu.bmrb.starch.Column c : mdl.getColumns() ) {
        sql.append( '"' );
        sql.append( c.getDbName() );
        sql.append( "\"," );
    }
    String cols = sql.substring( 0, sql.length() - 1 );
    sql.setLength( 0 );
    sql.append( "INSERT INTO " );
    sql.append( mdl.getName() );
    sql.append( " (" );
    sql.append( cols );
    sql.append( ") SELECT " );
    sql.append( cols );
    sql.append( " FROM " );
    sql.append( mdl.getName() );
    sql.append( atomcol );
    sql.append( "\"='" );
    sql.append( atom );
    sql.append( "' AND \"" );
    sql.append( labelcol );
    sql.append( "\"='" );
    sql.append( label );
//todo FIXME: and entity ID
    sql.append( "' AND \"" );
    sql.append( seqcol );
    sql.append( "\"='" );
    sql.append( seq );
    sql.append( '\'' );
if( DEBUG ) System.err.println( sql );
*/
}
