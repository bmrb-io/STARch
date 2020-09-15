<?php
include "globals.inc";
require_once 'functions.php';

global $HTMLINCLUDES;

parse_str( $_SERVER['QUERY_STRING'], $params );
//foreach( array_keys( $params ) as $key ) echo "$key => $params[$key]\n";

$tagcat = trim( $params['tagcat'] );
if( strlen( $tagcat ) < 1 ) {
    echo "Missing parameter!\n";
    return;
}

$filetypes = get_file_types( $tagcat );
//foreach( array_keys( $filetypes ) as $key ) echo "$key => $filetypes[$key]\n";
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
				    $datum = fetch_data_type( $tagcat );
				    if( $datum == NULL ) echo " data file";
				    else echo "$datum file";
				?>
			    </H1>
                        </TD></TR>
			
			<TR><TD><STRONG>2</STRONG> Select file type and click <STRONG>Continue</STRONG>.<BR>
			  (Click on the name for help/example)</TD></TR>
			
			<TR><TD><FORM method="get" name="upload_file" action="fileupl.php">
<?php
foreach( array_keys( $filetypes ) as $key ) echo "<input type=\"radio\" name=\"type\" value=\"$key\">$filetypes[$key]<br>\n";
echo "<p><input type=\"hidden\" name=\"tagcat\" value=\"$tagcat\">\n";
?>
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
