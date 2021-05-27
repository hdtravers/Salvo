var app = new Vue({
    el: '#app',
    data: {
        game:{},
        ships: [],
        gpId: null,
        Columnas: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"],
        Filas: ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"],
        oponente: {},
        player: {},
        salvos: [],
        shipsParaPost: [{
                "type": "Destructor",
                "size": 3,
                "shipLocations": [],
            },
            {
                "type": "Portaaviones",
                "size": 5,
                "shipLocations": [],
            },
            {
                "type": "Submarino",
                "size": 3,
                "shipLocations": [],
            },
            {
                "type": "Acorazado",
                "size": 4,
                "shipLocations": [],
            },
            {
                "type": "Bote de Patrulla",
                "size": 2,
                "shipLocations": [],
            }
        ],
        salvoParaPost:
            {
                "locations" : [],
                "turno" : 0, 
              },
        
        shipActual: {
            "type": "Destructor",
            "size": 3,

        },
        orientacionActual: "Vertical",


      
    },
    

    methods: {
        obtenerGpId: function () {
            const urlParams = new URLSearchParams(window.location.search);
            app.gpId = urlParams.get('Gp');
        },
        findGame: function () {
            $.get("/api/game_view/" + app.gpId, function (data) {
                app.game = data;
                app.vistaGp();
                app.allShip();
                app.allSalvos();


            })
        },

        allShip: function () {
            for (i = 0; i < app.game.ships.length; i++){
    
                for (k = 0; k < app.game.ships[i].ShipLocations.length; k++) {
                    var elemento = document.getElementById(app.game.ships[i].ShipLocations[k]);
                    elemento.classList.add("printCell");

                    for (j = 0; j < app.game.salvos.length; j++) {
                        if (app.oponente.id == app.game.salvos[j].playerid)
                            for (l = 0; l < app.game.salvos[j].Locations.length; l++) {
                                if (app.game.salvos[j].Locations[l] == app.game.ships[i].ShipLocations[k])
                                    elemento.innerHTML = "x";
                            }
                    }
                }
            }
        },
        vistaGp: function () {
            for (i = 0; i < app.game.gamePlayers.length; i++) {
                if (app.game.gamePlayers[i].id == app.gpId) {
                    app.player = app.game.gamePlayers[i].player
                } else {
                    app.oponente = app.game.gamePlayers[i].player
                }
            }
        },

        enviaLosShips: function () {
         if (app.shipsParaPost[0].shipLocations.length != 0 &&
             app.shipsParaPost[1].shipLocations.length != 0 && 
             app.shipsParaPost[2].shipLocations.length != 0 && 
             app.shipsParaPost[3].shipLocations.length != 0 &&
             app.shipsParaPost[4].shipLocations.length != 0) {


                $.post({
                        url: "/api/games/players/" + app.gpId + "/ships",
                        data: JSON.stringify(app.shipsParaPost),
                        dataType: "text",
                        contentType: "application/json"
                    })
                    .done(function (respuesta) { location.reload(); })
                } else {Swal.fire({
                    position: 'center',
                    icon: 'error',
                    title: JSON.parse(respuesta.responseText).error,
                    showConfirmButton: false,
                    timer: 3000
                })
            }
        },



       enviaSalvos: function () {
        
        $.post({
            url: "/api/games/players/" + app.gpId + "/salvos",
            data: JSON.stringify(app.salvoParaPost),
            dataType: "text",
            contentType: "application/json"
        })
        .done(function () {
            location.reload();
        })
        .fail(function (respuesta) {
            Swal.fire({
                position: 'center',
                icon: 'error',
                title: JSON.parse(respuesta.responseText).error,
                showConfirmButton: false,
                timer: 3000
            })
        })
},
             
        






        allSalvos: function () {
            for (i = 0; i < app.game.salvos.length; i++) {
                if (app.player.id == app.game.salvos[i].playerid) {
                    for (k = 0; k < app.game.salvos[i].Locations.length; k++) {
                        var elemento = document.getElementById(app.game.salvos[i].Locations[k] + "s");
                        elemento.classList.add("printSalvo");
                        elemento.innerHTML = app.game.salvos[i].turno;
                    }
                } else {
                    for (k = 0; k < app.game.salvos[i].Locations.length; k++) {
                        var elemento1 = document.getElementById(app.game.salvos[i].Locations[k]);
                        elemento1.classList.add("printSalvo");
                    }
                }
            }
        },

        clickSalvo: function (letra, numero){
            var slocation= letra + numero
            var turno = app.game.salvos.filter(el => el.playerid == app.player.id).length + 1
            var salvosFilter =  app.game.salvos.filter(el => el.playerid == app.player.id)
            //if( app.game.salvos.filter(el => el.playerid == app.player.id)){

             if(app.salvoParaPost.locations.length <= 4 &&  !app.salvoParaPost.locations.includes(slocation) && !salvosFilter.some(x => x.Locations.includes(slocation)) )  {

               app.salvoParaPost.turno = turno 
               app.salvoParaPost.locations.push(slocation)
               document.getElementById(slocation + "s").classList.add("printSalvo"); 

            } else{

                  if(app.salvoParaPost.locations.length <= 5 && app.salvoParaPost.locations.includes(slocation)) {
                     app.salvoParaPost.locations.splice(app.salvoParaPost.locations.indexOf(slocation),1 )
                     document.getElementById(slocation + "s").classList.remove("printSalvo")
                 
             }
             
            }
         //}
        },


        clickEnCel: function (letra, numero) {
            ships = []
            if(app.game.ships.length == 0){
            if (app.orientacionActual == "Vertical") {
                for (j = 0; j < app.shipActual.size; j++) {
                    for (i = 0; i < app.Filas.length; i++) {
                        if (app.Filas[i] == letra) {
                            ships.push(app.Filas[i + j] + numero)
                        }
                    }
                }
                if (!app.shipsParaPost.some(x => ships.some(y => x.shipLocations.includes(y))) && (app.Filas.indexOf(letra) + app.shipActual.size) < 11) {

                    for (k = 0; k < app.shipsParaPost.length; k++) {
                        if (app.shipsParaPost[k].type == app.shipActual.type) {
                            for (j = 0; j < app.shipsParaPost[k].shipLocations.length; j++) {
                                document.getElementById(app.shipsParaPost[k].shipLocations[j]).classList.remove("printCell")
                            }
                            app.shipsParaPost.find(h => h.type == app.shipActual.type).shipLocations = []
                            for (i = 0; i < ships.length; i++) {
                                app.shipsParaPost[k].shipLocations.push(ships[i])
                                document.getElementById(ships[i]).classList.add("printCell");

                            }
                        }
                    }
                }
            } else {
                for (j = 0; j < app.shipActual.size; j++) {
                    ships.push(letra + (parseInt(numero) + j))
                }
                if (!app.shipsParaPost.some(h => ships.some(m => h.shipLocations.includes(m))) && (Number(numero)) + Number(app.shipActual.size) <= 11) {

                    for (k = 0; k < app.shipsParaPost.length; k++) {
                        if (app.shipsParaPost[k].type == app.shipActual.type) {
                            for (j = 0; j < app.shipsParaPost[k].shipLocations.length; j++) {
                                document.getElementById(app.shipsParaPost[k].shipLocations[j]).classList.remove("printCell")
                            }
                            app.shipsParaPost.find(h => h.type == app.shipActual.type).shipLocations = []
                            for (i = 0; i < ships.length; i++) {
                                app.shipsParaPost[k].shipLocations.push(ships[i])
                                document.getElementById(ships[i]).classList.add("printCell");
                            }
                        }
                    }
                }
            }
        }
        }
      //
    }
})
app.obtenerGpId();
app.findGame();