package com.morkout.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Service.QueueBuffer;
import Service.WordService;


public class ClassicBluetoothServer extends Activity {

	public final static String TAG = "ClassicBluetoothServer";
	BluetoothAdapter mBluetoothAdapter;
	BluetoothServerSocket mBluetoothServerSocket;
	//final SpeechRecognizer speechRecognizer;

	public static final int MAX_LINES = 30;
	public static final int REQUEST_TO_START_BT = 100;
	public static final int REQUEST_FOR_SELF_DISCOVERY = 200;
	public static final int RESULT_SPEECH = 1337;
	public static final int REQUEST_ENABLE_BT = 137;

	private TextView mTvInfo;
	private ImageButton voiceBtn;
	private Button sendeBtn;
	private EditText eingabeTextEdit;
	private ToggleButton listenerToggleButton;
	
	UUID MY_UUID = UUID.fromString("D04E3068-E15B-4482-8306-4CABFA1726E7");

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);


					WordService.getUpperCaseList(text);
                    writeText(WordService.getTime() + ": " + text.get(0) + System.getProperty("line.separator"));
                    QueueBuffer.fuegeWortHinzu(text.get(0));

                }
                break;
            }
        }
    }


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cbslayout);


		mTvInfo = (TextView) findViewById(R.id.info);
		mTvInfo.setMovementMethod(new ScrollingMovementMethod());
		eingabeTextEdit = (EditText) findViewById(R.id.eingabeEditText);


		sendeBtn = (Button) findViewById(R.id.sendeButton);
		sendeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String eingabe = eingabeTextEdit.getText().toString();



				if (!eingabe.toString().isEmpty()) {
					writeText(WordService.getTime() + " " + eingabe);
					QueueBuffer.fuegeWortHinzu(eingabe);
				}



			}
		});

		voiceBtn = (ImageButton) findViewById(R.id.voiceButton);
		voiceBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "de");

				try {
					startActivityForResult(intent, RESULT_SPEECH);

				} catch (ActivityNotFoundException a) {
					Toast t = Toast.makeText(getApplicationContext(),
							"Ups!Ihr Gerät kann Speech to Text benutzen",
							Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});

		listenerToggleButton = (ToggleButton) findViewById(R.id.listenerButton);
		listenerToggleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				boolean on = ((ToggleButton) v).isChecked();

				if (on) {
					Toast.makeText(getApplicationContext(), "on", Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(getApplicationContext(), "off", Toast.LENGTH_SHORT).show();
				}

			}
		});


		// initialize Bluetooth and retrieve info about the BT radio interface
		//mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		if (mBluetoothAdapter == null) {
			writeText("Ihr Gerät unterstüzt nicht Bluetooth! Bitte benutzen sie ein anderes Gerät");
			return;
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				writeText("Bluetooth supported but not enabled");
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_TO_START_BT);
			} else {
				writeText("Ihr Gerät unterschützt Bluetooth");
				new AcceptThread().start();
			}
		}
	}

	public void writeText(String data) {
		mTvInfo.append(data + System.getProperty("line.separator"));

        int linienAnzahl = mTvInfo.getLineCount() - MAX_LINES;
        if(linienAnzahl > 0) {
            int eolIndex = -1;
            CharSequence charSequence = mTvInfo.getText();
            for (int i = 0; i < linienAnzahl; i++) {
                do {
                    eolIndex++;
                } while (eolIndex < charSequence.length() && charSequence.charAt(eolIndex) != '\n');
            }
            if (eolIndex < charSequence.length()) {
                mTvInfo.getEditableText().delete(0, eolIndex + 1);
            } else {
                mTvInfo.setText("");
            }
        }
    }



	private class AcceptThread extends Thread {
		private BluetoothServerSocket mServerSocket;

		public AcceptThread() {
			try {
				mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("ClassicBluetoothServer", MY_UUID);
			} 
			catch (IOException e) {
				final IOException ex = e;
				runOnUiThread(new Runnable() {
					public void run() {
						writeText(ex.getMessage());
					}
				});
			}
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					runOnUiThread(new Runnable() {
						public void run() {
							//mTvInfo.setText(mTvInfo.getText() + "\n\nWaiting for Bluetooth Client ..." );
                            writeText("\n\nWarte auf den Klienten");
						}
					});

					socket = mServerSocket.accept(); // blocking call

				} catch (IOException e) {
					Log.v(TAG, e.getMessage());
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work in a separate thread
					new ConnectedThread(socket).start();
					
					try {
						mServerSocket.close();						
					} catch (IOException e) {
						Log.v(TAG, e.getMessage());
					}
					break;
				}
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mSocket;
		private final OutputStream mOutStream;
		private int bytesRead;
		final private String FILE_TO_BE_TRANSFERRED = "marchmadness.png";
		final private String PATH = Environment.getExternalStorageDirectory().toString() + "/nbsocial/";		

		public ConnectedThread(BluetoothSocket socket) {
			mSocket = socket;
			OutputStream tmpOut = null;

			try {
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
			mOutStream = tmpOut;
		}
					
		
		public void run() {
			byte[] buffer = new byte[1024];

			// transfer a file
			if (mOutStream != null) {

                while(true)
                {
                    if(!QueueBuffer.isEmpty())
                    {
                        sendString(QueueBuffer.getWort(),buffer);
                    }
                }

			}

			new AcceptThread().start();

		}

		private BufferedInputStream getBufferedInputStream (String word)
		{
			InputStream stream = null;
			try {


				stream = new ByteArrayInputStream(word.getBytes("UTF-8"));
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			BufferedInputStream bis = new BufferedInputStream(stream);

			return bis;
		}



		public void sendString(String word, byte[] buffer){
			BufferedInputStream bis = getBufferedInputStream(word);
            sendeBtn.setClickable(false);

			try {
				bytesRead = 0;
				for (int read = bis.read(buffer); read >= 0; read = bis.read(buffer))
				{
					mOutStream.write(buffer, 0, read);
					bytesRead += read;





					runOnUiThread(new Runnable() {
						public void run() {
							writeText(mTvInfo.getText() + ".");
						}
					});

				}

			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
            sendeBtn.setClickable(true);

		}
		public void sendStringArray(List<String> stringarray, byte[] buffer) {

			for(String word: stringarray){
				sendString(word, buffer);
			}

		}
	}
}

