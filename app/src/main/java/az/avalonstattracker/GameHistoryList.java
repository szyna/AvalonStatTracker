package az.avalonstattracker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class GameHistoryList extends BaseExpandableListAdapter {

    private Context context;
    private Utilities utils;
    private List<String> headerData;
    private HashMap<String, List<Map.Entry<String, String>>> childData;

    GameHistoryList(Context context, List<String> headerData, HashMap<String, List<Map.Entry<String, String>>> childData, Utilities utils){
        this.context = context;
        this.headerData = headerData;
        this.childData = childData;
        this.utils = utils;
    }

    @Override
    public int getGroupCount() {
        return headerData.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return childData.get(headerData.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return headerData.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return childData.get(headerData.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int i, boolean b, View view, ViewGroup viewGroup) {
        final String headerTitle = (String) getGroup(i);
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.gamehistory_group, null);

            ImageButton deleteButton = view.findViewById(R.id.game_history_delete_btn);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO this is kinda dumb again, should pass id of Games to List rather than passing it in string
                    Pattern pattern = Pattern.compile("\\[(\\d+)\\]");
                    String header = (String) getGroup(i);
                    Matcher m = pattern.matcher(header);

                    while(m.find()){
                        utils.dbHelper.removeHistoryGame(m.group(1));
                        headerData.remove(header);
                        childData.remove(header);
                    }

                    notifyDataSetChanged();

                }
            });
        }

        TextView lblListHeader = view.findViewById(R.id.game_history_header);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
        if (headerTitle.contains("Good")){
            lblListHeader.setBackgroundColor(Color.parseColor("#38B6E0"));
        }else{
            lblListHeader.setBackgroundColor(Color.parseColor("#CC122B"));
        }


        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        final Map.Entry<String, String> childText = (Map.Entry<String, String>) getChild(i, i1);

        if (view == null) {
            LayoutInflater inf = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.gamehistory_item, null);
        }

        TextView txtListChild = view.findViewById(R.id.game_history_item_text);

        txtListChild.setText(childText.getKey() + " - " + childText.getValue());
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
