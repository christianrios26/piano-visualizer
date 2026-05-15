package com.chrisgames2003.PianoVisualizer;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

final class MidiReceiver implements Receiver
{
	protected PianoVisualizerGame pianoVisualizer;
	private MidiChannel[] channels;
	private boolean pedalIsDown;
	private boolean[] noteIsOn;
	private ScheduledExecutorService midiEventScheduler;

	protected MidiReceiver(PianoVisualizerGame pianoVisualizer)
	{
		this.pianoVisualizer = pianoVisualizer;
		pedalIsDown = false;
		noteIsOn = new boolean[88];
		midiEventScheduler = Executors.newSingleThreadScheduledExecutor();

		try
		{
			pianoVisualizer.synthesizer = MidiSystem.getSynthesizer();
			pianoVisualizer.synthesizer.open(); // Open the synthesizer
			pianoVisualizer.synthesizer.loadAllInstruments(MidiSystem.getSoundbank(pianoVisualizer.soundFont));
			channels = pianoVisualizer.synthesizer.getChannels();
		}
		catch (MidiUnavailableException | InvalidMidiDataException | IOException e)
		{
			e.printStackTrace();
		}
	}

	// this runs on the sequencer's internal, sequential thread. All MIDI events are received one at a time.
	@Override
	public void send(MidiMessage message, long timeStamp) 
	{
		if (message instanceof ShortMessage)
		{
			if (pianoVisualizer.inputPaused) return;
			ShortMessage sm = (ShortMessage) message;
			int command = sm.getCommand(), data1 = sm.getData1(), data2 = sm.getData2();
			if (command == ShortMessage.NOTE_ON || command == ShortMessage.NOTE_OFF)
			{
				int numericPitch = data1 - 21;
				if (command == ShortMessage.NOTE_ON && data2 != 0)
				{
					pianoVisualizer.notesToAdd.add(new Note
					(
						pianoVisualizer,
						pianoVisualizer.KEYS[numericPitch].name,
						!pianoVisualizer.KEYS[numericPitch].isBlackSized ? pianoVisualizer.pressedColor : pianoVisualizer.darkNotePressedColor,
						pianoVisualizer.KEYS[numericPitch].xPos,
						pianoVisualizer.playbackEnabled ? -18 : PianoVisualizerGame.RIBBON,
						!pianoVisualizer.KEYS[numericPitch].isBlackSized ? 22 : 14,
						numericPitch,
						pianoVisualizer.playbackEnabled
					));
					midiEventScheduler.schedule(() ->
					{
						channels[0].noteOn(data1, data2);
						noteIsOn[numericPitch] = true;
					}, pianoVisualizer.playbackEnabled ? 10 * (PianoVisualizerGame.GAME_HEIGHT - 142) / pianoVisualizer.noteSpeed : 0, TimeUnit.MILLISECONDS);
				}
				else
				{
					for (Note note : pianoVisualizer.NOTES)
					{
						if (note.numericPitch == numericPitch && note.isGrowing)
							note.isGrowing = false;
					}
					
					midiEventScheduler.schedule(() ->
					{
						if (!pedalIsDown)
						{
							channels[0].noteOff(data1);
						}
						noteIsOn[numericPitch] = false;
					}, pianoVisualizer.playbackEnabled ? 10 * (PianoVisualizerGame.GAME_HEIGHT - 142) / pianoVisualizer.noteSpeed : 0, TimeUnit.MILLISECONDS);
				}
			}
			else if (command == ShortMessage.CONTROL_CHANGE)
			{
				midiEventScheduler.schedule(() ->
				{
					pedalIsDown = data2 > 0;
					for (int i = 0; i < noteIsOn.length; i++)
					{
						if (!noteIsOn[i])
						{
							channels[0].noteOff(i + 21);
						}
					}
				}, pianoVisualizer.playbackEnabled ? 10 * (PianoVisualizerGame.GAME_HEIGHT - 142) / pianoVisualizer.noteSpeed : 0, TimeUnit.MILLISECONDS);
			}
		}
	}

	@Override
	public void close() {}
}
