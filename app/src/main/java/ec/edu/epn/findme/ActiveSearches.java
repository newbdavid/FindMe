package ec.edu.epn.findme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import ec.edu.epn.findme.Adapters.ActiveSearchAdapter;
import ec.edu.epn.findme.entity.ActiveSearch;

public class ActiveSearches extends AppCompatActivity {
    private static final String TAG = ActiveSearches.class.getSimpleName();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference activeSearches = db.collection("LocationData").document("Quito").collection("ActiveSearches");
    ArrayList<ActiveSearch> arrayListActiveSearch = new ArrayList<>();
    ActiveSearch[] activeSearchesArray;
    ArrayList<String> ids = new ArrayList<>();
    private ListView lvActiveSearch;
    private Button btnSeeMapSelectedSearchesIds;
    private String Uid;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_searches);
        lvActiveSearch = (ListView) findViewById(R.id.listViewActiveSearches);
        btnSeeMapSelectedSearchesIds = (Button) findViewById(R.id.btnGoWithSelectedIds);
        btnSeeMapSelectedSearchesIds.setEnabled(false);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(ActiveSearches.this, RegistroActivity.class);
                    startActivity(intent);
                }
                // ...
            }
        };

        Intent intent = getIntent();
        if(intent.hasExtra("Uid")){
            Uid = intent.getExtras().getString("Uid");
        } else{
            Uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        activeSearches.whereEqualTo("active",true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
//                        Log.d(TAG, document.getId() + " => " + document.getData() );

                        arrayListActiveSearch.add(document.toObject(ActiveSearch.class));
                        ids.add(document.getId());



                    }
                    inflateLayout(arrayListActiveSearch);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }


        });


    }

    private void inflateLayout(ArrayList<ActiveSearch> arrayListActiveSearch) {
        activeSearchesArray = new ActiveSearch[arrayListActiveSearch.size()];
        for(int i = 0; i<activeSearchesArray.length;i++){
            activeSearchesArray[i] = arrayListActiveSearch.get(i);
            activeSearchesArray[i].setId(ids.get(i));
//            Log.d(TAG,  "Elemento " + i+ " => " + activeSearchesArray[i] );
        }
        ActiveSearchAdapter asa = new ActiveSearchAdapter(this,activeSearchesArray);
        lvActiveSearch.setAdapter(asa);
        btnSeeMapSelectedSearchesIds.setEnabled(true);

        //lvActiveSearch.setOnClickListener();
    }

    public void entrarConSearchId(View view){
        ArrayList<String> idsActiveSearch = new ArrayList<>();

        for(ActiveSearch activeSearch : activeSearchesArray){
            if(activeSearch.isListSelected()){
                idsActiveSearch.add(activeSearch.getId());

            }

        }

        if(idsActiveSearch.size()>0){
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("selectedActiveSearchIds",idsActiveSearch);
            bundle.putString("Uid", Uid);
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }else {
            Toast.makeText(this,"Seleccione al menos 1 búsqueda activa",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onBackPressed() {
        FirebaseAuth.getInstance().signOut();
        Log.d(TAG, "onAuthStateChanged:signed_out");
        Intent intent = new Intent(ActiveSearches.this, RegistroActivity.class);
        startActivity(intent);
    }
}
