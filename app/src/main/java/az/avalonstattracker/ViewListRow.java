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
    }

    void setPlayer(String player){
        if (!selectedPlayer.equals("")){
            config.availablePlayers.add(selectedPlayer);
        }
        selectedPlayer = player;
        config.availablePlayers.remove(player);
    }

    void setRole(String role){
        Integer prevGoodRolesNr = 0, prevBadRolesNr = 0;
        if(!selectedRole.equals("") && !selectedRole.equals("Minion of Mordred") && !selectedRole.equals("Loyal Servant")){
            config.availableRoles.add(selectedRole);
        }

        if (config.goodRoles.contains(selectedRole)){
            prevGoodRolesNr = config.goodRolesNr;
            config.goodRolesNr -= 1;
        }else if(config.badRoles.contains(selectedRole)){
            prevBadRolesNr = config.badRolesNr;
            config.badRolesNr -= 1;
        }

        selectedRole = role;

        if (config.goodRoles.contains(selectedRole)){
            config.goodRolesNr += 1;
        }else if(config.badRoles.contains(selectedRole)){
            config.badRolesNr += 1;
        }

        if (!selectedRole.equals("") && !selectedRole.equals("Minion of Mordred") && !selectedRole.equals("Loyal Servant")) {
            config.availableRoles.remove(role);
        }

        if (config.badRolesNr.equals(config.playerConfig.get(config.playerNr).get("bad"))){
            config.availableRoles.removeAll(config.badRoles);
        }else if (prevBadRolesNr.equals(config.playerConfig.get(config.playerNr).get("bad"))){
            List<String> availableBadRoles = new LinkedList<>();
            availableBadRoles.addAll(config.badRoles);
            for (ViewListRow vlr : config.data){
                if(!vlr.selectedRole.equals("Minion of Mordred")) {
                    availableBadRoles.remove(vlr.selectedRole);
                }
            }
            config.availableRoles.addAll(availableBadRoles);
        }

        // TODO copy pasta ewwww
        // TODO its bugged if 3 good are first and then 2 bad
        if (config.goodRolesNr.equals(config.playerConfig.get(config.playerNr).get("good"))){
            config.availableRoles.removeAll(config.goodRoles);
        }else if (prevGoodRolesNr.equals(config.playerConfig.get(config.playerNr).get("good"))){
            List<String> availableGoodRoles = new LinkedList<>();
            availableGoodRoles.addAll(config.goodRoles);
            for (ViewListRow vlr : config.data){
                if(!vlr.selectedRole.equals("Loyal Servant")) {
                    availableGoodRoles.remove(vlr.selectedRole);
                }
            }
            config.availableRoles.addAll(availableGoodRoles);
        }
    }
}
