package cz.spse.maturitniprojekt;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class Ledinroom extends LinearLayout {
    private TextView name;
    private Switch state;
    private ImageView image;

    public Ledinroom(Context context) {
        super(context);
        initializeViews(context);
    }
    public Ledinroom(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public Ledinroom(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.ledinroom_view, this);

        this.state = findViewById(R.id.inRoom);
        this.name = findViewById(R.id.name);
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public void setState(boolean state) {
        this.state.setChecked(state);
    }

    public boolean getState() {
        return this.state.isChecked();
    }


}
