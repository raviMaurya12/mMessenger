package com.example.lenovo.mmessenger;
/* To Learn:The Adapter class should extend RecyclerView.Adapter<RecyclerView.Viewholder>
           :The ViewHolder class should extend recyclerView.Viewholder
           :For multiple view type recyclerView override the method getItemViewType that returns a integer on basis of your own logic.
           :The OncreateViewHolder itself calls getItemViewType and uses the returning viewType as an argument.
           :Then we can apply switch or if else to choose the view holder and pass the view to that Viewholders object.
           :Next in OnBindViewHolder we will have to call getItemViewType manually and then we will again have to create switch of if-else.
           :After that we will have to cast the default ViewHolder holder into our Own Viewholders depending on the upper logic.
           :then we can use our casted viewholders access the member variables(in ous case TextView messageText).
 */

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    String current_user_id=mAuth.getCurrentUser().getUid();
    public List<Messages> mMessageList;


    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch(viewType){
            case 1:
                view=LayoutInflater.from(parent.getContext()).inflate( R.layout.chat_single_msg,parent,false);
                MessageViewHolderOne viewHolderOne = new MessageViewHolderOne( view );
                return viewHolderOne;
            case 2:
                view=LayoutInflater.from(parent.getContext()).inflate( R.layout.chat_single_msg_two,parent,false);
                MessageViewHolderTwo viewHolderTwo = new MessageViewHolderTwo( view );
                return viewHolderTwo;
        }
        return null;
    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Messages m=mMessageList.get( position );
        String from_id=m.getFrom();

        final int msgType=getItemViewType( position );
        if(msgType==1){
            MessageViewHolderOne viewHolderOne = (MessageViewHolderOne)holder;
            viewHolderOne.messageText.setText(m.getMessage());
            viewHolderOne.setIsRecyclable(false);
        }else if(msgType==2){
            MessageViewHolderTwo viewHolderTwo = (MessageViewHolderTwo)holder;
            viewHolderTwo.messageText.setText(m.getMessage());
            viewHolderTwo.setIsRecyclable( false );

        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    @Override
    public int getItemViewType(int position) {
        int r=0;
        if(mMessageList!=null){
            Messages m=mMessageList.get(position);
            if(m!=null){
                if(m.getFrom().equals(current_user_id))
                    r=2;
                else r=1;
            }
        }
        return r;
    }

    public class MessageViewHolderOne extends RecyclerView.ViewHolder{

        public TextView messageText;

        public MessageViewHolderOne(View itemView) {
            super( itemView );

            messageText=(TextView)itemView.findViewById( R.id.single_msg_text );

        }

    }

    public class MessageViewHolderTwo extends RecyclerView.ViewHolder{

        public TextView messageText;

        public MessageViewHolderTwo(View itemView) {
            super( itemView );

            messageText=(TextView)itemView.findViewById( R.id.single_msg_text );

        }

    }

}
