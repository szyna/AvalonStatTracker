package az.avalonstattracker;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


class RankingList extends ArrayAdapter<String> {

    private List<String> data;

    RankingList(Context context, List<String> data) {
        super(context, R.layout.ranking_row, R.id.ranking_text, data);
        this.data = data;
    }

    @Override
    public View getView(final int position, View view, final ViewGroup parent){
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.activity_rankings, parent, false);
        }

        TextView tvName = (TextView) view.findViewById(R.id.ranking_text);
        tvName.setText(data.get(position));

        return view;
    }

}
