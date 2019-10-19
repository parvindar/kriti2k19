package com.test.learn;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;

public class PdfViewer extends AppCompatActivity {

    String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        url = getIntent().getStringExtra("url");

        PDFView pdfView = findViewById(R.id.pdfView);



    }
}
