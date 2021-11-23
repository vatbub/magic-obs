# Magic OBS

Lately, with the ongoing pandemic, I found myself playing Magic: The Gathering together with my friends on Discord. We
don't fancy to play the Arena version, so we decided to simply point our webcams down on our table so-that we can see
each other's cards on Discord. But with crappy cameras and a heckload of video compression it becomes impossible to read
the opponent's cards. So, it happened pretty frequently that we needed to read the card statistics to our opponents out
loud.

This was eventually pretty annoying, hence, I came up with this tool. Magic-obs creates an overlay that shows your
current life points, and the following statistics about the cards that you currently have on your battlefield:

- Power/Toughness
- Abilities like Flying, Trample or Haste
- Markers

You can then use a streaming software like [OBS](https://obsproject.com/) to overlay this information onto your webcam
image.

## Screenshots

![Battlefield with Overlay](screenshots/OBS.png)

![Control panel](screenshots/MainWindow.PNG)

![CardStatistics before keying](screenshots/CardStatisticsUnkeyed.PNG)

https://user-images.githubusercontent.com/13379225/142775132-8925aa86-174b-4e34-b58e-f73b72733d9a.mp4

## Download

1. Before downloading, you will need to install Java 15 or
   later ([Download link](https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot))
2. Download the
   launcher [here](https://oss.sonatype.org/content/repositories/snapshots/com/github/vatbub/magic-obs-bootstrap/1.0-SNAPSHOT/magic-obs-bootstrap-1.0-20211121.152509-17-jar-with-dependencies.jar)
   .
3. Double-click the launcher. It will download more necessary files and launch the application. Also, it will check for
   updates every time you start the app.

## Setting up your streaming software

Magic-OBS works with every streaming software that supports color keying, but I personally
recommend [OBS Studio](https://obsproject.com/). For that reason, this guide will assume that you use OBS Studio.

1. After downloading/compiling, double-click `magic-obs-bootstrap-1.0-SNAPSHOT-jar-with-dependencies.jar`
2. Four windows will open: One window displays your current life points, one which is currently just a red rectangle (
   this window will display the cards you have in your battlefield later), and a settings window which allows you to
   control everything. Lastly, a window depicting a day/night graphic will open (This is important if you are playing a
   deck that uses the day/night mechanic).
3. Open OBS Studio.
4. Create a new scene (using the `+` icon in the `Scenes` section)
5. Look at the `Sources` section in OBS. You will also find a `+` icon there. Click it and add a `Video capture device`.
6. Click `Okay`. Then select your webcam. You might want to click on `Deactivate when not visible`. Click `Okay` again.
7. Click again on the `+` icon in the sources section, but this time, add a `Window capture`.
8. Click `Okay`, then select `[java.exe]: Magic OBS Health Points` under `Window`.
9. Uncheck `Record mouse pointer`, then click on `Okay`.
10. Repeat steps 7 through 9 but this time, select `[java.exe]: Magic OBS Card Statistics` under `Window`.
11. If your deck uses the day/night mechanic, repeat steps 7 through 9 again, but this time,
    select `[java.exe]: Magic OBS Day Night` under `Window`.

You should now see your webcam with your health points and the card statistics window on top. Before applying the key, I
suggest you to arrange and scale the overlays on your webcam to your liking. To do that, use the preview window of OBS.

After that, you still need to remove the red background. To do so, repeat the following steps for all window captures
that you just created:

1. Select the window capture in the `Sources` section (by clicking once on it)
2. Click on `Filter` (should be right above the `Sources` section)
3. Click on the `+` icon in the window that just opened and select `Color Key` and click on `Okay`.
4. Under `Key-Color type` select `Red`.
5. If the key looks weird, you might want to play with the rest of the settings to get a better key.
6. When you're done, click on `Okay`.

OBS is now all set.

## Using OBS as a virtual camera

If you are using a meeting software like Discord or Zoom, you might want to use your video feed from OBS as your webcam
in those softwares. In OBS, click on `Activate virtual camera`. Then, in your meeting software, you'll need to select
`OBS Virtual Camera` as your webcam. In Discord or Zoom, simply click the small arrow next to the camera button to get
to that setting.

Confused? [Here's a video tutorial.](https://www.youtube.com/watch?v=66Hp1lItai4)

## Using Magic-OBS

The usage is quite simple and should be pretty intuitive. Nevertheless, here's a quick rundown of how to use Magic-OBS.

### Change life points

Click the `+` or `-` button next to  `Health points:` or type the number of health points in the text field. The health
points graphic in OBS will update automatically.

### When you summon a creature to the battlefield...

1. ... click `Add card`. A new card will be spawned.
2. Use the table view to set properties like Power/Toughness.
3. Use the drop down menu in the `Abilities` column to select any abilities that the creature has.
4. If you wish to duplicate a card, you can click the `x2` button next to that card.
5. You can also use the arrow buttons to change the order of the cards.

> **Hint:** Magic-OBS will learn over time which abilities you use the most.
> These will appear on the top of the list.
> To change this behaviour, select `Original` or `Alphabet` under `Sort ability list by:`.

> **Note:** If you are missing an ability, feel free to send me a message.
> See the section `Contributing` below for more information.

### When a creature dies...

...click the `Kill` button of that creature in the card table.

### Using the Day/Night mechanic

In the Innistrad: Midnight Hunt set, a new mechanic was introduced where it can be day or night. Some creatures behave
differently depending on the day/night state. If some of your cards use that mechanic, you can use the Day/Night-Drop
down to indicate whether it is day or night. A graphic is shown to the other players so-that everyone knows whether it's
day or night.

Unfortunately, you still need to transform your cards manually when day/night changes.

### When your game is over...

...click on `Reset game` and your game will be reset to its initial state (i.e. your HP will be reset to 20, the
battlefield will be cleared and day/night will be reset to `None`).

### Changing the look of things

By clicking `Customize appearance`, a menu will pop up where you can change the look of basically anything. Don't like
the font used for the card statistics? Change it!
Don't like the background image of the health points display? Change it!
Want to change the key color from red to green? Well... you get the point, change it :)

## Cloning and compiling

### Prerequisites

- JDK 15 or later ([Download](https://adoptopenjdk.net/))

### Cloning

1. Click on the green button titled `Code`
2. If you never heard of `cloning`, click on `Download ZIP` and unzip it. If you know what cloning is, you already know
   how to do it :)

## Compiling

1. Open `Command prompt`
2. Use the `cd` command to navigate to the folder you just unzipped/cloned
3. To compile the program, run `mvnw package` and wait for the magic to happen.
4. To actually launch the program, run `mvnw exec:java -pl magic-obs`.
5. Once Maven is done compiling, you will find a new folder called `target`. In that folder, you will find the
   file `magic-obs-1.0-SNAPSHOT.jar` which is the compiled version of the program.

## Contributing

There are lots of ways to contribute to this project. In particular, I need help with the following things:

- Find abilities that are still missing
- Draw icons for new abilities
- Improve some animations
- Setup a CI

If you feel like helping me with any of these things, feel free to submit an issue in the
issue [here](https://github.com/vatbub/magic-obs/issues) or create a pull request.

## Translations

- German: Mostly by myself, some translations are taken from [here](https://magic.freizeitspieler.de/MTGterms_EN-DE.txt)

## License

All parts of this program are licensed under the [Apache license v2](/LICENSE.txt) except for the parts mentioned below.

Copyright (C) 2021 - 2021 Frederik Kammel

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "
AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.

The ability icons are taken from the [MTG Fandom Wiki](https://mtg.fandom.com/wiki/Category:Magic_keyword_images) and
are thus licensed under the [CC BY-NC-SA 2.5](https://creativecommons.org/licenses/by-nc-sa/2.5/). Except for the
ability icons mentioned below, they were originally uploaded by [Fenhl](https://mtg.fandom.com/wiki/User:Fenhl) and were
modified to fit the look of this app.

- Commander icon: Uploaded by [Yandere-sliver](https://mtg.fandom.com/wiki/User:Yandere-sliver)

The Up/down arrow icons are made by <a href="https://www.flaticon.com/authors/google" title="Google">Google</a> and were
taken from [Flaticon](https://www.flaticon.com/) and are thus licensed under
the [Flaticon license](https://www.freepikcompany.com/legal#nav-flaticon-agreement).

The <a href="https://www.freepik.com/free-vector/camping-designs-collection_1082315.htm">Camping designs collection
created by zirconicusso</a> was taken from [Freepik](https://www.freepik.com/) and is thus licensed under
the [Freepik license](https://www.freepikcompany.com/legal#nav-freepik-agreement).

The font [Architects Daughter](https://fonts.google.com/specimen/Architects+Daughter) was taken
from [Google fonts](https://fonts.google.com/) and is licensed under
the [Open Font License](https://scripts.sil.org/cms/scripts/page.php?site_id=nrsi&id=OFL).

The font [Magic: The Gathering](https://www.urbanfonts.com/fonts/Magic:_the_Gathering.htm) was originally taken
from [urbanfonts.com](https://www.urbanfonts.com/fonts/Magic:_the_Gathering.htm) and was modified since.

The [LTT meme](https://rroll.to/GoppRq) was taken from [Reddit](https://rroll.to/kaTQWG) and is licensed under
the [CC BY-NC-SA-RI-KC-OR-LL](https://www.youtube.com/watch?v=dQw4w9WgXcQ).
