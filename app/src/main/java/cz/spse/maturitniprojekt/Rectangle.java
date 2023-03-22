package cz.spse.maturitniprojekt;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class Rectangle extends LinearLayout {
    private Button settings;
    private TextView name;
    private Switch state;
    private ImageView image;

    public Rectangle(Context context) {
        super(context);
        initializeViews(context);
    }
    public Rectangle(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public Rectangle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


    }


}
