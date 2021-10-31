package otp1.otpr21fotosdemo;

import javafx.scene.image.ImageView;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImageSelectorTest {
    private ImageSelector selector;
    @BeforeAll
    public void initialize() {
        selector = new ImageSelector();
    }

    @Test
    @DisplayName("Testataan addToAll(int, ImageView)")
    @Order(1)
    public void addToAllTest() {
        ImageView iv1 = new ImageView();
        ImageView iv2 = new ImageView();
        ImageView iv3 = new ImageView();
        ImageView iv4 = new ImageView();
        assertDoesNotThrow(() -> {
            selector.addToAll(11, iv1);
            selector.addToAll(12, iv2);
            selector.addToAll(13, iv3);
            selector.addToAll(14, iv4);
        });
        assertEquals(4, selector.countAll(), "countAll should return number of ImageViews in selector. Should be 4 in this case");
        //addToAll should throw exception if trying to add a duplicate ID
        assertThrows(Exception.class, () -> {
            selector.addToAll(11, iv1);
        });
    }
    @Test
    @DisplayName("Testataan addToSelection(int)")
    @Order(2)
    public void addToSelectionTest() {
        assertDoesNotThrow(() -> {
            selector.addToSelection(11);
            selector.addToSelection(13);
        });
        assertEquals(2, selector.countSelected(), "countSelected should return number of selected ImageViews. Should be 2 in this case");
    }
    @Test
    @DisplayName("Testataan isSelected(int)")
    @Order(3)
    public void isSelectedTest() {
        assertTrue(selector.isSelected(11), "isSelected should return true in this case");
    }
    @Test
    @DisplayName("Testataan getSelectedIds()")
    @Order(4)
    public void getSelectedIdsTest() {

        assertDoesNotThrow(() -> {
            ArrayList<Integer> list = selector.getSelectedIds();
            assertEquals(2, list.size(), "getSelectedIds should return arraylist with size of 2 in this case.");
            assertTrue(list.contains(Integer.valueOf(11)));
            assertTrue(list.contains(Integer.valueOf(13)));
        });
    }
    @Test
    @DisplayName("Testataan removeFromSelection(int)")
    @Order(5)
    public void removeFromSelectionTest() {

        assertTrue(selector.isSelected(11));
        assertDoesNotThrow(() -> {
            selector.removeFromSelection(11);
        });
        assertFalse(selector.isSelected(11));
        assertDoesNotThrow(() -> {
            selector.removeFromSelection(13);
        });
        assertFalse(selector.isSelected(13));
    }
    @Test
    @DisplayName("Testataan clearSelection()")
    @Order(6)
    public void clearSelectionTest() {
        assertDoesNotThrow(() -> {
            selector.addToSelection(11);
            selector.addToSelection(12);
        });
        assertEquals(2, selector.countSelected(), "countSelected should return 2 in this case.");
        assertDoesNotThrow(() -> {
            selector.clearSelection();
        });
        assertEquals(0, selector.countSelected(), "countSelected should return 0 in this case.");

    }
    @Test
    @DisplayName("Testataan clearAll")
    @Order(7)
    public void clearAllTest() {
        assertDoesNotThrow(() -> {
            selector.clearAll();
        });
        assertEquals(0, selector.countAll(), "countAll should return number of ImageViews in selector. Should be 0 in this case");
    }




}
