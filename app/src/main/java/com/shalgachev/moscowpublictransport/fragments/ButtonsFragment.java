package com.shalgachev.moscowpublictransport.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.shalgachev.moscowpublictransport.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ButtonsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ButtonsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ButtonsFragment extends Fragment {
    public enum Type
    {
        Digits,
        Alpha,
    }

    private static final String ARG_TYPE = "type";

    private Type mType;

    private OnFragmentInteractionListener mListener;

    public ButtonsFragment() {
        // Required empty public constructor
    }

    public static ButtonsFragment newInstance(Type type) {
        ButtonsFragment fragment = new ButtonsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = (Type) getArguments().getSerializable(ARG_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = null;
        switch (mType) {
            case Digits:
                root = inflater.inflate(R.layout.fragment_buttons_digits, container, false);
                break;
            case Alpha:
                root = inflater.inflate(R.layout.fragment_buttons_alpha, container, false);
                break;
        }

        if (root != null) {
            setupTransitionButton(root, R.id.button_goto_alpha);
            setupTransitionButton(root, R.id.button_goto_digits);

            final int[] buttonIds = {
                    R.id.button_minus,

                    R.id.button_digit_0,
                    R.id.button_digit_1,
                    R.id.button_digit_2,
                    R.id.button_digit_3,
                    R.id.button_digit_4,
                    R.id.button_digit_5,
                    R.id.button_digit_6,
                    R.id.button_digit_7,
                    R.id.button_digit_8,
                    R.id.button_digit_9,

                    R.id.button_alpha_1,
                    R.id.button_alpha_2,
                    R.id.button_alpha_3,
                    R.id.button_alpha_4,
                    R.id.button_alpha_5,
                    R.id.button_alpha_6,
                    R.id.button_alpha_7,
                    R.id.button_alpha_8,
                    R.id.button_alpha_9,
                    R.id.button_alpha_10,
                    R.id.button_alpha_11,
                    R.id.button_alpha_12,
                    R.id.button_alpha_13,
                    R.id.button_alpha_14,
                    R.id.button_alpha_15,
                    R.id.button_alpha_16,
                    R.id.button_alpha_17,
                    R.id.button_alpha_18,
            };

            final CharSequence[] buttonTitles = {
                    "-",

                    "0",
                    "1",
                    "2",
                    "3",
                    "4",
                    "5",
                    "6",
                    "7",
                    "8",
                    "9",

                    "А",
                    "Б",
                    "В",
                    "Д",
                    "З",
                    "К",
                    "М",
                    "Н",
                    "С",
                    "Т",
                    "Ц",
                    "Ч",
                    "Э",
            };

            for (int i = 0; i < buttonIds.length; i++) {
                int id = buttonIds[i];
                CharSequence title = i < buttonTitles.length ? buttonTitles[i] : null;
                setupAlphaNumButton(root, id, title);
            }
        }

        return root;
    }

    void setupAlphaNumButton(@NonNull View root, @IdRes int buttonId, CharSequence title)
    {
        final Button button = root.findViewById(buttonId);

        if (button != null) {
            button.setEnabled(title != null);
            button.setVisibility(title != null ? View.VISIBLE : View.INVISIBLE);
            if (title != null)
                button.setText(title);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onCharacterInput(button.getText());
                }
            });
        }
    }

    void setupTransitionButton(@NonNull View root, @IdRes int buttonId)
    {
        final Button button = root.findViewById(buttonId);

        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onTransitionRequested(mType);
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onCharacterInput(CharSequence str);
        void onTransitionRequested(Type currentType);
    }
}
