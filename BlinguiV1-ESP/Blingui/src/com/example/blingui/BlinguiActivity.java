package com.example.blingui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.example.blingui.WifiAnalizerActivity.WifiReceiver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BlinguiActivity extends Activity implements OnClickListener, OnInitListener, SensorEventListener{  

	private static final String TAG = "Blingui";
	//variable for checking Voice Recognition support on user device
	private static final int VR_REQUEST = 999;

	//variable for checking TTS engine data on user device
	private int MY_DATA_CHECK_CODE = 0;

	//Text To Speech instance
	private TextToSpeech repeatTTS; 

	//ListView for displaying suggested words
	private ListView wordList;

	//Log tag for output information
	private final String LOG_TAG = "SpeechRepeatActivity";



	Button btnOn, btnOff, buttonOsbstaculo, btnSpeak;
	TextView txtArduino, txtText;
	Handler h;

	final int RECIEVE_MESSAGE = 1;		// Status  for Handler
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private StringBuilder sb = new StringBuilder();
	TextView mainText;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	int count=0;
	int soma=0;
	private String ITEM_KEY = "key";
	private boolean localizar =false;
	private boolean destino = false;
	private boolean destino1=false;
	private boolean destino2=false;
	private boolean destino3=false;
	private boolean destino4=false;
	private boolean destino5=false;
	private boolean destino6=false;

	boolean local1 = false;
	boolean local2 = false;
	boolean local3 = false;
	boolean local4 = false;


	private List<HashMap<String, String>> networkList = new ArrayList<HashMap<String, String>>();



	private ConnectedThread mConnectedThread;

	// SPP UUID service
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// MAC-address of Bluetooth module (you must edit this line)
	private static String address = "20:14:05:19:33:23";
	private static String PA1="00:1a:3f:d2:4a:7c";
	private static String PA2="1c:af:f7:5d:2b:30";
	private static String PA3="00:24:01:9a:68:f8";
	private static String PA4="c8:3a:35:45:88:48";


	SensorManager sensorManager;
	Sensor sensor;
	int direcao = 0;
	DecimalFormat df = new DecimalFormat("#.#");

	private String A = "padaria";
	private String B = "banco";
	private String C = "quiosque";
	private String D = "cozinha";


	private final int PercursoAC =1;
	private final int PercursoCA =2;
	private final int PercursoAB =3;
	private final int PercursoBA =4;
	private final int PercursoBC =5;
	private final int PercursoCB =6; 
	private int numClicks = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blingui);


		//    btnOn = (Button) findViewById(R.id.btnOn);					// button LED ON
		//btnOff = (Button) findViewById(R.id.btnOff);				// button LED OFF
		txtArduino = (TextView) findViewById(R.id.txtArduino);		// for display the received data from the Arduino
		buttonOsbstaculo = (Button) findViewById(R.id.buttonOsbstaculo);
		txtText = (TextView) findViewById(R.id.txtText);
		mainText = (TextView) findViewById(R.id.mainText);

		//gain reference to speak button
		Button speechBtn = (Button) findViewById(R.id.speech_btn);
		Button localizar = (Button) findViewById(R.id.localizar);
		//gain reference to word list
		wordList = (ListView) findViewById(R.id.word_list);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);


		//find out whether speech recognition is supported
		PackageManager packManager = getPackageManager();
		List<ResolveInfo> intActivities = packManager.queryIntentActivities
				(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (intActivities.size() != 0) {
			//speech recognition is supported - detect user button clicks
			speechBtn.setOnClickListener((OnClickListener) this);
			//prepare the TTS to repeat chosen words
			Intent checkTTSIntent = new Intent();  
			//check TTS data  
			checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);  
			//start the checking Intent - will retrieve result in onActivityResult
			startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE); 
		}
		else 
		{
			//speech recognition not supported, disable button and output message
			speechBtn.setEnabled(false);
			Toast.makeText(this, "Oops - Speech recognition not supported!", Toast.LENGTH_LONG).show();
		}

		txtText.setText("");

		detctaObstaculo();
		btAdapter = BluetoothAdapter.getDefaultAdapter();		// get Bluetooth adapter
		checkBTState();

	}


	private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
		if(Build.VERSION.SDK_INT >= 10){
			try {
				final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, MY_UUID);
			} catch (Exception e) {
				Log.e(TAG, "Could not create Insecure RFComm Connection",e);
			}
		}
		return  device.createRfcommSocketToServiceRecord(MY_UUID);
	}


	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

		Log.d(TAG, "...onResume - try connect...");

		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		// Two things are needed to make a connection:
		//   A MAC address, which we got above.
		//   A Service ID or UUID.  In this case we are using the
		//     UUID for SPP.

		try {
			btSocket = createBluetoothSocket(device);

		} catch (IOException e) {
			errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
		}

		/*try {
	      btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
	    } catch (IOException e) {
	      errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
	    }*/

		// Discovery is resource intensive.  Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection.  This will block until it connects.
		Log.d(TAG, "...Connecting...");
		try {
			btSocket.connect();
			Log.d(TAG, "....Connection ok...");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
			}
		}

		// Create a data stream so we can talk to server.
		Log.d(TAG, "...Create Socket...");

		mConnectedThread = new ConnectedThread(btSocket);
		mConnectedThread.start();

	}


	@Override
	public void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);


		Log.d(TAG, "...In onPause()...");

		//    unregisterReceiver(receiverWifi);

		try     {


			btSocket.close();
		} catch (IOException e2) {
			errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
		}



	}

	private void checkBTState() {
		// Check for Bluetooth support and then check to make sure it is turned on
		// Emulator doesn't support Bluetooth and will return null
		if(btAdapter==null) { 
			errorExit("Fatal Error", "Bluetooth not support");
		} else {
			if (btAdapter.isEnabled()) {
				Log.d(TAG, "...Bluetooth ON...");
			} else {
				//Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, 1);
			}
		}
	}
	private void errorExit(String title, String message){
		Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
		finish();
	}

	private class ConnectedThread extends Thread {
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[256];  // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);		// Get number of bytes and message in "buffer"
					h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();		// Send to message queue Handler
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(String message) {
			Log.d(TAG, "...Data to send: " + message + "...");
			byte[] msgBuffer = message.getBytes();
			try {
				mmOutStream.write(msgBuffer);
			} catch (IOException e) {
				Log.d(TAG, "...Error data send: " + e.getMessage() + "...");     
			}
		}
	}
	public void Buttom_Click(View v)

	{
		destino = false;
		localizar();
		//Log.i("Informação:", "Botão clicado- vibrando!");
		// EditText cidade = (EditText) findViewById(R.id.cidade);//entrada de dados
		//Log.i("Meu Texto:",cidade.getText().toString());

	}

	public void detctaObstaculo(){
		h = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case RECIEVE_MESSAGE:													// if receive massage
					byte[] readBuf = (byte[]) msg.obj;
					String strIncom = new String(readBuf, 0, msg.arg1);					// create string from bytes array
					sb.append(strIncom);												// append string
					int endOfLineIndex = sb.indexOf("\r\n");							// determine the end-of-line
					if (endOfLineIndex > 0) { 											// if end-of-line,
						String sbprint = sb.substring(0, endOfLineIndex);				// extract string
						sb.delete(0, sb.length());										// and clear
						//	mainText.setText("Distância do Obstáculo: " + sbprint +"cm"); 	        // update TextView
					/*	try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
						//int x = Integer.parseInt(sbprint);
						Log.i("distancia:",sbprint);

						if(sbprint.equals("1")){
							//if(x>50 && x<60 || x<10 || x>90 && x<200){
							

							repeatTTS.speak("Obstáculo Superior...:", TextToSpeech.QUEUE_FLUSH, null);
							buttonOsbstaculo.setVisibility(View.VISIBLE);
							//	mainText.setText("teste: " + sbprint +"cm"); 	        // update TextView

						}

						else if (sbprint.equals("2")){
						
							repeatTTS.speak("Obstáculo Inferior...:", TextToSpeech.QUEUE_FLUSH, null);
							buttonOsbstaculo.setVisibility(View.VISIBLE);
							//	mainText.setText("teste2: " + sbprint +"cm"); 	        // update TextView

						}
						else if (sbprint.equals("3")){
							Vibrar();
							buttonOsbstaculo.setVisibility(View.VISIBLE);
						}

						else if (sbprint.equals("4")){
							buttonOsbstaculo.setVisibility(View.INVISIBLE);}

					}
					break;
				}
			};
		};

	}


	private void Vibrar()
	{
		Vibrator rr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		long milliseconds = 600;//'30' é o tempo em milissegundos, é basicamente o tempo de duração da vibração. portanto, quanto maior este numero, mais tempo de vibração você irá ter
		rr.vibrate(milliseconds); 
	}

	/**
	 * Called when the user presses the speak button
	 */
	public void onClick(View v) {
		destino = true;
		localizar= false;
		if (v.getId() == R.id.speech_btn) {
			repeatTTS.speak("Diga o Destino!", TextToSpeech.QUEUE_FLUSH, null);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//listen for results
			listenToSpeech();

		}
		else{
			unregisterReceiver(receiverWifi);
		}
	}

	private void localizar()
	{
		localizar =true;
		destino= false;
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		//	btAdapter = BluetoothAdapter.getDefaultAdapter();	
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		mainWifi.startScan();
		repeatTTS.speak("Localizando. Por favor aguarde!", TextToSpeech.QUEUE_FLUSH, null);
		mainText.setText("\nLocalizando usuário...\n");


	}

	private String identificarLocal(ScanResult result){

		double d1= distancia(result, "Nascimento");
		double d2= distancia(result, "Formar");
		double d3= distancia(result, "Blingui");
		double d5= distancia(result, "dlink");

		//*casa

		//if (d1>0 && d1 < 3 || (d1>0 & d1 <5) && (d3>4 & d3<6)){

		//*ABC padaria
		if (d1>0 && d1 <=3 || (d1>0 & d1 <=3) && (d3>3 & d3<6)) {
			local1=true;
			local2=false;
			local3=false;
			local4=false;
			return local(A);

		}

		///	else if (d3>0 && d3 < 20 || (d3>0 & d3 < 100) && (d1 >5 & d1<10))

		//Banco
		else if (d3>0 && d3 <=2 || (d3>0 && d3 <=2) & (d1>4 & d2 <6) ){
			local2=true;
			local1=false;
			local3=false;
			local4=false;
			return local(B);
		}

		//Quiosque

		else if (d2>0 && d2 <=4.2 ||  (d2>0 && d2<=4) & (d5>5 && d5<=9 )) {

			local3 = true;
			local1=false;
			local2=false;
			local4=false;
			return local(C);

		}

		//ABC Cozinhaa
		else if (d5>0 && d5 <=15 || (d5>0 && d5 <=15) &(d2 >5 && d2<10) ) {
			local4 =true;
			local1=false;
			local2=false;
			local3=false;
			return local(D);

		}

		else return "";
	}


	public String local(String local){
		localizar =true;
		Log.i("i:",""+count);

count++;
	if(count<=2){
		repeatTTS.speak("A sua localização é:"+local, TextToSpeech.QUEUE_FLUSH, null);
		sb.append("\nLOCALIZAÇÃO: "+local);
		mainText.setText(sb);
		Log.i("i:",""+count);
	}
	else if(count>30){
	count=0;
	}
		
			return local;

	}





	/**
	 * Instruct the app to listen for user speech input
	 */
	private void listenToSpeech() {

		//start the speech recognition intent passing required data
		Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		//indicate package
		listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		//message to display while listening
		listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga o Destino!");
		//set speech model
		listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		//specify number of results to retrieve
		listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

		//start listening
		startActivityForResult(listenIntent, VR_REQUEST);
	}


	public void guiarAteDestino(String destino){
		sb = new StringBuilder();

		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		localizar = true;

		if(destino.equals(A)){
			destino1 = true;
			destino2= false;
			destino3 = false;
			destino4 = false;
			mainWifi.startScan();
			repeatTTS.speak("...Localizando destino::"+destino, TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("\nLocalizando destino: " + destino+" ...\n");
			mainText.setText("Direção:"+getDirecao());

		}

		if(destino.equals(B)){
			destino2= true;
			destino1 = false;
			destino3 = false;
			destino4 = false;
			mainWifi.startScan();
			repeatTTS.speak("...Localizando destino::"+destino, TextToSpeech.QUEUE_FLUSH, null);
			repeatTTS.speak("...Localizando destino::"+destino, TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("\nLocalizando destino: " + destino+" ...\n");

		}

		if(destino.equals(C)){
			destino3=true;
			destino1 = false;
			destino2=false;
			destino4 = false;
			mainWifi.startScan();
			repeatTTS.speak("...Localizando destino::"+destino, TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("\nLocalizando destino: " + destino+" ...\n");
			mainText.setText("Direção:"+getDirecao());

		}
		if(destino.equals(D)){
			destino4= true;
			destino1 = false;
			destino2= false;
			destino3 = false;
			mainWifi.startScan();
			repeatTTS.speak("...Localizando destino::"+destino, TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("\nLocalizando destino: " + destino+" ...\n");
			mainText.setText("Direção:"+getDirecao());

		}
		/*else if(destino.equals("sala 3")){
			destino3=true;
			destino1 = false;
			destino2=false;
			destino4 = false;
			destino5 = false;
			mainWifi.startScan();
			mainText.setText("\nStarting Scan...\n");
			sb.append("destino:" +destino);

		}


		else if(destino.equals("banheiro")){
			destino5= true;
			destino1 = false;
			destino2= false;
			destino3 = false;
			destino4 = false;
			mainWifi.startScan();
			mainText.setText("\nStarting Scan...\n");
			sb.append("destino:" +destino);

		}
		 */

	}



	public void guiarAteDestino(String destino, String origem, double distOrigem, double distDestino){
		//*************percurso A até D********************
		//AD
		if( destino.equals(D) && origem.equals(A)) {
			destino4 = true;
			double d = (25 - distOrigem);
			percursoA(origem, destino,d);
		}

		//BD
		if( destino.equals(D) && origem.equals(B)) {
			destino4 = true;
			double d = (15 - distOrigem);
			percursoB(origem, destino,d);
		}

		//CD
		if( destino.equals(D) && origem.equals(C)) {
			destino4 = true;
			double d = (9 - distOrigem);
			percursoCD(origem, destino,d);
		}

		//DD
		if( destino.equals(D) && origem.equals(D)) {
			destino4 = true;
			double d = (5 - distOrigem);
			percursoD(origem, destino,d);
		}

		//*************percurso A até C********************

		//AC
		if( destino.equals(C) && origem.equals(A)) {
			destino3 = true;
			double d = (20 - distOrigem);
			percursoA(origem, destino,d);
		}

		//BD
		if( destino.equals(C) && origem.equals(B)) {
			destino3 = true;
			double d = (10 - distOrigem);
			percursoB(origem, destino,d);
		}

		//CD
		if( destino.equals(C) && origem.equals(C)) {
			destino3 = true;
			double d = (5 - distOrigem);
			percursoCC(origem, destino,d);
		}


		//*************percurso C até A********************

		//CA
		if( destino.equals(A) && origem.equals(C)) {
			destino1 = true;
			double d = (20 - distOrigem);
			percursoCB(origem, destino,d);
		}

		//BA
		if( destino.equals(A) && origem.equals(B)) {
			destino1 = true;
			double d = (10 - distOrigem);
			percursoBA(origem, destino,d);
		}

		//AA
		if( destino.equals(A) && origem.equals(A)) {
			destino3 = true;
			double d = (5 - distOrigem);
			percursoAA(origem, destino,d);
		}


	}

	//padaria para o Banco ..indo para sala cozinha
	public void percursoA(String origem, String destino, double distancia){
		//	if (getDirecao() >40 && getDirecao()<110){

		if (getDirecao() >140 && getDirecao()<250){
			
			repeatTTS.speak("Siga em Frente. Falta cerca de:"+ df.format(distancia)+"metros", TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("\n->Siga em Frente. Você está há aproximadamente:"+ df.format(distancia)+"m da "+destino);
			mainText.setText("Origem1:"+origem);
			Log.d("Origem1","" + origem);
			try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}		

	//Banco para quiosque ..indo para cozinha
	private void percursoB(String origem, String destino, double distancia) {
		//if (getDirecao() >40 && getDirecao()<110){
		if (getDirecao() >140 && getDirecao()<260){
			repeatTTS.speak("Vire à Esquerda, e Siga em frente. Faltam cerca de:"+ df.format(distancia)+"metros", TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("Origem2:"+origem);
			mainText.setText("->Vire à Esquerda, e Siga em frente");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
				//if (getDirecao() >310 && getDirecao()<340){
		//if(getDirecao() >320 && getDirecao()<360 || getDirecao() <140){
		if(getDirecao() >20 && getDirecao()<140){
				
		repeatTTS.speak("Siga em Frente. Falta cerca de:"+ df.format(distancia)+"metros", TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("\n->Siga em Frente. Você está há aproximadamente:"+ df.format(distancia)+"m da "+destino);
			try {
				Thread.sleep(7000); 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	//quiosque para cozinha...
	private void percursoCD(String origem, String destino, double distancia) {
			//	if (getDirecao() >310 && getDirecao()<340){

		if(getDirecao() >20 && getDirecao()<140){
			repeatTTS.speak("Vire à Esquerda!", TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("->Vire à Esquerda, e Siga em frente");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//if(getDirecao() >190 && getDirecao()<310){

		if(getDirecao() >300 && getDirecao()<360 || getDirecao()<20){
			repeatTTS.speak("Siga em Frente. Falta cerca de:"+ df.format(distancia)+"metros", TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("\n->Siga em Frente. Você está há aproximadamente:"+ df.format(distancia)+"m da "+destino);
			try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	//cozinha
	private void percursoD(String origem, String destino, double distancia) {
			//if(getDirecao() >190 && getDirecao()<310){
		if(getDirecao() >300 && getDirecao()<360 || getDirecao()<20){
			repeatTTS.speak("Vire à Direita e ande alguns passos lentos", TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("->Vire à direita, e Siga em frente");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}

		if(getDirecao() >20 && getDirecao()<140){
			repeatTTS.speak("Você chegou ao destino"+destino, TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("\n->Você chegou ao destino:"+ destino);
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		destino="";

	}

	// chegando no Quiosque
	private void percursoCC(String origem, String destino, double distancia) {
		repeatTTS.speak("Você chegou ao destino"+destino, TextToSpeech.QUEUE_FLUSH, null);
		mainText.setText("\n->Você chegou ao destino:"+ destino);
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	//Quiosque para Banco
	public void percursoCB( String origem, String destino, double distancia){
		if (getDirecao() >220 && getDirecao()<300){
			repeatTTS.speak("Siga em Frente. Falta cerca de:"+ df.format(distancia)+"metros", TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("\n->Siga em Frente. Você está há aproximadamente:"+ df.format(distancia)+"m da "+destino);
			try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	//Banco para padaria
	public void percursoBA( String origem, String destino, double distancia){

		if (getDirecao() >220 && getDirecao()<300){
			repeatTTS.speak("Siga em Frente. Falta cerca de:"+ df.format(distancia)+"metros", TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("\n->Siga em Frente. Você está há aproximadamente:"+ df.format(distancia)+"m da "+destino);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	//entrada da padaria para padaria
	private void percursoAA(String origem, String destino, double distancia) {
		if (getDirecao() >220 && getDirecao()<300){
			repeatTTS.speak("Vire à Direita e ande alguns passos lentos", TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("->Vire à Direita e ande alguns passos lentos");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (getDirecao() >300 && getDirecao()<360){
			repeatTTS.speak("Você chegou ao destino"+destino, TextToSpeech.QUEUE_FLUSH, null);
			mainText.setText("\n->Você chegou ao destino:"+ destino);
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}	

	public double deslocamento(double distOrigem, double distDestino){

		double d;
		d = distDestino - distOrigem;
		return d;
	}

	public String destino(String destino){
		return destino;
	}


	/**
	 * onActivityResults handles:
	 *  - retrieving results of speech recognition listening
	 *  - retrieving result of TTS data check
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//check speech recognition result 
		sb = new StringBuilder();
		ScanResult result = null;

		if (requestCode == VR_REQUEST && resultCode == RESULT_OK) 
		{
			//store the returned word list as an ArrayList
			ArrayList<String> suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			//set the retrieved list to display in the ListView using an ArrayAdapter
			//wordList.setAdapter(new ArrayAdapter<String> (this, R.layout.word, suggestedWords.subList(0, 1)));

			txtText.setText(suggestedWords.get(0));
			String text = txtText.getText().toString();

			repeatTTS.speak("O Seu Destino é: "+text, TextToSpeech.QUEUE_FLUSH, null);


			//output Toast message
			//cast the view
			//retrieve the chosen word

			//Toast.makeText(BlinguiActivity.this, "O Seu Destino é: "+text, Toast.LENGTH_SHORT).show();//**alter for your Activity name***

			guiarAteDestino(text);




		}

		//returned from TTS data check
		if (requestCode == MY_DATA_CHECK_CODE) 
		{  
			//we have the data - create a TTS instance
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)  
				repeatTTS = new TextToSpeech(this, (OnInitListener) this);  
			//data not installed, prompt the user to install it  
			else 
			{  
				//intent will take user to TTS download page in Google Play
				Intent installTTSIntent = new Intent();  
				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);  
				startActivity(installTTSIntent);  
			}  
		}



		//call superclass method
		super.onActivityResult(requestCode, resultCode, data);



	}

	/**
	 * onInit fires when TTS initializes
	 */
	public void onInit(int initStatus) { 
		//if successful, set locale
		if (initStatus == TextToSpeech.SUCCESS)  

			repeatTTS.setLanguage(Locale.getDefault());//***choose your own locale here***

	}




	class WifiReceiver extends BroadcastReceiver {

		public void onReceive(Context c, Intent intent) {
			sb = new StringBuilder();
			wifiList = mainWifi.getScanResults();
			count++;
			Comparator<ScanResult> comparator = new Comparator<ScanResult>() {
				@Override
				public int compare(ScanResult lhs, ScanResult rhs) {
					return (lhs.level >rhs.level ? -1 : (lhs.level==rhs.level ? 0 : 1)); //se lhs.level >rhs.level for verdadeiro return -1 , senao  returna (lhs.level==rhs.level ? 0 : 1)
				}
			};
			Collections.sort(wifiList, comparator);
			//	for (int i = 0; i < 3; i++) {
			//	ScanResult result = wifiList.get(i);
			for (ScanResult result : wifiList) {
				if (localizar ==true && destino ==false){
					identificarLocal(result);
					sb.append(result.SSID+":" +distancia(result, "Nascimento") + "m");
					sb.append(result.SSID+":" +distancia(result, "Formar") + "m");
					sb.append(result.SSID+":" +distancia(result, "dlink") + "m");
					sb.append(result.SSID+":" +distancia(result, "Blingui") + "m");
					sb.append("direção"+getDirecao());
					mainText.setText(sb);}

				else if (destino==true && localizar ==true){
					double d1= distancia(result, "Nascimento");
					double d2 = distancia(result, "Blingui");
					double d3 = distancia(result, "Formar");
					double d4 = distancia(result, "dlink");
					sb.append(result.SSID+":" +d1 + "m");
					sb.append(result.SSID+":" +d2 + "m");
					sb.append(result.SSID+":" +d3+ "m");
					sb.append(result.SSID+":" +d4+ "m");
					mainText.setText(sb);
					String origem = identificarLocal(result);

					//quiosque para padaria
					if(destino1==true){
						guiarAteDestino(A,origem,d3,d1);
					}

					//padaria para quiosque
					if(destino3 ==true){
						guiarAteDestino(C,origem,d1,d3);
					}

					//padaria para cozinha
					if(destino4 ==true){
						guiarAteDestino(D,origem,d1,d4);
					}
					//sala 1  para sala 2
					/*if(destino2 ==true){
						guiarAteDestino(B,origem,d1,d2);
					}


					//sala 2  para sala 1

					if(destino1 ==true){
						guiarAteDestino(A,origem,d2,d1);
					}

					//sala 4  para sala 2

					if(destino2 ==true){

						guiarAteDestino(B,origem,d3,d2);

					}

					//sala 2  para sala 4

					else if(destino3 ==true){

						Log.d("OrigemBC1","" + origem+destino);

						guiarAteDestino(C,origem,d2,d3);

					} */
				}

				sb.append("\n");
			}
			if(count < 100){

				mainWifi.startScan();  
			}    
			else{
				unregisterReceiver(receiverWifi);
			}
		}

	}

	//	mainText.setText(sb);

	//  sb.append(networkList.subList(0, networkList.size()));	

	//sb.append(mainWifi.getConnectionInfo().getMacAddress().toString());

	// sb.append(mainWifi.getConnectionInfo().getIpAddress());

	//			Toast t= Toast.makeText(c, "Executou o BroadCast", Toast.LENGTH_LONG);

	//  t.show();



	/*    int  size = wifiList.size();

           	HashMap<String, Integer> signalStrength  = new HashMap<String, Integer>();

          	ArrayList<ScanResult> mItems = new ArrayList<ScanResult>();

             for (int i = 0; i < size; i++) {

              ScanResult result = wifiList.get(i);

                if (!result.SSID.isEmpty()) {

                    String key = result.SSID;

             //       sb.append("ssid:" + result.SSID + "level:"+ result.level+ "Key:"+key);

                    if (!signalStrength.containsKey(key)) {

                        signalStrength.put(key, i);

                        mItems.add(result);

                        sb.append("\n");

                    } else {

                        int position = signalStrength.get(key);

                        ScanResult updateItem = mItems.get(position);

                        if (calculateSignalStength(mainWifi, updateItem.level) > 

                           calculateSignalStength(mainWifi, result.level)) {

                            mItems.set(position, updateItem);

                         //   sb.append("Itens"+mItems);

                          //  sb.append("\n");

                       }

                    }

                }

            }

           sb.append("map"+signalStrength);

           sb.append("Itens"+mItems);

            sb.append("\n");  */

	/*HashMap<String, String> items = new HashMap<String, String>();

               if (wifiList != null) {

                   for (int i=0; i<size; i++){

                	    ScanResult scanresult = mainWifi.getScanResults().get(i);

               	    String ssid = scanresult.SSID;

                       int rssi = scanresult.level;

                       String rssiString = String.valueOf(rssi);

                        sb.append(ssid + "," + rssiString);

                        sb.append("\n");

                    }

                   }

               else{

	               	unregisterReceiver(receiverWifi);

               }

	 */


	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh");
		menu.add(0, 1, 1, "Pause");
		menu.add(0, 2, 2, "Fechar");
		menu.add(0, 3, 3, "Conectar bluetooth");
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		//mainWifi.startScan();
		//	mainText.setText("Starting Scan");
		
		return super.onMenuItemSelected(featureId, item);
	}

	public double calculateDistance(double levelInDb, double freqInMHz)    {
		double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(levelInDb)) / 20.0;
		//DecimalFormat df = new DecimalFormat("#.##");
		return Math.pow(10.0, exp);
	}

	public void acessPointDistance(ScanResult result ,String PA1, String PA2, String PA3){

		double d1 =  calculateDistance((double)result.level, result.frequency);
		double d2 =  calculateDistance((double)result.level, result.frequency);
		double d3 =  calculateDistance((double)result.level, result.frequency);
		ArrayList<String> Vet1 = new ArrayList<String>();


		if (result.SSID.equals(PA1))
		{
			sb.append("teste"+result.BSSID + ": " +result.SSID+":" +result.frequency+":"+ result.level + ", d: " + 
					d1 + "m");
			//	Vet1.add(d1);

			sb.append(Vet1);

		}

		if (result.SSID.equals("Formar"))
		{
			sb.append("teste2"+result.BSSID + ": " +result.SSID+":" +result.frequency+":"+ result.level + ", d: " + 
					d2 + "m");
		}


	}

	public double distancia(ScanResult result, String PA){
		double d = 0;
		if (result.SSID.equals(PA)){

			d =  calculateDistance((double)result.level, result.frequency);

		}
		return d;
	}

	public static int calculateSignalStength(WifiManager wifiManager, int level){
		return wifiManager.calculateSignalLevel(level, 5) + 1;
	}


	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int orientation = (int) event.values[0]; // 
		Log.d("Compass", "Got sensor event: " + event.values[0]);
		    Log.d("Orientação CeluLAR","" + orientation);
		setDirecao(orientation); 
		getDirecao();

	}

	public void setDirecao(int direcao) { // 
		this.direcao = direcao;
	}

	public int getDirecao(){
		Log.d("Direção Blingui","" + direcao);
		return direcao;
	}

}






