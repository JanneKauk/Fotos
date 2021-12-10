package otp1.otpr21fotosdemo;


import javafx.scene.image.ImageView;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 * ImageSelector class handles the selection of images in current view. It keeps a list of selected image ID:s and changes the appearance of selected and not selected image-thumbnails.
 * @author Petri Immonen
 */
public class ImageSelector{
    private final double OPACITY_NOT_SELECTED = 0.3;
    private Map<Integer, ImageView> allImageViews;
    private ArrayList<Integer> selectedIds;
    /**
     * Constructor merely initializes the internal collections.
     */
    public ImageSelector(){
        allImageViews = new HashMap<>();
        selectedIds = new ArrayList<>();
    }
    /**
     * Clears selected images and all imageviews in current view. Used when the current view changes to e.g. another folder.
     */
    public void clearAll() {
        Iterator<Integer> it = allImageViews.keySet().iterator();
        while (it.hasNext()) {
            allImageViews.get(it.next()).setOpacity(1);
        }
        allImageViews.clear();
        selectedIds.clear();
    }
    /**
     * Clears the list of selected imageID:s and changes se thumbnail appearance back to "not selected".
     */
    public void clearSelection(){
        Iterator<Integer> it = allImageViews.keySet().iterator();
        while (it.hasNext()) {
            allImageViews.get(it.next()).setOpacity(1);
        }
        selectedIds.clear();
        System.out.println("Selection cleared.");
    }
    /**
     * Adds an imageID to selection and changes the thumbnails appearance accordingly. If the image is first to be selected, then changes the appearance of all not selected imageviews.
     * @param id ID of the image to be added to selection.
     */
    public void addToSelection(int id) {
        if (isSelected(id))
            return;
        if (selectedIds.size() == 0) {
            Iterator<Integer> it = allImageViews.keySet().iterator();
            while (it.hasNext()) {
                int i = it.next();
                if (i != id)
                    allImageViews.get(i).setOpacity(OPACITY_NOT_SELECTED);

            }

        } else {
            allImageViews.get(id).setOpacity(1);
        }
        selectedIds.add(id);
        StringBuilder b = new StringBuilder();
        for (Integer i : selectedIds){
            b.append(i + " ");
        }
        System.out.println("Selected id:s : "+ b.toString());
    }
    /**
     * Adds imageview reference and its ID to internal map of all imageviews in current view.
     * @param id imageID of the image to be added.
     * @param iv reference to the thumbnail imageview.
     * @throws Exception if <i>id</i> already exist in the internal map of images in current view.
     */
    public void addToAll(int id, ImageView iv) throws Exception{
        if (allImageViews.containsKey(id)){
            throw new Exception("ID " + id + " exists already in allImageViews!");
        }
        allImageViews.put(id,iv);
    }

    /**
     * Removes an imageID from selection and changes the thumbnails appearance accordingly. If the image is last selected image, then changes the appearance of all not selected imageviews.
     * @param id ID of the image to be removed from selection
     */
    public void removeFromSelection(int id){
        if (!isSelected(id))
            return;
        if (selectedIds.size() == 1) {
            Iterator<Integer> it = allImageViews.keySet().iterator();
            while (it.hasNext()) {
                allImageViews.get(it.next()).setOpacity(1);
            }

        } else {
            allImageViews.get(id).setOpacity(OPACITY_NOT_SELECTED);
        }

        selectedIds.remove(Integer.valueOf(id));

        StringBuilder b = new StringBuilder();
        for (Integer i : selectedIds){
            b.append(i + " ");
        }
        System.out.println("Selected id:s : "+ b.toString());
    }

    /**
     * Check whether the image is selected or not.
     * @param id ID of the image in question.
     * @return true if the list of selected ID:s contains the <i>id</i>.
     */
    public boolean isSelected(int id){
        return selectedIds.contains(Integer.valueOf(id));
    }

    /**
     * Counts the number of selected images.
     * @return the number of selected images.
     */
    public int countSelected(){
        return selectedIds.size();
    }

    /**
     * Gives a list of selected ID:s
     * @return ArrayList&#60;Integer&#62; containing ID:s of the selected images.
     */
    public ArrayList<Integer> getSelectedIds() {
        return new ArrayList<Integer>(selectedIds);
    }

    /**
     * For testing
     * @return number of images in view.
     */
    public int countAll(){
        return allImageViews.size();
    }
}
