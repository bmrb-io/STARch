#!/usr/bin/python -u
#

import sys
import os
import sqlite3
import threading

sys.path.append( "/bmrb/lib/python/starobj" )
import utils


class PrintStar( object ) :
    """simplified version of NMR-STAR unparser: print data table aa single loop"""

    _lock = None
    _dbfile = None
    _conn = None
    _curs = None
    _table = None
    _widths = None
    _row = 0
    _header_sent = None
    _footer_sent = None

    _verbose = False

    def __init__( self, filename = None, table = None ) :
        self._dbfile = filename
        self._table = table
        self._lock = threading.Lock()
        self._widths = []
        self._header_sent = False
        self._footer_sent = False

    def _get_dbfile( self ) : 
        return self._dbfile
    def _set_dbfile( self, filename ) :
        self._dbfile = filename
    filename = property( _get_dbfile, _set_dbfile )

    def _get_table( self ) : 
        return self._table
    def _set_table( self, table ) :
        self._table = table
    table = property( _get_table, _set_table )

    def __iter__( self ) :
        return self

    def next( self ) :
        if self._footer_sent :
            self._conn.close()
            os.unlink( self._dbfile )
            self._lock.release()
            raise StopIteration

        if self._header_sent :
            row = self._curs.fetchone()
            self._row += 1
            if row == None :
                self._curs.close()
                self._footer_sent = True
                return "stop_"

            txt = "   "
            i = -1
            if self._verbose : print >> sys.stderr, row
            for col in row :
                i += 1
                if self._verbose : print >> sys.stderr, "** %d : %s %s" % (i,str( col ),self._curs.description[i][0])
                if self._curs.description[i][0] == "Sf_ID" : continue
                val = utils.quote( col )
                if val == "?" : val = "."
                if val[0] == ";" :
                    txt += "\n";
                    txt += val;
                    txt += "\n";
                else :
                    txt += ("%" + str( self._widths[i] ) + "s") % (val)
            txt += "\n"
            return txt

# else send header & run query
        if not os.path.exists( self._dbfile ) : raise UnboundLocalError( "File not found: %s" % (self._dbfile) )
        self._lock.acquire()
        self._conn = sqlite3.connect( self._dbfile )
        self._curs = self._conn.cursor()
        curs = self._conn.cursor()
        self._curs.execute( 'select * from "%s"' % (self._table) ) # not a parameter
        txt = "loop_\n"
        for col in self._curs.description : 
            if col[0] == "Sf_ID" :
                self._widths.append( 2 ) # only doing this to keep the field count right
            else :
                txt += "    _%s.%s\n" % (self._table,col[0])
                sql = 'select max(coalesce(length("%s"),1)) from %s' % (col[0],self._table)
                curs.execute( sql )
                row = curs.fetchone()
                self._widths.append( 2 if (row[0] == 1) else (row[0] + 3) )

        txt += "\n"
        self._header_sent = True
        return txt

#
#
#
#if __name__ == '__main__':
#    print "Move along, citizen"
#
