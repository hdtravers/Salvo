
var app = new Vue({
    el: '#app',
    data: {
        games: [],
        scores: [],
        emailLogeado: "",
        passLogeado: "",
        playerLog: null,
    },
    methods: {
        findData: function () {
            $.get("/api/games", function (data) {
                app.games = data.games;
                app.scoresPlayers(app.games);
                app.playerLog = data.playerLog;
            })
        },
        logout: function () {
            $.post("/api/logout").done(function () {
                location.reload();
                alert("Desconectado con éxito")
            })
        },
        login: function () {
            var user = app.emailLogeado
            var pass = app.passLogeado
            if (user == "" || pass == "") {
                alert("Por favor llene todos los campos");
            } else {
                $.post("/api/login", {
                    userName: user,
                    password: pass,
                }).done(function () {
                    location.reload()
                })
                    .fail(function () {
                        alert("Por favor, inténtalo de nuevo :");
                    })
            }
        },
        signUp: function () {
            var user = app.emailLogeado
            var pass = app.passLogeado
            if (user == "" || pass == "") {
                alert("Por favor llene todos los campos");
            } else {
                $.post("/api/players", {
                    userName: user,
                    password: pass,
                }).done(login())
            }
        },
        create: function () {
            $.post("/api/games").done(function (data) {
                location.href = "/web/Game.html?Gp=" + data.gpid
            })
            
        },
        
        unirse: function (gameid) {
            $.post("/api/game/" + gameid +"/players ").done(function (respgameid) {
                location.href = "/web/Game.html?Gp=" + respgameid.gpid
            })   
        },
       
        scoresPlayers: function (games) {
            for (i = 0; i < games.length; i++) {
                var gamePlayers = games[i].gamePlayers;
                for (j = 0; j < gamePlayers.length; j++) {
                    var index = app.scores.findIndex(scorePlayer => scorePlayer.player === gamePlayers[j].player.userName);
                    if (index == -1) {
                        var scorePlayer = {
                            player: gamePlayers[j].player.userName,
                            nLoss: 0,
                            nTies: 0,
                            nWins: 0,
                            nTotal: 0,
                        };
                        if (gamePlayers[j].score != null) {
                            if (gamePlayers[j].score.score == 0.0) {
                                scorePlayer.nLoss++
                            } else if (gamePlayers[j].score.score == 0.5) {
                                scorePlayer.nTies++
                            } else if (gamePlayers[j].score.score == 1.0) {
                                scorePlayer.nWins++
                            };
                            scorePlayer.nTotal += gamePlayers[j].score.score;
                            app.scores.push(scorePlayer);
                        }
                    } else {
                        if (gamePlayers[j].score != null) {
                            if (gamePlayers[j].score.score == 0.0) {
                                app.scores[index].nLoss++
                            } else if (gamePlayers[j].score.score == 0.5) {
                                app.scores[index].nTies++
                            } else if (gamePlayers[j].score.score == 1.0) {
                                app.scores[index].nWins++
                            };
                            app.scores[index].nTotal += gamePlayers[j].score.score;
                        }
                    }
                };
            }
        },
    },
})
app.findData();
