package az.avalonstattracker;

import java.util.LinkedList;
import java.util.List;

class GameConfiguration {
    List<String> availableRoles;
    List<String> availablePlayers;
    Integer goodRolesNr;
    Integer badRolesNr;
    Integer playerNr;
    List<ViewListRow> data;
    Utilities utils;

    GameConfiguration(Utilities utils, Integer playerNr){
        this.goodRolesNr = new Integer(0);
        this.badRolesNr = new Integer(0);
        this.availablePlayers = utils.dbHelper.getPlayers();
        this.availablePlayers.add(0, utils.EMPTY_FIELD);
        this.playerNr = playerNr;
        this.availableRoles = new LinkedList<>();
        this.availableRoles.add(utils.EMPTY_FIELD);
        this.availableRoles.addAll(utils.goodRoles);
        this.availableRoles.addAll(utils.badRoles);
        this.utils = utils;
        this.data = new LinkedList<>();
    }

    void addDataRow(ViewListRow vlr){
        data.add(vlr);
    }
}
