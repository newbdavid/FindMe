package ec.edu.epn.findme.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ec.edu.epn.findme.R;
import ec.edu.epn.findme.entity.ActiveSearch;

/**
 * Created by David Moncayo on 16/04/2018.
 */

public class ActiveSearchAdapter extends ArrayAdapter{

    private ActiveSearch[] activeSearches;
    public ActiveSearchAdapter (Context context, ActiveSearch[] activeSearches){
        super(context,android.R.layout.simple_list_item_1,activeSearches);

        this.activeSearches = activeSearches;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView==null){
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.lv_active_search_item,null);
        }
        TextView nameTextView = (TextView) convertView.findViewById(R.id.txtName);
        TextView genderTextView = (TextView) convertView.findViewById(R.id.txtGender);
        TextView ageTextView = (TextView) convertView.findViewById(R.id.txtAge);
        TextView descriptionTextView = (TextView) convertView.findViewById(R.id.txtDescription);
        TextView lastSeenTextView = (TextView) convertView.findViewById(R.id.txtLastSeen);

        nameTextView.setText(activeSearches[position].getName());
        String gender = getContext().getResources().getString(R.string.gender)+" ";
        genderTextView.setText(gender +activeSearches[position].getGender());
        String age = getContext().getResources().getString(R.string.age)+" ";
        ageTextView.setText(age+String.valueOf(activeSearches[position].getAge()));
        String description = getContext().getResources().getString(R.string.description)+" ";
        descriptionTextView.setText(description+activeSearches[position].getDescription());
        StringBuilder stringBuilderLastSeen = new StringBuilder();
        stringBuilderLastSeen.append(getContext().getResources().getString(R.string.last_seen));
        stringBuilderLastSeen.append(getContext().getResources().getString(R.string.latitude)+": ");
        stringBuilderLastSeen.append(String.valueOf(activeSearches[position].getUltimoAvistamiento().getLatitude())+" ");
        stringBuilderLastSeen.append(getContext().getResources().getString(R.string.longitude)+": ");
        stringBuilderLastSeen.append(String.valueOf(activeSearches[position].getUltimoAvistamiento().getLongitude())+" ");
        lastSeenTextView.setText(stringBuilderLastSeen.toString());

        return convertView;
    }

}
