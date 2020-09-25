package edu.bmrb.starch;

/**
 * Nomenclature conversion and validation methods.
 *
 * @author D. Maziuk
 */

/*
 * Created by IntelliJ IDEA.
 * User: dmaziuk
 * Date: Dec 6, 2006
 * Time: 3:42:22 PM
 *
 * $Id$
 */

public class Nomenmap {
    private static final boolean DEBUG = false;
    /** table model. */
    private TableModel fModel = null;
/*******************************************************************************/
    /**
     * Constructor.
     * @param model table model
     */
    public Nomenmap( TableModel model ) {
        fModel = model;
    } //*************************************************************************
    /**
     * Insert entry ID into rows.
     * @param replace if true, insert only where it is null
     * @throws java.sql.SQLException -- JDBC problem
     */
    public void insertEntryId( boolean replace )
            throws java.sql.SQLException {
        for( Column c : fModel.getColumns() ) {
            if( c.getType() == ColTypes.ENTRYID ) {
                StringBuilder sql = new StringBuilder( "UPDATE " );
                sql.append( fModel.getName() );
                sql.append( " SET \"" );
                sql.append( c.getDbName() );
                sql.append( "\"='" );
                if( fModel.getEntryId() == null ) sql.append( "NEED_ACCNO" );
                sql.append( fModel.getEntryId() );
                sql.append( '\'' );
                if( ! replace ) sql.append( " WHERE \"Entry_ID\" IS NULL" );
                java.sql.Statement stat = fModel.getConnection().createStatement();
                stat.executeUpdate( sql.toString() );
                if( ! stat.getConnection().getAutoCommit() ) stat.getConnection().commit();
                stat.close();
                return;
            }
        } // endfor
    } //*************************************************************************
    /**
     * Insert entity ID into rows.
     * @param group column group number
     * @param replace if true, insert only where it is null
     * @throws java.sql.SQLException -- JDBC problem
     */
    public void insertEntityId( int group, boolean replace )
            throws java.sql.SQLException {
        int numgroups = fModel.getNumColGroups();
        if( (group < 0) || (group >= numgroups) ) return;
        int id = fModel.getEntityId( group );
        if( id < 0 ) id = 1;
        for( Column c : fModel.getColumns() ) {
            if( (c.getType() == ColTypes.ENTITYID) && (c.getGroupId() == group) ) {
                StringBuilder sql = new StringBuilder( "UPDATE " );
                sql.append( fModel.getName() );
                sql.append( " SET \"" );
                sql.append( c.getDbName() );
                sql.append( "\"=" );
                sql.append( id );
                if( ! replace ) {
                    sql.append( " WHERE \"" );
                    sql.append( c.getDbName() );
                    sql.append( "\" IS NULL" );
                }
                java.sql.Statement stat = fModel.getConnection().createStatement();
                stat.executeUpdate( sql.toString() );
                if( ! stat.getConnection().getAutoCommit() ) stat.getConnection().commit();
                stat.close();
                return;
            }
        } // endfor
    } //*************************************************************************
    /**
     * Insert default values into rows.
     * @param replace if true, insert only where it is null
     * @throws java.sql.SQLException -- JDBC problem
     */
    public void insertAllDefaults( boolean replace )
            throws java.sql.SQLException {
if( DEBUG ) System.err.println( "* Nomenmap.insertAllDefaults()" );
        StringBuilder sql = new StringBuilder();
        java.sql.Statement stat = fModel.getConnection().createStatement();
        for( Column c : fModel.getColumns() ) {
            if( c.getType() == ColTypes.LCLID ) {
                sql.setLength( 0 );
                sql.append( "UPDATE " );
                sql.append( fModel.getName() );
                sql.append( " SET \"" );
                sql.append( c.getDbName() );
                sql.append( "\"=1" );
                if( ! replace ) {
                    sql.append( " WHERE \"" );
                    sql.append( c.getDbName() );
                    sql.append( "\" IS NULL" );
                }
            }
            else if( c.getType() == ColTypes.ENTRYID ) {
                sql.setLength( 0 );
                sql.append( "UPDATE " );
                sql.append( fModel.getName() );
                sql.append( " SET \"" );
                sql.append( c.getDbName() );
                sql.append( "\"='" );
                if( fModel.getEntryId() == null ) sql.append( "NEED_ACCNO" );
                sql.append( fModel.getEntryId() );
                sql.append( '\'' );
                if( ! replace ) {
                    sql.append( " WHERE \"" );
                    sql.append( c.getDbName() );
                    sql.append( "\" IS NULL" );
                }
            }
            else if( c.getType() == ColTypes.ASSYID ) {
                sql.setLength( 0 );
                sql.append( "UPDATE " );
                sql.append( fModel.getName() );
                sql.append( " SET \"" );
                sql.append( c.getDbName() );
//todo: FIXME!!!
// an entity may occur multiple times in assembly (e.g. dimers)
                sql.append( "\"=1" );
                if( ! replace ) {
                    sql.append( " WHERE \"" );
                    sql.append( c.getDbName() );
                    sql.append( "\" IS NULL" );
                }
            }
if( DEBUG ) System.err.println( sql );
            stat.executeUpdate( sql.toString() );
        } // endfor
        if( ! stat.getConnection().getAutoCommit() ) stat.getConnection().commit();
        stat.close();
        for( int i = 0; i < fModel.getNumColGroups(); i++ )
            insertEntityId( i, replace );
    } //*************************************************************************
    /**
     * Delete rows where value is null.
     * @throws java.sql.SQLException
     */
    public void deleteEmptyRows() throws java.sql.SQLException {
        java.util.ArrayList<String> valcols = new java.util.ArrayList<String>();
        for( Column c : fModel.getColumns() ) {
            if( c.getType() == ColTypes.VAL ) valcols.add( c.getDbName() );
        }
        if( valcols.size() < 1 ) return;
        StringBuilder sql = new StringBuilder( "DELETE FROM ");
        sql.append( fModel.getName() );
        sql.append( " WHERE \"" );
        sql.append( valcols.get( 0 ) );
        sql.append( "\" IS NULL" );
        for( int i = 1; i < valcols.size(); i++ ) {
            sql.append( " AND \"" );
            sql.append( valcols.get( i ) );
            sql.append( "\" IS NULL" );
        }
if( DEBUG ) System.err.println( sql );
        java.sql.Statement stat = fModel.getConnection().createStatement();
        stat.executeUpdate( sql.toString() );
        if( ! stat.getConnection().getAutoCommit() ) stat.getConnection().commit();
        stat.close();
    } //*************************************************************************
    /**
     * Renumber rows.
     * @throws java.sql.SQLException
     */
    public void reindexRows() throws java.sql.SQLException {
        String col = null;
        StringBuilder sql = new StringBuilder( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        for( Column c : fModel.getColumns() )
            if( c.getType() == ColTypes.ROWIDX ) {
                col = c.getDbName();
                break;
            }
// not sure if all data tables have an index column. If they do, replace return
// with NPE ?
        if( col == null ) return;
        sql.append( col );
        sql.append( "\"=? WHERE ROW=?" );
        java.sql.PreparedStatement pstat = fModel.getConnection().prepareStatement( sql.toString() );
        String seqcol = null;
        for( Column c : fModel.getColumns() )
            if( c.getGroupId() == 0 )
                if( c.getType() == ColTypes.SEQID ) seqcol = c.getDbName();
        if( seqcol == null )
            throw new NullPointerException( "No seq ID column in group 1" );
        sql.setLength( 0 );
        sql.append( "SELECT ROW,\"" );
        sql.append( seqcol );
        sql.append( "\" FROM " );
        sql.append( fModel.getName() );
        sql.append( " ORDER BY \"" );
        sql.append( seqcol );
        sql.append( "\",ROW" );
        java.sql.Statement query = fModel.getConnection().createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                                           java.sql.ResultSet.CONCUR_READ_ONLY );
        int row = 1;
if( DEBUG ) System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        boolean commit = pstat.getConnection().getAutoCommit();
        pstat.getConnection().setAutoCommit( false );
        while( rs.next() ) {
            pstat.setInt( 1, row );
            pstat.setInt( 2, rs.getInt( 1 ) );
if( DEBUG ) System.err.println( pstat );
            pstat.executeUpdate();
            row++;
        }
        pstat.getConnection().commit();
        pstat.getConnection().setAutoCommit( commit );
        pstat.close();
        rs.close();
        query.close();
        sql.setLength( 0 );
        sql.append( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET ROW=\"" );
        sql.append( col );
        sql.append( '"' );
        java.sql.Statement stat = fModel.getConnection().createStatement();
if( DEBUG ) System.err.println( sql );
        stat.executeUpdate( sql.toString() );
        if( ! stat.getConnection().getAutoCommit() ) stat.getConnection().commit();
        stat.close();
    } //*************************************************************************
    /**
     * Checks author data in a residue for errors.
     * @param pquery query that returns {atom name,row index} pairs for the residue
     * @param restypeid residue type id
     * @param seq residue sequence number, for error messages
     * @param label residue label, for error messages and nomenclature checks
     * @return false on error
     * @throws java.sql.SQLException
     */
    private boolean checkResidue( java.sql.PreparedStatement pquery, int restypeid,
                                  String seq, String label )
            throws java.sql.SQLException {
//
// this assumes that table has no NULL atom names
//
        boolean rc = true;
        boolean has_gln_hg1 = false;
        IntStringPair gln_hg2 = null;
        String atom;
        boolean known;
        java.util.ArrayList<IntStringPair> atoms = new java.util.ArrayList<IntStringPair>();
//        Pair<Pair<String, String>, Pair<String, String>> meth;
        java.util.List<Pair<Pair<String, String>, Pair<String, String>>> mlist;
        IntStringPair [] bmrbatoms;
        
        label = label.toUpperCase();
        
        java.sql.ResultSet rs = pquery.executeQuery();
        while( rs.next() ) {
            atom = rs.getString( 1 );
            known = false;
// special case: THR HG usually means HG2 and is converted to HG21..HG23
// however, the warning is needed in case they meant HG1
            if( label.toUpperCase().equals( "THR" ) && atom.equals( "HG" ) )
                fModel.getErrorList().add( new Error( Error.Severity.WARN, rs.getInt( 2 ),
                                                      seq, label, atom, "Found THR HG" ) );
            bmrbatoms = fModel.getDictionary().getAtoms( fModel.getNomenclatureId(),
                                                         restypeid, label, atom );
if( DEBUG )
    System.err.printf( "!!! checking atom %s in row %d: %s %s\n", atom, rs.getInt( 2 ), seq, label );
            if( bmrbatoms == null ) {
if( DEBUG )
    System.err.println( "!!! bmrbatoms is null" );
// spec. case: IUPAC methyls
//FIXME

// special case:
// GLN HG2 without either HG1 or HG3
                if( label.toUpperCase().equals( "GLN" ) ) {
                    if( atom.equals( "HG1" ) || atom.equals( "HG3" ) )
                        has_gln_hg1 = true;
                    if( atom.equals( "HG2" ) )
                        gln_hg2 = new IntStringPair( rs.getInt( 2 ), atom );
                }
if( DEBUG )
    System.err.printf( "! Checking methylenes for %s %s\n", label, atom );
                mlist = fModel.getDictionary().getMethylenes( restypeid, label );
                if( mlist == null ) {
                    fModel.getErrorList().add( new Error( Error.Severity.CRIT, rs.getInt( 2 ),
                                                          seq, label, atom, "Unknown atom (1)" ) );
                    rc = false;
                }
                else {
                    for( Pair<Pair<String, String>, Pair<String, String>> meth : mlist ) {
if( DEBUG )
    System.err.printf( "! Checking methylene nomenclature for %s %s vs %s\n", label, atom, meth.getFirst().getFirst() );
                        if( atom.equals( meth.getFirst().getFirst() ) ) {
                            fModel.getErrorList().add( new Error( Error.Severity.INFO, rs.getInt( 2 ),
                                                          seq, label, atom, "Bad methylene nomenclature" ) );
                            known = true;
                            break;
                        }
                        if( atom.equals( meth.getSecond().getFirst() ) ) {
                            known = true;
                            break;
                        }
                    }
                }
                if( ! known )
                    fModel.getErrorList().add( new Error( Error.Severity.CRIT, rs.getInt( 2 ),
                                                          seq, label, atom, "Unknown atom (2)" ) );
            } // endif bmrbatoms is null
            else {
                for( IntStringPair p : bmrbatoms ) {
// replace atom id with row number
                    p.setInt( rs.getInt( 2 ) );
                    atoms.add( p );
if( DEBUG )
    System.err.printf( "Adding atom in row %d: %s\n", p.getInt(), p.getString() );
// special case:
// GLN HG2 without either HG1 or HG3
                    if( label.toUpperCase().equals( "GLN" ) ) {
                        if( p.getString().equals( "HG1" ) || p.getString().equals( "HG3" ) )
                            has_gln_hg1 = true;
                        if( p.getString().equals( "HG2" ) )
                            gln_hg2 = new IntStringPair( rs.getInt( 2 ), atom );
                    }
                }
if( DEBUG ) System.err.printf( "! %s %s has_gln_hg1=%s, has hg2=%s\n", seq, label, has_gln_hg1, (gln_hg2 == null ? "no" : "yes" ) );
            }
        } // endwhile
        rs.close();
        if( (gln_hg2 != null) && (! has_gln_hg1) )
            fModel.getErrorList().add(  new Error( Error.Severity.WARN, gln_hg2.getInt(),
                                                   seq, label, gln_hg2.getString(),
                                                   "Found GLN HG2 without HG1 or HG3" ) );
if( DEBUG )
{
    System.err.println( "* Atoms" );
    for( IntStringPair a : atoms ) System.err.printf( "* %s in row %d\n", a.getString(), a.getInt() );
    System.err.println( "***" );
}
// check for duplicate atoms
        for( int i = 0; i < atoms.size(); i++ )
            for( int j = i + 1; j < atoms.size(); j++ ) {
if( DEBUG )
    System.err.printf( "=== Check for duplicate |%s| - |%s| in %s %s\n", atoms.get( i ).getString(), atoms.get( j ).getString(), seq, label );
                if( atoms.get( i ).getString().equals( atoms.get( j ).getString() ) ) {
//todo: in chemical shifts it's ok to have duplicate atoms if they have different
// figure of merit
                    fModel.getErrorList().add(  new Error( Error.Severity.CRIT,
                                                           atoms.get( j ).getInt(),
                                                           seq, label, atoms.get( j ).getString(),
                                                           "Duplicate atom (has " + atoms.get( i ).getString() + ")" ) );
                    rc = false;
                }
            }
        atoms.clear();
        return rc;
    } //*************************************************************************
    /**
     * Returns false if atom names cannot be converted.
     * @param group column group
     * @return true or false
     * @throws java.sql.SQLException
     */
    public boolean checkAuthorData( int group ) throws java.sql.SQLException {
        boolean rc = true;
        String seqcol = null;
        String labelcol = null;
        String atomcol = null;
        for( Column c : fModel.getColumns() ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == ColTypes.ASEQID ) seqcol = c.getDbName();
                if( c.getType() == ColTypes.ACOMPID ) labelcol = c.getDbName();
                if( c.getType() == ColTypes.AATOMID ) atomcol = c.getDbName();
            }
        }
        if( seqcol == null )
            throw new NullPointerException( "No author seqID column in group " + (group + 1) );
        if( labelcol == null )
            throw new NullPointerException( "No author compID column in group " + (group + 1) );
        if( atomcol == null )
            throw new NullPointerException( "No author atomID column in group " + (group + 1) );
        StringBuilder sql = new StringBuilder();
// prepared query
        sql.append( "SELECT \"" );
        sql.append( atomcol );
        if( fModel.getRowIndexColumn() != null ) {
            sql.append( "\",\"" );
            sql.append( fModel.getRowIndexColumn().getDbName() );
            sql.append( '"' );
        }
        else sql.append( "\",ROW" );
        sql.append( " FROM " );
        sql.append( fModel.getName() );
        sql.append( " WHERE \"" );
        sql.append( seqcol );
        sql.append( "\"=? AND \"" );
        sql.append( labelcol );
        sql.append( "\"=?" );
        java.sql.PreparedStatement pquery =
                fModel.getConnection().prepareStatement( sql.toString(),
                                                         java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rowrs = null;
// residues
        sql.setLength( 0 );
        sql.append( "SELECT DISTINCT \"" );
        sql.append( seqcol );
        sql.append( "\",\"" );
        sql.append( labelcol );
        sql.append( "\" FROM " );
        sql.append( fModel.getName() );
        sql.append( " ORDER BY \"" );
        sql.append( seqcol );
        sql.append( '"' );
if( DEBUG ) System.err.println( sql );
        java.sql.Statement query = fModel.getConnection().createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                                           java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        int restype = fModel.getRestypeId( group );
        String lastseq = null;
        String lastres = null;
        int idx;
// assume we already checked for missing values
        while( rs.next() ) {
            if( (lastseq == null) && (lastres == null) ) {
                lastseq = rs.getString( 1 );
                lastres = rs.getString( 2 );
            }
            else {
                if( lastseq.equals( rs.getString( 1 ) ) && (! lastres.equals( rs.getString( 2 ) ) ) ) {
                    pquery.setString( 1, rs.getString( 1 ) );
                    pquery.setString( 2, rs.getString( 2 ) );
                    rowrs = pquery.executeQuery();
                    if( rowrs.next() ) idx = rowrs.getInt( 2 );
                    else idx = -1;
                    fModel.getErrorList().add(  new Error( Error.Severity.CRIT, idx,
                                                           rs.getString( 1 ), rs.getString( 2 ), "",
                                                           "Wrong residue for sequence number: was " +
                                                           lastres ) );
                    continue;
                }
                lastseq = rs.getString( 1 );
                lastres = rs.getString( 2 );
            }
            pquery.setString( 1, rs.getString( 1 ) );
            pquery.setString( 2, rs.getString( 2 ) );
            if( ! checkResidue( pquery, restype, rs.getString( 1 ), rs.getString( 2 ) ) )
                rc = false;
        } // endwhile
        if( rowrs != null ) rowrs.close();
        pquery.close();
        rs.close();
        query.close();
        return rc;
    } //*************************************************************************
    /**
     * Checks for missing residue numbers, names, and atom names.
     * @return false if there are missing names
     * @throws java.sql.SQLException
     */
    public boolean checkNullNames() throws java.sql.SQLException {
        boolean rc = true;
        StringBuilder sql = new StringBuilder( "SELECT \"" );
        if( fModel.getRowIndexColumn() != null )
            sql.append( fModel.getRowIndexColumn().getDbName() );
        else {
            sql.append( fModel.getColumn( fModel.getAuthSeqColIdx( 0 ) ).getDbName() );
            sql.append( "\",\"" );
            sql.append( fModel.getColumn( fModel.getAuthCompColIdx( 0 ) ).getDbName() );
            sql.append( "\",\"" );
            sql.append( fModel.getColumn( fModel.getAuthAtomColIdx( 0 ) ).getDbName() );
        }
        sql.append( "\" FROM " );
        sql.append( fModel.getName() );
        sql.append( " WHERE \"" );
        for( int i = 0; i < fModel.getNumColGroups(); i++ ) {
            sql.append( fModel.getColumn( fModel.getAuthSeqColIdx( i ) ).getDbName() );
            sql.append( "\" IS NULL OR \"" );
            sql.append( fModel.getColumn( fModel.getAuthCompColIdx( i ) ).getDbName() );
            sql.append( "\" IS NULL OR \"" );
            sql.append( fModel.getColumn( fModel.getAuthAtomColIdx( i ) ).getDbName() );
            if( i < (fModel.getNumColGroups() - 1) ) sql.append( "\" IS NULL OR \"" );
            else sql.append( "\" IS NULL" );
        }
        sql.append( " ORDER BY ROW" );
        Error e;
        java.sql.Statement query = fModel.getConnection().createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                                           java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG ) System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            rc = false;
            if( fModel.getRowIndexColumn() != null )
                e = new Error( Error.Severity.CRIT, rs.getInt( 1 ), "", "", "",
                               "Missing author seq, comp, or atom ID" );
            else e = new Error( Error.Severity.CRIT, -1, rs.getString( 1 ), rs.getString( 2 ),
                                rs.getString( 3 ), "Missing author seq, comp, or atom ID" );
            fModel.getErrorList().add( e );
        }
        rs.close();
        query.close();
        return rc;
    } //*************************************************************************
    /**
     * Sort rows.
     *
     * @throws java.sql.SQLException
     * @see AtomSorter
     */
    public void sort() throws java.sql.SQLException {
        String seqcol = null;
        String labelcol = null;
        String atomcol = null;
        String typecol = null;
        for( Column c : fModel.getColumns() ) {
            if( c.getGroupId() == 0 ) {
                if( c.getType() == ColTypes.SEQID ) seqcol = c.getDbName();
                if( c.getType() == ColTypes.COMPID ) labelcol = c.getDbName();
                if( c.getType() == ColTypes.ATOMID ) atomcol = c.getDbName();
                if( c.getType() == ColTypes.ATOMTYPE ) typecol = c.getDbName();
            }
        }
        if( seqcol == null )
            throw new NullPointerException( "No seq ID column in group 1" );
        if( labelcol == null )
            throw new NullPointerException( "No comp ID column in group 1" );
        if( atomcol == null )
            throw new NullPointerException( "No atom ID column in group 1" );
        if( typecol == null )
            throw new NullPointerException( "No atom type column in group 1" );
        StringBuilder sql = new StringBuilder();
// update statement
        sql.append( "UPDATE " );
        sql.append( fModel.getName() );
//        sql.append( " SET ROW=? WHERE ROW=?" );
        sql.append( " SET ROW=? WHERE \"" );
        sql.append( seqcol );
        sql.append( "\"=? AND \"" );
        sql.append( atomcol );
        sql.append( "\"=?" );
        java.sql.PreparedStatement pstat = fModel.getConnection().prepareStatement( sql.toString() );
// atom query
        sql.setLength( 0 );
        sql.append( "SELECT \"" );
        sql.append( atomcol );
        sql.append( "\",\"" );
        sql.append( typecol );
        sql.append( "\",ROW FROM " );
        sql.append( fModel.getName() );
        sql.append( " WHERE \"" );
        sql.append( seqcol );
        sql.append( "\"=? AND \"" );
        sql.append( labelcol );
        sql.append( "\"=?" );
        java.sql.PreparedStatement pquery =
                fModel.getConnection().prepareStatement( sql.toString(),
                                                         java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs2;
// max. row number
        int maxrow = fModel.getMaxRow() + 1;
// residue query
        sql.setLength( 0 );
        sql.append( "SELECT DISTINCT \"" );
        sql.append( seqcol );
        sql.append( "\",\"" );
        sql.append( labelcol );
        sql.append( "\" FROM " );
        sql.append( fModel.getName() );
        sql.append( " ORDER BY \"" );
        sql.append( seqcol );
        sql.append( '"' );
if( DEBUG ) System.err.println( sql );
// row number : atom name pairs
        java.util.ArrayList<IntStringPair> rows = new java.util.ArrayList<IntStringPair>();
// atom name : atom type pairs
        java.util.ArrayList<StringStringPair> names = new java.util.ArrayList<StringStringPair>();
// comparator
        AtomSorter s = new AtomSorter();
        java.sql.Statement query =
                fModel.getConnection().createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                        java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            rows.clear();
            names.clear();
            pquery.setInt( 1, rs.getInt( 1 ) );
            pquery.setString( 2, rs.getString( 2 ) );
if( DEBUG )
    System.err.println( pquery );
            rs2 = pquery.executeQuery();
            while( rs2.next() ) {
                rows.add( new IntStringPair( rs2.getInt( 3 ), rs2.getString( 1 ) ) );
                names.add( new StringStringPair( rs2.getString( 1 ), rs2.getString( 2 ) ) );
            }
            rs2.close();
            java.util.Collections.sort( names, s );
            for( StringStringPair n : names )
                for( IntStringPair r : rows )
                    if( (r.getString() != null) && (r.getString().equals( n.getFirst() )) ) {
                        pstat.setInt( 1, maxrow );
//                        pstat.setInt( 2, r.getInt() );
                        pstat.setInt( 2, rs.getInt( 1 ) );
                        pstat.setString( 3, r.getString() );
if( DEBUG )
    System.err.println( pstat );
                        pstat.executeUpdate();
                        maxrow++;
                        break;
                    }
        }
        if( ! pstat.getConnection().getAutoCommit() ) pstat.getConnection().commit();
        pstat.close();
        pquery.close();
        rs.close();
        query.close();
        fModel.updateTable();
    } //*************************************************************************
    /**
     * Convert residue code to labels, if necessary, and copy to "BMRB" column.
     * @param group group number
     * @return false on error
     * @throws java.sql.SQLException
     */
    public boolean convertResidues( int group ) throws java.sql.SQLException {
if( DEBUG ) System.err.printf( "** convert residues in group %d\n", group );
        boolean rc = true;
        String eidcol = null;
        String compidcol = null; // compID (target col.)
        String labelcol = null;  // author compID (source)
        String seqcol = null;
        for( Column c : fModel.getColumns() ) {
//            if( c.getGroupId() == 0 ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == ColTypes.ENTITYID ) eidcol = c.getDbName();
                if( c.getType() == ColTypes.ASEQID ) seqcol = c.getDbName();
                if( c.getType() == ColTypes.ACOMPID ) labelcol = c.getDbName();
                if( c.getType() == ColTypes.COMPID ) compidcol = c.getDbName();
            }
        }
        if( eidcol == null )
            throw new NullPointerException( "No entity ID column in group " + (group + 1) );
        if( labelcol == null )
            throw new NullPointerException( "No author comp ID column in group " + (group + 1) );
        if( seqcol == null )
            throw new NullPointerException( "No seq ID column in group " + (group + 1) );
// check residue type
        int restype = fModel.getRestypeId( group );
        if( restype < 0 ) {
            fModel.getErrorList().add( new Error( Error.Severity.CRIT, -1, "", "", "",
                                                  "No residue type for group " + (group + 1) ) );
            return false;
        }
if( DEBUG ) System.err.printf( "** eidcol %s, seqcol %s, labelcol %s, compidcol %s\n", eidcol, seqcol, labelcol, compidcol );
        StringBuilder sql = new StringBuilder();
// update statement
        sql.append( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( compidcol );
        sql.append( "\"=? WHERE \"" );
        sql.append( eidcol );
        sql.append( "\"=? AND \"" );
        sql.append( labelcol );
        sql.append("\"=?" );
        java.sql.PreparedStatement pstat =
                fModel.getConnection().prepareStatement( sql.toString() );
// fetch label from dictionary
        sql.setLength( 0 );
        sql.append( "SELECT LABEL FROM RESIDUES WHERE TYPEID=" );
        sql.append( restype );
        sql.append( " AND (LABEL=? OR CODE=?)" );
        java.sql.PreparedStatement pquery =
                fModel.getConnection().prepareStatement( sql.toString(),
                                                         java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                         java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs2 = null;
// fetch {entity id:residue label} pairs from data. Sequence # is for error messages
        sql.setLength( 0 );
        sql.append( "SELECT DISTINCT \"" );
        sql.append( eidcol );
        sql.append( "\",\"" );
        sql.append( labelcol );
        sql.append( "\",\"" );
        sql.append( seqcol );
        sql.append( "\" FROM ");
        sql.append( fModel.getName() );
        java.sql.Statement query =
                fModel.getConnection().createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                        java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG ) System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            pquery.setString( 1, rs.getString( 2 ).toUpperCase() );
            pquery.setString( 2, rs.getString( 2 ).toUpperCase() );
if( DEBUG ) System.err.println( pquery );
            rs2 = pquery.executeQuery();
            if( ! rs2.next() ) {
                fModel.getErrorList().add( new Error( Error.Severity.ERR, -1, rs.getString( 3 ),
                                                      rs.getString( 2 ), "",
                                                      "Unknown residue label " + rs.getString( 2 ) ) );
// disabled -- let them convert (copy) non-standard residues
//                rc = false;
                pstat.setString( 1, rs.getString( 2 ) );
                pstat.setInt( 2, rs.getInt( 1 ) );
                pstat.setString( 3, rs.getString( 2 ) );
            }
            else {
                pstat.setString( 1, rs2.getString( 1 ) );
                pstat.setInt( 2, rs.getInt( 1 ) );
                pstat.setString( 3, rs.getString( 2 ) );
            }
if( DEBUG ) System.err.println( pstat );
            pstat.executeUpdate();
//            }
        } // endwhile rs
        if( ! pstat.getConnection().getAutoCommit() ) pstat.getConnection().commit();
        pstat.close();
        if( rs2 != null ) rs2.close();
        pquery.close();
        rs.close();
        query.close();
        fModel.updateTable();
        return rc;
    } //*************************************************************************
    /**
     * Renumber residues (BMRB seq ID and comp index ID columns).
     * @param start starting number
     * @param group column group
     * @throws java.sql.SQLException
     */
    public void renumberResidues( int start, int group )
            throws java.sql.SQLException {
        String seqidcol = null;
        String idxidcol = null;
        String seqcol = null;
        for( Column c : fModel.getColumns() ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == ColTypes.COMPIDX ) idxidcol = c.getDbName();
                if( c.getType() == ColTypes.SEQID ) seqidcol = c.getDbName();
                if( c.getType() == ColTypes.ASEQID ) seqcol = c.getDbName();
            }
        }
        if( seqidcol == null )
            throw new NullPointerException( "No author comp ID column in group " + (group + 1) );
        if( seqcol == null )
            throw new NullPointerException( "No seq ID column in group " + (group + 1) );
        StringBuilder sql = new StringBuilder();
// update statement
        sql.append( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( seqidcol );
        sql.append( "\"=? WHERE \"" );
        sql.append( seqcol );
        sql.append( "\"=?" );
        java.sql.PreparedStatement pstat =
                fModel.getConnection().prepareStatement( sql.toString() );
// query
        sql.setLength( 0 );
        sql.append( "SELECT DISTINCT \"" );
        sql.append( seqcol );
//todo: CAST is not guaranteed to work since author sequence can be e.g. "1a, 1b"
//todo: see trac ticket #3
        sql.append( "\",CAST (\"" );
        sql.append( seqcol );
        sql.append( "\" AS INTEGER) FROM " );
        sql.append( fModel.getName() );
        sql.append( " ORDER BY CAST(\"" );
        sql.append( seqcol );
        sql.append( "\" AS INTEGER)" );
        java.sql.Statement query =
                fModel.getConnection().createStatement( java.sql.ResultSet.TYPE_FORWARD_ONLY,
                                                        java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG ) System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        int oldseq = -1;
        int num = start - 1;
        while( rs.next() ) {
// match numbers to author numbers
            if( oldseq == -1 ) num++;
            else {
                num = num + rs.getInt( 2 ) - oldseq;
            }
            oldseq = rs.getInt( 2 );
            pstat.setInt( 1, num );
            pstat.setString( 2, rs.getString( 1 ) );
if( DEBUG ) System.err.println( pstat );
            pstat.executeUpdate();
        }
        if( ! pstat.getConnection().getAutoCommit() ) pstat.getConnection().commit();
        pstat.close();
        rs.close();
        query.close();
// set comp index ID to seq ID
        if( idxidcol != null ) {
            sql.setLength( 0 );
            sql.append( "UPDATE " );
            sql.append( fModel.getName() );
            sql.append( " SET \"" );
            sql.append( idxidcol );
            sql.append( "\"=\"" );
            sql.append( seqidcol );
            sql.append( '"' );
            java.sql.Statement stat = fModel.getConnection().createStatement();
if( DEBUG ) System.err.println( sql );
            stat.executeUpdate( sql.toString() );
            if( ! stat.getConnection().getAutoCommit() ) stat.getConnection().commit();
            stat.close();
        }
        fModel.updateTable();
    } //*************************************************************************
    /**
     * Convert atom names in specified column group.
     * @param group column group
     * @return false on error
     * @throws java.sql.SQLException
     */
    public boolean convertAtoms( int group ) throws java.sql.SQLException {
        boolean rc = true;
        String eidcol = null;
        String seqcol = null;
        String labelcol = null;
        String atomcol = null;
        String tgtcol = null;
        for( Column c : fModel.getColumns() ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == ColTypes.ENTITYID ) eidcol = c.getDbName();
                if( c.getType() == ColTypes.ASEQID ) seqcol = c.getDbName();
                if( c.getType() == ColTypes.ACOMPID ) labelcol = c.getDbName();
                if( c.getType() == ColTypes.AATOMID ) atomcol = c.getDbName();
                if( c.getType() == ColTypes.ATOMID ) tgtcol = c.getDbName();
            }
        }
        if( eidcol == null )
            throw new NullPointerException( "No entity ID column in group " + (group + 1) );
        if( seqcol == null )
            throw new NullPointerException( "No author sequence ID column in group " + (group + 1) );
        if( labelcol == null )
            throw new NullPointerException( "No author comp ID column in group " + (group + 1) );
        if( atomcol == null )
            throw new NullPointerException( "No author atom ID column in group " + (group + 1) );
        if( tgtcol == null )
            throw new NullPointerException( "No atom ID column in group " + (group + 1) );
// convert (HB1, HB2) to (HB2, HB3), lone HB1 to HB2
        convertMethylenes( group, eidcol, labelcol, atomcol, tgtcol );
//todo: convert lone (e.g.) HB11 to (HB11, HB12, HB13) (does not convert HB1)
        convertMethyls( group, eidcol, labelcol, atomcol, tgtcol );
        StringBuffer sql = new StringBuffer();
// atoms
        sql.append( "SELECT DISTINCT \"" );
        sql.append( atomcol );
        sql.append( "\" FROM " );
        sql.append( fModel.getName() );
        sql.append( " WHERE \"" );
        sql.append( eidcol );
        sql.append( "\"=? AND \"" );
        sql.append( labelcol );
        sql.append( "\"=?" );
        java.sql.PreparedStatement pquery = fModel.getConnection().prepareStatement(
                sql.toString(), java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs2 = null;
// residues
        sql.setLength( 0 );
        sql.append( "SELECT DISTINCT \"" );
        sql.append( eidcol );
        sql.append( "\",\"" );
        sql.append( labelcol );
        sql.append( "\" FROM " );
        sql.append( fModel.getName() );
        IntStringPair [] atoms;
        java.util.List<Pair<Pair<String, String>, Pair<String, String>>> mlist;
        java.util.List<String []> methyls;
        java.sql.Statement query = fModel.getConnection().createStatement(
                java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG ) System.err.println( sql );
// foreach entity:residue
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            mlist = fModel.getDictionary().getMethylenes( fModel.getRestypeId( group ),
                                                          rs.getString( 2 ) );
            methyls = fModel.getDictionary().getMethyls( fModel.getRestypeId( group ),
                                                         rs.getString( 2 ) );
            pquery.setInt( 1, rs.getInt( 1 ) );
            pquery.setString( 2, rs.getString( 2 ) );
if( DEBUG ) System.err.println( pquery );
// foreach atom
            rs2 = pquery.executeQuery();
ATOMS:      while( rs2.next() ) {
// skip methylenes
                if( mlist != null ) {
                    for( Pair<Pair<String, String>, Pair<String, String>> methylenes : mlist )
                        if( methylenes.getFirst().getFirst().equals( rs2.getString( 1 ) )
                                || methylenes.getSecond().getFirst().equals( rs2.getString( 1 ) ) ) {
if( DEBUG ) System.err.printf( "+++ skip %s %s methylene\n", rs.getString( 2 ), rs2.getString( 1 ) );
                            continue ATOMS;
                        }
                }
// skip non-pseudo methyls
                if( methyls != null ) {
                    for( String [] m : methyls ) {
                        for( String methyl : m ) {
                            if( methyl.equals( rs2.getString( 1 ) ) ) {
if( DEBUG ) System.err.printf( "+++ skip %s %s methyl\n", rs.getString( 2 ), rs2.getString( 1 ) );
                                continue ATOMS;
                            }
                        }
                    }
                }
                atoms = fModel.getDictionary().getAtoms( fModel.getNomenclatureId(),
                                                         fModel.getRestypeId( group ),
                                                         rs.getString( 2 ),
                                                         rs2.getString( 1 ) );
//todo: error message needs row or sequence number, but that'll screw up "distinct"
//todo: the code updates all rows for entity:label in one statement now
                if( (atoms == null) || (atoms.length == 0) ) {
                    fModel.getErrorList().add(  new Error( Error.Severity.ERR, -1,
                                                           "", rs.getString( 2 ),
                                                           rs2.getString( 1 ),
                                                           "Unknown atom (3)" ) );
// continue anyway: this may be a non-standard residue
//                    rc = false;
                    if( ! updateAtom( eidcol, rs.getString( 1 ), labelcol, rs.getString( 2 ),
                                      atomcol, rs2.getString( 1 ), tgtcol, rs2.getString( 1 ) ) )
                        rc = false;
                }
                else if( atoms.length == 1 ) {
                    if( ! updateAtom( eidcol, rs.getString( 1 ), labelcol, rs.getString( 2 ),
                                      atomcol, rs2.getString( 1 ), tgtcol, atoms[0].getString() ) )
                        rc = false;
                }
                else {
//todo: fetch row number, write insertAtoms()
                    if( ! insertAtoms( eidcol, rs.getString( 1 ), labelcol, rs.getString( 2 ),
                                       atomcol, rs2.getString( 1 ), tgtcol, atoms ) ) rc = false;
                }
            } // endwhile rs2
        } // endwhile rs
        if( rs2 != null ) rs2.close();
        pquery.close();
        rs.close();
        query.close();
        fModel.updateTable();
        return rc;
    } //*************************************************************************
    /**
     * Converts methylene atom names.
     * If original naming is e.g. HB1, HB2, convert to HB2, HB3; if original naming
     * is HB2, HB3 -- don't convert. The problem is this has to be done in pairs:
     * to decide whether to convert HB2 to HB3 or not, we need to know if we already
     * have and HB3 and if this HB2 is a converted HB1.
     * @param group column group
     * @param eidcol entity ID column
     * @param labelcol residue label column
     * @param atomcol atom name column
     * @param tgtcol converted atom name column
     * @return false on error
     * @throws java.sql.SQLException
     */
    private boolean convertMethylenes( int group, String eidcol, String labelcol,
                                       String atomcol, String tgtcol )
            throws java.sql.SQLException {
if( DEBUG ) System.err.printf( "++ConvertMethylenes(%d, %s, %s, %s, %s)\n", group, eidcol, labelcol, atomcol, tgtcol );
        boolean rc = true;
        String seqcol = null;
        for( Column c : fModel.getColumns() ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == ColTypes.ASEQID ) {
                    seqcol = c.getDbName();
                    break;
                }
            }
        }
        if( seqcol == null )
            throw new NullPointerException( "No author sequence ID column in group " + (group + 1) );
        java.util.List<Pair<Pair<String, String>, Pair<String, String>>> mlist;
        boolean has_M1, has_M2;
        StringBuffer sql = new StringBuffer();
// update
        sql.append( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( tgtcol );
        sql.append( "\"=? WHERE \"" );
        sql.append( eidcol );
        sql.append( "\"=? AND \"" );
        sql.append( seqcol );
        sql.append( "\"=? AND \"" );
        sql.append( labelcol );
        sql.append( "\"=? AND \"" );
        sql.append( atomcol );
        sql.append( "\"=?" );
        java.sql.PreparedStatement pstat = fModel.getConnection().prepareStatement( sql.toString() );
// methylenes
        sql.setLength( 0 );
        sql.append( "SELECT \"" );
        sql.append( atomcol );
        sql.append( "\" FROM " );
        sql.append( fModel.getName() );
        sql.append( " WHERE \"" );
        sql.append( seqcol );
        sql.append( "\"=? AND \"" );
        sql.append( labelcol );
        sql.append( "\"=? AND (\"" );
        sql.append( atomcol );
        sql.append( "\"=? OR \"" );
        sql.append( atomcol );
        sql.append( "\"=?)" );
        java.sql.PreparedStatement pquery = fModel.getConnection().prepareStatement(
                sql.toString(), java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs2 = null;
// residues
        sql.setLength( 0 );
        sql.append( "SELECT DISTINCT \"" );
        sql.append( eidcol );
        sql.append( "\",\"" );
        sql.append( seqcol );
        sql.append( "\",\"" );
        sql.append( labelcol );
        sql.append( "\" FROM " );
        sql.append( fModel.getName() );
        java.sql.Statement query = fModel.getConnection().createStatement(
                java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG ) System.err.println( sql );
// foreach residue
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
//            has_M1 = false;
//            has_M2 = false;
            mlist = fModel.getDictionary().getMethylenes( fModel.getRestypeId( group ),
                                                         rs.getString( 3 ) );
            if( mlist != null ) {
// foreach methylene pair
                for( Pair<Pair<String, String>, Pair<String, String>> meth : mlist ) {
	            has_M1 = false;
    		    has_M2 = false;
                    pquery.setString( 1, rs.getString( 2 ) );
                    pquery.setString( 2, rs.getString( 3 ) );
                    pquery.setString( 3, meth.getFirst().getFirst() );
                    pquery.setString( 4, meth.getSecond().getFirst() );
if( DEBUG ) System.err.println( pquery );
                    rs2 = pquery.executeQuery();
                    while( rs2.next() ) {
                        if( rs2.getString( 1 ).equals( meth.getFirst().getFirst() ) ) {
if( DEBUG ) System.err.printf( "! %s:%s:%s has M1\n", rs.getString( 2 ), rs.getString( 3 ), rs2.getString( 1 ) );
                            has_M1 = true;
                        }
                        if( rs2.getString( 1 ).equals( meth.getSecond().getFirst() ) ) {
if( DEBUG ) System.err.printf( "! %s:%s:%s has M2\n", rs.getString( 2 ), rs.getString( 3 ), rs2.getString( 1 ) );
                            has_M2 = true;
                        }
                    }
                    if( has_M1 ) {
if( DEBUG ) System.err.printf( "* (1) Replace %s:%s:%s with %s\n", rs.getString( 2 ), rs.getString( 3 ), meth.getFirst().getFirst(), meth.getFirst().getSecond() );
                        pstat.setString( 1, meth.getFirst().getSecond() );
                        pstat.setInt( 2, rs.getInt( 1 ) );
                        pstat.setString( 3, rs.getString( 2 ) );
                        pstat.setString( 4, rs.getString( 3 ) );
                        pstat.setString( 5, meth.getFirst().getFirst() );
if( DEBUG ) System.err.println( pstat );
                        pstat.executeUpdate();
                        if( has_M2 ) {
if( DEBUG ) System.err.printf( "* (2) Replace %s:%s:%s with %s\n", rs.getString( 2 ), rs.getString( 3 ), meth.getSecond().getFirst(), meth.getSecond().getSecond() );
                            pstat.setString( 1, meth.getSecond().getSecond() );
                            pstat.setInt( 2, rs.getInt( 1 ) );
                            pstat.setString( 3, rs.getString( 2 ) );
                            pstat.setString( 4, rs.getString( 3 ) );
                            pstat.setString( 5, meth.getSecond().getFirst() );
if( DEBUG ) System.err.println( pstat );
                            pstat.executeUpdate();
                        }
                    }
                    else if( has_M2 ) { // leave as is
if( DEBUG ) System.err.printf( "* (3) Replace %s:%s:%s with %s\n", rs.getString( 2 ), rs.getString( 3 ), meth.getSecond().getFirst(), meth.getFirst().getSecond() );
                        pstat.setString( 1, meth.getSecond().getFirst() );
                        pstat.setInt( 2, rs.getInt( 1 ) );
                        pstat.setString( 3, rs.getString( 2 ) );
                        pstat.setString( 4, rs.getString( 3 ) );
                        pstat.setString( 5, meth.getSecond().getFirst() );

if( DEBUG ) System.err.println( "!" );
if( DEBUG ) System.err.println( pstat );
                        pstat.executeUpdate();
                    }
                } //endfor meth
            } //endif mlist
        } // endwhile rs
        if( ! pstat.getConnection().getAutoCommit() ) pstat.getConnection().commit();
        pstat.close();
        if( rs2 != null ) rs2.close();
        pquery.close();
        rs.close();
        query.close();
        return rc;
    } //*************************************************************************
    /**
     * Converts methyl atoms.
     * If methyl pseudo-atom is mis-labeled as IUPAC proton, e.g. LEU HD11, check
     * if the residue has HD12 and HD13. If not, add them. Otherwise (not mis-labeled),
     * do the standard conversion.
     * @param group column group
     * @param eidcol entity ID column
     * @param labelcol residue label column
     * @param atomcol atom name column
     * @param tgtcol converted atom name column
     * @return false on error
     * @throws java.sql.SQLException
     */
    private boolean convertMethyls( int group, String eidcol, String labelcol,
                                       String atomcol, String tgtcol )
            throws java.sql.SQLException {

if( DEBUG )
 System.err.printf( "++ConvertMethyls(%d, %s, %s, %s, %s)\n", group, eidcol, labelcol, atomcol, tgtcol );
        boolean rc = true;
        String seqcol = null;
        for( Column c : fModel.getColumns() ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == ColTypes.ASEQID ) {
                    seqcol = c.getDbName();
                    break;
                }
            }
        }
        if( seqcol == null )
            throw new NullPointerException( "No author sequence ID column in group " + (group + 1) );
        StringBuffer sql = new StringBuffer();
        java.util.List<String []> methyls;
        boolean has_M1, has_M2, has_M3;
        String methyl = null;
        int rownum = 0;
        java.util.List<Pair<Integer, String>> l = new java.util.ArrayList<Pair<Integer, String>>();
        Pair<Integer, String> p;
// atoms
        sql.setLength( 0 );
        sql.append( "SELECT \"" );
        sql.append( atomcol );
        sql.append( "\", ROW FROM " );
        sql.append( fModel.getName() );
        sql.append( " WHERE \"" );
        sql.append( seqcol );
        sql.append( "\"=? AND \"" );
        sql.append( labelcol );
        sql.append( "\"=? AND (\"" );
        sql.append( atomcol );
        sql.append( "\"=? OR \"" );
        sql.append( atomcol );
        sql.append( "\"=? OR \"" );
        sql.append( atomcol );
        sql.append( "\"=?)" );
        java.sql.PreparedStatement pquery = fModel.getConnection().prepareStatement(
                sql.toString(), java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY );
        java.sql.ResultSet rs2 = null;
// residues
        sql.setLength( 0 );
        sql.append( "SELECT DISTINCT \"" );
        sql.append( eidcol );
        sql.append( "\",\"" );
        sql.append( seqcol );
        sql.append( "\",\"" );
        sql.append( labelcol );
        sql.append( "\" FROM " );
        sql.append( fModel.getName() );
        java.sql.Statement query = fModel.getConnection().createStatement(
                java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG )
 System.err.println( sql );
// foreach residue
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            methyls = fModel.getDictionary().getMethyls( fModel.getRestypeId( group ),
                                                         rs.getString( 3 ) );
            if( methyls != null ) {
                for( String [] meth : methyls ) {
                    has_M1 = false;
                    has_M2 = false;
                    has_M3 = false;
                    l.clear();
                    for( String s : meth ) l.add( new Pair<Integer, String>( null, s ) );
                    pquery.setString( 1, rs.getString( 2 ) );
                    pquery.setString( 2, rs.getString( 3 ) );
                    pquery.setString( 3, meth[0] );
                    pquery.setString( 4, meth[1] );
                    pquery.setString( 5, meth[2] );
if( DEBUG )
    System.err.println( pquery );
                    rs2 = pquery.executeQuery();
                    while( rs2.next() ) {
                        if( rs2.getString( 1 ).equals( meth[0] ) ) {
                            has_M1 = true;
                            methyl = meth[0];
                            rownum = rs2.getInt( 2 );
                            for( java.util.Iterator<Pair<Integer, String>> i = l.iterator();
                                 i.hasNext(); ) {
                                p = i.next();
                                if( p.getSecond().equals( meth[0] ) ) i.remove();
                            }
                        }
                        if( rs2.getString( 1 ).equals( meth[1] ) ) {
                            has_M2 = true;
                            methyl = meth[1];
                            rownum = rs2.getInt( 2 );
                            for( java.util.Iterator<Pair<Integer, String>> i = l.iterator();
                                 i.hasNext(); ) {
                                p = i.next();
                                if( p.getSecond().equals( meth[1] ) ) i.remove();
                            }
                        }
                        if( rs2.getString( 1 ).equals( meth[2] ) ) {
                            has_M3 = true;
                            methyl = meth[2];
                            rownum = rs2.getInt( 2 );
                            for( java.util.Iterator<Pair<Integer, String>> i = l.iterator();
                                 i.hasNext(); ) {
                                p = i.next();
                                if( p.getSecond().equals( meth[2] ) ) i.remove();
                            }
                        }
                    } /// endwhile rs2
if( DEBUG )
 System.err.printf( "+++++ %s %s has M1: %s M2: %s M3: %s\n", rs.getString( 2 ), rs.getString( 3 ), has_M1, has_M2, has_M3 );
if( DEBUG )
 System.err.printf( "+++++ %s %s sizeof l: %d\n", rs.getString( 2 ), rs.getString( 3 ), l.size() );
// if it has all three, copy; if it has neither do nothing
//                    if( (l.size() > 0) && (l.size() <= 3) ) {
if( DEBUG )
 for( Pair<Integer, String> m : l ) System.err.printf( "++ %s\n", m.getSecond() );
                        if( has_M1 ) {
                            if( ! updateAtom( eidcol, rs.getString( 1 ), seqcol, rs.getString( 2 ),
                                              labelcol, rs.getString( 3 ), atomcol, meth[0],
                                              tgtcol, meth[0] ) )
                                rc = false;
                        }
                        if( has_M2 ) {
                            if( ! updateAtom( eidcol, rs.getString( 1 ), seqcol, rs.getString( 2 ),
                                              labelcol, rs.getString( 3 ), atomcol, meth[1],
                                              tgtcol, meth[1] ) )
                                rc = false;
                        }
                        if( has_M3 ) {
                            if( ! updateAtom( eidcol, rs.getString( 1 ), seqcol, rs.getString( 2 ),
                                              labelcol, rs.getString( 3 ), atomcol, meth[2],
                                              tgtcol, meth[2] ) )
                                rc = false;
                        }
                        if( (l.size() > 0) && (l.size() < 3) )
                            if( ! insertAtoms( eidcol, rs.getString( 1 ), seqcol, rs.getString( 2 ),
                                               labelcol, rs.getString( 3 ), atomcol, methyl, tgtcol, l ) )
                            rc = false;
//                    }
                } // enfor methyls
            } // endif methyls
        } // endwhile rs
        if( rs2 != null ) rs2.close();
        pquery.close();
        rs.close();
        query.close();
        return rc;
    } //*************************************************************************
    /**
     * Insert new atom name into target column.
     * @param eidcol entity id column name
     * @param eid entity id
     * @param labelcol author residue label column name
     * @param label author residue label
     * @param atomcol author atom id column name
     * @param atom author atom name
     * @param tgtcol target atom ID column name
     * @param val new atom name
     * @return false on error
     * @throws java.sql.SQLException
     */
    private boolean updateAtom( String eidcol, String eid, String labelcol,
                                String label, String atomcol, String atom,
                                String tgtcol, String val )
            throws java.sql.SQLException {
if( DEBUG )
    System.err.printf( "! UpdateAtoms( %s, %s, %s )\n", eid, label, atom );
        StringBuilder sql = new StringBuilder( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( tgtcol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( val ) );
        sql.append( "' WHERE \"" );
        sql.append( labelcol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( label ) );
        sql.append( "' AND \"" );
        sql.append( eidcol );
        sql.append( "\"=" );
        sql.append( eid );
        sql.append( " AND \"" );
        sql.append( atomcol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( atom ) );
        sql.append( '\'' );
        java.sql.Statement stat = fModel.getConnection().createStatement();
if( DEBUG )
    System.err.println( sql );
        int rc = stat.executeUpdate( sql.toString() );
        if( ! stat.getConnection().getAutoCommit() ) stat.getConnection().commit();
        stat.close();
        return( rc > 0 );
    } //*************************************************************************
    /**
     * Insert new atom name into target column.
     * @param eidcol entity id column name
     * @param eid entity id
     * @param seqcol author residue sequence column name
     * @param seq author residue sequence number
     * @param labelcol author residue label column name
     * @param label author residue label
     * @param atomcol author atom id column name
     * @param atom author atom name
     * @param tgtcol target atom ID column name
     * @param val new atom name
     * @return false on error
     * @throws java.sql.SQLException
     */
    private boolean updateAtom( String eidcol, String eid, String seqcol, String seq,
                                String labelcol, String label, String atomcol, String atom,
                                String tgtcol, String val )
            throws java.sql.SQLException {
if( DEBUG )
    System.err.printf( "! UpdateAtoms( %s, %s, %s )\n", eid, label, atom );
        StringBuilder sql = new StringBuilder( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( tgtcol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( val ) );
        sql.append( "' WHERE \"" );
        sql.append( labelcol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( label ) );
        sql.append( "' AND \"" );
        sql.append( eidcol );
        sql.append( "\"=" );
        sql.append( eid );
        sql.append( " AND \"" );
        sql.append( seqcol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( seq ) );
        sql.append( "' AND \"" );
        sql.append( atomcol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( atom ) );
        sql.append( '\'' );
        java.sql.Statement stat = fModel.getConnection().createStatement();
if( DEBUG )
    System.err.println( sql );
        int rc = stat.executeUpdate( sql.toString() );
        if( ! stat.getConnection().getAutoCommit() ) stat.getConnection().commit();
        stat.close();
        return( rc > 0 );
    } //*************************************************************************
    /**
     * Like updateAtom(), but for pseudo atoms that require insertion of extra
     * rows. This code iterates through rows instead of doing bulk updates.
     * @param eidcol entity id column name
     * @param eid entity id
     * @param labelcol author residue label column name
     * @param label author residue label
     * @param atomcol author atom id column name
     * @param atom author atom name
     * @param tgtcol target atom ID column name
     * @param val new atom name
     * @return false on error
     * @throws java.sql.SQLException
     */
    private boolean insertAtoms( String eidcol, String eid, String labelcol,
                                String label, String atomcol, String atom,
                                String tgtcol, IntStringPair [] val )
            throws java.sql.SQLException {
        java.util.List<Pair<Integer, String>> tmp = new java.util.ArrayList<Pair<Integer, String>>();
        for( IntStringPair p : val ) tmp.add( new Pair<Integer, String>( p.getInt(), p.getString() ) );
        return insertAtoms( eidcol, eid, labelcol, label, atomcol, atom, tgtcol, tmp );
    } //*************************************************************************
    private boolean insertAtoms( String eidcol, String eid, String labelcol,
                                 String label, String atomcol, String atom,
                                 String tgtcol, java.util.List<Pair<Integer, String>> val )
            throws java.sql.SQLException {

if( DEBUG ) {
    System.err.printf( "! InsertAtoms( %s, %s, %s ): ", eid, label, atom );
    for( Pair<Integer, String> p : val ) System.err.printf( "%s   ", p.getSecond() );
    System.err.println();
}
        StringBuilder sql = new StringBuilder();
// update statements
        sql.append( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( tgtcol );
        sql.append( "\"=? WHERE ROW=?" );
        java.sql.PreparedStatement pstat = fModel.getConnection().prepareStatement( sql.toString() );
        java.sql.Statement stat2 = fModel.getConnection().createStatement();
        java.sql.Statement stat = fModel.getConnection().createStatement();
// row number query
        sql.setLength( 0 );
        sql.append( "SELECT ROW FROM " );
        sql.append( fModel.getName() );
        sql.append( " WHERE \"" );
        sql.append( eidcol );
        sql.append( "\"=" );
        sql.append( eid );
        sql.append( " AND \"" );
        sql.append( labelcol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( label ) );
        sql.append( "' AND \"" );
        sql.append( atomcol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( atom ) );
        sql.append( "' ORDER BY ROW" );
        int pk = fModel.getMaxRow() + 1;
        java.sql.Statement query = fModel.getConnection().createStatement(
                java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG )
    System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
//            pk = fModel.getMaxRow() + 10;
//            pstat.setString( 1, val[0].getString() );
            pstat.setString( 1, val.get( 0 ).getSecond() );
            pstat.setInt( 2, rs.getInt( 1 ) );
if( DEBUG )
    System.err.println( pstat );
            pstat.executeUpdate();
            for( int i = 1; i < val.size(); i++ ) {
                sql.setLength( 0 );
                if( i == 1 ) {
                    sql.append( "CREATE TEMPORARY TABLE TMPTABLE AS SELECT * FROM " );
                    sql.append( fModel.getName() );
                    sql.append( " WHERE ROW=" );
                    sql.append( rs.getString( 1 ) );
                }
                else {
                    sql.append( "INSERT INTO TMPTABLE SELECT * FROM " );
                    sql.append( fModel.getName() );
                    sql.append( " WHERE ROW=" );
                    sql.append( rs.getString( 1 ) );
                }
if( DEBUG )
    System.err.println( sql );
                stat.executeUpdate( sql.toString() );
                sql.setLength( 0 );
                sql.append( "UPDATE TMPTABLE SET \"" );
                sql.append( tgtcol );
                sql.append( "\"='" );
//                sql.append( val[i].getString() );
                sql.append( val.get( i ).getSecond() );
                sql.append( "' WHERE ROW=" );
                sql.append( rs.getString( 1 ) );
if( DEBUG )
    System.err.println( sql );
                stat2.executeUpdate( sql.toString() );
                sql.setLength( 0 );
                sql.append( "UPDATE TMPTABLE SET ROW=" );
                sql.append( pk );
                sql.append( " WHERE ROW=" );
                sql.append( rs.getString( 1 ) );
if( DEBUG )
    System.err.println( sql );
                stat.executeUpdate( sql.toString() );
                pk++;
            } // endfor new atom names
if( DEBUG ) {
    java.sql.Statement tmpq = fModel.getConnection().createStatement();
    java.sql.ResultSet tmpr = tmpq.executeQuery( "SELECT * FROM TMPTABLE" );
    while( tmpr.next() ) System.err.printf( " - %d\t%s\t%s\t%s -> %s\t%s\n", tmpr.getInt( 1 ),
                                            tmpr.getString( 18 ), tmpr.getString( 19 ),
                                            tmpr.getString( 20 ), tmpr.getString( 8 ),
                                            tmpr.getString( 9 ) );
    tmpr.close();
    tmpq.close();
}
            sql.setLength( 0 );
            sql.append( "INSERT INTO " );
            sql.append( fModel.getName() );
            sql.append( " SELECT * FROM TMPTABLE" );
if( DEBUG )
    System.err.println( sql );
            stat.executeUpdate( sql.toString() );
if( DEBUG ) System.err.println( "DROP TMPTABLE" );
            stat.executeUpdate( "DROP TABLE TMPTABLE" );
        } // endwhile
        if( ! fModel.getConnection().getAutoCommit() ) fModel.getConnection().commit();
        stat.close();
        pstat.close();
        stat2.close();
        rs.close();
        query.close();
        return true;
    } //*************************************************************************
    private boolean insertAtoms( String eidcol, String eid, String seqcol, String seq,
                                 String labelcol, String label, String atomcol, String atom,
                                 String tgtcol, java.util.List<Pair<Integer, String>> val )
            throws java.sql.SQLException {

if( DEBUG ) {
    System.err.printf( "! InsertAtoms( %s, %s, %s ): ", eid, label, atom );
    for( Pair<Integer, String> p : val ) System.err.printf( "%s   ", p.getSecond() );
    System.err.println();
}
        StringBuilder sql = new StringBuilder();
// update statements
        java.sql.Statement stat2 = fModel.getConnection().createStatement();
        java.sql.Statement stat = fModel.getConnection().createStatement();
// row number query
        sql.setLength( 0 );
        sql.append( "SELECT ROW FROM " );
        sql.append( fModel.getName() );
        sql.append( " WHERE \"" );
        sql.append( eidcol );
        sql.append( "\"=" );
        sql.append( eid );
        sql.append( " AND \"" );
        sql.append( seqcol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( seq ) );
        sql.append( "' AND \"" );
        sql.append( labelcol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( label ) );
        sql.append( "' AND \"" );
        sql.append( atomcol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( atom ) );
        sql.append( '\'' );
        int pk = fModel.getMaxRow() + 1;
        java.sql.Statement query = fModel.getConnection().createStatement(
                java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG )
    System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            for( int i = 0; i < val.size(); i++ ) {
                sql.setLength( 0 );
                if( i == 0 ) {
                    sql.append( "CREATE TEMPORARY TABLE TMPTABLE AS SELECT * FROM " );
                    sql.append( fModel.getName() );
                    sql.append( " WHERE ROW=" );
                    sql.append( rs.getString( 1 ) );
                }
                else {
                    sql.append( "INSERT INTO TMPTABLE SELECT * FROM " );
                    sql.append( fModel.getName() );
                    sql.append( " WHERE ROW=" );
                    sql.append( rs.getString( 1 ) );
                }
if( DEBUG )
    System.err.println( sql );
                stat.executeUpdate( sql.toString() );
                sql.setLength( 0 );
                sql.append( "UPDATE TMPTABLE SET \"" );
                sql.append( tgtcol );
                sql.append( "\"='" );
//                sql.append( val[i].getString() );
                sql.append( val.get( i ).getSecond() );
                sql.append( "' WHERE ROW=" );
                sql.append( rs.getString( 1 ) );
if( DEBUG )
    System.err.println( sql );
                stat2.executeUpdate( sql.toString() );
                sql.setLength( 0 );
                sql.append( "UPDATE TMPTABLE SET ROW=" );
                sql.append( pk );
                sql.append( " WHERE ROW=" );
                sql.append( rs.getString( 1 ) );
if( DEBUG )
    System.err.println( sql );
                stat.executeUpdate( sql.toString() );
                pk++;
            } // endfor new atom names
if( DEBUG ) {
    java.sql.Statement tmpq = fModel.getConnection().createStatement();
    java.sql.ResultSet tmpr = tmpq.executeQuery( "SELECT * FROM TMPTABLE" );
    while( tmpr.next() ) System.err.printf( " - %d\t%s\t%s\t%s -> %s\t%s\n", tmpr.getInt( 1 ),
                                            tmpr.getString( 18 ), tmpr.getString( 19 ),
                                            tmpr.getString( 20 ), tmpr.getString( 8 ),
                                            tmpr.getString( 9 ) );
    tmpr.close();
    tmpq.close();
}
            sql.setLength( 0 );
            sql.append( "INSERT INTO " );
            sql.append( fModel.getName() );
            sql.append( " SELECT * FROM TMPTABLE" );
if( DEBUG )
    System.err.println( sql );
            stat.executeUpdate( sql.toString() );
if( DEBUG ) System.err.println( "DROP TMPTABLE" );
            stat.executeUpdate( "DROP TABLE TMPTABLE" );
        } // endwhile
        if( ! fModel.getConnection().getAutoCommit() ) fModel.getConnection().commit();
        stat.close();
        stat2.close();
        rs.close();
        query.close();
        return true;
    } //*************************************************************************
    /**
     * Inserts atom types.
     * @param group column group number
     * @throws java.sql.SQLException
     */
    public void addAtomTypes( int group ) throws java.sql.SQLException {
if( DEBUG ) System.err.printf( "** add atom types in group %d\n", group );
        String typecol = null;
        String labelcol = null;
        String atomcol = null;
        for( Column c : fModel.getColumns() ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == ColTypes.COMPID ) labelcol = c.getDbName();
                if( c.getType() == ColTypes.ATOMID ) atomcol = c.getDbName();
                if( c.getType() == ColTypes.ATOMTYPE ) typecol = c.getDbName();
            }
        }
if( DEBUG ) System.err.printf( "** labelcol %s, atomcol %s, typecol %s\n", labelcol, atomcol, typecol );
        if( typecol == null )
            throw new NullPointerException( "No atom type column in group " + (group + 1) );
        if( labelcol == null )
            throw new NullPointerException( "No comp ID column in group " + (group + 1) );
        if( atomcol == null )
            throw new NullPointerException( "No atom ID column in group " + (group + 1) );
        StringBuilder sql = new StringBuilder( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET\"" );
        sql.append( typecol );
        sql.append( "\"=(SELECT a.ATYPE FROM ATOMS a,RESIDUES r WHERE a.NAME=" );
        sql.append( fModel.getName() );
        sql.append( ".\"" );
        sql.append( atomcol );
        sql.append( "\" AND r.LABEL=" );
        sql.append( fModel.getName() );
        sql.append( ".\"" );
        sql.append( labelcol );
        sql.append( "\" AND r.TYPEID=" );
        sql.append( fModel.getRestypeId( group ) );
        sql.append( " AND a.RESID=r.ID)" );
        java.sql.Statement stat = fModel.getConnection().createStatement();
if( DEBUG ) System.err.println( sql );
        stat.executeUpdate( sql.toString() );
// unknown atoms (non-standard residues)
        sql.setLength( 0 );
        sql.append( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( typecol );
        sql.append( "\"=SUBSTRING( \"" );
        sql.append( atomcol );
        sql.append( "\" FROM 1 FOR 1 ) WHERE \"" );
        sql.append( typecol );
        sql.append( "\" IS NULL" );
if( DEBUG ) System.err.println( sql );
        stat.executeUpdate( sql.toString() );
        if( ! fModel.getConnection().getAutoCommit() ) fModel.getConnection().commit();
        stat.close();
        fModel.updateTable();
    } //*************************************************************************
    /**
     * Insert atom isotopes.
     * @param group column group
     * @param type atom type
     * @param isotope atom isotope number
     * @throws java.sql.SQLException
     */
    public void addAtomIsotopes( int group, String type, String isotope )
            throws java.sql.SQLException {
        String typecol = null;
        String isocol = null;
        for( Column c : fModel.getColumns() ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == ColTypes.ISOTOPE ) isocol = c.getDbName();
                if( c.getType() == ColTypes.ATOMTYPE ) typecol = c.getDbName();
            }
        }
        if( typecol == null )
            throw new NullPointerException( "No atom type column in group " + (group + 1) );
        if( isocol == null ) return;
// not all tables have isotope column
//            throw new NullPointerException( "No atom isotope column in group " + (group + 1) );
        StringBuilder sql = new StringBuilder( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET\"" );
        sql.append( isocol );
        sql.append( "\"=" );
        sql.append( isotope );
        sql.append( " WHERE \"" );
        sql.append( typecol );
        sql.append( "\"='" );
        sql.append( Utils.quoteForDB( type ) );
        sql.append( '\'' );
        java.sql.Statement stat = fModel.getConnection().createStatement();
if( DEBUG ) System.err.println( sql );
        stat.executeUpdate( sql.toString() );
        if( ! fModel.getConnection().getAutoCommit() ) fModel.getConnection().commit();
        stat.close();
        fModel.updateTable();
    } //*************************************************************************
    /**
     * Move data to author columns.
     * @param group group number
     * @throws java.sql.SQLException
     */
    public void moveAuthorData( int group ) throws java.sql.SQLException {
        String compidxcol = null;
        String authseqcol = null;
        String compidcol = null;
        String authcompcol = null;
        String atomidcol = null;
        String authatomcol = null;
        for( Column c : fModel.getColumns() ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == ColTypes.COMPIDX ) compidxcol = c.getDbName();
                if( c.getType() == ColTypes.ASEQID ) authseqcol = c.getDbName();
                if( c.getType() == ColTypes.COMPID ) compidcol = c.getDbName();
                if( c.getType() == ColTypes.ACOMPID ) authcompcol = c.getDbName();
                if( c.getType() == ColTypes.ATOMID ) atomidcol = c.getDbName();
                if( c.getType() == ColTypes.AATOMID ) authatomcol = c.getDbName();
            }
        }
        if( compidxcol == null )
            throw new NullPointerException( "No comp IDX column in group " + (group + 1) );
        if( authseqcol == null )
            throw new NullPointerException( "No author sequence column in group " + (group + 1) );
        if( compidcol == null )
            throw new NullPointerException( "No comp ID column in group " + (group + 1) );
        if( authcompcol == null )
            throw new NullPointerException( "No author comp ID column in group " + (group + 1) );
        if( atomidcol == null )
            throw new NullPointerException( "No atom ID column in group " + (group + 1) );
        if( authatomcol == null )
            throw new NullPointerException( "No author atom ID column in group " + (group + 1) );
        boolean cmt = fModel.getConnection().getAutoCommit();
        fModel.getConnection().setAutoCommit( false );
        moveColumn( compidxcol, authseqcol );
        moveColumn( compidcol, authcompcol );
        moveColumn( atomidcol, authatomcol );
        fModel.getConnection().setAutoCommit( cmt );
        fModel.updateTable();
    } //*************************************************************************
    /**
     * Move data from srccol to tgtcol.
     * @param srccol source column
     * @param tgtcol target column
     * @throws java.sql.SQLException
     */
    private void moveColumn( String srccol, String tgtcol )
            throws java.sql.SQLException {
        java.sql.Statement stat = fModel.getConnection().createStatement();
        StringBuilder sql = new StringBuilder( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( tgtcol );
        sql.append( "\"=\"" );
        sql.append( srccol );
        sql.append( "\"" );
if( DEBUG ) System.err.println( sql );
        stat.executeUpdate( sql.toString() );
        sql.setLength( 0 );
        sql.append( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( srccol );
        sql.append( "\"=NULL" );
if( DEBUG ) System.err.println( sql );
        stat.executeUpdate( sql.toString() );
        if( ! fModel.getConnection().getAutoCommit() ) fModel.getConnection().commit();
        stat.close();
    } //*************************************************************************
    /**
     * Insert entity assembly ids.
     * @param group column group
     * @param eid entity ID
     * @param aid entity asembly ID
     * @throws java.sql.SQLException
     */
    public void insertEntityAssemblyId( int group, int eid, int aid )
            throws java.sql.SQLException {
if( DEBUG ) System.err.printf( "Insert EA_ID %d, %d, %d\n", group, eid, aid );
        String eidcol = null;
        String aidcol = null;
        for( Column c : fModel.getColumns() ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == ColTypes.ENTITYID ) eidcol = c.getDbName();
                if( c.getType() == ColTypes.ASSYID ) aidcol = c.getDbName();
            }
        }
        if( (eidcol == null) || (aidcol == null) ) {
            fModel.getErrorList().add( new Error( Error.Severity.ERR, -1, "", "", "",
                                                  "Missing entity and/or entity assembly ID column" ) );
            return;
        }
        java.sql.Statement stat = fModel.getConnection().createStatement();
        StringBuilder sql = new StringBuilder( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( aidcol );
        sql.append( "\"=" );
// NOTE: entity ID and entity assembly ID columns are defined as INTEGER in the dictionary.
        sql.append( aid );
        sql.append( " WHERE \"" );
        sql.append( eidcol );
        sql.append( "\"=" );
        sql.append( eid );
if( DEBUG ) System.err.println( sql );
        stat.executeUpdate( sql.toString() );
        if( ! fModel.getConnection().getAutoCommit() ) fModel.getConnection().commit();
        stat.close();
        fModel.updateTable();
    } //*************************************************************************
    /**
     * Insert local ID.
     * @param id ID to insert
     * @throws java.sql.SQLException
     */
    public void insertLocalIds( int id ) throws java.sql.SQLException {
        String idcol = null;
        for( Column c : fModel.getColumns() )
            if( c.getType() == ColTypes.LCLID ) {
                idcol = c.getDbName();
                break;
            }
        if( idcol == null ) {
            fModel.getErrorList().add( new Error( Error.Severity.ERR, -1, "", "", "",
                                                  "Missing local ID column" ) );
            return;
        }
        java.sql.Statement stat = fModel.getConnection().createStatement();
        StringBuilder sql = new StringBuilder( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( idcol );
        sql.append( "\"=" );
        sql.append( id );
if( DEBUG ) System.err.println( sql );
        stat.executeUpdate( sql.toString() );
        if( ! fModel.getConnection().getAutoCommit() ) fModel.getConnection().commit();
        stat.close();
        fModel.updateTable();
    } //*************************************************************************
    public void insertDefaultAmbiCodes( int group ) throws java.sql.SQLException {
        String compidcol = null;
        String atomidcol = null;
        String ambicol = null;
        for( Column c : fModel.getColumns() ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == ColTypes.COMPID ) compidcol = c.getDbName();
                if( c.getType() == ColTypes.ATOMID ) atomidcol = c.getDbName();
                if( c.getType() == ColTypes.AMBICODE ) ambicol = c.getDbName();
            }
        }
        if( compidcol == null )
            throw new NullPointerException( "No comp ID column in group " + (group + 1) );
        if( atomidcol == null )
            throw new NullPointerException( "No atom ID column in group " + (group + 1) );
        if( ambicol == null )
            throw new NullPointerException( "No ambiguity code column in group " + (group + 1) );
        StringBuilder sql = new StringBuilder( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( ambicol );
        sql.append(  "\"=? WHERE \"" );
        sql.append( compidcol );
        sql.append(  "\"=? AND \"" );
        sql.append( atomidcol );
        sql.append(  "\"=?" );
        java.sql.PreparedStatement pstat = fModel.getConnection().prepareStatement( sql.toString() );
        sql.setLength( 0 );
        sql.append( "SELECT DISTINCT \"" );
        sql.append( compidcol );
        sql.append(  "\",\"" );
        sql.append( atomidcol );
        sql.append(  "\" FROM " );
        sql.append( fModel.getName() );
        sql.append( " WHERE \"" );
        sql.append( ambicol );
        sql.append(  "\" IS NULL" );
        java.sql.Statement query = fModel.getConnection().createStatement(
                java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY );
        int typeid = fModel.getRestypeId( group );
        int ambicode;
if( DEBUG )
    System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            ambicode = fModel.getDictionary().getDefaultAmbicode( typeid, rs.getString( 1 ), rs.getString( 2 ) );
            if( ambicode > 0 ) {
                pstat.setInt( 1, ambicode );
                pstat.setString( 2, rs.getString( 1 ) );
                pstat.setString( 3, rs.getString( 2 ) );
if( DEBUG )
    System.err.println( pstat );
                pstat.executeUpdate();
            }
        }
        rs.close();
        query.close();
        if( ! fModel.getConnection().getAutoCommit() ) fModel.getConnection().commit();
        pstat.close();
        fModel.updateTable();
    } //*************************************************************************
    /**
     * Replace ambiguity codes based on dictionary table.
     * @param group
     * @throws java.sql.SQLException
     */
    public void convertAmbiCodes( int group ) throws java.sql.SQLException {
        String compidcol = null;
        String atomidcol = null;
        String ambicol = null;
        String valcol = null;
        for( Column c : fModel.getColumns() ) {
            if( c.getGroupId() == group ) {
                if( c.getType() == ColTypes.COMPID ) compidcol = c.getDbName();
                if( c.getType() == ColTypes.ATOMID ) atomidcol = c.getDbName();
                if( c.getType() == ColTypes.AMBICODE ) ambicol = c.getDbName();
                if( c.getType() == ColTypes.VAL ) valcol = c.getDbName();
            }
        }
        if( compidcol == null )
            throw new NullPointerException( "No comp ID column in group " + (group + 1) );
        if( atomidcol == null )
            throw new NullPointerException( "No atom ID column in group " + (group + 1) );
        if( ambicol == null )
            throw new NullPointerException( "No ambiguity code column in group " + (group + 1) );
        if( valcol == null )
            throw new NullPointerException( "No value column in group " + (group + 1) );
        StringBuilder sql = new StringBuilder( "UPDATE " );
        sql.append( fModel.getName() );
        sql.append( " SET \"" );
        sql.append( ambicol );
        sql.append(  "\"=? WHERE ROW=?" );
        java.sql.PreparedStatement pstat = fModel.getConnection().prepareStatement( sql.toString() );
        sql.setLength( 0 );
        sql.append( "SELECT s.ROW,s.\"" );
        sql.append( valcol );
        sql.append( "\",s.\"" );
        sql.append( ambicol );
        sql.append(  "\",t.ROW,t.\"" );
        sql.append( valcol );
        sql.append( "\",t.\"" );
        sql.append( ambicol );
        sql.append(  "\",c.ORGCODE,c.NEWCODE FROM AMBIREPLACE c JOIN ATOMS a ON " +
                "a.ID=c.ATOMID1 JOIN RESIDUES r ON r.ID=a.RESID JOIN " );
        sql.append( fModel.getName() );
        sql.append( " s ON s.\"" );
        sql.append( compidcol );
        sql.append( "\"=r.LABEL AND s.\"" );
        sql.append( atomidcol );
        sql.append( "\"=a.NAME JOIN ATOMS b ON b.ID=c.ATOMID2 and b.RESID=r.ID JOIN " );
        sql.append( fModel.getName() );
        sql.append( " t ON t.\"" );
        sql.append( compidcol );
        sql.append( "\"=r.LABEL AND t.\"" );
        sql.append( atomidcol );
        sql.append( "\"=b.NAME WHERE s.\"" );
        sql.append( valcol );
        sql.append( "\"=t.\"" );
        sql.append( valcol );
        sql.append( "\" ORDER BY s.ROW" );
//        sql.append( "\" AND s.\"" );
//        sql.append( ambicol );
//        sql.append( "\" IS NOT NULL AND t.\"" );
//        sql.append( ambicol );
//        sql.append( "\" IS NOT NULL ORDER BY s.ROW" );
        java.sql.Statement query = fModel.getConnection().createStatement(
                java.sql.ResultSet.TYPE_FORWARD_ONLY,
                java.sql.ResultSet.CONCUR_READ_ONLY );
if( DEBUG )
    System.err.println( sql );
        java.sql.ResultSet rs = query.executeQuery( sql.toString() );
        while( rs.next() ) {
            rs.getInt( 3 );
            if( rs.wasNull() || (rs.getInt( 3 ) == rs.getInt( 7 )) ) {
                if( rs.getString( 2 ).equals( rs.getString( 5 ) ) ) {
                    pstat.setInt( 1, rs.getInt( 8 ) );
                    pstat.setInt( 2, rs.getInt( 1 ) );
if( DEBUG )
System.err.println( pstat );
                    pstat.executeUpdate();
                }
            }
            if( rs.getInt( 1 ) != rs.getInt( 4 ) ) {
                rs.getInt( 6 );
                if( rs.wasNull() || (rs.getInt( 6 ) == rs.getInt( 7 )) ) {
                    if( rs.getString( 2 ).equals( rs.getString( 5 ) ) ) {
                        pstat.setInt( 1, rs.getInt( 8 ) );
                        pstat.setInt( 2, rs.getInt( 4 ) );
if( DEBUG )
    System.err.println( pstat );
                        pstat.executeUpdate();
                    }
                }
            }
        }
        rs.close();
        query.close();
        if( ! fModel.getConnection().getAutoCommit() ) fModel.getConnection().commit();
        pstat.close();
        fModel.updateTable();
    } //*************************************************************************
//todo
    /**
     * Attempt to match up starting residue in data table with _Entity_comp_index
     * loop.
     * @return starting residue sequence numer
     */
    public int getSequenceStart() {
        return 1;
    } //*************************************************************************
}
