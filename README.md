#   Gym Tracker

##  To Do list

### High priority
-   Dropdowns for exercise field in Workouts
-   Dropdowns for exercises in ExAddScreen

### Medium priority
-   Filter full history by personal bests or latest
-   JSON export for Workouts
-   Add a warning that importing data overwrites the existing data.

### Low priority
-   Add an about screen with version number
-   Add a link to the Github repo in the About, when both are available
-   Add privacy policy to the About screen
-   Refactor main screen to home screen
-   Group the recent history by day
-   Add a timer
    -   Add timer settings on the settings screen
    -   Add timer button to the Exercise addition screen
    -   Hook it into the notifications permission

##  Bugs
-   There is no checking for integrity of the JSON file on import

##  Done
-   FIXED: Editing the name of an exercise doesn't delete the old exercise before adding the new one, maybe because the old name isn't passed to the delete function properly
-   Introduce UUIDs for exercises, so deleting them and editing them actually works properly
-   Add graphs of the best rating of exercises
    -   volume                                      [x]
    -   last set reps                               [x]
    -   all reps                                    [x]
    -   Total reps                                  [x]
    -   Drop-downs for existing exercises           [x]
    -   Last 3 months, last 6 months, last year     [x]
-   FIXED: The home screen loads sluggishly
-   Change recent activity to the last 3 weeks, and group by workout
-   Add editing and deletion to the Workouts list screen
-   Add TSV export
-   Change sorting on home screen and progress screen to by timestamp
-   Fixed: Back button in ExAddScreen from full history brings you back to Home, rather than the history.
-   FIXED After deleting an exercise on the main screen, the exercise is still there, because the data isn't reloaded and the UI state isn't updated after the deletion
-   Add a way of grouping the exercises into workouts in the workouts screen
-   Move away from loads of parameters for the ExAddScreen and WorkoutRunScreen, to sharedViewModels
-   Hook up the ExAddScreen to the progress screen, home screen and WorkoutRunScreen to the backStack such that they refresh histories and go back where they need to go.
-   Move New button on main screen to the top bar
-   Add a graph by recent weeks, of your recent exercises
-   Change those colours for the dark and light modes
-   Rename the Graphs tab to "Progress" with an appropriate icon
-   Name the project, and refactor all names to reflect that
-   Implement the data export button
-   Implement the data import button
-   Move the Add to the dropdown on the full history tab
-   Add a full history tab to the Log screen
-   Add editing to the full history screen
-   Add deleting to the full history screen
-   Add deleting to the main screen
-   Add editing to the main screen
    If the weight is zero, express it as a '-' in the recent activity 
-   Remove the burger menus, and move About into Settings, and Full History into the Log
-   Remove the timer tab
-   Make sure the recent history sets don't push the plus buttons off the screen
-   Change the background on the existing ex add buttons to secondary
-   Add a full history tab to log screen
-   On the Ex Add Screen, enable scrolling for when you add loads of the sets
-   Make an icon or series of icons, and add them to the thing
-   Increase the height of the bottom bar on the exercise addition screen, to be the same height as the bottom bar
-   Get rid of the AddEx route, rolling everything into the exAdd route
-   Add the kg/lb switch to the pre-filled states on the exercise addition screen
-   Remove the splash screen, for slickness
-   Fix the rounding on the recent activity on the main page
-   Add Dark Mode
-   Added dividers between the items on the lazycolumn on the main screen
-   Re-order the main screen by the latest exercise
-   Change overflow on the main screen for names, to a single row
