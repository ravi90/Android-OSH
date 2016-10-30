package com.eilifint.ravimal.ebooksfree;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Ravimal on 9/2/2016.
 * {@link BookAdapter} is an {@link ArrayAdapter} that can provide the layout for each book
 * based on a data source, which is a list of {@link Book} objects.
 */
public class BookAdapter extends ArrayAdapter<Book> {

    /**
     * Create a new {@link BookAdapter} object.
     *
     * @param context is the current context (i.e. Activity) that the adapter is being created in.
     * @param objects is the list of {@link Book}s to be displayed.
     */
    public BookAdapter(Context context, ArrayList<Book> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.book_item, parent, false);

        }

        // Get the {@link Book} object located at this position in the list
        Book currentItem = getItem(position);
        // Create a new {@link Holder} object

        Holder bookHolder = new Holder();
        // Find the TextView in the book_item.xml layout for the titleText
        bookHolder.titleText = (TextView) listItemView.findViewById(R.id.title_text_view);
        //set  text on the titleText TextView
        bookHolder.titleText.setText(currentItem.getTitle());

        // Find the TextView in the book_item.xml layout for the authorText
        bookHolder.authorText = (TextView) listItemView.findViewById(R.id.authors_text_view);
        //set  text on the authorText TextView
        bookHolder.authorText.setText(currentItem.getAuthors());

        // Find the TextView in the book_item.xml layout for the languageText
        bookHolder.languageText = (TextView) listItemView.findViewById(R.id.language_text_view);
        //set  text on the languageText TextView
        bookHolder.languageText.setText(getContext().getString(R.string.language_code) +" "+ currentItem.getLanguage());

        // Find the TextView in the book_item.xml layout for the pageCountText
        bookHolder.pageCountText = (TextView) listItemView.findViewById(R.id.page_count_text_);
        // if currentItem {@link Book} no of pages not equal to 0 then set real value on the  the pageCountText
        if (currentItem.getPageCount() != 0) {
            bookHolder.pageCountText.setText(getContext().getString(R.string.no_pages) +" "+currentItem.getPageCount());
        } else // else set as unknown
            bookHolder.pageCountText.setText(getContext().getString(R.string.no_pages) +" "+ getContext().getString(R.string.unknown));

        // if currentItem {@link Book} thumbnail value is not equal to "Not found"
        //then {@link AsyncTask} to load thumbnails
        if (!currentItem.getThumbnail().equals(getContext().getString(R.string.not_found))) {
            new DownloadImageTask((ImageView) listItemView.findViewById(R.id.thumbnail_image_view))
                    .execute(currentItem.getThumbnail());
        }

        // Return the whole list item layout so that it can be shown the ListView.
        return listItemView;
    }

    /**
     * {@link Holder} is to hold textViews
     */
    public static class Holder {

        //Declaration of textViews
        private TextView titleText;
        private TextView authorText;
        private TextView pageCountText;
        private TextView languageText;


    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the book thumbnails in the response.
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        // Declaring of ImageView
        ImageView bmImage;

        /**
         * Create a new {@link DownloadImageTask} object.
         *
         * @param bmImage is the ImageView for the particular thubnail
         */
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            // store url string
            String urlDisplay = urls[0];
            //create Bitmap object
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urlDisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(getContext().getString(R.string.error), e.getMessage());
                e.printStackTrace();
            }
            //return Bitmap object
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            //set thumbnail
            bmImage.setImageBitmap(result);
        }
    }


}
