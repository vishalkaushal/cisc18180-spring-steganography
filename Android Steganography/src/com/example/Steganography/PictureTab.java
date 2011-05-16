package com.example.Steganography;

import java.io.File;

import com.example.Steganography.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.PopupWindow;

public class PictureTab extends Activity{
	TextView textTargetUri1;
	TextView textTargetUri2;
	ProgressDialog pd;
	RadioGroup radioGroup;
	Context context;
	EditText editText;
	String writeTo;
	String saveTo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picturetab);
		Button buttonLoadImage = (Button)findViewById(R.id.button1);
		textTargetUri1 = (TextView)findViewById(R.id.textView3);
		/* Encode in. */
		Button buttonLoadImage2 = (Button)findViewById(R.id.button2);
		textTargetUri2 = (TextView)findViewById(R.id.textView4);
		/* Encode this. */
		Button buttonGo = (Button)findViewById(R.id.button3);
		radioGroup = (RadioGroup)findViewById(R.id.radioGroup1);
		context = this;
		editText = (EditText)findViewById(R.id.editText1);
		writeTo = new File(Environment.getExternalStorageDirectory() + File.separator + "encoded.png").toString();
		saveTo = new File(Environment.getExternalStorageDirectory() + File.separator + "secret.png").toString();
		
		buttonLoadImage.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, 0);
			}});

		buttonLoadImage2.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, 1);
			}});

		buttonGo.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new EncodeAndDecodeTask().execute();
			}});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Uri targetUri = data.getData();
		if (resultCode == RESULT_OK){
			if (requestCode == 0)
				textTargetUri1.setText(targetUri.getPath());
			else if (requestCode ==1)
				textTargetUri2.setText(targetUri.getPath());
		}
	}

	public class EncodeAndDecodeTask extends AsyncTask<Void, Void, Void>{

		byte[] key;
		Bitmap image;
		Bitmap image2;
		
		protected void onPreExecute(){
			pd = ProgressDialog.show(PictureTab.this, "Working..", "Working on your image.", true, false);
		}
	
		@Override
		protected Void doInBackground(Void... params) {
			TwoReturn get;
			if (radioGroup.getCheckedRadioButtonId() == 0){
				get = EncodeAndDecode.encodePicture(textTargetUri2.getText().toString(), textTargetUri1.getText().toString(), writeTo);
				image = (Bitmap)get.first;
				key = (byte[])get.second;
			}
			else if (radioGroup.getCheckedRadioButtonId() == 1){
				image2 = EncodeAndDecode.decodeImage(textTargetUri1.getText().toString(), saveTo, editText.getText().toString());
			}
			return null;
		}
		protected void onPostExecute(Void useless){
			pd.dismiss();
			final AlertDialog alertDialog = new AlertDialog.Builder(PictureTab.this).create();
			alertDialog.setMessage("Done!");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	 //alertDialog.dismiss();
			    } });
			alertDialog.show();
		}
	}
}
