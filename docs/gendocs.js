var fs = require('fs');
var docson = require("node-docson")();
var schema = require("./schema.json");
var element = docson.doc(schema);
fs.writeFileSync("./index.html", element.ownerDocument.documentElement.outerHTML);
