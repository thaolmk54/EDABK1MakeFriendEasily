/* using com.google.zing package to decode QR code*/
package edabk.org.qrcode;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

/*
 * The main settings activity.
 */
public final class PreferencesActivity extends PreferenceActivity
    implements OnSharedPreferenceChangeListener {

  static final String KEY_DECODE_1D = "preferences_decode_1D";
  static final String KEY_DECODE_QR = "preferences_decode_QR";
  public static final String KEY_CUSTOM_PRODUCT_SEARCH = "preferences_custom_product_search";
  
  private CheckBoxPreference decode1D;
  private CheckBoxPreference decodeQR;

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    addPreferencesFromResource(R.xml.preferences);

    PreferenceScreen preferences = getPreferenceScreen();
    preferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    decode1D = (CheckBoxPreference) preferences.findPreference(KEY_DECODE_1D);
    decodeQR = (CheckBoxPreference) preferences.findPreference(KEY_DECODE_QR);
  }

  // Prevent the user from turning off both decode options
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(KEY_DECODE_1D)) {
      decodeQR.setEnabled(decode1D.isChecked());
      decodeQR.setChecked(true);
    } else if (key.equals(KEY_DECODE_QR)) {
      decode1D.setEnabled(decodeQR.isChecked());
      decode1D.setChecked(true);
    }
  }
}
