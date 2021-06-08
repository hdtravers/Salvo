package com.codeoftheweb.salvo.models;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;


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
    public Map<String, Object> toDTOHitShips() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turno", this.turno);
        dto.put("hits", this.getHits());
        return dto;
    }


    public Map<String, Object> toDTOShipsSunks() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("turno", this.turno);
        dto.put("sunks", this.getSunks().stream().map(Ship::toDTOShips));
        return dto;
    }

public Set<String> getHits(){
    Set<String> hit = new HashSet<>();
    if(gamePlayer.getOponente().isPresent()){
        Set<String> locationShips = getGamePlayer().getOponente().get().getShips().stream().flatMap(ship -> ship.getShipLocations().stream()).collect(Collectors.toSet());
        locationShips.retainAll(Locations);
        hit=locationShips;}
    return hit;
    }

public List<Ship> getSunks(){
         List<Ship> sunks = new ArrayList<>();
         List<String> totalHitsLocations = gamePlayer.getSalvos().stream().filter(x -> x.turno <= this.getTurno()).flatMap(x -> x.getHits().stream()).collect(Collectors.toList());
         if(gamePlayer.getOponente().isPresent()) {
             sunks = gamePlayer.getOponente().get().getShips().stream().filter(x ->totalHitsLocations.containsAll(x.getShipLocations())).collect(Collectors.toList());
        }
        return sunks;
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
