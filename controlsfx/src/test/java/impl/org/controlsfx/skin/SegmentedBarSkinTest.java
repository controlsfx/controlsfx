package impl.org.controlsfx.skin;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.SegmentedBar;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testfx.api.FxToolkit;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeoutException;
@RunWith(Parameterized.class)
public class SegmentedBarSkinTest {

    @Parameterized.Parameters(name = "{index}: {0} = x: {1}, y: {2} ")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { {Orientation.HORIZONTAL, 100.0, 100.0 }, { Orientation.VERTICAL, 100.0, 200.0  }});
    }

    public SegmentedBarSkinTest(Orientation orientation, double x, double y) {
        this.orientation = orientation;
        this.x = x;
        this.y = y;
    }

    private Orientation orientation;

    private double x;

    private double y;

    @BeforeClass
    public static void setupSpec() throws TimeoutException {
        FxToolkit.registerPrimaryStage();
    }

    @AfterClass
    public static void afterClass() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    @Test
    public void testLayoutChildrenNotRelocatingToNaN() {
        SegmentedBar<SegmentedBar.Segment> control = new SegmentedBar<>();
        control.setOrientation(orientation);
        StackPane segmentView1 = new StackPane();
        StackPane segmentView2 = new StackPane();
        Map<SegmentedBar.Segment, Node> segmentNodes = Map.of(
                new SegmentedBar.Segment(0.0), segmentView1,
                new SegmentedBar.Segment(0.0), segmentView2
        );
        control.getSegments().addAll(segmentNodes.keySet());
        control.setSegmentViewFactory(segmentNodes::get);
        SegmentedBarSkin<SegmentedBar.Segment> segmentedBarSkin = new SegmentedBarSkin<>(control);

        segmentedBarSkin.layoutChildren(100,100,100,100);

        Assert.assertEquals(x, segmentView1.getLayoutX(),0.0);
        Assert.assertEquals(y, segmentView1.getLayoutY(),0.0);

        Assert.assertEquals(x, segmentView2.getLayoutX(),0.0);
        Assert.assertEquals(y, segmentView2.getLayoutY(),0.0);
    }
}