#!/usr/bin/python -u
#
# the form is wrapped in an iterable like the rest of them
#

import threading
import sqlite3

class EditForm( object ) :
    """shows "edit table" form"""

    _lock = None
    _dbfile = None
    _conn = None
    _table = None
    _column = None
    _at_end = None


    _footer = """
    </form>
    </body>
</html>
"""

    def __init__( self, dbfile = None, table = None, column = None ) :
        self._dbfile = dbfile
        self._table = table
        self._column = column
        self._lock = threading.Lock()

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
            self._lock.release()
            raise StopIteration

# this is probably an overkill
        self._lock.acquire()
# editing functions
#
# insert constant
        txt = """<div class="editbox">
<label for="ins_const">Insert value:</label> <input type="text" size="10" name="const_val" id="ins_const">
(For example, Entry or Entity ID. Put in a dot (period) and tick the checkbox below to delete existing values.)
<br><input type="checkbox" name="const_ovr" id="ovr_const"> <label for="ovr_const">overwrite existing values, if any</label>
<br><input type="submit" name="insert_constant" value="Update table">
</div>
"""

# insert sequence
        txt += """<div class="editbox">
<label for="ins_nums">Insert sequence of numbers starting at:</label> 
<input type="text" size="10" name="start_val" id="ins_nums" value="1"> (For example, row IDs. Starting number can be negative.)
<br><input type="checkbox" name="nums_ovr" id="ovr_nums" checked> <label for="ovr_nums">overwrite existing values, if any</label>
<br><input type="submit" name="insert_numbers" value="Update table">
</div>
"""

# copy to column
        txt += """<div class="editbox">
<p><label for="copy_col">Copy to column</label> """
        sql = """select * from "%s" limit 1""" % (self._table)


        conn = sqlite3.connect( self._dbfile )
        curs = conn.cursor()
        curs.execute( sql )
        if len( curs.description ) < 1 : raise Exception( "No columns!" )
        txt += """<select id="copy_col" name="col_copy">"""
        for col in curs.description :
            if col == self._table : continue
            txt += """<option value="%s">%s</option>""" % (col[0],col[0])

        curs.close()
        conn.close()
        txt += """</select> (For example, copy Comp_index_ID to Seq_ID.)
<br><input type="submit" name="copy_column" value="Update table">
</div>
"""

# insert residue sequence
        txt += """<div class="editbox">
<label for="res_seq">Insert residue codes from sequence string:</label> 
<br><textarea rows="5" cols="80" name="sequence" id="res_seq"> </textarea> 
<br><label for="seq_start">starting at:</label> 
<input type="text" size="10" name="start_seq" id="seq_start" value="1"> 
<br>Non-standard residue can be specified as <em>(ID)</em> (i.e. Comp_ID in parentheses), starting number can be negative.
<br><input type="submit" name="insert_sequence" value="Update table">
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
