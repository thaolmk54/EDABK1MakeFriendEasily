 /* using com.google.zing package to decode QR code*/
package edabk.org.result;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;


import android.app.Activity;

/*
Android-specific handlers 
*/
public final class ResultHandlerFactory {
 private ResultHandlerFactory() {
 }
//text result
 public static ResultHandler makeResultHandler(Activity activity, Result rawResult) {
   ParsedResult result = parseResult(rawResult);
     // The TextResultHandler is the fallthrough for unsupported formats.
   	return new TextResultHandler(activity, result);
   }

 private static ParsedResult parseResult(Result rawResult) {
   return ResultParser.parseResult(rawResult);
 }
}
