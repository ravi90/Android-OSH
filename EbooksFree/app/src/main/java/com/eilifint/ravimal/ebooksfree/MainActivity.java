package com.eilifint.ravimal.ebooksfree;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Implements AsyncResponse interface
 */
public class MainActivity extends AppCompatActivity implements BookAsyncTask.AsyncResponse {

    /**
     * Declaration of EditText ,String,ArrayList variables
     */
    private EditText bookType;
    private String argType = null;
    private ArrayList<Book> books;
    /**
     * Tag for the log savedInstanceState
     */
    public String TAG_BOOK_SEARCH = null;
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String ARG_START = this.getString(R.string.arg_start);
        TAG_BOOK_SEARCH = this.getString(R.string.tag_book);

        //find the button in the activity_main.xml layout for the search
        Button search = (Button) findViewById(R.id.search_button);
        //find the EditText in the activity_main.xml layout for the bookType
        bookType = (EditText) findViewById(R.id.book_type_edit_text);

        //initialising argType;
        argType = ARG_START;

        //execute {@link AsyncTask} object to get book details when app loads first time
        new BookAsyncTask(MainActivity.this).execute(getFinalUrl(ARG_START));

        if (savedInstanceState != null) {
            // restore the saved state
            this.argType = savedInstanceState.getString(TAG_BOOK_SEARCH, "");

            bookType.setText(String.valueOf(this.argType));

            //execute {@link AsyncTask} when state changed
            new BookAsyncTask(MainActivity.this).execute(getFinalUrl(argType));

        }


        if (search != null) {
            // search button on click listener
            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isNetworkConnected()) {
                        // get string value from EditText as assign it to argType variable
                        argType = bookType.getText().toString().trim().replace(" ", getBaseContext().getString(R.string.twenty));

                        //execute {@link AsyncTask} object to get book details
                        final BookAsyncTask task = new BookAsyncTask(MainActivity.this);
                        task.execute(getFinalUrl(argType));

                        // hide soft keyboard
                        try {
                            InputMethodManager inputMethod = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            inputMethod.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, getBaseContext().getString(R.string.exception), e);
                        }
                    }
                }
            });
        }


    }

    /**
     * save state
     *
     * @param outState Bundle object to save state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(TAG_BOOK_SEARCH, this.argType);
        super.onSaveInstanceState(outState);
    }

    /**
     * This will be invoked when an item in the ListView is long pressed and
     * define the menu items, usually by inflating a menu resource
     *
     * @param menu     floating context menu
     * @param v        view object
     * @param menuInfo information of the menu
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    /**
     * This will be invoked when a menu item is selected
     *
     * @param item single menu item
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // switch-case to identify menu items
        switch (item.getItemId()) {

            case R.id.menu_preview:
                if (!books.get(info.position).getPreviewLink().equals(this.getString(R.string.not_found))) {
                    openUrl(books.get(info.position).getPreviewLink());
                } else {
                    //if the preview link is not available toast message is shown
                    Toast.makeText(MainActivity.this, this.getString(R.string.message_one), Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.menu_download:
                if (!books.get(info.position).getDownloadLink().equals(this.getString(R.string.not_found))) {
                    openUrl(books.get(info.position).getDownloadLink());
                } else {
                    //if the download link is not available toast message is shown
                    Toast.makeText(MainActivity.this, this.getString(R.string.message_two), Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }


    }

    /**
     * start implicit intent
     */
    public void openUrl(String url) {
        Uri webPage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * return string value of the api endpoint
     *
     * @param argType argument value for the query
     */
    public String getFinalUrl(String argType) {
        String baseUrl = this.getString(R.string.base_url);
        StringBuffer query = new StringBuffer(baseUrl);
        String equal = this.getString(R.string.equal);
        String paramType = this.getString(R.string.query_para);
        String separator = this.getString(R.string.separate);
        String paramMaxResult = this.getString(R.string.max_result);
        final int MAX_RESULT = 20;

        //return final string
        return query.append(paramType).append(equal).append(argType).
                append(separator).append(paramMaxResult).append(equal).append(MAX_RESULT).toString();
    }

    /**
     * check internet connection
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            InternetAlert();
            return false;
        } else
            return true;
    }

    /**
     * internet connection alert
     */
    public void InternetAlert() {

        //create builder for alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set title
        builder.setTitle(this.getString(R.string.alert_title));
        builder.setCancelable(false);
        builder.setPositiveButton(this.getString(R.string.positive_btn), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton(this.getString(R.string.negative_btn), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
                finish();
            }
        });
        // create AlertDialog
        AlertDialog alert = builder.create();
        //show
        alert.show();
    }

    /**
     * give {@link Book} ArrayList as a result of {@link BookAsyncTask}
     *
     * @param output list of books
     */
    @Override
    public void processFinish(ArrayList<Book> output) {
        // assign value to global variable
        books = output;
        // Find the CoordinatorLayout in the activity_main.xml layout
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);


        // Find a reference to the {@link ListView} in the layout
        ListView bookListView = (ListView) findViewById(R.id.list);
        bookListView.setAdapter(null);
        if (output != null) {


        /*Create an {@link ItemAdapter}, whose data source is a list of {@link Book}s. The
         adapter knows how to create list items for each book in the list.*/
            BookAdapter adapter = new BookAdapter(this, output);

            // Set the adapter on the {@link ListView}
            // so the list can be populated in the user interface
            bookListView.setAdapter(adapter);
            //registering {@link ListView} to context menu
            registerForContextMenu(bookListView);
            //on click listener
            bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (!books.get(position).getWebReaderLink().equals(getBaseContext().getString(R.string.not_found))) {
                        openUrl(books.get(position).getWebReaderLink());
                    } else {
                        // show toast message if the web reader version is nt available
                        Toast.makeText(MainActivity.this, getBaseContext().getString(R.string.message_three), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            // show a snack bar message after loading book information
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, this.getString(R.string.mesage_four) + " " + argType, Snackbar.LENGTH_LONG);

            snackbar.show();
        } else {
            // show a snack bar message if there are no books
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, this.getString(R.string.message_five), Snackbar.LENGTH_LONG);

            snackbar.show();

        }

    }
}
