public class AddAnnotationsActivity{
private static final String PREVIEW_SHOWED = "previewShowed";
private boolean previewShowed = false;

 public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);    
        if (savedInstanceState != null && !savedInstanceState.isEmpty())
        {
            previewEnabled = savedInstanceState.getBoolean(PREVIEW_SHOWED);
        }

    }

protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState); 
        outState.putBoolean(PREVIEW_SHOWED, previewEnabled);
    }


private void disablePreviewImageButton()
    {
        cameraViewWithVideoView.disablePreviewBtnOnPhone();
        previewShowed = false;
    }
    
    
}
