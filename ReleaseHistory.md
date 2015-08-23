#MetCor release history (back-end + GUI package releases)

This page is an archive of informal release notes for every Jar-file of the back-end and GUI that has been released, starting from Rev. 15.

**[Rev. 25, Version 1.0](http://code.google.com/p/metcor/downloads/detail?name=MetCor_v1r25.zip)**
  * added an option for QTBA to determine averaging criteria for multisite analysis:

The final QTBA value for _multisite_ analyses is determined by averaging QTBA values obtained for a given receptor site in a grid. By default, the minimum number of receptor sites required is N = 2. However, N can be user-controlled to a different value by including the following optional line at the end of the correlated data file:

```
RECEPTORMAX     N
```
**RECEPTORMAX and N must be separated by a tab!**
Where N is a strictly positive (>=2) integer value representing the minimum number of receptor sites required in a grid, with a QTBA value not -1, to be averaged. Therefore, for a multisite calculation with in a grid cell (i,j) with R receptors and a  "RECEPTORMAX" value of N.

```
QTBA(i,j) = if R < N, QTBA = -1
             = if R >= N, QTBA = average(non-negative QTBA values)
```

Note that this implementation change or definition has no effect or bearing on single site calculation outcomes.

**[Rev. 23, Version 1.0](http://code.google.com/p/metcor/downloads/detail?name=MetCor_v1r23.zip)**
  * added a QTBA fix for multisite analysis
  * added GUI printout fix
  * this version uses the CWT averaging scheme outlined in [Revision 21](https://code.google.com/p/metcor/source/detail?r=21)

**[Rev. 21, Version 1.0](http://code.google.com/p/metcor/downloads/detail?name=MetCor_r21v1.zip) - DEPRECATED FOR QTBA analysis**
  * semantic change in redistributed concentrations method for averaging CWT specifically, which uses a weighted average formula instead of a simple averaging formula(see SVN-diff for more details):
<img src='http://latex.codecogs.com/gif.latex?CWT_{\rm AVG}(m) = \frac{1}{\sum_{\forall i,j}{N_{ijm}}}\sum_{\forall i,j}N_{ijm}CWT(i,j)_{m}%.png' />
  * download [Rev. 18, Version 1.0](http://code.google.com/p/metcor/downloads/detail?name=MetCor_r18v1.zip) for the previous version, which uses:
<img src='http://latex.codecogs.com/gif.latex?CWT_{\rm AVG}(x) = \frac{1}{N_{grids}}\sum_{\forall i,j}CWT(i,j)_{x}%.png' />

where x,m are endpoint identifiers (i.e. back trajectory originating dates/"source IDs"). **all future versions will use the revised CWT averaging scheme unless explicitly specified**

**[Rev. 18, Version 1.0](http://code.google.com/p/metcor/downloads/detail?name=MetCor_r18v1.zip)**
  * minor semantic change in redistributed concentrations method; the wrong method of redistributing concentrations was made before (see SVN-diff for more details).
  * **previous revisions should be considered deprecated for ALL RTWC methods**

**[Rev. 18, Version 1.0](http://code.google.com/p/metcor/downloads/detail?name=MetCor_r18v1.zip)**
  * minor semantic RTWC fix: `World.percentDiff(...)` has been changed to the correct definition of percent convergence. Previous builds added before [Revision 16](https://code.google.com/p/metcor/source/detail?r=16) should not be used for RTWC calculations where convergence criteria are specified.

**[Rev. 15, Version 1.0](http://code.google.com/p/metcor/downloads/detail?name=MetCor_r15v1.zip) - deprecated for RTWC-convergence criteria calculations**
  * matrices for RTWC-based calculations with convergence criteria are printed out during the calculation period if they have at least met the user-defined convergence criteria. For debugging purposes, they are also redundantly printed out at the end
  * minor technical RTWC fix - previous revisions should not be used for RTWC-calculations with convergence criteria