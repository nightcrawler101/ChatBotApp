package com.example.internchatbot;

import android.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import androidx.appcompat.widget.AppCompatImageView;


import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import com.google.firebase.database.core.Context;
import com.google.firebase.database.Query;


public class MainActivity extends AppCompatActivity implements AIListener{  //because I am recording audio too

    RecyclerView recyclerView;
    EditText editText;
    RelativeLayout addBtn;
    static String message;

    private DatabaseReference ref;
    Boolean flagFab = true;

    final   AIConfiguration config = new AIConfiguration("47f72f4d39b548edbbd11f123d3a40cb ",
            AIConfiguration.SupportedLanguages.English,
            AIConfiguration.RecognitionEngine.System);

    private AIService aiService;
    final AIDataService aiDataService = new AIDataService(config);

    final AIRequest aiRequest = new AIRequest();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);


        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editText);
        addBtn = findViewById(R.id.addBtn);

        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        ref=FirebaseDatabase.getInstance().getReference();

        final Query query=ref.child("chat").limitToLast(50);
        ref.keepSynced(true);
        query.keepSynced(true);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 String message = editText.getText().toString().trim();

                if (!message.equals("")) {


                    ChatMessage chatMessage = new ChatMessage(message, "user");
                    ref.child("chat").push().setValue(chatMessage);

                    MyAsyncTask myasynctask = new MyAsyncTask();
                    myasynctask.execute();

                }
                else {
                    aiService.startListening();
                }

                editText.setText("");

            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ImageView fab_img = findViewById(R.id.fab_img);
                Bitmap img = BitmapFactory.decodeResource(getResources(),R.drawable.ic_send_white_24dp);
                Bitmap img1 = BitmapFactory.decodeResource(getResources(),R.drawable.ic_mic_white_24dp);


                if (s.toString().trim().length()!=0 && flagFab){
                    ImageViewAnimatedChange(MainActivity.this,fab_img,img);

                    flagFab=false;

                }
                else if (s.toString().trim().length()==0){
                    ImageViewAnimatedChange(MainActivity.this,fab_img,img1);
                    flagFab=true;

                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }


        });

        FirebaseRecyclerOptions<ChatMessage> options = new FirebaseRecyclerOptions.Builder<ChatMessage>().setQuery(query,ChatMessage.class).build();

        final FirebaseRecyclerAdapter<ChatMessage, chat_rec> adapter = new FirebaseRecyclerAdapter<ChatMessage, chat_rec>(options) {


            @NonNull
            @Override
            public chat_rec onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.msglist,parent,false);

                return new chat_rec(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull chat_rec holder, int position, ChatMessage model) {

                if (model.getMsgUser().equals("user")) {

                    holder.rightText.setText(model.getMsgText());

                    holder.rightText.setVisibility(View.VISIBLE);
                    holder.leftText.setVisibility(View.GONE);
                }
                else {
                    holder.leftText.setText(model.getMsgText());

                    holder.rightText.setVisibility(View.GONE);
                    holder.leftText.setVisibility(View.VISIBLE);
                }

            }

        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);

                }

            }
        });

        recyclerView.setAdapter(adapter);

    }//onCreate ends here


    public class MyAsyncTask extends AsyncTask<AIRequest,Void,AIResponse>{
        @Override
        protected AIResponse doInBackground(AIRequest... aiRequests) {
            aiRequest.setQuery(message);

            try {
                final AIResponse response = aiDataService.request(aiRequest);
                return response;
            } catch (AIServiceException e) {
                System.out.print("Error in Aync Task. Please check!"+e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(AIResponse response) {

            if (response != null) {

                Result result = response.getResult();
                String reply = result.getFulfillment().getSpeech();
                ChatMessage chatMessage = new ChatMessage(reply, "bot");
                ref.child("chat").push().setValue(chatMessage);
                Log.d("-CHECK-ON POST-EXEC-",chatMessage.toString());
            }
        }
    }

    public void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    @Override
    public void onResult(ai.api.model.AIResponse response) {


        Result result = response.getResult();

        String message = result.getResolvedQuery();
        ChatMessage chatMessage0 = new ChatMessage(message, "user");
        Log.d("--CHECK ON RESULT REQ--",chatMessage0.toString());
        ref.child("chat").push().setValue(chatMessage0);


        String reply = result.getFulfillment().getSpeech();
        ChatMessage chatMessage = new ChatMessage(reply, "bot");

        Log.d("Reply",chatMessage.toString());
        ref.child("chat").push().setValue(chatMessage);

        Log.d("--CHECK ON REPLY REQ--",chatMessage0.toString());


    }

    @Override
    public void onError(ai.api.model.AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
}