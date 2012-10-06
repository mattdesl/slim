package slim.test.bare;

import de.matthiasmann.twl.ResizableFrame;

public class TestFrame extends ResizableFrame {

    public TestFrame() {
        PreviewWidgets previewWidgets = new PreviewWidgets();
        previewWidgets.setTheme("/previewwidgets_frame");
        addCloseCallback(new Runnable() {
        	public void run() {
        		//
        	}
        });
        
        add(previewWidgets);
        setTitle("Test");
        setTheme("resizableframe");
    }

}
