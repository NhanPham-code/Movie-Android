package com.example.ojtbadaassignment14;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ojtbadaassignment14.services.Base64Helper;
import com.example.ojtbadaassignment14.services.Validator;

import java.util.Calendar;

public class EditProfileActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 300;
    private static final int GALLERY_REQUEST_CODE = 400;

    Button btnCancel;
    Button btnDone;
    ImageView avatar;
    EditText edtFullName;
    EditText edtEmail;
    TextView tvBirthday;
    RadioGroup radioGroup;

    String fullName;
    String email;
    String birthday;
    String gender;
    String avatarBase64;

    String TAG = "check";

    int currentAction; // to check current action (camera or gallery)

    Base64Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // create base64Helper
        helper = new Base64Helper();

        // Inflate the layout for this fragment
        btnCancel = findViewById(R.id.cancelButton);
        btnDone = findViewById(R.id.doneButton);
        avatar = findViewById(R.id.avatarImage);
        edtFullName = findViewById(R.id.fullName);
        edtEmail = findViewById(R.id.email);
        tvBirthday = findViewById(R.id.birthday);
        radioGroup = findViewById(R.id.genderGroup);


        // get intent data
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            fullName = bundle.getString("fullName");
            email = bundle.getString("email");
            birthday = bundle.getString("birthday");
            gender = bundle.getString("gender", "");
            avatarBase64 = bundle.getString("avatar", "");
        }


        // Fill in the information
        edtFullName.setText(fullName);
        edtEmail.setText(email);
        // Set birthday
        tvBirthday.setText(birthday);

        // Hiển thị ảnh avatar từ Base64
        avatar.setImageBitmap(helper.convertBase64ToBitmap(avatarBase64));

        // Thiết lập giới tính vào RadioGroup
        if ("Male".equalsIgnoreCase(gender)) {
            ((RadioButton) findViewById(R.id.maleRadioButton)).setChecked(true);
        } else if ("Female".equalsIgnoreCase(gender)) {
            ((RadioButton) findViewById(R.id.femaleRadioButton)).setChecked(true);
        }

        // avatar click click
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open menu to choose camera or gallery
                showPopupMenu(view);
            }
        });

        tvBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open date picker
                showDatePickerDialog();
            }
        });

        // cancel button click
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // save button click
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfile();
            }
        });

    }

    /**
     * Show DatePickerDialog for birthday
     */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);  // Giá trị từ 0 đến 11
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Tạo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                String selectedDate = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay;
                tvBirthday.setText(selectedDate);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    /**
     * Save profile information
     */
    private void saveProfile() {
        Validator validator = new Validator();

        // Get updated information
        String updatedFullName = edtFullName.getText().toString();

        String updatedEmail = edtEmail.getText().toString();
        if(!validator.isValidEmail(updatedEmail)) {
            Toast.makeText(EditProfileActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        String updatedBirthday = tvBirthday.getText().toString();
        if(!validator.isValidDate(updatedBirthday)) {
            Toast.makeText(EditProfileActivity.this, "Invalid birthday format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected radio button from radioGroup
        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        String updatedGender = selectedRadioButton != null ? selectedRadioButton.getText().toString() : "";

        // Check if any field is empty
        if(updatedFullName.isEmpty() || updatedEmail.isEmpty() || updatedBirthday.isEmpty() || updatedGender.isEmpty()){
            Toast.makeText(EditProfileActivity.this, "Please fill all information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save profile
        if (avatarBase64 != null) {
            addProfileToLocal(updatedFullName, updatedEmail, updatedBirthday, updatedGender, avatarBase64);
        }

        // Finish edit profile activity and back to main activity
        finish();
    }

    /**
     * Add user profile to SharedPreferences
     * @param fullName: name
     * @param email: email
     * @param birthday: birthday
     * @param gender: gender
     * @param avatar: avatar image
     */
    private void addProfileToLocal(String fullName, String email, String birthday, String gender, String avatar) {
        // Lưu thông tin vào SharedPreferences ở đây

        SharedPreferences sharedPreferences = this.getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("fullName", fullName);
        editor.putString("email", email);
        editor.putString("birthday", birthday);
        editor.putString("gender", gender);
        editor.putString("avatar", avatar);
        editor.apply();

        Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
    }


    // Hiển thị Popup Menu
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        // Set OnMenuItemClickListener cho PopupMenu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.camera) {
                    openCamera();
                    return true;
                } else if (itemId == R.id.gallery) {
                    openGallery();
                    return true;
                } else {
                    return false;
                }
            }
        });

        popupMenu.show(); // Hiển thị PopupMenu
    }

    /**
     * Request permission to access camera or gallery
     */
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (currentAction == CAMERA_REQUEST_CODE) {
                        openCamera();
                    } else if (currentAction == GALLERY_REQUEST_CODE) {
                        openGallery();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Receive data from camera
     */
    private ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == this.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        avatar.setImageBitmap(photo);
                        avatarBase64 = helper.convertBitmapToBase64(photo); // Lưu ảnh dưới dạng Base64
                    }
                }
            });

    /**
     * Open camera to take a photo
     */
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        } else {
            currentAction = CAMERA_REQUEST_CODE;
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Receive data from gallery
     */
    private ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == this.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                            avatar.setImageBitmap(bitmap);
                            avatarBase64 = helper.convertBitmapToBase64(bitmap); // Lưu ảnh dưới dạng Base64
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    /**
     * Open gallery to choose a photo
     */
    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 trở lên
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(intent);
            } else {
                currentAction = GALLERY_REQUEST_CODE;
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android dưới 13
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(intent);
            } else {
                currentAction = GALLERY_REQUEST_CODE;
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

}