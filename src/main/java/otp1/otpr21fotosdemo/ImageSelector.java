package otp1.otpr21fotosdemo;

import javafx.scene.image.ImageView;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ImageSelector{
    private final double OPACITY_NOT_SELECTED = 0.3;
    private Map<Integer, ImageView> allImageViews;
    private ArrayList<Integer> selectedIds;
    public ImageSelector(){
        allImageViews = new HashMap<>();
        selectedIds = new ArrayList<>();
    }

    public void clearAll() {
        Iterator<Integer> it = allImageViews.keySet().iterator();
        while (it.hasNext()) {
            allImageViews.get(it.next()).setOpacity(1);
        }
        allImageViews.clear();
        selectedIds.clear();
    }

    public void clearSelection(){
        Iterator<Integer> it = allImageViews.keySet().iterator();
        while (it.hasNext()) {
            allImageViews.get(it.next()).setOpacity(1);
        }
        selectedIds.clear();
        System.out.println("Selection cleared.");
    }

    public void addToSelection(int id) {
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

    public void addToAll(int id, ImageView iv) throws Exception{
        if (allImageViews.containsKey(id)){
            throw new Exception("ID " + id + " exists already in allImageViews!");
        }
        allImageViews.put(id,iv);
    }

    public void removeFromSelection(int id){
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

    public boolean isSelected(int id){
        return selectedIds.contains(Integer.valueOf(id));
    }
    public int countSelected(){
        return selectedIds.size();
    }

    public ArrayList<Integer> getSelectedIds() {
        return new ArrayList<Integer>(selectedIds);
    }

    //For testing
    public int countAll(){
        return allImageViews.size();
    }
}
