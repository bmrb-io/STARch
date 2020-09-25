#!/usr/bin/python -u
#

import re
import threading

class SendFile( object ) :
    """wraps file in an iterable with simple pattern substitution"""
    _filename = None
    _fp = None
    _map = None
    _lock = None

    def __init__( self, filename = None ) :
        self._filename = filename
        self._lock = threading.Lock()

    def _get_filename( self ) : 
        return self._filename
    def _set_filename( self, filename ) :
        self._filename = filename
    filename = property( _get_filename, _set_filename )

    def replace( self, pattern, replacement ) :
        """add pattern-replacement pair to apply to the file we send"""
        if self._map == None : self._map = {}
        self._map[pattern] = replacement

    def __iter__( self ) :
        return self

    def next( self ) :
        if self._fp == None :
            self._lock.acquire()
            self._fp = open( self._filename )
        b = self._fp.read( 16384 )
        if not b :
            self._fp.close()
            self._lock.release()
            raise StopIteration
        else :
            if self._map != None :
                for pat in self._map.keys() :
                    b = re.sub( pat, self._map[pat], b )
            return b

#
#
#
#if __name__ == '__main__':
#    print "Move along, citizen"
#
