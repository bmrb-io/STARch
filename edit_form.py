#!/usr/bin/python -u
#
# the form is wrapped in an iterable like the rest of them
#

class EditForm( object ) :
    """shows "edit table" form"""

    _conn = None
    _table = None
    _column = None
    _at_end = None

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
            .editbox {
                margin: 1em;
                padding: 2em; 
                float: left; 
                clear: left; 
                vertical-align: top; 
                border-radius: 5px; 
                -moz-border-radius: 5px; 
                border: thin solid #222266; 
                background-color: #efefef;
                width: 94%;
            }
        </style>
<!--
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
-->
    </head>
    <body>
        <div style="padding-left: 2em; padding-top: 2em;">
        <a style="float: left; padding: 10px; vertical-align: top" href="http://www.bmrb.wisc.edu/">
        <img src="http://www.bmrb.wisc.edu/images/logo.gif" style="border: 0;" height="60" width="56"></a>
"""
    _header2 = """
        </div>
"""

    _footer = """
    </form>
    </body>
</html>
"""

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

    def __iter__( self ) :
        return self

    def next( self ) :
# endpoint
        if self._at_end :
            raise StopIteration

# editing functions
#
        txt = self._header1
        txt += '<h2 style="margin-left: 80px; float: left">Edit %s in %s</h2>\n'  % (self._column,self._table)
        txt += self._header2
        txt += """<p><form method="get" action="update" name="update" id="updatetable"">
<input type="hidden" name="table" value="%s">
<input type="hidden" name="column" value="%s">
""" % (self._table,self._column)

# insert constant
        txt += """<div class="editbox">
<p><label for="ins_const">Insert value:</label> <input type="text" size="10" name="const_val" id="ins_const">
(For example, Entry or Entity ID. Put in a dot (period) and tick the checkbox below to delete existing values.)
<br><input type="checkbox" name="const_ovr" id="ovr_const"> <label for="ovr_const">overwrite existing values, if any</label>
<br><input type="submit" value="Update table">
</div>
"""

# insert sequence

# copy to column
        txt += """<div class="editbox">
<p><label for="copy_col">Copy to column</label>
"""
        sql = """select * from "%s" limit 1""" % (self._table)
        curs = self._conn.cursor()
        curs.execute( sql )
        if len( curs.description ) < 1 : raise Exception( "No columns!" )
        txt += """<select id="copy_col" name="col_copy">
<option value="">-</option>
"""
        for col in curs.description :
            if col == self._table : continue
            txt += """<option value="%s">%s</option>""" % (col[0],col[0])

        txt += """</select> (For example, copy Comp_index_ID to Seq_ID.)
<br><input type="submit" value="Update table">
</div>
"""

        txt += self._footer
        self._at_end = True
        return txt


#
#
#
#if __name__ == '__main__':
#    print "Move along, citizen"
#
