package com.eilifint.ravimal.ebooksfree;

/**
 * Created by Ravimal on 9/2/2016.
 * {@link Book} represents a book object which user want to know.
 * It contains a book title , authors, no of pages, language,webReaderLink,downloadUrl,thumbnail and previewLink for that book.
 */


public class Book {
    //book title
    private String title;
    //author name
    private String authors;
    //no of pages
    private int pageCount;
    //language of the book
    private String language;
    // web reader url
    private String webReaderLink;
    //pdf download ul
    private String downloadLink;
    //book thumbnail
    private String thumbnail;
    //book preview link
    private String previewLink;

    /**
     * Create a new book object.
     *
     * @param title         is title for the book
     * @param authors       is authors for the book
     * @param pageCount     is the pageCount of the book
     * @param language      is language of the book
     * @param webReaderLink is webReaderLink for the book
     * @param downloadLink  is downloadLink for the book
     * @param previewLink   is previewLink for the book
     * @param thumbnail     is thumbnail for the book
     */
    public Book(String title, String authors, int pageCount,
                String language, String webReaderLink, String downloadLink, String previewLink, String thumbnail) {
        this.title = title;
        this.authors = authors;
        this.pageCount = pageCount;
        this.language = language;
        this.webReaderLink = webReaderLink;
        this.downloadLink = downloadLink;
        this.previewLink = previewLink;
        this.thumbnail = thumbnail;
    }

    /**
     * Return previewLink of the Item.
     */
    public String getPreviewLink() {
        return previewLink;
    }

    /**
     * Return title name.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Return author name.
     */
    public String getAuthors() {
        return authors;
    }

    /**
     * Return no of pages of the book.
     */
    public int getPageCount() {
        return pageCount;
    }

    /**
     * Return written language of the book.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Return webReader url.
     */
    public String getWebReaderLink() {
        return webReaderLink;
    }

    /**
     * Return download url.
     */
    public String getDownloadLink() {
        return downloadLink;
    }

    /**
     * Return thumbnail url.
     */
    public String getThumbnail() {
        return thumbnail;
    }
}
