/*
 * Copyright 2014-17 Fraunhofer ISE
 *
 * This file is part of j60870.
 * For more information visit http://www.openmuc.org
 *
 * j60870 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * j60870 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with j60870.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package main.java.org.openmuc.j60870.app;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.ActionListener;

import main.java.org.openmuc.j60870.ASdu;
import main.java.org.openmuc.j60870.CauseOfTransmission;
import main.java.org.openmuc.j60870.ClientConnectionBuilder;
import main.java.org.openmuc.j60870.Connection;
import main.java.org.openmuc.j60870.ConnectionEventListener;
import main.java.org.openmuc.j60870.IeNormalizedValue;
import main.java.org.openmuc.j60870.IeQualifierOfInterrogation;
import main.java.org.openmuc.j60870.IeQualifierOfSetPointCommand;
import main.java.org.openmuc.j60870.IeTime56;
import main.java.org.openmuc.j60870.internal.cli.CliParameter;
import main.java.org.openmuc.j60870.internal.cli.CliParameterBuilder;
import main.java.org.openmuc.j60870.internal.cli.CliParseException;
import main.java.org.openmuc.j60870.internal.cli.CliParser;
import main.java.org.openmuc.j60870.internal.cli.IntCliParameter;
import main.java.org.openmuc.j60870.internal.cli.StringCliParameter;

public final class Client {
	
	static JButton bforward, bbackward, binterrogation, bstop, bquit, bclock, bactibarrier;
	static JFrame frame;
	static JPanel panel_int, panel_motor;
	
	//Initialization of Host and Port parameters
    private static final StringCliParameter hostParam = new CliParameterBuilder("-h").buildStringParameter("host", "192.168.43.11");
    private static final IntCliParameter portParam = new CliParameterBuilder("-p")
            .setDescription("The port to connect to.").buildIntParameter("port", 2404);
    private static final IntCliParameter commonAddrParam = new CliParameterBuilder("-ca")
            .setDescription("The address of the target station or the broad cast address.")
            .buildIntParameter("common_address", 1);

    private static volatile Connection connection;

    //Listen the response given by the server
    private static class ClientEventListener implements ConnectionEventListener {

        @Override
        public void newASdu(ASdu aSdu) {
            System.out.println("\nReceived ASDU:\n" + aSdu);

        }

        @Override
        public void connectionClosed(IOException e) {
            System.out.print("Received connection closed signal. Reason: ");
            if (!e.getMessage().isEmpty()) {
                System.out.println(e.getMessage());
            }
            else {
                System.out.println("unknown");
            }
            connection.close();
        }

    }

    public static void main(String[] args) {
    	
    	//IP Address initialization
        InetAddress address;
        try {
            address = InetAddress.getByName(hostParam.getValue());
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + hostParam.getValue());
            return;
        }

        ClientConnectionBuilder clientConnectionBuilder = new ClientConnectionBuilder(address)
                .setPort(portParam.getValue());
        
        //Connection established
        try {
            connection = clientConnectionBuilder.connect();
        } catch (IOException e) {
            System.out.println("Unable to connect to remote host: " + hostParam.getValue() + ".");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                connection.close();
            }
        });

        //Starting data transfer
        try {
            connection.startDataTransfer(new ClientEventListener(), 5000);
        } catch (TimeoutException e2) {
            System.out.println("Starting data transfer timed out. Closing connection.");
            connection.close();
            return;
        } catch (IOException e) {
            System.out.println("Connection closed for the following reason: " + e.getMessage());
            return;
        }
        System.out.println("successfully connected");
        

        //Graphic Interface
    	JFrame frame = new JFrame("Dashboard");
    	frame.setSize(500, 500);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setVisible(true);	
    	
    	
    	//"Interrogation" Button (Implemented by Pr.Fraunhofer)
    	JButton binterrogation = new JButton("Interrogation");
    	binterrogation.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
				System.out.println("** Sending general interrogation command ** \n");
                try {
					connection.interrogation(commonAddrParam.getValue(), CauseOfTransmission.ACTIVATION,
					        new IeQualifierOfInterrogation(20));
					Thread.sleep(2000);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
                
			}
		});
    	
    	//"Synchronization" Button (Implemented by Pr.Fraunhofer)
    	JButton bclock = new JButton("Synchronization");
    	bclock.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
				 System.out.println("** Sending synchronize clocks command. **  \n");
                    try {
						connection.synchronizeClocks(commonAddrParam.getValue(), new IeTime56(System.currentTimeMillis()));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			}
		});
    	

    	//"Forward" Button
    	JButton bforward = new JButton("Forward");
    	bforward.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
				System.out.println("** Sending 'Forward' command. ** \n");
            	try {
					connection.setNormalizedValueCommand(commonAddrParam.getValue(), 
							CauseOfTransmission.ACTIVATION, 2, new IeNormalizedValue(1), 
							new IeQualifierOfSetPointCommand(0,false));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
    		
    	//"Backward" Button
    	JButton bbackward = new JButton("Backward");
    	bbackward.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
				System.out.println("** Sending 'Backward' command. ** \n");
            	try {
            		connection.setNormalizedValueCommand(commonAddrParam.getValue(), 
            				CauseOfTransmission.ACTIVATION, 2, new IeNormalizedValue(-1), 
            				new IeQualifierOfSetPointCommand(0,false));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
    	    					
    	//"Stop" Button
    	JButton bstop = new JButton("Stop");
    	bstop.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
				System.out.println("** Sending Stop command. ** \n");
            	try {
            		connection.setNormalizedValueCommand(commonAddrParam.getValue(), 
            				CauseOfTransmission.ACTIVATION, 2, new IeNormalizedValue(0), 
            				new IeQualifierOfSetPointCommand(0,false));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
    	//"Disconnection" Button
    	JButton bquit = new JButton("Disconnection");
    	bquit.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
	            System.out.println("** Closing connection. ** \n");
	            connection.close();
			}
		});
    	
    	
    	//"Activate Automatic Barrier" Button
    	JButton bactibarrier = new JButton("Activate Automatic Barrier");
    	bactibarrier.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
				System.out.println("** Sending 'Activate Automatic Barrier' command. ** \n");
            	try {
            		connection.setNormalizedValueCommand(commonAddrParam.getValue(), 
            				CauseOfTransmission.ACTIVATION, 3, new IeNormalizedValue(1), 
            				new IeQualifierOfSetPointCommand(0,false));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
    
    	
    	//Add buttons on the dashboard
    	JPanel panel_int = new JPanel();
    	frame.add(panel_int, BorderLayout.NORTH);
    	panel_int.add(binterrogation);
    	panel_int.add(bclock);
    	panel_int.add(bactibarrier);    
    	
    	JPanel panel_motor = new JPanel();
    	frame.add(panel_motor, BorderLayout.CENTER);
    	panel_motor.add(bforward);
    	panel_motor.add(bbackward);
    	panel_motor.add(bstop);
    	panel_motor.add(bquit);

    }

}
