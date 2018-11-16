public class SignOffActivity extends IBWaveBrandedActivity implements ISignOffActivityContract.IView
{
    private boolean editable = false;
    private EditText textEditor;
    private EditText signOffTitle;
    private Menu menu;
    private SignOffActivityPresenter signOffActivityPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_off);
        initPresenter();
        initEditText();
        initToolBar();
        if(savedInstanceState != null)
        {
            editable = savedInstanceState.getBoolean("editable");
        }
        if(!editable)
        {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if(menu.findItem(R.id.sign_off_edit_button) != null)
        {
            updateViews(menu.findItem(R.id.sign_off_edit_button));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onCancelPressed();
                return true;
            case R.id.sign_off_paste_button:
                pasteClipBoard();
                return true;
            case R.id.sign_off_edit_button:
                editable = !editable;
                updateViews(item);
                if (!editable)
                    this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.sign_off_edit_menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putBoolean("editable",editable);
        super.onSaveInstanceState(outState);
    }

    private void onCancelPressed()
    {
        if (hasChanges())
            showSaveDialog();
        else
            this.finish();
    }

    public void showSaveDialog()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(R.string.save_changes_alert_message);
        alert.setPositiveButton(R.string.save, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int i)
            {
                saveSignOff();
                dialog.dismiss();
                SignOffActivity.this.finish();
            }
        });
        alert.setNegativeButton(R.string.discard, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int i)
            {
                dialog.dismiss();
                SignOffActivity.this.finish();
            }
        });
        alert.setCancelable(true);
        alert.show();
    }

    @Override
    public void onBackPressed()
    {
        onCancelPressed();
    }

    private boolean hasChanges()
    {
        if (textEditor.getText().toString().compareTo(signOffActivityPresenter.getSignOffText()) != 0 || signOffTitle.getText().toString().compareTo(signOffActivityPresenter.getSignOffTitle()) != 0)
            return true;
        else
            return false;
    }

    private void initPresenter()
    {
        signOffActivityPresenter = new SignOffActivityPresenter();
    }

    private void initEditText()
    {
        textEditor = (EditText)findViewById(R.id.sign_off_text_editor);
        signOffTitle = (EditText)findViewById(R.id.sign_off_title);
        textEditor.setText(signOffActivityPresenter.getSignOffText());
        signOffTitle.setText(signOffActivityPresenter.getSignOffTitle());
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null)
            {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void initToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.sign_off_action_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
            actionBar.setTitle("");
        }
    }

    private void saveSignOff()
    {
        signOffActivityPresenter.setSignOffText(textEditor.getText().toString());
        signOffActivityPresenter.setSignOffTitle(signOffTitle.getText().toString());
    }

    private void updateViews(MenuItem item)
    {
        textEditor.setSelectAllOnFocus(editable);
        signOffTitle.setSelectAllOnFocus(editable);
        textEditor.setEnabled(editable);
        signOffTitle.setEnabled(editable);
        if(editable)
        {
            item.setIcon(R.drawable.ic_save_black_48dp);
            showSoftKeyboard(signOffTitle);
            signOffTitle.selectAll();
        }
        else
        {
            saveSignOff();
            item.setIcon(R.drawable.ic_edit_48dp);
            textEditor.setSelection(0);
            signOffTitle.setSelection(0);
        }
        if(menu.findItem(R.id.sign_off_paste_button) != null)
        {
            menu.findItem(R.id.sign_off_paste_button).setVisible(editable);
        }

    }

    private void pasteClipBoard()
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboard != null)
        {
            if (clipboard.hasPrimaryClip())
            {
                ClipData clipData = clipboard.getPrimaryClip();
                String pasteData = clipData.getItemAt(0).getText().toString();
                if (getCurrentFocus() != null)
                {
                    EditText currentText = (EditText)getCurrentFocus();
                    pasteData = currentText.getText() + pasteData;
                    currentText.setText(pasteData);
                    currentText.setSelection(currentText.getText().length());
                }
            }
            else
                showToast();
        }
        else
            showToast();

    }

    private void showToast()
    {
        Toast toast = Toast.makeText(this, getResources().getString(R.string.settings_screen_empty_clipboard), Toast.LENGTH_LONG);
        toast.show();
    }
}
