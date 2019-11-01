package com.journeyapps.barcodescanner;

import android.app.Activity;
import android.content.Intent;

import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.android.R;
import com.google.zxing.integration.android.IntentIntegrator;

import android.os.Bundle;
import android.os.Build;

import java.io.IOException;

/**
 *
 */
public class CaptureActivity extends Activity {
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private Button btnCancel;
    private Button btnChooseImage;
    private static final int REQUES_PICK_IMAGE_FROM_GALLERY = 1101;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 24 && IntentIntegrator.BLOCK_API){ // 24 is a android version (Build.VERSION_CODES.N)
            Toast.makeText(this,"you need to upgrade your phone to Android version 7 or higher, for use this future. thanks!",Toast.LENGTH_LONG).show();
            finish();
        }

        barcodeScannerView = initializeContent();

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
        btnCancel = (Button) findViewById(R.id.btnCancel);

        btnChooseImage = (Button) findViewById(R.id.btnChooseImage);
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                String[] mimeTypes = {"image/jpeg", "image/png"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
                // Launching the Intent
                startActivityForResult(intent,REQUES_PICK_IMAGE_FROM_GALLERY);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUES_PICK_IMAGE_FROM_GALLERY && resultCode == Activity.RESULT_OK){
            Uri selectedImage = data.getData();
            StarScanBarcode(selectedImage);
        }
    }

    private void StarScanBarcode(Uri uri) {
        String resultCode = "";
        Bitmap bMap = null;
        try {
            bMap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (IOException e) {
            Toast.makeText(this,"Can't open image",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            resultCode = result.getText();

            Intent intent = new Intent();
            intent.putExtra("SCAN_RESULT", resultCode);
            intent.putExtra("SCAN_RESULT_IMAGE_PATH", uri);
            setResult(RESULT_OK, intent);
            finish();
        }
        catch (Exception e) {
            Toast.makeText(this,"Can't find QRcode / Barcode",Toast.LENGTH_LONG).show();
            finish();
            e.printStackTrace();
        }
    }

    /**
     * Override to use a different layout.
     *
     * @return the DecoratedBarcodeView
     */
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.zxing_capture);
        return (DecoratedBarcodeView)findViewById(R.id.zxing_barcode_scanner);
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
