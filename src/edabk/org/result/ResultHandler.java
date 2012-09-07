/* using com.google.zing package to decode QR code*/
package edabk.org.result;

import edabk.org.qrcode.R;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Contacts;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/*
A base class for the Android-specific barcode handler
*/
public abstract class ResultHandler {
 public static final int MAX_BUTTON_COUNT = 4;

 private final ParsedResult result;
 protected final Activity activity;
 
 ResultHandler(Activity activity, ParsedResult result) {
   this.result = result;
   this.activity = activity;
 }

 ParsedResult getResult() {
   return result;
 }
 /*
Indicates how many buttons the derived class wants shown.
  */
 public abstract int getButtonCount();
 
 public abstract int getButtonText(int index);

public abstract void handleButtonPress(int index);

 /*
displaye the text
  */
 public CharSequence getDisplayContents() {
   String contents = result.getDisplayResult();
   return contents.replace("\r", "");
 }

 public abstract int getDisplayTitle();

 public final ParsedResultType getType() {
   return result.getType();
 }

 final void shareByEmail(String contents) {
	String[] content_detail = contents.split("\n");
	content_detail[0] = "Full name: " + content_detail[0] + "\n";
	content_detail[1] = "School: " + content_detail[1]+ "\n";
	content_detail[2] = "Phone number: " + content_detail[2]+ "\n";
	content_detail[3] = "Email: " + content_detail[3]+ "\n";
	content_detail[4] = "Facebook user: " + content_detail[4]+ "\n";
	content_detail[5] = "Address: " + content_detail[5]+ "\n";
	String result = new String();
	for (int i = 0; i<content_detail.length-1; i++){
		result = result+content_detail[i];
	}
	shareFromUri("mailto:", activity.getString(R.string.msg_share_subject_line), result);
 }
 

 final void sendEmail(String address, String subject, String body) {
   shareFromUri("mailto:" + address, subject, body);
 }

 // Use public Intent fields rather than private GMail app fields to specify subject and body.
 final void shareFromUri(String uri, String subject, String body) {
   Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse(uri));
   putExtra(intent, Intent.EXTRA_SUBJECT, subject);
   putExtra(intent, Intent.EXTRA_TEXT, body);
   intent.setType("text/plain");
   launchIntent(intent);
 }

 final void shareBySMS(String contents) {
   sendSMSFromUri("smsto:", activity.getString(R.string.msg_share_subject_line) + ":\n" +
       contents);
 }

 final void sendSMS(String phoneNumber, String body) {
   sendSMSFromUri("smsto:" + phoneNumber, body);
 }

 final void sendSMSFromUri(String uri, String body) {
   Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
   putExtra(intent, "sms_body", body);
   // Exit the app once the SMS is sent
   intent.putExtra("compose_mode", true);
   launchIntent(intent);
 }

final void addContact(String query){
	  String[] info = query.split("\n");
	  String name = info[0];
	  String phoneNumber = info[2];
	  String email = info[3];
	  String work = info[1];
	  String address = info[5];
	 Intent intent = new Intent(Contacts.Intents.Insert.ACTION, Contacts.People.CONTENT_URI);
	 	putExtra(intent, Contacts.Intents.Insert.NAME, name);
	 	putExtra(intent, Contacts.Intents.Insert.PHONE, phoneNumber);
	 	putExtra(intent, Contacts.Intents.Insert.EMAIL, email);
	 	putExtra(intent, Contacts.Intents.Insert.POSTAL, address);
	    putExtra(intent, Contacts.Intents.Insert.COMPANY, work);
	    launchIntent(intent);
}
final void saveFile(String query){
	String[] content_detail = query.split("\n");
	
	content_detail[0] = "Full name: " + content_detail[0] + "\n";
	content_detail[1] = "School: " + content_detail[1]+ "\n";
	content_detail[2] = "Phone number: " + content_detail[2]+ "\n";
	content_detail[3] = "Email: " + content_detail[3]+ "\n";
	content_detail[4] = "Facebook user: " + content_detail[4]+ "\n";
	content_detail[5] = "Address: " + content_detail[5]+ "\n";
	String result = new String();
	for (int i = 0; i<content_detail.length-1; i++){
		result = result+content_detail[i];
	}
		final byte[] data = result.getBytes();
		final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
       final EditText input = new EditText(activity);
       alert.setView(input);                    
       alert.setTitle("Save to SD card");
       alert.setMessage("Save the information with name: ");
       alert.setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		    	    public void onClick(DialogInterface dialog, int whichButton) {
			            // TODO Auto-generated method stub
			            String fileName=input.getText().toString()+".txt";
			            File file = new File(Environment.getExternalStorageDirectory(), fileName);
			            FileOutputStream fos;
			            try {
			            	fos = new FileOutputStream(file);
			    			fos.write(data);
			    			fos.flush();
			    			fos.close();            
			             Toast.makeText(activity, "Saved", Toast.LENGTH_LONG).show();		            
			            } catch (FileNotFoundException e) {
			             // TODO Auto-generated catch block
			             e.printStackTrace();
			             Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show();
			            } catch (IOException e) {
			             // TODO Auto-generated catch block
			             e.printStackTrace();
			             Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show();
			            }
			           }		 		       	})
		       	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           	public void onClick(DialogInterface dialog, int id) {
		                	dialog.cancel();
		          	}
		       	});
       alert.show();
}

 void launchIntent(Intent intent) {
   if (intent != null) {
     try {
       activity.startActivity(intent);
     } catch (ActivityNotFoundException e) {
       AlertDialog.Builder builder = new AlertDialog.Builder(activity);
       builder.setTitle(activity.getString(R.string.app_name));
       builder.setMessage(activity.getString(R.string.msg_intent_failed));
       builder.setPositiveButton(R.string.button_ok, null);
       builder.show();
     }
   }
 }

 private static void putExtra(Intent intent, String key, String value) {
   if (value != null && value.length() > 0) {
     intent.putExtra(key, value);
   }
 }
}
