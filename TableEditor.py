#!/usr/bin/python -u
#
#

class edit( object ) :
    """editing functions"""

    _table = None
    _column = None
    _conn = None

    _verbose = True

    def __init__( self, connection = None, table = None, column = None ) :
        self._conn = connection
        self._column = column
        self._table = table

    def _get_conn( self ) :
        """sqlite3 db connection"""
        return self._conn
    def _set_conn( self, connection ) :
        self._conn = connection
    connection = property( _get_conn, _set_conn )

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
        if self._verbose : print sql, value
        curs = self._conn.cursor()
        if value == "." : curs.execute( sql )
        else : curs.execute( sql, (value,) )
        rc = curs.rowcount
        if self._verbose : print rc
        curs.close()
        self._conn.commit()
        return rc

    def insert_numbers( self, startat = 1, overwrite = False ) :
        """insert sequence of numbers"""
        sql = """update "%s" set "%s"=?""" % (self._table,self._column)
        sql += " where rowid=?"
        if not overwrite :
            sql += """ and ("%s" is null or "%s"='.' or "%s"='?')""" % (self._column,self._column,self._column)
        if self._verbose : print sql,
        curs = self._conn.cursor()
        curs2 = self._conn.cursor()
        curs2.execute( """select rowid from "%s" order by rowid""" % (self._table) )
        i = int( startat )
        cnt = 0
        while True :
            row = curs2.fetchone()
            if row == None : break
            curs.execute( sql, (i,row[0],) )
            i += 1
            cnt += 1
        if self._verbose : print i
        curs2.close()
        curs.close()
        self._conn.commit()
        return cnt


    def copy_column( self, to_column = None, overwrite = True ) :
        """copy values to another column"""
        if to_column == None : raise UnboundLocalError( "copy_column called without argument" )
        sql = """select * from "%s" limit 1""" % (self._table)
        curs = self._conn.cursor()
        curs.execute( sql )
        found = False
        for col in curs.description :
            if col[0] == to_column :
                found = True
                break
        if not found : raise Exception( "Column not found: %s" % (to_column) )
        sql = """update "%s" set "%s"="%s" """ % (self._table,to_column,self._column)
        if self._verbose : print sql,
        curs.execute( sql )
        rc = curs.rowcount
        if self._verbose : print rc
        curs.close()
        self._conn.commit()
        return rc

#
#
#
#if __name__ == '__main__':
#    print "Move along, citizen"
#
