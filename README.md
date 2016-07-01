## Introduction ##
MetCor is modelling software which correlates geographical trajectories to multidimensional data and then analyzes them using trajectory-statistical modelling methods such as PSCF, CWT, RTWC and QTBA.

### Announcements ###
  1. The Downloads section of the google-code project website will soon be deprecated and new downloads are linked to a public Google Drive directory starting from [revision 31](https://code.google.com/p/metcor/source/detail?r=31) onwards. Old, deprecated revisions will still be available in the Downloads section. Downloads can be accessed by clicking the titles of the link in the release notes below, or from the page's sidebar under the "External Links" heading. The link to the entire Google Drive directory will be available soon.
  1. A newly overhauled user guide (PDF) is available! It is still a work in progress and will be updated separately from the subversion repository. **[Download](https://docs.google.com/uc?export=download&id=0Bx4y1oB8xOUvcWdfVXZzNUNIczg)** or **[view on Google Drive](https://docs.google.com/file/d/0Bx4y1oB8xOUvcWdfVXZzNUNIczg/edit?usp=sharing)**.

## Current Available Release ##
**[Rev. 33, Version 1.0](https://docs.google.com/uc?export=download&id=0Bx4y1oB8xOUvWkRBVG5hamNlamM)**
  * added GUI fixes, PSCF percentile thresholds weren't working before, thresholds now must be specified as a range between 0 and 1
  * added GUI revision file information: in order for this to work, the directory structure of the original .zip download must not be modified! (i.e, extract zip file without altering it's resulting structure, and ensure that the zip filename (e.x. "MetCor\_r30v3") exists somewhere in the full directory of the program
  * added a multisite fix for PSCF, QTBA: the maximum number of TAGGED receptors currently loaded into MetCor is now used as a criteria (as opposed [Revision 31](https://code.google.com/p/metcor/source/detail?r=31) and earlier, which used the number of receptors in the tagged and untagged back-trajectories)

**[Rev. 31, Version 1.0](https://docs.google.com/uc?export=download&id=0Bx4y1oB8xOUvWW9odlNGeVpmSEE)**
  * daylight savings time correction for tagging is now optional (GUI and Back-end fix)
  * **multisite-PSCF has now been implemented** - see the newly released manual for details.
  * PSCF now has a fractional weighting scheme based on average-NIJ (see release notes for rev. 27); however, weighting by sourceID is not fractional) - revisions earlier than rev. 30 support non-fractional PSCF weighting schemes
  * The back-trajectory interval field is irrelevant for HYSPLIT analyses: the interval defaults to 1.

**[Rev. 27, Version 1.0](http://code.google.com/p/metcor/downloads/detail?name=MetCor_v1r27.zip)**

  * changed the semantics of the upper and lower bound for RTWC, CWT and QTBA calculations:
For RTWC, CWT and QTBA calculations, lower and upper bounds should be filled as FRACTIONS of the average natural transport potential field values (for QTBA) and the average tagged endpoint population (RTWC, CWT).
  * A tooltip for the weighting table in the GUI was added to explain the weighting change
  * **Use Revisions 25 and earlier if lower and upper bounds should represent the actual values of the average-tagged population or transport-potential function rather than their fractions**

(For more revisions see ReleaseHistory).

## Structure ##
I programmed MetCor in two parts: the console-based modular back-end handles file operations and technical calculations/modelling, while a java-AWT GUI front-end handles all user input with which a single class interacts from the back-end.

> ### Considerations ###
    * The front-end was developed using the NetBeans IDE while the back-end was made from scratch with text-editors and developed there.
    * This started as an informal project, so no formal version control was used. The most recent implementation has been migrated to Subversion, and older implementations will be made available as archives.

> ### Older website and license change ###
> I previously [hosted MetCor at McMaster University](http://www.chemistry.mcmaster.ca/faculty/mccarry/MetCor.html) (Hamilton, ON, CA) where I was an undergraduate student. As of Dec 28, 2012 this googlecode page is now the main page of the project with a new OSI-approved license. The McMaster-hosted webpage contains an old version of MetCor which only supported PSCF calculations.


---


## Publications and Software using/modifying MetCor ##
_Feel free to shoot me an email with a citation of your work so I can add you to the list_
  1. 2. Perišić, M., Rajšić, S., Šoštarić, A., Mijić Z., Stojić A. (2016). Levels of PM10-bound species in Belgrade, Serbia: spatio-temporal distributions and related human health risk estimation. Air Quality, Atmosphere & Health. doi:10.1007/s11869-016-0411-6.
  1. Sofowote, U.M., Rastogi, A.K., Debosz, J., Hopke, P.K., 2014. Advanced receptor modeling of near-real-time, ambient PM2.5 and Its associated components collected at an urban-industrial site in Toronto, Ontario. Atmospheric Pollution Research 5, 13-23, doi: 10.5094/APR.2014.003.
  1. Venier, M., Yuning, M., Hites, R.A. (2012). Bromobenzene Flame Retardants in the Great Lakes Atmosphere. Environmental Science and Technology, 46, 8653 - 8660.
  1. Sofowote, U.M., Hung, H., Rastogi, A.K., Westgate, J.N., DeLuca, P.F., Su, Y., McCarry, B.E. (2010). Assessing the Long Range Transport of PAHs to a Sub‐Arctic Site using Positive Matrix Factorization and Potential Source Contribution Function. Atmospheric Environment, 45(4), 967 - 976
  1. Sofowote, U.M., Hung, H., Rastogi, A.K., Westgate, J.N., Su, Y., Sverko, E., D'Sa, I., Roach, P., Fellin, P., McCarry, B.E. (2010). The Gas/Particle Partitioning of Polycyclic Aromatic Hydrocarbons collected at a sub-Arctic Site in Canada. Atmospheric Environment, 44(38), 4919 - 4926 - _Used to plot back trajectories as a matrix._


