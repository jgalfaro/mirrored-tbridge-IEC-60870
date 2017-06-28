/*
 * This file is part of j60870.
 * For more information visit http://www.openmuc.org
 *
 * You are free to use code of this sample file in any
 * way you like and without any restrictions.
 *
 */
package main.java.org.openmuc.j60870.app;

import java.io.EOFException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.TimeoutException;

import lejos.hardware.Button;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.utility.Delay;
import main.java.org.openmuc.j60870.ASdu;
import main.java.org.openmuc.j60870.CauseOfTransmission;
import main.java.org.openmuc.j60870.Connection;
import main.java.org.openmuc.j60870.ConnectionEventListener;
import main.java.org.openmuc.j60870.IeNormalizedValue;
import main.java.org.openmuc.j60870.IeQuality;
import main.java.org.openmuc.j60870.IeScaledValue;
import main.java.org.openmuc.j60870.InformationElement;
import main.java.org.openmuc.j60870.InformationObject;
import main.java.org.openmuc.j60870.Server;
import main.java.org.openmuc.j60870.ServerEventListener;
import main.java.org.openmuc.j60870.TypeId;

public class SampleServer {
	
	RemoteEV3 ev3;
	static RMIRegulatedMotor motor;
	static EV3UltrasonicSensor sensor;
	
	int MOTOR_SPEED = 300;
	int BARRIER_ANGLE = 90;
	
	/*
	 * EV3 Close
	 */
	public void stopEV3() throws RemoteException {
		
		motor.close();
		sensor.close();
		System.exit(0);
		
	}

    public class ServerListener implements ServerEventListener  {
    	
    	
        public class ConnectionListener implements ConnectionEventListener {

            private final Connection connection;
            private final int connectionId;

            public ConnectionListener(Connection connection, int connectionId) {
                this.connection = connection;
                this.connectionId = connectionId;
            }
            
            /*
             * ASDU's informations received by the server 
             */            
            @Override
            public void newASdu(ASdu aSdu) {
                try {

                    switch (aSdu.getTypeIdentification()) {
                    
                    // interrogation command (Implemented by Pr.Fraunhofer) 
                    case C_IC_NA_1:
                        connection.sendConfirmation(aSdu);
                        System.out.println("Got interrogation command. Will send scaled measured values.\n");

                        connection.send(new ASdu(TypeId.M_ME_NB_1, true, CauseOfTransmission.SPONTANEOUS, false, false,
                                0, aSdu.getCommonAddress(),
                                new InformationObject[] { new InformationObject(1, new InformationElement[][] {
                                        { new IeScaledValue(-32768), new IeQuality(true, true, true, true, true) },
                                        { new IeScaledValue(10), new IeQuality(true, true, true, true, true) },
                                        { new IeScaledValue(-5), new IeQuality(true, true, true, true, true) } }) }));

                        break;
                     
                    // Clock command (Implemented by Pr.Fraunhofer) 
                    case C_CS_NA_1:
                    	connection.sendConfirmation(aSdu);
                    	
                  
                    // Motor command
                    case C_SE_NA_1:
                    	connection.sendConfirmation(aSdu);
                    	
                    	//Gets the Normalized Value and the Information Object Address
                    	IeNormalizedValue normalizedValue = (IeNormalizedValue) aSdu.getInformationObjects()[0].getInformationElements()[0][0];
                		int informationObjectAddress = aSdu.getInformationObjects()[0].getInformationObjectAddress();
                		
                		// Motor action
                		if(informationObjectAddress == 2){
                			
                			// Forward action
                			if (normalizedValue.getUnnormalizedValue() == 1){
                				System.out.println("Got Forward command");
                				motor.setSpeed(MOTOR_SPEED);
                				motor.forward();
                			}
                			
                			// Backward action
                			if (normalizedValue.getUnnormalizedValue() == -1){
                				System.out.println("Got Backward command");
                				motor.setSpeed(MOTOR_SPEED);
                				motor.backward();
                			}
                			
                			// Stop action
                			if (normalizedValue.getUnnormalizedValue() == 0){
                				System.out.println("Got Stop command");
                				motor.stop(true);
                			}
                		}
                		
                		// Barrier action
                		if(informationObjectAddress == 3){
                			
                			if (normalizedValue.getUnnormalizedValue() == 1){
                				sensor.enable();
                				
                				// Ultrasonic sensor initialization
                				int sampleSize = sensor.sampleSize();
                				float[] sample = new float[sampleSize];
                				
                				
                				while(true){
                					
                					sensor.fetchSample(sample, 0);
                					                					
                					//If an object is detected, the barrier opens
                					if(sample[0]<0.1){
                						motor.rotateTo(BARRIER_ANGLE);
                						Delay.msDelay(4000);
                						motor.rotateTo(- BARRIER_ANGLE);
                					}
                					if (Button.getButtons() == Button.ID_ENTER){
                						break;
                					}
                					
                				}
                				sensor.disable();
                			}
                		}

                    	break;
						
                    default:
                        System.out.println("Got unknown request: " + aSdu + ". Will not confirm it.\n");
                    }

                } catch (EOFException e) {
                    System.out.println("Will quit listening for commands on connection (" + connectionId
                            + ") because socket was closed.");
                } catch (IOException e) {
                    System.out.println("Will quit listening for commands on connection (" + connectionId
                            + ") because of error: \"" + e.getMessage() + "\".");
                } 

            }

            @Override
            public void connectionClosed(IOException e) {
                System.out.println("Connection (" + connectionId + ") was closed. " + e.getMessage());
            }

        }

        //Give rights to the server (Waiting for the datas)
        @Override
        public void connectionIndication(Connection connection) {

            int myConnectionId = connectionIdCounter++;
            System.out.println("A client has connected using TCP/IP. Will listen for a StartDT request. Connection ID: "
                    + myConnectionId);

            try {
                connection.waitForStartDT(new ConnectionListener(connection, myConnectionId), 5000);
            } catch (IOException e) {
                System.out.println("Connection (" + myConnectionId + ") interrupted while waiting for StartDT: "
                        + e.getMessage() + ". Will quit.");
                return;
            } catch (TimeoutException e) {
            }

            System.out.println(
                    "Started data transfer on connection (" + myConnectionId + ") Will listen for incoming commands.");

        }

        @Override
        public void serverStoppedListeningIndication(IOException e) {
            System.out.println(
                    "Server has stopped listening for new connections : \"" + e.getMessage() + "\". Will quit.");
        }

        @Override
        public void connectionAttemptFailed(IOException e) {
            System.out.println("Connection attempt failed: " + e.getMessage());

        }

    }

    private int connectionIdCounter = 1;

    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
    	
    	//Connection to Remote EV3
    	RemoteEV3 ev3 = new RemoteEV3("192.168.43.11");
		ev3.isLocal();
		
		//Port A initialization
		if(motor == null){
			ev3.getPort("A");
			motor = ev3.createRegulatedMotor("A", 'L');
		}	
		
		//Port 4 initialization
		if(sensor == null){
			sensor = new EV3UltrasonicSensor(SensorPort.S4);
		}
 
		Button.LEDPattern(4);
		Delay.msDelay(4000);
		Button.LEDPattern(5);
    	
    			
        new SampleServer().start();
    }

    public void start() {
        Server server = new Server.Builder().build();
        
        try {
            server.start(new ServerListener());
            if (Button.getButtons() == Button.ID_ESCAPE) {
            	stopEV3();
            }
        } catch (IOException e) {
            System.out.println("Unable to start listening: \"" + e.getMessage() + "\". Will quit.");
            return;
        }
    }

}