/*This Activity implement encode from Contact, String, Custom Card
 * After encode, QR image will be saved in SD card
 * Implement: EDABK1
 * 					Van Hiep Trinh
 * 					Minh Thao Le
 * 					Mai Trang Nguyen
 */
package edabk.org.qrcode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.google.zxing.BarcodeFormat;
import edabk.org.qrcode.R;

import edabk.org.qrcode.Contents;
import edabk.org.qrcode.Intents;
import edabk.org.qrcode.QRCodeEncoder;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi" })
public class QrcodeActivity extends Activity {
    /** Called when the activity is first created. */
	String encode_string,encode_card;
	ProgressDialog pg;
	AutoCompleteTextView txtPhoneNo;
	TextView tv;
	Button bt;
	
	public ArrayList<String> c_Name = new ArrayList<String>();
	public ArrayList<String> c_Number = new ArrayList<String>();
	String[] name_Val=null;
	String[] phone_Val=null;
	//Create
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.qrencode);
	    	//EditText text_name,text_school,text_phone,text_email,text_facebook,text_address;
	        final EditText text_name=(EditText)findViewById(R.id.text_name);
	        final EditText text_phone=(EditText)findViewById(R.id.text_phone);
	        final EditText text_school=(EditText)findViewById(R.id.text_school);
	        final EditText text_email=(EditText)findViewById(R.id.text_email);
	        final EditText text_facebook=(EditText)findViewById(R.id.text_facebook);
	        final EditText text_address=(EditText)findViewById(R.id.text_address);
	        final EditText text_string=(EditText)findViewById(R.id.text_string);
	        txtPhoneNo = (AutoCompleteTextView) findViewById(R.id.txtPhoneNo);
	        txtPhoneNo.setThreshold(1);
	        txtPhoneNo.setDropDownHeight(200);
            bt=(Button) findViewById(R.id.btEncodeContact);
          
          //Adapter contact for txtPhoneNo AutoCompleteTextView
           Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
           startManagingCursor(phones);
           if(phones != null)
           {
           int count = 0;
           while (phones.moveToNext())
           {
           count++;
           String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
           c_Name.add(name);
           
           }
           }
           phones.close();
           //}
           name_Val = (String[]) c_Name.toArray(new String[((ArrayList<String>) c_Name).size()]);
           ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, name_Val);
          txtPhoneNo.setAdapter(adapter);
          //end adapter
         //button encode contact
         bt.setOnClickListener(new OnClickListener()
  		{
  			@SuppressLint("NewApi")
			public void onClick(View v) {
  				// TODO Auto-generated method stub  			
  				String txt=txtPhoneNo.getText().toString();  				
  				String name ="";
  				   String number="";
  				   String email="";
  				   String id="";
  				   String address="";
  				   String organization="";
  				   String url="";
  				   String text="";
  				String contname = txt;
  				   if (contname != null && contname.length()>0) {
  					   Uri lkup = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,contname);       
  					   ContentResolver cr = getContentResolver();
  					   Cursor idCursor = getContentResolver().query(lkup, null, null, null, null);
  					    if(idCursor.move(1)) {
  					        id=idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts._ID));
  						    name = idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
    					   }		  
  		   
  					  	String Where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
  					// Phone Number
  					  	String phoneWhere = Phone.CONTACT_ID + " = ? ";
                        String[] phoneWhereParams = new String[] { id + "" };
                        Cursor phoneCur = cr.query(Phone.CONTENT_URI, null, phoneWhere, phoneWhereParams, null);
                        while (phoneCur.move(1)) {
  							number = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
  				 		}
                        //Email
                        Cursor emailCur = cr.query(Email.CONTENT_URI, null,
                                Email.CONTACT_ID + " = ?", new String[] { id + "" },
                                null);
                        while (emailCur.move(1)) {
  							email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
  				 		}
                        //Address
                        String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
                                + ContactsContract.Data.MIMETYPE + " = ?";
                        String[] addrWhereParams = new String[] { id + "",
                                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE };
                        Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI, null,
                                addrWhere, addrWhereParams, null);
                        while (addrCur.move(1)) {
                                String poBox = addrCur
                                                .getString(addrCur
                                                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
                                String street = addrCur
                                                .getString(addrCur
                                                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                                String city = addrCur
                                                .getString(addrCur
                                                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                                String state = addrCur
                                                .getString(addrCur
                                                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                                String postalCode = addrCur
                                                .getString(addrCur
                                                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
                                String country = addrCur
                                                .getString(addrCur
                                                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                                String neighbor = addrCur
                                                .getString(addrCur
                                                                .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD));
                                address = poBox+street+city+state+postalCode+country+neighbor;
  				 		}
                        //Organization
                        String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
                                + ContactsContract.Data.MIMETYPE + " = ? ";
		                String[] orgWhereParams = new String[] {
		                                id + "",
		                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };
		                Cursor orgCursor = cr.query(ContactsContract.Data.CONTENT_URI,
		                                null, orgWhere, orgWhereParams, null);
		                while (orgCursor.move(1)) {
		                        organization = orgCursor.getString(orgCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
		                }
		              
                
		                //URL
				        String websiteWhere = ContactsContract.Data.CONTACT_ID
				                        + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ? ";
				        String[] websiteWhereParams = new String[] { id + "",
				                        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE };
				        Cursor webCursor = cr.query(ContactsContract.Data.CONTENT_URI,
				                        null, websiteWhere, websiteWhereParams, null);
				        while (webCursor.move(1)) {
				                url = webCursor.getString(webCursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
				        }
  								text=(name+"\n"+organization+"\n"+number+"\n"+email+"\n"+url+"\n"+address+"\nEDABK");
  						if(idCursor.getCount()==0)
  						{final AlertDialog AD=new AlertDialog.Builder(QrcodeActivity.this).create();
 						AD.setTitle("Encode Error");
  						AD.setMessage("Can't find "+contname+" in contact");
  						AD.setButton("OK",
  								new DialogInterface.OnClickListener() {
  									
  									public void onClick(DialogInterface dialog, int which) {
  										AD.cancel();
  									}});
  						AD.show();}  						
  						else generateQRImage(QrcodeActivity.this, handler, text); 							
  			   
  			}}
  			
  		});
         
        //button encode Card        
        ((Button)findViewById(R.id.btEncodeCard)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				
				encode_card=text_name.getText().toString()+"\n"
							+text_school.getText().toString()+"\n"
							+text_phone.getText().toString()+"\n"
							+text_email.getText().toString()+"\n"
							+text_facebook.getText().toString()+"\n"
							+text_address.getText().toString()+"\n"
							+"EDABK";				
				generateQRImage(QrcodeActivity.this, handler, encode_card);
				
			}	
		});
        
       //button encode String
        ((Button)findViewById(R.id.btEncodeString)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				
				encode_string=text_string.getText().toString();
				generateQRImage(QrcodeActivity.this, handler, encode_string);
			}	
		});
        
    }
   //Generate QR code 
    public static void generateQRImage(Activity activity, Handler handler, String value){
		  Intent intent=new Intent();
		  intent.setAction(Intents.Encode.ACTION);
		  intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
		  intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
		  intent.putExtra(Intents.Encode.DATA, value);
		  generateImage(activity, intent, handler);
	}
    public static void generateImage(Activity activity, Intent intent, Handler handler){
		try{
			
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(activity, intent);
            
            activity.setTitle(activity.getString(R.string.app_name) + " - " + qrCodeEncoder.getTitle());
           
            qrCodeEncoder.requestBarcode(handler, 230);
            
        } catch (IllegalArgumentException e) {
        	    }
	}
    
  
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			System.out.println("**************");
			switch (message.what) {
		     	case R.id.encode_succeeded:
		          final Bitmap image = (Bitmap) message.obj;
		          if(pg!=null) pg.dismiss();
		          setContentView(R.layout.qr_image);
		          //Display QR code image
		          ImageView img=(ImageView)findViewById(R.id.qr);
		          System.out.println();
		          img.setImageBitmap(image);
		          Button btSaveQr=(Button)findViewById(R.id.btSaveQr);
		          //Save QR code image to SD card 
		          final String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
		            btSaveQr.setOnClickListener(new View.OnClickListener(){
		  			public void onClick(View v) {
		  			final AlertDialog.Builder alert = new AlertDialog.Builder(QrcodeActivity.this);
                    final EditText input = new EditText(QrcodeActivity.this);
                    alert.setView(input);                    
                    alert.setTitle("Save to SD card");
                    alert.setMessage("Save QR code image with name: ");
                    alert.setCancelable(false)
		 		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		 		    	    public void onClick(DialogInterface dialog, int whichButton) {
		 			            // TODO Auto-generated method stub
		 			            OutputStream outStream = null;
		 			            String name_qr=input.getText().toString()+".PNG";
		 			            File file = new File(extStorageDirectory,name_qr);
		 			            try {
		 			             outStream = new FileOutputStream(file);
		 			             image.compress(Bitmap.CompressFormat.PNG, 100, outStream);
		 			             outStream.flush();
		 			             outStream.close();		            
		 			             Toast.makeText(QrcodeActivity.this, "Saved", Toast.LENGTH_LONG).show();		            
		 			            } catch (FileNotFoundException e) {
		 			             // TODO Auto-generated catch block
		 			             e.printStackTrace();
		 			             Toast.makeText(QrcodeActivity.this, e.toString(), Toast.LENGTH_LONG).show();
		 			            } catch (IOException e) {
		 			             // TODO Auto-generated catch block
		 			             e.printStackTrace();
		 			             Toast.makeText(QrcodeActivity.this, e.toString(), Toast.LENGTH_LONG).show();
		 			            }
		 			           }		 		       	})
		 		       	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		 		           	public void onClick(DialogInterface dialog, int id) {
		 		                	dialog.cancel();
		 		          	}
		 		       	});
                    alert.show();
		  			}});		          	         
		        		     	case R.id.encode_failed:
		     	  if(pg!=null) pg.dismiss();		      
		          break;
			}
		}
	};
	
	//Menu and back press
			private static final int SETTINGS_ID = Menu.FIRST;
			  private static final int ABOUT_ID = Menu.FIRST + 1;
			  private static final int EXIT_ID = Menu.FIRST + 2;
			@Override
			  public boolean onCreateOptionsMenu(Menu menu) {
			    super.onCreateOptionsMenu(menu);
			    menu.add(0, SETTINGS_ID, 0, R.string.menu_settings)
			        .setIcon(R.drawable.setting);
			    menu.add(0, ABOUT_ID, 0, R.string.menu_about)
			        .setIcon(R.drawable.get_info);
			    menu.add(0, EXIT_ID, 0, R.string.menu_exit)
			    	.setIcon(R.drawable.exit);
			    return true;
			  }

			  @Override
			  public boolean onOptionsItemSelected(MenuItem item) {
			    switch (item.getItemId()) {
			      case SETTINGS_ID: {
			        Intent intent = new Intent(Intent.ACTION_VIEW);
			        intent.setClassName(this, PreferencesActivity.class.getName());
			        startActivity(intent);
			        break;
			      }
			      case ABOUT_ID:
			        AlertDialog.Builder builder = new AlertDialog.Builder(this);
			        builder.setTitle(getString(R.string.title_about));
			        builder.setMessage(getString(R.string.msg_about) + "\n\n" + getString(R.string.author_contact));
			        builder.setNegativeButton(R.string.button_ok, null);
			        builder.show();
			        break;
			      case EXIT_ID:
			    	  exitOptionsDialog();
			    	  break;
			      
			    }
			    return super.onOptionsItemSelected(item);
			  }
			  
			  @SuppressLint("NewApi")
			public void exitOptionsDialog() {
				     AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Do you want to exit this application?")
				       .setCancelable(false)
				       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   finish();
				        	   android.os.Process.killProcess(android.os.Process.myPid());
				               System.exit(0);

				           }
				       })
				       .setNegativeButton("No", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
				    }

		@Override
		public void onBackPressed() {
		   super.onBackPressed();
		  QrcodeActivity.this.finish();
		      Intent home=new Intent(QrcodeActivity.this,HomeActivity.class);
		      startActivity(home);
		      finish();
		}//End menu, backpress
	
	
	
}