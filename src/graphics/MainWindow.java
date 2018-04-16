package graphics;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;

import javax.swing.JSplitPane;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
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
	
	// graphical objects for future access
	private JLabel lblType;                         // label for machine type
	private String machineType = "DFA";             //   (default is DFA)
	private JLabel lblStatus;                       // label for machine status
	private String machineStatus = Automaton.READY; //   (default is Ready)
	private JLabel lblCurrentInput;                 // label for input char
	private JTextArea inputText;                    // text area for input

	// storage information
	static final String EXT = "hlt";                // file extension
	static final String VERIFY_STRING = "HALTING Automaton Save File";
	static final String userManualFilename = "src/resources/UserManual.html";
	
	
	public MainWindow() {
		
		// set window basics
		setTitle("HALTING");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 750, 400);
		setLocationByPlatform(true);
		
		// set icon
		URL url = MainWindow.class.getResource("/resources/h.png");
		setIconImage(new ImageIcon(url).getImage());
		
		// init content containers
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		JSplitPane splitPane = new JSplitPane();
		add(splitPane);
		
		// make option menus, buttons
		initMenuBar();
		initSideBar(splitPane);       // lefthand side of splitPanel
		
		// make graphics display panel (righthand side of splitPanel)
		graphicManager = new StateGraphicsManager(this);
		graphicManager.setPreferredSize(new Dimension(1500, 1000));
		JScrollPane rightComponent = new JScrollPane(graphicManager);
		splitPane.setRightComponent(rightComponent);
		
		// set default machine
		addAutomaton(new Automaton());

		setVisible(true);
	}

	private void initMenuBar() {
		
		// MENU BAR
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
				newFrame.addAutomaton(new Automaton());
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
				setVisible(false);
				dispose();
			}
		});
		menuFile.add(menuItemExit);
	
		
		// EDIT MENU //
		
		// Refresh
		final JMenuItem menuItemRefresh = new JMenuItem(new AbstractAction("Refresh") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				update();    // update states, labels, text
			}			
		});
		menuEdit.add(menuItemRefresh);
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
		final JButton btnStepForward = new JButton("Step");;
		
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
				graphicManager.clearStates();                // clear selections
				if (machine.atStart()) {                     // if 1st step:
					machine.setInput(inputText.getText());    // set input
					inputText.setEditable(false);             // disable input edit
					btnRun.setEnabled(false);                 // disable run btn
				}
				try { 
					machine.step();                          // take a step
					btnStepBack.setEnabled(true);            // enable step back
					lblCurrentInput.setText("Just read: " + 
					                        machine.getCurrentInput());
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
		
		// input label
		final JLabel lblInput = new JLabel("Input:");
		gbc_sidebar.insets = new Insets(5, 5, 0, 5);  // T, L, B, R
		gbc_sidebar.anchor = GridBagConstraints.SOUTH;
		gbc_sidebar.gridx = 0;
		gbc_sidebar.gridy++;
		gbc_sidebar.gridwidth = 2;          // fill entire row
		sidebar.add(lblInput, gbc_sidebar);

		// input text field (scrollable)
		inputText = new JTextArea();
		JScrollPane areaScrollPane = new JScrollPane(inputText);
		gbc_sidebar.insets = new Insets(0, 5, 5, 5);  // T, L, B, R
		gbc_sidebar.gridy++;
		gbc_sidebar.weighty = 1.0;
		gbc_sidebar.fill = GridBagConstraints.BOTH;
		sidebar.add(areaScrollPane, gbc_sidebar);
		
		// current input label
		lblCurrentInput = new JLabel(" ");
		gbc_sidebar.insets = new Insets(5, 5, 5, 5);
		gbc_sidebar.gridy++;
		gbc_sidebar.weighty = 0;
		gbc_sidebar.fill = GridBagConstraints.NONE;
		sidebar.add(lblCurrentInput, gbc_sidebar);
		
		// status label
		lblStatus = new JLabel("Status: " + machineStatus); // default: ready
		gbc_sidebar.insets = new Insets(5, 5, 5, 5);
		gbc_sidebar.gridy++;
		gbc_sidebar.weighty = 0;
		gbc_sidebar.fill = GridBagConstraints.NONE;
		sidebar.add(lblStatus, gbc_sidebar);
		
		// automaton type label
		lblType = new JLabel("Type: " + machineType);       // default: DFA
		gbc_sidebar.gridy++;
		sidebar.add(lblType, gbc_sidebar);
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
		helpWindow.setBounds(100, 100, 500, 600);
		//helpWindow.setIconImage(new ImageIcon(imgURL).getImage()); // MM: TO DO: add icon
	
		// make editor pane
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		
		// load text with HTML formatting
		try {
			URL helpURL = new File(userManualFilename).toURI().toURL();
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
}
