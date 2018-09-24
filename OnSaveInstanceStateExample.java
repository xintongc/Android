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
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PicturePreviewActivity.REQUEST_PREVIEW && resultCode == RESULT_CANCELED)
            {
                disablePreviewImageButton();
            }
        }
        else if(requestCode == CameraHelper.SELECT_PIC_CODE && resultCode == RESULT_OK)
        {
            disablePreviewImageButton();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

private void disablePreviewImageButton()
    {
        cameraViewWithVideoView.disablePreviewBtnOnPhone();
        previewShowed = false;
    }
 

    
    
}
