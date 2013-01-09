/* $Id: $
   Copyright 2012, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.marakana.android.labs.concurrency.svc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.marakana.android.labs.concurrency.MainActivity;


/**
 * A Service Helper based on PendingIntent
 *
 * @version $Revision: $
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 */
public class PostingServiceHelper {
    private static final String TAG = "POST_HELPER";
    private static final int REQ_ID = 42;

    public static interface PostCompletionHandler { void postComplete(int res); }

    private static final PostingServiceHelper instance = new PostingServiceHelper();

    public static PostingServiceHelper getInstance() { return instance; }

    public void post(Activity ctxt, String text) {
        Intent intent = new Intent(ctxt, PostingService.class);
        intent.putExtra(PostingService.TEXT, text);
        intent.putExtra(
            PostingService.CALLBACK,
            ctxt.createPendingResult(
                REQ_ID,
                new Intent(), // empty default response
                PendingIntent.FLAG_ONE_SHOT));
        Log.d(TAG, "sending post");
        ctxt.startService(intent);
    }

    public boolean onResult(
        int reqCode,
        int resCode,
        Intent resp,
        PostCompletionHandler hdlr)
    {
        Log.d(TAG, "post reply: " + reqCode + ", " + resCode);
        if (REQ_ID != reqCode) { return false; }

        int status = MainActivity.STATUS_FAIL;
        if (Activity.RESULT_OK == resCode) {
            status = resp.getExtras().getInt(PostingService.RESULT);
        }

        hdlr.postComplete(status);

        return true;
    }
}
