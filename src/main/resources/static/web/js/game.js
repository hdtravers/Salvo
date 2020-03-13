var app = new Vue({
   el: '#app',
   data: {
      ships: [],
      gpId : null,
      Columnas:["1","2","3","4","5","6","7","8","9","10"],
      Filas:["A","B","C","D","E","F","G","H","I","J"],
      oponente:{} ,
      player:{} ,
      salvos: [],
   },
   methods: {
       obtenerGpId: function(){
           const urlParams = new URLSearchParams(window.location.search);
           app.gpId = urlParams.get('Gp');
       },
       findGame: function () {
                $.get("/api/game_view/"+ app.gpId, function (data) {
                   app.game = data;
                   app.vistaGp ();
                   app.allShip ();
                   app.allSalvos();
                })
        },
       allShip : function (){
          for (i = 0 ; i < app.game.ships.length; i++ ){
            for(k= 0; k < app.game.ships[i].ShipLocations.length; k++){
                var elemento = document.getElementById( app.game.ships[i].ShipLocations[k]);
                elemento.classList.add ("printCell");

                for( j = 0 ; j < app.game.salvos.length; j++ ){
                   if(app.oponente.id == app.game.salvos[j].playerid)
                    for(l = 0 ; l < app.game.salvos[j].Locations.length ; l++){
                      if(app.game.salvos[j].Locations[l] == app.game.ships[i].ShipLocations[k])
                        elemento.innerHTML = "x";
                    }

                }
            }
          }
       },
        vistaGp : function (){
          for (i = 0 ; i < app.game.gamePlayers.length; i++){
           if (app.game.gamePlayers[i].id == app.gpId){
            app.player = app.game.gamePlayers[i].player
           }
           else{
           app.oponente = app.game.gamePlayers[i].player
           }
          }
        },
         allSalvos : function (){
            for (i = 0 ; i < app.game.salvos.length; i++ ){
                if(app.player.id == app.game.salvos[i].playerid){
                    for(k= 0; k < app.game.salvos[i].Locations.length; k++){
                        var elemento = document.getElementById( app.game.salvos[i].Locations[k]+"s");
                        elemento.classList.add ("printSalvo");
                        elemento.innerHTML = app.game.salvos[i].turno;
                   }
                }
                else{
                    for(k= 0; k < app.game.salvos[i].Locations.length; k++){
                        var elemento1 = document.getElementById( app.game.salvos[i].Locations[k]);
                            elemento1.classList.add ("printSalvo");
                       }
                }
            }
         },
   }

})
app.obtenerGpId ();
app.findGame ();
