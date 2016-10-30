package com.eilifint.ravimal.inventory;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.eilifint.ravimal.inventory.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class AddProductActivity extends AppCompatActivity {
    /**
     * EditText field to enter the supplier name
     */
    private EditText mSupplier;

    /**
     * EditText field to enter the name
     */
    private EditText mName;

    /**
     * EditText field to enter the quantity
     */
    private EditText mQuantity;
    /**
     * EditText field to enter the item price
     */
    private EditText mItemPrice;

    /**
     * EditText field to enter the description
     */
    private EditText mDescription;

    /**
     * EditText field to enter the contact no
     */
    private EditText mContact;

    /**
     * Image ID
     */
    private static final int SELECTED_IMAGE = 1;
    /**
     * Image views for loading image
     */
    ImageView mImage, mBackgroundImg;

     final static int  IMAGE_SIZE = 350;

    /**
     * onCreate creates new instance of {@link AddProductActivity}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Find all relevant views that we will need to read user input from
        mSupplier = (EditText) findViewById(R.id.supplier_edit_text);
        mName = (EditText) findViewById(R.id.name_edit_text);
        mQuantity = (EditText) findViewById(R.id.quantity_edit_text);
        mItemPrice = (EditText) findViewById(R.id.price_edit_text);
        mDescription = (EditText) findViewById(R.id.description_edit_text);
        mContact = (EditText) findViewById(R.id.contact_edit_text);
        mImage = (ImageView) findViewById(R.id.product_image_view);
        mBackgroundImg = (ImageView) findViewById(R.id.image_view);

        //image on click listener to store image
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //start mediaStore to select image
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECTED_IMAGE);
                // //set background imageView to invisible if image is selected
                mBackgroundImg.setVisibility(View.INVISIBLE);


            }
        });
    }

    /**
     * onActivityResult is used to get request code,uri a for the image
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //data uri if requestCode and resultCode are equal to given ones
        if (requestCode == SELECTED_IMAGE && resultCode == RESULT_OK) {
            Uri targetUri = data.getData();

            //set image
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                mImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            }
        } else {
            //set background imageView to visible if no image is selected
            mBackgroundImg.setVisibility(View.VISIBLE);
        }

    }

    /**
     * actionbar item selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                insertProduct();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem menuItem = menu.findItem(R.id.action_delete);
        menuItem.setVisible(false);

        return true;
    }

    /**
     * Helper method to insert  product data into the database.
     */
    private void insertProduct() {

        byte[] image = null;
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String supplierString = mSupplier.getText().toString().trim();
        String nameString = mName.getText().toString().trim();
        String quantityString = mQuantity.getText().toString().trim();
        String priceString = mItemPrice.getText().toString().trim();
        String descriptionString = mDescription.getText().toString().trim();
        String contactString = mContact.getText().toString().trim();

        //if image is selected convert it to byte array
        if (mImage.getDrawable() != null) {
            Bitmap bitmap = ((BitmapDrawable) mImage.getDrawable()).getBitmap();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            image = bos.toByteArray();

        }


        // Create a ContentValues object where column names are the keys,
        // and Toto's product attributes are the values.
        if (!TextUtils.isEmpty(supplierString) &&
                !TextUtils.isEmpty(nameString) &&
                !TextUtils.isEmpty(quantityString) &&
                !TextUtils.isEmpty(priceString) &&
                !TextUtils.isEmpty(contactString) &&
                image != null) {
            //check image size is acceptable to load before store in to database

            if ((image.length) / 1024 < IMAGE_SIZE) {
                //convert data
                double price = Double.parseDouble(priceString);
                int quantity = Integer.parseInt(quantityString);
                int contactNo = Integer.parseInt(contactString);
                //content value to store key value pair
                ContentValues values = new ContentValues();
                values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
                values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
                values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                values.put(ProductEntry.COLUMN_PRODUCT_DESCRIPTION, descriptionString);
                values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, image);
                values.put(ProductEntry.COLUMN_PRODUCT_CONTACT_NO, contactNo);


                // Insert a new row for Toto in the database, returning the ID of that new row.
                Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
                // Show a toast message depending on whether or not the insertion was successful
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                            Toast.LENGTH_SHORT).show();
                    // Exit activity
                    finish();
                }
            } else
                Toast.makeText(this, getString(R.string.image_too_large), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.dont_miss),
                    Toast.LENGTH_SHORT).show();
        }

    }
}
