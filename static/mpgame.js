var ws = new WebSocket("ws://127.0.0.1:8090/mpgame");
var hasGrantStart = false;

var colors = [null, "orange", "indianred", "olivedrab", "mediumseagreen", "mediumorchid", "limegreen", "lightslategray"]
var gameInProgress = false;

$(document).ready(function () {
    $("#sign-in").click(function () {
        var playerName = $("#playerName").val().trim()
        if(!playerName) {
            alert("Please enter a valid name")
            $("#playerName").val("")
            $("#playerName").focus()
        } else {
            ws.send(JSON.stringify({
                action: 'logIn',
                data: playerName
            }));
        }
    });

    $("#start-game").click(function() {
        ws.send(JSON.stringify({
            action: 'ready',
            data: hasGrantStart
        }));
    });
});

ws.onmessage = function (evt) {
    var obj = JSON.parse(evt.data)
    if (obj.action == "connect" || obj.action == "disconnect") {
        refreshUserList(obj.data);
    } else if (obj.action == "logIn") {
        refreshUserList(obj.data);
        $(".navbar").hide()
        $("#start-game").removeClass("disabled")
    } else if (obj.action == "ready") {
        $("#ready-btn").hide()
    } else if (obj.action == "grantStart") {
        $("#start-game").text("Start game")
        hasGrantStart = true;
    } else if (obj.action == "startGame") {
        $("#start-game").hide()
        gameInProgress = true;
        var arr = JSON.parse(obj.data)
        for (var i = 0; i < arr.length; i++) {
            var colorIndex = arr[i]
            if (colorIndex > 0) {
                $("#cell_" + i).css("background-color", colors[colorIndex]);
            }
        }
    } else if (obj.action == "cellClick") {
        $("#cell_" + obj.data).css("background-color", "grey");
    } else if (obj.action == "winner") {
        alert("We have a winner: " + obj.data)
    } else if (obj.action == "gameOver") {
        alert("Please start a new game!")
    }
};

function cellClicked(cellId) {
    sendToServer(JSON.stringify({
        action: 'cellClick',
        data: cellId
    }));
}

function refreshUserList(userList) {
    if (userList.length > 0) {
        $('#player-list').empty();
        for (var i = 0; i < userList.length; i++) {
            $("#player-list").append("<li class='list-group-item' style='background-color: " + colors[userList[i].color] + "'>" + userList[i].name + "</li>");
        }
    }
}

// called when socket connection established
ws.onopen = function () {
    console.log("Connected to game service!")
};

// called when socket connection closed
ws.onclose = function () {
    console.log("Disconnected from game service!")
};

// called in case of an error
ws.onerror = function (err) {
    console.log("ERROR!", err)
};

// sends msg to the server over websocket
function sendToServer(msg) {
    ws.send(msg);
}
