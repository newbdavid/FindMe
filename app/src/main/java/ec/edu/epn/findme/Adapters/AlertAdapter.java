package ec.edu.epn.findme.Adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
        public CheckBox reviewed;

        public ViewHolder(View view){
            super(view);
            type = (TextView) view.findViewById(R.id.txtTipoAlerta);
            title = (TextView) view.findViewById(R.id.txtTituloAlerta);
            description = (TextView) view.findViewById(R.id.txtAlertDescription);
            location = (TextView) view.findViewById(R.id.txtAlertLocation);
            status = (TextView) view.findViewById(R.id.txtEstatusAlerta);
            typeImageView = (ImageView) view.findViewById(R.id.alertImageView);
            reviewed =(CheckBox) view.findViewById(R.id.chkAlert);
        }
    }

    public AlertAdapter(List<Alert> alertsList){
        //super(context,alertsList);
        this.alertsList = alertsList;
    }

    @Override
    public AlertAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_alert_item,parent,false);
        return new AlertAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AlertAdapter.ViewHolder holder, final int position) {
        Alert alert = alertsList.get(position);
        String alertType = "Alert Type: ";
        holder.type.setText(alertType+alert.getAlertType());
        holder.title.setText(alert.getTitle());
        String alertDescription = "Description: ";
        holder.description.setText(alertDescription+alert.getDescription());
        holder.location.setText("Latitud: "+alert.getLocation().getLatitude()+" Longitud: "+alert.getLocation().getLongitude());
        String alertStatus = "Status: ";
        holder.status.setText(alert.getStatus());
        if(alert.getStatus().equals("Pending")){
            holder.status.setTextColor(Color.parseColor("#3867e0"));
        } else if (alert.getStatus().equals("Rejected")){
            holder.status.setTextColor(Color.parseColor("#ef6621"));
        } else {
            holder.status.setTextColor(Color.parseColor("#2ba522"));
        }


        if(alert.getAlertType().equals("Avistamiento")){
            holder.typeImageView.setImageResource(R.drawable.ic_binocular_avistamiento_background);
        }

        holder.reviewed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                AlertAdapter.this.alertsList.get(position).setReviewed(b);
            }
        });
        if(alert.isReviewed()){
            holder.reviewed.setChecked(true);
        }
    }

    @Override
    public int getItemCount() {
        return alertsList.size();
    }
}
