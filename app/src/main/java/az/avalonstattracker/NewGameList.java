package az.avalonstattracker;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;



public class NewGameList extends ArrayAdapter<ViewListRow> {

    private String TAG = "XD";
    private List<ViewListRow> data;
    private ViewListRow initialData;
    private final Activity context;
    private List<String> selectedPlayers;
    ListView lv;

    public NewGameList(Activity context, List<ViewListRow> data, ListView lv){
        super(context, R.layout.viewlist_row, data);
        this.data = data;
        this.initialData = new ViewListRow(new ArrayList<>(data.get(0).players), new ArrayList<>(data.get(0).characters));
        this.context = context;
        this.lv = lv;
        this.selectedPlayers = new ArrayList<>();
        for (int i=0; i<data.size(); i++){
            selectedPlayers.add(data.get(0).players.get(0));
        }
    }

    @Override
    public View getView(final int position, View view, final ViewGroup parent){
        class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
            private boolean userSelect = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                userSelect = true;
                return false;
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (userSelect) {
                    Log.d(TAG, "$$$$$$$$$$$$$$$$$$$$");
                    List<String> availablePlayers = new ArrayList<>(initialData.players);
                    selectedPlayers.set(position, data.get(position).players.get(pos));
                    for (String p : selectedPlayers) {
                        availablePlayers.remove(p);
                    }
                    Log.d(TAG, selectedPlayers.toString());
                    Log.d(TAG, availablePlayers.toString());
                    View listItem;
                    Spinner s;
                    List<String> temp;
                    for (int r = 0; r < lv.getChildCount(); r++) {
                        temp = new ArrayList<>(availablePlayers);
                        temp.add(0, selectedPlayers.get(r));
                        Log.d(TAG, temp.toString());
                        data.get(position).changePlayers(temp);
                        listItem = lv.getChildAt(r);
                        s = listItem.findViewById(R.id.playerSpinner);
                        s.setSelection(0, false);
                        Log.d(TAG, s.getSelectedItem().toString());
                    }

                    userSelect = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        }

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.viewlist_row, parent, false);

        Spinner playerSpinner = rowView.findViewById(R.id.playerSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, data.get(position).players);
        ArrayAdapter.createFromResource(context, R.array.roles, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerSpinner.setAdapter(adapter);
        SpinnerInteractionListener s = new SpinnerInteractionListener();
        playerSpinner.setOnItemSelectedListener(s);
        playerSpinner.setOnTouchListener(s);

        Spinner characterSpinner = rowView.findViewById(R.id.characterSpinner);
        adapter = new ArrayAdapter<>(context, R.layout.spinner_item, data.get(position).characters);
        ArrayAdapter.createFromResource(context, R.array.roles, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        characterSpinner.setAdapter(adapter);

        return rowView;
    }
}
