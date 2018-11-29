package ec.edu.epn.findme;

import android.app.Activity;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ec.edu.epn.findme.entity.ActiveSearch;

public class NewActiveSearch extends AppCompatActivity {
    TextView fullNameTv, ageTv, descriptionTv, lastSeenLatitudeTv, lastSeenLongitudeTv;
    Spinner genderSpinner;
    Switch isFoundYetSwitch;
    Button createActiveSearchButton;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String username = user.getUid();

    private DocumentReference cityRef = db.collection("LocationData").document("Quito");


    private ActiveSearch activeSearch;
    private long numberOfActiveSearches;
    private final static String TAG = NewActiveSearch.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_active_search);
        fullNameTv = (TextView)findViewById(R.id.as_full_name);
        ageTv = (TextView)findViewById(R.id.as_age);
        descriptionTv = (TextView)findViewById(R.id.as_description);
        lastSeenLatitudeTv = (TextView)findViewById(R.id.as_last_seen_latitude);
        lastSeenLongitudeTv = (TextView)findViewById(R.id.as_last_seen_longitude);
        genderSpinner = (Spinner) findViewById(R.id.as_gender);
        isFoundYetSwitch = (Switch) findViewById(R.id.as_is_found_yet_switch);
        createActiveSearchButton = (Button) findViewById(R.id.add_active_search_button);
        createActiveSearchButton.setEnabled(false);
        createActiveSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreateActiveSearch();
            }
        });
        List<String> genderList = new ArrayList<>();
        genderList.add("Masculino");
        genderList.add("Femenino");

        //declare an adapter with a simple format and source which is genderList above
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,android.R.layout.simple_spinner_item,genderList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        getCurrentNumberOfActiveSearches();
    }


    private void getCurrentNumberOfActiveSearches() {
        Intent intent = getIntent();
        if(intent.hasExtra("numberOfAlerts")){
            numberOfActiveSearches = intent.getExtras().getLong("numberOfActiveSearches");
        }else{
            cityRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if(document.getLong("numberOfActiveSearches")!=null){
                            numberOfActiveSearches = document.getLong("numberOfActiveSearches");
                            createActiveSearchButton.setEnabled(true);
                        }
                    }
                }
            });
        }

    }

    private void attemptCreateActiveSearch() {
        Long numerofActiveSearchesDouble = new Long(numberOfActiveSearches);
        if(numerofActiveSearchesDouble == null){
            Toast.makeText(NewActiveSearch.this,"Obteniendo actualizaciones de búsquedas activas",Toast.LENGTH_LONG).show();
            return;
        }
        //alertType.setError(null);
        fullNameTv.setError(null);
        ageTv.setError(null);
        descriptionTv.setError(null);
        lastSeenLatitudeTv.setError(null);
        lastSeenLongitudeTv.setError(null);

        boolean alertReviewed = false;
        
        GeoPoint alertGeopoint;

        Intent intent = getIntent();
        if(intent.hasExtra("usuarioDinased")){
            if(!intent.getExtras().getBoolean("usuarioDinased")){
                finishActivity(1);
                finish();
            }

        }

        if(intent.hasExtra("alertLatitude")){
            alertGeopoint = new GeoPoint(intent.getExtras().getDouble("alertLatitude"),intent.getExtras().getDouble("alertLongitude"));
        }else{
            alertGeopoint = new GeoPoint(0,0);
        }

        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(fullNameTv.getText().toString())){
            focusView = fullNameTv;
            fullNameTv.setError(getResources().getString(R.string.full_name_empty));
            cancel = true;
        }
        if(TextUtils.isEmpty(ageTv.getText().toString())){
            focusView = ageTv;
            ageTv.setError(getResources().getString(R.string.age_empty));
            cancel = true;
        }
        if(TextUtils.isEmpty(descriptionTv.getText().toString())){
            focusView = descriptionTv;
            descriptionTv.setError(getResources().getString(R.string.description_empty));
            cancel = true;
        }
        if(TextUtils.isEmpty(lastSeenLatitudeTv.getText().toString())){
            focusView = lastSeenLatitudeTv;
            lastSeenLatitudeTv.setError(getResources().getString(R.string.description_empty));
            cancel = true;
        }
        if(TextUtils.isEmpty(lastSeenLongitudeTv.getText().toString())){
            focusView = lastSeenLongitudeTv;
            lastSeenLongitudeTv.setError(getResources().getString(R.string.description_empty));
            cancel = true;
        }
        if(cancel){
            focusView.requestFocus();
        }else{
            GeoPoint lastSeen = new GeoPoint(Double.parseDouble(lastSeenLatitudeTv.getText().toString()),Double.parseDouble(lastSeenLongitudeTv.getText().toString()));
            String gender = genderSpinner.getSelectedItem().toString().equals("Masculino")?"M":"F";
            activeSearch =  new ActiveSearch(fullNameTv.getText().toString(),gender,
                    lastSeen,Long.parseLong(ageTv.getText().toString()),descriptionTv.getText().toString(),true,String.valueOf(numberOfActiveSearches+1),isFoundYetSwitch.isChecked(),false);
            Map<String,Object> activeSearchToSend = new HashMap<>();
            activeSearchToSend.put("active",activeSearch.isActive());
            activeSearchToSend.put("age",activeSearch.getAge());
            activeSearchToSend.put("description",activeSearch.getDescription());
            activeSearchToSend.put("gender",activeSearch.getGender());
            activeSearchToSend.put("isFoundYet",activeSearch.getIsFoundYet());
            activeSearchToSend.put("name",activeSearch.getName());
            activeSearchToSend.put("ultimoAvistamiento",activeSearch.getUltimoAvistamiento());
            //activeSearchToSend.put("alertTimeMillis",alert.getAlertTimeMillis());

            Log.d(TAG, "This is what we are sending:" + activeSearch);

            cityRef.collection("ActiveSearches").document(activeSearch.getId()).set(activeSearchToSend).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(NewActiveSearch.this,"Búsqueda agregada",Toast.LENGTH_LONG).show();
                    numberOfActiveSearches+=1;
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
        Map<String,Object> numberOfActiveSearchesObj = new HashMap<>();
        numberOfActiveSearchesObj.put("numberOfActiveSearches",numberOfActiveSearches);

        cityRef.set(numberOfActiveSearchesObj, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(NewActiveSearch.this,"Base de datos actualizada",Toast.LENGTH_LONG).show();
                /*Bundle bundle = new Bundle();
                bundle.putStringArrayList("selectedActiveSearchIds",activeSearches);
                bundle.putString("Uid", username);

                Intent intent = new Intent(NewAlertActivity.this,MapsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);*/
                Intent intentCarryingAlertData = new Intent();
                Bundle bundle = new Bundle();
                bundle.putInt("createdActiveSearchId",(int)numberOfActiveSearches);
                //bundle.putDouble("alertLongitude",alert.getLocationLatLng().longitude);
                intentCarryingAlertData.putExtras(bundle);
                setResult(Activity.RESULT_OK, intentCarryingAlertData);
                finish();
            }
        });

    }
}
