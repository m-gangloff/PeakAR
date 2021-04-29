package com.github.bgabriel998.softwaredevproject.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.github.bgabriel998.softwaredevproject.points.ComputePOIPoints;
import com.github.bgabriel998.softwaredevproject.points.POIPoint;
import com.github.bgabriel998.softwaredevproject.R;

import java.util.List;
import java.util.Map;

/**
 * CompassView draws the compass on the display
 */
public class CameraUiView extends View {
    //Paints used to draw the lines and heading of the compass on the camera-preview
    private Paint mainLinePaint;
    private Paint secondaryLinePaint;
    private Paint terciaryLinePaint;
    private Paint mainTextPaint;
    private Paint mountainInfo;
    private Paint secondaryTextPaint;

    //Colors of the compass-view
    private int compassColor;
    private int mountainInfoColor;

    //Font size of the text
    private int mainTextSize;

    //Max opacity for the paints
    private static final int MAX_ALPHA = 255;

    //Rotation to be applied for the addition info of the mountains
    private static final int LABEL_ROTATION = -45;
    //Offset for the text to be above the marker
    private static final int OFFSET_MOUNTAIN_INFO = 15;

    //Factors for the sizes
    private static final int MAIN_TEXT_FACTOR = 20;
    private static final int SEC_TEXT_FACTOR = 15;
    private static final int MAIN_LINE_FACTOR = 5;
    private static final int SEC_LINE_FACTOR = 3;
    private static final int TER_LINE_FACTOR = 2;

    //Heading of the user
    private float horizontalDegrees;
    private float verticalDegrees;

    //Number of pixels per degree
    private float pixDeg;

    //Range of the for-loop to draw the compass
    private float minDegrees;
    private float maxDegrees;
    private float rangeDegreesVertical;
    private float rangeDegreesHorizontal;

    //Compass canvas
    private Canvas canvas;

    //Heights of the compass
    private int textHeight;
    private int mainLineHeight;
    private int secondaryLineHeight;
    private int terciaryLineHeight;

    //Height of the view in pixel
    private int height;

    //Marker used to display mountains on camera-preview
    private Bitmap mountainMarkerVisible;
    private Bitmap mountainMarkerNotVisible;

    //List that contains the POIPoints
    private List<POIPoint> POIPoints;
    //Map that contains the labeled POIPoints
    private Map<POIPoint, Boolean> labeledPOIPoints;

    /**
     * Constructor for the CompassView which initializes the widges like the font height and paints used
     * @param context Context of the activity on which the camera-preview is drawn
     * @param attrs AttributeSet so that the CompassView can be used from the xml directly
     */
    public CameraUiView(Context context, AttributeSet attrs){
        super(context, attrs);

        widgetInit();
    }

    /**
     * Initializes all needed widgets for the compass like paint variables
     */
    private void widgetInit(){
        //Initialize colors
        compassColor = R.color.Black;
        mountainInfoColor = R.color.Black;

        //Initialize fonts
        float screenDensity = getResources().getDisplayMetrics().scaledDensity;
        mainTextSize = (int) (MAIN_TEXT_FACTOR * screenDensity);

        //Initialize mountain marker that are in line of sight
        mountainMarkerVisible = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_mountain_marker_visible);

        //Initialize mountain marker that are not in line of sight
        mountainMarkerNotVisible = getBitmapFromVectorDrawable(getContext(), R.drawable.ic_mountain_marker_not_visible);

        //Initialize paints
        mountainInfo = new Paint(Paint.ANTI_ALIAS_FLAG);
        mountainInfo.setTextAlign(Paint.Align.LEFT);
        mountainInfo.setTextSize(MAIN_TEXT_FACTOR*screenDensity);
        mountainInfo.setColor(compassColor);
        mountainInfo.setAlpha(MAX_ALPHA);

        //Paint used for the main text heading (N, E, S, W)
        mainTextPaint = configureTextPaint(mainTextSize);

        //Paint used for the secondary text heading (NE, SE, SW, NW)
        secondaryTextPaint = configureTextPaint(SEC_TEXT_FACTOR*screenDensity);

        //Paint used for the main lines (0°, 90°, 180°, ...)
        mainLinePaint = configureLinePaint(MAIN_LINE_FACTOR*screenDensity);

        //Paint used for the secondary lines (45°, 135°, 225°, ...)
        secondaryLinePaint = configureLinePaint(SEC_LINE_FACTOR*screenDensity);

        //Paint used for the terciary lines (15°, 30°, 60°, 75°, 105°, ...)
        terciaryLinePaint = configureLinePaint(TER_LINE_FACTOR*screenDensity);
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        assert drawable != null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Method to create the line paints for the compass
     * @param strokeWidth width of the lines
     * @return configured paint
     */
    private Paint configureLinePaint(float strokeWidth){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(compassColor);
        paint.setAlpha(MAX_ALPHA);
        return paint;
    }

    /**
     * Method to create the text paints for the compass
     * @param textSize size of the text
     * @return configured paint
     */
    private Paint configureTextPaint(float textSize){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(textSize);
        paint.setColor(compassColor);
        paint.setAlpha(MAX_ALPHA);
        return paint;
    }

    /**
     * Set the horizontal and vertical degrees for the compass and markers. When setDegrees is called,
     * it updates the canvas calling invalidate() and requestLayout() which redraws the view.
     * @param horizontalDegrees set the horizontal heading in degrees
     * @param verticalDegrees set the vertical heading in degrees
     */
    public void setDegrees(float horizontalDegrees, float verticalDegrees) {
        this.horizontalDegrees = horizontalDegrees;
        this.verticalDegrees = verticalDegrees;
        invalidate();
        requestLayout();
    }

    /**
     * Sets the range in degrees of the compass-view, corresponds to the field of view of the camera
     * @param cameraFieldOfView Pair containing the horizontal and vertical field of view
     */
    public void setRange(Pair<Float, Float> cameraFieldOfView) {
        int orientation = getResources().getConfiguration().orientation;

        //Switch horizontal and vertical fov depending on the orientation
        this.rangeDegreesHorizontal = orientation==Configuration.ORIENTATION_LANDSCAPE ?
                cameraFieldOfView.first : cameraFieldOfView.second;
        this.rangeDegreesVertical = orientation==Configuration.ORIENTATION_LANDSCAPE ?
                cameraFieldOfView.second : cameraFieldOfView.first;
    }

    /**
     * Set the POIs that will be drawn on the camera-preview
     * @param POIPoints List of POIPoints
     * @param labeledPOIPoints Map of the POIPoints with the line of sight boolean
     */
    public void setPOIs(List<POIPoint> POIPoints, Map<POIPoint, Boolean> labeledPOIPoints){
        this.POIPoints = POIPoints;
        this.labeledPOIPoints = labeledPOIPoints;
        invalidate();
        requestLayout();
    }

    /**
     * onDraw method is used to draw the compass on the screen.
     * To draw the compass, 3 different types of lines are used, mainLinePaint, secondaryLinePaint
     * and terciaryLinePaint. The compass is drawn by going through a for-loop starting from minDegree
     * until maxDegrees. They correspond to the actual heading minus and plus half of the field of view
     * of the device camera.
     * @param canvas Canvas on which the compass is drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.canvas = canvas;
        //Get width and height of the view in Pixels
        int width = getMeasuredWidth();
        height = getMeasuredHeight();
        //Make the canvas take 1/5 of the screen height
        //The text is at the highest point
        textHeight = height - height/5;
        //mainLineHeight is just under the text
        //The text is centered thus we add the textsize divided by 2
        mainLineHeight = textHeight + mainTextSize/2;
        //Then increment each by mainTextSize to get the next line height
        // (the higher the result the lower the line)
        secondaryLineHeight = mainLineHeight + mainTextSize;
        terciaryLineHeight = secondaryLineHeight + mainTextSize;

        //Get the starting degree and ending degree of the compass
        minDegrees = horizontalDegrees - rangeDegreesHorizontal/2;
        maxDegrees = horizontalDegrees + rangeDegreesHorizontal/2;

        //Calculate the width in pixel of one degree
        pixDeg = width/rangeDegreesHorizontal;

        //Draws the compass
        drawCanvas();
    }

    /**
     * Draws the compass on the canvas
     */
    private void drawCanvas(){
        //Start going through the loop to draw the compass
        for(int i = (int)Math.floor(minDegrees); i <= Math.ceil(maxDegrees); i++){
            //Draw the compass
            drawCompass(i);

            //Draw the mountains on the canvas
            drawMountains(i);
        }
    }

    /**
     * Draws the compass on the canvas
     * @param i degree of the compass
     */
    private void drawCompass(int i) {
        //Draw a main line for every 90°
        if (i % 90 == 0){
            //Draw the line,
            //startX starts with 0 at the top left corner and increases when going from left to right
            //startY also increases from top to bottom -> to start at the bottom,
            // we will use the height of the compass-view.
            //stopX is the same as startX since we want to draw a vertical line
            //stopY is the height of the canvas minus some height to leave enough space to write the heading above
            drawLine(i, mainLineHeight, mainLinePaint);

            //Select the correct heading depending on the degree with selecHeadingString()
            //Draw the heading with the mainTextPaint above the line
            canvas.drawText(selectHeadingString(i), pixDeg * (i - minDegrees), textHeight, mainTextPaint);
        }

        //Draw a secondary line for every 45° excluding every 90° (45°, 135°, 225° ...)
        else if (i % 45 == 0){
            drawLine(i, secondaryLineHeight, secondaryLinePaint);
            canvas.drawText(selectHeadingString(i), pixDeg * (i - minDegrees), textHeight, secondaryTextPaint);
        }

        //Draw tertiary line for every 15° excluding every 45° and 90° (15, 30, 60, 75, ...)
        else if (i % 15 == 0){
            drawLine(i, terciaryLineHeight, terciaryLinePaint);
        }
    }

    /**
     * Draws a line on the compass canvas
     * @param degree Degree where the line needs to be drawn
     * @param lineHeight Height of the line in pixel
     * @param paint paint to be used
     */
    private void drawLine(float degree, int lineHeight, Paint paint){
        canvas.drawLine(pixDeg * (degree - minDegrees), height, pixDeg * (degree - minDegrees), lineHeight, paint);
    }

    /**
     * Selects which source needs to be used and then calls either {@link #drawLabeledPOIs(int)}
     * if the map has been downloaded to check if the POIPoint is in the line of sight or not,
     * {@link #drawPOIs(int)} if the map has not been downloaded but the POIPoints are already
     * available or checks if the POIPoints are available.
     * @param i The degree for which the mountains need to be drawn
     */
    private void drawMountains(int i){
        if(labeledPOIPoints != null && !labeledPOIPoints.isEmpty()){
            //Draw the labeled POIs depending on their visibility
            drawLabeledPOIs(i);
        }
        else if(POIPoints != null && !POIPoints.isEmpty()){
            //Draw all POIPoints as not visible since the map has not been downloaded
            drawPOIs(i);
            //Update the labeledPOIPoints
            labeledPOIPoints = ComputePOIPoints.labeledPOIPoints;
        }
        else{
            //Update the POIPoints
            POIPoints = ComputePOIPoints.POIPoints;
        }
    }

    /**
     * Draws the POIs on the display using the horizontal and vertical bearing of the mountain
     * to the user
     * @param actualDegree degree on which the POIPoint is drawn
     */
    private void drawPOIs(int actualDegree){
        //Go through all POIPoints
        for(POIPoint poiPoint : POIPoints){
            if((int)poiPoint.getHorizontalBearing() == (actualDegree + 360) % 360){
                drawMountainMarker(poiPoint, false, actualDegree);
            }
        }
    }

    /**
     * Draws the POIs depending on their visibility on the display using the horizontal and
     * vertical bearing of the mountain to the user
     * @param actualDegree degree on which the POIPoint is drawn
     */
    private void drawLabeledPOIs(int actualDegree){
        //Go through all POIPoints
        labeledPOIPoints.entrySet().stream()
                .filter(p -> (int)p.getKey().getHorizontalBearing() == (actualDegree + 360) % 360)
                        .forEach(p -> drawMountainMarker(p.getKey(), p.getValue(), actualDegree));
    }

    /**
     * Draws the mountain marker on the canvas depending on the visibility of the POIPoint
     * @param poiPoint POIPoint that gets drawn
     * @param isVisible Boolean that indicates if the POIPoint is visible or not
     * @param actualDegree degree on which the POIPoint is drawn
     */
    private void drawMountainMarker(POIPoint poiPoint, Boolean isVisible, int actualDegree){
        //Use both results and substract the actual vertical heading
        float deltaVerticalAngle = (float) (poiPoint.getVerticalBearing() - verticalDegrees);

        //Calculate position in Pixel to display the mountainMarker
        float mountainMarkerPosition = height * (rangeDegreesVertical - 2*deltaVerticalAngle) / (2*rangeDegreesVertical)
                - (float)mountainMarkerVisible.getHeight()/2;

        //Calculate the horizontal position
        float left = pixDeg * (actualDegree - minDegrees);

        //Draw the marker on the preview depending on the line of sight
        Bitmap mountainMarker = isVisible ? mountainMarkerVisible : mountainMarkerNotVisible;
        mountainInfo.setColor(isVisible ? getResources().getColor(mountainInfoColor, null) : compassColor);
        mountainInfo.setAlpha(MAX_ALPHA);

        canvas.drawBitmap(mountainMarker, left, mountainMarkerPosition, null);

        //Save status before Screen Rotation
        canvas.save();
        canvas.rotate(LABEL_ROTATION, left, mountainMarkerPosition);
        canvas.drawText(poiPoint.getName() + " " + poiPoint.getAltitude() + "m",
                left + mountainInfo.getTextSize() - OFFSET_MOUNTAIN_INFO,
                mountainMarkerPosition + mountainInfo.getTextSize() + OFFSET_MOUNTAIN_INFO,
                mountainInfo);
        //Restore the saved state
        canvas.restore();
    }

    /**
     * Used to get the string of the actual heading
     * @param degree degree to get the string
     * @return String for the degree
     */
    String selectHeadingString(int degree){
        switch (degree){
            case 0: case 360:
                return "N";
            case 90: case 450:
                return "E";
            case -180: case 180:
                return "S";
            case -90: case 270:
                return "W";
            case 45: case 405:
                return "NE";
            case -45: case 315:
                return "NW";
            case 135: case 495:
                return "SE";
            case -135: case 225:
                return "SW";
            default:
                return "";
        }
    }

    /**
     * Get a bitmap of the compass-view
     * @return a bitmap of the compass-view
     */
    public Bitmap getBitmap(){
        CameraUiView cameraUiView = findViewById(R.id.compass);
        cameraUiView.setDrawingCacheEnabled(true);
        cameraUiView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(cameraUiView.getDrawingCache());
        cameraUiView.setDrawingCacheEnabled(false);
        return bitmap;
    }
}