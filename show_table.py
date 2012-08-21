#!/usr/bin/python -u
#

import sys
import sqlite3

class ShowTable( object ) :
    """shows sqlite3 table as html page, wrapped in iterable"""

    _dbfile = None
    _conn = None
    _curs = None
    _row = 0

    _table = None
    _header_sent = None
    _footer_sent = None

    _msg = None

    _header1 = """
<!doctype html public "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en-US">
    <head>
     <title>NMR-STAR table editor</title>
 <!--    <link rel="stylesheet" TYPE="text/css" href="/stylesheets/chem_comp.css" title="main stylesheet"> -->
     <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
     <meta http-equiv="CACHE-CONTROL" content="NO-CACHE">
     <meta http-equiv="PRAGMA" content="NO-CACHE">
     <meta http-equiv="EXPIRES" content="0">
        <style type="text/css">
            .current { font-weight: bold; text-decoration: underline; }
            .col { text-align: left; vertical-align: top; }
        </style>
        <script type="text/javascript" src="http://www.bmrb.wisc.edu/includes/jquery.js"></script>
        <script type="text/javascript">

        $(document).ready( function() {

/* submit form when column header is clicked */
            $( "th.col" ).click( function( event ) {
                var input = $( "<input>" ).attr( "type", "hidden" ).attr( "name", "column" ).val( event.target.id );
                $( "form#editstar" ).append( $( input ) );
                $( "form#editstar" ).submit();
            } );

/* highlight selected Comp ID */
            $( "th.col" ).hover( function() {
                $( this ).addClass( "current" );
            }, function() {
                $( this ).removeClass( "current" );
            } );
        } );
    </script>
    </head>
    <body>
        <div style="padding-left: 2em; padding-top: 2em;">
        <a style="float: left; padding: 10px; vertical-align: top" href="http://www.bmrb.wisc.edu/">
        <img src="http://www.bmrb.wisc.edu/images/logo.gif" style="border: 0;" height="60" width="56"></a>
"""
    _header2 = """
        </div>
        <div style="padding: 2em; float: left; clear: left; vertical-align: top; width: 50%;">
        <p><form method="get" action="print" name="print" id="printstar">
            <input type="submit" value="Make STAR">
        </form>
        <p><form method="get" action="edit" name="edit" id="editstar"> </form>
"""
#    _forms = """
#        <p><form method="get" action="print" name="print" id="printstar">
#            <input type="hidden" name="dbfile" value="%s">
#            <input type="hidden" name="table" value="%s">
#            <input type="submit" value="Make STAR">
#        </form>
#        <p><form method="get" action="edit" name="edit" id="editstar"> 
#            <input type="hidden" name="dbfile" value="%s">
#            <input type="hidden" name="table" value="%s">
#        </form>
#"""
#        <table cellspacing="2" cellpadding="1" style="border-radius: 5px; -moz-border-radius: 5px; border: thin solid #222266; background-color: #cccccc;">
#"""

    _footer = """
         </table>
       </div>
    </body>
</html>
"""

    def __init__( self, dbfile = None, table = None ) :
        self._dbfile = dbfile
        self._table = table
        print >> sys.stderr, self._dbfile, self._table
        self._header_sent = False
        self._footer_sent = False

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
            raise StopIteration

# fetch & send rows
        if self._header_sent :
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
        self._conn = sqlite3.connect( self._dbfile )
        self._curs = self._conn.cursor()
        self._curs.execute( 'select * from "%s"' % (self._table) ) # not a parameter
        txt = self._header1
        txt += '<h2 style="margin-left: 80px; float: left">%s</h2>'  % (self._table)
        txt += self._header2 
#        txt += self._forms % (self._dbfile, self._table, self._dbfile, self._table)
        if self._msg != None :
            txt += "<p>%s</p>" % (self._msg)
            self._msg = None

        txt += '<table cellspacing="2" cellpadding="1" style="border-radius: 5px; -moz-border-radius: 5px; border: thin solid #222266; background-color: #cccccc;">'
        txt += '<tr style="background-color: #ccdeef;">'
        for col in self._curs.description :
            txt += """<th class="col" id="%s">%s</th>""" % (col[0],col[0].replace( "_", "<br>" ))
        txt += "</tr>"
        self._header_sent = True
        return txt


#
#
#
#if __name__ == '__main__':
#    print "Move along, citizen"
#
