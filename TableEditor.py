#!/usr/bin/python -u
#
#

import sys
import sqlite3
import psycopg2

class edit( object ) :
    """editing functions"""

    _dbfile = None
    _table = None
    _column = None
    _ccdb = None

    _verbose = True

    def __init__( self, dbfile = None, table = None, column = None, ccdb = None ) :
        self._dbfile = dbfile
        self._column = column
        self._table = table
        self._ccdb = ccdb

    def _get_dbfile( self ) :
        """sqlite3 db filename"""
        return self._dbfile
    def _set_dbfile( self, dbfile ) :
        self._dbfile = dbfile
    dbfile = property( _get_dbfile, _set_dbfile )

    def _get_col( self ) : 
        """column (tag) name"""
        return self._column
    def _set_col( self, column ) :
        self._column = column
    column = property( _get_col, _set_col )

    def _get_table( self ) : 
        """name of db table to display"""
        return self._table
    def _set_table( self, table ) :
        self._table = table
    table = property( _get_table, _set_table )

    def _get_ccdb( self ) : 
        """chem comp database DSN (postgres)"""
        return self._ccdb
    def _set_ccdb( self, ccdb ) :
        self._ccdb = ccdb
    ccdb = property( _get_ccdb, _set_ccdb )

# functions
    def insert_value( self, value = None, overwrite = False ) :
        """insert a constant"""
        if value == None : raise UnboundLocalError( "insert_value called without argument" )
        if value == "." :
            sql = """update "%s" set "%s"=NULL""" % (self._table,self._column)
        else :
            sql = """update "%s" set "%s"=?""" % (self._table,self._column)
        if not overwrite :
            sql += """ where ("%s" is null or "%s"='.' or "%s"='?')""" % (self._column,self._column,self._column)
        if self._verbose : print >> sys.stderr, sql, value
        conn = sqlite3.connect( self._dbfile )
        curs = conn.cursor()
        if value == "." : curs.execute( sql )
        else : curs.execute( sql, (value,) )
        rc = curs.rowcount
        if self._verbose : print >> sys.stderr, rc
        curs.close()
        conn.commit()
        conn.close()
        return "%d rows updated" % (rc)

#
    def insert_numbers( self, startat = 1, overwrite = False ) :
        """insert sequence of numbers"""
        sql = """update "%s" set "%s"=?""" % (self._table,self._column)
        sql += " where rowid=?"
        if not overwrite :
            sql += """ and ("%s" is null or "%s"='.' or "%s"='?')""" % (self._column,self._column,self._column)
        if self._verbose : print >> sys.stderr, sql,
        conn = sqlite3.connect( self._dbfile )
        curs = conn.cursor()
        curs2 = conn.cursor()
        curs2.execute( """select rowid from "%s" order by rowid""" % (self._table) )
        i = int( startat )
        cnt = 0
        while True :
            row = curs2.fetchone()
            if row == None : break
            if self._verbose : print >> sys.stderr, sql, i, row[0]
            curs.execute( sql, (i,row[0],) )
            i += 1
            cnt += 1
        if self._verbose : print >> sys.stderr, i
        curs2.close()
        curs.close()
        conn.commit()
        conn.close()
        return "%d rows updated" % (cnt)

#
    def copy_column( self, to_column = None, overwrite = True ) :
        """copy values to another column"""
        if to_column == None : raise UnboundLocalError( "copy_column called without argument" )
        sql = """select * from "%s" limit 1""" % (self._table)
        conn = sqlite3.connect( self._dbfile )
        curs = conn.cursor()
        curs.execute( sql )
        found = False
        for col in curs.description :
            if col[0] == to_column :
                found = True
                break
        if not found : raise Exception( "Column not found: %s" % (to_column) )
        sql = """update "%s" set "%s"="%s" """ % (self._table,to_column,self._column)
        if self._verbose : print >> sys.stderr, sql,
        curs.execute( sql )
        rc = curs.rowcount
        if self._verbose : print >> sys.stderr, rc
        curs.close()
        conn.commit()
        conn.close()
        return "%d rows updated" % (rc)

#
    def insert_residues( self, sequence = None, startat = 1 ) :
        """insert residue codes from sequence string"""
        if sequence == None : raise UnboundLocalError( "insert_residues called without argument" )

        codes = []
        while len( sequence ) > 0 :
            if sequence[0] != "(" : 
                codes.append( sequence[0].upper() )
                sequence = sequence[1:]
            else :
                code = ""
                sequence = sequence[1:] # pop "("
                while sequence[0] != ")" :
                    code += sequence[0]
                    sequence = sequence[1:]
                sequence = sequence[1:] # pop ")"
                codes.append( code.upper() )


        sql = """update "%s" set "Comp_ID"=? where "Comp_index_ID"=?""" % (self._table)

        conn = sqlite3.connect( self._dbfile )
        curs = conn.cursor()
        cnt = 0
        num = int( startat )
        for code in codes :
            if self._verbose : print >> sys.stderr, sql, code, num
            curs.execute( sql, (code, num,) )
            num += 1
            cnt += 1
        curs.close()
        conn.commit()
        conn.close()
        return "%d rows updated" % (cnt)

#
#
#
#if __name__ == '__main__':
#    print "Move along, citizen"
#
