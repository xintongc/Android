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

}
