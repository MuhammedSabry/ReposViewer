package com.sabry.muhammed.reposviewer.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sabry.muhammed.reposviewer.R;
import com.sabry.muhammed.reposviewer.models.GitModel;

import java.util.ArrayList;


public class Adapter extends RecyclerView.Adapter<Adapter.mViewHolder> {
    private Context mContext;
    private ArrayList<GitModel> gitModelList;

    public Adapter(Context context, ArrayList<GitModel> list ) {
        mContext = context;
        gitModelList = list;
    }

    @Override
    public mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.main_recycler_layout
                        , parent
                        , false);
        return new mViewHolder(view);
    }

    @Override
    public void onBindViewHolder(mViewHolder holder, int position) {
        GitModel dataHolder = gitModelList.get(position);
        holder.bind(dataHolder);
    }

    @Override
    public int getItemCount() {
        return gitModelList.size();
    }

    class mViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView name, user, description;
        ConstraintLayout background;

        mViewHolder(View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
            background = itemView.findViewById(R.id.recyclerLayout);
            name = itemView.findViewById(R.id.repoNameTextView);
            user = itemView.findViewById(R.id.userNameTextView);
            description = itemView.findViewById(R.id.descriptionTextView);
        }

        void bind(GitModel data) {

            if (!data.isForkFlag())
                background.setBackgroundColor(mContext.getResources().getColor(R.color.noFork));
            else
                background.setBackgroundColor(mContext.getResources().getColor(R.color.offWhite));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                background.setElevation(8);
            }
            name.setText( data.getName());
            user.setText(data.getUserName());
            description.setText( data.getDescription());
        }

        @Override
        public void onClick(View view) {
            Log.e("NetworkUtility", "onCLick");

        }

        //handling longClicks
        @Override
        public boolean onLongClick(View view) {
            final int position = getAdapterPosition();
            Log.e("NetworkUtility", "onLongCLick item at " + position);

            String dialogArray[] = new String[2];
            dialogArray[0] = "Owner";
            dialogArray[1] = "Repository";

            //creating the dialog
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
            dialogBuilder
                    .setTitle("Pick a link")
                    .setItems(dialogArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String url;

                            if (i == 0) {
                                url = gitModelList.get(position).getOwnerURL();
                            } else {
                                url = gitModelList.get(position).getRepoURL();
                            }
                            Intent intent = new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse(url));
                            if (intent.resolveActivity(mContext.getPackageManager()) != null)
                                mContext.startActivity(intent);
                        }
                    });
            dialogBuilder.create().show();
            return true;
        }
    }
}