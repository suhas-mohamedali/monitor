# Ping Log Monitor

A pure-Python (no bash / perl / awk required) re-implementation of the
ping-log normalisation and analysis pipeline, built to run natively on a
local Windows machine.

## What it replaces

| Original shell step | Python equivalent |
|---|---|
| `find ... -exec bash -c 'doIt "{}"'` (path metadata extraction) | `ping_monitor/parser.py::parse_file_metadata` |
| `perl -ne 's/.../.../ '` (line filter + field capture) | `ping_monitor/parser.py::parse_line` |
| `awk '...'` (month lookup, mcas s→ms conversion, ISO timestamp) | `ping_monitor/parser.py::normalise_row` |
| `... | sort -u >> PingLogs_NormalForm.txt` | `ping_monitor/parser.py::build_normal_form` |
| `awk '{print "\Q"$2"\E\t\Q"$3"\E"}' | sort -u` (unique host pairs) | `ping_monitor/analyzer.py::unique_host_pairs` |

## Expected input layout

The tool scans a root folder for files matching:

```
<root>/<sourceHost>/monitor/<pingType>-ping/<destHost>/logYYYYMMDD.txt
```

Confirmed real-world example:

```
C:\Logs\server_name\monitor\icmp-ping\dest_server\log20260701.txt
C:\Logs\server_name\monitor\mcas-ping\dest_server\log20260625.txt
C:\Logs\server_name\monitor\tcp-ping\dest_server\log20260701.txt
```

Run the build against `C:\Logs` as the root and it will recurse through all
three `-ping` subfolders under every source host automatically:

```bat
python cli.py build --root C:\Logs --out PingLogs_NormalForm.txt
```

Folder/host names with spaces are fine as long as you quote the path.

Each line is expected to contain a syslog-style timestamp (either
abbreviated or full month names, e.g. `Jun` or `June`), a `Host` or `from`
token naming the destination, and a numeric latency ending in `ms` or `S`
(ISO-8601 duration seconds, e.g. `PT0.045S` from a Java `Duration`), e.g.:

```
Jun 29 08:26:29 hostA pinger: Host db01.example.com is alive 12.345 ms
July 01 00:00:55 [INFO] 64 bytes from vm-paap-pp1-twb.hbs.net.au (10.110.24.174): icmp_seq=1 ttl=60 time=4.00 ms
June 25 00:00:10 [INFO] Host vm-paap-pp1-twb.hbs.net.au [10.110.24.174] is reachable on port 4 (elapsed: PT0.004325512S)
```

**Unit conversion is based on what's actually in each line, not on the
folder name.** Any value suffixed `S` (seconds) is converted to
milliseconds automatically — this covers `mcas-ping` logs, and also covers
`tcp-ping` or `icmp-ping` logs if they happen to come from the same
seconds-reporting HostChecker tool rather than a plain `ping` command. You
don't need to tell the tool which folders report which unit.

> **Note:** the destination host in the output always comes from what's
> written *inside* each log line (e.g. `Host db01.example.com`), not from
> the folder name it was found under. These normally match, but if a log
> ever uses a different name (e.g. an IP or short name vs. the folder's
> FQDN), the line content wins — same as the original bash/perl pipeline.

## Output

`PingLogs_NormalForm.txt` — a tab-separated file:

```
Timestamp               SourceHost  DestinationHost    PingType  PingTimeMillis
2026-06-29T08:26:29     hostA       db01.example.com   icmp      12.345
```

## Setup on Windows

1. Install Python 3.10+ from [python.org](https://www.python.org/downloads/)
   (tick "Add Python to PATH" during install).
2. Unzip this project anywhere, e.g. `C:\Tools\ping_monitor`.
3. Double-click **`run_gui.bat`** — it will create a virtual environment,
   install `pandas` and `matplotlib` automatically on first run, then open
   the app.

No manual `pip install` needed unless you prefer the command line.

## Using the GUI (`run_gui.bat` / `python gui.py`)

1. **Raw log root folder** — browse to the folder containing your log tree,
   click **Build normal-form file**.
2. **Capture single file...** — for a loose file that isn't in the
   `monitor/*-ping/` layout (e.g. one exported directly from a
   HostChecker tool). It asks for the source host and ping type by hand,
   then merges the result into your normal-form file.
3. Once built/loaded, the data auto-loads into the table below.
4. Use the **Filters** row (source / destination / ping type dropdowns) to
   narrow down a specific host pair, then **Apply filter**.
5. **Show stats** — min/max/mean/median/p95/p99/stdev for the current filter.
6. **Find spikes** — lists rows above a latency threshold you set.
7. **Plot latency...** — saves a PNG chart of latency over time.

## Using the command line (`cli.py`)

```bat
:: Build the normal-form file from a folder tree in the monitor/*-ping/ layout
python cli.py build --root C:\Logs --out PingLogs_NormalForm.txt

:: Capture a single loose file that ISN'T in that folder layout
:: (source host and ping type aren't in the file, so you supply them)
python cli.py capture --input log20260625.txt --source server_name --pingtype mcas --out PingLogs_NormalForm.txt --append

:: List every unique source/destination/ping-type combination
python cli.py list-pairs --file PingLogs_NormalForm.txt

:: Stats for everything, grouped by host pair
python cli.py stats --file PingLogs_NormalForm.txt

:: Stats for one specific pair
python cli.py stats --file PingLogs_NormalForm.txt --source hostA --dest db01.example.com --pingtype icmp

:: Latency spikes over 100ms
python cli.py spikes --file PingLogs_NormalForm.txt --threshold 100

:: Likely outages, assuming a ping every 5 seconds
python cli.py gaps --file PingLogs_NormalForm.txt --interval 5

:: Save a latency-over-time chart
python cli.py plot --file PingLogs_NormalForm.txt --source hostA --dest db01.example.com --out latency.png
```

Run `python cli.py <command> --help` for the full option list on any
subcommand.

## Project layout

```
ping_monitor/
├── ping_monitor/
│   ├── __init__.py       # public API
│   ├── parser.py         # path + line parsing, normalisation, build_normal_form
│   └── analyzer.py       # pandas-based filtering, stats, spike/gap detection
├── cli.py                 # command-line interface
├── gui.py                  # Tkinter desktop app
├── requirements.txt
├── run_gui.bat             # Windows: launch the GUI
└── run_build.bat           # Windows: build normal-form file from the command line
```

## Adjusting for a different raw log format

If your real ping-monitor log lines differ from the assumed format, the
only thing you need to edit is `LINE_RE` in `ping_monitor/parser.py` (and
`PATH_INFO_RE` if your folder layout differs) — everything else
(normalisation, dedupe, stats, GUI, plotting) works unchanged against the
resulting fields.
