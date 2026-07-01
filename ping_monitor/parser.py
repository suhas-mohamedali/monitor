"""
ping_monitor.parser
====================

Pure-Python re-implementation of the original bash / perl / awk pipeline
used to turn raw ping-monitor logs into a normalised TSV file.

Expected input layout (same as the original `find` pattern
``*/monitor/*-ping/*/log*.txt``)::

    <root>/<sourceHost>/monitor/<pingType>-ping/<destHost>/logYYYYMMDD.txt

Expected raw line content (syslog-style timestamp, then a "Host" or
"from" token naming the destination, then a numeric latency and a unit)::

    Jun 29 08:26:29 ... Host db01.example.com ... 12.345 ms
    Jun 29 08:26:31 ... from db02.example.com ... 0.987S

Output normal-form row (tab separated, matches PingLogs_NormalForm.txt)::

    Timestamp                SourceHost  DestinationHost  PingType  PingTimeMillis
    2026-06-29T08:26:29      hostA       db01.example.com icmp      12.345
"""

from __future__ import annotations

import re
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Iterable, Iterator, Optional
from zoneinfo import ZoneInfo

# ---------------------------------------------------------------------------
# Constants (mirrors the awk BEGIN block: split("Jan Feb ... Dec", m))
# ---------------------------------------------------------------------------

MONTHS = {
    name: idx
    for idx, name in enumerate(
        ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
         "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
        start=1,
    )
}

# Matches the `find . -path '*/monitor/*-ping/*/log*.txt'` selection plus
# the field-extraction regex from the second bash script:
#   \.\/(.*)\/monitor\/([^-]+)-ping\/([^\/]+)\/log([0-9]{4})([0-9]{4})\.txt
PATH_INFO_RE = re.compile(
    r"(?P<source>.*)/monitor/(?P<pingtype>[^-/]+)-ping/(?P<dest>[^/]+)/log(?P<year>\d{4})(?P<monthday>\d{4})\.txt$"
)

# Matches the perl filter/normalise regex:
#   ([a-zA-Z]{3}\s+\d{2}\s+\d+\:\d+\:\d+).*\s+(?:(?:Host|from))\s+([^\s]+).*?([\.\d]+)(\s+ms|S).*
LINE_RE = re.compile(
    r"(?P<mon>[a-zA-Z]{3})\s+(?P<day>\d{2})\s+(?P<time>\d+:\d+:\d+)"
    r".*\s+(?:Host|from)\s+(?P<desthost>\S+)"
    r".*?(?P<value>[.\d]+)(?:\s+ms|S)"
)


@dataclass
class PingRow:
    timestamp: str          # ISO-8601, e.g. 2026-06-29T08:26:29
    source_host: str
    dest_host: str
    ping_type: str
    ping_time_ms: float

    def as_line(self) -> str:
        return (
            f"{self.timestamp}\t{self.source_host}\t{self.dest_host}\t"
            f"{self.ping_type}\t{self.ping_time_ms:.3f}"
        )


class PathParseError(ValueError):
    pass


def find_ping_log_files(root: Path) -> list[Path]:
    """Equivalent of: find . -path '*/monitor/*-ping/*/log*.txt'"""
    root = Path(root)
    return sorted(
        p for p in root.rglob("log*.txt")
        if re.search(r"/monitor/[^-/]+-ping/[^/]+/log\d+\.txt$", p.as_posix())
    )


def parse_file_metadata(path: Path, root: Path) -> dict:
    """
    Derive per-file metadata the same way the shell `doIt()` function did:

    - year          -> first 4 digits after 'log' in the filename
    - from_hostname -> 2nd path component (relative to root), i.e. the
                        directory the pipeline treats as the source host
    - pingType      -> the '<type>-ping' directory component, minus '-ping'
    - destHostFromPath -> the directory the file lives directly under
                           (kept for reference / validation)
    """
    rel = path.relative_to(root).as_posix()
    m = PATH_INFO_RE.search(rel)
    if not m:
        raise PathParseError(f"Path did not match expected ping-log layout: {rel}")

    parts = rel.split("/")
    if len(parts) < 2:
        raise PathParseError(f"Path too shallow to contain a source host: {rel}")
    from_hostname = parts[0]

    return {
        "source_host": from_hostname,
        "ping_type": m.group("pingtype"),
        "dest_host_from_path": m.group("dest"),
        "year": m.group("year"),
        "month_day": m.group("monthday"),
    }


def parse_line(line: str) -> Optional[dict]:
    m = LINE_RE.search(line)
    if not m:
        return None
    return {
        "mon": m.group("mon"),
        "day": m.group("day"),
        "time": m.group("time"),
        "dest_host": m.group("desthost"),
        "value": m.group("value"),
    }


def normalise_row(
    mon: str,
    day: str,
    time_str: str,
    year: str,
    source_host: str,
    dest_host: str,
    ping_type: str,
    raw_value: str,
    tz: Optional[str] = None,
) -> Optional[PingRow]:
    """Equivalent of the awk normalisation stage."""
    month_num = MONTHS.get(mon[:3].title())
    if month_num is None:
        return None
    try:
        h, mi, s = (int(x) for x in time_str.split(":"))
        dt = datetime(int(year), month_num, int(day), h, mi, s)
        if tz:
            dt = dt.replace(tzinfo=ZoneInfo(tz))
    except ValueError:
        return None

    try:
        value = float(raw_value)
    except ValueError:
        return None

    # mcas pings are reported in seconds -> convert to ms, same as the
    # original: `if ($6 == "mcas") { pt=pt*1000.0 }`
    if ping_type == "mcas":
        value *= 1000.0

    return PingRow(
        timestamp=dt.strftime("%Y-%m-%dT%H:%M:%S"),
        source_host=source_host,
        dest_host=dest_host,
        ping_type=ping_type,
        ping_time_ms=value,
    )


def process_file(path: Path, root: Path, tz: Optional[str] = None) -> Iterator[PingRow]:
    meta = parse_file_metadata(path, root)
    year = meta["year"]
    source_host = meta["source_host"]
    ping_type = meta["ping_type"]

    with path.open("r", errors="replace") as fh:
        for line in fh:
            parsed = parse_line(line)
            if not parsed:
                continue
            row = normalise_row(
                mon=parsed["mon"],
                day=parsed["day"],
                time_str=parsed["time"],
                year=year,
                source_host=source_host,
                dest_host=parsed["dest_host"],
                ping_type=ping_type,
                raw_value=parsed["value"],
                tz=tz,
            )
            if row:
                yield row


def build_normal_form(
    root: Path,
    output_path: Path,
    tz: Optional[str] = "Australia/Adelaide",
    on_progress=None,
) -> tuple[int, list[str]]:
    """
    Walk `root` for ping-log files, normalise every matching line, dedupe +
    sort, and write the result (with header) to `output_path`.

    Returns (row_count, list_of_files_with_path_errors).
    """
    root = Path(root)
    files = find_ping_log_files(root)
    errors: list[str] = []
    rows: set[str] = set()

    for f in files:
        if on_progress:
            on_progress(f)
        try:
            for row in process_file(f, root, tz=tz):
                rows.add(row.as_line())
        except PathParseError as e:
            errors.append(str(e))

    sorted_rows = sorted(rows)

    with Path(output_path).open("w", newline="\n") as out:
        out.write("Timestamp\tSourceHost\tDestinationHost\tPingType\tPingTimeMillis\n")
        for line in sorted_rows:
            out.write(line + "\n")

    return len(sorted_rows), errors
