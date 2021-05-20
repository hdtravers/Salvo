package com.codeoftheweb.salvo;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Entity
public class Salvo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ElementCollection
    @Column(name = "salvoLocation")
    private List<String> Locations = new ArrayList<>();

   private int turno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    public Salvo() {
    }

    public Salvo(List<String> locations, int turno, GamePlayer gamePlayer) {
        Locations = locations;
        this.turno = turno;
        this.gamePlayer = gamePlayer;
    }

    public Map<String, Object> toDTOSalvos() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("Locations", this.Locations);
        dto.put("turno", this.turno);
        dto.put("playerid", this.gamePlayer.getPlayer().getId());
        return dto;
    }
    public long getId() {return id;}


    public List<String> getLocations() {return Locations;}

    public int getTurno() { return turno;}

    public void setTurno(int turno) { this.turno = turno;}

    public GamePlayer getGamePlayer() {return gamePlayer;}

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }
}
