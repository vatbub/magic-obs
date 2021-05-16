# Magic OBS

Lately, with the ongoing pandemic, I found myself playing Magic: The Gathering together with my friends on Discord. We
don't fancy to play the Arena version, so we decided to simply point our webcams down on our table so-that we can see
each other's cards on Discord. But with crappy cameras and a heckload of video compression it becomes impossible to read
the opponent's cards. So, it happened pretty frequently that we needed to read the card statistics to our opponents out
loud.

This was eventually pretty annoying, hence, I came up with this tool. Magic-obs creates an overlay that shows your
current life points, and the following statistics about the cards that you currently have on your battlefield:

- Power/Toughness
- Abilities like Flying or Trample

You can then use a streaming software like [OBS](https://obsproject.com/) to overlay this information onto your webcam
image.

## Screenshots

Tba

## Download

Because I'm lazy, there's no download yet. Please follow the instructions below for cloning and compiling.

## Setting up your streaming software

Magic-OBS works with every streaming software that supports color keying, but I personally
recommend [OBS Studio](https://obsproject.com/). For that reason, this guide will assume that you use OBS Studio.

1. After downloading/compiling, double-click `magic-obs-1.0-SNAPSHOT-jar-with-dependencies.jar`
2. Three windows will open: One window displays your current life points, one which is currently just a red rectangle (
   this window will display your card statistics later), and a settings window which allows you to control everything.
3. Open OBS Studio.
4. Create a new scene (using the `+` icon in the `Scenes` section)
5. Look at the `Sources` section in OBS. You will also find a `+` icon there. Click it and add a `Video capture device`.
6. Click `Okay`. Then select your webcam. You might want to click on `Deactivate when not visible`. Click `Okay` again.
7. Click again on the `+` icon in the sources section, but this time, add a `Window capture`.
8. Click `Okay`, then select `[java.exe]: Magic OBS Health Points` under `Window`.
9. Uncheck `Record mouse pointer`, then click on `Okay`.
10. Repeat steps 7 through 9 but this time, select `[java.exe]: Magic OBS Card Statistics` under `Window`.

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

## Using Magic-OBS

The usage is quite simple and should be pretty intuitive. Nevertheless, here's a quick rundown of how to use Magic-OBS.

### Change life points

Click the `+` or `-` button next to  `Health points:` or type the number of health points in the text field. The health
points graphic in OBS will update automatically.

### When you summon a creature to the battlefield...

1. ... click `Add card`. A new card will be spawned.
2. Use the table view to set properties like Power/Toughness.
3. Use the drop down menu in the `Abilities` column to select any abilities that the creature has.

> **Hint:** Magic-OBS will learn over time which abilities you use the most.
> These will appear on the top of the list.
> To change this behaviour, select `Original` or `Alphabet` under `Sort ability list by:`.

> **Note:** If you are missing an ability, feel free to send me a message.
> See the section `Contributing` below for more information.

### When a creature dies...

...click the `Kill` button of that creature in the card table.

## Cloning and compiling

### Prerequisites

- JDK 14 or later ([Download](https://adoptopenjdk.net/))

### Cloning

1. Click on the green button titled `Code`
2. If you never heard of `cloning`, click on `Download ZIP` and unzip it. If you know what cloning is, you already know
   how to do it :)

## Compiling

1. Open `Command prompt`
2. Use the `cd` command to navigate to the folder you just unzipped/cloned
3. Run `mvnw package` and wait for the magic to happen
4. Once Maven is doe compiling, you will find a new folder called `target`. In that folder, you will find the
   file `magic-obs-1.0-SNAPSHOT-jar-with-dependencies.jar` which is the compiled version of the program.

## Contributing

There are lots of ways to contribute to this project. In particular, I need help with the following things:

- Find abilities that are still missing
- Draw icons for new abilities
- Improve some animations
- Deal with the aftermath
  of [Bintray going down](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/)
- Setup a CI
- Create a downloadable jar so-that users don't need to compile the thing on their own

If you feel like helping me with any of these things, feel free to submit an issue in the
issue [here](https://github.com/vatbub/magic-obs/issues) or create a pull request.

## Translations

- German: Mostly by myself, some translations are taken from [here](https://magic.freizeitspieler.de/MTGterms_EN-DE.txt)
  .

## License

All parts of this program are licensed under the [Apache license v2](/LICENSE.txt) except for the parts mentioned below.

Copyright (C) 2021 - 2021 Frederik Kammel

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "
AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.

The following icons were taken from [Flaticon](https://www.flaticon.com/) and is thus licensed under the [Flaticon license](https://www.freepikcompany.com/legal#nav-flaticon-agreement):
- Up/down arrow: Icons made by <a href="https://www.flaticon.com/authors/google" title="Google">Google</a>
  from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com </a>
  
The following fonts are taken from [Google fonts](https://fonts.google.com/) and are licensed under the [Open Font License](https://scripts.sil.org/cms/scripts/page.php?site_id=nrsi&id=OFL):
- [Architects Daughter](https://fonts.google.com/specimen/Architects+Daughter)
