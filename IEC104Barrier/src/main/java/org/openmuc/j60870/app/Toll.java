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

import javax.swing.JLabel;

import lejos.hardware.Button;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import main.java.org.openmuc.j60870.ASdu;
import main.java.org.openmuc.j60870.CauseOfTransmission;
import main.java.org.openmuc.j60870.Connection;
import main.java.org.openmuc.j60870.ConnectionEventListener;
import main.java.org.openmuc.j60870.IeNormalizedValue;
import main.java.org.openmuc.j60870.IeQuality;
import main.java.org.openmuc.j60870.InformationElement;
import main.java.org.openmuc.j60870.InformationObject;
import main.java.org.openmuc.j60870.Server;
import main.java.org.openmuc.j60870.ServerEventListener;
import main.java.org.openmuc.j60870.TypeId;

public class Toll {
	
	RemoteEV3 ev3;
	static RMIRegulatedMotor barrierMotor;
	static RMIRegulatedMotor coinMotor;
	static EV3UltrasonicSensor ultrasonicSensor;
	static EV3TouchSensor touchSensor;
	static EV3ColorSensor coinColorSensor;
	
	private static int BARRIER_ANGLE = 80;
	private static int BARRIER_MOTOR_SPEED = 60;
	private static int COIN_ANGLE = 2520;
	private static int BARRIER_COIN_SPEED = 300;
		
	/*
	 * EV3 Close
	 */
	public void stopEV3() throws RemoteException {
		
		barrierMotor.close();
		coinMotor.close();
		coinColorSensor.close();
		ultrasonicSensor.close();
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
                    
                    case C_SE_NA_1:
                    	connection.sendConfirmation(aSdu);
                    	
                    	//Gets the Normalized Value and the Information Object Address 
                    	IeNormalizedValue normalizedValue = (IeNormalizedValue) aSdu.getInformationObjects()[0].getInformationElements()[0][0];
                		int informationObjectAddress = aSdu.getInformationObjects()[0].getInformationObjectAddress();                		
                		
                		if (informationObjectAddress == 2){
                			
                			//Free DAY
                			if (normalizedValue.getUnnormalizedValue() == 1){
                				
                				ultrasonicSensor.enable();
                				
                				//Car detection (ultrasonic sensor initialization)
                				SampleProvider sampleProvider=ultrasonicSensor.getDistanceMode();
                				float[] sample=new float[sampleProvider.sampleSize()];
                				
                				while(ultrasonicSensor.isEnabled() == true){
                					
                					sampleProvider.fetchSample(sample, 0);
                					
                					//Touch Sensor initialization
                					int sampleSize1 = touchSensor.sampleSize();
                    				float[] sample1 = new float[sampleSize1];
                					touchSensor.fetchSample(sample1, 0);
                					                					
                					//If a car is detected, the barrier opens
                					if(sample[0]<0.1){
            							Delay.msDelay(2000);
                						barrierMotor.setSpeed(BARRIER_MOTOR_SPEED);
                						barrierMotor.rotate(BARRIER_ANGLE);
                						Delay.msDelay(4000);
                					}
                					
                					//When the car crosses the barrier, it closes (When the touch sensor is activated)
                					if(sample1[0] == 1){
                						barrierMotor.setSpeed(BARRIER_MOTOR_SPEED);
                						barrierMotor.rotate(- BARRIER_ANGLE);
                					}
                					
                					//Break out of  the "While" Loop
                					if(Button.getButtons() == Button.ID_ENTER){
                						ultrasonicSensor.disable();
                					}
                				}
                			}
                			
                			//Paying Day
                			if (normalizedValue.getUnnormalizedValue() == 2){
                				                				
                				ultrasonicSensor.enable();
                				coinColorSensor.setFloodlight(true);
                				
                				//Car detection (ultrasonic sensor initialization)
                				SampleProvider sampleProvider=ultrasonicSensor.getDistanceMode();
                				float[] sample=new float[sampleProvider.sampleSize()];
                				
                				
                				while(ultrasonicSensor.isEnabled() == true){
                					
                					//Touch Sensor Initialization
                					int sampleSize1 = touchSensor.sampleSize();
                    				float[] sample1 = new float[sampleSize1];
                					touchSensor.fetchSample(sample1, 0);
                					
                					//Color sensor initialization
                					SensorMode color = coinColorSensor.getColorIDMode();
                    				float[] sample2 = new float[color.sampleSize()];
                					color.fetchSample(sample2, 0);
                					
                					//If a car is detected
                					if (sample[0]<0.1) {
                						
                						//If the coin is green, the barrier opens
                    					if (sample2[0] == Color.GREEN) {
                							System.out.println("Coin accepted");
                							
                							coinMotor.setSpeed(BARRIER_COIN_SPEED);
                							coinMotor.rotate(COIN_ANGLE);
                							Delay.msDelay(2000);
                							coinMotor.rotate(- COIN_ANGLE);
                							
                							barrierMotor.setSpeed(BARRIER_MOTOR_SPEED);
                							barrierMotor.rotate(+ BARRIER_ANGLE);                							
                    					}
                    					
                    					//If the coin is red, the coin is rejected
                    					if (sample2[0] == Color.RED) {
                							System.out.println("Coin rejected");
                							
                							coinMotor.setSpeed(BARRIER_COIN_SPEED);
                							coinMotor.rotate(- COIN_ANGLE);
                							Delay.msDelay(2000);
                							coinMotor.rotate(COIN_ANGLE);
                							                							
                    					}
                					}
                    					
                    				//When the car crosses the barrier, it closes (When the touch sensor is activated)
                    				if(sample1[0] == 1){
                    					barrierMotor.setSpeed(BARRIER_MOTOR_SPEED);
                    					barrierMotor.rotate(- BARRIER_ANGLE);
                    				}
                    				
                    				//Break out of  the "While" Loop
                    				if(Button.getButtons() == Button.ID_ENTER){
                    					coinColorSensor.setFloodlight(false);
                    					ultrasonicSensor.disable();
                    				}
                				}
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
    	
    	// Connection to Remote EV3
    	RemoteEV3 ev3 = new RemoteEV3("192.168.43.237");
		ev3.isLocal();
		
		//Port A initialization
		if(coinMotor == null){
			ev3.getPort("A");
			coinMotor = ev3.createRegulatedMotor("A", 'L');
		}	
		
		//Port B initialization
		if(barrierMotor == null){
			ev3.getPort("B");
			barrierMotor = ev3.createRegulatedMotor("B", 'L');
		}	
		
		//Port 1 initialization
		if(coinColorSensor == null){
			coinColorSensor = new EV3ColorSensor(SensorPort.S1);
		}
		
		//Port 2 initialization
		if(touchSensor == null){
			touchSensor = new EV3TouchSensor(SensorPort.S2);
		}
		
		//Port 3 initialization
		if(ultrasonicSensor == null){
			ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S3);
		}
		
		
		Button.LEDPattern(4);
		Delay.msDelay(4000);
		Button.LEDPattern(5);
		
        new Toll().start();
        
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