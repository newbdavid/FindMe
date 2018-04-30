package ec.edu.epn.findme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ec.edu.epn.findme.Adapters.AlertAdapter;
import ec.edu.epn.findme.entity.Alert;

public class Alerts extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Alert> alertsList = new ArrayList<>();
    private ArrayList<String> idsActiveSearches;
    private Button btnAddAlert,btnApproveAlert,btnRejectAlert;
    private TextView tvalertsTitle;
    private static final String TAG = Alerts.class.getSimpleName();

    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String username = user.getUid();
    CollectionReference usuarios = db.collection("LocationData").document("Quito").collection("usuarios");

    private GeoPoint mLastKnownLocation;
    private int numberOfAlerts;


    private final static long SINCE_20_YEARS_AGO = 63070000;
    private long timeDiff = 864000000;//10 days
    private boolean isUsuarioDinased = false;
    private boolean approveAlert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alertas);
        recyclerView = (RecyclerView) findViewById(R.id.alerts_recycle_view);
        btnAddAlert =(Button) findViewById(R.id.add_new_alert);
        btnApproveAlert = (Button) findViewById(R.id.approve_alert);
        btnRejectAlert = (Button) findViewById(R.id.reject_alert);
        tvalertsTitle = (TextView) findViewById(R.id.alerts_title_tv);
        Intent intent = getIntent();
        if(intent.hasExtra("selectedActiveSearchIds")){
            idsActiveSearches = intent.getExtras().getStringArrayList("selectedActiveSearchIds");
        }
        if(intent.hasExtra("usuarioDinased")){
            isUsuarioDinased = intent.getExtras().getBoolean("usuarioDinased");
        }
        if(intent.hasExtra("alertLatitude")){
            mLastKnownLocation = new GeoPoint(intent.getExtras().getDouble("alertLatitude"),intent.getExtras().getDouble("alertLongitude"));
        }
        if(intent.hasExtra("numberOfAlerts")){
            numberOfAlerts = intent.getExtras().getInt("numberOfAlerts");
        }

        btnAddAlert.setEnabled(false);
        btnApproveAlert.setEnabled(isUsuarioDinased);
        btnRejectAlert.setEnabled(isUsuarioDinased);


        adapter =  new AlertAdapter(alertsList);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        if(intent.hasExtra("showAllAlerts")){
            if(intent.getExtras().getBoolean("showAllAlerts")){
                getUsersWithSameSearchIds();
                tvalertsTitle.setText("All Recent Alerts");
            }else{
                getAlertsFromThisUser(username,true);
            }
        }


    }

    private void getUsersWithSameSearchIds() {
        usuarios.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData() );
                        ArrayList<String> searchIdsFromForeignUser = (ArrayList<String>)document.get("activeSearches");
                        Log.d(TAG, "Contenido de este ID: "+searchIdsFromForeignUser);
                        //if(searchIdsFromForeignUser!=null){
                        for(int i =0;i<searchIdsFromForeignUser.size();i++){
                            Log.d(TAG, "idActiveSearches: "+idsActiveSearches+"ForeignUserId"+searchIdsFromForeignUser);
//                                if(Arrays.asList(idsActiveSearches).contains(searchIdsFromForeignUser.get(i).toString())){
                            if(idsActiveSearches.contains(searchIdsFromForeignUser.get(i).toString())){

                                Log.d(TAG, "Entro para traer Alerts");
                                i=searchIdsFromForeignUser.size();
                                getAlertsFromThisUser(document.getId(),false);
                            }

                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void getAlertsFromThisUser(final String id, final boolean queryAllFromThisUser) {
        Query searchAlerts;
        long longtimeToSearch;
        if(queryAllFromThisUser){
            searchAlerts = usuarios.document(id).collection("alerts");
        } else {
            longtimeToSearch = System.currentTimeMillis()-timeDiff;
            searchAlerts = usuarios.document(id).collection("alerts").whereGreaterThanOrEqualTo("alertTimeMillis",longtimeToSearch);
        }

        searchAlerts.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot document : task.getResult()){
                        Log.d(TAG, id + " and "+username);
                        /*if(id.equals(username) ){
                            currentNumberOfTracksOnFirebase+=1;
                            Log.d(TAG, document.getId() + "Current tracks: "+currentNumberOfTracksOnFirebase);
                        }*/
                        Log.d(TAG, document.getId() + " => " +  document.getData());
                        if(document.toObject(Alert.class)!= null){
                            if(document.get("status").equals("Checked")){
                                alertsList.add(document.toObject(Alert.class));
                            }

                        }
                    }
                    SetAdapter();
                }else {

                }
            }
        });

    }

    private void SetAdapter() {

        adapter.notifyDataSetChanged();
        btnAddAlert.setEnabled(true);
    }

    public void agregarNuevaAlerta(View view){
        Bundle bundle = new Bundle();
        bundle.putDouble("alertLatitude",mLastKnownLocation.getLatitude());
        bundle.putDouble("alertLongitude",mLastKnownLocation.getLongitude());
        bundle.putInt("numberOfAlerts",numberOfAlerts);
        bundle.putStringArrayList("selectedActiveSearchIds",idsActiveSearches);
        bundle.putString("Uid", username);
        bundle.putBoolean("usuarioDinased",isUsuarioDinased);

        Intent intent = new Intent(this, NewAlertActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent,1);
    }

    public void aprobarNuevaAlerta(View view){
        approveAlert = true;
        for(Alert alert : alertsList){
            if(alert.isReviewed()){
                Log.d(TAG, "Esta marcado"+alert.getOwnerUid() + " => " +  alert.getAlertTimeMillis());
                getAlertIdOnFirebase(alert.getOwnerUid(),alert.getAlertTimeMillis());
                alert.setStatus("Checked");
                adapter.notifyDataSetChanged();
            }
        }

    }

    public void rechazarNuevaAlerta(View view){
        approveAlert = false;
        for(Alert alert : alertsList){
            if(alert.isReviewed()){
                getAlertIdOnFirebase(alert.getOwnerUid(),alert.getAlertTimeMillis());
                alert.setStatus("Rejected");
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void getAlertIdOnFirebase(final String ownerUid, long alertTimeMillis) {
        usuarios.document(ownerUid).collection("alerts").whereEqualTo("alertTimeMillis",alertTimeMillis).whereEqualTo("ownerUid",ownerUid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot document : task.getResult()){
                        Log.d(TAG, "Encontrado el id de alerta"+document.getId());
                        updateAlertStatus(document.getId(),ownerUid);
                    }

                }
            }
        });
    }

    private void updateAlertStatus(final String id, String ownerUid) {
        final Map<String,Object> newStatus = new HashMap<>();
        newStatus.put("reviewed",true);
        if(approveAlert){

            newStatus.put("status","Checked");
        } else {

            newStatus.put("status","Rejected");
        }
        usuarios.document(ownerUid).collection("alerts").document(id).set(newStatus, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Entro y actualizo: "+id + " "+newStatus);

            }
        });
    }
}
