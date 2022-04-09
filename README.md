# LogParser

This is an app that parses log file(s) and store logs into a .csv file after reformatting.
It's simple and built upon several assumptions:
1. The first occurrence of a unique key(logid) would hold the activity start information, 
and the second occurrence would hold the end information. If there exists more than two occurrence, 
the rest should be treated as other independent events.
2. Unique keys(logid) that appears only once do not need to be recorded.
3. We don't need to keep pre-existing data if the set output file name already exists.



Running Environment:
+ Windows or Mac Platform
+ JDK 17/18

## Quick Start

Pull or download the repo

CD to the "src" folder and enter the following in terminal/command prompt, 
and you will find the sample results in "out.csv" file.
```shell
java LogParser.java out.csv api.log
```

If you wish to parse multiple log files together into a single .csv file,
 simply add the corresponding file names at the end. The app would assume that each
 of the log files contains individual, non-overlapping log information.

```shell
java LogParser.java out.csv api.log api.log api.log
```

