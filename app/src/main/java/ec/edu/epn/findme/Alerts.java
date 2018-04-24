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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ec.edu.epn.findme.Adapters.AlertAdapter;
import ec.edu.epn.findme.entity.Alert;

public class Alerts extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Alert> alertsList = new ArrayList<>();
    private ArrayList<String> idsActiveSearches;

    private static final String TAG = Alerts.class.getSimpleName();

    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String username = user.getUid();
    CollectionReference usuarios = db.collection("LocationData").document("Quito").collection("usuarios");

    private long timeDiff = 864000000;//10 days
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alertas);
        recyclerView = (RecyclerView) findViewById(R.id.alerts_recycle_view);
        Intent intent = getIntent();
        if(intent.hasExtra("selectedActiveSearchIds")){
            idsActiveSearches = intent.getExtras().getStringArrayList("selectedActiveSearchIds");
        }
        adapter =  new AlertAdapter(alertsList);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        getUsersWithSameSearchIds();

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
                                getAlertsFromThisUser(document.getId());
                            }

                        }





                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void getAlertsFromThisUser(final String id) {
        long longtimeToSearch = System.currentTimeMillis()-timeDiff;
        //usuarios.document(id).collection("tracks").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){


        usuarios.document(id).collection("alerts").whereGreaterThanOrEqualTo("alertTimeMillis",longtimeToSearch).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
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
                            alertsList.add(document.toObject(Alert.class));
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
    }

    private void agregarNuevaAlerta(View view){

    }

    private void aprobarNuevaAlerta(View view){

    }
}
