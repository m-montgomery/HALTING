package graphics;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;
import javax.swing.JLabel;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = -7929001871259770152L;  // MM: auto-generated; necessary?
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		setTitle("HALTING");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(2, 3, 0, 0));
		
		JLabel lbloptions = new JLabel("Options");
		contentPane.add(lbloptions);       // row 0 col 0
		contentPane.add(new JLabel("a"));  // row 0 col 1
		contentPane.add(new JLabel("b"));  // row 0 col 2
		contentPane.add(new JLabel("c"));  // row 1 col 0
		contentPane.add(new JLabel("d"));  // row 1 col 1
		contentPane.add(new JLabel("e"));  // row 1 col 2
	}
}
