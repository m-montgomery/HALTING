package graphics;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JSplitPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = -7929001871259770152L;  // MM: auto-generated; necessary?
	private JPanel contentPane;

	// launch
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public MainWindow() {
		setTitle("HALTING");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 750, 400);
		
		// MENU BAR
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);                             // MM: to do: align items left
		JMenuItem menuItemFile = new JMenuItem("File");   // MM: to do: add click actions
		menuBar.add(menuItemFile);
		JMenuItem menuItemEdit = new JMenuItem("Edit");   // MM: to do: add click actions
		menuBar.add(menuItemEdit);
		
		
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
		JButton btnRun = new JButton("Run");   // MM: to do: add click actions
		GridBagConstraints gbc_btnRun = new GridBagConstraints();
		gbc_btnRun.insets = new Insets(5, 5, 5, 5);
		gbc_btnRun.anchor = GridBagConstraints.CENTER;
		gbc_btnRun.gridx = 0;
		gbc_btnRun.gridy = 0;
		sidebar.add(btnRun, gbc_btnRun);
		
		// reset button
		JButton btnReset = new JButton("Reset");   // MM: to do: add click actions
		GridBagConstraints gbc_btnReset = new GridBagConstraints();
		gbc_btnReset.insets = new Insets(5, 5, 5, 5);
		gbc_btnReset.anchor = GridBagConstraints.CENTER;
		gbc_btnReset.gridx = 1;
		gbc_btnReset.gridy = 0;
		sidebar.add(btnReset, gbc_btnReset);
		
		// step back
		JButton btnStepBack = new JButton("<-- Step");   // MM: to do: add click actions
		GridBagConstraints gbc_btnStepBack = new GridBagConstraints();
		gbc_btnStepBack.insets = new Insets(5, 5, 5, 5);
		gbc_btnStepBack.anchor = GridBagConstraints.CENTER;
		gbc_btnStepBack.gridx = 0;
		gbc_btnStepBack.gridy = 1;
		sidebar.add(btnStepBack, gbc_btnStepBack);
		
		// step forward
		JButton btnStepForward = new JButton("Step -->");   // MM: to do: add click actions
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
		JTextArea inputText = new JTextArea();   // MM: to do: add buttons to clear & set
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
		JLabel lblStatus = new JLabel("Status: Ready");  // default status // MM: will need to change to currentStatus variable
		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
		gbc_lblStatus.insets = new Insets(5, 5, 5, 5);
		gbc_lblStatus.gridx = 0;
		gbc_lblStatus.gridy = 4;
		gbc_lblStatus.gridwidth = 2;       // fill entire row
		sidebar.add(lblStatus, gbc_lblStatus);
		
		// type
		JLabel lblType = new JLabel("Type: DFA");  // default type // MM: will need to change to defaultType variable
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.insets = new Insets(5, 5, 5, 5);
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 5;
		gbc_lblType.gridwidth = 2;         // fill entire row
		sidebar.add(lblType, gbc_lblType);
		
		
		
		// MAIN DISPLAY 
		JPanel mainPanel = new JPanel();
		JScrollPane rightComponent = new JScrollPane(mainPanel);
		splitPane.setRightComponent(rightComponent);
		
	}
}
