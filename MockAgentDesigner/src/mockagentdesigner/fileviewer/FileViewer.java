package mockagentdesigner.fileviewer;

import mockagentdesigner.actions.CreateNewFolderDialog;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.program.*;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * File Viewer example
 */
public class FileViewer { 
 	private static ResourceBundle resourceBundle = ResourceBundle.getBundle("mockagentdesigner_fileviewer");

	private final static String DRIVE_A = "a:" + File.separator;
	private final static String DRIVE_B = "b:" + File.separator;

	/* UI elements */ 	
	private Display display; 
	private Shell shell;
	private ToolBar toolBar;

	private Label numObjectsLabel;
	
	private File currentDirectory = null;
	private boolean initial = true;
	
	/* Drag and drop optimizations */
	private boolean isDragging = false; // if this app is dragging
	private boolean isDropping = false; // if this app is dropping

	private File[]  deferredRefreshFiles = null;      // to defer notifyRefreshFiles while we do DND
	private boolean deferredRefreshRequested = false; // to defer notifyRefreshFiles while we do DND


	/* Combo view */
	private static final String COMBODATA_ROOTS = "Combo.roots";
		// File[]: Array of files whose paths are currently displayed in the combo
	private static final String COMBODATA_LASTTEXT = "Combo.lastText";
		// String: Previous selection text string

	private Combo combo;
	
	private Button buttonOK, buttonCreateNewFolder, buttonCancel;

	/* Tree view */
	private IconCache iconCache = new IconCache();
	private static final String TREEITEMDATA_FILE = "TreeItem.file";
		// File: File associated with tree item
	private static final String TREEITEMDATA_IMAGEEXPANDED = "TreeItem.imageExpanded";
		// Image: shown when item is expanded
	private static final String TREEITEMDATA_IMAGECOLLAPSED = "TreeItem.imageCollapsed";
		// Image: shown when item is collapsed
	private static final String TREEITEMDATA_STUB = "TreeItem.stub";
		// Object: if not present or null then the item has not been populated

	private Tree tree;
	private Label treeScopeLabel;

	/* Table update worker */
	// Control data
	private final Object workerLock = new Object();
	
	// Lock for all worker control data and state
	private volatile Thread  workerThread = null;
	
	private String directoryPath;

	/**
	 * @param directoryPath the directoryPath to set
	 */
	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}
	/**
	 * @return the directoryPath
	 */
	public String getDirectoryPath() {
		return directoryPath;
	}
	
	/**
	 * Runs main program.
	 */
	public static void main (String [] args) {
		Display display = new Display ();
		FileViewer application = new FileViewer();
		Shell shell = application.open(display);
		while (! shell.isDisposed()) {
			if (! display.readAndDispatch()) display.sleep();
		}
		application.close();
		display.dispose();
	}

	/**
	 * Opens the main program.
	 */
	public Shell open(Display display) {		
		// Create the window
		this.display = display;
		iconCache.initResources(display);
		shell = new Shell();
		shell.setSize(400,500);
		createShellContents();
		notifyRefreshFiles(null);
		shell.open();
		return shell;
	}

	/**
	 * Closes the main program.
	 */
	public void close() {
		workerStop();
		iconCache.freeResources();
	}
	
	/**
	 * Returns a string from the resource bundle.
	 * We don't want to crash because of a missing String.
	 * Returns the key if not found.
	 */
	static String getResourceString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}			
	}

	/**
	 * Returns a string from the resource bundle and binds it
	 * with the given arguments. If the key is not found,
	 * return the key.
	 */
	static String getResourceString(String key, Object[] args) {
		try {
			return MessageFormat.format(getResourceString(key), args);
		} catch (MissingResourceException e) {
			return key;
		} catch (NullPointerException e) {
			return "!" + key + "!";
		}
	}

	/**
	 * Construct the UI
	 * 
	 * @param container the ShellContainer managing the Shell we are rendering inside
	 */
	private void createShellContents() {

		shell.setText(getResourceString("Title", new Object[] { "" }));	
		shell.setImage(iconCache.stockImages[iconCache.shellIcon]);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		shell.setLayout(gridLayout);

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.widthHint = 185;
		createComboView(shell, gridData);
		
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		createToolBar(shell, gridData);
		
		SashForm sashForm = new SashForm(shell, SWT.NONE);
		sashForm.setOrientation(SWT.HORIZONTAL);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gridData.horizontalSpan = 3;
		sashForm.setLayoutData(gridData);
		
		createTreeView(sashForm);
	
		numObjectsLabel = new Label(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gridData.widthHint = 185;
		numObjectsLabel.setLayoutData(gridData);
		
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		createOptionsGroup(shell, gridData);
	}	
	
    /**
	 * Creates the "Options" group.
	 * 
	 * @param parent the parent control
	*/
	private void createOptionsGroup(Composite parent, Object layoutData) {		
	
		/*
		 * Create the "Options" group.  
		 * This is the group from which the user can either click OK, Create New Folder, or  Close this form.
		 */	

		Group optionsGroup = new Group (parent, SWT.NONE);
		optionsGroup.setLayout (new GridLayout (3, true));
		optionsGroup.setLayoutData (layoutData);
		
		/* Create the widgets inside this group*/
        createButtonOK(optionsGroup);
        createButtonCreateNewFolder(optionsGroup);
      	createButtonCancel(optionsGroup);       
	}
	
    /**
     * Creates an OK button.
     */
    private void createButtonOK(Composite parent) {
        buttonOK = new Button(parent, SWT.PUSH);
        buttonOK.setText("OK");
        buttonOK.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
        buttonOK.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	directoryPath = combo.getText();
            	shell.close();
            }
        });
    }
    
    
    /**
     * Creates a "Create New Folder" button.
     */
    private void createButtonCreateNewFolder(Composite parent) {
        buttonCreateNewFolder = new Button(parent, SWT.PUSH);
        buttonCreateNewFolder.setText("Create New Folder");
        buttonCreateNewFolder.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
        buttonCreateNewFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {            	
            	CreateNewFolderDialog newFolderDialog = new CreateNewFolderDialog();
            	newFolderDialog.setNewFolderLocation(combo.getText());
            	newFolderDialog.open();
            }
        });
    }
    
    /**
     * Creates a Cancel button.
     */
    private void createButtonCancel(Composite parent) {
        buttonCancel = new Button(parent, SWT.PUSH);
        buttonCancel.setText("Cancel");
        buttonCancel.setLayoutData (new GridData(GridData.FILL, GridData.CENTER, true, false));
        buttonCancel.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
            	shell.close();
            }
        });
    }
    
	/**
	 * Creates the combo box view.
	 * 
	 * @param parent the parent control
	 */
	private void createComboView(Composite parent, Object layoutData) {
		combo = new Combo(parent, SWT.NONE);
		combo.setLayoutData(layoutData);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final File[] roots = (File[]) combo.getData(COMBODATA_ROOTS);
				if (roots == null) return;
				int selection = combo.getSelectionIndex();
				if (selection >= 0 && selection < roots.length) {
					notifySelectedDirectory(roots[selection]);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				final String lastText = (String) combo.getData(COMBODATA_LASTTEXT);
				String text = combo.getText();
				if (text == null) return;
				if (lastText != null && lastText.equals(text)) return;
				combo.setData(COMBODATA_LASTTEXT, text);
				notifySelectedDirectory(new File(text));
			}
		});
	}
	
	/**
	 * Creates the toolbar
	 * 
	 * @param shell the shell on which to attach the toolbar
	 * @param layoutData the layout data
	*/
	
	private void createToolBar(final Shell shell, Object layoutData) {
		toolBar = new ToolBar(shell, SWT.NONE);
		toolBar.setLayoutData(layoutData);
		ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);
		item = new ToolItem(toolBar, SWT.PUSH);
		item.setImage(iconCache.stockImages[iconCache.cmdParent]);
		item.setToolTipText(getResourceString("tool.Parent.tiptext"));
		item.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				doParent();
			}
		});
		item = new ToolItem(toolBar, SWT.PUSH);
		item.setImage(iconCache.stockImages[iconCache.cmdRefresh]);
		item.setToolTipText(getResourceString("tool.Refresh.tiptext"));
		item.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				doRefresh();
			}
		});
	}

	/**
	 * Creates the file tree view.
	 * 
	 * @param parent the parent control
	 */
	private void createTreeView(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = gridLayout.marginWidth = 2;
		gridLayout.horizontalSpacing = gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);

		treeScopeLabel = new Label(composite, SWT.BORDER);
		treeScopeLabel.setText(FileViewer.getResourceString("details.AllFolders.text"));
		treeScopeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		tree = new Tree(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
		tree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		tree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				final TreeItem[] selection = tree.getSelection();
				if (selection != null && selection.length != 0) {
					TreeItem item = selection[0];
					File file = (File) item.getData(TREEITEMDATA_FILE);
				
					notifySelectedDirectory(file);
				}
			}
			public void widgetDefaultSelected(SelectionEvent event) {
				final TreeItem[] selection = tree.getSelection();
				if (selection != null && selection.length != 0) {
					TreeItem item = selection[0];
					item.setExpanded(true);
					treeExpandItem(item);
				}
			}
		});
		tree.addTreeListener(new TreeAdapter() {
			public void treeExpanded(TreeEvent event) {
				final TreeItem item = (TreeItem) event.item;
				final Image image = (Image) item.getData(TREEITEMDATA_IMAGEEXPANDED);
				if (image != null) item.setImage(image);
				treeExpandItem(item);
			}
			public void treeCollapsed(TreeEvent event) {
				final TreeItem item = (TreeItem) event.item;
				final Image image = (Image) item.getData(TREEITEMDATA_IMAGECOLLAPSED);
				if (image != null) item.setImage(image);
			}
		});

	}

	/**
	 * Handles expand events on a tree item.
	 * 
	 * @param item the TreeItem to fill in
	 */
	private void treeExpandItem(TreeItem item) {
		shell.setCursor(iconCache.stockCursors[iconCache.cursorWait]);
		final Object stub = item.getData(TREEITEMDATA_STUB);
		if (stub == null) treeRefreshItem(item, true);
		shell.setCursor(iconCache.stockCursors[iconCache.cursorDefault]);
	}
	
	/**
	 * Traverse the entire tree and update only what has changed.
	 * 
	 * @param roots the root directory listing
	 */
	private void treeRefresh(File[] masterFiles) {
		TreeItem[] items = tree.getItems();
		int masterIndex = 0;
		int itemIndex = 0;
		for (int i = 0; i < items.length; ++i) {
			final TreeItem item = items[i];
			final File itemFile = (File) item.getData(TREEITEMDATA_FILE);
			if ((itemFile == null) || (masterIndex == masterFiles.length)) {
				// remove bad item or placeholder
				item.dispose();
				continue;
			}
			final File masterFile = masterFiles[masterIndex];
			int compare = compareFiles(masterFile, itemFile);
			if (compare == 0) {
				// same file, update it
				treeRefreshItem(item, false);
				++itemIndex;
				++masterIndex;
			} else if (compare < 0) {
				// should appear before file, insert it
				TreeItem newItem = new TreeItem(tree, SWT.NONE, itemIndex);
				treeInitVolume(newItem, masterFile);
				new TreeItem(newItem, SWT.NONE); // placeholder child item to get "expand" button
				++itemIndex;
				++masterIndex;
				--i;
			} else {
				// should appear after file, delete stale item
				item.dispose();
			}
		}
		for (;masterIndex < masterFiles.length; ++masterIndex) {
			final File masterFile = masterFiles[masterIndex];
			TreeItem newItem = new TreeItem(tree, SWT.NONE);
			treeInitVolume(newItem, masterFile);
			new TreeItem(newItem, SWT.NONE); // placeholder child item to get "expand" button
		}		
	}
	
	/**
	 * Traverse an item in the tree and update only what has changed.
	 * 
	 * @param dirItem the tree item of the directory
	 * @param forcePopulate true iff we should populate non-expanded items as well
	 */
	private void treeRefreshItem(TreeItem dirItem, boolean forcePopulate) {
		final File dir = (File) dirItem.getData(TREEITEMDATA_FILE);
		
		if (! forcePopulate && ! dirItem.getExpanded()) {
			// Refresh non-expanded item
			if (dirItem.getData(TREEITEMDATA_STUB) != null) {
				treeItemRemoveAll(dirItem);
				new TreeItem(dirItem, SWT.NONE); // placeholder child item to get "expand" button
				dirItem.setData(TREEITEMDATA_STUB, null);
			}
			return;
		}
		// Refresh expanded item
		dirItem.setData(TREEITEMDATA_STUB, this); // clear stub flag

		/* Get directory listing */
		File[] subFiles = (dir != null) ? FileViewer.getDirectoryList(dir) : null;
		if (subFiles == null || subFiles.length == 0) {
			/* Error or no contents */
			treeItemRemoveAll(dirItem);
			dirItem.setExpanded(false);
			return;
		}

		/* Refresh sub-items */
		TreeItem[] items = dirItem.getItems();
		final File[] masterFiles = subFiles;
		int masterIndex = 0;
		int itemIndex = 0;
		File masterFile = null;
		for (int i = 0; i < items.length; ++i) {
			while ((masterFile == null) && (masterIndex < masterFiles.length)) {
				masterFile = masterFiles[masterIndex++];
				if (! masterFile.isDirectory()) masterFile = null;
			}

			final TreeItem item = items[i];
			final File itemFile = (File) item.getData(TREEITEMDATA_FILE);
			if ((itemFile == null) || (masterFile == null)) {
				// remove bad item or placeholder
				item.dispose();
				continue;
			}
			int compare = compareFiles(masterFile, itemFile);
			if (compare == 0) {
				// same file, update it
				treeRefreshItem(item, false);
				masterFile = null;
				++itemIndex;
			} else if (compare < 0) {
				// should appear before file, insert it
				TreeItem newItem = new TreeItem(dirItem, SWT.NONE, itemIndex);
				treeInitFolder(newItem, masterFile);
				new TreeItem(newItem, SWT.NONE); // add a placeholder child item so we get the "expand" button
				masterFile = null;
				++itemIndex;
				--i;
			} else {
				// should appear after file, delete stale item
				item.dispose();
			}
		}
		while ((masterFile != null) || (masterIndex < masterFiles.length)) {
			if (masterFile != null) {
				TreeItem newItem = new TreeItem(dirItem, SWT.NONE);
				treeInitFolder(newItem, masterFile);
				new TreeItem(newItem, SWT.NONE); // add a placeholder child item so we get the "expand" button
				if (masterIndex == masterFiles.length) break;
			}
			masterFile = masterFiles[masterIndex++];
			if (! masterFile.isDirectory()) masterFile = null;
		}
	}

	/**
	 * Foreign method: removes all children of a TreeItem.
	 * @param treeItem the TreeItem
	 */
	private static void treeItemRemoveAll(TreeItem treeItem) {
		final TreeItem[] children = treeItem.getItems();
		for (int i = 0; i < children.length; ++i) {
			children[i].dispose();
		}
	}

	/**
	 * Initializes a folder item.
	 * 
	 * @param item the TreeItem to initialize
	 * @param folder the File associated with this TreeItem
	 */
	private void treeInitFolder(TreeItem item, File folder) {
		item.setText(folder.getName());
		item.setImage(iconCache.stockImages[iconCache.iconClosedFolder]);
		item.setData(TREEITEMDATA_FILE, folder);
		item.setData(TREEITEMDATA_IMAGEEXPANDED, iconCache.stockImages[iconCache.iconOpenFolder]);
		item.setData(TREEITEMDATA_IMAGECOLLAPSED, iconCache.stockImages[iconCache.iconClosedFolder]);
	}

	/**
	 * Initializes a volume item.
	 * 
	 * @param item the TreeItem to initialize
	 * @param volume the File associated with this TreeItem
	 */
	private void treeInitVolume(TreeItem item, File volume) {
		item.setText(volume.getPath());
		item.setImage(iconCache.stockImages[iconCache.iconClosedDrive]);
		item.setData(TREEITEMDATA_FILE, volume);
		item.setData(TREEITEMDATA_IMAGEEXPANDED, iconCache.stockImages[iconCache.iconOpenDrive]);
		item.setData(TREEITEMDATA_IMAGECOLLAPSED, iconCache.stockImages[iconCache.iconClosedDrive]);
	}


	/**
	 * Notifies the application components that a new current directory has been selected
	 * 
	 * @param dir the directory that was selected, null is ignored
	 */
	void notifySelectedDirectory(File dir) {
		if (dir == null) return;
		if (currentDirectory != null && dir.equals(currentDirectory)) return;
		currentDirectory = dir;
		notifySelectedFiles();
		
		/* Shell:
		 * Sets the title to indicate the selected directory
		 */
		shell.setText(getResourceString("Title", new Object[] { currentDirectory.getPath() }));



		/* Combo view:
		 * Sets the combo box to point to the selected directory.
		 */
		final File[] comboRoots = (File[]) combo.getData(COMBODATA_ROOTS);
		int comboEntry = -1;
		if (comboRoots != null) {		
			for (int i = 0; i < comboRoots.length; ++i) {
				if (dir.equals(comboRoots[i])) {
					comboEntry = i;
					break;
				}
			}
		}
		if (comboEntry == -1) combo.setText(dir.getPath());
		else combo.select(comboEntry);

		/* Tree view:
		 * If not already expanded, recursively expands the parents of the specified
		 * directory until it is visible.
		 */
		Vector /* of File */<File> path = new Vector<File>();
		// Build a stack of paths from the root of the tree
		while (dir != null) {
			path.add(dir);
			dir = dir.getParentFile();
		}
		// Recursively expand the tree to get to the specified directory
		TreeItem[] items = tree.getItems();
		TreeItem lastItem = null;
		for (int i = path.size() - 1; i >= 0; --i) {
			final File pathElement = path.elementAt(i);

			// Search for a particular File in the array of tree items
			// No guarantee that the items are sorted in any recognizable fashion, so we'll
			// just sequential scan.  There shouldn't be more than a few thousand entries.
			TreeItem item = null;
			for (int k = 0; k < items.length; ++k) {
				item = items[k];
				if (item.isDisposed()) continue;
				final File itemFile = (File) item.getData(TREEITEMDATA_FILE);
				if (itemFile != null && itemFile.equals(pathElement)) break;
			}
			if (item == null) break;
			lastItem = item;
			if (i != 0 && !item.getExpanded()) {
				treeExpandItem(item);
				item.setExpanded(true);
			}
			items = item.getItems();
		}
		tree.setSelection((lastItem != null) ? new TreeItem[] { lastItem } : new TreeItem[0]);
	}
	
	/**
	 * Notifies the application components that files have been selected
	 * 
	 */
	void notifySelectedFiles() {			
			if (currentDirectory != null) {
				int numObjects = getDirectoryList(currentDirectory).length;
				numObjectsLabel.setText(getResourceString("details.DirNumberOfObjects.text",
					new Object[] { new Integer(numObjects) }));
			} else {
				numObjectsLabel.setText("");
			}
	}

	/**
	 * Notifies the application components that files must be refreshed
	 * 
	 * @param files the files that need refreshing, empty array is a no-op, null refreshes all
	 */
	void notifyRefreshFiles(File[] files) {
		if (files != null && files.length == 0) return;

		if ((deferredRefreshRequested) && (deferredRefreshFiles != null) && (files != null)) {
			// merge requests
			File[] newRequest = new File[deferredRefreshFiles.length + files.length];
			System.arraycopy(deferredRefreshFiles, 0, newRequest, 0, deferredRefreshFiles.length);
			System.arraycopy(files, 0, newRequest, deferredRefreshFiles.length, files.length);
			deferredRefreshFiles = newRequest;
		} else {
			deferredRefreshFiles = files;
			deferredRefreshRequested = true;
		}
		handleDeferredRefresh();
	}

	/**
	 * Handles deferred Refresh notifications (due to Drag & Drop)
	 */
	void handleDeferredRefresh() {
		if (isDragging || isDropping || ! deferredRefreshRequested) return;

		deferredRefreshRequested = false;
		File[] files = deferredRefreshFiles;
		deferredRefreshFiles = null;

		shell.setCursor(iconCache.stockCursors[iconCache.cursorWait]);

		/* Combo view:
		 * Refreshes the list of roots
		 */
		final File[] roots = getRoots();

		if (files == null) {
			boolean refreshCombo = false;
			final File[] comboRoots = (File[]) combo.getData(COMBODATA_ROOTS);
		
			if ((comboRoots != null) && (comboRoots.length == roots.length)) {
				for (int i = 0; i < roots.length; ++i) {
					if (! roots[i].equals(comboRoots[i])) {
						refreshCombo = true;
						break;
					}
				}
			} else refreshCombo = true;

			if (refreshCombo) {
				combo.removeAll();
				combo.setData(COMBODATA_ROOTS, roots);
				for (int i = 0; i < roots.length; ++i) {
					final File file = roots[i];
					combo.add(file.getPath());
				}
			}
		}

		/* Tree view:
		 * Refreshes information about any files in the list and their children.
		 */
		treeRefresh(roots);
		
		// Remind everyone where we are in the filesystem
		final File dir = currentDirectory;
		currentDirectory = null;
		notifySelectedDirectory(dir);

		shell.setCursor(iconCache.stockCursors[iconCache.cursorDefault]);
	}

	/**
	 * Performs the default action on a set of files.
	 * 
	 * @param files the array of files to process
	 */
	void doDefaultFileAction(File[] files) {
		// only uses the 1st file (for now)
		if (files.length == 0) return;
		final File file = files[0];

		if (file.isDirectory()) {
			notifySelectedDirectory(file);
		} else {
			final String fileName = file.getAbsolutePath();
			if (! Program.launch(fileName)) {	
				MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				dialog.setMessage(getResourceString("error.FailedLaunch.message", new Object[] { fileName }));
				dialog.setText(shell.getText ());
				dialog.open();
			}
		}
	}

	/**
	 * Navigates to the parent directory
	 */
	void doParent() {
		if (currentDirectory == null) return;
		File parentDirectory = currentDirectory.getParentFile();
		notifySelectedDirectory(parentDirectory);
	}
	 
	/**
	 * Performs a refresh
	 */
	void doRefresh() {
		notifyRefreshFiles(null);
	}

	/**
	 * Gets filesystem root entries
	 * 
	 * @return an array of Files corresponding to the root directories on the platform,
	 *         may be empty but not null
	 */
	File[] getRoots() {
		/*
		 * On JDK 1.22 only...
		 */
		// return File.listRoots();

		/*
		 * On JDK 1.1.7 and beyond...
		 * -- PORTABILITY ISSUES HERE --
		 */
		if (System.getProperty ("os.name").indexOf ("Windows") != -1) {
			Vector /* of File */<File> list = new Vector<File>();
			list.add(new File(DRIVE_A));
			list.add(new File(DRIVE_B));
			for (char i = 'c'; i <= 'z'; ++i) {
				File drive = new File(i + ":" + File.separator);
				if (drive.isDirectory() && drive.exists()) {
					list.add(drive);
					if (initial && i == 'c') {
						currentDirectory = drive;
						initial = false;
					}
				}
			}
			File[] roots = list.toArray(new File[list.size()]);
			sortFiles(roots);
			return roots;
		}
		File root = new File(File.separator);
		if (initial) {
			currentDirectory = root;
			initial = false;
		}
		return new File[] { root };
	}

	/**
	 * Gets a directory listing
	 * 
	 * @param file the directory to be listed
	 * @return an array of files this directory contains, may be empty but not null
	 */
	static File[] getDirectoryList(File file) {
		File[] list = file.listFiles();
		if (list == null) return new File[0];
		sortFiles(list);
		return list;
	}
	

	/**
	 * Sorts files lexicographically by name.
	 * 
	 * @param files the array of Files to be sorted
	 */
	static void sortFiles(File[] files) {
		/* Very lazy merge sort algorithm */
		sortBlock(files, 0, files.length - 1, new File[files.length]);
	}
	private static void sortBlock(File[] files, int start, int end, File[] mergeTemp) {
		final int length = end - start + 1;
		if (length < 8) {
			for (int i = end; i > start; --i) {
				for (int j = end; j > start; --j)  {
					if (compareFiles(files[j - 1], files[j]) > 0) {
					    final File temp = files[j]; 
					    files[j] = files[j-1]; 
					    files[j-1] = temp;
					}
			    }
			}
			return;
		}
		final int mid = (start + end) / 2;
		sortBlock(files, start, mid, mergeTemp);
		sortBlock(files, mid + 1, end, mergeTemp);
		int x = start;
		int y = mid + 1;
		for (int i = 0; i < length; ++i) {
			if ((x > mid) || ((y <= end) && compareFiles(files[x], files[y]) > 0)) {
				mergeTemp[i] = files[y++];
			} else {
				mergeTemp[i] = files[x++];
			}
		}
		for (int i = 0; i < length; ++i) files[i + start] = mergeTemp[i];
	}
	private static int compareFiles(File a, File b) {
//		boolean aIsDir = a.isDirectory();
//		boolean bIsDir = b.isDirectory();
//		if (aIsDir && ! bIsDir) return -1;
//		if (bIsDir && ! aIsDir) return 1;

		// sort case-sensitive files in a case-insensitive manner
		int compare = a.getName().compareToIgnoreCase(b.getName());
		if (compare == 0) compare = a.getName().compareTo(b.getName());
		return compare;
	}
	
	/*
	 * This worker updates the table with file information in the background.
	 * <p>
	 * Implementation notes:
	 * <ul>
	 * <li> It is designed such that it can be interrupted cleanly.
	 * <li> It uses asyncExec() in some places to ensure that SWT Widgets are manipulated in the
	 *      right thread.  Exclusive use of syncExec() would be inappropriate as it would require a pair
	 *      of context switches between each table update operation.
	 * </ul>
	 * </p>
	 */

	/**
	 * Stops the worker and waits for it to terminate.
	 */
	void workerStop() {
		if (workerThread == null) return;
		synchronized(workerLock) {
			workerLock.notifyAll();
		}
		while (workerThread != null) {
			if (! display.readAndDispatch()) display.sleep();
		}
	}
}
