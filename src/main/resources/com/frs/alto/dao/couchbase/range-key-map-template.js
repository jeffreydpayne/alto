function(doc, meta) {
	var keyTokens = meta.id.split("#");
	if (keyTokens.length == 3) {
		if (keyTokens[1] == '$keyNameSpace') {
			emit(keyTokens[0] + '#' doc.$hashKey + '#' + keyTokens[2] + doc.$rangeKey, keyTokens[2]);
		}
	}
	else {
		if (keyTokens[0] == '$keyNameSpace') {
			emit(doc.$hashKey + '#'  + doc.$rangeKey + '#' + keyTokens[1], keyTokens[1]);
		}
	}
}