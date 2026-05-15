package com.chrisgames2003.PianoVisualizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;

import com.christools2003.gamedev.GameFrame;

public final class PianoVisualizerGame extends GameFrame
{	
	protected static final int GAME_WIDTH = 1248;
	protected static final int GAME_HEIGHT = 720;
	protected static final int RIBBON = GAME_HEIGHT - 160;
	protected static final String TITLE_FONT_PATH = "resources/COPRGTB.TTF";
	protected static final String MENU_FONT_PATH = "resources/Abadi MT Std Regular.otf";
	protected static final String SOUND_FONT_PATH = "resources/The C7.sf2";
	protected static final String ICON_PATH = "resources/icon.png";
	protected static final Color WHITE = new Color(0xEAEEF7);
	protected static final Color WHITE_BORDER = new Color(0x5F646D);
	protected static final Color BLACK = new Color(0x303849);
	protected static final Color BACKGROUND = new Color(0x141416);
	protected final PianoKey[] KEYS;
	protected final Set<Note> NOTES;
	protected Color pressedColor = new Color(0x26F653);
	protected Color darkPressedColor = getShiftedColor(pressedColor, WHITE, WHITE_BORDER);
	protected Color darkNotePressedColor = getShiftedColor(darkPressedColor, BLACK, WHITE_BORDER);
	protected int noteSpeed = 2; // doesn't need to be global
	protected int loadingScreenOpacity;
	protected float feedbackOpacity;
	protected float menuOpacity;
	protected ScheduledExecutorService scheduler; // doesn't need to be global
	protected List<Note> notesToAdd;
	protected MidiReceiver receiver;
	protected Synthesizer synthesizer;
	protected Sequencer sequencer;
	protected boolean playbackEnabled;
	protected boolean menuVisible;
	protected boolean inputPaused;
	protected boolean feedbackVisible;
	protected JFileChooser fileChooser;
	protected JColorChooser colorChooser;
	protected JDialog chooserDialog;
	protected String feedback;
	protected Font titleFont;
	protected Font menuFont;
	protected InputStream soundFont;

	public PianoVisualizerGame()
	{
		// Create Frame
		super("PractiKey Piano Visualizer", GAME_WIDTH, GAME_HEIGHT);
		
		// Change window size by 1 pixel if the screen resolution is scaled by 125% (makes the game look better)
		if (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth() / Toolkit.getDefaultToolkit().getScreenSize().getWidth() == 1.25)
		{
			JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(GAME_PANEL);
			frame.setSize(frame.getWidth(), frame.getHeight() + 1);
		}
		
		// Load resources
		try
		{
			ClassLoader loader = getClass().getClassLoader();
			titleFont = Font.createFont(Font.TRUETYPE_FONT, loader.getResourceAsStream(TITLE_FONT_PATH)).deriveFont(Font.PLAIN, 46);
			menuFont = Font.createFont(Font.TRUETYPE_FONT, loader.getResourceAsStream(MENU_FONT_PATH)).deriveFont(Font.PLAIN, 28);
			soundFont = loader.getResourceAsStream(SOUND_FONT_PATH);
			setIcon(ImageIO.read(loader.getResourceAsStream(ICON_PATH)));
		}
		catch (FontFormatException | IOException e)
		{
			e.printStackTrace();
		}
		
		// Draw initial loading screen text
		setColor(new Color(0xE1E6E5));
		drawString("Loading...", 40, 40, 28, 1, menuFont);
		repaintGame();

		// Initialize fields
		String[] noteNamesSharp = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"};
		receiver = new MidiReceiver(this);
		playbackEnabled = false;
		notesToAdd = Collections.synchronizedList(new ArrayList<Note>());
		loadingScreenOpacity = 255;
		feedbackOpacity = 0;
		menuOpacity = 1;
		menuVisible = true;
		feedbackVisible = false;
		inputPaused = false;
		fileChooser = new JFileChooser();
		colorChooser = new JColorChooser();
		
		// Create and set preview panel for color chooser
		ColorSelectionModel selectionModel = colorChooser.getSelectionModel();
		@SuppressWarnings("serial")
		JPanel previewPanel = new JPanel()
		{
			@Override
			public void paintComponent(Graphics g)
			{
				g.setColor(pressedColor);
				g.fillRect(0, 0, 144, 63);
				g.setColor(selectionModel.getSelectedColor());
				g.fillRect(144, 0, 144, 63);
			}
		};
		previewPanel.setPreferredSize(new Dimension(288, 63));
		colorChooser.setPreviewPanel(previewPanel);
		for (AbstractColorChooserPanel colorPanel : colorChooser.getChooserPanels())
		{
            if (!colorPanel.getDisplayName().equals("HSV") && !colorPanel.getDisplayName().equals("RGB"))
            	colorChooser.removeChooserPanel(colorPanel);
        }

		// Initialize and populate KEYS array
		KEYS = new PianoKey[88];
		NOTES = ConcurrentHashMap.newKeySet();
		int whiteKeyCount = 0;
		for (int i = 0; i < KEYS.length; i++)
		{
			int octave = i > 2 ? (i / 12) + 1 : 0;
			String name = noteNamesSharp[i % 12] + octave;
			boolean isBlackSized = name.length() > 2;

			KEYS[i] = new PianoKey(this, name, !isBlackSized ? WHITE : BLACK, isBlackSized);
			KEYS[i].xPos = !isBlackSized ? whiteKeyCount++ * 24 : KEYS[i].xPos;
			KEYS[i].yPos = GAME_HEIGHT - 156;
		}
		KEYS[1].xPos = 20;
		for (int i = 0; i < 7; i++)
		{
			KEYS[4 + i * 12].xPos = 62 + i * 168;
			KEYS[6 + i * 12].xPos = 90 + i * 168;
			KEYS[9 + i * 12].xPos = 132 + i * 168;
			KEYS[11 + i * 12].xPos = 160 + i * 168;
			KEYS[13 + i * 12].xPos = 188 + i * 168;
		}

		// Initiate the game loop for drawing rectangles
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(() ->	gameLoop(), 0, 1000 / 100, TimeUnit.MILLISECONDS); // Updates at 100 FPS

		// Prepare all connected Midi devices for input detection (set the receiver for each one)
		MidiDevice.Info[] midiDevices = MidiSystem.getMidiDeviceInfo();
		for (MidiDevice.Info deviceInfo : midiDevices)
		{
			try
			{
				MidiDevice device = MidiSystem.getMidiDevice(deviceInfo);
				if (device.getMaxTransmitters() != 0)
				{
					device.open();
					device.getTransmitter().setReceiver(receiver);
				}
			}
			catch (MidiUnavailableException e)
			{
				e.printStackTrace();
			}
		}
		
		GAME_PANEL.addKeyListener(new PianoVisualizerKeyListener(this));
		repaintGame();
	}

	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		PianoVisualizerGame pianoVisualizer = new PianoVisualizerGame();	
	}

	protected void gameLoop()
	{
		while (notesToAdd.size() > 0)
			NOTES.add(notesToAdd.removeLast());
		
		boolean[] keyIsPressed = new boolean[88];
		ArrayList<Note> notesToRemove = new ArrayList<>();
		
		for (Note note : NOTES)
		{
			if (note.isFalling)
				note.yPos += note.isGrowing ? 0 : noteSpeed; 
			else
				note.yPos -= noteSpeed;
			
			if (note.isGrowing || note.height < 16)
				note.height += noteSpeed;
			
			boolean noteHitsKey = note.yPos + note.height >= RIBBON && note.yPos < RIBBON;
			boolean noteIsOffScreen = note.yPos > RIBBON || note.yPos + note.height < -2 && !note.isFalling;
			if (noteHitsKey)
				keyIsPressed[note.numericPitch] = true;
			
			if (noteIsOffScreen)
				notesToRemove.add(note);
		}

		for (Note note: NOTES)
		{
			boolean recolored = KEYS[note.numericPitch].color.equals(pressedColor) || KEYS[note.numericPitch].color.equals(darkPressedColor);
			
			if (keyIsPressed[note.numericPitch] && !recolored)
				KEYS[note.numericPitch].color = !KEYS[note.numericPitch].isBlackSized ? pressedColor : darkPressedColor;
			else if (!keyIsPressed[note.numericPitch] && recolored)
				KEYS[note.numericPitch].color = !KEYS[note.numericPitch].isBlackSized ? WHITE : BLACK;
		}
		
		while (notesToRemove.size() > 0)
		{
			NOTES.remove(notesToRemove.removeLast());
			if (NOTES.size() == 0)
				repaintGame();
		}
			
		if (NOTES.size() != 0)
			repaintGame();
		loadingScreenOpacity = loadingScreenOpacity > 0 ? loadingScreenOpacity - 8 : loadingScreenOpacity;
		feedbackOpacity = !feedbackVisible && feedbackOpacity > 0 ? feedbackOpacity - 0.03125f : feedbackOpacity;
		menuOpacity = menuVisible && menuOpacity < 1 ? menuOpacity + 0.03125f : menuOpacity;
		menuOpacity = !menuVisible && menuOpacity > 0 ? menuOpacity - 0.03125f : menuOpacity;
	}
	
	protected void drawMenu()
	{
		setColor(new Color(0, 0, 0, (int) (140 * menuOpacity)));
		fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
		setColor(new Color(0xE1E6E5));
		drawString("PractiKey", 497, 228, 46, menuOpacity, titleFont);
		setColor(new Color(0x8F9696));
		drawString("Open/Close Menu", 452, 312, 28, menuOpacity, menuFont);
		drawString("Play MIDI File", 452, 350, 28, menuOpacity, menuFont);
		drawString("Enter Free Play Mode", 452, 388, 28, menuOpacity, menuFont);
		drawString("Select Key Color", 452, 426, 28, menuOpacity, menuFont);
		drawString("Enter/Exit Fullscreen", 452, 464, 28, menuOpacity, menuFont);
		drawString("Quit", 452, 502, 28, menuOpacity, menuFont);
		drawString("[1]", 740, 312, 28, menuOpacity, menuFont);
		drawString("[2]", 740, 350, 28, menuOpacity, menuFont);
		drawString("[3]", 740, 388, 28, menuOpacity, menuFont);
		drawString("[4]", 740, 426, 28, menuOpacity, menuFont);
		drawString("[5]", 740, 464, 28, menuOpacity, menuFont);
		drawString("[ESC]", 740, 502, 28, menuOpacity, menuFont);
	}
	
	@Override
	protected void drawGame()
	{
		// Draw the game
		if (NOTES != null && KEYS != null)
		{
			// Draw background
			setColor(BACKGROUND);
			fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
			
			// Draw white notes
			for (Note note : NOTES)
			{
				if (note != null && note.width == 22)
					note.draw();
			}

			// Draw black notes
			for (Note note : NOTES)
			{
				if (note != null && note.width == 14)
					note.draw();	
			}
			
			// Draw white keys
			for (PianoKey key : KEYS)
			{
				if (key != null && !key.isBlackSized)
					key.draw();
			}

			// Draw black keys
			for (PianoKey key : KEYS)
			{
				if (key != null && key.isBlackSized)
					key.draw();
			}
			
			// Draw red ribbon
			setColor(new Color(0xDD1533));
			fillRect(0, RIBBON - 4, 1248, 8);
			setColor(getShiftedColor(new Color(0xDD1533), WHITE, WHITE_BORDER));
			fillRect(0, RIBBON - 4, 1248, 2);
			
			// Draw menu and feedback
			if (menuVisible || menuOpacity > 0)
			{
				drawMenu();
				repaintGame();
			}
			if (feedbackVisible || feedbackOpacity > 0)
			{
				setColor(new Color(0xE1E6E5));
				drawString(feedback, 40, 40, 28, feedbackOpacity, menuFont);
				repaintGame();
			}
		}
		
		// Draw loading screen
		if (loadingScreenOpacity > 0)
		{
			setColor(new Color(0, 0, 0, loadingScreenOpacity));
			fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
			repaintGame();
		}
	}

	protected static Color getShiftedColor(Color input, Color reference1, Color reference2)
	{
		float[] inputHSBVals = new float[3], ref1HSBVals = new float[3], ref2HSBVals = new float[3];

		Color.RGBtoHSB(input.getRed(), input.getGreen(), input.getBlue(), inputHSBVals);
		Color.RGBtoHSB(reference1.getRed(), reference1.getGreen(), reference1.getBlue(), ref1HSBVals);
		Color.RGBtoHSB(reference2.getRed(), reference2.getGreen(), reference2.getBlue(), ref2HSBVals);

		float hueShift = ref2HSBVals[0] - ref1HSBVals[0], saturationShift = ref2HSBVals[1] - ref1HSBVals[1], brightnessShift = ref2HSBVals[2] - ref1HSBVals[2];
		float newHue = inputHSBVals[0] + hueShift, newSaturation = inputHSBVals[1] + saturationShift, newBrightness = inputHSBVals[2] + brightnessShift;

		newHue = newHue < 0 ? newHue + 1 : (newHue > 1 ? newHue - 1 : newHue);
		newSaturation = Math.max(0, Math.min(1, newSaturation));
		newBrightness = Math.max(0, Math.min(1, newBrightness));

		return new Color(Color.HSBtoRGB(newHue, newSaturation, newBrightness));
	}
}
