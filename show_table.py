#!/usr/bin/python -u
#

import sys
import sqlite3
import threading

class ShowTable( object ) :
    """shows sqlite3 table as html snippet, wrapped in iterable"""

    _lock = None
    _dbfile = None
    _conn = None
    _curs = None
    _row = 0
    _table = None
    _query_ready = None
    _footer_sent = None
    _msg = None
    _footer = """
         </table>
       </div>
    </body>
</html>
"""

    def __init__( self, dbfile = None, table = None ) :
        self._lock = threading.Lock()
        self._dbfile = dbfile
        self._table = table
#        print >> sys.stderr, self._dbfile, self._table
        self._footer_sent = False
        self._query_ready = False

    def _get_dbfile( self ) : 
        """sqlite3 db file"""
        return self._dbfile
    def _set_dbfile( self, dbfile ) :
        self._dbfile = dbfile
    dbfile = property( _get_dbfile, _set_dbfile )

    def _get_conn( self ) : 
        """sqlite3 db connection"""
        return self._conn
    def _set_conn( self, connection ) :
        self._conn = connection
    connection = property( _get_conn, _set_conn )

    def _get_table( self ) : 
        """name of db table to display"""
        return self._table
    def _set_table( self, table ) :
        self._table = table
    table = property( _get_table, _set_table )

    def _get_msg( self ) : 
        """status message"""
        return self._msg
    def _set_msg( self, message ) :
        self._msg = message
    status_message = property( _get_msg, _set_msg )


    def __iter__( self ) :
        return self

    def next( self ) :
# endpoint
        if self._footer_sent :
            self._lock.release()
            raise StopIteration

# fetch & send rows
        if self._query_ready :
            row = self._curs.fetchone()
            self._row += 1
            if row == None :
                self._curs.close()
                self._conn.close()
                self._footer_sent = True
                return self._footer

            if self._row % 2 == 0 :
                txt = '<tr style="background-color: #dddddd;">'
            else :
                txt = '<tr style="background-color: #eeeeee;">'
            for col in row :
                if col == None :
                    txt += """<td style="text-align: left;">.</td>"""
                else :
                    txt += """<td style="text-align: left;">%s</td>""" % (col)
            txt += "</tr>"
            return txt

# else send header & run query
        self._lock.acquire()
        self._conn = sqlite3.connect( self._dbfile )
        self._curs = self._conn.cursor()
        self._curs.execute( 'select * from "%s"' % (self._table) ) # not a parameter
        txt = """
<table cellspacing="2" cellpadding="1" style="border-radius: 5px; -moz-border-radius: 5px; border: thin solid #222266; background-color: #cccccc;">
<tr style="background-color: #ccdeef;">"""
        for col in self._curs.description :
            txt += """<th class="col" id="%s">%s</th>""" % (col[0],col[0].replace( "_", "<br>" ))
        txt += "</tr>"
        self._query_ready = True
        return txt

#
#
#
#if __name__ == '__main__':
#    print "Move along, citizen"
#
