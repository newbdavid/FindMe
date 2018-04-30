package ec.edu.epn.findme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ec.edu.epn.findme.entity.Alert;

public class NewAlertActivity extends AppCompatActivity {
    Spinner alertType;
    TextView alertTitle;
    TextView alertDescription;
    TextView alertLocation;
    Button sendAlertButton;
    
    //FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String username = user.getUid();
    CollectionReference usuarios = db.collection("LocationData").document("Quito").collection("usuarios");

    Alert alert;
    private int numberOfAlerts;
    ArrayList<String> activeSearches;
    private final static String TAG = NewAlertActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_alert);
        alertType =  (Spinner) findViewById(R.id.alert_type);
        alertTitle = (TextView) findViewById(R.id.alert_title);
        alertDescription = (TextView) findViewById(R.id.alert_description);
        alertLocation = (TextView) findViewById(R.id.alert_location);
        sendAlertButton = (Button) findViewById(R.id.send_new_alert_button);
        sendAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                attemptSendAlert();
            }
        });
        List<String> alertTypeList = new ArrayList<>();
        alertTypeList.add("Pista");
        alertTypeList.add("Avistamiento");

        //declare an adapter with a simple format and source which is alertTypeList above
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,android.R.layout.simple_spinner_item,alertTypeList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alertType.setAdapter(adapter);

        getCurrentNumberOfAlerts();
        Intent intent = getIntent();
        GeoPoint alertGeopoint;
        if(intent.hasExtra("alertLatitude")){
            alertGeopoint = new GeoPoint(intent.getExtras().getDouble("alertLatitude"),intent.getExtras().getDouble("alertLongitude"));
        }else{
            alertGeopoint = new GeoPoint(0,0);
        }
        alertLocation.setText("Latitude: "+alertGeopoint.getLatitude()+", Longitude: "+alertGeopoint.getLongitude());


    }

    private void getCurrentNumberOfAlerts() {
        Intent intent = getIntent();
        if(intent.hasExtra("numberOfAlerts")){
            numberOfAlerts = intent.getExtras().getInt("numberOfAlerts");
        }else{
            usuarios.document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if(document.getLong("numberOfAlerts")!=null){
                            numberOfAlerts = document.getLong("numberOfAlerts").intValue();
                        }
                    }
                }
            });
        }

    }

    private void attemptSendAlert() {
        //alertType.setError(null);
        alertTitle.setError(null);
        alertDescription.setError(null);
        alertLocation.setError(null);
        sendAlertButton.setError(null);

        boolean alertReviewed = false;
        String alertStatus = "Pending";
        String alertTypestr = "Avistamiento";
        GeoPoint alertGeopoint;

        Intent intent = getIntent();
        if(intent.hasExtra("usuarioDinased")){
            if(intent.getExtras().getBoolean("usuarioDinased")){
                alertReviewed = true;
                alertStatus = "Checked";
            }

        }
        if(alertType.getSelectedItem().toString().equals("Pista")){
            alertTypestr = "Pista";
        }
        if(intent.hasExtra("alertLatitude")){
            alertGeopoint = new GeoPoint(intent.getExtras().getDouble("alertLatitude"),intent.getExtras().getDouble("alertLongitude"));
        }else{
            alertGeopoint = new GeoPoint(0,0);
        }
        if(intent.hasExtra("selectedActiveSearchIds")){
            activeSearches = intent.getExtras().getStringArrayList("selectedActiveSearchIds");
        }
        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(alertTitle.getText().toString())){
            focusView = alertTitle;
            alertTitle.setError("Title shouldn't be empty");
            cancel = true;
        }
        if(TextUtils.isEmpty(alertDescription.getText().toString())){
            focusView = alertDescription;
            alertDescription.setError("Description shouldn't be empty");
            cancel = true;
        }
        if(cancel){
            focusView.requestFocus();
        }else{
            alert =  new Alert(alertReviewed,alertStatus,username,alertTypestr,alertTitle.getText().toString(),
                    alertDescription.getText().toString(),alertGeopoint,System.currentTimeMillis());
            Map<String,Object> alertToSend = new HashMap<>();
            alertToSend.put("reviewed",alert.isReviewed());
            alertToSend.put("status",alert.getStatus());
            alertToSend.put("ownerUid",alert.getOwnerUid());
            alertToSend.put("alertType",alert.getAlertType());
            alertToSend.put("title",alert.getTitle());
            alertToSend.put("description",alert.getDescription());
            alertToSend.put("location",alert.getLocation());
            alertToSend.put("alertTimeMillis",alert.getAlertTimeMillis());
            numberOfAlerts+=1;
            Log.d(TAG, "This is what we are sending:" + alert);

           usuarios.document(username).collection("alerts").document("alert"+numberOfAlerts).set(alertToSend).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(NewAlertActivity.this,"Alerta agregada",Toast.LENGTH_LONG).show();
                    addNumberOfAlertsToFirebase();
                }
            }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
                   Log.w(TAG, "Error writing document", e);
               }
           });




        }



    }

    private void addNumberOfAlertsToFirebase() {
        Map<String,Object> numberOfAlertsObj = new HashMap<>();
        numberOfAlertsObj.put("numberOfAlerts",numberOfAlerts);

        usuarios.document(username).set(numberOfAlertsObj, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(NewAlertActivity.this,"Base de datos actualizada",Toast.LENGTH_LONG).show();
                /*Bundle bundle = new Bundle();
                bundle.putStringArrayList("selectedActiveSearchIds",activeSearches);
                bundle.putString("Uid", username);

                Intent intent = new Intent(NewAlertActivity.this,MapsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);*/
                finishActivity(1);
            }
        });

    }
}
