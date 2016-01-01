var ws = new WebSocket("ws://127.0.0.1:8090/stocks");

function cellClicked(cellId) {
    sendToServer(JSON.stringify({
        action: 'cellClick',
        data: cellId
    }));
}

function startGame(name) {
    sendToServer(JSON.stringify({
        action: 'startGame',
        data: name
    }));
    document.getElementById('gameDiv').style.display = 'block';
}

// called when socket connection established
ws.onopen = function() {
    appendLog("Connected to stock service! Press 'Start' to get stock info.")
};

// called when a message received from server
ws.onmessage = function (evt) {
	if(evt.action == 'startGame') {
		appendLog('gameStarted')
	} else {
		appendLog(evt)
	}
};

// called when socket connection closed
ws.onclose = function() {
    appendLog("Disconnected from stock service!")
};

// called in case of an error
ws.onerror = function(err) {
    console.log("ERROR!", err )
};

// appends logText to log text area
function appendLog(logText) {
    var log = document.getElementById("log");
    log.value = log.value + logText + "\n"
}

// sends msg to the server over websocket
function sendToServer(msg) {
    ws.send(msg);
}
