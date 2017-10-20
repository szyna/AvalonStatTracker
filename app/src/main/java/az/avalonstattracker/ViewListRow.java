package az.avalonstattracker;

import android.util.Log;
import android.view.View;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ViewListRow {
    String selectedPlayer;
    String selectedRole;
    GameConfiguration config;
    List<String> availablePlayers;
    List<String> availableRoles;

    ViewListRow(String selectedPlayer, String selectedRole, GameConfiguration config){
        this.selectedPlayer = selectedPlayer;
        this.selectedRole = selectedRole;
        this.config = config;
        this.availableRoles = new LinkedList<>(config.availableRoles);
        this.availablePlayers = new LinkedList<>(config.availablePlayers);
        for(ViewListRow vlr: config.data){
            String s = vlr.selectedPlayer;
            if(!s.equals(config.utils.EMPTY_FIELD)){
                this.availablePlayers.remove(s);
            }
            if(!this.selectedPlayer.equals(config.utils.EMPTY_FIELD)){
                vlr.availablePlayers.remove(this.selectedPlayer);
            }
        }
        config.addDataRow(this);
    }

    void setPlayer(String player){
        for(ViewListRow vlr : config.data) {
            if (vlr.availablePlayers != availablePlayers) {
                if (!selectedPlayer.equals(config.utils.EMPTY_FIELD)) {
                    vlr.availablePlayers.add(selectedPlayer);
                }
                if (!player.equals(config.utils.EMPTY_FIELD)) {
                    vlr.availablePlayers.remove(player);
                }
            }
        }
        selectedPlayer = player;
    }

    void setRole(String role){
        Integer prevGoodRolesNr = 0, prevBadRolesNr = 0;
        if (config.utils.goodRoles.contains(selectedRole)){
            prevGoodRolesNr = config.goodRolesNr;
            config.goodRolesNr -= 1;
        }else if(config.utils.badRoles.contains(selectedRole)){
            prevBadRolesNr = config.badRolesNr;
            config.badRolesNr -= 1;
        }

        if (config.utils.goodRoles.contains(role)){
            config.goodRolesNr += 1;
        }else if(config.utils.badRoles.contains(role)){
            config.badRolesNr += 1;
        }

        for(ViewListRow vlr : config.data) {
            if (vlr.availableRoles != availableRoles){
                if(!selectedRole.equals(config.utils.EMPTY_FIELD) && !selectedRole.equals("Minion of Mordred") && !selectedRole.equals("Loyal Servant")){
                    vlr.availableRoles.add(selectedRole);
                }
                if (!role.equals(config.utils.EMPTY_FIELD) && !role.equals("Minion of Mordred") && !role.equals("Loyal Servant")) {
                    vlr.availableRoles.remove(role);
                }

                if (config.goodRolesNr.equals(config.utils.playerConfig.get(config.playerNr).get("good"))){
                    vlr.availableRoles.removeAll(config.utils.goodRoles);
                }else if (prevGoodRolesNr.equals(config.utils.playerConfig.get(config.playerNr).get("good"))){
                    List<String> availableGoodRoles = new LinkedList<>();
                    availableGoodRoles.addAll(config.utils.goodRoles);
                    for (ViewListRow innerVlr : config.data){
                        if(!innerVlr.selectedRole.equals("Loyal Servant")) {
                            availableGoodRoles.remove(innerVlr.selectedRole);
                        }
                    }
                    vlr.availableRoles.addAll(availableGoodRoles);
                }

                if (config.badRolesNr.equals(config.utils.playerConfig.get(config.playerNr).get("evil"))){
                    vlr.availableRoles.removeAll(config.utils.badRoles);
                }else if (prevBadRolesNr.equals(config.utils.playerConfig.get(config.playerNr).get("evil"))){
                    List<String> availableBadRoles = new LinkedList<>();
                    availableBadRoles.addAll(config.utils.badRoles);
                    for (ViewListRow innerVlr : config.data){
                        if(!innerVlr.selectedRole.equals("Minion of Mordred")) {
                            availableBadRoles.remove(innerVlr.selectedRole);
                        }
                    }
                    vlr.availableRoles.addAll(availableBadRoles);
                }
            }
        }

        selectedRole = role;
    }
}
