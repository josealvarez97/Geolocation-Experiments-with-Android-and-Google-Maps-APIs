package io.google.devicetracker2;





import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//import static android.icu.text.RelativeDateTimeFormatter.Direction.THIS;

public class OnWayOrdersActivity  extends AppCompatActivity
        /*implements LoaderManager.LoaderCallbacks<Cursor>*/{

    // This is the Adapter being used to display the list's data
    ArrayAdapter<String> mAdapter;
    String[] mArray;
    private static final String TAG = FirebaseDatabaseManagement.class.getSimpleName();

    // These are the Contacts rows that we will retrieve
    //static final String[] PROJECTION = new String[] {"1110616", "Jose" };


    FirebaseDatabaseManagement mFbDatabaseManagementObj;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_way_orders);



        String[] kittens = new String[] {"Fluffy", "Muffy", "Tuffy"};


        mFbDatabaseManagementObj = new FirebaseDatabaseManagement();
        DatabaseReference userOnWayOrdersDatabaseReference = mFbDatabaseManagementObj.mDatabaseReference
                .child("users")
                .child(mFbDatabaseManagementObj.getCurrentFbUserId())
                .child("orders");


        //List<String> ordersList = mFbDatabaseManagementObj.getListFromAPath(userOnWayOrdersDatabaseReference);
        //ArrayList<String> ordersArrayList = mFbDatabaseManagementObj
                //.getChildArrayListFromReference(userOnWayOrdersDatabaseReference);
        retrieveUserOnWayOrdersList(userOnWayOrdersDatabaseReference);
        //mFbDatabaseManagementObj.getChildArrayListFromReference(userOnWayOrdersDatabaseReference);
        //String[] ordersArray = ordersArrayList.toArray(new String[ordersArrayList.size()]); // http://viralpatel.net/blogs/convert-arraylist-to-arrays-in-java/

        //int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()


        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        //getLoaderManager().initLoader(0, null, this);




    }


    public void retrieveUserOnWayOrdersList(DatabaseReference aReference) {
        final ArrayList<String> ordersArrayList = new ArrayList<>();
        aReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childrenSnapshot: dataSnapshot.getChildren()) {
                    // Handle children
                    ordersArrayList.add(childrenSnapshot.getKey());
                }

                // Next step...
                //ordersArrayList.toArray(new String[ordersArrayList.size()]);
                mArray = ordersArrayList.toArray(new String[ordersArrayList.size()]);
                initializeListView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting childSnapshot failed, log a message
                Log.w(TAG, "loadChild:onCancelled", databaseError.toException());
                // ...
            }
        });

        //return mArrayList;
    }

    private void initializeListView() {
        mAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                mArray/*, toViews, 0*/);

        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(mAdapter);
    }
/*

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this);
    }

    // Called when a previously crated loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no longer using it.
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Do something when a list item is clicked
    }

*/

}
