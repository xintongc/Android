public class AddAnnotationsActivity{
private static final String PREVIEW_ENABLED = "previewEnabled";
private boolean previewEnabled = false;

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
        previewEnabled = false;
    }
 
public void launchSelectPicture(IbwmIdentity identity, String parType)
    {
        Bundle params = identity.getBundle();
        params.putString(Annotation.PARENT_TYPE_EXTRA, parType);
        params.putString(PicturePreviewActivity.PARENT_ACTIVITY_NAME_KEY, TAG);

        Intent intent = new Intent(this, PictureFolderViewerActivity.class);
        intent.putExtra(PictureFolderViewerActivity.BUNDLE_EXTRA, params);
        intent.putExtra(PictureFolderContentViewerActivity.START_FOR_ANNOTATION_EXTRA, true);
        startActivityForResult(intent, CameraHelper.SELECT_PIC_CODE);
    }
    
    
}
