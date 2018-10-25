package ec.edu.epn.findme.Adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import ec.edu.epn.findme.R;
import ec.edu.epn.findme.entity.Alert;

public class AlertViewHolder extends RecyclerView.ViewHolder {

    private TextView type;
    private TextView title;
    private TextView description;
    private TextView location;
    private TextView status;
    private ImageView alertTypeImage;
    private CheckBox reviewed;


    public AlertViewHolder(View itemView) {
        super(itemView);
        type = (TextView) itemView.findViewById(R.id.txtTipoAlerta);
        title = (TextView) itemView.findViewById(R.id.txtTituloAlerta);
        description = (TextView) itemView.findViewById(R.id.txtAlertDescription);
        location = (TextView) itemView.findViewById(R.id.alert_location);
        status = (TextView) itemView.findViewById(R.id.txtEstatusAlerta);
        alertTypeImage = (ImageView) itemView.findViewById(R.id.alertImageView);
        reviewed = (CheckBox) itemView.findViewById(R.id.chkAlert);
    }

    public void bindAlertToListener(final Alert alert, AlertAdapter.OnItemClickListener listener){
        String alertType = "Alert Type: ";
        type.setText(alertType+alert.getAlertType());
        title.setText(alert.getTitle());
        String alertDescription = "Description: ";
        description.setText(alertDescription+alert.getDescription());
        location.setText("Latitud: "+alert.getLocation().getLatitude()+" Longitud: "+alert.getLocation().getLongitude());
        String alertStatus = "Status: ";
        status.setText(alert.getStatus());

        if(alert.getStatus().equals("Pending")){
            status.setTextColor(Color.parseColor("#3867e0"));
        } else if (alert.getStatus().equals("Rejected")){
            status.setTextColor(Color.parseColor("#ef6621"));
        } else {
            status.setTextColor(Color.parseColor("#2ba522"));
        }


        if(alert.getAlertType().equals("Avistamiento")){
            alertTypeImage.setImageResource(R.drawable.ic_binocular_avistamiento_background);
        }

        reviewed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                alert.setReviewed(b);
            }
        });
        if(alert.isReviewed()){
            reviewed.setChecked(true);
        }
    }
}
