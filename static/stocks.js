var ws = new WebSocket("ws://127.0.0.1:8090/stocks");

var hasGrantStart = false;
var colors = [null, "red", "green", "blue", "orange"]
var gameInProgress = false;

ws.onmessage = function (evt) {
    var obj = JSON.parse(evt.data)
    console.log(obj.action + " >> " + obj.data)
    if (obj.action == "cellClick") {
        var cellId = obj.data
        var cell = document.getElementById(cellId)
        cell.style.backgroundColor = "grey"
    } else if (obj.action == "connect" || obj.action == "disconnect") {
        refreshUserList(obj.data);
    } else if (obj.action == "logIn") {
        refreshUserList(obj.data);
        if (!gameInProgress) {
            document.getElementById("login").style.display = "none"
            document.getElementById("ready").style.display = "block"
        }
    } else if (obj.action == "pleaseWait") {
        alert("Please wait - game in progress")
    } else if (obj.action == "ready") {
        document.getElementById("ready").style.display = "none"
    } else if (obj.action == "grantStart") {
        document.getElementById("ready").value = "Start game"
        hasGrantStart = true;
    } else if (obj.action == "startGame") {
        document.getElementById("ready").style.display = "none"
        gameInProgress = true;
        var arr = JSON.parse(obj.data)
        for (var i = 0; i < arr.length; i++) {
            var colorIndex = arr[i]
            if (colorIndex > 0) {
                document.getElementById(i).style.backgroundColor = colors[colorIndex];
            }
        }
    } else if (obj.action == "wrongColor") {
        alert("Click your color only")
    } else if (obj.action == "winner") {
        alert("We have a winner: " + obj.data)
    } else if (obj.action == "gameOver") {
        alert("Please start a new game!")
    }
};

function ready() {
    sendToServer(JSON.stringify({
        action: 'ready',
        data: hasGrantStart
    }));
}

function cellClicked(cellId) {
    sendToServer(JSON.stringify({
        action: 'cellClick',
        data: cellId
    }));
}

function connect() {
    var name = document.getElementById('name').value
    sendToServer(JSON.stringify({
        action: 'logIn',
        data: name
    }));
}

function refreshUserList(userList) {
    var users = document.getElementById("users")
    for (var i = users.options.length - 1; i > 1; i--) {
        users.remove(i);
    }
    var myStringArray = new Array()
    myStringArray = ("" + userList).split(",")
    var arrayLength = myStringArray.length
    for (var i = 0; i < arrayLength; i++) {
        var option = new Option(myStringArray[i], myStringArray[i])
        option.style.color = colors[i + 1]
        users.options[users.options.length] = option
    }
}

// called when socket connection established
ws.onopen = function () {
    console.log("Connected to stock service! Press 'Start' to get stock info.")
};

// called when socket connection closed
ws.onclose = function () {
    console.log("Disconnected from stock service!")
};

// called in case of an error
ws.onerror = function (err) {
    console.log("ERROR!", err)
};

// sends msg to the server over websocket
function sendToServer(msg) {
    ws.send(msg);
}
