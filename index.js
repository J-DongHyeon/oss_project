const express = require('express')
const app = express()

app.listen(8080, function() {
    console.log("listening  on 8080")
})

app.get('/', function(req, res) {
    res.sendFile(__dirname + '/index.html')
})

app.get('/running_etc1', function(req, res) {
    res.sendFile(__dirname + '/running_etc1.html')
})

app.get('/running_etc2', function(req, res) {
    res.sendFile(__dirname + '/running_etc2.html')
})

app.get('/running_etc3', function(req, res) {
    res.sendFile(__dirname + '/running_etc3.html')
})

app.get('/cycling_etc1', function(req, res) {
    res.sendFile(__dirname + '/cycling_etc1.html')
})

app.get('/cycling_etc2', function(req, res) {
    res.sendFile(__dirname + '/cycling_etc2.html')
})

app.get('/cycling_etc3', function(req, res) {
    res.sendFile(__dirname + '/cycling_etc3.html')
})

app.get('/skiing_etc1', function(req, res) {
    res.sendFile(__dirname + '/skiing_etc1.html')
})

app.get('/skiing_etc2', function(req, res) {
    res.sendFile(__dirname + '/skiing_etc2.html')
})

app.get('/skiing_etc3', function(req, res) {
    res.sendFile(__dirname + '/skiing_etc3.html')
})

