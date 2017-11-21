package dpyl.eddy.piedfly.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ncorti.slidetoact.SlideToActView;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.AppPermissions;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.view.adapter.UserAdapter;
import dpyl.eddy.piedfly.view.recyclerview.RecyclerItemTouchHelper;

public class MainActivity extends BaseActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, UserAdapter.ListItemClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PICK_CONTACT = 100;
    private static final int REQUEST_PICK_IMAGE = 101;

    //member variables
    private CircleImageView mUserImage;
    private ImageView mUserDirections;
    private SlideToActView mSlideForAlarm;
    private FloatingActionButton mFab;

    private Uri mUriContact;
    private Uri mUriUserImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Once the Activity is ready, swap the splash screen for the actual theme
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpViewsAndRecycler();

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick a contact
                startContactPick();
            }
        });

        //custom made swipe on recycler view (Used to delete objects)
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        // new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        //scroll on recycler view
        /*
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //hides or shows fab button according to the scrolling
                if (!recyclerView.canScrollVertically(1)) {
                    mFab.show();
                } else {
                    mFab.hide();
                }

            }
        });
        */

        //profile pic
        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick app user image
                startPickGalleryImage();
            }
        });

        //user directions
        mUserDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
            }
        });

        //slider
        mSlideForAlarm.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NotNull SlideToActView slideToActView) {
                //TODO:start emergency
                //I reset the slider by now
                mSlideForAlarm.resetSlider();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //TODO: I should only do this now, if it was called before for an alarm it should be on
        //reset slider
        mSlideForAlarm.resetSlider();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case AppPermissions.REQUEST_READ_CONTACTS:
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_READ_CONTACTS, grantResults)) {
                    startContactPick();
                } break;

            case AppPermissions.REQUEST_EXTERNAL_STORAGE:
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_EXTERNAL_STORAGE, grantResults)) {
                    startPickGalleryImage();
                } break;

            case AppPermissions.REQUEST_CALL_PHONE:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_CONTACT && resultCode == RESULT_OK) {
            mUriContact = data.getData();
        }

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            mUriUserImage = data.getData();
        }
    }


    //Setting up stuff

    private void setUpViewsAndRecycler() {

        //app bar and main layout
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //other views
        mUserImage = (CircleImageView) findViewById(R.id.userImage);
        mUserDirections = (ImageView) findViewById(R.id.userDirections);
        mSlideForAlarm = (SlideToActView) findViewById(R.id.slide_for_alarm);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.hide();

        //recycler
        /*
        mRecyclerView = (RecyclerView) findViewById(R.id.flock_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new BottomOffsetDecoration());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        */
    }

    //UserAdapter listener implementation

    @Override
    public void onListItemClick(int position, View view) {

        switch (view.getId()) {

            case R.id.contact_call:
                // startPhoneCall(DatabaseUtils.getUserPhoneNumber(this, (long) view.getTag()));
                break;

            case R.id.contact_image:
                Log.d(TAG, "Contact image clicked for item: " + view.getTag() + "!");
                break;
            case R.id.contact_directions:
                Toast.makeText(this, "Future: you'll find this contact on the Map !", Toast.LENGTH_SHORT).show();

            default:
                Log.d(TAG, "Item view position: " + position);
                break;

        }
    }

    //Implementing recycler onSwiped

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position) {
        // TODO: Delete User
    }

    //Intent action methods

    private void startPhoneCall(String phoneNumber) {

        if (AppPermissions.requestCallPermission(this)) {

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }

        }

    }

    private void startContactPick() {
        if (AppPermissions.requestReadContactsPermission(this)) {
            startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_PICK_CONTACT);
        }
    }

    private void startPickGalleryImage() {
        if (AppPermissions.requestExternalStoragePermission(this)) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        }

    }
}
