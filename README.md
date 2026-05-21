# Piano Visualizer
The application visualizes live note input and music files recorded from a digital piano instrument.

## Running the Application
This build of the application contains a large sound font (.sf2) file that makes hosting and sharing on GitHub somewhat cumbersome. For a demonstration of the application, please contact me directly for a short demo.

## About the Application
The application is intended to be a recreation of other popular piano visualizer applications such as Synthesia and Embers. The idea is to visualize recorded piano music with falling rectangles representing notes that collide with piano keys in time with the music (see https://www.youtube.com/watch?v=mGEyj9qKfZ8 for a clearer visual). Applications like these help beginner pianists learn pieces if they have not learned to read sheet music yet as the visualization shows what notes to play and when to play them.

Additionally, the application also supports visualizing live note inputs from the user. The user is intended to provide live note inputs using a digital piano instrument connected to the computer through a USB port. A keyboard input scheme also exists for testing purposes (keys [Z] through [,] on the bottom row of the keyboard). When live note input is visualized, rectangles representing played notes emerge from the keys and rise upwards. Visualizing live note inputs can be useful for practicing.

## Use Details
The default mode of the application is Free Play Mode, which allows the user to visualize their live note inputs from a digital piano instrument. In this mode, rectangles representing note inputs rise from the keys as inputs are received. The application's main menu screen, which appears on startup, includes five options. Three are not self-explanatory and are explained below:

- Option 2 allows the user to browse for and select a .mid file for playback. Upon selection, the file will begin playing within the application, and rectangles representing the notes will fall down to the keys. The user will hear the music as rectangles collide with the keys. As the file plays, the application is in Playback Mode.
- Option 3 allows the user to halt playback of a .mid file early. Upon selecting the option, the remaining rectangles on the screen will finish falling. Afterwards, the application will switch back to Free Play Mode.
- Option 4 allows the user to change the color of the rectangles that represent notes.
