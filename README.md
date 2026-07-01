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

e.g. `C:\logs\hostA\monitor\icmp-ping\db01.example.com\log20260629.txt`

Each line is expected to contain a syslog-style timestamp, a `Host` or
`from` token naming the destination, and a numeric latency ending in `ms`
or `S` (seconds), e.g.:

```
Jun 29 08:26:29 hostA pinger: Host db01.example.com is alive 12.345 ms
Jun 29 08:26:31 hostA mcasping: response from db02.example.com 0.045S
```

`mcas`-type pings are assumed to be reported in seconds and are
automatically converted to milliseconds, matching the original awk logic.

> **Note:** the three log files you originally attached are a different,
> general application log (not this ping format) — this tool is meant for
> when you point it at your actual ping-monitor log tree.

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
2. Once built, it auto-loads into the table below.
3. Use the **Filters** row (source / destination / ping type dropdowns) to
   narrow down a specific host pair, then **Apply filter**.
4. **Show stats** — min/max/mean/median/p95/p99/stdev for the current filter.
5. **Find spikes** — lists rows above a latency threshold you set.
6. **Plot latency...** — saves a PNG chart of latency over time.

## Using the command line (`cli.py`)

```bat
:: Build the normal-form file
python cli.py build --root C:\logs --out PingLogs_NormalForm.txt

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
