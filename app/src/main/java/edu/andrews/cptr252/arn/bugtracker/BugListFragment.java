package edu.andrews.cptr252.arn.bugtracker;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Fragment to display list of Bugs
 */
public class BugListFragment extends ListFragment {
    /** Tag for message log */
    private static final String TAG = "BugListFragment";

    /** Reference to list of bugs in display */
    private ArrayList<Bug> mBugs;

    public BugListFragment() {
        // Required empty public constructor
    }

    /**
     * Adapter responsible for getting the view for a bug
     */
    private class BugAdapter extends ArrayAdapter<Bug> {

        public BugAdapter(ArrayList<Bug> bugs) {
            super(getActivity(), 0, bugs);
        }

        /**
         * Return the view for a given bug in the list
         * @param position Position of view in the list
         * @param convertView Existing view that can still be used
         * @param parent The layout that contains the views generated
         * @return View containing bug information
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // If we weren't given a view, infalte one
            if (null == convertView) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_bug, null);
            }

            // Configure the view for this bug
            Bug bug = getItem(position);

            // set the title for the bug in the view
            TextView titleTextView =
                    convertView.findViewById(R.id.bug_list_item_titleTextView);
            titleTextView.setText(bug.getTitle());
            // set the date bug was logged in the view
            TextView dateTextView =
                    convertView.findViewById(R.id.bug_list_item_dateTextView);
            dateTextView.setText(bug.getDate().toString());

            // show whether bug is solved or not
            CheckBox solvedCheckBox =
                    convertView.findViewById(R.id.bug_ilist_item_solvedCheckBox);
            solvedCheckBox.setChecked(bug.isSolved());
            return convertView;
        }
    } // end BugAdapter

    /** Create a new bug, add it to the list, and launch the editor */
    private void addBug() {
        // create the new bug
        Bug bug = new Bug();
        // add to the list
        BugList.getInstance(getActivity()).addBug(bug);
        // create an intent to send to BugDetailsActivity
        // add the bug Id as an extra so BugDetailsFragment can edit it.
        Intent intent = new Intent(getActivity(), BugDetailsActivity.class);
        intent.putExtra(BugDetailsFragment.EXTRA_BUG_ID, bug.getId());
        // launch BugDetailsActivity which will launch BugDetailsFragment
        startActivityForResult(intent, 0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_bug_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_add_bug:
                // new bug icon clicked
                addBug();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        getActivity().setTitle(R.string.bug_list_label);
        mBugs = BugList.getInstance(getActivity()).getBugs();

        // use our custom bug adapter for generating views for each bug
        BugAdapter adapter = new BugAdapter(mBugs);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        // get ListView for the ListFragment
        ListView listView = v.findViewById(android.R.id.list);

        // allow user to select multiple bugs in the list
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // nothing to do
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.bug_list_item_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // nothing to do
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item_delete_bug:
                        BugAdapter adapter = (BugAdapter)getListAdapter();
                        BugList bugList = BugList.getInstance(getActivity());
                        // Check each bug in the list
                        // If it is selected, delete the bug
                        for (int i = adapter.getCount() - 1; i >= 0; i--) {
                            if (getListView().isItemChecked(i)) {
                                // Bug has been selected. Delete it.
                                bugList.deleteBug(adapter.getItem(i));
                            }
                        }
                        mode.finish();
                        // We have changed the bug list
                        // Tell the bug adapter to update the list of views
                        adapter.notifyDataSetChanged();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // nothing to do
            }
        });

        return v;
    }

    /**
     * Respond to user "click" on individual bugs in list.
     * @param l ListView containing the item that was clicked
     * @param v View that was clicked within the ListView
     * @param position Position of view in list
     * @param id row id of item that was clicked
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Bug bug = (Bug)(getListAdapter()).getItem(position);

        // start an instance of Bug Details Fragment
        Intent i = new Intent(getActivity(), BugDetailsActivity.class);
        // pass the id of the bug as an intent
        i.putExtra(BugDetailsFragment.EXTRA_BUG_ID, bug.getId());
        startActivity(i);
    }

    /**
     * Bug list fragment was paused (user was most likely editing a bug).
     * Notify the adapter that the data set (Bug list) may have changed.
     * THe adapter should update the views.
     */
    @Override
    public void onResume() {
        super.onResume(); // first execute the parent's onResume method
        ((BugAdapter)getListAdapter()).notifyDataSetChanged();
    }
}
