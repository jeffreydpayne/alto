function(doc, meta){
	var keyTokens = meta.id.split("#");
	if (keyTokens.length() == 3) {
		if (keyTokens[1] == $keyNameSpace) {
			emit(meta.id, meta.id);
		}
	}
	else {
		if (keyTokens[0] == $keyNameSpace) {
			emit(meta.id, meta.id);
		}
	}

}