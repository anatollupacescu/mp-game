var ws = new WebSocket("ws://127.0.0.1:8090/stocks");

function cellClicked(cellId) {
    sendToServer(JSON.stringify({
        action: 'cellClick',
        data: cellId
    }));
}

function connect(name) {
    sendToServer(JSON.stringify({
        action: 'logIn',
        data: name
    }));
}

// called when a message received from server
ws.onmessage = function (evt) {
	var obj = JSON.parse(evt.data)
	appendLog(obj.action + " >> " + obj.data)
	if(obj.action == "cellClick") {
		var cellId = obj.data
		var cell = document.getElementById(cellId)
		cell.style.backgroundColor="green"
	} else if(obj.action == "connect" || obj.action == "disconnect") {
		refreshUserList(obj.data);
	} else if(obj.action == "logIn") {
		refreshUserList(obj.data);
		document.getElementById("login").style.display="none";
	}
};

function refreshUserList(userList) {
	var users = document.getElementById("users")
	removeOptions(users)
	var myStringArray = new Array()
	myStringArray = ("" + userList).split(",")
	var arrayLength = myStringArray.length
	for (var i = 0; i < arrayLength; i++) {
		users.options[users.options.length] = new Option(myStringArray[i], myStringArray[i]);
	}
}

function removeOptions(selectbox)
{
    var i;
    for(i=selectbox.options.length-1;i>=0;i--){
        selectbox.remove(i);
    }
}

// called when socket connection established
ws.onopen = function() {
    appendLog("Connected to stock service! Press 'Start' to get stock info.")
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
