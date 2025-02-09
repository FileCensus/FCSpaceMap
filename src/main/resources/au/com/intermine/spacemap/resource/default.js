importClass(java.lang.System);
importClass(Packages.java.awt.Desktop);
importClass(Packages.au.com.intermine.spacemap.model.NodeType);

FileMenuItems = {
	'Print': print,
	'Dump filenames' : dumpFilenames
};

Collections = {
    'AI': ['onnx','tflite','pt','pth','gguf','pb','h5','ckpt','safetensors','bin','mlmodel','pmml','pkl','weights','pbtxt','meta','torchscript','joblib','savedmodel','paddle','npz','keras','ggml','ggjt','ggml_v3','fp16','int4','int8','q4_k_m','q6_k','q8_0','rwkv','llama','bin','para','model','tokenizer'],
    'AR_VR': ['usdz','glb','gltf','fbx','vrml','x3d','obj','abc','usd','blend','ma','wrl','3dm','unity','unitypackage','usda','usdc','arproj','reality'],
    'Block': ['iso','img','vhdx','vhd','vmdk','qcow2','vdi','zvhd2','raw'],
    'Cad': ['3mf','stl','step','iges','dwg','dxf','skp','f3d','3dm','sldprt','sldasm','glb','gltf','svg'],
    'Code': ['py','js','ts','go','rs','cpp','h','java','cs','rb','php','swift','kotlin','sql','r','jl','css','scss','less','html','htm','json','yaml','xml','sh','bat','ps1','vbs'],
    'Compressed': ['zip','7z','rar','tar','gz','xz','bz2','zst','br','lz4'],
    'Crypto': ['wallet','key','pem','crt','csr','p12','jks','keystore','bip39','dat','aes','gpg','kbd','seed','ssk','ppk','acsm'],
    'Databases': ['sql','sqlite','json','jsonl','csv','parquet','avro','xml','yaml','toml','db','mdf'],
    'Documents': ['pdf','docx','xlsx','pptx','ppt','doc','xls','odf','md','tex','epub','odt','rtf','txt','ods','odp','pages','numbers','key','html','htm','xhtml','dotx','potx','xlsm','pptm','docm'],
    'Email': ['eml','msg','mbox','pst','ost','ics','vcf','dbx','nsf'],
    'Fonts': ['ttf','otf','woff','woff2','eot','svg','ufo','pfa','pfb','sfd','bdf','pcf','snf','dfont','ttc'],
    'Game': ['pkg','pak','bsp','map','sav','unity3d','ue4','asset','rpg','rom','n64','gba','nds','3ds','iso','cso','pkg','gbasav','flt','wad','pak','bsa','vol'],
    'Images': ['png','jpeg','jpg','webp','heif','avif','svg','psd','raw','cr3','arw','bmp','tiff','mxf','ai','eps','indd','xd','dng','cr2','nef','raf','orf','erf','sr2','pef','rw2','arw','crw','dcr','k25','kdc','mrw','mef','mos','ptx','r3d','x3f','3fr','heic','psdt','psb'],
    'Logs': ['log','logx','logs','zlog','loglz4','logrotate','evtx','audit','journald','syslog','access','error','debug','trace','nlog','glog','elog','perflog','apm','metrics','elk','splunk','kibana'],
    'Programs': ['exe','dll','so','dylib','app','apk','ipa','wasm','msi','appx','jar','sys'],
    'Audio': ['mp3','aac','flac','ogg','opus','m4a','wav','aiff','wma'],
    'Shader': ['hlsl','glsl','cg','vert','frag','comp','tesc','tese','geom','spvasm','metal','shadergraph','surfshader','materialx'],
    'Temporary': ['tmp','temp','bak','old','swp','crdownload','part','cache','dmp','$$$','~','gid','chk','wbk','prv','partial','download','suspend','ctx','stackdump','grunt','lock','dpkg-new','dpkg-old','rpm-new','rpm-old'],
    'Video': ['mp4','mkv','webm','av1','hevc','mov','m4v','mpg','avi'],
    'Container': ['yaml','dockerignore','dockerfile','cue','hcl','tfstate','helm','k8s','compose']
};

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
