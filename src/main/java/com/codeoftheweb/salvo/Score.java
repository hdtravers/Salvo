package com.codeoftheweb.salvo;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Entity
public class Score {
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

    private double score;

    private LocalDateTime finishDate;



    public Score(){}

    public Score( Player player, Game game, double score) {

        this.player = player;
        this.game = game;
        this.score = score;
        this.finishDate = LocalDateTime.now();
    }


    public Map<String, Object> toDTOScore() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("score", this.score);
        dto.put("finishDate", this.finishDate);
        return dto;
    }

    public long getId() {return id;}

    public Player getPlayer() {return player;}

    public Game getGame() {return game;}

    public double getScore() {return score;}

    public LocalDateTime getFinishDate() {return finishDate;}


}
