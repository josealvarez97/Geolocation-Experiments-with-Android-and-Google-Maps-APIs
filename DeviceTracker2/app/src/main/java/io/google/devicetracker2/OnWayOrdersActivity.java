package io.google.devicetracker2;





import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

//import static android.icu.text.RelativeDateTimeFormatter.Direction.THIS;

public class OnWayOrdersActivity  extends AppCompatActivity
        /*implements LoaderManager.LoaderCallbacks<Cursor>*/{

    // This is the Adapter being used to display the list's data
    ArrayAdapter<String> mAdapter;
    String[] mOrdersID_Array;
    String[] mOrdersDescription_Array;
    private static final String TAG = FirebaseDatabaseManagement.class.getSimpleName();

    // These are the Contacts rows that we will retrieve
    //static final String[] PROJECTION = new String[] {"1110616", "Jose" };


    FirebaseDatabaseManagement mFbDatabaseManagementObj;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_way_orders);



        String[] kittens = new String[] {"Fluffy", "Muffy", "Tuffy"};





        //List<String> ordersList = mFbDatabaseManagementObj.getListFromAPath(userOnWayOrdersDatabaseReference);
        //ArrayList<String> ordersArrayList = mFbDatabaseManagementObj
                //.getChildArrayListFromReference(userOnWayOrdersDatabaseReference);
        retrieveUserOnWayOrdersList();

        //mFbDatabaseManagementObj.getChildArrayListFromReference(userOnWayOrdersDatabaseReference);
        //String[] ordersArray = ordersArrayList.toArray(new String[ordersArrayList.size()]); // http://viralpatel.net/blogs/convert-arraylist-to-arrays-in-java/

        //int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()


        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        //getLoaderManager().initLoader(0, null, this);




    }


    public void retrieveUserOnWayOrdersList() {

        mFbDatabaseManagementObj = new FirebaseDatabaseManagement();
        DatabaseReference userOnWayOrdersDatabaseReference = mFbDatabaseManagementObj.getUserOrdersReference();
        final ArrayList<String> ordersArrayList = new ArrayList<>();
        final ArrayList<String> descriptionsArrayList = new ArrayList<>();



        userOnWayOrdersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childrenSnapshot: dataSnapshot.getChildren()) {
                    // Handle children
                    ordersArrayList.add(childrenSnapshot.getKey());
                    descriptionsArrayList.add(childrenSnapshot.getValue().toString());

                }

                // Next step...
                //ordersArrayList.toArray(new String[ordersArrayList.size()]);
                mOrdersID_Array = ordersArrayList.toArray(new String[ordersArrayList.size()]);
                mOrdersDescription_Array = descriptionsArrayList.toArray(new String[descriptionsArrayList.size()]);
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


    /*public void retrieveUserOrdersDescriptionArray() {

        mFbDatabaseManagementObj = new FirebaseDatabaseManagement();
        DatabaseReference userOnWayOrdersDatabaseReference = mFbDatabaseManagementObj.getUserOrdersReference();
        final ArrayList<String> ordersArrayList = new ArrayList<>();


        userOnWayOrdersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childrenSnapshot: dataSnapshot.getChildren()) {
                    // Handle children
                    ordersArrayList.add(childrenSnapshot.getKey());
                    //descriptionsArrayList.add(childrenSnapshot.getValue().toString());

                }

                // Next step...
                //ordersArrayList.toArray(new String[ordersArrayList.size()]);
                mOrdersID_Array = ordersArrayList.toArray(new String[ordersArrayList.size()]);
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
    }*/

    private void initializeListView() {
/*        mAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_2,
                android.R.id.text2,
                mOrdersID_Array*//*, toViews, 0*//*);*/
        mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, mOrdersID_Array) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text2.setText(mOrdersID_Array[position]);
                text1.setText(mOrdersDescription_Array[position]);
                return view;
            }
        };
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(mMessageClickedHandler);





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
// Create a message handling object as an anonymous class.
private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
    // https://stackoverflow.com/questions/11256563/how-to-set-both-lines-of-a-listview-using-simple-list-item-2
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        // Do something in response to the click
        String orderId = ((TextView) v.findViewById(android.R.id.text2)).getText().toString();
        Toast.makeText(OnWayOrdersActivity.this, orderId, Toast.LENGTH_SHORT).show();
    }
};

}
