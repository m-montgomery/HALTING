package graphics;

import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import automata.Automaton;
import automata.State;
import automata.Transition;

public class StateWindow extends JDialog {

	private static final long serialVersionUID = 8616821171044495426L;  // MM: necessary?

	private State state;                          // the state
	private Automaton machine;                    // the entire automaton
	private MainWindow mainWindow;                // the main window

	// variables for saving changes (user can 'save' or 'cancel' to finish)
	private JTextField nameInput;                 // new name input area
	private boolean isStart;                      // new start status
	private boolean isAccept;                     // new accept status
	private boolean madeTransitionChange;         // track transition changes
	private ArrayList<Transition> transitionsCopy;    // new transitions

	public StateWindow(StateGraphic stateGraphic, Automaton auto, MainWindow window) {

		super(window, "State");

		// save references
		state = stateGraphic.getState();
		machine = auto;
		mainWindow = window;

		// initialize variables for tracking changes
		isStart = state.isStart();
		isAccept = state.isAccept();
		transitionsCopy = state.getTransitionsCopy();  // get deep copy
		madeTransitionChange = false;

		// set basic settings
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 250, 350);
		setLayout(new GridBagLayout());

		// update main window upon close
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent we) {
				GraphicsTest.debug("State window is closed!");
				mainWindow.update();
			}
		});

		// create all window content
		initName();          // name of state
		initAccept();        // accept state yes/no
		initStart();         // start state yes/no
		initTransitions();   // state-to-state transitions
		initButtons();       // save and cancel
		
		setModalityType(Dialog.ModalityType.DOCUMENT_MODAL); // hog focus
		setVisible(true);	
	}
	
	private void initName() {
		
		// name label
		JLabel lblName = new JLabel("Name: ");
		GridBagConstraints gbc_nameInput = new GridBagConstraints();
		gbc_nameInput.anchor = GridBagConstraints.CENTER;
		gbc_nameInput.insets = new Insets(5, 5, 5, 5);
		gbc_nameInput.gridx = 0;
		gbc_nameInput.gridy = 0;
		add(lblName, gbc_nameInput);

		// name text field
		nameInput = new JTextField(state.getName());
		nameInput.setFont(new Font("Monospaced", Font.PLAIN, 14));
		gbc_nameInput.gridx++;
		gbc_nameInput.gridwidth = 2;
		gbc_nameInput.fill = GridBagConstraints.HORIZONTAL;
		add(nameInput, gbc_nameInput);
	}
	
	private void initAccept() {

		// accept label
		JLabel lblAccept = new JLabel("Accept: ");
		GridBagConstraints gbc_lblAccept = new GridBagConstraints();
		gbc_lblAccept.insets = new Insets(5, 5, 5, 5);
		gbc_lblAccept.gridx = 0;
		gbc_lblAccept.gridy = 1;
		add(lblAccept, gbc_lblAccept);
		ButtonGroup acceptBtns = new ButtonGroup();

		// accept yes radio button
		JRadioButton rbtnAcceptYes = new JRadioButton(new AbstractAction("Yes") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				isAccept = true;
			}
		});
		gbc_lblAccept.gridx++;
		acceptBtns.add(rbtnAcceptYes);
		if (state.isAccept())
			rbtnAcceptYes.setSelected(true);
		add(rbtnAcceptYes, gbc_lblAccept);

		// accept no radio button
		JRadioButton rbtnAcceptNo = new JRadioButton(new AbstractAction("No") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				isAccept = false;
			}
		});
		gbc_lblAccept.gridx++;
		acceptBtns.add(rbtnAcceptNo);
		if (!state.isAccept())
			rbtnAcceptNo.setSelected(true);
		add(rbtnAcceptNo, gbc_lblAccept);
	}

	private void initStart() {

		// start label
		JLabel lblStart = new JLabel("Start: ");
		GridBagConstraints gbc_lblStart = new GridBagConstraints();
		gbc_lblStart.insets = new Insets(5, 5, 5, 5);
		gbc_lblStart.gridx = 0;
		gbc_lblStart.gridy = 2;
		add(lblStart, gbc_lblStart);
		ButtonGroup startBtns = new ButtonGroup();

		// start yes radio button
		JRadioButton rbtnStartYes = new JRadioButton(new AbstractAction("Yes") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				isStart = true;
			}
		});
		gbc_lblStart.gridx++;
		startBtns.add(rbtnStartYes);
		if (state.isStart())
			rbtnStartYes.setSelected(true);
		add(rbtnStartYes, gbc_lblStart);

		// start no radio button
		JRadioButton rbtnStartNo = new JRadioButton(new AbstractAction("No") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				isStart = false;
			}
		});
		gbc_lblStart.gridx++;
		startBtns.add(rbtnStartNo);
		if (!state.isStart())
			rbtnStartNo.setSelected(true);
		add(rbtnStartNo, gbc_lblStart);
	}

	private void initTransitions() {

		// transitions label
		JLabel lblTransitions = new JLabel("Transitions", SwingConstants.CENTER);
		GridBagConstraints gbc_transitions = new GridBagConstraints();
		gbc_transitions.anchor = GridBagConstraints.CENTER;
		gbc_transitions.insets = new Insets(5, 5, 5, 5);
		gbc_transitions.gridx = 0;
		gbc_transitions.gridy = 3;
		gbc_transitions.gridwidth = 3;
		gbc_transitions.fill = GridBagConstraints.HORIZONTAL;
		add(lblTransitions, gbc_transitions);

		// transitions panel
		JPanel transitions = new JPanel();
		transitions.setLayout(new GridBagLayout());
		JScrollPane transitionsScroll = new JScrollPane(transitions);
		gbc_transitions.gridy++;
		gbc_transitions.weighty = 1.0;                  // hog vertical space
		gbc_transitions.fill = GridBagConstraints.BOTH; // hog all space
		add(transitionsScroll, gbc_transitions);

		// constraints for input & arrow
		GridBagConstraints gbc_input = new GridBagConstraints();
		gbc_input.insets = new Insets(5, 5, 5, 5);
		gbc_input.gridx = 0;
		gbc_input.gridy = 0;

		// constraints for dropdown list
		GridBagConstraints gbc_state = new GridBagConstraints();
		gbc_state.insets = new Insets(5, 5, 5, 5);
		gbc_state.gridx = 2;
		gbc_state.gridy = 0;
		gbc_state.fill = GridBagConstraints.HORIZONTAL; // hog horizontal space

		// create input, arrow, state entry for each transition
		for (Transition t : transitionsCopy) { // use copy to edit in real time  

			// make a tracker (listens for changes to input & target state)
			TransitionTracker tracker = new TransitionTracker(t);
			
			// input field
			gbc_input.gridx = 0;
			transitions.add(tracker.inputField, gbc_input);
			gbc_input.gridx++;

			// arrow label
			transitions.add(new JLabel("-->"), gbc_input);
			gbc_input.gridy++;

			// state selection dropdown list
			transitions.add(tracker.stateNames, gbc_state);
			gbc_state.gridy++;
		}
	}
	
	private void initButtons() {
		
		// cancel button
		JButton btnCancel = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				cleanUp(false);
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.anchor = GridBagConstraints.CENTER;
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 5;
		add(btnCancel, gbc_btnCancel);

		// save button
		JButton btnSave = new JButton(new AbstractAction("Save") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				cleanUp(true);
			}
		});
		gbc_btnCancel.gridx++;
		add(btnSave, gbc_btnCancel);
	}
	
	private void cleanUp(boolean saving) {
		// if user selected 'save'
		if (saving) {
			
			// update name
			String newName = nameInput.getText();
			if (newName != state.getName() && 
					!machine.hasStateNamed(newName))   // MM: to do: add warning about repeat names?
				state.setName(newName);

			// update start status
			if (isStart != state.isStart()) {
				if (isStart)
					machine.setStart(state);
				else 
					machine.removeStart();
			}	

			// update accept status
			state.setAccept(isAccept);

			// update transitions
			if (madeTransitionChange)
				state.setTransitions(transitionsCopy);
		}

		// close dialog window
		setVisible(false);
		dispose();
	}

	class TransitionTracker {
		public Transition transition;
		public JComboBox<String> stateNames;
		public JTextField inputField;

		public TransitionTracker(Transition t) {
			transition = t;
			stateNames = new JComboBox<String>();
			
			// make dropdown list of state names
			for (State s : machine.getStates()) {  
				stateNames.addItem(s.getName());

				// make current transition's target state the selected item
				if (t.getNext().getName() == s.getName())
					stateNames.setSelectedItem(s.getName());
			}
			
			// when changed, update transition's target state
			stateNames.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					// set new target
					transition.setNext(machine.getStateNamed(
							(String)stateNames.getSelectedItem()));
					
					// mark that there's been a change
					madeTransitionChange = true;
				}
			});
			
			// make text field for input
			inputField = new JTextField(t.getInput());
			inputField.setFont(new Font("Monospaced", Font.PLAIN, 14));
			inputField.setColumns(2);
			
			// when changed, update transition's input if valid
			inputField.getDocument().addDocumentListener(new DocumentListener() {
				public void removeUpdate(DocumentEvent de) {
					changedUpdate(de);
				}
				public void insertUpdate(DocumentEvent de) {
					changedUpdate(de);
				}
				public void changedUpdate(DocumentEvent de) {
					// assert valid input length
					if (inputField.getText().length() > 1)
						JOptionPane.showMessageDialog(null,
								"Input cannot be longer than 1 character.",
								"Error", JOptionPane.ERROR_MESSAGE);
					else {
						// change transition input
						t.setInput(inputField.getText());

						// mark that there's been a change
						madeTransitionChange = true;
					}
				}
			});
		}
	}	
}
