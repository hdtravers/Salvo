package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
        dto.put("id", this.id);
        dto.put("created", this.joinDate);
        dto.put("gamePlayers", this.game.getGamePlayers().stream().map(GamePlayer::toDTO).collect(toList()));
        dto.put("ships",this.getShips().stream().map(Ship::toDTOShips).collect(toList()));
        dto.put( "salvos" ,this.game.getGamePlayers().stream().flatMap(gamePlayer -> gamePlayer.getSalvos().stream().map(Salvo::toDTOSalvos)).collect(toList()));
        return dto;
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
