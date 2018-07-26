package graphics;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import automata.Automaton;
import automata.NoStartStateDefined;
import automata.NoTransitionDefined;
import automata.State;
import automata.Transition;

import java.awt.GridLayout;

import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JSplitPane;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenu;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
	
	// the main backend components
	private Automaton machine;                      // the automaton
	private StateGraphicsManager graphicManager;    // the graphical manager
	
	// properties
	private Properties properties;
	private float fontSize;
	private String machineType;
	private String machineStatus;
	static String preferencesFilename = "/config.properties";
	
	// graphical objects for future access
	private JLabel lblInput;                        // label that says Input:
	private JLabel lblType;                         // label for machine type
	private JLabel lblStatus;                       // label for machine status
	private JLabel lblCurrentInput;                 // label for input char
	private JTextArea inputText;                    // text area for input
	private ArrayList<Component> components;        // all (for style editing)

	// storage information
	static final String EXT = "hlt";                // file extension
	static final String VERIFY_STRING = "HALTING Automaton Save File";
	static final String userManualFilename = "/resources/UserManual.html";
	static final String iconFilename = "/resources/h.png";
 
	
	public MainWindow() {
		
		// set window basics
		setTitle("HALTING");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 750, 400);
		setLocationByPlatform(true);
		
		// clean up if closed
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent we) {
				closingProcedures();
			}
		});
		
		// set icon
		URL url = MainWindow.class.getResource(iconFilename);
		setIconImage(new ImageIcon(url).getImage());
		
		// init content containers
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		JSplitPane splitPane = new JSplitPane();
		add(splitPane);
		
		// make option menus, buttons
		components = new ArrayList<Component>();
		initMenuBar();
		initSideBar(splitPane);       // lefthand side of splitPanel
		
		// make graphics display panel (righthand side of splitPanel)
		graphicManager = new StateGraphicsManager(this);
		graphicManager.setPreferredSize(new Dimension(1500, 1000));
		JScrollPane rightComponent = new JScrollPane(graphicManager);
		splitPane.setRightComponent(rightComponent);
		
		// initialize automaton and properties
		addAutomaton(new Automaton());
		initProperties();
		graphicManager.warn(Boolean.valueOf(properties.getProperty("warnBeforeDeleting")));
		
		setVisible(true);
	}

	private void closingProcedures() {
		setVisible(false);
		
		// output user preferences to file
		if (new File(preferencesFilename).exists()) {
			try {
				OutputStream output = new FileOutputStream(preferencesFilename);
				properties.store(output, null);
				output.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		dispose();
	}
	
	private void initProperties() {
		
		properties = new Properties();

		// get user home directory
		String userHome = System.getProperty("user.home");
		if (userHome == null) {
			reportException(new FileError("Cannot find user home directory; " +
		                                  "unable to load preferences."));
			return;
		}

		// create app directory if needed
		File settingsDirectory = new File(new File(userHome), ".halting");
		if (! settingsDirectory.exists() && ! settingsDirectory.mkdir()) {
			reportException(new FileError("Cannot create program folder at " +
					settingsDirectory.toString() + 
                    "; unable to load preferences."));
			return;
		}
		
		// build user preferences filename
		preferencesFilename = settingsDirectory.toString() + "/" +
				preferencesFilename;
		
		// load properties
		try {
			// use user's preferences if available, otherwise default
			InputStream input = new File(preferencesFilename).exists() ?
					new FileInputStream(preferencesFilename) :
					MainWindow.class.getResourceAsStream("/resources/config.properties");
			
			properties.load(input);
			
			// set program properties
			setFontSize(Float.parseFloat(properties.getProperty("fontSize")));
			machine.setType(properties.getProperty("machineType"));
			
			input.close();
			
		} catch (Exception e) {
			reportException(new FileError("Unable to load program properties."));
		}
	}

	private void initMenuBar() {
		
		// MENU BAR //
		JMenuBar menuBar = new JMenuBar(); 
		JMenu menuFile = new JMenu("File");
		JMenu menuEdit = new JMenu("Edit");
		menuBar.add(menuFile);
		menuBar.add(menuEdit);
		setJMenuBar(menuBar);
		
		// FILE MENU //
		
		// New
		final JMenuItem menuItemNew = new JMenuItem(new AbstractAction("New") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				MainWindow newFrame = new MainWindow();
				newFrame.setFontSize(fontSize);
			}			
		});
		menuFile.add(menuItemNew);
		
		// Open
		final JMenuItem menuItemOpen = new JMenuItem(new AbstractAction("Open") {
			@Override
			public void actionPerformed(ActionEvent ae) {

				// open a file dialog window
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("HALTING Files", EXT);
				chooser.setFileFilter(filter);
				int choice = chooser.showOpenDialog(MainWindow.this);
				if (choice != JFileChooser.APPROVE_OPTION)
					return;
				
				// attempt to read from file
				String filename = chooser.getSelectedFile().getAbsolutePath();
				try {
					readFromFile(filename);
				}
				// warn the user about a failure
				catch (Exception e) {
					
					// if already a builtin Exception, just report it
					if (e.getClass().getSimpleName().equals("FileError"))
						reportException(e);
						
					// build an Exception message and report it
					else {
						String msg = e.getClass().getSimpleName() + ": ";
						msg += e.getMessage() == null ? filename : e.getMessage();
						reportException(new FileError(filename, msg));
					}
				}
			}
		});
		menuFile.add(menuItemOpen);
		
		// Save
		final JMenuItem menuItemSave = new JMenuItem(new AbstractAction("Save") {
			@Override
			public void actionPerformed(ActionEvent ae) {

				// open a file dialog window
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("HALTING Files", EXT);
				chooser.setFileFilter(filter);
				int choice = chooser.showSaveDialog(MainWindow.this);
				if (choice != JFileChooser.APPROVE_OPTION)
					return;
				
				// ensure correct extension
				File file = chooser.getSelectedFile();
				if (! file.getName().endsWith("." + EXT))
					file = new File(file + "." + EXT);
				String filename = file.getAbsolutePath();

				// attempt to output automaton to file
				try {
					outputToFile(filename);
					
					// delete the original (if overwriting)
					if (file.exists())
						Files.delete(file.toPath());
					
					// rename the tmp file
					File newFile = new File(filename + ".tmp");
					if (!newFile.renameTo(file))
						throw new FileError(file.getAbsolutePath(), true);
				}
				// warn the user about a failure
				catch (Exception e) {
					String msg = e.getMessage();
					reportException(new FileError(msg == null ? filename 
							                          : msg, true));
				}
			}
		});
		menuFile.add(menuItemSave);
		
		// Help
		final JMenuItem menuItemHelp = new JMenuItem(new AbstractAction("Help") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				showHelp();
			}
		});
		menuFile.add(menuItemHelp);
		
		// Exit
		final JMenuItem menuItemExit = new JMenuItem(new AbstractAction("Exit") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				closingProcedures();
			}
		});
		menuFile.add(menuItemExit);
		
		// save to components
		components.add(menuFile);
		for (Component item : menuFile.getMenuComponents())
			components.add(item);
		
		
		// EDIT MENU //
		
		// Refresh
		final JMenuItem menuItemRefresh = new JMenuItem(new AbstractAction("Refresh") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				update();    // update states, labels, text
			}			
		});
		menuEdit.add(menuItemRefresh);
		
		// Preferences
		final JMenuItem menuItemPreferences = new JMenuItem(new AbstractAction("Preferences") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				new PreferencesDialog();
				update();    // update states, labels, text
			}
		});
		menuEdit.add(menuItemPreferences);
		
		// save to components
		components.add(menuEdit);
		for (Component item : menuEdit.getMenuComponents())
			components.add(item);
	}
	
	private void initSideBar(JSplitPane splitPane) {
		
		// create side bar panel
		JPanel sidebar = new JPanel();
		JScrollPane leftComponent = new JScrollPane(sidebar);
		leftComponent.setMinimumSize(new Dimension(210, 400));
		splitPane.setLeftComponent(leftComponent);
		GridBagLayout gbl_sidebar = new GridBagLayout();
		sidebar.setLayout(gbl_sidebar);

		// init basic constraints
		GridBagConstraints gbc_sidebar = new GridBagConstraints();
		gbc_sidebar.insets = new Insets(5, 5, 5, 5);
		gbc_sidebar.anchor = GridBagConstraints.CENTER;
		gbc_sidebar.gridx = 0;
		gbc_sidebar.gridy = 0;
		
		// init step buttons here so other buttons can reference them
		final JButton btnStepBack = new JButton("Step");
		final JButton btnStepForward = new JButton("Step");
		
		// run button
		final JButton btnRun = new JButton(new AbstractAction("Run") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				graphicManager.clearStates();
				btnStepForward.setEnabled(false);
				try {
					machine.run(inputText.getText());
				} catch (NoStartStateDefined e) {
					reportException(e);
				} catch (NoTransitionDefined e) {
					reportException(e);
				}
				update();                      // update states, labels, text
			}
		});
		sidebar.add(btnRun, gbc_sidebar);
		components.add(btnRun);
		
		// reset button
		final JButton btnReset = new JButton(new AbstractAction("Reset") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				machine.reset();                 // reset the automaton
				graphicManager.clearStates();    // clear state selection
				lblCurrentInput.setText(" ");    // clear read input display
				
				inputText.setEditable(true);     // enable input editing
				btnRun.setEnabled(true);         // enable run btn
				btnStepBack.setEnabled(false);   // disable step back btn
				btnStepForward.setEnabled(true); // enable step forward btn
				
				update();                        // update states, labels, text
			}
		});
		gbc_sidebar.gridx++;
		sidebar.add(btnReset, gbc_sidebar);
		components.add(btnReset);
		
		// step back button:
		// add arrow icon (Hamilton Continental blue)
		try {
			URL url = MainWindow.class.getResource("/resources/arrow_left.png");
			btnStepBack.setIcon(new ImageIcon(url));
			btnStepBack.setHorizontalTextPosition(SwingConstants.TRAILING);
		} 
		catch (Exception e) {
			btnStepBack.setText("<-- Step");
		}
		// add button action
		btnStepBack.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				graphicManager.clearStates();      // clear selections
				machine.stepBack();
				
				// if reached beginning of current input
				if (machine.atStart()) {
					btnStepBack.setEnabled(false); // disable step back
					lblCurrentInput.setText(" ");  // clear read input display
				}
				else
					lblCurrentInput.setText("Just read: " + 
	                        machine.getCurrentInput());
				btnStepForward.setEnabled(true);   // enable step forward
				update();                          // update states, labels, text
			}
		});
		btnStepBack.setEnabled(false);         // disable step back
		gbc_sidebar.gridx = 0;
		gbc_sidebar.gridy++;
		sidebar.add(btnStepBack, gbc_sidebar);
		components.add(btnStepBack);
		
		// step forward button:
		// add arrow icon (Hamilton Continental blue)
		try {
			URL url = MainWindow.class.getResource("/resources/arrow_right.png");
			btnStepForward.setIcon(new ImageIcon(url));
			btnStepForward.setHorizontalTextPosition(SwingConstants.LEADING);
		} 
		catch (Exception e) {
			btnStepForward.setText("Step -->");
		}
		// add button action
		btnStepForward.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {

				// if first step, set input
				if (machine.atStart())
					machine.setInput(inputText.getText());
				
				try { 
					machine.step();                          // take a step
					
					// if there is input
					if (inputText.getText().length() > 0) {
						inputText.setEditable(false);        // disable input edit
						btnRun.setEnabled(false);            // disable run
						btnStepBack.setEnabled(true);        // enable step back

						// report character just read
						lblCurrentInput.setText("Just read: " + 
						                        machine.getCurrentInput());
					}

					// if end of input, disable step forward
					if (! (machine.getStatus().equals(Automaton.READY) ||
							machine.getStatus().equals(Automaton.RUN)))
						btnStepForward.setEnabled(false);
					
				} catch (NoStartStateDefined e) {
					reportException(e);
				} catch (NoTransitionDefined e) {
					reportException(e);
				}
				update();  // update states, labels, text
			}
		});
		gbc_sidebar.gridx++;
		sidebar.add(btnStepForward, gbc_sidebar);
		components.add(btnStepForward);
		
		// input label
		lblInput = new JLabel("Input:");
		gbc_sidebar.insets = new Insets(5, 5, 0, 5);  // T, L, B, R
		gbc_sidebar.anchor = GridBagConstraints.SOUTH;
		gbc_sidebar.gridx = 0;
		gbc_sidebar.gridy++;
		gbc_sidebar.gridwidth = 2;          // fill entire row
		sidebar.add(lblInput, gbc_sidebar);
		components.add(lblInput);
		
		// input text field (scrollable)
		inputText = new JTextArea();
		JScrollPane areaScrollPane = new JScrollPane(inputText);
		gbc_sidebar.insets = new Insets(0, 5, 5, 5);  // T, L, B, R
		gbc_sidebar.gridy++;
		gbc_sidebar.weighty = 1.0;
		gbc_sidebar.fill = GridBagConstraints.BOTH;
		sidebar.add(areaScrollPane, gbc_sidebar);
		components.add(inputText);
		
		// current input label
		lblCurrentInput = new JLabel(" ");
		gbc_sidebar.insets = new Insets(5, 5, 5, 5);
		gbc_sidebar.gridy++;
		gbc_sidebar.weighty = 0;
		gbc_sidebar.fill = GridBagConstraints.NONE;
		sidebar.add(lblCurrentInput, gbc_sidebar);
		components.add(lblCurrentInput);
		
		// status label
		lblStatus = new JLabel("Status: " + machineStatus); // default: ready
		gbc_sidebar.gridy++;
		sidebar.add(lblStatus, gbc_sidebar);
		components.add(lblStatus);
		
		// automaton type label
		lblType = new JLabel("Type: " + machineType);       // default: DFA
		gbc_sidebar.gridy++;
		sidebar.add(lblType, gbc_sidebar);
		components.add(lblType);
	}

	private void addAutomaton(Automaton auto) {
		machine = auto;
		graphicManager.addAutomaton(machine);
		update();
	}
	
	private void addStateGraphic(State s, int x, int y) {
		graphicManager.addState(s, x, y);
	}
	
	public void update() {
		// update state graphics
		graphicManager.repaint();
		
		// update automaton type label
		if (! machine.getType().equals(machineType)) {
			machineType = machine.getType();
			lblType.setText("Type: " + machineType);
		}
		
		// update automaton status label
		if (! machine.getStatus().equals(machineStatus)) {
			machineStatus = machine.getStatus();
			lblStatus.setText("Status: " + machineStatus);
		}
	}
	
	public float getFontSize() {
		return fontSize;
	}
	
	private void setFontSize(float newFontSize) {

		// save new font size
		fontSize = newFontSize;
		properties.setProperty("fontSize", Float.toString(newFontSize));

		// update all text-based components
		for (Component c : components)
			c.setFont(c.getFont().deriveFont(newFontSize));
		
		// update machine graphics
		update();  
	}
	
	private void reportException(Exception e) {
		JOptionPane.showMessageDialog(null,
				e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	}

	private void outputToFile(String filename) throws FileNotFoundException, IOException {

		// output to temp file
		BufferedWriter writer = new BufferedWriter(
				new FileWriter(filename+".tmp"));
		writer.write(VERIFY_STRING + "\n");

		// output automata info
		writer.write(machine.getName() + "\n");
		writer.write(machine.getType() + "\n");

		// output state info
		for (State state : machine.getStates()) {
			writer.write("STATE\n");
			writer.write(state.getName() + "\n");
			writer.write(state.isAccept() + " " + state.isStart() + " " + 
					state.getID() + " " + state.getGraphic().getX() + " " + 
					state.getGraphic().getY() + "\n");

			// output transitions
			for (Transition t : state.getTransitions())
				writer.write(t.getID() + " " + t.getInput() + " " + 
						t.getNext().getID() + "\n");
		}
		writer.close();
	}

	private void readFromFile(String filename) throws FileNotFoundException, IOException, FileError {

		BufferedReader reader = new BufferedReader(new FileReader(filename));
		ArrayList<String> lines = new ArrayList<String>();
		
		// set up new window if canvas is not blank
		boolean usingNewWindow = machine.getStates().size() > 0;
		MainWindow newFrame = null;
		if (usingNewWindow) {
			newFrame = new MainWindow();
			newFrame.setVisible(false);    // don't show yet
			newFrame.setFontSize(fontSize);
		}

		// confirm valid automaton file
		String line = reader.readLine();
		if (! line.equals(VERIFY_STRING)) {
			reader.close();
			throw new FileError(filename, 
					"This does not appear to be a valid HALTING file.");
		}

		// read all lines into list
		while ((line = reader.readLine()) != null)
			lines.add(line);
		reader.close();

		// get automata info
		String name = lines.get(0);
		String type = lines.get(1);
		State startState = null;

		// get state info
		ArrayList<State> states = new ArrayList<State>();
		for (int i = 3; i < lines.size(); i++) {

			// extract info from line
			String stateName = lines.get(i++);                 // name
			String[] stateInfo = lines.get(i++).split(" ");
			boolean isAccept = Boolean.valueOf(stateInfo[0]);  // accept?
			boolean isStart = Boolean.valueOf(stateInfo[1]);   // start?
			int stateID = Integer.valueOf(stateInfo[2]);       // ID number
			int stateX = Integer.valueOf(stateInfo[3]);        // x-coordinate
			int stateY = Integer.valueOf(stateInfo[4]);        // y-coordinate

			// get transition info
			ArrayList<Transition> transitions = new ArrayList<Transition>();
			for (String curr = null; 
					i < lines.size() && ! (curr = lines.get(i)).equals("STATE");
					i++) {

				// extract info from line
				String[] info = curr.split(" ");
				int ID = Integer.valueOf(info[0]);
				String input = info[1];
				int nextID = Integer.valueOf(info[2]); // save next's ID
				// ^ (since the states haven't all been made yet)

				// add a new transition to the list
				transitions.add(new Transition(input, nextID, ID));
			}

			// add a new state to the list
			State newState = new State(stateName, isAccept, isStart, 
					                   stateID, transitions);
			states.add(newState);
			if (isStart)
				startState = newState;
			
			// make a state graphic
			if (usingNewWindow)
				newFrame.addStateGraphic(newState, stateX, stateY);
			else
				graphicManager.addState(newState, stateX, stateY);
		}

		// make the automaton
		Automaton newMachine = new Automaton(name, type, states);
		if (startState != null)
			newMachine.setStart(startState);

		// point all state transitions to actual state objects
		for (State state : newMachine.getStates()) {
			for (Transition t : state.getTransitions()) {
				State next = newMachine.getStateWithID(t.getNextID());
				if (next != null)
					t.setNext(next);
			}
		}
		
		// open in a new window unless canvas is blank
		if (usingNewWindow) {
			newFrame.addAutomaton(newMachine);
			newFrame.setVisible(true);
		}
		else
			addAutomaton(newMachine);
	}
	
	private void showHelp() {
		
		// set window basics
		JFrame helpWindow = new JFrame();
		helpWindow.setTitle("HALTING Help");
		helpWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		helpWindow.setBounds(100, 100, 800, 600);
		
		// set icon
		URL url = MainWindow.class.getResource(iconFilename);
		helpWindow.setIconImage(new ImageIcon(url).getImage());
	
		// make editor pane
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		
		// load text with HTML formatting
		try {
			URL helpURL = MainWindow.class.getResource(userManualFilename);
			editorPane.setPage(helpURL);
		} 
		catch (Exception e) {
			reportException(new FileError("Unable to load user's manual."));
			helpWindow.dispose();
			return;
		}
		
		// add to window and display
		editorPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		helpWindow.add(new JScrollPane(editorPane));
		helpWindow.setVisible(true);
	}
	
	class PreferencesDialog extends JDialog {

		private JTextField fontInput;
		private JTextField machineTypeInput;
		private JRadioButton warnBeforeDeletingYes;

		public PreferencesDialog() {

			// set basic settings
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setBounds(100, 100, 250, 400);
			setLayout(new GridBagLayout());

			// label constraints
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.anchor = GridBagConstraints.WEST;
			gbc_label.insets = new Insets(5, 5, 5, 5);
			gbc_label.gridx = 0;
			gbc_label.gridy = 0;

			// input field constraints
			GridBagConstraints gbc_field = new GridBagConstraints();
			gbc_field.anchor = GridBagConstraints.CENTER;
			gbc_field.insets = new Insets(5, 5, 5, 5);
			gbc_field.gridx = 1;
			gbc_field.gridy = 0;
			gbc_field.gridwidth = 2;
			gbc_field.fill = GridBagConstraints.HORIZONTAL;
			
			// button constraints
			GridBagConstraints gbc_button = new GridBagConstraints();
			gbc_button.anchor = GridBagConstraints.CENTER;
			gbc_button.insets = new Insets(5, 5, 5, 5);
			gbc_button.gridx = 1;
			gbc_button.gridy = 0;
			
			
			// property: font size (label)
			JLabel fontLabel = new JLabel("Font size: "); 
			add(fontLabel, gbc_label);
			gbc_label.gridy++;
			
			// property: font size (input field)
			String fontText = Integer.toString(Math.round(fontSize));
			fontInput = new JTextField(fontText);
			add(fontInput, gbc_field);
			gbc_field.gridy++;
			
			// property: machine type (label)
			JLabel machineTypeLabel = new JLabel("Default machine type: ");
			add(machineTypeLabel, gbc_label);
			gbc_label.gridy++;
			
			// property: machine type (input field)
			machineTypeInput = new JTextField(machineType);
			add(machineTypeInput, gbc_field);
			gbc_field.gridy++;
			
			// property: warn before deleting a state (label)
			JLabel warnBeforeDeletingLabel = new JLabel("Confirm before deleting a state: ");
			add(warnBeforeDeletingLabel, gbc_label);
			gbc_button.gridy = gbc_label.gridy;
			gbc_label.gridy++;
			
			
			// property: warn before deleting a state (radio button - yes)
			ButtonGroup warnBeforeDeleteBtns = new ButtonGroup();
			warnBeforeDeletingYes = new JRadioButton("Yes");
			warnBeforeDeleteBtns.add(warnBeforeDeletingYes);
			add(warnBeforeDeletingYes, gbc_button);
			if (Boolean.valueOf(properties.getProperty("warnBeforeDeleting")))
				warnBeforeDeletingYes.setSelected(true);
			gbc_button.gridx++;
			
			// property: warn before deleting a state (radio button - no)
			JRadioButton warnBeforeDeletingNo = new JRadioButton("No");
			warnBeforeDeleteBtns.add(warnBeforeDeletingNo);
			add(warnBeforeDeletingNo, gbc_button);
			if (!Boolean.valueOf(properties.getProperty("warnBeforeDeleting")))
				warnBeforeDeletingNo.setSelected(true);
			gbc_button.gridx--;
			
			
			// cancel button
			gbc_label.anchor = GridBagConstraints.EAST;
			gbc_label.gridx++;
			JButton btnCancel = new JButton(new AbstractAction("Cancel") {
				@Override
				public void actionPerformed(ActionEvent ae) {
					cleanUp(false);
				}
			});
			btnCancel.setFont(btnCancel.getFont().deriveFont(fontSize));
			add(btnCancel, gbc_label);

			// save button
			JButton btnSave = new JButton(new AbstractAction("Save") {
				@Override
				public void actionPerformed(ActionEvent ae) {
					cleanUp(true);
				}
			});
			gbc_label.gridx++;
			btnSave.setFont(btnSave.getFont().deriveFont(fontSize));
			add(btnSave, gbc_label);

			// set font sizes
			for (Component c : getContentPane().getComponents())
				c.setFont(c.getFont().deriveFont(fontSize));

			// finish setup
			pack();
			setModalityType(Dialog.ModalityType.DOCUMENT_MODAL); // hog focus
			setVisible(true);	
		}

		private void cleanUp(boolean saving) {

			// update properties
			if (saving) {
				try {
					float newFontSize = Float.parseFloat(fontInput.getText());
					setFontSize(newFontSize);
				} catch (Exception e) {
					reportException(new FileError("Invalid font size. Must enter a number."));
				}
				
				try {
					File userFile = new File(preferencesFilename); 
					if (! userFile.exists())
						userFile.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				machineType = machineTypeInput.getText();
				properties.setProperty("machineType", machineType);
				
				if (warnBeforeDeletingYes.isSelected()) {
					properties.setProperty("warnBeforeDeleting", "true");
					graphicManager.warn(true);
				}
				else {
					properties.setProperty("warnBeforeDeleting", "false");
					graphicManager.warn(false);
				}
					
					
			}

			// close dialog window
			setVisible(false);
			dispose();
		}
	}
}
