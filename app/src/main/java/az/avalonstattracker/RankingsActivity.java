package az.avalonstattracker;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.content.Context;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.content.res.Resources.Theme;

import android.widget.TextView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RankingsActivity extends AppCompatActivity {

    static Utilities utils = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rankings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup spinner
        String[] t1 = {"Overall", "Good", "Evil"};
        String[] t2 = {"winrate", "duo winrate"};
        List<String> rankings = new LinkedList<>();

        for(String s2: t2){
            for(String s1: t1){
                rankings.add(MessageFormat.format("{0} {1}", s1, s2));
            }
        }
        rankings.add("Top killer");
        rankings.add("Top saboteur");
        rankings.add("Top M&P");
        rankings.add("Top secret Merlin");

        List<String> roles = new ArrayList<>();
        roles.addAll(utils.badRoles);
        roles.addAll(utils.goodRoles);
        for(String s: roles){
            rankings.add("Top " + s);
        }

        Spinner spinner = (Spinner) findViewById(R.id.ranking_spinner);
        spinner.setAdapter(new MyAdapter(
                toolbar.getContext(),
                rankings.toArray(new String[0])));

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance((String)parent.getItemAtPosition(position)))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private static class MyAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        public MyAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_RANKING_NAME = "ranking_name";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(String rankingName) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_RANKING_NAME, rankingName);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_rankings, container, false);

            String rankingName = getArguments().getString(ARG_RANKING_NAME);
            List<String> data;
            List<String> rankings = new LinkedList<>();
            switch (rankingName){
                case "Overall winrate": data = utils.dbHelper.getTopWinrate("overall");
                    break;
                case "Good winrate": data = utils.dbHelper.getTopWinrate("good");
                    break;
                case "Evil winrate": data = utils.dbHelper.getTopWinrate("evil");
                    break;
                case "Overall duo winrate": data = utils.dbHelper.getTopDuoWinrate("overall");
                    break;
                case "Good duo winrate": data = utils.dbHelper.getTopDuoWinrate("good");
                    break;
                case "Evil duo winrate": data = utils.dbHelper.getTopDuoWinrate("evil");
                    break;
                case "Top killer": data = utils.dbHelper.getTopKills("Assassin", true);
                    break;
                case "Top saboteur": data = utils.dbHelper.getTopSaboteur();
                    break;
                case "Top M&P": data = utils.dbHelper.getTopDuoWinrate("M&P");
                    break;
                case "Top secret Merlin": data = utils.dbHelper.getTopKills("Merlin", false);
                    break;
                default:
                    data = utils.dbHelper.getTopWinrate(rankingName.split("Top ")[1]); // Top character winrate
                    break;
            }

            int place = 0;
            String prevPercent = "";
            //TODO dont retrieve it via regex
            Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)%");
            Matcher m;
            String placeString;

            for(String e: data){
                m = pattern.matcher(e);
                if(m.find() && !m.group(1).equals(prevPercent)){
                    place += 1;
                    prevPercent = m.group(1);
                    placeString = MessageFormat.format("{0}. ", place);
                }else{
                    //TODO this feels stupid
                    placeString = "     ";
                }
                rankings.add(MessageFormat.format("{0}{1}", placeString, e));
            }

            ListView lv = rootView.findViewById(R.id.ranking_lv);
            lv.setAdapter(new ArrayAdapter<>(container.getContext(), R.layout.ranking_row, rankings));
            lv.setOnTouchListener(new View.OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            setListViewHeightBasedOnChildren(lv);

            return rootView;
        }
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, Toolbar.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
