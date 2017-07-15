package io.google.devicetracker2;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MakeOrderActivity extends FragmentActivity {

    private FirebaseDatabaseManagement mFbDatabaseManagmentObj;
    private ArrayList<Product> mProductList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_order);

        mFbDatabaseManagmentObj = new FirebaseDatabaseManagement();


        triggerProcesses();


    }

    public void triggerProcesses() {
        retrieveProductMap();
    }

    public void initialiazeViewPager() {
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager
        mDemoCollectionPagerAdapter =
                new DemoCollectionPagerAdapter(
                        getSupportFragmentManager(), mProductList);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
    }

    public void retrieveProductMap() {
        mFbDatabaseManagmentObj = new FirebaseDatabaseManagement();
        mProductList = new ArrayList<>();
        final DatabaseReference productOBJsRef = mFbDatabaseManagmentObj.getProductOBJsReference();

        productOBJsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot productSnapShot : dataSnapshot.getChildren()) {
                    Product product = productSnapShot.getValue(Product.class);

                    mProductList.add(product);
                }
                initialiazeViewPager();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void pushOrder(View view) {


        mFbDatabaseManagmentObj.writeToDatabase();

        mFbDatabaseManagmentObj.pushOrderToFirebaseDatabase("PROBANDO A LA 1 AM");

        /*Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);*/
    }

    //When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;


    // Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter
    public static class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {


        FirebaseDatabaseManagement mFbDatabaseManagementObj;
        Map<String, Product> productsMap;
        ArrayList<Product> mProductList;

        public DemoCollectionPagerAdapter(android.support.v4.app.FragmentManager fm, ArrayList<Product> productList) {
            super(fm);
            mProductList = productList;
            //retrieveProductMap();

            productsMap = new HashMap<>();
            Product alMacarone = new Product("sfsdfsdfs", "comida", "Pizza Al Macarone", "14*15 pulgadas", "15", "https://directorio.guatemala.com/custom/domain_1/image_files/sitemgr_photo_27672.jpg");
            Product pinulito = new Product("gdfgfdfgg", "comida", "Pollo Pinulito", "14*15 pulgadas", "15", "https://directorio.guatemala.com/custom/domain_1/image_files/sitemgr_photo_27672.jpg");
            Product sushiito = new Product("ergxfdfgr", "comida", "Sushiito", "14*15 pulgadas", "15", "https://directorio.guatemala.com/custom/domain_1/image_files/sitemgr_photo_27672.jpg");
            Product granjero = new Product("dsercderf", "comida", "Pollo Granjero", "14*15 pulgadas", "15", "https://directorio.guatemala.com/custom/domain_1/image_files/sitemgr_photo_27672.jpg");

            productsMap.put(alMacarone.productID, alMacarone);
            productsMap.put(pinulito.productID, pinulito);
            productsMap.put(sushiito.productID, sushiito);
            productsMap.put(granjero.productID, granjero);

        }


        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DemoObjectFragment();

            Bundle args = new Bundle();
            // Our object is just an integer :-P
            //args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1);

            Product product = mProductList.get(i);
            //Product product = productsMap.get("sfsdfsdfs");
            args.putString(DemoObjectFragment.ARG_OBJECT, product.productName);
            args.putString(DemoObjectFragment.ARG_PRODUCT_NAME, product.productName);
            args.putString(DemoObjectFragment.ARG_PRODUCT_CATEGORY, product.category);
            args.putString(DemoObjectFragment.ARG_PRODUCT_DESCRIPTION, product.description);
            args.putString(DemoObjectFragment.ARG_PRODUCT_PRICE, product.price);
            args.putString(DemoObjectFragment.ARG_PRODUCT_IMAGE_URL, product.imageURL);

            fragment.setArguments(args);

            return fragment;


        }

        @Override
        public int getCount() {
            return mProductList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }

        // Instances of this class are fragments representing a single
        // object in our collection
        public static class DemoObjectFragment extends Fragment {


            public static final String ARG_OBJECT = "object";
            public static final String ARG_PRODUCT_NAME = "productName";
            public static final String ARG_PRODUCT_CATEGORY = "productCategory";
            public static final String ARG_PRODUCT_DESCRIPTION = "productDescription";
            public static final String ARG_PRODUCT_PRICE = "productPrice";
            public static final String ARG_PRODUCT_IMAGE_URL = "productImageURL";

            @Override
            public View onCreateView(LayoutInflater inflater,
                                     ViewGroup container, Bundle savedInstanceState) {
                // The last two arguments ensure LayoutParams are inflated
                // properly
                View rootView = inflater.inflate(
                        R.layout.fragment_collection_object, container, false);
                Bundle args = getArguments();
                ((TextView) rootView.findViewById(R.id.txtProductName)).setText(
                        args.getString(ARG_PRODUCT_NAME));

                ((TextView) rootView.findViewById(R.id.txtProductCategory)).setText(
                        args.getString(ARG_PRODUCT_CATEGORY));
                ((TextView) rootView.findViewById(R.id.txtProductDescription)).setText(
                        args.getString(ARG_PRODUCT_DESCRIPTION));
                ((TextView) rootView.findViewById(R.id.txtProductPrice)).setText(
                        "Q" + args.getString(ARG_PRODUCT_PRICE));
                ImageView productImageView = ((ImageView) rootView.findViewById(R.id.imvProductImage));

                new ImageLoadTask(args.getString(ARG_PRODUCT_IMAGE_URL), productImageView).execute();

/*                try {

                    InputStream is = (InputStream) new URL("https://directorio.guatemala.com/custom/domain_1/image_files/sitemgr_photo_27672.jpg").getContent();
                    Drawable buttonBg = Drawable.createFromStream(is, null);
                    ((Button) rootView.findViewById(R.id.btnProductImage)).setText("probando");
                    ((Button) rootView.findViewById(R.id.btnProductImage)).setBackground(buttonBg).;

                } catch (Exception e) {
                    ((Button) rootView.findViewById(R.id.btnProductImage)).setText("probando");

                }*/


                return rootView;


            }
        }
    }
}







