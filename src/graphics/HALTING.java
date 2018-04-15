package graphics;

public class HALTING {
	
	public static void main(String[] args) {
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new MainWindow();
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});		
	}
}
