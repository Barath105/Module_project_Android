package com.example.hremp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.util.Base64;

public class AttendanceFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;

    private String userId = "1001"; // Set the user ID to the appropriate value
    private DatabaseReference databaseReference;

    public AttendanceFragment() {
        // Initialize databaseReference with the user ID
        databaseReference = FirebaseDatabase.getInstance().getReference("adminhr/useratt/" + userId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_attendance, container, false);

        Button buttonPickLocation = rootView.findViewById(R.id.buttonPickLocation);
        buttonPickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request CAMERA permission if not granted
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_REQUEST_CODE);
                } else {
                    // Permission already granted, proceed to capture image
                    captureImage();
                }
            }
        });

        return rootView;
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(requireContext(), "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // CAMERA permission granted, proceed to capture image
                captureImage();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            // Image captured successfully, show confirmation dialog
            Bitmap capturedImage = (Bitmap) data.getExtras().get("data");
            if (capturedImage != null) {
                showConfirmationDialog(capturedImage);
            } else {
                Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showConfirmationDialog(Bitmap capturedImage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Attendance")
                .setMessage("Are you sure you want to mark attendance?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Proceed with marking attendance
                        markAttendance(capturedImage);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Cancel marking attendance
                        Toast.makeText(requireContext(), "Attendance marking cancelled", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void markAttendance(Bitmap capturedImage) {
        String currentTime = getCurrentTime();
        String currentDateNode = getCurrentDate();

        DatabaseReference dayReference = databaseReference.child(currentDateNode);

        // Set your keys for the database
        String att1StatusKey = "att_1";
        String att1TimeKey = "att_1_time";
        String att1ImageKey = "attendance_1_image";
        String lateStatusKey = "late";
        String att2Key = "att_2";
        String att2TimeKey = "att_2_time";
        String att2ImageKey = "attendance_2_image";
        String overallAttKey = "overall_att";
        String attendanceDateKey = "attendance_date";

        String imageBase64 = bitmapToBase64(capturedImage);

        // Store the attendance image
        if (isTimeInRange(currentTime, "10:15:00", "11:21:00")) {
            dayReference.child(att1ImageKey).setValue(imageBase64);
        } else if (isTimeInRange(currentTime, "11:21:00", "16:00:00")) {
            dayReference.child(att2ImageKey).setValue(imageBase64);
        }

        // Check if attendance 1 is already marked
        dayReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild(att1StatusKey)) {
                    // Attendance 1 is already marked, show log message
                    Toast.makeText(requireContext(), "Attendance 1 is already marked", Toast.LENGTH_SHORT).show();
                } else {
                    // Attendance 1 is not marked, mark it
                    if (isTimeInRange(currentTime, "10:15:00", "11:21:00")) {
                        // Mark att_1 as Present and overall_att as Absent
                        dayReference.child(att1StatusKey).setValue("Present");
                        dayReference.child(att1TimeKey).setValue(currentTime);
                        dayReference.child(overallAttKey).setValue("Absent");
                    } else if (isTimeInRange(currentTime, "11:21:00", "11:21:00")) {
                        // Mark att_1 as Present and late status as "late"
                        dayReference.child(att1StatusKey).setValue("Present");
                        dayReference.child(att1TimeKey).setValue(currentTime);
                        dayReference.child(lateStatusKey).setValue("Late");
                    } else {
                        // Show log message or handle the case when not within the specified time range
                        Toast.makeText(requireContext(), "Mark attendance within the specified time range", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        // Check if att_2 can be marked
        if (isTimeInRange(currentTime, "11:21:00", "16:00:00")) {
            // Check if att_1 is marked
            dayReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChild(att1StatusKey)) {
                        // Attendance 1 is marked, mark att_2 as Present and update overall_att
                        dayReference.child(att2Key).setValue("Present");
                        dayReference.child(att2TimeKey).setValue(currentTime);
                        dayReference.child(overallAttKey).setValue("Present");
                        Toast.makeText(requireContext(), "Attendance 2 marked successfully", Toast.LENGTH_SHORT).show();

                    } else {
                        // Attendance 1 is not marked, mark att_2 as Present and update overall_att as Absent
                        dayReference.child(att2Key).setValue("Present");
                        dayReference.child(att2TimeKey).setValue(currentTime);
                        dayReference.child(overallAttKey).setValue("Absent");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        } else {
            // Show log message or handle the case when not within the specified time range for att_2
            Toast.makeText(requireContext(), "Mark attendance within the specified time range for att_2", Toast.LENGTH_SHORT).show();
        }

        // Store the attendance date
        dayReference.child(attendanceDateKey).setValue(getCurrentDate());
    }

    private boolean isTimeInRange(String currentTime, String startTime, String endTime) {
        return currentTime.compareTo(startTime) >= 0 && currentTime.compareTo(endTime) <= 0;
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private String getCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return timeFormat.format(new Date());
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}