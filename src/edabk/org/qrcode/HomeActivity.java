/*Home Screen display 2 option:
 * Create ID: Encode QR code from Contact or String and Custom card and save QR image to SDcard (QrcodeACtivity)
 * Read ID: Decode QR code and display contents are encoded in image (CaptureActivity)
 * 			User can: Search Name
 * 					  Search Position of Name and School
 * 					  Call and Send message 
 * 					  Compose Email
 * 					  View Facebook Profile and send add friend request
 * Implement: EDABK1
 * 					Minh Thao Le
 * 					Van Hiep Trinh
 * 					Mai Trang Nguyen
 */
package edabk.org.qrcode;

import edabk.org.qrcode.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class HomeActivity extends Activity{
	
	//Create
	  public void onCreate(Bundle savedInstanceState){
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.home);
	   
	    // Encode
	    final Intent encode=new Intent(HomeActivity.this,QrcodeActivity.class);
	    TextView t_encode = (TextView) findViewById(R.id.button_encode);
	    Typeface faceDecode=Typeface.createFromAsset(getAssets(),
	                                            "fonts/LCALLIG.TTF");
	    
	    t_encode.setTypeface(faceDecode);
	    t_encode.setOnClickListener(new OnClickListener()
	    {
	    	public void onClick(View v)
	    	{
	    		HomeActivity.this.finish();
	    		startActivity(encode);
	    		finish();
	    	}
	    });
	    
	    //Decode
	    TextView t_decode = (TextView) findViewById(R.id.button_decode);
	    Typeface faceEncode=Typeface.createFromAsset(getAssets(),
	                                          "fonts/LCALLIG.TTF");
	    t_decode.setTypeface(faceEncode);
	    t_decode.setOnClickListener(new OnClickListener()
	    {
	    	public void onClick(View v)
	    	{
	    		HomeActivity.this.finish();
	    		Intent decode=new Intent(HomeActivity.this,CaptureActivity.class);
	    		startActivity(decode);	    		
	    		finish();
	    	}
	    });
};
public static void setAutoOrientationEnabled(ContentResolver resolver, boolean enabled)
{
  Settings.System.putInt(resolver, Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
}
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
//menu hard button
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
 moveTaskToBack(true);
}}
