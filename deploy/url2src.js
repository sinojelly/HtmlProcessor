
var page = require('webpage').create(),
    system = require('system'),
    t, address, output;

if (system.args.length !== 3) {
    console.log('Usage: url2src.js <some URL> <output File path>');
    phantom.exit();
}

t = Date.now();
address = system.args[1];
output = system.args[2];
page.open(address, function (status) {
    if (status !== 'success') {
        console.log('FAIL to load the address : ' + address);
    } else {
        t = Date.now() - t;
        //console.log('Loading time ' + t + ' ms');
		var js = page.evaluate(function () {
			return document;
		});
		//console.log(js.all[0].outerHTML); 
		var fs = require('fs');
        try {
		    fs.write(output, js.all[0].outerHTML, 'w');
		} catch(e) {
			console.log(e);
		}
	}
    phantom.exit();
});
