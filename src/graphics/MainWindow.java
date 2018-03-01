package graphics;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import automata.Automaton;
import automata.NoStartStateDefined;
import automata.NoTransitionDefined;

import java.awt.GridLayout;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JSplitPane;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenu;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = -7929001871259770152L;  // MM: auto-generated; necessary?
	
	private Automaton machine;                      // the automaton
	private StateGraphicsManager graphicManager;    // the graphical manager
	
	private JLabel lblType;                         // label for machine type
	private String machineType = "DFA";             // (default is DFA)
	private JLabel lblStatus;                       // label for machine status
	private String machineStatus = Automaton.READY; // (default is Ready)
	
	private JTextArea inputText;                    // text area for input
	
	public MainWindow() {
		
		// set window basics
		setTitle("HALTING");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 750, 400);
		//frame.setIconImage(new ImageIcon(imgURL).getImage()); // MM: TO DO: add icon
		
		// init content containers
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		JSplitPane splitPane = new JSplitPane();
		add(splitPane);
		
		// make option menus, buttons
		initMenuBar();
		initSideBar(splitPane);       // lefthand side of splitPanel
		
		// make graphics display panel (righthand side of splitPanel)
		graphicManager = new StateGraphicsManager(this);
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
		JMenuItem menuItemNew = new JMenuItem(new AbstractAction("New") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				MainWindow newFrame = new MainWindow();
				newFrame.addAutomaton(new Automaton());
				newFrame.setVisible(true);
			}			
		});
		menuFile.add(menuItemNew);
		
//		// Open
//		// MM: to do: add click actions
//		JMenuItem menuItemOpen = new JMenuItem("Open");
//		menuFile.add(menuItemOpen);
//		
//		// Save
//		// MM: to do: add click actions
//		JMenuItem menuItemSave = new JMenuItem("Save");
//		menuFile.add(menuItemSave);
//		
//		// Help
//		// MM: to do: add click actions
//		JMenuItem menuItemHelp = new JMenuItem("Help");
//		menuFile.add(menuItemHelp);
		
		// Exit
		JMenuItem menuItemExit = new JMenuItem(new AbstractAction("Exit") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
				dispose();
			}
		});
		menuFile.add(menuItemExit);
	
		
		// EDIT MENU //
		
		// Refresh
		JMenuItem menuItemRefresh = new JMenuItem(new AbstractAction("Refresh") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				update();
			}			
		});
		menuEdit.add(menuItemRefresh);
		
//		// Change to...
//		// MM: to do: add click actions
//		JMenuItem menuItemChange = new JMenuItem("Change to...");
//		menuEdit.add(menuItemChange);
	}
	
	private void initSideBar(JSplitPane splitPane) {
		
		// create side bar panel
		JPanel sidebar = new JPanel();
		JScrollPane leftComponent = new JScrollPane(sidebar);
		splitPane.setLeftComponent(leftComponent);
		GridBagLayout gbl_sidebar = new GridBagLayout();
		sidebar.setLayout(gbl_sidebar);

		// init basic constraints
		GridBagConstraints gbc_sidebar = new GridBagConstraints();
		gbc_sidebar.insets = new Insets(5, 5, 5, 5);
		gbc_sidebar.anchor = GridBagConstraints.CENTER;
		gbc_sidebar.gridx = 0;
		gbc_sidebar.gridy = 0;
		
		// run button
		JButton btnRun = new JButton(new AbstractAction("Run") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				graphicManager.clearStates();
				try {
					machine.run(inputText.getText());
				} catch (NoStartStateDefined e) {
					reportException(e);
				} catch (NoTransitionDefined e) {
					reportException(e);
				}
				update();  // update states and status
			}
		});
		sidebar.add(btnRun, gbc_sidebar);
		
		// reset button
		JButton btnReset = new JButton(new AbstractAction("Reset") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				machine.reset();
				graphicManager.clearStates();
				update();  // update states and status
			}
		});
		gbc_sidebar.gridx++;
		sidebar.add(btnReset, gbc_sidebar);
		
		// step back button
		JButton btnStepBack = new JButton(new AbstractAction("<-- Step") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				machine.stepBack();
				update();  // update states and status
			}
		});
		gbc_sidebar.gridx = 0;
		gbc_sidebar.gridy++;
		sidebar.add(btnStepBack, gbc_sidebar);
		
		// step forward button
		JButton btnStepForward = new JButton(new AbstractAction("Step -->") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				graphicManager.clearStates();                // clear selections
				if (machine.getStatus() == Automaton.READY)  // if 1st step..
					machine.setInput(inputText.getText());   // ..set input
				try { 
					machine.step();                          // take a step
				} catch (NoStartStateDefined e) {
					reportException(e);
				} catch (NoTransitionDefined e) {
					reportException(e);
				}
				update();  // update states and status
			}
		});
		gbc_sidebar.gridx++;
		sidebar.add(btnStepForward, gbc_sidebar);
		
		// input label
		JLabel lblInput = new JLabel("Input");
		gbc_sidebar.insets = new Insets(5, 5, 0, 5);  // T, L, B, R
		gbc_sidebar.anchor = GridBagConstraints.SOUTH;
		gbc_sidebar.gridx = 0;
		gbc_sidebar.gridy++;
		gbc_sidebar.gridwidth = 2;          // fill entire row
		sidebar.add(lblInput, gbc_sidebar);

		// input text field
		inputText = new JTextArea();
		inputText.setFont(new Font("Monospaced", Font.PLAIN, 14));
		inputText.setLineWrap(true);
		JScrollPane areaScrollPane = new JScrollPane(inputText);  // make input field scrollable
		gbc_sidebar.insets = new Insets(0, 5, 5, 5);  // T, L, B, R
		gbc_sidebar.gridy++;
		gbc_sidebar.weighty = 1.0;
		gbc_sidebar.fill = GridBagConstraints.BOTH;
		sidebar.add(areaScrollPane, gbc_sidebar);

		// status label
		lblStatus = new JLabel("Status: " + machineStatus); // default: ready
		gbc_sidebar.insets = new Insets(5, 5, 5, 5);
		gbc_sidebar.gridy++;
		gbc_sidebar.weighty = 0;
		gbc_sidebar.fill = GridBagConstraints.NONE;
		sidebar.add(lblStatus, gbc_sidebar);
		
		// automaton type label
		lblType = new JLabel("Type: " + machineType);   // default: DFA
		gbc_sidebar.gridy++;
		sidebar.add(lblType, gbc_sidebar);
	}
	
	public void addAutomaton(Automaton auto) {
		machine = auto;
		graphicManager.addAutomaton(machine);
		graphicManager.createStates(machine.getStates()); // make StateGraphics
	}
	
	public void update() {
		// update state graphics
		graphicManager.repaint();
		
		// update labels
		if (machine.getType() != machineType) {           // automaton type
			machineType = machine.getType();
			lblType.setText("Type: " + machineType);
		}
		if (machine.getStatus() != machineStatus) {       // automaton status
			machineStatus = machine.getStatus();
			lblStatus.setText("Status: " + machineStatus);
		}		
	}
	
	private void reportException(Exception e) {
		JOptionPane.showMessageDialog(null,
				e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	}
}
