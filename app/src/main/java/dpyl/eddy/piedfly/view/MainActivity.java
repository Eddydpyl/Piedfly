package dpyl.eddy.piedfly.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ncorti.slidetoact.SlideToActView;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.AppPermissions;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.model.User;
import dpyl.eddy.piedfly.view.adapter.UserAdapter;
import dpyl.eddy.piedfly.view.recyclerview.BottomOffsetDecoration;
import dpyl.eddy.piedfly.view.recyclerview.RecyclerItemTouchHelper;

public class MainActivity extends BaseActivity implements UserAdapter.ListItemClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PICK_CONTACT = 100;
    private static final int REQUEST_PICK_IMAGE = 101;

    private UserAdapter mUserAdapter;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Once the Activity is ready, swap the splash screen for the actual theme
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final CircleImageView userImage = (CircleImageView) findViewById(R.id.userImage);
        final ImageView userDirections = (ImageView) findViewById(R.id.userDirections);
        final SlideToActView slideForAlarm = (SlideToActView) findViewById(R.id.slide_for_alarm);
        final FloatingActionButton faButton = (FloatingActionButton) findViewById(R.id.fab); faButton.hide();

        faButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPickContact();
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPickGalleryImage();
            }
        });

        userDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), MapsActivity.class));
            }
        });

        slideForAlarm.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NotNull SlideToActView slideToActView) {
                // TODO: Start an Emergency for the user
                slideToActView.resetSlider(); // We reset the slider by now
            }
        });

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.flock_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new BottomOffsetDecoration());
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        // Custom made swipe on recycler view (Used to delete objects)
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, new RecyclerItemTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                // TODO: Prompt delete user dialogue
            }
        }); new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        // Floating button only appears at the end of the list
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) faButton.show();
                else faButton.hide();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        } return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachRecyclerViewAdapter();
        if (mUserAdapter != null) mUserAdapter.startListening();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppPermissions.REQUEST_READ_CONTACTS:
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_READ_CONTACTS, grantResults)) {
                    startPickContact();
                } break;
            case AppPermissions.REQUEST_EXTERNAL_STORAGE:
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_EXTERNAL_STORAGE, grantResults)) {
                    startPickGalleryImage();
                } break;
            case AppPermissions.REQUEST_CALL_PHONE:
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_CALL_PHONE, grantResults)) {
                    if (phoneNumber != null) startPhoneCall(phoneNumber);
                } break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_CONTACT && resultCode == RESULT_OK) {
            Uri uriContact = data.getData();
            // TODO: Search for a user with the retrieved phone number and, if it exists, send a Request
        }
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            Uri uriUserImage = data.getData();
            // TODO: Upload the image to Firebase and update the user's photoUrl
        }
    }

    @Override
    public void onListItemClick(int position, View view, String uid) {
        switch (view.getId()) {
            case R.id.contact_call:
                startPhoneCall((String)view.getTag());
                break;
            case R.id.contact_image:
                // TODO: ???
                break;
            case R.id.contact_directions:
                // TODO: Open MapsActivity and point to the user's lastKnownLocation (View.getTag())
            default:
                Log.d(TAG, "Item view position: " + position);
                break;
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        super.onAuthStateChanged(firebaseAuth);
        attachRecyclerViewAdapter();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mUserAdapter != null) {
            mUserAdapter.stopListening();
            mUserAdapter = null;
        }
    }

    private void attachRecyclerViewAdapter() {
        if (mAuth != null && mAuth.getCurrentUser() != null && mUserAdapter == null) {
            Query keyQuery = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("flock");
            DatabaseReference dataQuery = FirebaseDatabase.getInstance().getReference().child("users");
            FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setIndexedQuery(keyQuery, dataQuery, User.class).build();
            mUserAdapter =  new UserAdapter(options, this, this);
        }
    }

    private void startPhoneCall(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        if (AppPermissions.requestCallPermission(this)) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    private void startPickContact() {
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
