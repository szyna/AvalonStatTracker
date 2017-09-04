package az.avalonstattracker;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.List;



public class NewGameList extends ArrayAdapter {

    private List<List<String>> players;
    private final List<List<String>> characters;
    private final Activity context;

    public NewGameList(Activity context, List<List<String>> players, List<List<String>> characters){
        super(context, R.layout.viewlist_row);
        this.players = players;
        this.characters = characters;
        this.context = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.viewlist_row, null, false);

        Spinner playerSpinner = rowView.findViewById(R.id.playerSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(), R.layout.spinner_item, players.get(position));
        ArrayAdapter.createFromResource(this.getContext(), R.array.roles, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerSpinner.setAdapter(adapter);

        Spinner characterSpinner = rowView.findViewById(R.id.characterSpinner);
        adapter = new ArrayAdapter<>(this.getContext(), R.layout.spinner_item, characters.get(position));
        ArrayAdapter.createFromResource(this.getContext(), R.array.roles, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        characterSpinner.setAdapter(adapter);

        return rowView;
    }
}
