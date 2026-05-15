package com.chrisgames2003.PianoVisualizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;

final class PianoVisualizerKeyListener implements KeyListener
{
	protected final HashMap<Integer, Boolean> KEY_CODES_MAP = new HashMap<>();
	protected PianoVisualizerGame pianoVisualizer;
	private int[] keyCodes;
	private ScheduledExecutorService fadeOutScheduler;
	private ScheduledFuture<?> fadeOutFuture;
	
	protected PianoVisualizerKeyListener(PianoVisualizerGame pianoVisualizer)
	{
		keyCodes = new int[]
		{
			KeyEvent.VK_Z, KeyEvent.VK_S, KeyEvent.VK_X,
			KeyEvent.VK_D, KeyEvent.VK_C, KeyEvent.VK_V,
			KeyEvent.VK_G, KeyEvent.VK_B, KeyEvent.VK_H,
			KeyEvent.VK_N, KeyEvent.VK_J, KeyEvent.VK_M,
			KeyEvent.VK_COMMA
		};
		for (int code : keyCodes)
			KEY_CODES_MAP.put(code, false);
		KEY_CODES_MAP.put(KeyEvent.VK_1, false);
		KEY_CODES_MAP.put(KeyEvent.VK_2, false);
		KEY_CODES_MAP.put(KeyEvent.VK_3, false);
		KEY_CODES_MAP.put(KeyEvent.VK_4, false);
		KEY_CODES_MAP.put(KeyEvent.VK_5, false);
		KEY_CODES_MAP.put(KeyEvent.VK_ESCAPE, false);
		
		this.pianoVisualizer = pianoVisualizer;
		
		fadeOutScheduler = Executors.newSingleThreadScheduledExecutor();
		fadeOutFuture = null;
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		Boolean keyIsBeingHeld = KEY_CODES_MAP.get(keyCode);
		
		int numericPitch = -1;
		for (int i = 0; i < keyCodes.length; i++)
		{
			if (keyCode == keyCodes[i])
			{
				numericPitch = i + 60;
				break;
			}
		}
		
		if (keyIsBeingHeld != null && !keyIsBeingHeld)
		{
			if (numericPitch > 0)
			{
				try
				{
					pianoVisualizer.receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, numericPitch, 64), -1);
				}
				catch (InvalidMidiDataException e1)
				{
					e1.printStackTrace();
				}
			} // NEW: Test playback
			else if (keyCode == KeyEvent.VK_1)
			{
				pianoVisualizer.menuVisible = !pianoVisualizer.menuVisible;
				pianoVisualizer.repaintGame();
			}
			else if (keyCode == KeyEvent.VK_2)
			{
				pianoVisualizer.fileChooser.setSelectedFile(null);
				int result = pianoVisualizer.fileChooser.showOpenDialog(pianoVisualizer.GAME_PANEL);
				File selectedFile = pianoVisualizer.fileChooser.getSelectedFile();
				if (result == JFileChooser.APPROVE_OPTION && selectedFile != null)
				{
					pianoVisualizer.playbackEnabled = true;
					pianoVisualizer.menuVisible = false;
					new Thread(() ->
					{
						try 
						{	 
							pianoVisualizer.sequencer = MidiSystem.getSequencer(false); // Get a sequencer that's not automatically connected to the default synthesizer
							pianoVisualizer.sequencer.open();
							pianoVisualizer.sequencer.getTransmitter().setReceiver(pianoVisualizer.receiver);
							pianoVisualizer.sequencer.setSequence(MidiSystem.getSequence(selectedFile));
							pianoVisualizer.sequencer.start();
							updateFeedback("Now Playing: " + selectedFile.getName().substring(0, (int) selectedFile.getName().length() - 4), true);
							
							while (pianoVisualizer.sequencer.isRunning())
								Thread.sleep(1000); // wait for playback to finish
							
							pianoVisualizer.sequencer.close();
							pianoVisualizer.playbackEnabled = false;
						}
						catch (MidiUnavailableException | InvalidMidiDataException | IOException | InterruptedException e1)
						{
							e1.printStackTrace();
						}
					}).start();
				}
			}
			else if (keyCode == KeyEvent.VK_3)
			{
				new Thread(() ->
				{
					if (pianoVisualizer.sequencer != null && pianoVisualizer.sequencer.isOpen() && pianoVisualizer.sequencer.isRunning())
					{
						pianoVisualizer.sequencer.stop();
						pianoVisualizer.inputPaused = true;
						pianoVisualizer.menuVisible = false;
						updateFeedback("Finishing Playback...", false);
			            
			            try
			            {
			            	Thread.sleep(10 * (PianoVisualizerGame.GAME_HEIGHT - 142) / pianoVisualizer.noteSpeed + 10);
			            	for (int i = 21; i <= 108; i++)
			            		pianoVisualizer.receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, i, 64), -1);
			            	
			            	pianoVisualizer.receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, 64, 0), -1);
			            	while (pianoVisualizer.notesToAdd.size() > 0)
			            		pianoVisualizer.notesToAdd.removeLast();
			            	
			            	pianoVisualizer.NOTES.clear();
			            	for (PianoKey key : pianoVisualizer.KEYS)
			            		key.color = !key.isBlackSized ? PianoVisualizerGame.WHITE : PianoVisualizerGame.BLACK;
			            	
			            	pianoVisualizer.inputPaused = false;
			            	updateFeedback("Free Play Mode Enabled", true);
						}
			            catch (InvalidMidiDataException | InterruptedException e1)
			            {
							e1.printStackTrace();
			            }
					}
				}).start();
			}
			else if (keyCode == KeyEvent.VK_4)
			{
				pianoVisualizer.colorChooser.setColor(pianoVisualizer.pressedColor);
				pianoVisualizer.chooserDialog = JColorChooser.createDialog(pianoVisualizer.GAME_PANEL, "Choose Key Color", true, pianoVisualizer.colorChooser, new NewKeyColorActionListener(), null);
				pianoVisualizer.chooserDialog.setVisible(true);
			}
			else if (keyCode == KeyEvent.VK_5)
			{
				pianoVisualizer.toggleFullScreen();
			}
			else if (keyCode == KeyEvent.VK_ESCAPE)
			{
				pianoVisualizer.receiver.close();
				if (pianoVisualizer.synthesizer != null && pianoVisualizer.synthesizer.isOpen())
					pianoVisualizer.synthesizer.close();
				
				if (pianoVisualizer.sequencer != null && pianoVisualizer.sequencer.isOpen())
					pianoVisualizer.sequencer.close();
				System.exit(0);
			}
		}
		
		if (keyCode != KeyEvent.VK_2 && keyCode != KeyEvent.VK_4)
			KEY_CODES_MAP.put(keyCode, true);
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		int keyCode = e.getKeyCode(), numericPitch = -1;
		KEY_CODES_MAP.put(keyCode, false);
		
		for (int i = 0; i < keyCodes.length; i++)
		{
			if (keyCode == keyCodes[i])
			{
				numericPitch = i + 60;
				break;
			}
		}
	
		if (numericPitch > 0)
		{
			try
			{
				pianoVisualizer.receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, numericPitch, 64), -1);
			}
			catch (InvalidMidiDataException e1)
			{
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}
	
	private void updateFeedback(String feedback, boolean fadesOut)
	{
		pianoVisualizer.feedback = feedback;
		pianoVisualizer.feedbackVisible = true;
		pianoVisualizer.feedbackOpacity = 1;
		pianoVisualizer.repaintGame();
		if (fadeOutFuture != null)
			fadeOutFuture.cancel(false);
		if (fadesOut)
		{
			fadeOutFuture = fadeOutScheduler.schedule(() ->
			{
				pianoVisualizer.feedbackVisible = false;
				pianoVisualizer.repaintGame();
			}, 2, TimeUnit.SECONDS);
		}
	}
	
	private class NewKeyColorActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			pianoVisualizer.pressedColor = pianoVisualizer.colorChooser.getColor();
			pianoVisualizer.darkPressedColor = PianoVisualizerGame.getShiftedColor(pianoVisualizer.pressedColor, PianoVisualizerGame.WHITE, PianoVisualizerGame.WHITE_BORDER);
			pianoVisualizer.darkNotePressedColor = PianoVisualizerGame.getShiftedColor(pianoVisualizer.darkPressedColor, PianoVisualizerGame.BLACK, PianoVisualizerGame.WHITE_BORDER);
			pianoVisualizer.menuVisible = false;
			updateFeedback("Selected New Key Color", true);
		}
	}
}
