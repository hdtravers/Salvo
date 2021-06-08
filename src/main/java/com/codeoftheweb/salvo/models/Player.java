package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @OneToMany(mappedBy="player",fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy="player",fetch=FetchType.EAGER)
    private Set<Score> scores;

    private String userName;

    private  String password;


    public Player() { }



    public Player(String user, String password) {
            this.userName = user;
            this.password = password;

    }

    public Score getScore(Game game){
        Score playerScore =  scores.stream().filter(score -> score.getGame().getId()==game.getId()).findFirst().orElse(null);
        return playerScore;
    }

    public Set<Score> getScores() {return scores;}

    public long getId() {return id;}

    public String getUserName() {
        return userName;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public Map<String, Object> toDTO() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("id", this.id);
        dto.put("userName", this.userName);
        return dto;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}