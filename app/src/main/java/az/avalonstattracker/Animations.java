package az.avalonstattracker;


import android.content.Context;
import android.graphics.Color;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


class Animations {

    static void addFadeOutTextAnimation(Context context, RelativeLayout rl, String text, boolean success){
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);

        if (success) {
            toast.getView().setBackgroundColor(Color.CYAN);
            v.setTextColor(0xFF4176d2);
        }else{
            toast.getView().setBackgroundColor(Color.CYAN);
            v.setTextColor(0xFFBF0003);
        }

        toast.show();

    }

}
