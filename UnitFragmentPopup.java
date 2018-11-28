package com.ibwave.ibwavemobile.fragments.fragmentpopups;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.ibwave.ibwavemobile.ProjectManager;
import com.ibwave.ibwavemobile.R;
import com.ibwave.ibwavemobile.entities.Area;
import com.ibwave.ibwavemobile.entities.LayoutPlan;
import com.ibwave.ibwavemobile.ibwm.IbwmPlanDataAccessHelper;
import com.ibwave.utils.androidapp.customview.others.TextViewWithMandatoryIndicator;
import com.ibwave.utils.androidapp.dialogs.IBWaveDialogFragment;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

/**
 * Popup for selecting the associated closet and setting unit number
 */

public class UnitFragmentPopup extends IBWaveDialogFragment
{

    public interface UnitFragmentListener
    {
        void onUnitNumberCreatedFromUnitPopup(int unitNumber, Area spinnerValue, String description);

        void updateUnitNumber(int unitNumber);

        void unconnectAssociatedCloset(int unitNumber);

        void updateLivingUnit(Area area);

        boolean isValidUnitNumber(int unitNumber);

        void cancelPropertiesUnitDialog();

        void onUnitTagChangedFromUnitPopup(String customizedTag);

        void clearCustomizedTag();

        void clearUnitNumber();
    }

    public static final String TAG = UnitFragmentPopup.class.getName();
    private EditText mAreaUnitEditText;
    private UnitFragmentListener mUnitFragmentListener;
    private static List<Area> mAreaValues;
    private NumberPicker mAreaNumberPicker;
    private static String[] usageArray;
    private TextInputLayout mAreaUnitTextInputLayout;
    private boolean mErrorVisible = false;
    private static int mUnitNumber;
    private static int mAssociatedAreaNumber;
    private static boolean mIsFromToolbar;
    private WeakReference<Area> areaRef;

    private final View.OnFocusChangeListener mOnAreaUnitEditTextFocusChangedListener = new View.OnFocusChangeListener()
    {
        @Override
        public void onFocusChange(View v, boolean hasFocus)
        {
            updateCurrentAreaUnitStrings();
        }
    };

    private final TextView.OnEditorActionListener mOnAreaUnitEditorActionListener = new TextView.OnEditorActionListener()
    {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
        {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
            {
                if (event == null || !event.isShiftPressed())
                {
                    forceKeyboardDown();
                    updateCurrentAreaUnitStrings();
                    return true;
                }
            }
            return false;
        }
    };

    private final TextWatcher mOnAreaUnitTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {

        }

        @Override
        public void afterTextChanged(Editable s)
        {
            updateCurrentAreaUnitStrings();
        }
    };

    @Nullable
    private Area getArea()
    {
        return areaRef == null ? null : areaRef.get();
    }

    public static UnitFragmentPopup create(List<Area> spinnerValues, Area selectedArea,
                                           Context context, int unitNumber, int associatedAreaNumber, boolean isFromToolbar)
    {
        mAreaValues = spinnerValues;
        mUnitNumber = unitNumber;
        mAssociatedAreaNumber = associatedAreaNumber;
        mIsFromToolbar = isFromToolbar;

        IbwmPlanDataAccessHelper dbhelper = new IbwmPlanDataAccessHelper(context.getApplicationContext(), ProjectManager.INSTANCE.getCurrentProject().getGuid());
        LayoutPlan plan;
        int i = 0;

        usageArray = new String[spinnerValues.size() + 1];
        for (Area a : spinnerValues)
        {
            plan = new LayoutPlan();
            plan.setIbwmDbId(a.getIbwmDbParentId());
            LayoutPlan res = dbhelper.loadLayoutPlan(plan);

            String formatStr = associatedAreaNumber != 0
                                && a.getNumber() == associatedAreaNumber
                                ? "%s - %s %d*" : "%s - %s %d";
            usageArray[i] = String.format(Locale.US, formatStr, res.getName(), a.getDisplayLabel(context), a.getNumber());

            ++i;
        }
        usageArray[spinnerValues.size()] = context.getResources().getString(R.string.no_connected_equip_area);
        if (associatedAreaNumber == 0) //There is no associated closet
        {
            usageArray[spinnerValues.size()] += "*";
        }

        UnitFragmentPopup dlg = new UnitFragmentPopup();
        dlg.areaRef = new WeakReference<>(selectedArea);
        return dlg;
    }

    private void updateCurrentAreaUnitStrings()
    {
        String areaUnitEditTextText = mAreaUnitEditText.getText().toString();
        if (mErrorVisible)
        {
            mAreaUnitTextInputLayout.setError(null);
            mErrorVisible = false;
        }
        enablePositiveButton(!areaUnitEditTextText.isEmpty());
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            // This fragment is being re-created by the Android platform. The current implementation
            // does not handle this situation. The fragment and its parent activity are unable to
            // operate normally. It is better to simply dismiss this dialog, then.
            // See bug MOBL-6896 in Jira.
            dismiss();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        setCancelable(false);

        View contentView = getActivity().getLayoutInflater().inflate(R.layout.area_unit_fragment_popup_layout, null);

        mAreaUnitTextInputLayout = contentView.findViewById(R.id.unit_number_input_layout);

        TextViewWithMandatoryIndicator livingUnitNumberPickerLabel = contentView.findViewById(R.id.area_item_picker_title_textview);
        livingUnitNumberPickerLabel.setText(R.string.living_unit_associated_with_equip_area);
        livingUnitNumberPickerLabel.enableMandatoryIndicator();

        mAreaUnitEditText = contentView.findViewById(R.id.area_edit_text_button);
        mAreaUnitEditText.setText(String.valueOf(mUnitNumber));
        mAreaUnitEditText.setFilters(new InputFilter[]{Area.EMOJI_FILTER});
        mAreaUnitEditText.setOnFocusChangeListener(mOnAreaUnitEditTextFocusChangedListener);
        mAreaUnitEditText.setOnEditorActionListener(mOnAreaUnitEditorActionListener);
        mAreaUnitEditText.addTextChangedListener(mOnAreaUnitTextWatcher);

        mAreaNumberPicker = contentView.findViewById(R.id.area_number_picker);
        if(usageArray.length > 0)
        {
            mAreaNumberPicker.setMinValue(0);
            mAreaNumberPicker.setMaxValue(usageArray.length - 1);
            mAreaNumberPicker.setDisplayedValues(usageArray);

            if (mAssociatedAreaNumber == 0)
            {
                mAreaNumberPicker.setValue(usageArray.length - 1);
            }
            else
            {
                mAreaNumberPicker.setValue(getAssociatedAreaValue());
            }
        }
        else
        {
            livingUnitNumberPickerLabel.setVisibility(View.GONE);
            mAreaNumberPicker.setVisibility(View.GONE);
        }

        mAreaUnitEditText.setSelectAllOnFocus(true);
        mAreaUnitEditText.requestFocus();

        AlertDialog.Builder builder = createAlertDialogBuilder();

        builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //DO NOTHING HERE SEE ON START()
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if (!mIsFromToolbar)
                    mUnitFragmentListener.cancelPropertiesUnitDialog();
            }
        });

        builder.setTitle(getString(R.string.living_unit_properties));
        builder.setView(contentView);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog)
            {
                enablePositiveButton(!mAreaUnitEditText.getText().toString().isEmpty());
            }
        });

        return alertDialog;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        //Set the OnClickListener here to prevent the automatic dismiss
        Button positiveButton = getPositiveButton().get();
        if (positiveButton != null)
        {
            if(getArea() != null)
            {
                positiveButton.setText(R.string.ok);
            }

            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int unitNumber = 0;
                    String input = "";
                    boolean isNotNumber = false;
                    try
                    {
                        unitNumber = Integer.parseInt(mAreaUnitEditText.getText().toString());
                    }
                    catch (NumberFormatException e)
                    {
                        isNotNumber = true;
                    }

                    if(isNotNumber)
                    {
                     input = mAreaUnitEditText.getText().toString();
                     mUnitFragmentListener.onUnitTagChangedFromUnitPopup(input);
                     mUnitFragmentListener.clearUnitNumber();
                     dismiss();
                     return;
                    }
                    if(getArea() == null)//new area
                    {
                        if (mUnitFragmentListener.isValidUnitNumber(unitNumber))
                        {
                            if (mAreaNumberPicker.getValue() >= mAreaValues.size())
                            {
                                mUnitFragmentListener.unconnectAssociatedCloset(unitNumber);
                            }
                            else
                            {
                                if (mAreaValues.get(mAreaNumberPicker.getValue()).getNumber() != mAssociatedAreaNumber) //If associated closet was changed
                                    mUnitFragmentListener.onUnitNumberCreatedFromUnitPopup(unitNumber, mAreaValues.get(mAreaNumberPicker.getValue()), usageArray[mAreaNumberPicker.getValue()]);
                                else
                                    mUnitFragmentListener.updateUnitNumber(unitNumber);
                            }
                            dismiss();
                        }
                        else if(!isNotNumber)
                        {
                            mAreaUnitTextInputLayout.setError(getResources().getString(R.string.unit_number_taken));
                            mErrorVisible = true;
                        }
                    }
                    else
                    {
                        if(unitNumber != getArea().getNumber() && !mUnitFragmentListener.isValidUnitNumber(unitNumber) && !isNotNumber)
                        {
                            mAreaUnitTextInputLayout.setError(getResources().getString(R.string.unit_number_taken));
                            mErrorVisible = true;
                        }
                        else
                        {
                            getArea().setNumber(unitNumber);
                            if(usageArray.length > 0)
                            {
                                int selectedCloset = mAreaNumberPicker.getValue();
                                if (selectedCloset >= mAreaValues.size())
                                {
                                    getArea().setAssociatedClosetLabel(null);
                                    getArea().setAssociatedClosetArea(null);
                                    getArea().setAssociatedCloset(null);
                                }
                                else
                                {
                                    Area closet = mAreaValues.get(selectedCloset);
                                    getArea().setAssociatedClosetLabel(closet.getLabel());
                                    getArea().setAssociatedClosetArea(closet);
                                    getArea().setAssociatedCloset(closet.getIbwmDbId());
                                }
                            }

                            mUnitFragmentListener.updateLivingUnit(getArea());
                            mUnitFragmentListener.clearCustomizedTag();
                            dismiss();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof UnitFragmentListener)
            mUnitFragmentListener = (UnitFragmentListener) context;
        else
            throw new IllegalArgumentException("Parent activity should implement UnitFragmentListener!");
    }

    private void forceKeyboardDown()
    {
        // force keyboard down
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mAreaUnitEditText.getWindowToken(), 0);
    }

    private int getAssociatedAreaValue()
    {
        int i = 0;
        for (Area area : mAreaValues)
        {
            if (area.getNumber() == mAssociatedAreaNumber)
            {
                break;
            }
            i++;
        }
        return mAssociatedAreaNumber > 0 ? i : 0;
    }
}
