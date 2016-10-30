package com.eilifint.ravimal.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.eilifint.ravimal.inventory.data.ProductContract.ProductEntry;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;
    // Identifies a particular Loader being used in this component
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * EditText field to enter the product supplier
     */
    private EditText mSupplierEditText;

    /**
     * EditText field to enter the product quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the product description
     */
    private EditText mDescriptionEditText;

    /**
     * EditText field to enter the product contactNo
     */
    private EditText mContactEditText;

    /**
     * ImageView  to load product image
     */
    private ImageView mImageView;
    /**
     * button to update shipment
     */
    private Button mShipmentBtn;
    /**
     * button to update sales
     */
    private Button mSaleBtn;

    /**
     * contact button
     */
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Find all relevant views that we will need to read user input from
        mSupplierEditText = (EditText) findViewById(R.id.supplier_detail_edit);
        mQuantityEditText = (EditText) findViewById(R.id.quantity_detail_edit);
        mDescriptionEditText = (EditText) findViewById(R.id.description_detail_edit);
        mContactEditText = (EditText) findViewById(R.id.contact_detail_edit);
        mShipmentBtn = (Button) findViewById(R.id.shipment_btn);
        mSaleBtn = (Button) findViewById(R.id.sale_btn);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        mImageView = (ImageView) findViewById(R.id.image_detail_view);

        //getting intent
        Intent intent = getIntent();
        //getting item uri
        mCurrentProductUri = intent.getData();

        //button click listeners
        mShipmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setQuantity(false);
            }
        });
        mSaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setQuantity(true);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mContactEditText.getText().toString().trim()));
                startActivity(intent);
            }
        });
        //set action bar title
        setTitle(getString(R.string.edit_product));
        //initializing the loader
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

    }

    /**
     * Action bar item selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database

                updateProduct();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to update product item
     */
    private void updateProduct() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String supplierString = mSupplierEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String contactString = mContactEditText.getText().toString().trim();
        //if all the values are null then exit
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(supplierString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(descriptionString) && TextUtils.isEmpty(contactString)) {
            finish();
            return;
        }
        //all the textFields must be not empty to update a item
        if (!TextUtils.isEmpty(supplierString) && !TextUtils.isEmpty(quantityString) &&
                !TextUtils.isEmpty(descriptionString) && !TextUtils.isEmpty(contactString)) {


            // Create a ContentValues object where column names are the keys,
            // and product attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
            // If the quantity is not provided by the user, Use 0 by default.
            int quantity = 0;

            quantity = Integer.parseInt(quantityString);

            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
            values.put(ProductEntry.COLUMN_PRODUCT_DESCRIPTION, descriptionString);
            values.put(ProductEntry.COLUMN_PRODUCT_CONTACT_NO, Integer.parseInt(contactString));


            if (mCurrentProductUri != null) {
                //get updated row count
                int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(DetailActivity.this, getString(R.string.update_fail),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(DetailActivity.this, getString(R.string.updated),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(DetailActivity.this, getString(R.string.dont_miss),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {


        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_fail),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.deleted),
                        Toast.LENGTH_SHORT).show();
            }

            // Close the activity
            finish();
        }
    }

    /**
     * Helper method to show {@link AlertDialog} when trying to deete a product
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Inflating menu from R.menu.menu_editor.xml to actionBar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * returns {@link CursorLoader} to display data
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies which columns from the database

        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_CONTACT_NO

        };


        // creating a Cursor for the data being displayed.
        return new CursorLoader(this,
                mCurrentProductUri,
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
        try {


            if (cursor.moveToFirst()) {

                // Find the columns of product attributes that we're interested in
                int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
                int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
                int descriptionColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DESCRIPTION);
                int contactColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CONTACT_NO);
                int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

                // Extract out the value from the Cursor for the given column index
                String supplier = cursor.getString(supplierColumnIndex);
                String quantity = cursor.getString(quantityColumnIndex);
                String description = cursor.getString(descriptionColumnIndex);
                String contact = cursor.getString(contactColumnIndex);
                byte[] image = cursor.getBlob(imageColumnIndex);

                Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);

                //set image
                mImageView.setImageBitmap(bmp);
                //set values
                mSupplierEditText.setText(supplier);
                mQuantityEditText.setText(quantity);
                mDescriptionEditText.setText(description);
                mContactEditText.setText(contact);

            }
        } catch (IllegalStateException e) {
            Toast.makeText(DetailActivity.this, getString(R.string.large_data), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to update quentity
     */
    public void setQuantity(final boolean isSale) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.set_qtty));
        builder.setCancelable(false);
        // Set up the input
        final EditText input = new EditText(this);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!isSale) {
                    String value = mQuantityEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(value)) {
                        int quantity = Integer.parseInt(value);
                        int addQuantity = Integer.parseInt(input.getText().toString().trim());
                        quantity = quantity + addQuantity;
                        mQuantityEditText.setText(Integer.toString(quantity));

                    }
                } else {
                    String value = mQuantityEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(value)) {
                        int quantity = Integer.parseInt(value);
                        int addQuantity = Integer.parseInt(input.getText().toString().trim());
                        quantity = quantity - addQuantity;
                        if (quantity > 0)
                            mQuantityEditText.setText(Integer.toString(quantity));
                        else {
                            Toast.makeText(DetailActivity.this, getString(R.string.invalid_qtty)
                                    ,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }

                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        //show dialog box
        builder.show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
