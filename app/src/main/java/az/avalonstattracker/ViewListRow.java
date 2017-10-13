package az.avalonstattracker;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ViewListRow {
    String selectedPlayer;
    String selectedRole;
    GameConfiguration config;

    ViewListRow(String selectedPlayer, String selectedRole, GameConfiguration config){
        this.selectedPlayer = selectedPlayer;
        this.selectedRole = selectedRole;
        this.config = config;
        if (!selectedPlayer.equals(config.utils.EMPTY_FIELD)){
            config.availablePlayers.remove(this.selectedPlayer);
        }
    }

    void setPlayer(String player){
        if (!selectedPlayer.equals(config.utils.EMPTY_FIELD)){
            config.availablePlayers.add(selectedPlayer);
        }
        selectedPlayer = player;
        if (!selectedPlayer.equals(config.utils.EMPTY_FIELD)) {
            config.availablePlayers.remove(player);
        }
    }

    void setRole(String role){
        Integer prevGoodRolesNr = 0, prevBadRolesNr = 0;
        if(!selectedRole.equals(config.utils.EMPTY_FIELD) && !selectedRole.equals("Minion of Mordred") && !selectedRole.equals("Loyal Servant")){
            config.availableRoles.add(selectedRole);
        }

        if (config.utils.goodRoles.contains(selectedRole)){
            prevGoodRolesNr = config.goodRolesNr;
            config.goodRolesNr -= 1;
        }else if(config.utils.badRoles.contains(selectedRole)){
            prevBadRolesNr = config.badRolesNr;
            config.badRolesNr -= 1;
        }

        selectedRole = role;

        if (config.utils.goodRoles.contains(selectedRole)){
            config.goodRolesNr += 1;
        }else if(config.utils.badRoles.contains(selectedRole)){
            config.badRolesNr += 1;
        }

        if (!selectedRole.equals(config.utils.EMPTY_FIELD) && !selectedRole.equals("Minion of Mordred") && !selectedRole.equals("Loyal Servant")) {
            config.availableRoles.remove(role);
        }

        if (config.badRolesNr.equals(config.utils.playerConfig.get(config.playerNr).get("evil"))){
            config.availableRoles.removeAll(config.utils.badRoles);
        }else if (prevBadRolesNr.equals(config.utils.playerConfig.get(config.playerNr).get("evil"))){
            List<String> availableBadRoles = new LinkedList<>();
            availableBadRoles.addAll(config.utils.badRoles);
            for (ViewListRow vlr : config.data){
                if(!vlr.selectedRole.equals("Minion of Mordred")) {
                    availableBadRoles.remove(vlr.selectedRole);
                }
            }
            config.availableRoles.addAll(availableBadRoles);
        }

        // TODO copy pasta ewwww
        if (config.goodRolesNr.equals(config.utils.playerConfig.get(config.playerNr).get("good"))){
            config.availableRoles.removeAll(config.utils.goodRoles);
        }else if (prevGoodRolesNr.equals(config.utils.playerConfig.get(config.playerNr).get("good"))){
            List<String> availableGoodRoles = new LinkedList<>();
            availableGoodRoles.addAll(config.utils.goodRoles);
            for (ViewListRow vlr : config.data){
                if(!vlr.selectedRole.equals("Loyal Servant")) {
                    availableGoodRoles.remove(vlr.selectedRole);
                }
            }
            config.availableRoles.addAll(availableGoodRoles);
        }
    }
}
