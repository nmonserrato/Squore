package com.doubleyellow.util;

import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import com.doubleyellow.android.view.ViewUtil;
import com.doubleyellow.prefs.RWValues;
import com.doubleyellow.scoreboard.view.ExpandableListUtil;

/**
 * Class that helps in easily setting up searching capabilities for an EL (ExpandableListview) or ListView
 *
 * Used by FeedFeedSelector and ExpandableMatchSelector for now.
 */
public class SearchEL implements SearchView.OnQueryTextListener, SearchView.OnCloseListener
{
    private Context            context            = null;
    private SimpleELAdapter    simpleELAdapter    = null;
    private SearchView         searchView         = null;
    private ListView           listView           = null;

    private String sPrefKey = null;
    public SearchEL(Context context, SimpleELAdapter simpleELAdapter, SearchView searchView, ListView listView, boolean bStoreLastSearch) {
        this.context            = context;
        this.simpleELAdapter    = simpleELAdapter;
        this.searchView         = searchView;
        this.listView           = listView;
        if ( bStoreLastSearch ) {
            this.sPrefKey = simpleELAdapter.getClass().getName();
        }
        init();
    }

    // ------------------------------------------
    // Filtering
    // ------------------------------------------

    private void init() {
        searchView.setOnCloseListener    (this);
        searchView.setOnQueryTextListener(this);

        repeatLastSearch();

        showSearchView();
    }

    private void repeatLastSearch() {
        if ( sPrefKey != null ) {
            CharSequence sLastSearch = RWValues.getString(sPrefKey, "", context);
            if ( StringUtil.isEmpty(sLastSearch) ) {
                sLastSearch = searchView.getQuery();
            }
            if ( StringUtil.isNotEmpty(sLastSearch) ) {
                searchView.setQuery(sLastSearch, true);
            }
        } else {
            // TODO: test
            simpleELAdapter.filterData("");
        }
    }

    private void showSearchView() {
        searchView.setVisibility(View.VISIBLE);
        searchView.requestFocus();
    }

    public void toggleSearchViewVisibility() {
        if ( simpleELAdapter == null ) { return; }

        final int iNewVisibility = ViewUtil.toggleViewVisibility(searchView, View.GONE);
        if ( iNewVisibility != View.VISIBLE ) {
            simpleELAdapter.filterData("");
        } else {
            repeatLastSearch();
        }
    }

    // SearchView.OnCloseListener

    @Override public boolean onClose() {
        if ( simpleELAdapter == null ) { return false; }

        simpleELAdapter.filterData("");
        if ( sPrefKey != null ) {
            RWValues.setString(sPrefKey, context, "");
        }
        searchView.setVisibility(View.GONE);
        return false;
    }

    // SearchView.OnQueryTextListener

    /** Triggered if magnifying glass on keyboard is pressed */
    @Override public boolean onQueryTextSubmit(String query) {
        if ( simpleELAdapter == null ) { return false; }

        simpleELAdapter.filterData(query);
        ExpandableListUtil.expandAll(listView);

        if ( sPrefKey != null ) {
            RWValues.setString(sPrefKey, context, query);
        }
        return false;
    }

    /** Also triggered if the 'x' is ticked, query string is empty in that case */
    @Override public boolean onQueryTextChange(String query) {
        if ( simpleELAdapter == null ) { return false; }

        simpleELAdapter.filterData(query);
        ExpandableListUtil.expandAll(listView);

        if ( sPrefKey != null && StringUtil.isNotEmpty(query) ) {
            RWValues.setString(sPrefKey, context, query);
        }
        return false;
    }
}
