
let mySocket;
let userName;
let myMsg;
let roomName;

function joinServer(){
	 userName = document.getElementById("username").value.toLowerCase();
	 roomName = document.getElementById("roomname").value.toLowerCase();

	// connecting to the socket
	 mySocket = new WebSocket("ws://localhost:8080/chat");
	mySocket.onopen = function(event){
			  console.log("connecting.....");
			 mySocket.send("join "+roomName);		 
		}


	mySocket.onmessage = function(event){
		console.log(event.data);
		var msgObj = JSON.parse(event.data);
		var msgDiv = document.getElementById("messageDisplay");

		var container = document.createElement("div");
		container.id = "container";

		var oneMsg = document.createElement("div");
		oneMsg.id = "bubble";
		var msgContent = document.createTextNode(msgObj.user + ": " + msgObj.message);
		oneMsg.appendChild(msgContent);
		// msgDiv.appendChild(oneMsg);
		container.appendChild(oneMsg);
		msgDiv.appendChild(container);
		if (msgObj.user === userName) {
			oneMsg.className = "you";
		}else{
			oneMsg.className = "yme";
		}

		// var msgWrap = document.createElement("div");
		// msgWrap.id = "msgWrap";
		// msgDiv.appendChild(oneMsg);
		// oneMsg.appendChild(msgWrap);

		// var msg = document.createElement("p");
		// var msgContent = document.createTextNode(msgObj.user + ": " + msgObj.message);

		// if(msgObj.user === userName){
		// 	msgWrap.className = "you";
		// 	console.log("equal");
		// }else{
		// 	msgWrap.className = "others";
		// 	console.log("unequal");
		// }

		// var img = document.createElement("img");
		// img.src = "chat.jpg";
		// img.style = "width:100%";
		// img.id = "charimg";
		// msg.appendChild(msgContent);
		// msgWrap.appendChild(img);
		// msgWrap.appendChild(msg);
	}

	// mySocket.onerror = function(event){
	// 	alter("Unable to join the room");
	// }

	// taking to the second page
	var register = document.getElementById("register");
	register.style.display = 'none';

	var chatroom = document.getElementById("chatroom");
	chatroom.style.display = 'block';

}


function sendMsg(){
	    myMsg = document.getElementById("chatmessage").value.toLowerCase();
	    mySocket.send(userName + " "+myMsg);

}


	