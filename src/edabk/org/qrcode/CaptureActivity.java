/* Decode QR code to display 6 fields Content
 * Implement: EDABK1
 * 					Van Hiep Trinh
 * 					Minh Thao Le
 * 					Mai Trang Nguyen
 */
package edabk.org.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import edabk.org.camera.CameraManager;
import edabk.org.qrcode.R;
import edabk.org.result.ResultButtonListener;
import edabk.org.result.ResultHandler;
import edabk.org.result.ResultHandlerFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * The barcode reader activity itself. This is loosely based on the CameraPreview
 * example included in the Android SDK.
 *
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {

  private static final String TAG = CaptureActivity.class.getSimpleName();

  private static final int SETTINGS_ID = Menu.FIRST;
  private static final int ABOUT_ID = Menu.FIRST + 1;
  private static final int EXIT_ID = Menu.FIRST + 2;

  private static final long INTENT_RESULT_DURATION = 1500L;
  
  private static final String ZXING_URL = "http://zxing.appspot.com/scan";
  private static final String RETURN_CODE_PLACEHOLDER = "{CODE}";
  private static final String RETURN_URL_PARAM = "ret";

  private static final Set<ResultMetadataType> DISPLAYABLE_METADATA_TYPES;
  static {
    DISPLAYABLE_METADATA_TYPES = new HashSet<ResultMetadataType>(5);
    DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.ISSUE_NUMBER);
    DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.SUGGESTED_PRICE);
    DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.ERROR_CORRECTION_LEVEL);
    DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.POSSIBLE_COUNTRY);
  }

  private enum Source {
    NATIVE_APP_INTENT,
    PRODUCT_SEARCH_LINK,
    ZXING_LINK,
    NONE
  }

  private CaptureActivityHandler handler;
  private ViewfinderView viewfinderView;
  private TextView statusView;
  private View resultView;
  private Result lastResult;
  private boolean hasSurface;
  private Source source;
  private String sourceUrl;
  private String returnUrlTemplate;
  private Vector<BarcodeFormat> decodeFormats;
  private String characterSet;
  private InactivityTimer inactivityTimer;
  
  protected String[] vals;//include 6 splited fields
  protected String number;

  ViewfinderView getViewfinderView() {
    return viewfinderView;
  }

  public Handler getHandler() {
    return handler;
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    Window window = getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.capture);

    CameraManager.init(getApplication());
    viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
    resultView = findViewById(R.id.result_view);
    statusView = (TextView) findViewById(R.id.status_view);
    handler = null;
    lastResult = null;
    hasSurface = false;
    inactivityTimer = new InactivityTimer(this);
   
  }

  @Override
  protected void onResume() {
    super.onResume();
    SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
    SurfaceHolder surfaceHolder = surfaceView.getHolder();
    if (hasSurface) {
      // The activity was paused but not stopped, so the surface still exists. Therefore
      // surfaceCreated() won't be called, so init the camera here.
      initCamera(surfaceHolder);
    } else {
      // Install the callback and wait for surfaceCreated() to init the camera.
      surfaceHolder.addCallback(this);
      surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    Intent intent = getIntent();
    String action = intent == null ? null : intent.getAction();
    String dataString = intent == null ? null : intent.getDataString();
    if (intent != null && action != null) {
      if (action.equals(Intents.Scan.ACTION)) {
        // Scan the formats the intent requested, and return the result to the calling activity.
        source = Source.NATIVE_APP_INTENT;
        decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
        if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
          int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
          int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
          if (width > 0 && height > 0) {
            CameraManager.get().setManualFramingRect(width, height);
          }
        }
      } 
      else if (dataString != null && dataString.startsWith(ZXING_URL)) {
        // Scan formats requested in query string (all formats if none specified).
        // If a return URL is specified, send the results there. Otherwise, handle it ourselves.
        source = Source.ZXING_LINK;
        sourceUrl = dataString;
        Uri inputUri = Uri.parse(sourceUrl);
        returnUrlTemplate = inputUri.getQueryParameter(RETURN_URL_PARAM);
        decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri);
      } else {
        // Scan all formats and handle the results ourselves (launched from Home).
        source = Source.NONE;
        decodeFormats = null;
      }
      characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
    } else {
      source = Source.NONE;
      decodeFormats = null;
      characterSet = null;
    }
    inactivityTimer.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (handler != null) {
      handler.quitSynchronously();
      handler = null;
    }
    inactivityTimer.onPause();
    CameraManager.get().closeDriver();
  }

  @Override
  protected void onDestroy() {
    inactivityTimer.shutdown();
    super.onDestroy();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (source == Source.NATIVE_APP_INTENT) {
        setResult(RESULT_CANCELED);
        finish();
        return true;
      } else if ((source == Source.NONE || source == Source.ZXING_LINK) && lastResult != null) {
        resetStatusView();
        if (handler != null) {
          handler.sendEmptyMessage(R.id.restart_preview);
        }
        return true;
      }
    } else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
      // Handle these events so they don't launch the Camera app
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

//menu hard button
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
  

  public void surfaceCreated(SurfaceHolder holder) {
    if (!hasSurface) {
      hasSurface = true;
      initCamera(holder);
    }
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    hasSurface = false;
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }


  public void handleDecode(Result rawResult, Bitmap barcode) {
	    lastResult = rawResult;
	    if (barcode == null) {
	      handleDecodeInternally(rawResult);
	    }
	      switch (source) {
	        case NATIVE_APP_INTENT:
	        case ZXING_LINK:
	        case NONE:
	          handleDecodeInternally(rawResult);
	          break;
	      }
	  }
  
 private void handleDecodeInternally(Result rawResult) {
	    statusView.setVisibility(View.GONE);
	    viewfinderView.setVisibility(View.GONE);
	    resultView.setVisibility(View.VISIBLE);    
	    final ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
	    CharSequence title = getString(resultHandler.getDisplayTitle());
	    SpannableStringBuilder styled = new SpannableStringBuilder(title + "\n\n");
	    styled.setSpan(new UnderlineSpan(), 0, title.length(), 0);
	    
	    final CharSequence displayContents = resultHandler.getDisplayContents();
	    final String[] vals = displayContents.toString().split("\n");// split content
	    if (vals.length==7){    	  
	    	//Search name
	    	TextView typeFieldViewName = (TextView) findViewById(R.id.type_field_view_name);	    
	    	styled.append(vals[0]);
	    	final String[] Firstname = vals[0].toString().split(" "); 
	    	if (vals[0].length()==0){
	    		typeFieldViewName.setText("No information from this card");	
	    	}
	    	else {
	    		typeFieldViewName.setText(vals[0]);
	    	TextView nameSearch = (TextView) findViewById(R.id.type_field_view_name);
	    	nameSearch.setOnClickListener(new OnClickListener()
	    	{
	    		public void onClick(View v)
	    		{
	    			final String mEditText_name = vals[0];
	    		    	if(mEditText_name!=null){
	    		    	try {
	    		 		     	AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
	    		 		     builder.setMessage("Do you want to search this name?")
	    		 		       .setCancelable(false)
	    		 		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    		 		           public void onClick(DialogInterface dialog, int id) {
	    		 		        	   //Search action
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
	    	//Search school position
	    	TextView typeFieldViewSchool = (TextView) findViewById(R.id.type_field_view_school);
	    	styled.append(vals[1]);
	    	if (vals[1].length()==0){
	    		typeFieldViewSchool.setText("No information from this card");
	    	}
	    	else {
	    		typeFieldViewSchool.setText(vals[1]);
	    	TextView FindSchool = (TextView) findViewById(R.id.type_field_view_school);
	    	FindSchool.setOnClickListener(new OnClickListener()
	    	{    		
	    		public void onClick(View v)
	    		{   			 
	    		
	    			final String mEditText_School = vals[1];
	    		    	if(mEditText_School!=null){
	    		    		try {
	    		    			
	    		    			AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
	    		 		     	builder.setMessage("Do you want to find the school's position?")
	    		 		       .setCancelable(false)
	    		 		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    		 		           public void onClick(DialogInterface dialog, int id) {
	    		 		        	   //action
	    		 		        	  String query = mEditText_School;
	    		 		        	  String title = vals[1];
	    		 		        	    if (title != null && title.length() > 0) {
	    		 		        	      query = query + " (" + title + ')';
	    		 		        	    }
	    		 		        	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(query))));
	    		 	                           
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

	    	
	    	//call and send massage
	    	TextView typeFieldViewPhonenb = (TextView) findViewById(R.id.type_field_view_phonenb);
	    	styled.append(vals[2]);
	    	if (vals[2].length() == 0) {
	    	typeFieldViewPhonenb.setText("No information from this card");
	    	}
	    	else {
	   		typeFieldViewPhonenb.setText(vals[2]);
	    		number=vals[2];
	    	TextView tvPhone = (TextView) findViewById(R.id.type_field_view_phonenb);
	    	//Context menu Call and Send message
	    	tvPhone.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {    	   
	    	    public void onCreateContextMenu(ContextMenu menu, View v,
	    	        ContextMenuInfo menuInfo) {
	    	    menu.setHeaderTitle("Choose action");
	    	    menu.add(0, 0, 0, "Call");
	    	    menu.add(0, 1, 1, "Send SMS");
	    	     	    }
	    	});   	

	    	tvPhone.setOnClickListener(new OnClickListener() {
	     	    public void onClick(View v) {
	    	    openContextMenu(v); 
	    	    }
	    	});
	    	}
	    //Compose email
	    	
	    	TextView typeFieldViewEmail = (TextView) findViewById(R.id.type_field_view_email);
	    	styled.append(vals[3]);
	    	if (vals[3].length() <2) {
	    		typeFieldViewEmail.setText("No information from card");
	    	}
	    	else {
	    	typeFieldViewEmail.setText(vals[3]);
	    	   
	    	TextView sendEmail = (TextView) findViewById(R.id.type_field_view_email);
	    	sendEmail.setOnClickListener(new OnClickListener()
	    	{
	    		public void onClick(View v)
	    		{
	    			final String mEditText_email = vals[3];
	    		    	if(mEditText_email!=null){
	    		    	try {
	    		 		     	AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
	    		 		     builder.setMessage("Do you want to send an email to "+ Firstname[0])
	    		 		       	.setCancelable(false)
	    		 		       	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    		 		           	public void onClick(DialogInterface dialog, int id) {
	    		 		           	//Action
	    		 		        	   startActivity(new Intent(Intent.ACTION_SENDTO, 
	    		 	                           Uri.fromParts("mailto", mEditText_email, null)));
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
	    	
	    	//View facebook profile
	    	TextView typeFieldViewFacebook = (TextView) findViewById(R.id.type_field_view_facebook);
	    	styled.append(vals[4]);
	    	if (vals[4].length() ==0) {
	    		typeFieldViewFacebook.setText("No information from this card");
	    	}
	    	else {
	    	typeFieldViewFacebook.setText("facebook.com/"+vals[4]);
	    	
	    	TextView AddFacebook = (TextView) findViewById(R.id.type_field_view_facebook);
	    	AddFacebook.setOnClickListener(new OnClickListener()
	    	{
	    		public void onClick(View v){
	    		if(isOnline()){  //test internet connection
	    			String uri="http://graph.facebook.com/"+vals[4];
	    	    	String content="content";
	    	    	try {
	    				content=getStringContent(uri);
	    			} catch (Exception e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	    	       
	    	        String[] content_field=content.split("\"");
	    				final String userId = content_field[3];
	    		    	if(userId!=null){
	    		    	try {
	    		 		     	AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
	    		 		     	builder.setMessage("Do you want to view "+Firstname[0]+"'s facebook")
	    		 		       	.setCancelable(false)
	    		 		       	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    		 		           	public void onClick(DialogInterface dialog, int id) {
	    		 		        	   	String url = "fb://page/" + userId;   
	    		 		        	   	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));   

	    		 		        	   // Add try and catch because the scheme could be changed in an update! 
	    		 		        	   // Another reason is that the Facebook-App is not installed 
	    		 		        	   try {      startActivity(intent);   
	    		 		        	 	} catch (ActivityNotFoundException ex) {      
	    		 		        		 // start web browser and the facebook mobile page as fallback    
	    		 		        		 String uriMobile = "http://touch.facebook.com/pages/x/" + userId;    
	    		 		        	 	Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uriMobile));    
	    		 		        	 	startActivity(i); 
	    		 		        	 	}	   
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
	    		else 
	    		{try {
	    			AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
	    			builder.setTitle("Error connection");
	 		     	builder.setMessage("Can't not connect to Internet." +
	 		     			"Please check your connection settings and try again")
	 		       .setCancelable(false)
	 		       .setPositiveButton("Setting", new DialogInterface.OnClickListener() {
	 		           public void onClick(DialogInterface dialog, int id) {
	 		        	  Intent setting=new Intent(Settings.ACTION_WIRELESS_SETTINGS);
	 		             startActivity(setting); 		        	   
	 		           }
	 		       })
	 		       .setNegativeButton("OK", new DialogInterface.OnClickListener() {
	 		           public void onClick(DialogInterface dialog, int id) {
	 		                dialog.cancel();
	 		           	}
	 		       	});
	 			AlertDialog alert = builder.create();
	 			alert.show();
	      	} catch (Exception e) {
	    	  e.printStackTrace();
	      		}}
	    		}});  
	    	}
	    	
	    	// Search Address
	    	TextView typeFieldViewAddress = (TextView) findViewById(R.id.type_field_view_add);
	    	styled.append(vals[5]);
	    	if (vals[5].length() == 0) {
	    		typeFieldViewAddress.setText("No information from this card");
	    	}
	    	else {
	    		typeFieldViewAddress.setText(vals[5]);
	    	styled.append(vals[5]);
	    
	    	TextView FindAdd = (TextView) findViewById(R.id.type_field_view_add);
	    	FindAdd.setOnClickListener(new OnClickListener()
	    	{
	    		public void onClick(View v)
	    		{
	    			final String mEditText_Add = vals[5];
	    		    	if(mEditText_Add!=null){
	    		    		try {
	    		    			AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
	    		 		     	builder.setMessage("Do you want to find "+Firstname[0]+"'s position?")
	    		 		       .setCancelable(false)
	    		 		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    		 		           public void onClick(DialogInterface dialog, int id) {
	    		 		        	   //Action
	    		 		        	  String query = mEditText_Add;
	    		 		        	  String title = vals[0];
	    		 		        	    if (title != null && title.length() > 0) {
	    		 		        	      query = query + " (" + title + ')';
	    		 		        	    }
	    		 		        	    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(query))));
	    		 	                           
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
	    	int buttonCount = resultHandler.getButtonCount();
	    	ViewGroup buttonView = (ViewGroup) findViewById(R.id.result_button_view);
	    	buttonView.requestFocus();
	    	for (int x = 0; x < ResultHandler.MAX_BUTTON_COUNT; x++) {
	    		TextView button = (TextView) buttonView.getChildAt(x);
	    		if (x < buttonCount) {
	    			button.setVisibility(View.VISIBLE);
	    			button.setText(resultHandler.getButtonText(x));
	    			button.setOnClickListener(new ResultButtonListener(resultHandler, x));
	    		} else {
	    			button.setVisibility(View.GONE);
	    		}
	    	}
	    }

	    // String without 6 fields
	  else 
	  {
		  try {	 
			     AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
			     builder.setMessage("Not the card. Do you want scan this barcode?")
			       .setCancelable(false)
			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   	  CaptureActivity.this.finish();
			        	   	  Intent capture2=new Intent(CaptureActivity.this, CaptureActivity2.class);
			        		  capture2.putExtra("displayContents", displayContents);
			        		  startActivity(capture2);
			        		  finish();
			        		  }
			           public void onKeyDown(int keyCode, KeyEvent event) {
			        	    if (keyCode == KeyEvent.KEYCODE_BACK) {
			        	       startActivity(new Intent(CaptureActivity.this, CaptureActivity.class));
			        	    }
			        	}
			           })
			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   Intent capture1=new Intent(CaptureActivity.this,CaptureActivity.class);
			        		  startActivity(capture1);}
			       });
			AlertDialog alert = builder.create();
			alert.show();
	   } catch (Exception e) {
	     e.printStackTrace();
	   } 
	  }
	}
	 //Onclick Call and Send message Context menu
	  public boolean onContextItemSelected(MenuItem item) {  
	      if(item.getTitle()=="Call"){
	     	Intent callIntent = new Intent(Intent.ACTION_CALL);
	     	 callIntent.setData(Uri.parse("tel:"+number ));
	          startActivity(callIntent);
	      }  
	      else if(item.getTitle()=="Send SMS"){
	    	  Intent sendIntent = new Intent(Intent.ACTION_VIEW);
	    	  sendIntent.setData(Uri.parse("sms:"+number ));
	    	  startActivity(sendIntent);
	    	 
	      }  
	      else {return false;}  
	  return true;  
	  }  
	  //get content grapht.facebook.com
	public static String getStringContent(String uri) throws Exception {
	      
	      HttpClient client = new DefaultHttpClient();
	      HttpGet request = new HttpGet();
	      request.setURI(new URI(uri));
	      HttpResponse response = client.execute(request);
	      InputStream ips  = response.getEntity().getContent();
	      BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"UTF-8"));

	      StringBuilder sb = new StringBuilder();
	      String s;
	      while(true )
	      {
	          s = buf.readLine();
	          if(s==null || s.length()==0)
	              break;
	          sb.append(s);

	      }
	      buf.close();
	      ips.close();
	      return sb.toString();                 
	  
	      }
	

  // Briefly show the contents of the barcode, then handle the result outside Barcode Scanner.
  private void handleDecodeExternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
    viewfinderView.drawResultBitmap(barcode);

    statusView.setText(getString(resultHandler.getDisplayTitle()));

    if (source == Source.NATIVE_APP_INTENT) {
      // Hand back whatever action they requested - this can be changed to Intents.Scan.ACTION when
      // the deprecated intent is retired.
      Intent intent = new Intent(getIntent().getAction());
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
      intent.putExtra(Intents.Scan.RESULT, rawResult.toString());
      intent.putExtra(Intents.Scan.RESULT_FORMAT, rawResult.getBarcodeFormat().toString());
      byte[] rawBytes = rawResult.getRawBytes();
      if (rawBytes != null && rawBytes.length > 0) {
        intent.putExtra(Intents.Scan.RESULT_BYTES, rawBytes);
      }
      Message message = Message.obtain(handler, R.id.return_scan_result);
      message.obj = intent;
      handler.sendMessageDelayed(message, INTENT_RESULT_DURATION);
    } else if (source == Source.PRODUCT_SEARCH_LINK) {
      // Reformulate the URL which triggered us into a query, so that the request goes to the same
      // TLD as the scan URL.
      Message message = Message.obtain(handler, R.id.launch_product_query);
      int end = sourceUrl.lastIndexOf("/scan");
      message.obj = sourceUrl.substring(0, end) + "?q=" +
          resultHandler.getDisplayContents().toString() + "&source=zxing";
      handler.sendMessageDelayed(message, INTENT_RESULT_DURATION);
    } else if (source == Source.ZXING_LINK) {
      // Replace each occurrence of RETURN_CODE_PLACEHOLDER in the returnUrlTemplate
      // with the scanned code. This allows both queries and REST-style URLs to work.
      Message message = Message.obtain(handler, R.id.launch_product_query);
      message.obj = returnUrlTemplate.replace(RETURN_CODE_PLACEHOLDER,
          resultHandler.getDisplayContents().toString());
      handler.sendMessageDelayed(message, INTENT_RESULT_DURATION);
    }
  }


//initCamera
  private void initCamera(SurfaceHolder surfaceHolder) {
    try {
      CameraManager.get().openDriver(surfaceHolder);
      // Creating the handler starts the preview, which can also throw a RuntimeException.
      if (handler == null) {
        handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
      }
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      displayFrameworkBugMessageAndExit();
    } catch (RuntimeException e) {
      // Barcode Scanner has seen crashes in the wild of this variety:
      // java.?lang.?RuntimeException: Fail to connect to camera service
      Log.w(TAG, "Unexpected error initializating camera", e);
      displayFrameworkBugMessageAndExit();
    }
  }

  private void displayFrameworkBugMessageAndExit() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.app_name));
   
    builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
    builder.setOnCancelListener(new FinishListener(this));
    builder.show();
  }

  public void drawViewfinder() {
    viewfinderView.drawViewfinder();
  }
  
//test intertnet
  public boolean isOnline() {
	  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	  try {
	  return cm.getActiveNetworkInfo().isConnectedOrConnecting( );
	  } catch (Exception e) {
	  return false;
	  }
	  }
  private void resetStatusView() {
	    resultView.setVisibility(View.GONE);
	    statusView.setVisibility(View.VISIBLE);
	    statusView.setBackgroundColor(getResources().getColor(R.color.status_view));
	    viewfinderView.setVisibility(View.VISIBLE);

	    TextView textView = (TextView) findViewById(R.id.status_view);
	    textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
	    textView.setTextSize(14.0f);
	    textView.setText(R.string.msg_default_status);
	    lastResult = null;
	  }


  public void exitOptionsDialog() {
		     AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you want to exit this application?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   CaptureActivity.this.finish();
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
 
 
public void onBackPressed() {
      super.onBackPressed();
      CaptureActivity.this.finish();
      Intent home=new Intent(CaptureActivity.this, HomeActivity.class);
      startActivity(home);
      finish();
  }
}
