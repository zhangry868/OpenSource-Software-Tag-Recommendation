

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class SubWindows extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2215166237228509322L;
	JPanel explanationPanel;
	JTextArea WordAera;
	JButton jbtAbout;
	JScrollPane textArea;
	//protected MessagePanel messagePanel;
	public SubWindows(String str)
	{
		
		explanationPanel = new JPanel(new BorderLayout(5,0));	
		WordAera = new JTextArea("Welcome To ML-kNNCSF!\n" + "Author: Rui-Yi ZHANG\n" +
		"Version 0.1.7\n" + "If there is any bugs, Feel free to contact me:\n" + "zhangry868@126.com"
				+"\n" + "This is a MultiLabel CostSensitive Clasifier Based On ML-kNN." + "\n"
		+"GitHub:https://github.com/zhangry868/ML-kNNCSF");
		WordAera.setLineWrap(true);
		WordAera.setFont(new Font("Consola",Font.BOLD,12));
		//WordAera.setBackground(new Color(120, 145, 230));
		textArea = new JScrollPane(WordAera);
		explanationPanel.add(textArea,BorderLayout.CENTER);
		add(explanationPanel, BorderLayout.CENTER);
		
		jbtAbout = new JButton("Close");
		jbtAbout.setMnemonic('S');
		jbtAbout.setToolTipText("About the dictionary");
		add(jbtAbout, BorderLayout.SOUTH);
		
		jbtAbout.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
	}
}