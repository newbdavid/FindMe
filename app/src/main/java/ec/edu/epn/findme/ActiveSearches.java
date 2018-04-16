package ec.edu.epn.findme;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_searches);
        lvActiveSearch = (ListView) findViewById(R.id.listViewActiveSearches);
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
    }

//    public void abr
}
