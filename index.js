const express = require('express');
const socket = require('socket.io');
const http = require('http');

const app = express();
const server = http.createServer(app);
const io = socket.listen(server);
server.listen(80);
/* 
express는 기본적으로 3000 포트로 지정되어 있다.
js 프로그램이 3000 포트로 실행되며, 웹 서버와 소켓 모두 3000 포트에 존재한다.
이때, 웹 서버는 8080 포트로부터 listen 한다. 즉, 웹 서버와 웹 클라이언트는 8080 포트로 통신한다.
소켓은 80 포트로부터 listen 한다. 즉, html의 소켓과 js 프로그램의 소켓은 80 포트로 통신한다.
*/

var g_socket;
io.sockets.on('connection', function(socket) {
    g_socket = socket;
    //console.log("socket connection OK (port : 80)");
})


var mqtt = require("mqtt");
const { stringify } = require('querystring');
var mqtt_client = mqtt.connect("mqtt://127.0.0.1");

mqtt_client.on("connect", function(){
    mqtt_client.subscribe("weather");
    console.log("Subscribing Topic weather (MQTT client)");
  })

mqtt_client.on("message", function(topic, message){

    if (topic == "weather"){
      const str = message.toString();

      io.sockets.emit("weather", str);

      console.log("--------------------------------");
      console.log("PTY(강수 형태), REH(습도), T1H(기온), WSD(풍속)");
      console.log(str);
      console.log("--------------------------------");
    }
  }); 

app.use(express.static('imgs'));
// express 에서 imgs 내의 파일들을 사용하기 위함
app.use(express.static('node_modules'));

app.listen(8080, function() {
    console.log("listening  on 8080 (Web server)")
})

app.get('/', function(req, res) {
    res.sendFile(__dirname + '/htmls/index.html')
})

app.get('/running_etc1', function(req, res) {
    mqtt_client.publish("region", "37.87013551233077 127.6929494677985")
    res.sendFile(__dirname + '/htmls/running_etc1.html')
})


app.get('/running_etc2', function(req, res) {
    mqtt_client.publish("region", "37.85788883074562 127.69192574178021")
    res.sendFile(__dirname + '/htmls/running_etc2.html')
})

app.get('/running_etc3', function(req, res) {
    mqtt_client.publish("region", "37.89077659464432 127.68975515045994")
    res.sendFile(__dirname + '/htmls/running_etc3.html')
})

app.get('/cycling_etc1', function(req, res) {
    mqtt_client.publish("region", "37.90702777705924 127.74471385262407")
    res.sendFile(__dirname + '/htmls/cycling_etc1.html')
})

app.get('/cycling_etc2', function(req, res) {
    mqtt_client.publish("region", "37.776928885183565 127.46569112867188")
    res.sendFile(__dirname + '/htmls/cycling_etc2.html')
})

app.get('/cycling_etc3', function(req, res) {
    mqtt_client.publish("region", "37.92789965914075 127.79067029406818")
    res.sendFile(__dirname + '/htmls/cycling_etc3.html')
})

app.get('/skiing_etc1', function(req, res) {
    mqtt_client.publish("region", "37.820457997195916 127.58948927181696")
    res.sendFile(__dirname + '/htmls/skiing_etc1.html')
})

app.get('/skiing_etc2', function(req, res) {
    mqtt_client.publish("region", "37.64429135948726 127.68186650064948")
    res.sendFile(__dirname + '/htmls/skiing_etc2.html')
})

