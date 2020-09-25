#!/usr/bin/perl -w
#
#

use strict;
use English;
use Getopt::Std;
use File::Basename;

my %RES = ( "ALA" => "A", "ARG" => "R", "ASP" => "D", "ASN" => "N",
            "CYS" => "C", "GLU" => "E", "GLN" => "Q", "GLY" => "G",
            "HIS" => "H", "ILE" => "I", "LEU" => "L", "LYS" => "K",
            "MET" => "M", "PHE" => "F", "PRO" => "P", "SER" => "S",
            "THR" => "T", "TRP" => "W", "TYR" => "Y", "VAL" => "V" );

my %opts;
getopts( "s:p:o:", \%opts );

if( ! (defined( $opts{"p"} ) && defined( $opts{"s"} )) ) { 
    die "Usage: $0 <-s seq file> <-p prot file> [-o output file]\n"; 
}

my $outfile;
if( defined( $opts{"o"} ) ) { $outfile = $opts{"o"}; }
else {
  ($outfile, undef, undef) = fileparse( $opts{"s"}, qr/\..*/ );
  $outfile .= ".tab";
}

my %residues;
my $line;
my @fields = ();
open( SEQ, $opts{"s"} ) or die "Cannot open seq file " . $opts{"s"} . ": $OS_ERROR\n";
while( $line = <SEQ> ) {
    if( $line =~ /^\s*$/ ) { next; }
    chomp( $line );
    $line =~ s/^\s+//;
    $line =~ s/\s+$//;
    @fields = split( /\s+/, $line );
    $residues{$fields[1]} = $fields[0];
}
close( SEQ );

#foreach $line( sort( { $a <=> $b } keys( %residues ) ) ) { print $line . "->" . $residues{$line} . "\n"; }

open( TAL, ">$outfile" )  or die "Cannot create $outfile: $OS_ERROR\n";
print TAL "REMARK generated from XEASY files " . $opts{"s"} . " and " . $opts{"p"} . "\n\n";
print TAL "DATA SEQUENCE ";
foreach $line( sort( { $a <=> $b } keys( %residues ) ) ) { print TAL $RES{$residues{$line}}; }
print TAL "\n\nVARS   RESID RESNAME ATOMNAME SHIFT\n";
print TAL "FORMAT %4d   %1s     %4s      %8.3f\n\n";
open( PROT, $opts{"p"} ) or die "Cannot open prot file " . $opts{"p"} . ": $OS_ERROR\n";
while( $line = <PROT> ) {
    if( $line =~ /^\s*$/ ) { next; }
    chomp( $line );
    $line =~ s/^\s+//;
    $line =~ s/\s+$//;
    @fields = split( /\s+/, $line );
    printf TAL "%4d   %1s     %4s      %8.3f\n", $fields[4], $RES{$residues{$fields[4]}}, $fields[3], $fields[1];
}
close( PROT );
close( TAL );
exit( 0 );
