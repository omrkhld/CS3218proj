package sg.edu.nus.oztrafficcamera;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import sg.edu.nus.data.SensorDBHelper;
import sg.edu.nus.data.SensorsContract;

/**
 * Created by delvinlow on 14/4/16.
 */
public class ThumbnailAdapter extends CursorAdapter{

    private static Context context;
    SensorDBHelper helper;

    public ThumbnailAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        this.context = context;
        helper = new SensorDBHelper(context);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.log_cam_view, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find Views to populate in inflated template
        TextView textViewTimestamp    = (TextView) view.findViewById(R.id.log_cam_textview_timestamp);
        TextView textViewUri    = (TextView) view.findViewById(R.id.log_cam_textview_image_uri);
        ImageView   thumbnail = (ImageView) view.findViewById(R.id.log_cam_thumbnail);

        // Extract properties from cursor
        final String timestamp = cursor.getString(cursor.getColumnIndex(SensorsContract.CameraEntry.COLUMN_TIMESTAMP));
        final String uri = cursor.getString(cursor.getColumnIndex(SensorsContract.CameraEntry.COLUMN_IMAGE_URI));

        // Populate views with extracted properties
        textViewTimestamp.setText(timestamp);
        textViewUri.setText(uri);

//        Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
//                context.getContentResolver(), selectedimageuri,
//                MediaStore.Images.Thumbnails.MINI_KIND,
//                (BitmapFactory.Options) null);

        int THUMBSIZE = 128;
        Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(uri),
                THUMBSIZE,
                THUMBSIZE);
        thumbnail.setImageBitmap(ThumbImage);

    }
}
