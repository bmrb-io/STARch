#!/usr/bin/python -u
#


import os
import sys
import itertools
import warnings
import werkzeug.routing
import werkzeug.wrappers
import werkzeug.exceptions

import sqlite3

import ConfigParser

sys.path.append( "/bmrb/lib/python" )
from sans.lexer import STARLexer
from sans.handlers import ErrorHandler, ContentHandler

_HERE = os.path.realpath( os.path.split( __file__ )[0] )
sys.path.append( _HERE )
import LoopParser
import TableEditor
import send_file
import show_table
import edit_form
import print_star

CONFIG = "%s/tabedit.conf" % (_HERE)

class TableEdit( object ) :

    _map = None
    _props = None
    _verbose = True

    def __init__( self, conffile = None ) :

        if conffile == None : raise UnboundLocalError( "Missing config file" )
        if len( conffile.strip() ) < 1 : raise UnboundLocalError( "Missing config file" )
        if not os.path.exists( conffile ) : raise UnboundLocalError( "Config file not found: %s" % (conffile) )
        self._props = ConfigParser.SafeConfigParser()
        self._props.read( conffile )

        self._map = werkzeug.routing.Map( [
            werkzeug.routing.Rule( "/", endpoint = "new" ),
            werkzeug.routing.Rule( "/upload", endpoint = "upload" ),
            werkzeug.routing.Rule( "/edit", endpoint = "edit" ),
            werkzeug.routing.Rule( "/update", endpoint = "update" ),
            werkzeug.routing.Rule( "/print", endpoint = "print" ),
            werkzeug.routing.Rule( "/help", endpoint = "help" ),
            werkzeug.routing.Rule( "/help/", endpoint = "help" )
        ] )


#
    def dispatch_request( self, request ) :
        if self._verbose : print >> sys.stderr, request.environ
        adapter = self._map.bind_to_environ( request.environ )
        try :
            endpoint, values = adapter.match()
            return getattr( self, "on_" + endpoint )( request, **values )
        except werkzeug.exceptions.HTTPException, e :
            return e

#
    def wsgi_app( self, environ, start_response ) :
        request = werkzeug.wrappers.Request( environ )
        response = self.dispatch_request( request )
        return response( environ, start_response )

#
    def __call__( self, environ, start_response ) :
        return self.wsgi_app( environ, start_response )

#
# url handlers
#

# TFM: just a static file
#
    def on_help( self, request ) :
        """show help file"""
        s = send_file.SendFile()
        s.filename = os.path.realpath( self._props.get( "wsgi", "html_files" ) + "/help.html" )
        return werkzeug.wrappers.Response( s, status = 200, content_type = "text/html" )
#        return redirect( "http://www.bmrb.wisc.edu" )


# index: upload form
#
    def on_new( self, request, **values ) :
        """show file upload form"""

        s = send_file.SendFile()
        baseurl = request.environ["SCRIPT_NAME"]
        if baseurl == "/" : baseurl = ""
        else : baseurl = baseurl.rstrip( "/" )
        s.replace( "<!-- baseurl -->", baseurl )
        s.filename = os.path.realpath( self._props.get( "wsgi", "html_files" ) + "/index.html" )
        response = werkzeug.wrappers.Response( s, status = 200, content_type = "text/html" )
        return response

# upload
#
    def on_upload( self, request ) :
        """read uploaded file into sqlite3 table and display it"""
#FIXME: read from ccdb if available
        dictdsn = self._props.get( "main", "dictionary_dsn" )
        if not os.path.exists( dictdsn ) : raise UnboundLocalError( "Dictionary missing" )
        with warnings.catch_warnings() :
            warnings.simplefilter( "ignore", RuntimeWarning ) # yes I know about tempnam thankyouverymuch
            dbfile = os.tempnam( self._props.get( "main", "data_files" ) )
        dbfile = os.path.realpath( dbfile )
        conn = sqlite3.connect( dbfile )
        curs = conn.cursor()
        curs.execute( "attach database '%s' as dict" % (dictdsn) )
        curs.close()

        l = STARLexer( request.files["filename"].stream )
        h = LoopParser.handler( conn = conn )
        p = LoopParser.parser2( l, h, h )
        p.parse()
        conn.close()
        if h.hasErrors() :
            os.unlink( dbfile )
            return werkzeug.wrappers.Response( h.errors, status = 200, content_type = "text/plain" )

        r = send_file.SendFile()
        baseurl = request.environ["SCRIPT_NAME"]
        if baseurl == None : baseurl = ""
        elif baseurl == "/" : baseurl = ""
        else : baseurl = baseurl.rstrip( "/" )
        r.replace( "<!-- baseurl -->", baseurl )
        r.replace( "<!-- page title -->", h.table )
        r.replace( "<!-- sqlite3 file -->", dbfile )
        r.replace( "<!-- sqlite3 table -->", h.table )
        r.replace( "<!-- status message -->", "" )
        r.filename = os.path.realpath( self._props.get( "wsgi", "html_files" ) + "/table.hdr" )

        s = show_table.ShowTable( dbfile = dbfile, table = h.table )
        response = werkzeug.wrappers.Response( itertools.chain( r, s ), status = 200, content_type = "text/html" )
        return response

#
#
    def on_print( self, request ) :
        """unparse sqlite3 table to NMR-STAR and display as text/plain"""
        s = print_star.PrintStar( filename = request.args["dbfile"], table = request.args["table"] )
        response = werkzeug.wrappers.Response( s, status = 200, content_type = "text/plain" )
        return response

# edit form
#
    def on_edit( self, request ) :
        """show edit form"""
        r = send_file.SendFile()
        baseurl = request.environ["SCRIPT_NAME"]
        if baseurl == None : baseurl = ""
        elif baseurl == "/" : baseurl = ""
        else : baseurl = baseurl.rstrip( "/" )
        r.replace( "<!-- baseurl -->", baseurl )
        r.replace( "<!-- page title -->", "Edit %s : %s" % (request.args["table"], request.args["column"]) )
        r.replace( "<!-- sqlite3 file -->", request.args["dbfile"] )
        r.replace( "<!-- sqlite3 table -->", request.args["table"] )
        r.replace( "<!-- sqlite3 column -->", request.args["column"] )
        r.filename = os.path.realpath( self._props.get( "wsgi", "html_files" ) + "/edit.hdr" )

        s = edit_form.EditForm( dbfile = request.args["dbfile"], table = request.args["table"], column = request.args["column"] )
        return werkzeug.wrappers.Response( itertools.chain( r, s ), status = 200, content_type = "text/html" )

# updater
#
    def on_update( self, request, **values ) :
        """run editing function(s) and show table"""

        if self._verbose : print >> sys.stderr, "ARGS", request.args, "\n"
        if self._verbose : print >> sys.stderr, "VALS", values, "\n"

        if self._verbose : print >> sys.stderr, self._props.get( "main", "ccdb_dsn" )

        e = TableEditor.edit( dbfile = request.args["dbfile"], table = request.args["table"], column = request.args["column"] )
        e.ccdb = self._props.get( "main", "ccdb_dsn" )
        rc = None

        if "insert_constant" in request.args.keys() : 
            ovr = False
            if ("const_ovr" in request.args.keys()) and (request.args["const_ovr"] == "on") : ovr = True
            val = request.args["const_val"].strip()
            if len( val ) > 0 : rc = e.insert_value( value = val, overwrite = ovr )
            else : rc = "Missing parameter"

        elif "insert_numbers" in request.args.keys() : 
            ovr = False
            if ("nums_ovr" in request.args.keys()) and (request.args["nums_ovr"] == "on") : ovr = True
            val = request.args["start_val"].strip()
            try :
                int( val )
                rc = e.insert_numbers( startat = val, overwrite = ovr )
            except ValueError :
                rc = "Not a number: %s" % (val)

        elif "copy_column" in request.args.keys() :
            val = request.args["col_copy"].strip()
            if len( val ) > 0 : rc = e.copy_column( to_column = val )
            else : rc = "Missing parameter"

        elif "insert_sequence" in request.args.keys() :
            seq = request.args["sequence"].strip().replace( "\n", "" )
            if len( seq ) < 1 : rc = "Missing sequence"
            else :
                start = request.args["start_seq"].strip()
                try :
                    int( start )
                    rc = e.insert_residues( sequence = seq, startat = start )
                except ValueError :
                    rc = "Not a number: %s" % (start)


        else : rc = "No such function!"

        r = send_file.SendFile()
        baseurl = request.environ["SCRIPT_NAME"]
        if baseurl == None : baseurl = ""
        elif baseurl == "/" : baseurl = ""
        else : baseurl = baseurl.rstrip( "/" )
        r.replace( "<!-- baseurl -->", baseurl )
        r.replace( "<!-- page title -->", request.args["table"] )
        r.replace( "<!-- sqlite3 file -->", request.args["dbfile"] )
        r.replace( "<!-- sqlite3 table -->", request.args["table"] )
        if rc != None :
            r.replace( "<!-- status message -->", "%s: %s" % (request.args["column"], rc) )
        r.filename = os.path.realpath( self._props.get( "wsgi", "html_files" ) + "/table.hdr" )

        s = show_table.ShowTable( request.args["dbfile"], table = request.args["table"] )
        response = werkzeug.wrappers.Response( itertools.chain( r, s ), status = 200, content_type = "text/html" )
        return response


#
# wsgi starting point
#

application = TableEdit( CONFIG )

#
#
#
if __name__ == '__main__':

    from werkzeug.serving import run_simple
    from werkzeug.debug import DebuggedApplication
    app = TableEdit( CONFIG )
    run_simple( '127.0.0.1', 5000, app, use_debugger = True, use_reloader = True )
