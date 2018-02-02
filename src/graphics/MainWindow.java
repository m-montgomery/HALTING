package graphics;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import automata.Automaton;
import automata.State;

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
import javax.swing.JMenu;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = -7929001871259770152L;  // MM: auto-generated; necessary?
	
	private JPanel contentPane;                   // entire window
	private Automaton machine;                    // the automaton
	private StateGraphicsManager graphicManager;  // the graphical manager
	
	private JLabel lblType;
	private String machineType = "DFA";
	private JLabel lblStatus;
	private String appStatus = "ready";
	
	private JTextArea inputText;
	
	public MainWindow() {
		setTitle("HALTING");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 750, 400);
		setVisible(true);
		
		initMenuBar();
		
		// CONTENT CONTAINERS
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		JSplitPane splitPane = new JSplitPane();
		contentPane.add(splitPane);
		
		
		// MM: may want to split up sidebar creation into functions per button
		//     for organization; adding button actions will only complicate this further
		// SIDEBAR
		JPanel sidebar = new JPanel();
		JScrollPane leftComponent = new JScrollPane(sidebar);
		splitPane.setLeftComponent(leftComponent);
		GridBagLayout gbl_sidebar = new GridBagLayout();
		sidebar.setLayout(gbl_sidebar);

		// run button
		JButton btnRun = new JButton(new AbstractAction("Run") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				graphicManager.clearStates();
				machine.run(inputText.getText());
				update();  // update states and status
			}
		});
		
		GridBagConstraints gbc_btnRun = new GridBagConstraints();
		gbc_btnRun.insets = new Insets(5, 5, 5, 5);
		gbc_btnRun.anchor = GridBagConstraints.CENTER;
		gbc_btnRun.gridx = 0;
		gbc_btnRun.gridy = 0;
		sidebar.add(btnRun, gbc_btnRun);
		
		// reset button
		JButton btnReset = new JButton(new AbstractAction("Reset") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				machine.reset();
				graphicManager.clearStates();
				update();  // update states and status
			}
		});
		GridBagConstraints gbc_btnReset = new GridBagConstraints();
		gbc_btnReset.insets = new Insets(5, 5, 5, 5);
		gbc_btnReset.anchor = GridBagConstraints.CENTER;
		gbc_btnReset.gridx = 1;
		gbc_btnReset.gridy = 0;
		sidebar.add(btnReset, gbc_btnReset);
		
		// step back
		JButton btnStepBack = new JButton(new AbstractAction("<-- Step") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				machine.stepBack();
				update();  // update states and status
			}
		});
		GridBagConstraints gbc_btnStepBack = new GridBagConstraints();
		gbc_btnStepBack.insets = new Insets(5, 5, 5, 5);
		gbc_btnStepBack.anchor = GridBagConstraints.CENTER;
		gbc_btnStepBack.gridx = 0;
		gbc_btnStepBack.gridy = 1;
		sidebar.add(btnStepBack, gbc_btnStepBack);
		
		// step forward
		JButton btnStepForward = new JButton(new AbstractAction("Step -->") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				machine.step();
				update();  // update states and status
			}
		});
		GridBagConstraints gbc_btnStepForward = new GridBagConstraints();
		gbc_btnStepForward.insets = new Insets(5, 5, 5, 5);
		gbc_btnStepForward.anchor = GridBagConstraints.CENTER;
		gbc_btnStepForward.gridx = 1;
		gbc_btnStepForward.gridy = 1;
		sidebar.add(btnStepForward, gbc_btnStepForward);
		
		// input label
		JLabel lblInput = new JLabel("Input");
		GridBagConstraints gbc_lblInput = new GridBagConstraints();
		gbc_lblInput.insets = new Insets(5, 5, 0, 5);  // T, L, B, R
		gbc_lblInput.anchor = GridBagConstraints.SOUTH;
		gbc_lblInput.gridx = 0;
		gbc_lblInput.gridy = 2;
		gbc_lblInput.gridwidth = 2;         // fill entire row
		sidebar.add(lblInput, gbc_lblInput);

		// input text field
		inputText = new JTextArea();
		inputText.setFont(new Font("Monospaced", Font.PLAIN, 14));
		inputText.setLineWrap(true);
		JScrollPane areaScrollPane = new JScrollPane(inputText);  // make input field scrollable
		GridBagConstraints gbc_inputText = new GridBagConstraints();
		gbc_inputText.insets = new Insets(0, 5, 5, 5);  // T, L, B, R
		gbc_inputText.gridx = 0;
		gbc_inputText.gridy = 3;
		gbc_inputText.gridwidth = 2;         // fill entire row
		gbc_inputText.weighty = 1.0;         // give all extra vertical space
		gbc_inputText.fill = GridBagConstraints.BOTH; // fill all extra space
		sidebar.add(areaScrollPane, gbc_inputText);

		// status
		lblStatus = new JLabel("Status: " + appStatus); // default: ready
		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
		gbc_lblStatus.insets = new Insets(5, 5, 5, 5);
		gbc_lblStatus.gridx = 0;
		gbc_lblStatus.gridy = 4;
		gbc_lblStatus.gridwidth = 2;       // fill entire row
		sidebar.add(lblStatus, gbc_lblStatus);
		
		// type
		lblType = new JLabel("Type: " + machineType);   // default: DFA
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.insets = new Insets(5, 5, 5, 5);
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 5;
		gbc_lblType.gridwidth = 2;         // fill entire row
		sidebar.add(lblType, gbc_lblType);
		
		
		// MAIN DISPLAY 
		graphicManager = new StateGraphicsManager();
		JScrollPane rightComponent = new JScrollPane(graphicManager);
		splitPane.setRightComponent(rightComponent);
		
		
		// init default machine
		addAutomaton(new Automaton());
	}

	private void initMenuBar() {
		
		// Menu Bar
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu menuFile = new JMenu("File");
		menuBar.add(menuFile);
		JMenu menuEdit = new JMenu("Edit");
		menuBar.add(menuEdit);
		
		// File
		JMenuItem menuItemNew = new JMenuItem(new AbstractAction("New") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				MainWindow newFrame = new MainWindow();
				newFrame.setVisible(true);
			}			
		});
		menuFile.add(menuItemNew);
		// MM: to do: add click actions
		JMenuItem menuItemOpen = new JMenuItem("Open");
		menuFile.add(menuItemOpen);
		// MM: to do: add click actions
		JMenuItem menuItemSave = new JMenuItem("Save");
		menuFile.add(menuItemSave);
		// MM: to do: add click actions
		JMenuItem menuItemHelp = new JMenuItem("Help");
		menuFile.add(menuItemHelp);
		// MM: to do: add click actions
		JMenuItem menuItemExit = new JMenuItem("Exit");
		menuFile.add(menuItemExit);
		// MM: to do: add click actions
		
		// Edit
		JMenuItem menuItemRefresh = new JMenuItem("Refresh");
		menuEdit.add(menuItemRefresh);
		// MM: to do: add click actions
		JMenuItem menuItemChange = new JMenuItem("Change to...");
		menuEdit.add(menuItemChange);
		// MM: to do: add click actions

	}
	
	public void addAutomaton(Automaton auto) {
		machine = auto;
		graphicManager.addAutomaton(machine);
		graphicManager.createStates(machine.getStates());
	}
	
	public void update() {
		// update state graphics
		drawStates();
		
		// update labels
		if (machine.getType() != machineType) {
			machineType = machine.getType();
			lblType.setText("Type: " + machineType);
		}
		if (machine.getStatus() != appStatus) {
			appStatus = machine.getStatus();
			lblStatus.setText("Status: " + appStatus);
		}		
	}
	
	private void drawStates() {
		graphicManager.repaint();
	}
}
