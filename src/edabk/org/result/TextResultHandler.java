/* using com.google.zing package to decode QR code*/
package edabk.org.result;

import edabk.org.qrcode.R;
import com.google.zxing.client.result.ParsedResult;

import edabk.org.qrcode.PreferencesActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/*
This class handles TextParsedResult as well as unknown formats
*/
public final class TextResultHandler extends ResultHandler {
 private static final int[] buttons = {
     R.string.button_save_data,
     R.string.button_share,
     R.string.button_add_contact,
 };

 private final String customProductSearch;

 public TextResultHandler(Activity activity, ParsedResult result) {
   super(activity, result);
   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
   customProductSearch = prefs.getString(PreferencesActivity.KEY_CUSTOM_PRODUCT_SEARCH, null);
   
 }

 @Override
 public int getButtonCount() {
   return customProductSearch != null && customProductSearch.length() > 0 ?
           buttons.length : buttons.length;
 }

 @Override
 public int getButtonText(int index) {
   return buttons[index];
 }

 @Override
 public void handleButtonPress(int index) {
   String text = getResult().getDisplayResult();
   switch (index) {
     case 0:
   	  saveFile(text);
       break;
     case 1:
       shareByEmail(text);
       break;
    case 2:
   	addContact(text);
   	break;
   }
 }
 @Override
 public int getDisplayTitle() {
   return R.string.result_text;
 }
}
