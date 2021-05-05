package com.codeoftheweb.salvo;


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

        import static java.util.stream.Collectors.reducing;
        import static java.util.stream.Collectors.toList;

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
                return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
            }

            if (playerRepository.findByUserName(userName) != null) {
                return new ResponseEntity<>("Name already in use", HttpStatus.FORBIDDEN);
            }

            playerRepository.save(new Player(userName, passwordEncoder.encode(password)));
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
    @PostMapping("/games")
        public ResponseEntity<Map<String, Object>> newGame(Authentication authentication) {
            ResponseEntity<Map<String, Object>> response;
            if(this.isGuest(authentication)){
                response = new ResponseEntity<>(makeMap("error", "You must log in to create a game"), HttpStatus.FORBIDDEN);
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
            response = new ResponseEntity<>(makeMap("error", "You must log in to create a game"), HttpStatus.FORBIDDEN);
        }
        else{
            Game game = gameRepository.findById(gameId).orElse(null);
            Player player  = playerRepository.findByUserName(authentication.getName());
            if(game==null){
                response = new ResponseEntity<>(makeMap("error", "the game does not exist"), HttpStatus.FORBIDDEN);
            }
            else if (game.getGamePlayers().size()>1){
                response = new ResponseEntity<>(makeMap("error", "the game is full"), HttpStatus.FORBIDDEN);
            }
            else if (game.getGamePlayers().stream().anyMatch(gamePlayer -> gamePlayer.getPlayer().getId()==player.getId())){
                response = new ResponseEntity<>(makeMap("error", "Your are already in this game "), HttpStatus.FORBIDDEN);
            }
            else {
                GamePlayer newGamePlayer = new GamePlayer(game,player);
                gamePlayerRepository.save(newGamePlayer);
                response = new ResponseEntity<>(makeMap("gpid",newGamePlayer.getId()),HttpStatus.CREATED);
            }
        }
        return response;
    }
    @PostMapping("games/players/{gamePlayerId}/ships")
    public ResponseEntity<Map<String, Object>> saveShips(Authentication authentication, @PathVariable long gamePlayerId, @RequestBody List<Ship> ships) {
        if (isGuest(authentication)) {
            return new ResponseEntity<>(makeMap("error", "You have to logged in."), HttpStatus.UNAUTHORIZED);

        } else {
            Optional<GamePlayer> gamePlayer = gamePlayerRepository.findById(gamePlayerId);
            Player player = playerRepository.findByUserName(authentication.getName());
            if (gamePlayer.isEmpty()) {
                return new ResponseEntity<>(makeMap("error", "Game not found"), HttpStatus.NOT_FOUND);

            } else if (gamePlayer.get().getPlayer().getId() != player.getId()) {
                return new ResponseEntity<>(makeMap("error", "This is not your game"), HttpStatus.UNAUTHORIZED);

            } else if (gamePlayer.get().getShips().size() > 0) {
                return new ResponseEntity<>(makeMap("error", "all ships placed"), HttpStatus.FORBIDDEN);

            } else if (ships.size() != 5) {
                return new ResponseEntity<>(makeMap("error", "you must have 5 ships"), HttpStatus.FORBIDDEN);

            } else {
                for (Ship ship : ships) {
                    gamePlayer.get().addShip(ship);
                }
                gamePlayerRepository.save(gamePlayer.get());
                return new ResponseEntity<>(makeMap("done!", "You have added the ships"), HttpStatus.CREATED);
            }
        }
    }

}

