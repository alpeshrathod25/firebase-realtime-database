package connectingpixels.com.firebasedatabasewithimage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import connectingpixels.com.firebasedatabasewithimage.entities.Education;
import connectingpixels.com.firebasedatabasewithimage.entities.Person;
import connectingpixels.com.firebasedatabasewithimage.utils.FirebaseUtils;
import connectingpixels.com.firebasedatabasewithimage.utils.Paramters;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = MainActivity.this.getClass().getSimpleName();
    private final String deviceUniqueId = "5432-3425-6543-7863";
    private final String objectKey = "Person";
    private EditText editTextName, editTextLocation;
    private TextView btnAdd, btnUpdate, btnDelete, btnUploadFile, btnDownloadFile;
    private ListView listView;
    private String currentKeyIndex;
    private List<String> dataList = new ArrayList<>();
    private HashMap<Integer, String> keyHashMap = new HashMap<>();
    private HashMap<String, Person> personHashMap = new HashMap<>();
    private List<Education> innerList = new ArrayList<>();
    private ImageView imageDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Education education1 = new Education();
        education1.Degree = "BE-IT";
        education1.Result = "First Class";
        innerList.add(education1);

        Education education2 = new Education();
        education2.Degree = "MCA";
        education2.Result = "Distinction";
        innerList.add(education2);

        Education education3 = new Education();
        education3.Degree = "PH.D.";
        education3.Result = "Second Class";
        innerList.add(education3);

//        deviceUniqueId = UUID.randomUUID().toString();

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentKeyIndex = keyHashMap.get(position);
                String str = dataList.get(position);
                editTextName.setText(str.split("-")[0]);
                editTextLocation.setText(str.split("-")[1]);
            }
        });
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextLocation = (EditText) findViewById(R.id.editTextLocation);
        btnAdd = (TextView) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        btnUpdate = (TextView) findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(this);

        btnDelete = (TextView) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(this);

        btnUploadFile = (TextView) findViewById(R.id.btnUploadFile);
        btnUploadFile.setOnClickListener(this);

        btnDownloadFile = (TextView) findViewById(R.id.btnDownloadFile);
        btnDownloadFile.setOnClickListener(this);

        imageDownload = (ImageView) findViewById(R.id.imageDownload);

        FirebaseUtils.getInstance().readData(1, objectKey, new FirebaseListeners.OnFirebaseReadListener() {
            @Override
            public void onReadSuccess(int requestCode, DataSnapshot dataSnapshot) {
                Log.e(TAG, "Child count - " + dataSnapshot.getChildrenCount() + "");
                dataList.clear();
                keyHashMap.clear();
                personHashMap.clear();
                int headPosition = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Person object = postSnapshot.getValue(Person.class);
                    keyHashMap.put(headPosition, postSnapshot.getKey());
                    headPosition++;
                    personHashMap.put(postSnapshot.getKey(), object);
                    dataList.add(object.Name + "-" + object.Location);
                }
                ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, dataList);
                listView.setAdapter(itemsAdapter);

            }

            @Override
            public void onReadFail(int requestCode) {

            }

            @Override
            public void onReadCancel(int requestCode) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAdd:
                Person person = new Person();
                person.Name = editTextName.getText().toString().trim();
                person.Location = editTextLocation.getText().toString().trim();

                person.InnerList = innerList;
                FirebaseUtils.getInstance().writeData(1, objectKey, person, new FirebaseListeners.OnFirebaseWriteListener() {
                    @Override
                    public void onWriteSuccess(int requestCode) {

                    }

                    @Override
                    public void onWriteFail(int requestCode) {

                    }
                });

                editTextName.setText("");
                editTextLocation.setText("");
                currentKeyIndex = "";
                break;
            case R.id.btnUpdate:
                if (!currentKeyIndex.isEmpty()) {
                    Person updatedPerson = personHashMap.get(currentKeyIndex);
                    updatedPerson.Name = editTextName.getText().toString().trim();
                    updatedPerson.Location = editTextLocation.getText().toString().trim();
                    FirebaseUtils.getInstance().updateData(1, objectKey, currentKeyIndex, updatedPerson, new FirebaseListeners.OnFirebaseUpdateListener() {
                        @Override
                        public void onUpdateSuccess(int requestCode) {

                        }
                    });
                }
                editTextName.setText("");
                editTextLocation.setText("");
                currentKeyIndex = "";
                break;
            case R.id.btnDelete:
                if (!currentKeyIndex.isEmpty()) {
                    FirebaseUtils.getInstance().deleteData(1, objectKey, currentKeyIndex, new FirebaseListeners.OnFirebaseDeleteListener() {
                        @Override
                        public void onDeleteSuccess(int requestCode) {
                            editTextName.setText("");
                            editTextLocation.setText("");
                            currentKeyIndex = "";
                        }
                    });
                }
                editTextName.setText("");
                editTextLocation.setText("");
                currentKeyIndex = "";
                break;
            case R.id.btnUploadFile:
                chooseFile();
                break;
            case R.id.btnDownloadFile:

                FirebaseUtils.getInstance().downloadFile(1,  "ABC", "images/pic.jpg", "tempimage", ".png", new FirebaseListeners.OnFirebaseFileDownloadListener() {
                    @Override
                    public void onDownloadSuccess(int requestCode, String path) {
                        Toast.makeText(MainActivity.this, "File download success "+path, Toast.LENGTH_SHORT).show();
                        imageDownload.setImageURI(Uri.parse(path));
                    }

                    @Override
                    public void onDownloadFailed(int requestCode, String exception) {
                        Toast.makeText(MainActivity.this, "File download failed "+exception, Toast.LENGTH_SHORT).show();
                    }
                });

                break;
        }
    }

    private static final int PICK_IMAGE_REQUEST = 101;

    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadFile(Uri uri) {
        //if there is a file to upload
        if (uri != null) {
            //displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            StorageReference riversRef = // Register observers to listen for when the download is done or if it fails
                    FirebaseStorage.getInstance().getReference("ABC").child("images/pic.jpg");
            riversRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        //if there is not any file
        else {
            //you can display an error toast
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            uploadFile(filePath);
        }
    }
}
