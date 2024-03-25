package com.example.hremp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class PayrollFragment extends Fragment {

    // Declare TextInputLayouts
    private TextInputLayout basicIncomeInputLayout;
    private TextInputLayout houseRentInputLayout;
    private TextInputLayout incomeTaxInputLayout;
    private TextInputLayout fundInputLayout;
    private TextInputLayout grossEarningsInputLayout;
    private TextInputLayout totalDeductionInputLayout;
    private TextInputLayout totalAmountInputLayout;
    private TextInputLayout payDateTextInputLayout;

    // Declare variables to hold employee name and ID
    private String empName;
    private String empId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve arguments from Bundle
        if (getArguments() != null) {
            empName = getArguments().getString("name");
            empId = getArguments().getString("userId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hr_fragment_payroll, container, false);

        // Find the TextInputLayouts by ID
        TextInputLayout empNameInputLayout = view.findViewById(R.id.empname);
        TextInputLayout empIdInputLayout = view.findViewById(R.id.empid);
        TextInputLayout organizationInputLayout = view.findViewById(R.id.empOrganization);

        // Set text for the EditText inside the TextInputLayout
        empNameInputLayout.getEditText().setText(empName);
        empIdInputLayout.getEditText().setText(empId);
        organizationInputLayout.getEditText().setText("SSE Soft Tech");

        // Find TextInputLayouts by ID
        basicIncomeInputLayout = view.findViewById(R.id.basicIncome);
        houseRentInputLayout = view.findViewById(R.id.houseRent);
        incomeTaxInputLayout = view.findViewById(R.id.incomeTax);
        fundInputLayout = view.findViewById(R.id.fund);
        grossEarningsInputLayout = view.findViewById(R.id.grossEarnings);
        totalDeductionInputLayout = view.findViewById(R.id.totalDeduction);
        totalAmountInputLayout = view.findViewById(R.id.totalAmount);
        payDateTextInputLayout = view.findViewById(R.id.payDate);

        // Set current date to payDate TextInputLayout
        String currentDate = getCurrentDate();
        payDateTextInputLayout.getEditText().setText(currentDate);

        // Find the generate PDF button and set an onClickListener
        Button generatePdfButton = view.findViewById(R.id.generate);
        generatePdfButton.setOnClickListener(v -> generatePdf());

        // Set up TextChangedListeners for automatic calculation
        setUpTextChangedListeners();

        return view;
    }

    // Method to get the current date in the specified format
    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    // Method to set up TextChangedListeners for automatic calculation
    private void setUpTextChangedListeners() {
        basicIncomeInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateGrossEarnings();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        houseRentInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateGrossEarnings();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        incomeTaxInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalDeduction();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fundInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalDeduction();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Method to calculate grossEarnings and update UI
    private void calculateGrossEarnings() {
        String basicIncomeStr = basicIncomeInputLayout.getEditText().getText().toString();
        String houseRentStr = houseRentInputLayout.getEditText().getText().toString();
        if (!basicIncomeStr.isEmpty() && !houseRentStr.isEmpty()) {
            double basicIncome = Double.parseDouble(basicIncomeStr);
            double houseRent = Double.parseDouble(houseRentStr);
            double grossEarnings = basicIncome + houseRent;
            grossEarningsInputLayout.getEditText().setText(String.valueOf(grossEarnings));
        }
    }

    // Method to calculate totalDeduction and update UI
    private void calculateTotalDeduction() {
        String incomeTaxStr = incomeTaxInputLayout.getEditText().getText().toString();
        String fundStr = fundInputLayout.getEditText().getText().toString();
        if (!incomeTaxStr.isEmpty() && !fundStr.isEmpty()) {
            double incomeTax = Double.parseDouble(incomeTaxStr);
            double fund = Double.parseDouble(fundStr);
            double totalDeduction = incomeTax + fund;
            totalDeductionInputLayout.getEditText().setText(String.valueOf(totalDeduction));
            calculateTotalAmount();
        }
    }

    // Method to calculate totalAmount and update UI
    private void calculateTotalAmount() {
        String grossEarningsStr = grossEarningsInputLayout.getEditText().getText().toString();
        String totalDeductionStr = totalDeductionInputLayout.getEditText().getText().toString();
        if (!grossEarningsStr.isEmpty() && !totalDeductionStr.isEmpty()) {
            double grossEarnings = Double.parseDouble(grossEarningsStr);
            double totalDeduction = Double.parseDouble(totalDeductionStr);
            double totalAmount = grossEarnings - totalDeduction;
            totalAmountInputLayout.getEditText().setText(String.valueOf(totalAmount));
        }
    }

    private void generatePdf() {
        // Create a new PDF document
        PdfDocument document = new PdfDocument();

        // Create a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 800, 1).create();

        // Start a page
        PdfDocument.Page page = document.startPage(pageInfo);

        // Get canvas to draw on
        Canvas canvas = page.getCanvas();

        // Define text size and padding
        float textSize = 24;
        float padding = 20;

        // Create paint object for text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);

        // Draw title
        String title = "Payroll Slip";
        float titleWidth = textPaint.measureText(title);
        float titleX = (canvas.getWidth() - titleWidth) / 2;
        canvas.drawText(title, titleX, padding + textSize, textPaint);

        // Define line separator y-coordinate
        float lineY = 2 * padding + 2 * textSize;

        // Draw line separator
        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(2);
        canvas.drawLine(padding, lineY, canvas.getWidth() - padding, lineY, linePaint);

        // Define content text size and padding
        float contentTextSize = 18;
        float contentPadding = 10;

        // Create paint object for content text
        Paint contentPaint = new Paint();
        contentPaint.setTextSize(contentTextSize);

        // Define starting y-coordinate for content
        float contentY = lineY + 2 * padding;

        // Draw content
        String[] content = {
                "Employee Name: " + empName,
                "Employee ID: " + empId,
                "Organization: SSE Soft Tech",
                "Basic Income: " + basicIncomeInputLayout.getEditText().getText().toString(),
                "House Rent: " + houseRentInputLayout.getEditText().getText().toString(),
                "Income Tax: " + incomeTaxInputLayout.getEditText().getText().toString(),
                "Provident Fund: " + fundInputLayout.getEditText().getText().toString(),
                "Gross Earnings: " + grossEarningsInputLayout.getEditText().getText().toString(),
                "Total Deduction: " + totalDeductionInputLayout.getEditText().getText().toString(),
                "Total Amount: " + totalAmountInputLayout.getEditText().getText().toString()
        };

        // Draw content lines
        for (String line : content) {
            if (line.startsWith("Total Deduction")) {
                contentPaint.setColor(Color.RED);
            } else if (line.startsWith("Total Amount")) {
                contentPaint.setColor(Color.GREEN);
                // Adjust x-coordinate to center text
                float lineWidth = contentPaint.measureText(line);
                float lineX = (canvas.getWidth() - lineWidth) / 2;
                canvas.drawText(line, lineX, contentY, contentPaint);
            } else {
                contentPaint.setColor(Color.BLACK);
                canvas.drawText(line, padding, contentY, contentPaint);
            }
            contentY += contentTextSize + contentPadding;
        }

        // Finish the page
        document.finishPage(page);

        // Define output file directory
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Payroll");
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it doesn't exist
        }

        // Define output file
        File outputFile = new File(directory, "payroll_slip.pdf");

        try {
            // Write the document content to the output file
            document.writeTo(new FileOutputStream(outputFile));
            Toast.makeText(getContext(), "PDF generated successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error generating PDF", Toast.LENGTH_SHORT).show();
        } finally {
            // Close the document
            document.close();
        }
    }


}