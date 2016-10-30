package com.eilifint.ravimal.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eilifint.ravimal.inventory.data.ProductContract.ProductEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Database helper that will provide us access to the database
     */
    ListView productListView;


    // Identifies a particular Loader being used in this component
    private static final int URL_LOADER = 0;

    ProductCursorAdapter mCursorAdapter;
    /**
     * product quantity
     */
    Integer quantity = 0;
    /**
     * Item Id
     */
    Integer id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the ListView which will be populated with the product data
        productListView = (ListView) findViewById(R.id.list);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        productListView.setItemsCanFocus(true);
        mCursorAdapter = new ProductCursorAdapter(this, null);
        //set {@link ProductCursorAdapter}
        productListView.setAdapter(mCursorAdapter);

        //listView onclick listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                //from the content uri  that represent the specific product that was clicked on
                // Utility methods useful for working with Uri objects that use the "content" (content://) scheme.

                Uri currentPrtUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                //set the uri on the data field on the intent
                intent.setData(currentPrtUri);
                //launch the {@link EditorActivity} to display the data for the current product
                startActivity(intent);

            }

        });
        //OnScrollListener for listView
        productListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                for (int i = firstVisibleItem; i <= firstVisibleItem + visibleItemCount; i++) {
                    View v = productListView.getChildAt(i);
                    //update quantity for sale button click
                    if (v != null) {
                        Button saleBtn = (Button) v.findViewById(R.id.sale_btn);
                        final TextView qntty = (TextView) v.findViewById(R.id.quantity_text_view);
                        final TextView idTxt = (TextView) v.findViewById(R.id.id_text_view);
                        saleBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                quantity = Integer.parseInt(qntty.getText().toString().trim()) - 1;
                                id = Integer.parseInt(idTxt.getText().toString().trim());
                                updateQuantity();

                            }
                        });

                    }
                }

            }
        });

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
                startActivity(intent);
            }
        });

          /*
         * Initializes the CursorLoader. The URL_LOADER value is eventually passed
         * to onCreateLoader().
         */
        getLoaderManager().initLoader(URL_LOADER, null, this);


    }

    /**
     * helper method to update quantity
     */
    public void updateQuantity() {
        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        Uri currentPrtUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

        if (quantity > 0) {
            int rowsAffected = getContentResolver().update(currentPrtUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(MainActivity.this, getString(R.string.update_fail),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(MainActivity.this, getString(R.string.row_updated),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * returns {@link CursorLoader} to display data
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE
        };


        // creating a Cursor for the data being displayed.
        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    /**
     * dataBase data is loaded
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //swap cursor
        mCursorAdapter.swapCursor(cursor);


    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
