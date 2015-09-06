import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class UI extends JFrame
{
	/**
	 * A class holds many panels
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel explanationPanel;
	private JPanel jpTextField;
	private JPanel SettingField;
	
	private JTextField jtfMessage = new JTextField(10);
	private JButton jbtSearch = new JButton(new ImageIcon("button.gif"));//"Search");
	private JList<String> jlist;
	private JTextArea WordAera = new JTextArea();
	JButton jbtAddWord;
	JButton jbtAbout;
	JTextArea main,text;
	public UI()
	{
		Add();
	}
	
	public void Add()
	{
		
		main = new JTextArea();
		main.setEditable(false);
		main.setText("Welcome to ML-kNNCSF Classifier!\n"
				+ "By Rui-Yi Zhang");
		main.setFont(new Font("Consola",Font.BOLD,24));
		
		text = new JTextArea();
		text.setEditable(false);
		text.setText("\n\nIntroduction:\n\n"
				+"1、Please make sure Rename Your Test File: test.data, and put it in the same Folder with this program.\n "
				+"2、Please Make Sure The Code is in UTF-8.\n"
				+"3、Just Click test to get the Prediction Results。\n"
				+"4、To Learn More, You Can Click About。");
		
		text.setFont(new Font("Consola",Font.PLAIN,12));
		
		jbtAddWord = new JButton("Test");//) 
		jbtAddWord.setMnemonic('S');
		jbtAddWord.setToolTipText("Test Data Set");
		
		jbtAbout = new JButton("About");
		jbtAbout.setMnemonic('S');
		jbtAbout.setToolTipText("About the Classifier");
		
		JPanel buttonJPanel = new JPanel();
		buttonJPanel.add(jbtAbout,BorderLayout.EAST);
		buttonJPanel.add(jbtAddWord,BorderLayout.WEST);
		add(main, BorderLayout.NORTH);
		add(text, BorderLayout.CENTER);
		add(buttonJPanel,BorderLayout.SOUTH);
		
		jbtAddWord.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(null,"Press Button to Start Testing.","Testing...", JOptionPane.INFORMATION_MESSAGE);
				Test.model.test("./test.data", "./test.output");
				JOptionPane.showMessageDialog(null,"Output to test.output","Testing...", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		jbtAbout.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				SubWindows subFrame = new SubWindows("Welcome To Roy's Dictionary!");
				subFrame.setSize(400, 200);
				subFrame.setTitle("About This Program");
				subFrame.setLocationRelativeTo(null);//Center the Frame
				subFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				subFrame.setVisible(true);
			}
		});
		
	}
	
	public static void main(String args[])
	{
		UI frame = new UI();
		JOptionPane.showMessageDialog(null,"Press Button to start Load.","Loading Model...", JOptionPane.INFORMATION_MESSAGE);
		Test.Testmain(null);
		frame.setSize(600,300);
		frame.setTitle("ML-kNNCSF By Roy");
		frame.setLocationRelativeTo(null);//Center the Frame
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
	