#   Gym Tracker

##  To Do list

### High priority
-   (Feature) (For release) Add an about screen with version number
-   (Feature) Add a link to the Github repo in the About, when both are available
-   (Feature) (For release) Add privacy policy to the About screen
-   (For release) (Change) sharedWorkoutViewModel so that when navigating away from the workout run screen, pressing the workout button on the nav bar takes you back to the active workout

### Medium priority
-   (For release) Add a proper changelog
-   (For release) Setup versioning in Github
-   (For release) Setup versioning in the package, and keep it synced with the Github version
-   (For release) Setup building of the APK on Github
-   (For release) Make a release on Github
-   (For release) Write a How to Build guide for devs
-   (For release) Write basic intro to the app, with screenshots
-   (For release) Work out how to get it submitted to F-droid
-   (Feature) Add a warning that importing data overwrites the existing data.

### Low priority
-   (Feature) Add a timer
    -   Add timer settings on the settings screen               [ ]
    -   Add timer button to the Exercise addition screen        [ ]
    -   Hook it into the notifications permission               [ ]
-   (Feature) Add fatigue index to the graphs tab

##  Bugs
-   (Fix) There is no checking for integrity of the JSON file on import

##  Done
-   (UX/UI) Maybe move the up and down arrows on the ExAddScreen to either side of the reps counter
-   (For release) Stick project on Github
-   (Feature) Add autosave to the ExAddEditScreen
-   (Fix) Time display on the exercise add screen - harmonise with the rest of the app
-   (Feature) Filter full history by personal bests or latest
-   (For release) Tidy up unused semi-complete features
-   (Feature) Workout data import and export
-   (Feature) Dropdowns for exercise field in WorkoutEditScreen
-   (Feature) Dropdowns for exercises in ExAddScreen
-   (Fix) Editing the name of an exercise doesn't delete the old exercise before adding the new one, maybe because the old name isn't passed to the delete function properly
-   (Change) Introduce UUIDs for exercises, so deleting them and editing them actually works properly
-   (Feature) Add graphs of the best rating of exercises
    -   volume                                      [x]
    -   last set reps                               [x]
    -   all reps                                    [x]
    -   Total reps                                  [x]
    -   Drop-downs for existing exercises           [x]
    -   Last 3 months, last 6 months, last year     [x]
-   (Fix) The home screen loads sluggishly
-   (Change) Change recent activity to the last 3 weeks, and group by workout
-   (Feature) Add editing and deletion to the Workouts list screen
-   (Feature) Add TSV export
-   (Change) Change sorting on home screen and progress screen to by timestamp
-   (Fix) Back button in ExAddScreen from full history brings you back to Home, rather than the history.
-   (Fix) After deleting an exercise on the main screen, the exercise is still there, because the data isn't reloaded and the UI state isn't updated after the deletion
-   (Feature) Add a way of grouping the exercises into workouts in the workouts screen
-   (Change) Move away from loads of parameters for the ExAddScreen and WorkoutRunScreen, to sharedViewModels
-   (Fix) Hook up the ExAddScreen to the progress screen, home screen and WorkoutRunScreen to the backStack such that they refresh histories and go back where they need to go.
-   (Change) Move New button on main screen to the top bar
-   (Feature) Add a graph by recent weeks, of your recent exercises
-   (Change) Change those colours for the dark and light modes
-   (Change) Rename the Graphs tab to "Progress" with an appropriate icon
-   (Change) Name the project, and refactor all names to reflect that
-   (Feature) Implement the data export button
-   (Feature) Implement the data import button
-   (Change) Move the Add to the dropdown on the full history tab
-   (Feature) Add a full history tab to the Log screen
-   (Feature) Add editing to the full history screen
-   (Feature) Add deleting to the full history screen
-   (Feature) Add deleting to the main screen
-   (Feature) Add editing to the main screen
    (Change) If the weight is zero, express it as a '-' in the recent activity 
-   (Modify) Remove the burger menus, and move About into Settings, and Full History into the Log
-   (Change) Remove the timer tab
-   (Fix) Make sure the recent history sets don't push the plus buttons off the screen
-   (Change) Change the background on the existing ex add buttons to secondary
-   (Feature) Add a full history tab to log screen
-   (Fix) On the Ex Add Screen, enable scrolling for when you add loads of the sets
-   (Feature) Make an icon or series of icons, and add them to the thing
-   (Fix) Increase the height of the bottom bar on the exercise addition screen, to be the same height as the bottom bar
-   (Change) Get rid of the AddEx route, rolling everything into the exAdd route
-   (Feature) Add the kg/lb switch to the pre-filled states on the exercise addition screen
-   (Change) Remove the splash screen, for slickness
-   (Fix) Fix the rounding on the recent activity on the main page
-   (Feature) Add Dark Mode
-   (Feature) Added dividers between the items on the lazycolumn on the main screen
-   (Change) Re-order the main screen by the latest exercise
-   (Change) Change overflow on the main screen for names, to a single row
