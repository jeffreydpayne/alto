/**
Wraps jQuery.ajax() in a way that handles encryption, cross site request forgery tokens
and json content headers.
*/


jQuery.extend({
	
	altoAjax: 	function(url, options) {
		
		
		if ( typeof url === "object" ) {
			options = url;
			url = undefined;
		}

		// Force options to be an object
		options = options || {};
		
		options.url = url;
		
		return jQuery.ajax(url, options);
		
	},
	
	/*
	 * Encrypts the data using the public RSA key
	 * before posting.
	 */
	postCipher: function(url, data) {
		
		
	},
	
	postJson: function(url, data) {
		console.log("Posting to " + url);
		
	},
	
	deleteJson: function(url, data) {
		
		
	}
	
	
	
});
