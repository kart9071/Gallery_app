package com.example.imagemanagement;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.io.OutputStream;

public class Gallery_editing extends AppCompatActivity {
    private CropImageView cropImageView;
    private Uri selectedImageUri;
    private Uri outputUri;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Bitmap croppedImage;
    private float rotationAngle = 0f;

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 2;
    private static final int REQUEST_DIRECTORY = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_editing);

        cropImageView = findViewById(R.id.cropImageView);
        cropImageView.setImageResource(R.drawable.gallery2);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSelectionDialog();
            }
        });

        Button cropButton = findViewById(R.id.cropButton);
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImage();
            }
        });

        Button captureImageButton = findViewById(R.id.captureImageButton);
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
            }
        });

        Button rotateButton = findViewById(R.id.rotateButton);
        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage();
            }
        });

        // Initialize the activity result launcher for gallery and camera
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    Uri imageUri = data.getData();
                    startCrop(imageUri);
                }
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                if (imageBitmap != null) {
                    Uri savedImageUri = saveCameraImage(imageBitmap);
                    startCrop(savedImageUri);
                }
            }
        });
    }

    private void showImageSelectionDialog() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void startCrop(Uri imageUri) {
        selectedImageUri = imageUri;
        cropImageView.setImageUriAsync(imageUri);
    }

    private void cropImage() {
        croppedImage = cropImageView.getCroppedImage();
        if (croppedImage != null) {
            checkWriteExternalStoragePermission();
        } else {
            Toast.makeText(this, "You have to select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void checkWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getDesiredImageDirectory();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    private void getDesiredImageDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_DIRECTORY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DIRECTORY && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri treeUri = data.getData();
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                saveCroppedImage(treeUri);
            }
        }
    }

    private Uri saveCameraImage(Bitmap imageBitmap) {
        try {
            String fileName = "camera_image.jpg";
            Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

            ContentResolver resolver = getContentResolver();
            Uri uri = resolver.insert(imageUri, values);
            OutputStream outputStream = resolver.openOutputStream(uri);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            return uri;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save camera image", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    private void saveCroppedImage(Uri treeUri) {
        try {
            String fileName = "cropped_image.jpg";
            Uri imageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri imageContentUri = createImageContentUri(imageUri, fileName, treeUri);

            OutputStream outputStream = getContentResolver().openOutputStream(imageContentUri);
            croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            Toast.makeText(this, "Cropped image saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save cropped image", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri createImageContentUri(Uri imageUri, String fileName, Uri treeUri) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(this, treeUri);
        DocumentFile imageFile = documentFile.createFile("image/jpeg", fileName);
        return imageFile.getUri();
    }

    private void rotateImage() {
        if (croppedImage != null) {
            rotationAngle += 90f;
            if (rotationAngle >= 360f) {
                rotationAngle = 0f;
            }

            Matrix matrix = new Matrix();
            matrix.postRotate(rotationAngle);

            Bitmap rotatedImage = Bitmap.createBitmap(croppedImage, 0, 0, croppedImage.getWidth(), croppedImage.getHeight(), matrix, true);
            croppedImage.recycle();
            croppedImage = rotatedImage;

            cropImageView.setImageBitmap(croppedImage);
        } else {
            Toast.makeText(this, "You have to select and crop an image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDesiredImageDirectory();
            } else {
                Toast.makeText(this, "Write external storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
