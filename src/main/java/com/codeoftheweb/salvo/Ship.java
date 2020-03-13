package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    @Column(name = "shipLocation")
    private List<String> ShipLocations = new ArrayList<>();


    public Ship() {
    }

    public Ship(String type, List<String> shipLocations) {
        this.type = type;
        ShipLocations = shipLocations;
    }


    public Map<String, Object> toDTOShips() {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("type", this.type);
        dto.put("ShipLocations", this.ShipLocations);
        return dto;
    }

    public String getType() {
        return type;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public List<String> getShipLocations() {
        return ShipLocations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

}



