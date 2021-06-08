package com.codeoftheweb.salvo.controllers;


        import com.codeoftheweb.salvo.models.*;
        import com.codeoftheweb.salvo.repositories.*;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.security.authentication.AnonymousAuthenticationToken;
        import org.springframework.security.core.Authentication;
        import org.springframework.security.crypto.password.PasswordEncoder;
        import org.springframework.web.bind.annotation.*;

        import java.time.LocalDateTime;
        import java.util.*;
        import java.util.stream.Collectors;



@RestController
@RequestMapping("/api")
public class SalvoController {
    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GamePlayerRepository gamePlayerRepository;

    @Autowired
    private SalvoRepository salvoRepository;
    @Autowired
    private ScoreRepository scoreRepository;

    private boolean isGuest(Authentication authentication) {
        return authentication == null || authentication instanceof AnonymousAuthenticationToken;
    }
    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @GetMapping("/games")
    public Map<String, Object> Games(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();
        if (isGuest(authentication)) {
            dto.put("playerLog", null);
        } else {
            Player playerLog = playerRepository.findByUserName(authentication.getName());
            dto.put("playerLog", playerLog.toDTO());
        }
        dto.put("games",
                gameRepository
                        .findAll()
                        .stream()
                        .map(Game::toDTO)
                        .collect(Collectors.toList()));
        return dto;
    }

        @GetMapping("/game_view/{gamePlayerId}")
        public Map<String, Object> gameView (@PathVariable Long gamePlayerId){
            return gamePlayerRepository.findById(gamePlayerId).get().toDTOGameView();            //en optional se usa .get() para acceder a su valor.
        }

        @PostMapping("/players")
        public ResponseEntity<Object> register (@RequestParam String userName, @RequestParam String password){

            if (userName.isEmpty() || password.isEmpty()) {
                return new ResponseEntity<>("Datos perdidos", HttpStatus.FORBIDDEN);
            }

            if (playerRepository.findByUserName(userName) != null) {
                return new ResponseEntity<>("Nombre ya en uso", HttpStatus.FORBIDDEN);
            }

            playerRepository.save(new Player(userName, passwordEncoder.encode(password)));
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
    @PostMapping("/games")
        public ResponseEntity<Map<String, Object>> newGame(Authentication authentication) {
            ResponseEntity<Map<String, Object>> response;
            if(this.isGuest(authentication)){
                response = new ResponseEntity<>(makeMap("error", "Debes iniciar sesión para crear un juego"), HttpStatus.FORBIDDEN);
            }
            else{
                Game newGame = new Game (LocalDateTime.now());
                Player player  = playerRepository.findByUserName(authentication.getName());
                GamePlayer newGamePlayer = new GamePlayer(newGame,player);
                gameRepository.save(newGame);
                gamePlayerRepository.save(newGamePlayer);
                response = new ResponseEntity<>(makeMap("gpid",newGamePlayer.getId()),HttpStatus.CREATED);

            }
            return response;

    }
    @PostMapping("/game/{gameId}/players")
      public ResponseEntity<Map<String, Object>> joinGame(Authentication authentication, @PathVariable long gameId) {
        ResponseEntity<Map<String, Object>> response;
        if(this.isGuest(authentication)){
            response = new ResponseEntity<>(makeMap("error", "Debes iniciar sesión para crear un juego"), HttpStatus.FORBIDDEN);
        }
        else{
            Game game = gameRepository.findById(gameId).orElse(null);
            Player player  = playerRepository.findByUserName(authentication.getName());
            if(game==null){
                response = new ResponseEntity<>(makeMap("error", "El juego no existe"), HttpStatus.FORBIDDEN);
            }
            else if (game.getGamePlayers().size()>1){
                response = new ResponseEntity<>(makeMap("error", "El juego esta completo"), HttpStatus.FORBIDDEN);
            }
            else if (game.getGamePlayers().stream().anyMatch(gamePlayer -> gamePlayer.getPlayer().getId()==player.getId())){
                response = new ResponseEntity<>(makeMap("error", "Tu ya estas en este juego "), HttpStatus.FORBIDDEN);
            }
            else {
                GamePlayer newGamePlayer = new GamePlayer(game,player);
                gamePlayerRepository.save(newGamePlayer);
                response = new ResponseEntity<>(makeMap("gpid",newGamePlayer.getId()),HttpStatus.CREATED);
            }
        }
        return response;
    }
    @PostMapping("/games/players/{gamePlayerId}/salvos")
    public ResponseEntity<Map<String, Object>> saveSalvos(Authentication authentication, @PathVariable long gamePlayerId, @RequestBody Salvo salvos) {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Tienes que iniciar sesión."), HttpStatus.UNAUTHORIZED);
        }else {
            Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);
            Player player = playerRepository.findByUserName(authentication.getName());
            if (!gamePlayer.isPresent()) {
                return new ResponseEntity<>(makeMap("error", "Game not found"), HttpStatus.NOT_FOUND);

            } else if (gamePlayer.get().getPlayer().getId()  != player.getId()) {
                return new ResponseEntity<>(makeMap("error", "Este no es tu juego"), HttpStatus.UNAUTHORIZED);

            } else if (gamePlayer.get().getSalvos().size() == salvos.getTurno() ) {
                return new ResponseEntity<>(makeMap("error", "espera tu turno"), HttpStatus.FORBIDDEN);

            }else if (gamePlayer.get().getSalvos().size() +1 != salvos.getTurno() ) {
                return new ResponseEntity<>(makeMap("error", "Espera tu turno"), HttpStatus.FORBIDDEN);

            }else if (salvos.getLocations().size() != 5  ) {
                return new ResponseEntity<>(makeMap("error", "Debes enviar 5 salvas por turno"), HttpStatus.FORBIDDEN);


            } else {
                Optional<GamePlayer> player2 = gamePlayer.get().getOponente();
                if(player2.isPresent()) {
                    if (gamePlayer.get().getSalvos().size() - player2.get().getSalvos().size() >= 1) {
                        return new ResponseEntity<>(makeMap("error", "You can't skip turns, cheater!"), HttpStatus.UNAUTHORIZED);
                    }

                }else{
                    return new ResponseEntity<>(makeMap("error", "Espera un oponente"), HttpStatus.FORBIDDEN);
                }

                Salvo salvo = new Salvo(salvos.getLocations(), salvos.getTurno(), gamePlayer.get());
                salvoRepository.save(salvo);
                gamePlayer.get().getSalvos().add(salvo);
                if (gamePlayer.get().gameStatus() == GameStatus.TIE) {
                    scoreRepository.save(new Score(LocalDateTime.now(), gamePlayer.get().getPlayer(), gamePlayer.get().getGame(), 0.5));
                    scoreRepository.save(new Score(LocalDateTime.now(), gamePlayer.get().getOponente().get().getPlayer(), gamePlayer.get().getGame(), 0.5));
                }else if (gamePlayer.get().gameStatus() == GameStatus.WIN) {
                    scoreRepository.save(new Score(LocalDateTime.now(), gamePlayer.get().getPlayer(), gamePlayer.get().getGame(), 1.0));
                    scoreRepository.save(new Score(LocalDateTime.now(), gamePlayer.get().getOponente().get().getPlayer(), gamePlayer.get().getGame(), 0.0));
                }else if (gamePlayer.get().gameStatus() == GameStatus.LOSE) {
                    scoreRepository.save(new Score(LocalDateTime.now(), gamePlayer.get().getOponente().get().getPlayer(), gamePlayer.get().getGame(), 1.0));
                    scoreRepository.save(new Score(LocalDateTime.now(), gamePlayer.get().getPlayer(), gamePlayer.get().getGame(), 0.0));
                }
                return new ResponseEntity<>(makeMap("done!", "Has agregado las salvas"), HttpStatus.CREATED);
            }
        }

    }

    @PostMapping("games/players/{gamePlayerId}/ships")
    public ResponseEntity<Map<String, Object>> saveShips(Authentication authentication, @PathVariable long gamePlayerId, @RequestBody List<Ship> ships) {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "Tienes que iniciar sesión."), HttpStatus.UNAUTHORIZED);

        } else {
            Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);
            Player player = playerRepository.findByUserName(authentication.getName());
            if (!gamePlayer.isPresent()) {
                return new ResponseEntity<>(makeMap("error", "Juego no encontrado"), HttpStatus.NOT_FOUND);

            } else if (gamePlayer.get().getPlayer().getId() != player.getId()) {
                return new ResponseEntity<>(makeMap("error", "Este no es tu juego"), HttpStatus.UNAUTHORIZED);

            } else if (gamePlayer.get().getShips().size() > 0) {
                return new ResponseEntity<>(makeMap("error", "Todos los barcos colocados"), HttpStatus.FORBIDDEN);

            } else if (ships.size() != 5) {
                return new ResponseEntity<>(makeMap("error", "Debes tener 5 barcos"), HttpStatus.FORBIDDEN);

            } else {
                for (Ship ship : ships) {
                    gamePlayer.get().addShip(ship);
                }
                gamePlayerRepository.save(gamePlayer.get());
                return new ResponseEntity<>(makeMap("done!", "Has agregado los barcos"), HttpStatus.CREATED);
            }
        }
    }

}

