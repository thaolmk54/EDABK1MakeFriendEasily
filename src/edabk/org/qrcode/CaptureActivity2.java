/* Display Content without 6 fields
 * Implement: EDABK1
 *
 * 					Minh Thao Le
 * 					Van Hiep Trinh
 * 					Mai Trang Nguyen
 */
package edabk.org.qrcode;


import edabk.org.qrcode.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public final class CaptureActivity2<backListener> extends Activity{
	private static final int SETTINGS_ID = Menu.FIRST;
	private static final int ABOUT_ID = Menu.FIRST + 1;
	private static final int EXIT_ID = Menu.FIRST + 2;
	public void onCreate(Bundle savedInstanceState){
	    super.onCreate(savedInstanceState);
	    Window window = getWindow();
	    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    //Get and display content
	    setContentView(R.layout.capture2);
	    Bundle b=getIntent().getExtras();
	     final CharSequence displayContents=b.getCharSequence("displayContents");
	    TextView tv=(TextView) findViewById(R.id.type_contents_2);
	    tv.setText(displayContents);
	     //Google Search display content       
        TextView nameSearch = (TextView) findViewById(R.id.type_contents_2);
    	nameSearch.setOnClickListener(new OnClickListener()
    	{
    		public void onClick(View v)
    		{
    			final String mEditText_name = displayContents.toString();
    		    	if(mEditText_name!=null){
    		    	try {
    		 		     	AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity2.this);
    		 		     builder.setMessage("Do you want to search this name?")
    		 		       .setCancelable(false)
    		 		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    		 		           public void onClick(DialogInterface dialog, int id) {
    		 		        	   	Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
    		 		        	    intent.putExtra("query", mEditText_name);
    		 		        	    startActivity(intent);
    		 		           	}
    		 		       	})
    		 		       	.setNegativeButton("No", new DialogInterface.OnClickListener() {
    		 		           	public void onClick(DialogInterface dialog, int id) {
    		 		                	dialog.cancel();
    		 		          	}
    		 		       	});
    		 			AlertDialog alert = builder.create();
    		 			alert.show();
    		      	} catch (Exception e) {
    		    	  e.printStackTrace();
    		      	}
    		    	}
    		  	}
    	});  

  }
	
	//Menu and Back press
	public void onBackPressed() {
	      //super.onBackPressed();
	      	CaptureActivity2.this.finish();
      		Intent capture1=new Intent(CaptureActivity2.this,CaptureActivity.class);
		  	startActivity(capture1); 	
		  	finish();
	  }
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
	public void exitOptionsDialog() {
	     AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setMessage("Do you want to exit this application?")
	       .setCancelable(false)
	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   CaptureActivity2.this.finish();
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
}