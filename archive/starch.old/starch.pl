#!PERLPATH_REPLACEME
#
# CGI front-edn to STARch
#

use CGI qw/-no_xhtml :standard/;
use strict;
use English;

my $JAVA="JAVAPATH_REPLACEME";

srand( time ^ $$ );
my $tmpnum = int( rand 99999999 );
my $tmpname = "";
my $handle;
my $path = $ENV{'SCRIPT_FILENAME'};
$path =~ s/\/[^\/]+$//g;
$path = "$path/tmp";

# check parameters
#
if( (param( 'informat' ) eq "") || (param( 'file1' ) eq "") || (param( 'outformat' ) eq "") ) {
# HTML header
#
    print header,
          start_html(-lang=>'en-US', -title=>"Conversion error" ),
          "\n";

    print p,
          strong( "Required parameter missing! Use your browser's Back button to return to STARch page." ),
	  "\n";
	  
# HTML footer
#
    print end_html;
    print "\n";
}
else {
    if( ((param( 'informat' ) eq "XEASY") || (param( 'informat' ) eq "XEASY_N")) && (param( 'file2' ) eq "") ) {
# HTML header
#
        print header,
              start_html(-lang=>'en-US', -title=>"Conversion error" ),
              "\n";

        print p,
	      strong( "Need two files for XEASY conversion! Use your browser's Back button to return to STARch page." ),
	  "\n";
	  
# HTML footer
#
        print end_html;
        print "\n";
    }
    else {
        my $infile = $path . "/" . $tmpnum;
        if( ((param( 'informat' ) eq "XEASY") ||  (param( 'informat' ) eq "XEASY_N"))
	   && (param( 'file2' ) ne "") ) {
	    save_file( param( 'file1' ), $infile . ".prot" );
	    save_file( param( 'file2' ), $infile . ".seq" );
     	}
	else { save_file( param( 'file1' ), $infile ); }
	my $inf = param( 'informat' );
	my $outf;
	if( param( 'outformat' ) eq "NMR-STAR 3.0" ) { $outf = "-3"; }
	elsif ( param( 'outformat' ) eq "CSV" ) { $outf = "-c"; }
	elsif( param( 'outformat' ) eq "XEASY" ) { $outf = "-x"; }
	my $restype;
	if( param( 'restype' ) < 0 ) { $restype = ""; }
	else { $restype = "-r " . param( 'restype' ); }
	my $cmd = "$JAVA -cp CLASSPATH_REPLACEME EDU.bmrb.starch.Main $restype -f $inf -i $infile -v $outf 2>&1";
	$ENV{'CLASSPATH'} = "CLASSPATH_REPLACEME";

# text/plain header
#
        print header('text/plain'),
              "\n";
#print "format: " . param('informat') . " file2 " . param('file2') . "\n";
#print $cmd . "\n";
#print `ls ./tmp`;
	open( CONV,  "$cmd |" ) or die "Cannot read command output: $OS_ERROR\n";
	while( <CONV> ) { print; }
	close(CONV);
        if( ((param( 'informat' ) eq "XEASY") ||  (param( 'informat' ) eq "XEASY_N"))
	   && (param( 'file2' ) ne "") ) {
            unlink $infile . ".seq";
            if( -e $infile . ".xeasy" ) { unlink $infile . ".xeasy"; }
            if( -e $infile . ".prot" ) { unlink $infile . ".prot"; }
	}
	else { unlink $infile; }
    }
}

exit 0;

					     

#
# save uploaded file
#
sub save_file( @ ) {
    my( $srcname, $dstname ) = @_;
#print header, start_html, "Saving " . $srcname . " as " . $dstname . "\n";
    open( OUT, ">$dstname" ) or die "Cannot write to $dstname: $OS_ERROR\n";
    while( <$srcname> ) {
        print OUT;
    }
    close( OUT );
}

exit 0;


