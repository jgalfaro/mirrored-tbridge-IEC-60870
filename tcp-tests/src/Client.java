import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class Client {
	
	static JButton bforward, bbackward, bsound, bstop, bquit, btouch;
	static JFrame frame;
	static JPanel panel;
	
	
	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
		
		//Socket initialization
		final Socket clientsocket;
		final PrintWriter out;
		final int port = 2404;
		final String host = "192.168.43.222";
		
		try {
			//Connection to the server
			clientsocket = new Socket(InetAddress.getByName(host),port);
			System.out.println("Connection established !");
			
			//Variable in which the text to be sent is included
			out = new PrintWriter(clientsocket.getOutputStream());
			
			//Graphic Interface
			JFrame frame = new JFrame("Dashboard");
			frame.setSize(500, 500);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);	
		
			//"Forward" Button
			JButton bforward = new JButton("Forward");
			bforward.addActionListener(new ActionListener(){
				public void actionPerformed(java.awt.event.ActionEvent e){
					//out = Forward
					out.println("Forward");
					out.flush();
				}
			});
			
			//"Backward" Button
			JButton bbackward = new JButton("Backward");
			bbackward.addActionListener(new ActionListener(){
				public void actionPerformed(java.awt.event.ActionEvent e){
					//out = Backward
					out.println("Backward");
					out.flush();
				}
			});
			
			//"Horn" Button
			JButton bsound = new JButton("Horn");
			bsound.addActionListener(new ActionListener(){
				public void actionPerformed(java.awt.event.ActionEvent e){
					//out = Horn
					out.println("Horn");
					out.flush();
				}
			});
			
			//"Stop" Button
			JButton bstop = new JButton("Stop");
			bstop.addActionListener(new ActionListener(){
				public void actionPerformed(java.awt.event.ActionEvent e){
					//out = Stop
					out.println("Stop");
					out.flush();
				}
			});
			
			//"Disconnection" Button
			JButton bquit = new JButton("Disconnection");
			bquit.addActionListener(new ActionListener(){
				public void actionPerformed(java.awt.event.ActionEvent e){
					//out = Disconnection
					out.println("Disconnection");
					out.flush();
					try {
						clientsocket.close();
						System.out.println("Disconnection done !");
						
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			
			
			//Add buttons on the dashboard
			JPanel panel = new JPanel();
			frame.add(panel, BorderLayout.NORTH);
			panel.add(bforward);
			panel.add(bbackward);
			panel.add(bsound);
			panel.add(bstop);
			panel.add(bquit);
		
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
