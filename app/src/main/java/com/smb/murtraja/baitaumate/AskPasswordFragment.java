package com.smb.murtraja.baitaumate;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smb.murtraja.baitaumate.OnFragmentInteractionListener.FragmentResultType;

public class AskPasswordFragment extends Fragment {

    /*
    This Fragment simply displays an EditText in which the user can input
    the password

     */


    private static final String ARG_RESULT_TYPE = "RESULT_TYPE";

    private FragmentResultType mResultType;
    private OnFragmentInteractionListener mListener;

    private String mPassword;

    private Button mSetPasswordButton;
    private EditText mPasswordEditText;
    private TextView mSetPasswordTextView;

    public AskPasswordFragment() {
        // Required empty public constructor
    }

    public static AskPasswordFragment newInstance(FragmentResultType resultType) {
        AskPasswordFragment fragment = new AskPasswordFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESULT_TYPE, resultType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResultType = (FragmentResultType) getArguments().getSerializable(ARG_RESULT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ask_password, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mPasswordEditText = (EditText) view.findViewById(R.id.et_password);
        mSetPasswordButton = (Button) view.findViewById(R.id.btn_set_password);
        mSetPasswordTextView = (TextView) view.findViewById(R.id.tv_set_password);
        mSetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSetPasswordClick();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        //http://stackoverflow.com/questions/32604552/onattach-not-called-in-fragment
        super.onAttach(context);
        onAttachInit(context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        onAttachInit(activity);
    }

    private void onAttachInit(Context context) {
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        Log.d(MainActivity.TAG, "inside on attach of fragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void onSetPasswordClick() {
        String password = mPasswordEditText.getText().toString();
        if(isPasswordInvalid(password)) {
            mSetPasswordTextView.setText("Please enter a valid password!");
        } else {
            mPassword = password;
            sendResultToActivity();
        }
    }

    private void sendResultToActivity() {
        mListener.onFragmentInteraction(mResultType, mPassword);
    }

    private boolean isPasswordInvalid(String password) {
        return "".equals(password);
    }
}
