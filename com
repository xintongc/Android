package com.ibwave.ibwavemobile.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.ibwave.ibwavemobile.ComponentDetailActivity;
import com.ibwave.ibwavemobile.NetworkComponentDetailActivity;
import com.ibwave.ibwavemobile.R;
import com.ibwave.ibwavemobile.adapters.ComponentListViewPagerFragmentAdapter;
import com.ibwave.ibwavemobile.contracts.ComponentSelectionActivityContract;
import com.ibwave.ibwavemobile.entities.DrawingMedia;
import com.ibwave.ibwavemobile.entities.smallcell.PartSubcategory;
import com.ibwave.ibwavemobile.fragments.ComponentListViewPagerFragment;
import com.ibwave.ibwavemobile.iBwaveMobileApplication;
import com.ibwave.ibwavemobile.media.MediaManager;
import com.ibwave.ibwavemobile.objects.component.ToolBarComponentType;
import com.ibwave.ibwavemobile.presenters.ComponentSelectionActivityPresenter;
import com.ibwave.ibwavemobile.presenters.communication.busevent.PresenterMessageBus;
import com.ibwave.utils.ImageUtils;
import com.ibwave.utils.androidapp.IBWaveBrandedActivity;

import java.io.File;
import java.util.List;

import static com.ibwave.ibwavemobile.plans.create.PicturePreviewActivity.PATH_KEY;

/**
 * Fragment activity that host the Component list for the selection
 */
public class ComponentSelectionActivity
        extends IBWaveBrandedActivity
        implements ComponentSelectionActivityContract.IView, ComponentListViewPagerFragment.ComponentListListener
{
    public final static String ARG_COMPONENT_TYPE = "ARG_COMPONENT_TYPE";
    public static final String ARG_COMPONENT_SUBCATEGORY_ID_FOR_VIEW_PAGER_AUTO_SELECT = "ARG_COMPONENT_SUBCATEGORY_ID_FOR_VIEW_PAGER_AUTO_SELECT";
    public static final String ARG_IS_TWISTED_PAIR_CABLE = "ARG_IS_TWISTED_PAIR_CABLE";

    public final static int START_COMPONENT_DETAIL_ACTIVITY = 101;

    private ComponentSelectionActivityContract.IPresenter mIPresenterComponentSelectionActivity;
    private PresenterMessageBus mPresenterMessageBus;
    private ToolBarComponentType mComponentType;

    private boolean mIsTwistedPairCable;
    private boolean mIsMarkupPictureRequest;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setActivityToFullScreenMode();

        setContentView(R.layout.component_selection_activity_layout);
        initActionBar();
        initMVP();

        mIsTwistedPairCable = getIntent().getBooleanExtra(ARG_IS_TWISTED_PAIR_CABLE, false);

        mIsMarkupPictureRequest = getIntent().getAction() == MarkupEditionActivity.SELECT_COMPONENT_FOR_PICTURE_ACTION ? true : false;

        mComponentType = (ToolBarComponentType) getIntent().getSerializableExtra(ARG_COMPONENT_TYPE);
        List<PartSubcategory> componentSubcategories = mIPresenterComponentSelectionActivity.getComponentSubTypeList(mComponentType, getResources().getBoolean(R.bool.has_cellular));

        ComponentListViewPagerFragmentAdapter sectionsPagerAdapter =
                new ComponentListViewPagerFragmentAdapter(
                        this,
                        getSupportFragmentManager(),
                        componentSubcategories);

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        assert viewPager != null;
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        assert tabLayout != null;
        if (componentSubcategories.size() > 1)
        {
            tabLayout.setupWithViewPager(viewPager);
        }
        else
        {
            tabLayout.setVisibility(View.GONE);
        }

        attemptViewPagerTabAutoSelect(componentSubcategories, viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                if (mIsMarkupPictureRequest)
                {
                    finish();
                    return true;
                }
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    void attemptViewPagerTabAutoSelect(List<PartSubcategory> componentSubcategories, ViewPager viewPager)
    {
        int subcategoryIdToRestore = getIntent().getIntExtra(ARG_COMPONENT_SUBCATEGORY_ID_FOR_VIEW_PAGER_AUTO_SELECT, -1);
        if (subcategoryIdToRestore != -1)
        {
            for (PartSubcategory componentSubcategory : componentSubcategories)
            {
                if (componentSubcategory.subcategoryType == subcategoryIdToRestore)
                {
                    viewPager.setCurrentItem(componentSubcategories.indexOf(componentSubcategory));
                }
            }

        }
    }

    private void initMVP()
    {
        mPresenterMessageBus = new PresenterMessageBus();
        mIPresenterComponentSelectionActivity = new ComponentSelectionActivityPresenter(this);
        initPresenterMessageBus(mPresenterMessageBus);
    }

    @Override
    public void initPresenterMessageBus(PresenterMessageBus bus)
    {
        mIPresenterComponentSelectionActivity.registerToMessageBus(mPresenterMessageBus);
        mIPresenterComponentSelectionActivity.setEventBus(mPresenterMessageBus);
    }

    private void initActionBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setDisplayHomeAsUpEnabled(true);
    }


    private void startComponentDetailActivity(String compGUID, String compHashData, boolean compRecentlyUsed, int compSubcategory)
    {
        Intent intent;
        switch (mComponentType)
        {
            case SMALL_CELL_AND_WIFI:
                intent = new Intent(this, ComponentDetailActivity.class);
                break;
            default:
                intent = new Intent(this, NetworkComponentDetailActivity.class);
                break;
        }

        intent.putExtra(ComponentDetailActivity.COMPONENT_GUID_EXTRA, compGUID);
        intent.putExtra(ComponentDetailActivity.COMPONENT_HASH_EXTRA, compHashData);
        if (compRecentlyUsed)
        {
            intent.putExtra(ComponentDetailActivity.COMPONENT_SUBCATEGORY_EXTRA, compSubcategory);
            intent.putExtra(ComponentDetailActivity.COMPONENT_DETAIL_MODE_EXTRA, ComponentDetailActivity.ComponentDetailMode.SHOW_LAST_COMP_SELECTED);
        }
        else
        {
            intent.putExtra(ComponentDetailActivity.COMPONENT_DETAIL_MODE_EXTRA, ComponentDetailActivity.ComponentDetailMode.NEW_COMPONENT_SELECTION);
        }
        intent.putExtra(ARG_COMPONENT_TYPE, mComponentType);
        if (mIsTwistedPairCable)
        {
            intent.putExtra(ARG_IS_TWISTED_PAIR_CABLE, true);
            intent.putExtra(iBwaveMobileApplication.WEB_PARAM_BUNDLE_NAME, getIntent().getBundleExtra(iBwaveMobileApplication.WEB_PARAM_BUNDLE_NAME));
            startActivityForResult(intent, PlanEditionActivity.START_TWISTED_PAIR_CABLE_ACTIVITY_REQUEST);
        }
        else
        {
            startActivityForResult(intent, START_COMPONENT_DETAIL_ACTIVITY);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case MarkupEditionActivity.SELECT_COMPONENT_FOR_PICTURE:
                if (resultCode == RESULT_OK)
                {
                    String savedImageName = data.getStringExtra(PATH_KEY);
                    data.putExtra(PATH_KEY, savedImageName);
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;

            case START_COMPONENT_DETAIL_ACTIVITY:
            case PlanEditionActivity.START_TWISTED_PAIR_CABLE_ACTIVITY_REQUEST:
                if (resultCode == RESULT_OK)
                {
                    setResult(RESULT_OK, data);
                    finish();
                }//else nothing
                break;
        }
    }

    @Override
    public void onComponentSelected(String compGUID, String compHashData, boolean compRecentlyUsed, int compSubcategory, Bitmap componentImageBitmap)
    {
        if (mIsMarkupPictureRequest)
        {
            if (componentImageBitmap != null)
            {
                String componentImage = saveComponentImage(componentImageBitmap);
                Intent intent = new Intent();
                intent.putExtra(PATH_KEY, componentImage);
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        else
        {
            //start the comp detail act
            startComponentDetailActivity(compGUID, compHashData, compRecentlyUsed, compSubcategory);
        }
    }

    protected String saveComponentImage(Bitmap bitmap)
    {
        File generatedFile = ImageUtils.createFileFromBitmap(bitmap);
        MediaManager.storeMediaFile(generatedFile, new DrawingMedia());

        return generatedFile.getName();
    }
}
