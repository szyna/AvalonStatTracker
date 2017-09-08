package az.avalonstattracker;

import java.util.List;

class ViewListRow {
    protected List<String> players;
    protected List<String> characters;

    ViewListRow(List<String> players, List<String> characters){
        this.players = players;
        this.characters = characters;
    }

    public void changePlayers(List<String> newPlayers){
        players.clear();
        players.addAll(newPlayers);
    }

    public String removePlayer(int index){
        return players.remove(index);
    }

    public String removeCharacter(int index){
        return characters.remove(index);
    }
}
