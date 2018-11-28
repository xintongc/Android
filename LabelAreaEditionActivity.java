package com.ibwave.ibwavemobile.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ibwave.ibwavemobile.ProjectManager;
import com.ibwave.ibwavemobile.R;
import com.ibwave.ibwavemobile.contracts.AbstractViews.IDelegateView;
import com.ibwave.ibwavemobile.contracts.AbstractViews.LayersData;
import com.ibwave.ibwavemobile.contracts.AbstractViews.SelectedObjectData;
import com.ibwave.ibwavemobile.contracts.LabelAreaEditionActivityContract;
import com.ibwave.ibwavemobile.entities.Area;
import com.ibwave.ibwavemobile.fragments.fragmentpopups.ClosetFragmentPopup;
import com.ibwave.ibwavemobile.fragments.fragmentpopups.UnitFragmentPopup;
import com.ibwave.ibwavemobile.iBwaveMobileApplication;
import com.ibwave.ibwavemobile.ibwm.IbwmIdentity;
import com.ibwave.ibwavemobile.objects.area.AreaType;
import com.ibwave.ibwavemobile.objects.touchable.TouchableImageViewState;
import com.ibwave.ibwavemobile.presenters.DatabaseChanges;
import com.ibwave.ibwavemobile.presenters.LabelAreaEditionActivityPresenter;
import com.ibwave.ibwavemobile.presenters.communication.busevent.PresenterMessageBus;
import com.ibwave.ibwavemobile.presenters.communication.messages.system.dialogs.ShowErrorDialogAlertMessage;
import com.ibwave.ibwavemobile.tasks.LoadPlanFromAreaActivityAsyncTask;
import com.ibwave.utils.androidapp.asynctasks.IBWaveAsyncTaskWithProgressDialog;
import com.ibwave.utils.androidapp.customview.brandedview.IBWaveBrandedIconAndTextToggleButton;

import java.util.List;

/**
 * Created by ddibblee on 3/20/2017.
 */

public class LabelAreaEditionActivity extends BasePlanActivity implements LabelAreaEditionActivityContract.IView, UnitFragmentPopup.UnitFragmentListener, ClosetFragmentPopup.ClosetFragmentListener
{
    private static final String SAVED_STATE_DB_CHANGES = "saved_state_db_changes";

    private LabelAreaEditionActivityContract.IPresenter mIPresenterLabelAreaEditionActivity;

    private View mLabelAreaSubmenu;

    private TextView mActionBarTitleAreaName;
    private TextView mActionBarTitleAreaType;

    private TextView mEquipmentAreaLabel;
    private TextView mEquipmentAreaValue;
    private TextView mEquipmentAreaDescriptionLabel;
    private TextView mEquipmentAreaDescriptionValue;

    private AreaType mAreaType;
    private String descriptionValue;
    private Area area;

    private IBWaveBrandedIconAndTextToggleButton mUnitToggle;
    private IBWaveBrandedIconAndTextToggleButton mClosetToggle;
    private IBWaveBrandedIconAndTextToggleButton mSaveButton;
    private IBWaveBrandedIconAndTextToggleButton mCurrentActionToggleButtonChecked = null;

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVED_STATE_DB_CHANGES, mIPresenterLabelAreaEditionActivity.getCurrentChanges());
    }

    private final IBWaveBrandedIconAndTextToggleButton.OnCheckedChangeListener mActionToggleButtonOnClickListener = new IBWaveBrandedIconAndTextToggleButton.OnCheckedChangeListener()
    {

        @Override
        public void onCheckedChanged(IBWaveBrandedIconAndTextToggleButton view, boolean isChecked)
        {
            if (mCurrentActionToggleButtonChecked != null
                    &&
                    mCurrentActionToggleButtonChecked.getId() == view.getId() && !isChecked)
            {
                mCurrentActionToggleButtonChecked = null;
                mAreaType = AreaType.NONE;
                mIPresenterLabelAreaEditionActivity.setAreaType(mAreaType);
                initTextviewLabels();
                initTextviewValues();
                mIPresenterLabelAreaEditionActivity.sendPlanViewStatusChangeMessage(TouchableImageViewState.NONE);
            }

            //Try to disabled the current toggle action button checked before try to enable the new one
            if (isChecked)
            {
                //Only one action toggle button could be activated at the same time
                if (mCurrentActionToggleButtonChecked != null
                        &&
                        mCurrentActionToggleButtonChecked.getId() != view.getId())
                {
                    mCurrentActionToggleButtonChecked.setChecked(false);
                }
                mCurrentActionToggleButtonChecked = view;
            }

            if (isChecked)
            {
                switch (view.getId())
                {
                    case R.id.active_action_unit:
                        mAreaType = AreaType.UNIT;
                        mIPresenterLabelAreaEditionActivity.setAreaType(mAreaType);
                        initTextviewLabels();
                        initTextviewValues();
                        mIPresenterLabelAreaEditionActivity.sendPlanViewStatusChangeMessage(TouchableImageViewState.IS_AREA_UNIT);
                        showLabelAreaSubMenu();
                        break;
                    case R.id.active_action_closet:
                        mAreaType = AreaType.CLOSET;
                        mIPresenterLabelAreaEditionActivity.setAreaType(mAreaType);
                        initTextviewLabels();
                        initTextviewValues();
                        showLabelAreaSubMenu();
                        mIPresenterLabelAreaEditionActivity.sendPlanViewStatusChangeMessage(TouchableImageViewState.IS_AREA_CLOSET);
                        break;
                    case R.id.active_action_done:
                        saveAreasToDb();
                        setResult(RESULT_OK);
                        break;
                }
            }
            else
            {
                mIPresenterLabelAreaEditionActivity.setAssociatedCloset(null);
                mLabelAreaSubmenu.setVisibility(View.GONE);
            }
        }
    };

    private void showLabelAreaSubMenu()
    {
        mLabelAreaSubmenu.setVisibility(View.VISIBLE);
        mLabelAreaSubmenu.bringToFront();
    }

    private void saveAreasToDb()
    {
        SaveAreasAsyncTask asyncTask = new SaveAreasAsyncTask(
                getSupportFragmentManager(),
                mIPresenterLabelAreaEditionActivity);
        asyncTask.execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setActivityToFullScreenMode();

        setContentView(R.layout.label_area_edition_activity_layout);

        mLabelAreaSubmenu = findViewById(R.id.label_area_submenu);

        mActionBarTitleAreaName = (TextView) findViewById(R.id.actionBarTitleTextView1);
        mActionBarTitleAreaType = (TextView) findViewById(R.id.actionBarTitleTextView2);

        mEquipmentAreaLabel = (TextView) findViewById(R.id.equipment_area_label);
        mEquipmentAreaValue = (TextView) findViewById(R.id.equipment_area_value);
        mEquipmentAreaDescriptionLabel = (TextView) findViewById(R.id.equipment_area_description_label);
        mEquipmentAreaDescriptionValue = (TextView) findViewById(R.id.equipment_area_description_value);

        mAreaType = (AreaType) getIntent().getSerializableExtra("mAreaType");

        initPlanViewsAndPresenter();

        if (savedInstanceState != null && !savedInstanceState.isEmpty())
        {
            mIPresenterLabelAreaEditionActivity.setSavedDbChanges((DatabaseChanges) savedInstanceState.getParcelable(SAVED_STATE_DB_CHANGES));
            mIPresenterLabelAreaEditionActivity.reAssociateClosets();
        }
        loadPlanViewPartData();

        initTextviewLabels();
    }

    private void initTextviewLabels()
    {
        mEquipmentAreaLabel.setVisibility(View.VISIBLE);
        mEquipmentAreaDescriptionLabel.setVisibility(View.VISIBLE);

        if (mAreaType == AreaType.UNIT)
        {
            mEquipmentAreaLabel.setText(R.string.living_unit_pound);
            mEquipmentAreaDescriptionLabel.setText(R.string.associated_area);
            mIPresenterLabelAreaEditionActivity.initStatus(TouchableImageViewState.IS_AREA_UNIT);
        }
        else if (mAreaType == AreaType.CLOSET)
        {
            mEquipmentAreaLabel.setText(R.string.equipment_area_pound);
            mEquipmentAreaDescriptionLabel.setText(R.string.equipment_description);
            mIPresenterLabelAreaEditionActivity.initStatus(TouchableImageViewState.IS_AREA_CLOSET);
        }
        else
        {
            mEquipmentAreaLabel.setVisibility(View.INVISIBLE);
            mEquipmentAreaDescriptionLabel.setVisibility(View.INVISIBLE);
            mIPresenterLabelAreaEditionActivity.initStatus(TouchableImageViewState.NONE);
        }
    }

    @Override
    public void initTextviewValues()
    {
        int unitNumber = mIPresenterLabelAreaEditionActivity.getFirstUnitNumber(LabelAreaEditionActivity.this);
        while (!mIPresenterLabelAreaEditionActivity.validAreaNumber(unitNumber))
        {
            unitNumber++;
        }
        mIPresenterLabelAreaEditionActivity.setmUnitNumber(unitNumber);

        mEquipmentAreaValue.setVisibility(View.VISIBLE);
        mEquipmentAreaDescriptionValue.setVisibility(View.VISIBLE);

        if (mAreaType == AreaType.UNIT)
        {
            mIPresenterLabelAreaEditionActivity.setAssociatedCloset(null);
            mIPresenterLabelAreaEditionActivity.setmEquipmentDescription(getResources().getString(R.string.unit));
            descriptionValue = null;
        }
        else if (mAreaType == AreaType.CLOSET)
        {
            mIPresenterLabelAreaEditionActivity.setmEquipmentDescription(getResources().getString(R.string.equip_area));
            descriptionValue = getResources().getString(R.string.equip_area);
        }
        else
        {
            mEquipmentAreaValue.setVisibility(View.GONE);
            mEquipmentAreaDescriptionValue.setVisibility(View.GONE);
        }

        updateTitleValues(unitNumber);
    }


    @Override
    protected void initPresenters(Bundle bundle)
    {
        Bundle webParams = getIntent().getBundleExtra(iBwaveMobileApplication.WEB_PARAM_BUNDLE_NAME);
        IbwmIdentity ibwmIdentityForPlan = IbwmIdentity.createFromBundle(webParams);
        String layoutPlanGuid = webParams.getString("layoutPlanGuid");
        String layoutID = String.valueOf(ibwmIdentityForPlan.getId());


        mIPresenterLabelAreaEditionActivity = new LabelAreaEditionActivityPresenter(this);
        mPresenterMessageBus = new PresenterMessageBus();
        initPresenterMessageBus(mPresenterMessageBus);

        mIPresenterLabelAreaEditionActivity.initFromValue(ibwmIdentityForPlan, layoutPlanGuid, layoutID);
    }

    @Override
    protected void loadPlanViewPartData()
    {
        //start android async task to load data
        LoadPlanFromAreaActivityAsyncTask asyncTask = new LoadPlanFromAreaActivityAsyncTask(
                this,
                getSupportFragmentManager(),
                mIPresenterLabelAreaEditionActivity);
        asyncTask.execute();
    }

    @Override
    protected void initActionBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
        }

        mUnitToggle = (IBWaveBrandedIconAndTextToggleButton) toolbar.findViewById(R.id.active_action_unit);
        setToggleButtonOnClickListener(mUnitToggle);
        mClosetToggle = (IBWaveBrandedIconAndTextToggleButton) toolbar.findViewById(R.id.active_action_closet);
        setToggleButtonOnClickListener(mClosetToggle);
        mSaveButton = (IBWaveBrandedIconAndTextToggleButton) toolbar.findViewById(R.id.active_action_done);
        setToggleButtonOnClickListener(mSaveButton);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                cancelAreaEdition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        cancelAreaEdition();
    }

    private void cancelAreaEdition()
    {
        if (mIPresenterLabelAreaEditionActivity.hasPendingChanges())
        {
            showSaveDialog();
        }
        else
        {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void initPresenterMessageBus(PresenterMessageBus bus)
    {
        mIPresenterLabelAreaEditionActivity.setEventBus(bus);
        mIPresenterLabelAreaEditionActivity.registerToMessageBus(bus);
    }

    public void openNumberDialog(View v)
    {
        mIPresenterLabelAreaEditionActivity.setupDialogRequested(true);
    }

    public void openDescriptionDialog(View v)
    {
        mIPresenterLabelAreaEditionActivity.setupDialogRequested(false);
    }

    @Override
    public void openSetupDialog(boolean isNumberFocused, boolean fromToolbar)
    {
        Area selectedArea = getSelectedArea();
        AreaType currentType = selectedArea == null ? mAreaType : AreaType.create(selectedArea.getType());
        mIPresenterLabelAreaEditionActivity.setAreaType(mAreaType);

        if (currentType == AreaType.UNIT)
        {
            mIPresenterLabelAreaEditionActivity.initStatus(TouchableImageViewState.IS_AREA_UNIT);
            showUnitSetupDialog(selectedArea, fromToolbar);
        }
        else
        {
            mIPresenterLabelAreaEditionActivity.initStatus(TouchableImageViewState.IS_AREA_CLOSET);
            showClosetSetupDialog(selectedArea, isNumberFocused, fromToolbar);
        }
    }

    public void showUnitSetupDialog(Area selectedArea, boolean fromToolbar)
    {
        List<Area> closets = mIPresenterLabelAreaEditionActivity.loadAllClosets();

        Area closet = mIPresenterLabelAreaEditionActivity.getAssociatedCloset();

        int associatedClosetNumber = closet == null ? 0 : closet.getNumber();
        int unitNumber = mIPresenterLabelAreaEditionActivity.getmUnitNumber();

        if(selectedArea != null)
        {
            setCloset(selectedArea, closets);
            if(selectedArea.getAssociatedClosetArea() != null)
            {
                associatedClosetNumber = selectedArea.getAssociatedClosetArea().getNumber();
            }

            unitNumber = selectedArea.getNumber();
        }

        UnitFragmentPopup areaDialog = UnitFragmentPopup.create(closets, selectedArea, this, unitNumber, associatedClosetNumber, fromToolbar);
        areaDialog.show(getSupportFragmentManager(), UnitFragmentPopup.TAG);
    }

    private void setCloset(Area selectedArea, List<Area> closets)
    {
        if(selectedArea == null)
            return;

        Area cArea = selectedArea.getAssociatedClosetArea();
        if(cArea != null)
        {
            for (Area closet : closets)
            {
                if(cArea.getNumber() == closet.getNumber())
                {
                    selectedArea.setAssociatedClosetArea(closet);
                    selectedArea.setAssociatedCloset(closet.getIbwmDbId());
                }
            }
        }
        else
        {
            Integer closetId = selectedArea.getAssociatedCloset();
            if(closetId == null)
                return;

            for (Area closet : closets)
            {
                if(closetId == closet.getIbwmDbId())
                {
                    selectedArea.setAssociatedClosetArea(closet);
                    selectedArea.setAssociatedCloset(closet.getIbwmDbId());
                }
            }
        }
    }

    public void showClosetSetupDialog(Area selectedArea, boolean isNumberFocused, boolean fromToolbar)
    {
        int unitNumber = selectedArea == null
                ? mIPresenterLabelAreaEditionActivity.getmUnitNumber()
                : selectedArea.getNumber();


        String closetSetupDialogDescription = null;
        if (mCurrentActionToggleButtonChecked == null)
        {
            if (selectedArea != null)
            {
                closetSetupDialogDescription = selectedArea.getLabel();
            }
        }
        else
        {
            closetSetupDialogDescription = descriptionValue;
        }

        ClosetFragmentPopup closetDialog = ClosetFragmentPopup.create(unitNumber, selectedArea, closetSetupDialogDescription, isNumberFocused, fromToolbar);
        closetDialog.show(getSupportFragmentManager(), ClosetFragmentPopup.TAG);
    }

    @Nullable
    public Area getSelectedArea()
    {
        Area selectedArea = null;
        IDelegateView dv = getDelegateView();
        if(dv != null)
        {
            SelectedObjectData selected = getDelegateView().createData();
            selectedArea = selected == null ? null : selected.getSelectedArea();
        }
        return selectedArea;
    }

    @Override
    public void updateTitleFields()
    {
        String title = ProjectManager.INSTANCE.getCurrentProject().getName();
        if (mActionBarTitleAreaName != null)
        {
            mActionBarTitleAreaName.setText(title);
        }
        if (mActionBarTitleAreaType != null)
        {
            mActionBarTitleAreaType.setText(mIPresenterLabelAreaEditionActivity.getPlanName());
        }
    }

    @Override
    public void updateTitleValues(int unitNumber)
    {
        SpannableString content = new SpannableString(String.valueOf(unitNumber));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        mEquipmentAreaValue.setText(content);
        if (descriptionValue == null)
        {
            content = new SpannableString(getResources().getString(R.string.no_connected_equip_area));
        }
        else
        {
            if (mAreaType == AreaType.CLOSET && descriptionValue.length() > Area.LABEL_DISPLAY_LENGTH)
                content = new SpannableString(getString(R.string.display_label, descriptionValue.substring(0, Area.LABEL_DISPLAY_LENGTH)));
            else
                content = new SpannableString(descriptionValue);
        }
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        mEquipmentAreaDescriptionValue.setText(content);
    }

    @Override
    public void showErrorDialog(final ShowErrorDialogAlertMessage.ErrorMessageID errorMessageID)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(LabelAreaEditionActivity.this);
                int textStringResourceID;
                switch (errorMessageID)
                {
                    case LABEL_AREA_DUPLICATE_NUMBER:
                        textStringResourceID = R.string.unit_number_already_exists;
                        break;
                    default:
                        textStringResourceID = R.string.unknown_issue_error_message;
                        break;
                }
                builder.setMessage(textStringResourceID);
                builder.setPositiveButton(R.string.ok, null);
                builder.setCancelable(false);
                builder.show();
            }
        });
    }

    @Override
    public void showSaveDialog()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.save_changes_alert_message);
        alert.setPositiveButton(R.string.save, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int whichButton)
            {
                saveAreasToDb();
                setResult(RESULT_OK);
            }
        });
        alert.setNegativeButton(R.string.discard, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                setResult(RESULT_OK);
                finish();
            }
        });
        alert.setCancelable(true);
        alert.show();
    }

    @Override
    public void setActionBarState()
    {
        switch (mAreaType)
        {
            case UNIT:
                mUnitToggle.setChecked(true);
                break;
            case CLOSET:
                mClosetToggle.setChecked(true);
                break;
        }
    }

    @Override
    public void onUnitNumberCreatedFromUnitPopup(int unitNumber, Area spinnerValue, String description)
    {
        mIPresenterLabelAreaEditionActivity.setAssociatedCloset(spinnerValue);
        descriptionValue = description;
        updateUnitNumberAndTitle(unitNumber);
    }

    @Override
    public void updateUnitNumber(int unitNumber)
    {
        updateUnitNumberAndTitle(unitNumber);
    }

    @Override
    public void unconnectAssociatedCloset(int unitNumber)
    {
        mIPresenterLabelAreaEditionActivity.setAssociatedCloset(null);
        descriptionValue = null;
        updateUnitNumberAndTitle(unitNumber);
    }

    private void updateUnitNumberAndTitle(int unitNumber)
    {
        mIPresenterLabelAreaEditionActivity.setmUnitNumber(unitNumber);
        updateTitleValues(unitNumber);
    }

    @Override
    public boolean isValidUnitNumber(int unitNumber)
    {
        return mIPresenterLabelAreaEditionActivity.validAreaNumber(unitNumber);
    }

    @Override
    public void cancelPropertiesUnitDialog()
    {
        mAreaType = AreaType.NONE;
        mIPresenterLabelAreaEditionActivity.setAreaType(AreaType.NONE);
        mIPresenterLabelAreaEditionActivity.initStatus(TouchableImageViewState.NONE);
    }

    @Override
    public void onClosetNumberCreatedFromClosetPopup(String equipmentDescription, int equipmentNumber)
    {
        mIPresenterLabelAreaEditionActivity.setmUnitNumber(equipmentNumber);
        mIPresenterLabelAreaEditionActivity.setmEquipmentDescription(equipmentDescription);
        descriptionValue = equipmentDescription;
        updateTitleValues(equipmentNumber);
    }

    @Override
    public void updateCloset(Area area)
    {
        if(area != null)
        {
            mIPresenterLabelAreaEditionActivity.updateArea(area);
            descriptionValue = area.getLabel();
            updateTitleValues(area.getNumber());
        }

        mIPresenterLabelAreaEditionActivity.setAreaType(AreaType.NONE);
        mIPresenterLabelAreaEditionActivity.sendPlanViewStatusChangeMessage(TouchableImageViewState.NONE);
    }

    @Override
    public void updateLivingUnit(Area area)
    {
        if(area != null)
        {
            mIPresenterLabelAreaEditionActivity.updateArea(area);
            if(area.getAssociatedClosetArea() != null)
                descriptionValue = area.getAssociatedClosetArea().getLabel();
            updateTitleValues(area.getNumber());
        }

        mIPresenterLabelAreaEditionActivity.setAreaType(AreaType.NONE);
        mIPresenterLabelAreaEditionActivity.sendPlanViewStatusChangeMessage(TouchableImageViewState.NONE);
    }

    @Override
    public boolean isValidEquipmentNumber(int equipmentNumber)
    {
        return mIPresenterLabelAreaEditionActivity.validAreaNumber(equipmentNumber);
    }

    @Override
    public void cancelClosetDialogProperties()
    {
        mAreaType = AreaType.NONE;
        mIPresenterLabelAreaEditionActivity.setAreaType(AreaType.NONE);
        mIPresenterLabelAreaEditionActivity.initStatus(TouchableImageViewState.NONE);
    }

    private void setToggleButtonOnClickListener(IBWaveBrandedIconAndTextToggleButton ibWaveBrandedIconAndTextToggleButton)
    {
        if (ibWaveBrandedIconAndTextToggleButton != null)
        {
            ibWaveBrandedIconAndTextToggleButton.setOnCheckedChangeListener(mActionToggleButtonOnClickListener);
        }
    }

    class SaveAreasAsyncTask extends IBWaveAsyncTaskWithProgressDialog<Void, Void, Void>
    {
        private final LabelAreaEditionActivityContract.IPresenter mIPresenterAreaActivity;

        public SaveAreasAsyncTask(FragmentManager supportFragmentManager, LabelAreaEditionActivityContract.IPresenter presenter)
        {
            super(supportFragmentManager, LabelAreaEditionActivity.this);
            mIPresenterAreaActivity = presenter;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute("SaveAreasAsyncTask dialog", R.string.saving);
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            mIPresenterAreaActivity.saveAllNewAreasInDB();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            finish();
        }
    }

    @Override
    public LayersData createLayersData()
    {
        LayersData m = super.createLayersData();
        mIPresenterLabelAreaEditionActivity.updateData(m);
        return m;
    }

    public void clearCustomizedTag()
    {
        area.setLabel("Unit ");
    }
    public void clearUnitNumber()
    {
        area.setNumber(0);
    }
    public void onUnitTagChangedFromUnitPopup(String customizedTag)
    {
        area.setLabel("Unit " + customizedTag);
    }
}
