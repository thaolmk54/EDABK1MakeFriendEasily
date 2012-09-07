/* Start Screen using loading screen. After run this Activity apps will run HomeActivity
 * Implement: EDABK1
 * 					Minh Thao Le
 * 					Van Hiep Trinh
 * 					Mai Trang Nguyen
 */
package edabk.org.qrcode;

/* splash screen is a loading screen*/
import edabk.org.qrcode.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;



public class SplashScreen extends Activity 
{
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {    	
        super.onCreate(savedInstanceState);  
		
		//Initialize a LoadViewTask object and call the execute() method
        new LoadViewTask().execute();
        
    }
    
    //To use the AsyncTask, it must be subclassed
    private class LoadViewTask extends AsyncTask<Void, Integer, Void>
    {
    	//A TextView object and a ProgressBar object
    	private TextView tv_progress;
    	private ProgressBar pb_progressBar;
    	Intent homeIntent=new Intent(SplashScreen.this,HomeActivity.class);
		   	
    	//Before running code in the separate thread
		@Override
		protected void onPreExecute() 
		{
			setContentView(R.layout.loadingscreen);
			
			//Initialize the TextView and ProgressBar instances - IMPORTANT: call findViewById()
			tv_progress = (TextView) findViewById(R.id.tv_progress);
			pb_progressBar = (ProgressBar) findViewById(R.id.pb_progressbar);
			//Sets the maximum value of the progress bar to 100 			
			pb_progressBar.setMax(100);
			
		}

		//The code to be executed in a background thread.
		@Override
		protected Void doInBackground(Void... params) 
		{
			/* This is just a code that delays the thread execution 4 times, 
			 * during 850 milliseconds and updates the current progress. This 
			 * is where the code that is going to be executed on a background
			 * thread must be placed. 
			 */
			try 
			{
				//Get the current thread's token
				synchronized (this) 
				{
					//Initialize an integer (that will act as a counter) to zero
					int counter = 0;
					//While the counter is smaller than four
					while(counter <= 4)
					{
						//Wait 850 milliseconds
						this.wait(850);
						//Increment the counter 
						counter++;
						//Set the current progress. 
						//This value is going to be passed to the onProgressUpdate() method.
						publishProgress(counter*25);
					}
				}
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			return null;
		}

		//Update the TextView and the progress at progress bar
		@Override
		protected void onProgressUpdate(Integer... values) 
		{
			//Update the progress at the UI if progress value is smaller than 100
			if(values[0] <= 100)
			{
				tv_progress.setText(Integer.toString(values[0]) + "%");
				pb_progressBar.setProgress(values[0]);
			}
			//when progress finished, start HomeActivity
			else { startActivity(homeIntent);
					SplashScreen.this.finish();
			}
		}
		
		
    }
    
    
}