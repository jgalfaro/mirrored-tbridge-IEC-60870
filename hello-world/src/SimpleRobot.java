import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import lejos.hardware.*;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.hardware.ev3.EV3;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.remote.ev3.RemoteEV3;
import lejos.hardware.ev3.*;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class SimpleRobot {
	
	RemoteEV3 ev3;
	static RMIRegulatedMotor motor;
	static int t;
	
	public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
		
		//Creates the remote EV3
		RemoteEV3 ev3 = new RemoteEV3("192.168.43.237");
		ev3.isLocal();
		
		Button.LEDPattern(4);
		Delay.msDelay(4000);
		
		Button.LEDPattern(5);
			
		EV3TouchSensor ts = new EV3TouchSensor(SensorPort.S1);
		 
		 
		//Gets port
		ev3.getPort("A");
		
		//Creates motor
		if(motor == null){
			motor = ev3.createRegulatedMotor("A", 'L');
		}
			
		//Lets move
		while (t<5) {
			
			// Touch sensor initialization
			int sampleSize = ts.sampleSize();
			float[] sample = new float[sampleSize];
			ts.fetchSample(sample, 0);
			
			// If the touch sensor is activated
			if(sample[0] == 1){
				motor.forward();
				timer(1000);
				motor.stop(true);
				Sound.buzz();
				
				timer(500);
				t=t+1;
			}
		
			else{
				motor.backward();
				timer(1000);
				motor.stop(true);
				Sound.twoBeeps();
			
				timer(1);
				t=t+1;
			}
		}	
		
		//Closes ports
		motor.close();
		ts.close();
	}
	
	public static void timer(int num){
		try{	
			Thread.sleep(num);
		}	
		catch(InterruptedException e){
			e.printStackTrace();
		}
	}
}
	


