importClass(java.lang.System);
importClass(Packages.java.awt.Desktop);
importClass(Packages.au.com.intermine.spacemap.model.NodeType);

FileMenuItems = {
	'Print': print,
	'Dump filenames' : dumpFilenames
};

Collections = {
		'Image Files' : ['bmp','gif','jpeg','jpg','pcm','pgm','png','prt','psd','shp','sid','tif','tiff'],
		'Program Files' : ['exe', 'com', 'bat', 'dll', 'cmd'],
        'Sound Files' : ['3g2','dmr','fls','m4a','mld','mp3','mpc','msv','ogg','ra','wav','wma','wv','wvc'],
        'Video Files' : ['3gp','amc','asf','avi','flv','mcf','mmv','mov','mp4','mpeg','mpg','qt','rm','rmx','ts','vob','wmv'],
        'Document Files' : ['123','cap','doc','dot','eps','fla','htm','html','jaw','jbw','jtd','lwp','pdf','ppt','ps','rtf','sam','sht','trn','txt','vsd','vss','wk4','xls'],                         
        'Compressed Files' : ['arc','arj','b64','cab','dvf','gz','ilk','lzh','pak','pgd','pgp','rar','tar','tgz','tzip','z','zip','7z'],
        'Temporary Files' : ['bak','old','temp','tmp']
}

function print(node, file) {
	if (Desktop.isDesktopSupported()) {
		var desktop = Desktop.getDesktop();
		desktop.print(file);
	} else {
		System.err.println("Desktop not supported");
	}
}

function dumpFilenames(node, file) {
	for (i = 0; i < node.getChildren().size(); ++i) {
		var child = node.getChildren().get(i);
		if (child.getNodeType() == NodeType.File) {
			System.out.println(child.getLabel() + " (" + child.getWeight() + ")");
		}
	}
}
