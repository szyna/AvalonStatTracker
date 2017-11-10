package az.avalonstattracker;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


class Animations {

    static class FadeOutAnimationListener implements Animation.AnimationListener{

        private View v;

        private FadeOutAnimationListener(View v){
            this.v = v;
        }

        @Override
        public void onAnimationStart(Animation animation) { }

        @Override
        public void onAnimationEnd(Animation animation) {
            v.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) { }
    }

    static void addFadeOutTextAnimation(Context context, RelativeLayout rl, String text, boolean success){
        LinearLayout ll = (LinearLayout) View.inflate(context, R.layout.info_view, null);
        GradientDrawable gd = ((GradientDrawable) ll.getBackground());
        TextView tv = ll.findViewById(R.id.infoView);
        tv.setText(text);

        if (success) {
            gd.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }else{
            gd.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        }

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        rlp.setMargins(0, 0, 0, (int) (size.y * 0.1));
        rl.addView(ll, rlp);

        Animation a = AnimationUtils.loadAnimation(context, R.anim.fade_out_animation);
        a.setAnimationListener(new FadeOutAnimationListener(ll));
        ll.setAnimation(a);

    }

}