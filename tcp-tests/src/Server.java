import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RemoteEV3;
import lejos.utility.Delay;


public class Server {

	RemoteEV3 ev3;
	static RMIRegulatedMotor motor;
	static EV3UltrasonicSensor sensor;
	static int MOTOR_SPEED = 300;

	public static void timer(int num){
		try{	
			Thread.sleep(num);
		}	
		catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
		//Connection to Remote EV3
		RemoteEV3 ev3 = new RemoteEV3("192.168.43.222");
		ev3.isLocal();
		
		//Socket initialization
		int port = 2404;
		String host = "192.168.43.222";
		int fileAttente = 1;
		Socket serverdusocket;
		BufferedReader in;
		String command;
	
		Button.LEDPattern(4);
		Delay.msDelay(4000);
		Button.LEDPattern(5);
		
		//Port A initialization 
		ev3.getPort("A");
		if(motor == null){
			motor = ev3.createRegulatedMotor("A", 'L');
		}
		
		try{
			// Connection to the client
			ServerSocket serversocket = new ServerSocket(port, fileAttente, InetAddress.getByName(host));
			serverdusocket = serversocket.accept();
			
			//Variable in which received text is included
			in = new BufferedReader (new InputStreamReader (serverdusocket.getInputStream()));
			
			while(true){
												
				//We read the variable received
				command = in.readLine();
				
				//if in = "Forward" -> Forward action
				if(command.equals("Forward")==true){
						motor.setSpeed(MOTOR_SPEED);
						motor.forward();
				}
			
				//if in = "Backward" -> Backward action
				if(command.equals("Backward")==true){
						motor.setSpeed(MOTOR_SPEED);
						motor.backward();
				}
				
				//if in = "Horn" -> Horn action
				if(command.equals("Horn")==true){
					Sound.buzz();
				}
				
				//if in = "Stop" -> Stop action
				if(command.equals("Stop")==true){
						motor.stop(true);
				}
				
				//if in = "Disconnection" -> Disconnection action
				if(command.equals("Disconnection")==true){
						motor.close();
						serverdusocket.close();
						serversocket.close();
				}
			}
		}catch (IOException e) {
			
			e.printStackTrace();
		}
	}
}
