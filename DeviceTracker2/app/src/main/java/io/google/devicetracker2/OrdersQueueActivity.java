package io.google.devicetracker2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrdersQueueActivity extends AppCompatActivity {

    // LIST VIEW STUFF
    ArrayAdapter<String> mAdapter;
    String[] mOrdersID_Array;
    String[] mOrdersDetailsArray;
    private static final String TAG = OnWayOrdersActivity.class.getSimpleName();

    // DATABASE STUFF
    FirebaseDatabaseManagement mFbDatabaseManagementObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_queue);

        retrieveOrdersQueue();
    }


    public void retrieveOrdersQueue() {
        mFbDatabaseManagementObj = new FirebaseDatabaseManagement();
        DatabaseReference ordersQueueReference = mFbDatabaseManagementObj.getOrdersQueueReference();
        final ArrayList<String> ordersIDArrayList = new ArrayList<>();
        //final ArrayList<String> ordersDetailsArrayList = new ArrayList<>();

        ordersQueueReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot queuedOrder : dataSnapshot.getChildren()) {
                    ordersIDArrayList.add(queuedOrder.getKey());
                    //ordersDetailsArrayList.add(queuedOrder.getValue().toString());
                }

                mOrdersID_Array = ordersIDArrayList.toArray(new String[ordersIDArrayList.size()]);
                //mOrdersDetailsArray = ordersDetailsArrayList.toArray(new String[ordersDetailsArrayList.size()]);

//                initializeListView(); // cuando esta vacia da error. IMPORTANTE
                retrieveOrderDescriptions();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadChild:onCancelled", databaseError.toException());
            }
        });
    }

    public void retrieveOrderDescriptions() {
        mFbDatabaseManagementObj = new FirebaseDatabaseManagement();
        DatabaseReference ordersDescriptionRefernce = mFbDatabaseManagementObj.getOrderDescriptionsReference();
        final ArrayList<String> ordersDetailsArrayList = new ArrayList<>();

        ordersDescriptionRefernce.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(String orderID : mOrdersID_Array) {
                    String orderDescription = dataSnapshot.child(orderID).getValue().toString();
                    ordersDetailsArrayList.add(orderDescription);
                }

                mOrdersDetailsArray = ordersDetailsArrayList.toArray(new String[ordersDetailsArrayList.size()]);

                initializeListView();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadChild:onCancelled", databaseError.toException());
            }
        });

    }

    private void initializeListView() {
        // variar los layouts para encontrar la mejor opcion
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, mOrdersID_Array) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(mOrdersID_Array[position]);
                text2.setText(mOrdersDetailsArray[position]);
                 return view;
            }
        };
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(mAcceptOrderHandler);
    }

    private AdapterView.OnItemClickListener mAcceptOrderHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            String orderId = ((TextView) v.findViewById(android.R.id.text2)).getText().toString();
            Toast.makeText(OrdersQueueActivity.this, orderId, Toast.LENGTH_LONG).show();
        }
    };
}
