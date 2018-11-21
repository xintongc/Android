package com.ibwave.utils.androidapp.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;

import com.ibwave.utils.androidapp.dialogs.validators.IEditNameDialogListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link DialogFragment} that implements a {@link AlertDialog} for simple choice selection.
 * You may create a subclass to customize the behavior of the dialog, or extend the builder that
 * creates the dialog to add more parameters.
 */
public class IBWaveSingleChoiceDialog extends IBWaveDialogFragment
{
    public static final String TAG = "IBWaveSingleChoiceDialog";

    private static final String KEY_TITLE = "title";
    private static final String KEY_SELECTED_ITEM = "selectedItem";

    private List<CharSequence> choices;
    private SingleChoiceDialogListener singleChoiceDialogListener;

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreateDialog(savedInstanceState);

        Bundle bundle = getArguments();
        String title = bundle.getString(KEY_TITLE, "");
        int selectedItem = bundle.getInt(KEY_SELECTED_ITEM, -1);

        AlertDialog.Builder alert = createAlertDialogBuilder();
        alert.setTitle(title);
        CharSequence[] stockArr = new CharSequence[choices.size()];
        stockArr = choices.toArray(stockArr);
        alert.setSingleChoiceItems(stockArr, selectedItem, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dismiss();
                singleChoiceDialogListener.onItemClick(choices.get(which).toString(), which);
            }
        });

        return alert.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putCharSequenceArrayList(SAVED_CHOICES_LIST, (ArrayList<CharSequence>) choices);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && !savedInstanceState.isEmpty())
        {
            choices = savedInstanceState.getCharSequenceArrayList(SAVED_CHOICES_LIST);
        }

    }

    public void setListener(SingleChoiceDialogListener listener)
    {
        this.singleChoiceDialogListener = listener;
    }

    /**
     * A class should implement this interface in order to be notified when the user is done with
     * this dialog.
     *
     * @see IBWaveSingleChoiceDialog.Builder#setListener(SingleChoiceDialogListener)
     */
    public static abstract class SingleChoiceDialogListener
    {
        public abstract void onItemClick(String selectedStringValue, int position);
    }

    /**
     * This class partially implements {@link IEditNameDialogListener}
     * No action is performed when the user discards the dialog by clicking Cancel or Back buttons.
     */
    public static class Builder
    {
        protected Bundle args;
        protected SingleChoiceDialogListener singleChoiceDialogListener;
        protected List<CharSequence> choices;

        /**
         * Default constructor for this builder.
         */
        public Builder()
        {
            args = new Bundle();
        }

        /**
         * Set the list of choices.
         *
         * @param c list of choices, as text strings.
         * @return this instance of the Builder
         */
        public Builder setChoices(List<CharSequence> c)
        {
            choices = c;
            return this;
        }

        /**
         * Assign a title to the dialog.
         *
         * @param title the title of the dialog
         * @return this instance of the Builder
         */
        public Builder setTitle(String title)
        {
            args.putString(KEY_TITLE, title);
            return this;
        }

        /**
         * Set which item is currently set.
         *
         * @param selectedItem the position of the item in the list
         * @return this instance of the Builder
         */
        public Builder setSelectedItem(int selectedItem)
        {
            args.putInt(KEY_SELECTED_ITEM, selectedItem);
            return this;
        }

        /**
         * Set the listener to notify when the user is done with this dialog prompt.
         *
         * @param l listener
         * @return this instance of the Builder
         */
        public Builder setListener(SingleChoiceDialogListener l)
        {
            singleChoiceDialogListener = l;
            return this;
        }

        public Builder setArguments(Bundle args)
        {
            this.args.putAll(args);
            return this;
        }

        /**
         * Show the dialog.
         *
         * @param fragmentManager the fragment manager initiating this dialog.
         */
        public void show(FragmentManager fragmentManager)
        {
            IBWaveSingleChoiceDialog IBWaveSingleChoiceDialog = new IBWaveSingleChoiceDialog();
            IBWaveSingleChoiceDialog.setArguments(args);
            IBWaveSingleChoiceDialog.choices = choices;
            IBWaveSingleChoiceDialog.singleChoiceDialogListener = singleChoiceDialogListener;
            IBWaveSingleChoiceDialog.show(fragmentManager, IBWaveSingleChoiceDialog.TAG);
        }
    }
}

package com.ibwave.ibwavemobile;

import android.content.res.Resources;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.ibwave.ibwavemobile.entities.smallcell.CustomPropertyType;
import com.ibwave.ibwavemobile.entities.smallcell.CustomPropertiesChoice;
import com.ibwave.ibwavemobile.entities.smallcell.CustomProperty;
import com.ibwave.ibwavemobile.entities.smallcell.SmallCellPart;
import com.ibwave.utils.androidapp.dialogs.IBWaveEditMacAddressDialog;
import com.ibwave.utils.androidapp.dialogs.IBWaveEditValueDialog;
import com.ibwave.utils.androidapp.dialogs.IBWaveSingleChoiceDialog;
import com.ibwave.utils.androidapp.dialogs.IBWaveSingleChoiceWithEditTextDialog;
import com.ibwave.utils.androidapp.dialogs.validators.NoActionOnCancelListener;

import java.util.ArrayList;
import java.util.List;

public class ComponentPropertiesRecyclerViewAdapter extends RecyclerView.Adapter<ComponentPropertiesViewHolder>
{
    private static final String TRUE = "1";
    private static final String FALSE = "0";

    private List<? extends CustomProperty> customProperties;
    private SmallCellPart mComponent;
    private FragmentManager fragmentManager;

    public ComponentPropertiesRecyclerViewAdapter(FragmentManager fm, List<? extends CustomProperty> properties, SmallCellPart component)
    {
        fragmentManager = fm;
        customProperties = properties;
        mComponent = component;
    }

    @Override
    public ComponentPropertiesViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v;

        if (viewType == CustomPropertyType.BOOL.getType())
        {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_custom_properties_boolean_list_layout, parent, false);
            return new ComponentPropertiesBooleanViewHolder(v);
        }
        else
        {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_custom_properties_list_layout, parent, false);
            return new ComponentPropertiesViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(ComponentPropertiesViewHolder holder, int position)
    {
        CustomProperty componentProperty = customProperties.get(position);
        CustomPropertyType type = componentProperty.getType();
        String mode = componentProperty.getMode();

        boolean isReadWrite = mode == null || mode.isEmpty() || mode.equalsIgnoreCase(CustomProperty.MODE_READ_WRITE);

        holder.title.setText(componentProperty.getFullName());
        holder.title.setEnabled(isReadWrite);

        final int pos = position;
        if (type == CustomPropertyType.BOOL)
        {
            ComponentPropertiesBooleanViewHolder booleanHolder = (ComponentPropertiesBooleanViewHolder) holder;
            booleanHolder.value.setOnCheckedChangeListener(null);
            booleanHolder.value.setChecked(getCheckedValue(componentProperty.getValue()));
            booleanHolder.value.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    updateComponentPropertyBoolean(pos, isChecked);
                }
            });
            booleanHolder.value.setClickable(isReadWrite);
            booleanHolder.value.setEnabled(isReadWrite);
        }
        else
        {
            holder.value.setVisibility(View.GONE);
            if (componentProperty.format() != null && !componentProperty.format().isEmpty())
            {
                holder.value.setText(componentProperty.format());
                holder.value.setVisibility(View.VISIBLE);
            }
            else if (componentProperty.getValue() != null && !componentProperty.getValue().isEmpty())
            {
                holder.value.setText(componentProperty.getValue());
                holder.value.setVisibility(View.VISIBLE);
            }

            holder.value.setEnabled(isReadWrite);

            if (isReadWrite)
            {
                holder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onItemEdit(pos);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount()
    {
        if (customProperties == null)
        {
            return 0;
        }
        return customProperties.size();
    }

    public int getItemViewType(int position)
    {
        CustomProperty property = customProperties.get(position);
        return property.getType().getType();
    }

    private boolean getCheckedValue(String value)
    {
        return value != null && value.equals(TRUE);
    }

    private String convertCheckedValue(boolean value)
    {
        return value ? TRUE : FALSE;
    }

    private void updateComponentProperty(int position, String newValue)
    {
        CustomProperty property = customProperties.get(position);
        CustomPropertyType type = property.getType();
        Resources resources = iBwaveMobileApplication.get().getResources();

        if (property.getFullName().equalsIgnoreCase(resources.getString(R.string.component_selection_serial)) && mComponent != null)
        {
            mComponent.setSerialNumber(newValue);
        }
        else if (property.getFullName().equalsIgnoreCase(resources.getString(R.string.component_selection_sector)) && mComponent != null)
        {
            onEditSector(newValue);
        }
        else if ((type.equals(CustomPropertyType.DECIMAL) || type.equals(CustomPropertyType.INTEGER)) && newValue.isEmpty())
        {
            newValue = property.getValue();
        }

        property.setValue(newValue);
        notifyItemChanged(position);
    }

    private void updateComponentPropertyBoolean(int position, boolean newValue)
    {
        CustomProperty property = customProperties.get(position);
        property.setValue(convertCheckedValue(newValue));
    }

    private void onItemEdit(int position)
    {
        CustomProperty property = customProperties.get(position);
        if (property.getType().equals(CustomPropertyType.MAC))
        {
            showMacAddressDialog(position);
        }
        else if (property.getAllowCustom() != null && property.getAllowCustom())
        {
            showSelectionListPlusEditDialog(property, position);
        }
        else if (property.getType().equals(CustomPropertyType.CHOICES))
        {
            showChoiceDialog(property, position);
        }
        else
        {
            showEditComponentPropertyDialog(property, position);
        }
    }

    private void showMacAddressDialog(final int position)
    {
        CustomProperty property = customProperties.get(position);

        IBWaveEditMacAddressDialog.Builder dialogBuilder = new IBWaveEditMacAddressDialog.Builder();
        dialogBuilder.setTitle(property.getFullName());
        dialogBuilder.setValue(property.getValue());
        dialogBuilder.setHint(property.getName());
        dialogBuilder.allowEmptyString(true);
        dialogBuilder.setListener(new NoActionOnCancelListener()
        {
            @Override
            public void onPositiveButtonClicked(String newValue)
            {
                updateComponentProperty(position, newValue);
            }
        });
        dialogBuilder.show(fragmentManager);
    }

    private void showEditComponentPropertyDialog(CustomProperty property, final int position)
    {
        int inputType = getInputType(property);

        (new IBWaveEditValueDialog.Builder())
                .setTitle(property.getFullName())
                .setValue(property.getValue())
                .setHint(property.getName())
                .setInputType(inputType)
                .setSingleLine(property.getType() != CustomPropertyType.MULTILINE)
                .setCustomValues(property.getValidations())
                .setUnits(property.getDisplayUnits())
                .allowEmptyString((inputType & InputType.TYPE_CLASS_TEXT) == InputType.TYPE_CLASS_TEXT)
                .setListener(new NoActionOnCancelListener()
                {
                    @Override
                    public void onPositiveButtonClicked(String newName)
                    {
                        updateComponentProperty(position, newName);
                    }
                }).show(fragmentManager);
    }

    private void showChoiceDialog(CustomProperty property, final int pos)
    {
        List<CharSequence> sequences = convertChoicesToCharSequence(property.getChoiceList());
        (new IBWaveSingleChoiceDialog.Builder()
                .setTitle(property.getFullName())
                .setChoices(sequences)
                .setSelectedItem(getChoicePosition(property))
                .setListener(new IBWaveSingleChoiceDialog.SingleChoiceDialogListener() {
                    @Override
                    public void onItemClick(String selectedStringValue, int position)
                    {
                        updateComponentProperty(pos, selectedStringValue);
                    }
                })).show(fragmentManager);
    }

    private List<CharSequence> convertChoicesToCharSequence(List<CustomPropertiesChoice> choices)
    {
        List<CharSequence> result = new ArrayList<>();

        if (choices != null)
        {
            for (CustomPropertiesChoice choice : choices)
            {
                result.add(choice.getValue());
            }
        }

        return result;
    }

    private int getChoicePosition(CustomProperty property)
    {
        if (property.getValue() != null && property.getChoiceList() != null && !property.getChoiceList().isEmpty())
        {
            String value = property.getValue();
            int i = 0;
            for (CustomPropertiesChoice choice : property.getChoiceList())
            {
                if (value.equalsIgnoreCase(choice.getValue()))
                {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    private int getInputType(CustomProperty property)
    {
        CustomPropertyType type = property.getType();
        switch (type)
        {
            case TEXT:
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
            case MULTILINE:
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            case INTEGER:
                return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
            case DECIMAL:
                return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL;
            case DATE:
                return InputType.TYPE_CLASS_DATETIME;
            case IP:
                return InputType.TYPE_CLASS_PHONE;
            case CAPS:
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
            default:
                return InputType.TYPE_CLASS_TEXT;
        }
    }

    private void onEditSector(String sector)
    {
        int sectorNumber;
        try
        {
            if (!sector.isEmpty())
            {
                sectorNumber = Integer.valueOf(sector);
            }
            else
            {
                sectorNumber = 0;
            }
            if (sectorNumber == 0)
            {
                sectorNumber = -1;
            }
        }
        catch (Exception e)
        {
            sectorNumber = -1;
        }
        mComponent.setSectorNumber(sectorNumber);
    }

    private void showSelectionListPlusEditDialog(CustomProperty property, final int pos)
    {
        List<CharSequence> sequences = convertChoicesToCharSequence(property.getChoiceList());
        int propertyChoicePosition = getChoicePosition(property);
        String customChoiceValue = getCustomChoiceValue(propertyChoicePosition, property.getValue());

        (new IBWaveSingleChoiceWithEditTextDialog.Builder()
                .setTitle(property.getFullName())
                .setChoices(sequences)
                .setSelectedItem(propertyChoicePosition)
                .setCustomPropertyChoice(customChoiceValue)
                .setValidations(property.getValidations())
                .setListener(new IBWaveSingleChoiceWithEditTextDialog.SingleChoiceDialogListener() {
                    @Override
                    public void onItemClick(String selectedStringValue, int position)
                    {
                        updateComponentProperty(pos, selectedStringValue);
                    }
                })
        ).show(fragmentManager);
    }

    private String getCustomChoiceValue(int propertyChoicePosition, String value)
    {
        if (propertyChoicePosition == -1 && value != null && !value.isEmpty())
        {
            return value;
        }

        return null;
    }
}
package com.ibwave.ibwavemobile.entities.smallcell;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Created by agidaro on 3/17/2017.
 */

public class CustomPropertiesChoice implements Parcelable
{
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public CustomPropertiesChoice createFromParcel(Parcel in)
        {
            return new CustomPropertiesChoice(in);
        }

        public CustomPropertiesChoice[] newArray(int size)
        {
            return new CustomPropertiesChoice[size];
        }
    };
    private Integer id;
    private Integer customPropertyId;
    private String value;

    public CustomPropertiesChoice(){}

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public void setCustomPropertyId(Integer customPropertyId)
    {
        this.customPropertyId = customPropertyId;
    }

    public Integer getCustomPropertyId()
    {
        return customPropertyId;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public CustomPropertiesChoice(Parcel in)
    {
        id = in.readInt();
        customPropertyId = in.readInt();
        value = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(customPropertyId);
        dest.writeString(value);
    }
}

