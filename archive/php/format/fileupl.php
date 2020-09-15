<?php
include "globals.inc";
require_once 'functions.php';

global $HTMLINCLUDES;

parse_str( $_SERVER['QUERY_STRING'], $params );
//foreach( array_keys( $params ) as $key ) echo "$key => $params[$key]\n";

$tagcat = trim( $params['tagcat'] );
$filetype = trim( $params['type'] );
if( (strlen( $tagcat ) < 1) || (strlen( $filetype ) < 1) ) {
    echo "Missing parameter!\n";
    return;
}

?>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML>
    <HEAD>
        <TITLE>Data converter</TITLE>
        <link REL="stylesheet" TYPE="text/css" HREF="/stylesheets/main.css" TITLE="main stylesheet">
    </HEAD>
	
    <BODY>
        <CENTER>
	    <?php
                readfile( "$HTMLINCLUDES/xmenu.html" );
	    ?>
	    <TABLE BORDER="0" CELLSPACING="0" ALIGN="LEFT">  <!-- layout table -->
	    <!-- This is a two column table. It displays a side column menu and the main page -->
	        <TR><TD class="sidemenuCol">
		    <?php
		        readfile( "$HTMLINCLUDES/deposit_menu.html" );
		    ?>
		</TD>
								      
		<TD VALIGN="TOP">
		    <TABLE class="content"  BORDER="0" CELLPADDING="4" CELLSPACING="0" ALIGN="LEFT">  <!-- content table -->
		        <TR class="titleAndIcon"><TD class="topContentTitle">
                            <H1>Convert
			        <?php
			            if( $filetype == "xeasyp" ) echo " XEASY";
			            else if( ($filetype == "format4") || ($filetype == "format5") ) echo " delimited";
			            else echo " $filetype ";
				    $datum = fetch_data_type( $tagcat );
				    if( $datum == NULL ) echo " data file";
				    else echo " $datum file";
				?>
			    </H1>
                        </TD></TR>
			
			<TR><TD><STRONG>3</STRONG> Select file(s) to upload and processing options (if any),
			        then click <STRONG>Continue</STRONG>.</TD></TR>
			
			<TR><TD><FORM method="post" action="markcols.php" enctype="multipart/form-data" name="mark_cols">
			    <INPUT type="hidden" name="MAX_FILE_SIZE" value="1048576">
			    <?php
			        if( $filetype == "xeasy" ) {
			            echo "<TABLE border=\"0\">\n";
                                    echo "<TR><TD><LABEL for=\"protbox\">Protons file:</LABEL></TD>\n";
			            echo "<TD><INPUT type=\"file\" name=\"protfile\" id=\"protbox\" size=\"32\"></TD></TR>\n";
                                    echo "<TR><TD><LABEL for=\"seqbox\">Sequence file:</LABEL></TD>\n";
			            echo "<TD><INPUT type=\"file\" name=\"uplfile\" id=\"seqbox\" size=\"32\"></TD></TR>\n";
			            echo "<TR><TD colspan=\"2\"><INPUT type=\"checkbox\" name=\"seqnums\" id=\"chkbox\"> ";
			            echo "<LABEL for=\"chkbox\">Residue sequence numbers in 2nd column of sequence file</LABEL></TD></TR>\n";
			            echo "</TABLE>\n";
			        }
			        else {
			            echo "<P><INPUT type=\"file\" name=\"uplfile\" size=\"32\"><P>";
			            if( $filetype == "xeasyp" ) {
				        echo "<LABEL for=\"seqbox\">Residue sequence string</LABEL><BR>\n";
				        echo "<TEXTAREA rows=\"8\" cols=\"64\" name=\"seq\" id=\"seqbox\"></TEXTAREA><BR>";
				        echo "<LABEL for=\"numbox\">Starting number for residue sequence</LABEL><BR>\n";
				        echo "<INPUT TYPE=\"text\" name=\"num\" id=\"numbox\" size=\"2\" maxlength=\"3\" value=\"1\">";
			            }
			            if( ($filetype == "tab-delimited") || ($filetype == "comma-delimited") )
                                        echo "Skip first <INPUT type=\"text\" name=\"headers\" size=\"2\" maxlength=\"3\" value=\"0\"> lines (header rows)";
				    if( ($filetype == "star3.0") || ($filetype == "star2.1") || ($filetype == "star") ) {
				        echo "<LABEL for=\"loopbox\">Convert loop # </LABEL>\n";
				        echo "Convert loop # <INPUT TYPE=\"text\" name=\"loop\" id=\"loopbox\" size=\"2\" maxlength=\"3\" value=\"1\">";
				        echo " (for output of BMRB table generators: convert loop 2..5)";
				    }
				    if( $filetype == "format4" ) {
				        echo "<P>Input file must contain column names in the first line. First column must contain ";
				        echo "residue labels (1- or 3-letter), second column must contain residue sequence numbers. ";
				        echo "Column names for subsequent columns are atom names, the data is chemical shift values.";
				    }
				    if( $filetype == "nmrview" ) {
				        echo "<LABEL for=\"seqbox\">Residue sequence string (one-letter codes)</LABEL><BR>\n";
				        echo "<TEXTAREA rows=\"8\" cols=\"64\" name=\"seq\" id=\"seqbox\"></TEXTAREA><BR>";
				        echo "<STRONG>Note</STRONG> that residue sequence string above must match the sequence ";
				        echo "in uploaded table exactly. You can use the <EM>starting number</EM> field below to ";
				        echo "adjust for extra residues at the beginning of the sequence (if any). This, however, ";
				        echo "will not handle any gaps and/or extra residues at the end of the sequence.<P>";
				        echo "<LABEL for=\"numbox\">Starting number for residue sequence</LABEL><BR>\n";
				        echo "<INPUT TYPE=\"text\" name=\"num\" id=\"numbox\" size=\"2\" maxlength=\"3\" value=\"1\">";
				    }
				}
			        echo "<input type=\"hidden\" name=\"tagcat\" value=\"$tagcat\">\n";
			        echo "<input type=\"hidden\" name=\"filetype\" value=\"$filetype\">\n";
			    ?>
			    <P>
                            <INPUT type="submit" value="Continue">
                        </FORM></TD></TR>
                    </TABLE>   <!-- END  content table -->
	        </TD></TR>
		<?php
		    readfile( "$HTMLINCLUDES/footer.html" );
		?>
	    </TABLE>  <!-- END  layout table -->
	</CENTER>
    </BODY>
</HTML>
