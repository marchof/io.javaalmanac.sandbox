'use strict';


exports.handler = (event, context, callback) => {
    const request = event.Records[0].cf.request;

    const host = request.headers.host[0].value;
    console.log('original host', host);

    const match = host.match(/java([0123456789]+)\../);
    
    if (match) {
        const port = 8000 + parseInt(match[1], 10);
        request.origin.custom.port = port;
        console.log('use port', port);
    }

    callback(null, request);
};