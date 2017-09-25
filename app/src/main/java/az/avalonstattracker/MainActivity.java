package az.avalonstattracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public String TAG = "MainActivity";
    Utilities utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        utils = new Utilities(this);
    }

    public void addGame(View view){
        AddGameActivity.utils = utils;
        Intent intent = new Intent(this, AddGameActivity.class);
        startActivity(intent);
    }
}
