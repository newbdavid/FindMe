package ec.edu.epn.findme.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ec.edu.epn.findme.R;
import ec.edu.epn.findme.entity.Alert;

/**
 * Created by David Moncayo on 19/04/2018.
 */

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder>{

    private List<Alert> alertsList;

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView type, title, description, location, status;
        public ImageView typeImageView;
        public CheckBox approved;

        public ViewHolder(View view){
            super(view);
            type = (TextView) view.findViewById(R.id.txtTipoAlerta);
            title = (TextView) view.findViewById(R.id.txtTituloAlerta);
            description = (TextView) view.findViewById(R.id.txtDescription);
            location = (TextView) view.findViewById(R.id.txtAlertLocation);
            status = (TextView) view.findViewById(R.id.txtEstatusAlerta);
            typeImageView = (ImageView) view.findViewById(R.id.alertImageView);
            approved =(CheckBox) view.findViewById(R.id.chkAlert);
        }
    }

    public AlertAdapter(List<Alert> alertsList){
        this.alertsList = alertsList;
    }

    @Override
    public AlertAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(AlertAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
