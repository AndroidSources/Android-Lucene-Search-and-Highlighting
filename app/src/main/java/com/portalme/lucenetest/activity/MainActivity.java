package com.portalme.lucenetest.activity;

/**
 * Copyright (c) 2015 Lukhnos Liu
 * <p>
 * Licensed under the MIT License.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.portalme.lucenetest.model.Document;
import com.portalme.lucenetest.R;
import com.portalme.lucenetest.lucene.SearchResult;
import com.portalme.lucenetest.lucene.Searcher;
import com.portalme.lucenetest.lucene.Study;

import org.apache.lucene.queryparser.classic.ParseException;
//import org.lukhnos.lucenestudy.Document;
//import org.lukhnos.lucenestudy.SearchResult;
//import org.lukhnos.lucenestudy.Searcher;
//import org.lukhnos.lucenestudy.Study;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String TAG = MainActivity.class.getSimpleName();
    static final String DATA_SOURCE = "acl-imdb-subset.json";
    static final String INDEX_DIR_NAME = "index";

    ArrayAdapter<Result> itemsAdapter;
    ListView listView;
    View statusOuterView;
    TextView statusText;
    private Searcher searcher;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView) findViewById(R.id.status_text);
        statusOuterView = findViewById(R.id.status_outer_view);

        itemsAdapter = new ResultAdapter(this, new ArrayList<Result>());
        listView = (ListView) findViewById(R.id.search_results_list);
        listView.setAdapter(itemsAdapter);

        setStatus(getString(R.string.welcome_search_text));



    }

    private void searchAndGetResults(String query) {
        try {
            Searcher searcher = new Searcher(getIndexRootDir().getAbsolutePath());
            SearchResult result = searcher.search(query, 20);
            List<Result> results = Result.fromSearchResult(result);
            searcher.close();

            itemsAdapter.clear();
            itemsAdapter.addAll(results);
            itemsAdapter.notifyDataSetChanged();

            if (results.size() == 0) {
                setStatus(getString(R.string.query_no_results_msg));
            } else {
                setStatus(null);
            }


        } catch (ParseException e) {
            setStatus(getString(R.string.query_no_results_msg));
            Toast.makeText(MainActivity.this, R.string.query_parsing_error_msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            setStatus(getString(R.string.query_no_results_msg));
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        rebuildIndexIfNotExists();
    }


    void setStatus(String text) {
        if (text == null) {
            statusOuterView.setVisibility(View.INVISIBLE);
            statusText.setText("");
            listView.setVisibility(View.VISIBLE);
        } else {
            statusOuterView.setVisibility(View.VISIBLE);
            statusText.setText(text);
            listView.setVisibility(View.INVISIBLE);
        }
    }

    static class Result {
        final SearchResult searchResult;
        final Document doc;
        final String title;
        final String info;
        final String source;
        final String review;

        Result(SearchResult searchResult, Document doc) {
            this.searchResult = searchResult;
            this.doc = doc;
            title = searchResult.getHighlightedTitle(doc) + String.format(" (%d)", doc.year);
            info = String.format("%s %d/10",
                    doc.positive ? "\uD83D\uDC4D" : "\uD83D\uDC4E", // thumb up/down emojis
                    doc.rating
            );
            source = String.format("<a href=\"%s\">source</a>", doc.source);
            review = searchResult.getHighlightedReview(doc);
        }

        static List<Result> fromSearchResult(SearchResult searchResult) {
            ArrayList<Result> results = new ArrayList<>();
            for (Document doc : searchResult.documents) {
                results.add(new Result(searchResult, doc));
            }
            return results;
        }
    }

    static class ViewHolder {
        TextView title;
        TextView info;
        TextView source;
        TextView review;
    }

    File getIndexRootDir() {
        return new File(getCacheDir(), INDEX_DIR_NAME);
    }

    void rebuildIndex() {
        final ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.rebuild_index_progress_title), getString(R.string.rebuild_index_progress_message), true);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    InputStream is = MainActivity.this.getAssets().open(DATA_SOURCE);
                    Study.importData(is, getIndexRootDir().getAbsolutePath(), true);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                dialog.dismiss();

                if (result) {
                    setStatus(getString(R.string.welcome_search_text));
                } else {
                    Toast.makeText(MainActivity.this, R.string.rebuild_index_failed_msg, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search); // get my MenuItem with placeholder submenu
        searchMenuItem.expandActionView();

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                //Do whatever you want
                onBackPressed();
                return true;
            }
        });
        // Retrieve the SearchView and plug it into SearchManager
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        searchEditText.setTextColor(getResources().getColor(R.color.white));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchAndGetResults(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                return true;
            }
        });


        return super.onCreateOptionsMenu(menu);

    }


    void rebuildIndexIfNotExists() {
        if (!getIndexRootDir().exists()) {
            rebuildIndex();
        }
    }

    class ResultAdapter extends ArrayAdapter<Result> {
        public ResultAdapter(Context context, List<Result> results) {
            super(context, R.layout.result_item, results);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Result result = getItem(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.result_item, parent, false);
                viewHolder.title = (TextView) convertView.findViewById(R.id.result_title);
                viewHolder.info = (TextView) convertView.findViewById(R.id.result_info);
                viewHolder.source = (TextView) convertView.findViewById(R.id.result_source);
                viewHolder.review = (TextView) convertView.findViewById(R.id.result_review);

                // Make source clickable.
                viewHolder.source.setMovementMethod(LinkMovementMethod.getInstance());

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.title.setText(Html.fromHtml(result.title));
            viewHolder.info.setText(result.info); // info is not in HTML
            viewHolder.source.setText(Html.fromHtml(result.source));
            viewHolder.review.setText(Html.fromHtml(result.review));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra(DetailActivity.EXTRA_TITLE, result.title);
                    intent.putExtra(DetailActivity.EXTRA_INFO, result.info);
                    intent.putExtra(DetailActivity.EXTRA_SOURCE, result.source);
                    intent.putExtra(DetailActivity.EXTRA_REVIEW, result.searchResult.getFullHighlightedReview(result.doc));
                    startActivity(intent);
                }
            });

            return convertView;
        }
    }
}
