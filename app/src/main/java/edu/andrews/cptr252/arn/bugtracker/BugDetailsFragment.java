package edu.andrews.cptr252.arn.bugtracker;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.UUID;


/**
 * Show the details for a bug and allow editing
 */
public class BugDetailsFragment extends Fragment {
    /** Key used to pass the id of a bug */
    public static final String EXTRA_BUG_ID = "edu.andrews.cptr252.arn.bugtracker.bug_id";

    /** Tag used to identify the DatePickerDialog in the FragmentManager */
    private static final String DIALOG_DATE = "date";

    /** Result code used for communication with DatePickerFragment */
    private static final int REQUEST_DATE = 0;

    /** Tag for logging fragment messages */
    public static final String TAG = "BugDetailsFragment";

    /** Reference to bug description field */
    private EditText mDescriptionField;
    /** Reference to bug date button */
    private Button mDateButton;
    /** Reference to bug solved check box */
    private CheckBox mSolvedCheckBox;
    /** Bug that is begin viewed/edited */
    private Bug mBug;
    /** Reference to title field for bug*/
    private EditText mTitleField;

    /** Request code used for taking pictures */
    private static final int REQUEST_PHOTO = 1;
    /** Reference to camera button */
    private ImageButton mPhotoButton;
    /** Reference to image thumbnail */
    private ImageView mPhotoView;
    /** FIle containing image of bug */
    private File mPhotoFile;

    public BugDetailsFragment() {
        // Required empty public constructor
    }

    /** Required interface to be implemented in hosting activities */
    public interface Callbacks {
        void onBugUpdated(Bug bug);
    }

    private Callbacks mCallbacks;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void updateBug() {
        BugList.getInstance(getActivity()).updateBug(mBug);
        mCallbacks.onBugUpdated(mBug);
    }

    /**
     * Create a new BugDetailsFragment with a given Bug id as an argument
     * @param bugId
     * @return A reference to the new BugDetailsFragment
     */
    public static BugDetailsFragment newInstance(UUID bugId) {
        // Create a new argument Bundle object.
        // Add the Bug id as a n argument
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_BUG_ID, bugId);
        // Create a new instance of BugDetailsFragment
        BugDetailsFragment fragment = new BugDetailsFragment();
        // Pass the bundle (containing the Bug id) to the fragment
        // THe bundle will be unpacked in the fragment's onCreate(Bundle) method
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Extract bug id from Bundle
        UUID bugId = (UUID)getArguments().getSerializable(EXTRA_BUG_ID);

        // Get the bug with the id from the Bundle.
        // This will be the bug that the fragment displays
        mBug = BugList.getInstance(getActivity()).getBug(bugId);

        // get the image file for this bug
        mPhotoFile = BugList.getInstance(getActivity()).getPhotoFile(mBug);
    }

    /**
     * Update the date displayed on the date button
     * */
    public void updateDate() {
        mDateButton.setText(mBug.getDate().toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_bug_details, container, false);

        // get reference to EditText box for bug title
        mTitleField = v.findViewById(R.id.bug_title);
        mTitleField.setText(mBug.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // left intentionally blank
            }
            @Override
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                // user typed text, update the bug title
                mBug.setTitle(s.toString());
                // write the new title to the message log for debugging
                Log.d(TAG, "Title changed to " + mBug.getTitle());
                updateBug();
            }
            @Override
            public void afterTextChanged(Editable s) {
                // left intentionally blank
            }
        });

        // get reference to EditText box for bug description
        mDescriptionField = v.findViewById(R.id.bug_description);
        mDescriptionField.setText(mBug.getDescription());
        mDescriptionField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // left intentionally blank
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBug.setDescription(s.toString());
                Log.d(TAG, "Set description to "+s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {
                // left intentionally blank
            }
        });

        // get reference to bug date button.
        mDateButton = v.findViewById(R.id.bug_date);
        // Set the date text on the bug date button
        updateDate();
        // Create a listener to handle date button clicks
        mDateButton.setOnClickListener(new View.OnClickListener() {
            // Launch the date picker dialog when user clicks the button
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                // Create a new instance of the Date picker fragment
                // Give it the current bug date
                DatePickerFragment dialog = DatePickerFragment.newInstance(mBug.getDate());
                // Let DatePickerFragment communicate directly with BugDetailsFragment
                dialog.setTargetFragment(BugDetailsFragment.this, REQUEST_DATE);
                // Display the DatePickerFragment
                dialog.show(fm, DIALOG_DATE);
            }
        });

        // get reference to solved check box
        mSolvedCheckBox = v.findViewById(R.id.bug_solved);
        mSolvedCheckBox.setChecked(mBug.isSolved());
        // toggle bug solved status when check box is tapped
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // set the bugs's solved property
                mBug.setSolved(isChecked);
                Log.d(TAG, "Set solved status to "+isChecked);
                updateBug();
            }
        });

        // create intent used to start camera app
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // if camera is not available, disable camera functionality
        PackageManager pm = getActivity().getPackageManager();
        boolean canTakePhoto = false;
        if (mPhotoFile != null && captureImage.resolveActivity(pm) != null) {
            //we can create a photo file and the camera is available
            canTakePhoto = true;
            // Give the camera app the location and filename for the future image
            // Put this information in the intent used to launch the app
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    getActivity().getApplicationContext().getPackageName() + ".provider",
                    mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        // setup on-click listener for camera button
        mPhotoButton = v.findViewById(R.id.bug_imageButton);
        mPhotoButton.setImageResource(android.R.drawable.ic_menu_camera);
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // launch the camera activity
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
        mPhotoView = v.findViewById(R.id.bug_imageView);
        updatePhotoView();

        return v;
    }

    /** Update the thumbnail displayed in the image view. */
    private void updatePhotoView() {
        if (mPhotoFile == null || mPhotoFile.exists() == false) {
            // No image available.
            mPhotoView.setImageDrawable(null);
        } else {
            // Image is available. Display it in ImageView.
            try {
                FileInputStream fis = new FileInputStream(mPhotoFile);
                mPhotoView.setImageBitmap(BitmapFactory.decodeStream(fis));
            } catch (FileNotFoundException e) {
                mPhotoView.setImageDrawable(null); // no image available
            }
        }
    }

    /**
     * Save the bug list when app is paused
     */
    @Override
    public void onPause() {
        super.onPause();
        BugList.getInstance(getActivity()).updateBug(mBug);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            // user did not click ok in the dialog
            // ignore intent
            return;
        }

        if (requestCode == REQUEST_DATE) {
            // a date is sent back in the intent. extract the date
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            // update the bug with the new date
            mBug.setDate(date);
            updateBug();
            // update the bug date button text with the new date
            updateDate();
        }

        if (requestCode == REQUEST_PHOTO) {
            // User took a new picture with the camera app
            updatePhotoView();
        }
    }
}
