package az.avalonstattracker;

import android.app.Activity;
import android.hardware.camera2.CameraManager;
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
import java.util.LinkedList;
import java.util.List;



public class NewGameList extends ArrayAdapter<ViewListRow> {

    private String TAG = "XD";
    private final Activity context;
    private GameConfiguration config;
    ListView lv;

    public NewGameList(Activity context, ListView lv, GameConfiguration config){
        super(context, R.layout.viewlist_row, config.data);
        this.context = context;
        this.lv = lv;
        this.config = config;
    }

    public List<ViewListRow> getData() { return config.data; }

    public void changeDataSize(int newSize){
        int prevSize = config.data.size();
        config.playerNr = newSize;
        if (newSize > prevSize){
            for(int i=prevSize; i<newSize; i++){
                new ViewListRow(config.utils.EMPTY_FIELD, config.utils.EMPTY_FIELD, config);
            }
        }else if (newSize < prevSize){
            for(int i=prevSize-1; i>=newSize; i--){
                config.data.get(i).setPlayer(config.utils.EMPTY_FIELD);
                config.data.get(i).setRole(config.utils.EMPTY_FIELD);
                config.data.remove(i);
            }
        }
    }

    @Override
    public View getView(final int position, View view, final ViewGroup parent){
        class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
            private boolean userSelect = false;
            private boolean playerSpinner;

            SpinnerInteractionListener(boolean playerSpinner){
                this.playerSpinner = playerSpinner;
                // TODO do this in more elegant way, such us passing method reference (Command pattern)
            }

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                userSelect = true;
                return false;
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (userSelect) {
                    Spinner s;
                    if (playerSpinner){
                        s = lv.getChildAt(position).findViewById(R.id.playerSpinner);
                        config.data.get(position).setPlayer(s.getSelectedItem().toString());
                    }else{
                        s = lv.getChildAt(position).findViewById(R.id.characterSpinner);
                        config.data.get(position).setRole(s.getSelectedItem().toString());
                    }

                    // TODO something does not work here properly sometimes
                    userSelect = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        }

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.viewlist_row, parent, false);

        ViewListRow vlr = config.data.get(position);

        Spinner playerSpinner = rowView.findViewById(R.id.playerSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, vlr.availablePlayers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerSpinner.setAdapter(adapter);
        playerSpinner.setSelection(vlr.availablePlayers.indexOf(vlr.selectedPlayer));

        SpinnerInteractionListener s = new SpinnerInteractionListener(true);
        playerSpinner.setOnItemSelectedListener(s);
        playerSpinner.setOnTouchListener(s);

        Spinner characterSpinner = rowView.findViewById(R.id.characterSpinner);
        adapter = new ArrayAdapter<>(context, R.layout.spinner_item, vlr.availableRoles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        characterSpinner.setAdapter(adapter);
        s = new SpinnerInteractionListener(false);
        characterSpinner.setOnItemSelectedListener(s);
        characterSpinner.setOnTouchListener(s);
        Log.d(TAG, vlr.selectedRole + " " + vlr.availableRoles);
        characterSpinner.setSelection(vlr.availableRoles.indexOf(vlr.selectedRole));

        return rowView;
    }

}
