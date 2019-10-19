package com.test.learn;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Booklist extends AppCompatActivity {


    FirebaseStorage storage;
    String club;
    Button uploadfile;
    FirebaseFirestore db;
    ProgressDialog progressDialog;
    ListView lv;
    Uri pdfUri;
    TextView selectedbookname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_booklist);
        lv = findViewById(R.id.lv_booklist);

        club= getIntent().getStringExtra("club");

        Log.d("DEBUG : "," dep/club --> "+club);

        db.collection("Books").document("Category").collection(club).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<Book> bookArrayList = new ArrayList<>();
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots)
                {
                    Book book=new Book(documentSnapshot.get("name").toString(),documentSnapshot.get("author").toString(),documentSnapshot.get("tag").toString(),documentSnapshot.get("rating").toString(),documentSnapshot.get("url").toString());
                    bookArrayList.add(book);
                }

                BookListAdaptor bookListAdaptor = new BookListAdaptor(Booklist.this,R.layout.booklist_elem,bookArrayList);

                lv.setAdapter(bookListAdaptor);

            }



        });

        uploadfile = findViewById(R.id.btn_upload_file);

        uploadfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Booklist.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_upload_pdf, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog b = dialogBuilder.create();
                b.show();

                final EditText bookname = dialogView.findViewById(R.id.et_bookname);
                final EditText author = dialogView.findViewById(R.id.et_author);
                final EditText tag = dialogView.findViewById(R.id.et_tag);
                selectedbookname = dialogView.findViewById(R.id.tv_selected_filename);
                Button btn_selectbook = dialogView.findViewById(R.id.btn_browse);
                Button submit = dialogView.findViewById(R.id.btn_upload);



                btn_selectbook.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View v) {

                        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                        {
                            selectpdf();

                        }
                        else {
                            requestPermissions( new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
                        }

                    }
                });


                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(bookname.getText().toString().isEmpty()||tag.getText().toString().isEmpty())
                        {
                            Toast.makeText(getApplicationContext(),"Fill the details first",Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(pdfUri!=null)
                        uploadFile(pdfUri,bookname.getText().toString(),author.getText().toString(),tag.getText().toString());
                        else
                            Toast.makeText(Booklist.this,"Please select a file",Toast.LENGTH_LONG).show();
                    }
                });





            }
        });


    }

    void uploadFile(final Uri pdfUri, final String name, final String author, final String tag)
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading file...");
        progressDialog.setProgress(0);
        progressDialog.show();

        String fileid = System.currentTimeMillis()+"";
        StorageReference storageReference = storage.getReference();
        storageReference.child("books").child(club).child(fileid).putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                String url = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                Map<String,Object> book = new HashMap<>();
                book.put("url",url);
                book.put("name",name);
                book.put("author",author);
                book.put("tag",tag);
                book.put("rating",0);
                book.put("n",0);
                db.collection("Books").document("Category").collection(club).document(tag+name+author).set(book);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Booklist.this,"File did not uploaded",Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                int currentprogress = (int)(100*(taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount()));
                progressDialog.setProgress(currentprogress);
                Log.d("DEBUG","progress  -->  "+currentprogress);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==9&& grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            selectpdf();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Please provide permissions",Toast.LENGTH_LONG).show();
        }
    }

    void selectpdf()
    {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,86);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==86&&resultCode==RESULT_OK && data!=null)
        {
            pdfUri = data.getData();
            selectedbookname.setText(data.getData().getLastPathSegment());
        }
        else
        {
            Toast.makeText(this,"Please select a file",Toast.LENGTH_LONG).show();
        }
    }

    class Book {

        String name;
        String author;
        String tag;
        String rating;
        String url;

        Book()
        {

        }

        public Book(String name, String author, String tag, String rating,String url) {
            this.name = name;
            this.author = author;
            this.tag = tag;
            this.rating = rating;
            this.url = url;
        }
    }



    private class BookListAdaptor extends ArrayAdapter<Book> {
        private static final String TAG = "PlayerListAdaptor";
        private Context mContext;
        private int mResource;

        public BookListAdaptor(Context context, int resource, List<Book> objects) {
            super(context, resource, objects);
            this.mContext = context;
            this.mResource = resource;
        }




        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if(getItem(position)!=null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);

                TextView name = convertView.findViewById(R.id.tv_bookname);
                TextView author = convertView.findViewById(R.id.tv_authorname);
                TextView rating = convertView.findViewById(R.id.tv_rating);

                name.setText(getItem(position).name);
                author.setText(getItem(position).author);
                rating.setText(getItem(position).rating);


                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getItem(position)!=null)
                        {


                        }

                    }
                });


            }
            return convertView;

        }



    }




}
