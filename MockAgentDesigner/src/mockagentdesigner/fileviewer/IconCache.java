package mockagentdesigner.fileviewer;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.util.*;

/**
 * Manages icons for the application.
 * This is necessary as we could easily end up creating thousands of icons
 * bearing the same image.
 */
class IconCache {
	// Stock images
	public final int
		shellIcon = 0,
		iconClosedDrive = 1,
		iconClosedFolder = 2,
		iconOpenDrive = 3,
		iconOpenFolder = 4,
		cmdParent = 5,
		cmdRefresh = 6;
	public final String[] stockImageLocations = {
		"generic_example.gif",
		"icon_ClosedDrive.gif",
		"icon_ClosedFolder.gif",
		"icon_OpenDrive.gif",
		"icon_OpenFolder.gif",
		"cmd_Parent.gif",
		"cmd_Refresh.gif",

	};
	public Image stockImages[];
	
	// Stock cursors
	public final int
		cursorDefault = 0,
		cursorWait = 1;
	public Cursor stockCursors[];
	// Cached icons
	private Hashtable<Object, Object> iconCache; /* map Program to Image */
	
	public IconCache() {
	}
	/**
	 * Loads the resources
	 * 
	 * @param display the display
	 */
	public void initResources(Display display) {
		if (stockImages == null) {
			stockImages = new Image[stockImageLocations.length];
				
			for (int i = 0; i < stockImageLocations.length; ++i) {
				Image image = createStockImage(display, stockImageLocations[i]);
				if (image == null) {
					freeResources();
					throw new IllegalStateException(
						FileViewer.getResourceString("error.CouldNotLoadResources"));
				}
				stockImages[i] = image;
			}
		}	
		if (stockCursors == null) {
			stockCursors = new Cursor[] {
				null,
				new Cursor(display, SWT.CURSOR_WAIT)
			};
		}
		iconCache = new Hashtable<Object, Object>();
	}
	/**
	 * Frees the resources
	 */
	public void freeResources() {
		if (stockImages != null) {
			for (int i = 0; i < stockImages.length; ++i) {
				final Image image = stockImages[i];
				if (image != null) image.dispose();
			}
			stockImages = null;
		}
		if (iconCache != null) {
			for (Enumeration<Object> it = iconCache.elements(); it.hasMoreElements(); ) {
				Image image = (Image) it.nextElement();
				image.dispose();
			}
		}
		if (stockCursors != null) {
			for (int i = 0; i < stockCursors.length; ++i) {
				final Cursor cursor = stockCursors[i];
				if (cursor != null) cursor.dispose();
			}
			stockCursors = null;
		}
	}
	/**
	 * Creates a stock image
	 * 
	 * @param display the display
	 * @param path the relative path to the icon
	 */
	private Image createStockImage(Display display, String path) {
		InputStream stream = IconCache.class.getResourceAsStream (path);
		ImageData imageData = new ImageData (stream);
		ImageData mask = imageData.getTransparencyMask ();
		Image result = new Image (display, imageData, mask);
		try {
			stream.close ();
		} catch (IOException e) {
			e.printStackTrace ();
		}
		return result;
	}

}
