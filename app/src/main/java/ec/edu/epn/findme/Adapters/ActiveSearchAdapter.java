package ec.edu.epn.findme.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import ec.edu.epn.findme.R;
import ec.edu.epn.findme.entity.ActiveSearch;

/**
 * Created by David Moncayo on 16/04/2018.
 */

public class ActiveSearchAdapter extends RecyclerView.Adapter<ActiveSearchAdapter.ViewHolder>{

    public interface OnItemClickListener {
        void atItemClick(ActiveSearch activeSearch);
    }

    private List<ActiveSearch> activeSearchesList;
    private final OnItemClickListener listener;
    private Context context;

    public ActiveSearchAdapter (List<ActiveSearch> activeSearchesList,OnItemClickListener listener){
        this.activeSearchesList = activeSearchesList;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, gender, age, description, lastSeen;
        //public ImageView typeImageView;
        public CheckBox chkActiveSearch;


        public ViewHolder(View view) {
            super(view);
            name= (TextView) view.findViewById(R.id.txtName);
            gender= (TextView) view.findViewById(R.id.txtGender);
            age= (TextView) view.findViewById(R.id.txtAge);
            description= (TextView) view.findViewById(R.id.txtDescription);
            lastSeen= (TextView) view.findViewById(R.id.txtLastSeen);
            chkActiveSearch = (CheckBox) view.findViewById(R.id.chkActiveSearch);
            context = view.getContext();
        }
    }


    @Override
    public ActiveSearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.lv_active_search_item, parent, false);
        return new ActiveSearchAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ActiveSearchAdapter.ViewHolder holder, final int position) {
        final ActiveSearch activeSearch = activeSearchesList.get(position);
        holder.name.setText(activeSearch.getName());
        String gender = context.getResources().getString(R.string.gender)+" ";
        holder.gender.setText(gender +activeSearch.getGender()+" ");
        String age = context.getResources().getString(R.string.age)+" ";
        holder.age.setText(age+String.valueOf(activeSearch.getAge()));
        String description = context.getResources().getString(R.string.description)+" ";
        holder.description.setText(description+activeSearch.getDescription());
        StringBuilder stringBuilderLastSeen = new StringBuilder();
        stringBuilderLastSeen.append(context.getResources().getString(R.string.last_seen));
        stringBuilderLastSeen.append(context.getResources().getString(R.string.latitude)+": ");
        stringBuilderLastSeen.append(String.valueOf(activeSearch.getUltimoAvistamiento().getLatitude())+" ");
        stringBuilderLastSeen.append(context.getResources().getString(R.string.longitude)+": ");
        stringBuilderLastSeen.append(String.valueOf(activeSearch.getUltimoAvistamiento().getLongitude())+" ");
        holder.lastSeen.setText(stringBuilderLastSeen.toString());
        holder.chkActiveSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ActiveSearchAdapter.this.activeSearchesList.get(position).setListSelected(b);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                listener.atItemClick(activeSearch);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activeSearchesList.size();
    }
}
