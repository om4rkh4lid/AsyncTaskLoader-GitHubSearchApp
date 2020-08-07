package com.example.improvedgithubsearchapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
// TODO (2) - implement LoaderManager.LoaderCallbacks<String> on the activity that will start the Asynchronous task

/**
 * This interface contains callback methods that are called when loader events occur. The interface defines three callback methods:
 * 1 - onCreateLoader(int, Bundle) - called when the system needs a new loader to be created.
 *     Your code should create a Loader object and return it to the system.
 * 2 - onLoadFinished(Loader<D>, D) - called when a loader has finished loading data.
 *     Typically, your code should display the data to the user.
 * 3 - onLoaderReset(Loader<D>) - called when a previously created loader is being reset
 *     (when you call destroyLoader(int) or when the activity or fragment is destroyed , and thus making its data unavailable.
 *     Your code should remove any references it has to the loader's data.
 *     This interface is typically implemented by your activity or fragment and is registered when you call initLoader() or restartLoader().
 * NOTE: the generic type represents the type of data that is returned by the loading
 */

public class ResultsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private static final int SEARCH_LOADER = 69 ;
    //constants
    final String baseURI = "https://api.github.com/search/repositories";
    static final String SEARCH_RESULTS_KEY = "search_results"; // for onSaveInstanceState
    static final String QUERY_KEY = "query"; //for the Bundle to be passed to the loader


    TextView resultsTextView;
    ProgressBar progressBar;
    String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        //enable navigation to the parent by <- arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init views
        resultsTextView = findViewById(R.id.tv_result);
        progressBar = findViewById(R.id.progressBar);

        //intent that started this activity
        //the intent could be null if the activity is started from a savedInstance state
        Intent intent = getIntent();

        //get the search query from the intent
        String data = null;

        if(intent != null) {
            //check if there is a search query attached
            if (intent.hasExtra("data")) {
                data = (String) intent.getCharSequenceExtra("data");
            }
            if(data != null){
                searchQuery = data;
            }

        }

        //check if the savedInstanceState is not null (the activity has been destroyed and recreated), in this case just display cached data in the UI
        if(savedInstanceState != null && savedInstanceState.getString(SEARCH_RESULTS_KEY) != null){
            resultsTextView.setText(savedInstanceState.getString(SEARCH_RESULTS_KEY));
            progressBar.setVisibility(View.INVISIBLE);
        }else {
            //TODO (15) create a bundle that will contain the arguments passed to the loader
            Bundle queryBundle = new Bundle();
            //TODO (16) put the searchQuery into the bundle
            queryBundle.putString(QUERY_KEY, searchQuery);
            /*
             * Now that we've created our bundle that we will pass to our Loader, we need to decide
             * if we should restart the loader (if the loader already existed) or if we need to
             * initialize the loader (if the loader did NOT already exist).
             *
             *
             * hold on the support loader manager, (loaderManager) we can attempt to access our
             * searchLoader.
             */
            // TODO (17) store the support loader manager in the variable loaderManager.
            // All things related to the Loader go through through the LoaderManager. we need the LoaderManager to decide
            // if we should restart the loader (if the loader already existed) or if we need to
            // initialize the loader (if the loader did NOT already exist).
            LoaderManager loaderManager = getSupportLoaderManager();
            // TODO (18) attempt to access our searchLoader.
        /* To do this, we use LoaderManager's method, "getLoader", and pass in
            the ID we assigned in its creation. You can think of this process similar to finding a
            View by ID. We give the LoaderManager an ID and it returns a loader (if one exists). If
            one doesn't exist, we tell the LoaderManager to create one. If one does exist, we tell
            the LoaderManager to restart it.*/
            // NOTE: the callback parameter refers to the object that implements the callback methods
            Loader<String> searchLoader = loaderManager.getLoader(SEARCH_LOADER);
            if (searchLoader == null) {
                loaderManager.initLoader(SEARCH_LOADER, queryBundle, this);
            } else {
                loaderManager.restartLoader(SEARCH_LOADER, queryBundle, this);
            }

        }

    }

    // TODO (19) if there is a configuration change, cache the UI data
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO (20) Make sure super.onSaveInstanceState is called before doing anything else
        super.onSaveInstanceState(outState);

        String text = resultsTextView.getText().toString();

        // TODO (21) Put the contents of the TextView that contains our results into the outState Bundle
        outState.putString(SEARCH_RESULTS_KEY,
                TextUtils.isEmpty(text)? null : text);
    }







    // TODO (3) - implement the methods from LoaderCallbacks
    @NonNull
    @Override
    /** Instantiate and return a new Loader that will be doing the work with the given ID (arbitrary, but will be used to identify this particular loader).*/
    // TODO (4) - override onCreateLoad
    public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {

        // TODO (5) - create and return an AsyncTaskLoader that will do the network request
        /** AsyncTaskLoader is an abstract Loader that provides an AsyncTask to do the work.
         * The Loader API lets you load data from a content provider or other data source for display in an FragmentActivity or Fragment.
         * Loaders run on separate threads to prevent janky or unresponsive UI.
         * Loaders simplify thread management by providing callback methods when events occur.
         * Loaders persist and cache results across configuration changes to prevent duplicate queries.
         * Loaders can implement an observer to monitor for changes in the underlying data source.
         */
        return new AsyncTaskLoader<String>(this) {
            // TODO(6) create a variable to cache the result
            String cache = null;

            // TODO (7) override onStartLoading for any prep work
            //This is not called by clients directly, but as a result of a call to startLoading().
            @Override
            protected void onStartLoading() {
                /* If no arguments were passed, we don't have a query to perform. Simply return. */
                if (args == null) {
                    return;
                }

                // TODO (8) If result (cached) is not null, deliver that result. Otherwise, force a load
                /*
                 * If we already have cached results, just deliver them now. If we don't have any
                 * cached results, force a load.
                 */
                if (cache != null) {
                    //Sends the result of the load to the registered listener. Should only be called by subclasses. Must be called from the process's main thread.
                    deliverResult(cache);
                } else {
                    /*
                     * When we initially begin loading in the background, we want to display the
                     * loading indicator to the user
                     */
                    progressBar.setVisibility(View.VISIBLE);

                    /**Force an asynchronous load. Unlike startLoading() this will ignore a previously loaded data set and load a new one.
                     * This simply calls through to the implementation's onForceLoad().
                     * You generally should only call this when the loader is started -- that is, isStarted() returns true.*/
                    forceLoad();
                }
            }


            // TODO(9) - implement loadInBackground, this is where the code to be executed asynchronously should be
            @Nullable
            @Override
            public String loadInBackground() {
                // TODO(10) - make an connection to the url

                // build a URL from the query in the arguments bundle
                URL url = buildURL(args.getString(QUERY_KEY));

                //String for the result
                String result = null;

                // represents a communications link between the application and a URL and can be used both to read from and to write to the resource referenced by the URL.
                // https://developer.android.com/reference/java/net/HttpURLConnection
                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // TODO(11) - get results from the input stream returned by the connection
                // Returns an input stream that reads from this open connection.
                // A SocketTimeoutException can be thrown when reading from the returned
                // input stream if the read timeout expires before data is available for read.
                InputStream input = null;
                try {
                    input = httpURLConnection.getInputStream();
                    Scanner scanner = new Scanner(input);
                    scanner.useDelimiter("\\A");
                    if (scanner.hasNext()) {
                        result = scanner.next();
                    }
                }catch (Exception x){
                    x.printStackTrace();
                }finally {
                    //Disconnecting releases the resources held by a connection so they may be closed or reused.
                    httpURLConnection.disconnect();
                }

                return result;
            }

            // TODO(12) modify deliverResult to cache the result before calling super.deliverResult()
            @Override
            public void deliverResult(@Nullable String data) {
                cache = data;
                super.deliverResult(data);
            }

            private URL buildURL(String query) {
                // make a URL Object
                //method that builds a URL from a base URI and parameters, the result will be a URL formatted like:
                //"https://api.github.com/search/repositories?q=WHATEVER+language:assembly&sort=stars&order=desc"
                Uri builtUri = Uri.parse(baseURI).buildUpon()
                        .appendQueryParameter("q", query)
                        .appendQueryParameter("sort", "stars")
                        .appendQueryParameter("order", "desc")
                        .build();

                URL url = null;
                try {
                    url = new URL(builtUri.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                return url;
            }
        };
    }

    // TODO (13) - implement onLoadFinished
    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        /**
         * Called when a previously created loader has finished its load.
         * Note that normally an application is not allowed to commit fragment transactions while in this call, since it can happen after an activity's state is saved.
         * This function is guaranteed to be called prior to the release of the last data that was supplied for this Loader.
         * At this point you should remove all use of the old data (since it will be released soon),
         * but should not do your own release of the data since its Loader owns it and will take care of that.
         * The Loader will take care of management of its data so you don't have to.*/

        /* When we finish loading, we want to hide the loading indicator from the user. */
        progressBar.setVisibility(View.INVISIBLE);

        if (data == null) {
            resultsTextView.setText("Search Failed");
        } else {
            resultsTextView.setText(data);
        }

    }

    // TODO (14) - implement onLoaderReset
    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {
        /** Called when a previously created loader is being reset (resetLoader() is called), and thus making its data unavailable.
         *  The application should at this point remove any references it has to the Loader's data.*/
    }

}