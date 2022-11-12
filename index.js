const express = require('express')
const app = express()

app.use(express.static('imgs'));
// express 에서 imgs 내의 파일들을 사용하기 위함

app.listen(8080, function() {
    console.log("listening  on 8080")
})

app.get('/', function(req, res) {
    res.sendFile(__dirname + '/htmls/index.html')
})

app.get('/running_etc1', function(req, res) {
    res.sendFile(__dirname + '/htmls/running_etc1.html')
})

app.get('/running_etc2', function(req, res) {
    res.sendFile(__dirname + '/htmls/running_etc2.html')
})

app.get('/running_etc3', function(req, res) {
    res.sendFile(__dirname + '/htmls/running_etc3.html')
})

app.get('/cycling_etc1', function(req, res) {
    res.sendFile(__dirname + '/htmls/cycling_etc1.html')
})

app.get('/cycling_etc2', function(req, res) {
    res.sendFile(__dirname + '/htmls/cycling_etc2.html')
})

app.get('/cycling_etc3', function(req, res) {
    res.sendFile(__dirname + '/htmls/cycling_etc3.html')
})

app.get('/skiing_etc1', function(req, res) {
    res.sendFile(__dirname + '/htmls/skiing_etc1.html')
})

app.get('/skiing_etc2', function(req, res) {
    res.sendFile(__dirname + '/htmls/skiing_etc2.html')
})

app.get('/skiing_etc3', function(req, res) {
    res.sendFile(__dirname + '/htmls/skiing_etc3.html')
})

