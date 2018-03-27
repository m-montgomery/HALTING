package graphics;

import java.awt.Dimension;
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
import javax.swing.JTextPane;
import javax.swing.text.StyleContext;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
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
	
	//private JTextArea inputText;                    // text area for input
	private JTextPane inputText;                    // text area for input
    // MM: ^ currently using JTextPane for text bolding, but the line wrapping
    //       doesn't work. might revert to JTextArea and skip the bolding. 
	
	public MainWindow() {
		
		// set window basics
		setTitle("HALTING");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 750, 400);
		setLocationByPlatform(true);
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
		splitPane.setRightComponent(graphicManager);  // MM: don't need scroll?
		// JScrollPane rightComponent = new JScrollPane(graphicManager);
		// splitPane.setRightComponent(rightComponent);
		
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
				newFrame.setVisible(true);
			}			
		});
		menuFile.add(menuItemNew);
		
//		// Open
//		// MM: to do: add click actions
//		final JMenuItem menuItemOpen = new JMenuItem("Open");
//		menuFile.add(menuItemOpen);
//		
//		// Save
//		// MM: to do: add click actions
//		final JMenuItem menuItemSave = new JMenuItem("Save");
//		menuFile.add(menuItemSave);
//		
//		// Help
//		// MM: to do: add click actions
//		final JMenuItem menuItemHelp = new JMenuItem("Help");
//		menuFile.add(menuItemHelp);
		
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
		
//		// Change to...
//		// MM: to do: add click actions
//		final JMenuItem menuItemChange = new JMenuItem("Change to...");
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
		
		// init step back button here so reset button can reference it
		final JButton btnStepBack = new JButton("<-- Step");
		
		// run button
		final JButton btnRun = new JButton(new AbstractAction("Run") {
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
				inputText.setEditable(true);     // enable input editing
				btnRun.setEnabled(true);         // enable run btn
				btnStepBack.setEnabled(false);   // disable step back
				update();                        // update states, labels, text
			}
		});
		gbc_sidebar.gridx++;
		sidebar.add(btnReset, gbc_sidebar);
		
		// step back button
		// initialized first (above, so btnReset can use it), now add action;
		// otherwise, reference to self in btnStepBack.setEnabled causes error
		btnStepBack.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				graphicManager.clearStates();      // clear selections
				machine.stepBack();
				if (machine.atStart())
					btnStepBack.setEnabled(false); // disable step back
				update();                          // update states, labels, text
			}
		});
		btnStepBack.setEnabled(false);         // disable step back
		gbc_sidebar.gridx = 0;
		gbc_sidebar.gridy++;
		sidebar.add(btnStepBack, gbc_sidebar);
		
		// step forward button
		final JButton btnStepForward = new JButton(new AbstractAction("Step -->") {
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
		final JLabel lblInput = new JLabel("Input");
		gbc_sidebar.insets = new Insets(5, 5, 0, 5);  // T, L, B, R
		gbc_sidebar.anchor = GridBagConstraints.SOUTH;
		gbc_sidebar.gridx = 0;
		gbc_sidebar.gridy++;
		gbc_sidebar.gridwidth = 2;          // fill entire row
		sidebar.add(lblInput, gbc_sidebar);

		// input text field (scrollable)
		// MM: TO DO: JTextPane no longer wraps lines properly; add workaround
		inputText = initTextPane();
		JScrollPane areaScrollPane = new JScrollPane(inputText);
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
		lblType = new JLabel("Type: " + machineType);       // default: DFA
		gbc_sidebar.gridy++;
		sidebar.add(lblType, gbc_sidebar);
	}
	
	private JTextPane initTextPane() {
		// make text pane
		JTextPane pane = new JTextPane();
		
		// initialize styles
		StyledDocument doc = pane.getStyledDocument();
		Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		
		// regular is monospaced 16pt
		Style regularStyle = doc.addStyle("regular", defaultStyle);
		StyleConstants.setFontFamily(defaultStyle, "Monospaced");
		StyleConstants.setFontSize(defaultStyle, 16);
		pane.setFont(new Font("Monospaced", Font.PLAIN, 16));
		
		// bold is regular + bolded (used for current input character)
		Style boldStyle = doc.addStyle("bold", regularStyle);
		StyleConstants.setBold(boldStyle, true);
		
		return pane;
	}
	
	private void updateTextStyle(int boldIndex) {
		String input = inputText.getText();
		StyledDocument doc = inputText.getStyledDocument();
		inputText.setText("");  // clear text
		
		// add each character of input one by one
		for (int i = 0; i < input.length(); i++) {
			try {
				// bold character if current input
				if (i == boldIndex)
					doc.insertString(doc.getLength(), 
							input.substring(i, i+1), 
							doc.getStyle("bold"));
				
				// all other characters are normal
				else
					doc.insertString(doc.getLength(), 
							input.substring(i, i+1), 
							doc.getStyle("regular"));
			} catch (BadLocationException e) {
				// MM: ?
				e.printStackTrace();
			}
		}
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
		
		// update input text styling
		updateTextStyle(machine.getInputNum()-1);    // bold current input char
	}
	
	private void reportException(Exception e) {
		JOptionPane.showMessageDialog(null,
				e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	}
}
