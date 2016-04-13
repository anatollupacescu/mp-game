var ws = new WebSocket("ws://127.0.0.1:8090/mpgame");

var colors = [null, "orange", "indianred", "olivedrab", "mediumseagreen", "mediumorchid", "limegreen", "lightslategray"]
var gameInProgress = false;
var playerName;
var playerReady = false;

$(document).ready(function () {
    $("#ready-btn").hide()
    $("#sign-in").click(function () {
        playerName = $("#playerName").val().trim()
        if(!playerName) {
            alert("Please enter a valid name")
            $("#playerName").val("")
            $("#playerName").focus()
        } else {
            sendToServer('logIn', playerName)
            $(".navbar").hide()
            $("#start-game").removeClass("disabled")
        }
    });

    $("#start-game").click(function() {
        sendToServer('ready', null);
        playerReady = true;
    });
});

ws.onmessage = function (evt) {
    var obj = JSON.parse(evt.data)
    if (obj.action == "log") {
        console.log(obj.value)
    } else if (obj.action == "alert") {
        alert(obj.value)
    } else if (obj.action == "playerList") {
        refreshUserList(obj.value);
    } else if (obj.action == "startGame") {
        $("#start-game").toggle()
        gameInProgress = true;
        var arr = obj.value
        for (var i = 0; i < arr.length; i++) {
            var player = arr[i].owner;
            if(player != null) {
                var colorIndex = player.color
                if (colorIndex > 0) {
                    $("#cell_" + i).css("background-color", colors[colorIndex]);
                }
            }
        }
    } else if (obj.action == "cellClick") {
        $("#cell_" + obj.value.id).css("background-color", "grey");
    } else if (obj.action == "winner") {
        alert("We have a winner: " + obj.value.name)
    } else if (obj.action == "gameOver") {
        alert("Please start a new game!")
    }
};

function cellClicked(cellId) {
    sendToServer('cellClick', cellId);
}

function refreshUserList(userList) {
    if (userList.length > 0) {
        $('#player-list').empty();
        var playersReady = 0;
        for (var i = 0; i < userList.length; i++) {
            var player = userList[i];
            var name = player.name;
            if(name === playerName) {
                if(player.status === "ready" || player.status === "standby") {
                    $("#ready-btn").toggle()
               }
            }
            if(player.status === "ready" && !gameInProgress) {
                name += " (ready)";
                playersReady++;
            }
            $("#player-list").append("<li class='list-group-item' style='background-color: " + colors[player.color] + "'>" + name + "</li>");
        }
        if(playersReady === 1 && !playerReady) {
          $("#start-game").text("Start game")
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
function sendToServer(action, value) {
    var msg = JSON.stringify({
        action: action,
        value: value
    })
    ws.send(msg);
}
