package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Entity
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @OneToMany(mappedBy="gamePlayer",fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Ship> ships = new HashSet<>();

    @OneToMany(mappedBy="gamePlayer",fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Salvo> salvos= new HashSet<>();


    private LocalDateTime joinDate;



    public GamePlayer(){}

    public GamePlayer(Game game, Player player ) {
        this.joinDate = LocalDateTime.now();
        this.player = player;
        this.game = game;
}

    public Map<String, Object> toDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.id);
        dto.put("player", this.player.toDTO());

        Score score = getScore();
        if (score != null) {
            dto.put("score", score.toDTOScore());
        }
        else{
            dto.put("score", null);
        }
        return dto;
    }


    public Score getScore(){
        Score score = this.player.getScore(this.game);
       return score;
    }

    public Map<String, Object> toDTOGameView() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("gameStatus",this.gameStatus());
        dto.put("id", this.id);
        dto.put("created", this.joinDate);
        dto.put("gamePlayers", this.game.getGamePlayers().stream().map(GamePlayer::toDTO).collect(toList()));
        dto.put("ships",this.getShips().stream().map(Ship::toDTOShips).collect(toList()));
        dto.put( "salvos" ,this.game.getGamePlayers().stream().flatMap(gamePlayer -> gamePlayer.getSalvos().stream().map(Salvo::toDTOSalvos)).collect(toList()));
        dto.put("hits", this.getSalvos().stream().map(Salvo::toDTOHitShips).collect(toList()));
        dto.put("sunkeds",this.getSalvos().stream().map(Salvo::toDTOShipsSunks).collect(toList()));
        if(this.getOponente().isPresent()) {
           dto.put("sunkedsOpponent", this.getOponente().get().getSalvos().stream().map(Salvo::toDTOShipsSunks).collect(toList()));
           dto.put("hitsOpponent", this.getOponente().get().getSalvos().stream().map(Salvo::toDTOHitShips).collect(toList()));
        }
        return dto;
    }

    public  GameStatus gameStatus(){
        if (this.getShips().isEmpty()) {
            return GameStatus.PLACE_SHIPS;
        } else {
            if (this.getOponente().isPresent()) {
                if (this.getOponente().get().getShips().isEmpty()) {
                    return GameStatus.WAIT_FOR_SHIPS_OPPONENT;
                } else {
                    if (this.getSalvos().stream().noneMatch(em -> em.getTurno() == this.getSalvos().size())) {
                        return GameStatus.PLACE_SALVOS;
                    } else {
                        if (this.getOponente().get().getSalvos().stream().noneMatch(em -> em.getTurno() == this.getSalvos().size())) {
                            return GameStatus.WAIT_FOR_SALVO_OPPONENT;
                        } else if (this.getSalvos().size() == this.getOponente().get().getSalvos().size()) {
                            List<Long> mySunks = this.getSalvos().stream().filter(x -> x.getTurno() == this.getSalvos().size()).flatMap(x -> x.getSunks().stream()).map(Ship::getId).collect(toList());
                            List<Long> opponentSunks = new ArrayList<>();

                            if (this.getOponente().isPresent()) {
                                opponentSunks = this.getOponente().get().getSalvos().stream().filter(x -> x.getTurno() == this.getSalvos().size()).flatMap(x -> x.getSunks().stream()).map(Ship::getId).collect(toList());
                            }
                            if (mySunks.size() == 5 && opponentSunks.size() == 5) {
                                return GameStatus.TIE;
                            } else if (mySunks.size() == 5) {
                                return GameStatus.WIN;
                            } else if (opponentSunks.size() == 5) {
                                return GameStatus.LOSE;
                            } else {
                                return GameStatus.PLACE_SALVOS;
                            }
                        } else {
                            return GameStatus.PLACE_SALVOS;

                        }
                    }
                }
            } else {
                return GameStatus.WAIT_OPPONENT;
            }
        }
    }



    public Optional<GamePlayer> getOponente() {
       return this.game.getGamePlayers().stream().filter(gp -> gp.getId() != this.getId()).findFirst();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public Player getPlayer() {
        return player;
    }

    public Game getGame() {
        return game;
    }

    public Set<Ship> getShips() { return ships;}

    public void addShip(Ship ship){
        ship.setGamePlayer(this);
        ships.add(ship);
    }

    public Set<Salvo> getSalvos() {
        return salvos;
    }

    public void addSalvo(Salvo salvo){
        salvo.setGamePlayer(this);
        salvos.add(salvo);
    }


    public void AddShip(Ship ship) {
    }
}
