package com.eilifint.ravimal.ebooksfree;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Ravimal on 9/3/2016.
 * {@link AsyncTask} to perform the network request on a background thread, and then
 * update the UI with the book information  in the response.
 */
public class BookAsyncTask extends AsyncTask<String, Void, ArrayList<Book>> {
    // progressDialog for indicate data loading
    ProgressDialog dialog;
    // reference variable
    public AsyncResponse delegate = null;
    Context context;

    /**
     * Create a new BookAsyncTask object.
     *
     * @param delegate is {@link AsyncResponse} object
     */
    public BookAsyncTask(AsyncResponse delegate) {
        this.delegate = delegate;
        dialog = new ProgressDialog((Context) delegate);
    }


    /**
     * this interface is implemented in MainActivity class
     */
    public interface AsyncResponse {
        /**
         * Abstract method
         */
        void processFinish(ArrayList<Book> output);
    }


    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * start showing progress dialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // set indeterminate style
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // set title and message
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setMessage(dialog.getContext().getString(R.string.progress_message));
        dialog.setCanceledOnTouchOutside(false);
        // and show it
        dialog.show();
    }

    /**
     * invoke the processFinish method in {@link AsyncResponse}
     * and parse  {@link ArrayList} to the MainActivity).
     *
     * @param books list of books
     */
    @Override
    protected void onPostExecute(ArrayList<Book> books) {
        super.onPostExecute(books);
        delegate.processFinish(books);
        //stop progress dialog
        dialog.dismiss();
    }

    /**
     * perform the network request on a background thread
     *
     * @param params string values
     */
    @Override
    protected ArrayList<Book> doInBackground(String... params) {

        // Create URL object
        URL url = null;
        context = dialog.getContext();
        try {
            url = createUrl(params[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            // TODO Handle the IOException
        }

        // Extract relevant fields from the JSON response and create an {@link Book} object
        ArrayList<Book> book = extractFeatureFromJson(jsonResponse);

        // Return the {@link Book} object as the result fo the {@link BookAsyncTask}

        return book;
    }

    /**
     * Returns new URL object from the given string URL.
     *
     * @param stringUrl url as a string
     */
    private URL createUrl(String stringUrl) throws MalformedURLException {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, context.getString(R.string.error_url), exception);
            return null;
        }
        //return url
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     *
     * @param url endpoint url
     */
    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        String LOG_TAG = MainActivity.class.getSimpleName();

        //if the url is null, then return early
        if (url == null) {
            return jsonResponse;
        }
        // url connection
        HttpURLConnection urlConnection = null;
        // data stream
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(context.getString(R.string.get));
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            //connect to network
            urlConnection.connect();

            //if the request was successful ( response code is 200)
            //then read input stream and parse the response
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else
                Log.e(LOG_TAG, context.getString(R.string.code) + urlConnection.getResponseCode());
        } catch (IOException e) {
            // TODO: Handle the exception
            Log.e(LOG_TAG, context.getString(R.string.problem_json), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     *
     * @param inputStream data stream
     */
    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName(context.getString(R.string.utf8)));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return an {@link Book} object by parsing out information
     * about  books from the input bookJSON string.
     *
     * @param bookJSON string value of json object
     */
    private ArrayList<Book> extractFeatureFromJson(String bookJSON) {

        // Create an empty ArrayList that we can start adding books to list
        ArrayList<Book> bookList = new ArrayList<>();

        //if the json string is empty or null , then return early
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }
        try {

            //json object from bookJSON String
            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            //check item array exists in json response
            if (baseJsonResponse.has(context.getString(R.string.items))) {
                JSONArray itemArray = baseJsonResponse.getJSONArray(context.getString(R.string.items));

                // If there are results in the features array
                if (itemArray.length() > 0) {
                    for (int i = 0; i < itemArray.length(); i++) {

                        // author name
                        String author = context.getString(R.string.unknown);
                        // no of pages
                        int pageCount = 0;
                        // book download link
                        String downloadLink = context.getString(R.string.not_found);
                        // thumbnail url
                        String thumbnail = context.getString(R.string.not_found);
                        // written language of book
                        String language = context.getString(R.string.unknown);
                        // web reader link for the book
                        String webReaderLink = context.getString(R.string.not_found);
                        // preview link of the book
                        String previewLink = context.getString(R.string.not_found);

                        // Extract out the volumeInfo (which is an JSONObject)
                        JSONObject volumeInfo = itemArray.getJSONObject(i).getJSONObject(context.getString(R.string.volume_info));
                        // get book title
                        String title = volumeInfo.getString(context.getString(R.string.title));

                        // check existence of authors and set value to author variable
                        if (volumeInfo.has(context.getString(R.string.authors))) {
                            author = volumeInfo.getJSONArray(context.getString(R.string.authors)).getString(0);
                        }
                        // check existence of authors and set value to author variable
                        if (volumeInfo.has(context.getString(R.string.page_count))) {
                            pageCount = volumeInfo.getInt(context.getString(R.string.page_count));
                        }
                        // check existence of previewLink and set value to previewLink variable
                        if (volumeInfo.has(context.getString(R.string.preview_link))) {
                            previewLink = volumeInfo.getString(context.getString(R.string.preview_link));
                        }
                        // check existence of language and set value to language variable
                        if (volumeInfo.has(context.getString(R.string.query_language))) {
                            if (volumeInfo.getString(context.getString(R.string.query_language)).equals(context.getString(R.string.en)))
                                language = context.getString(R.string.english);
                            else
                                language = volumeInfo.getString(context.getString(R.string.query_language));
                        }
                        // check existence of imageLinks and set value to thumbnail variable
                        if (volumeInfo.has(context.getString(R.string.image_link)) &&
                                volumeInfo.getJSONObject(context.getString(R.string.image_link)).has(context.getString(R.string.small_thumbnail))) {

                            thumbnail = volumeInfo.getJSONObject(context.getString(R.string.image_link)).
                                    getString(context.getString(R.string.small_thumbnail));
                        }
                        // Extract out the accessInfo (which is an JSONObject)
                        JSONObject accessInfo = itemArray.getJSONObject(i).getJSONObject(context.getString(R.string.access_info));

                        // check existence of pdf and set value to downloadLink variable
                        if (accessInfo.has(context.getString(R.string.pdf)) &&
                                accessInfo.getJSONObject(context.getString(R.string.pdf)).has(context.getString(R.string.download_link))) {

                            downloadLink = accessInfo.getJSONObject(context.getString(R.string.pdf)).getString(context.getString(R.string.download_link));
                        }
                        // check existence of webReaderLink and set value to webReaderLink variable
                        if (accessInfo.has(context.getString(R.string.web_reader))) {
                            webReaderLink = accessInfo.getString(context.getString(R.string.web_reader));
                        }
                        // create a new {@link Book} object
                        Book currentBook = new Book(title, author, pageCount, language, webReaderLink, downloadLink, previewLink, thumbnail);
                        // add object ArrayList
                        bookList.add(currentBook);

                    }
                    // return ArrayList
                    return bookList;
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, context.getString(R.string.problem_parsing), e);
        }
        return null;
    }

}
