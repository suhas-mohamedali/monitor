"""
ping_monitor.analyzer
======================

Loads a PingLogs_NormalForm.txt (Timestamp / SourceHost / DestinationHost /
PingType / PingTimeMillis) file and provides the analysis operations the
original shell one-liners were building towards:

- unique host-pair + ping-type discovery
  (equivalent of the `awk '{print "\\Q"$2"\\E\\t\\Q"$3"\\E"}' | sort -u`)
- filtering by source / destination / ping type / time range
- summary statistics (min/max/mean/median/p95/p99/stdev/count)
- simple outage / latency-spike detection
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Optional

import pandas as pd


def load_normal_form(path: Path) -> pd.DataFrame:
    df = pd.read_csv(
        path,
        sep="\t",
        dtype={
            "SourceHost": "string",
            "DestinationHost": "string",
            "PingType": "string",
        },
        parse_dates=["Timestamp"],
    )
    df["PingTimeMillis"] = pd.to_numeric(df["PingTimeMillis"], errors="coerce")
    df = df.dropna(subset=["PingTimeMillis"]).sort_values("Timestamp")
    return df.reset_index(drop=True)


def unique_host_pairs(df: pd.DataFrame) -> pd.DataFrame:
    return (
        df[["SourceHost", "DestinationHost", "PingType"]]
        .drop_duplicates()
        .sort_values(["SourceHost", "DestinationHost", "PingType"])
        .reset_index(drop=True)
    )


def filter_rows(
    df: pd.DataFrame,
    source: Optional[str] = None,
    dest: Optional[str] = None,
    ping_type: Optional[str] = None,
    start: Optional[str] = None,
    end: Optional[str] = None,
) -> pd.DataFrame:
    out = df
    if source:
        out = out[out["SourceHost"] == source]
    if dest:
        out = out[out["DestinationHost"] == dest]
    if ping_type:
        out = out[out["PingType"] == ping_type]
    if start:
        out = out[out["Timestamp"] >= pd.to_datetime(start)]
    if end:
        out = out[out["Timestamp"] <= pd.to_datetime(end)]
    return out.reset_index(drop=True)


@dataclass
class PingStats:
    count: int
    min_ms: float
    max_ms: float
    mean_ms: float
    median_ms: float
    p95_ms: float
    p99_ms: float
    stdev_ms: float

    def as_dict(self) -> dict:
        return self.__dict__


def compute_stats(df: pd.DataFrame) -> Optional[PingStats]:
    if df.empty:
        return None
    s = df["PingTimeMillis"]
    return PingStats(
        count=int(s.count()),
        min_ms=round(float(s.min()), 3),
        max_ms=round(float(s.max()), 3),
        mean_ms=round(float(s.mean()), 3),
        median_ms=round(float(s.median()), 3),
        p95_ms=round(float(s.quantile(0.95)), 3),
        p99_ms=round(float(s.quantile(0.99)), 3),
        stdev_ms=round(float(s.std() or 0.0), 3),
    )


def stats_by_group(df: pd.DataFrame) -> pd.DataFrame:
    """Per source/dest/pingtype summary table - handy overview across a whole file."""
    groups = []
    for (src, dst, pt), g in df.groupby(["SourceHost", "DestinationHost", "PingType"]):
        st = compute_stats(g)
        if st is None:
            continue
        row = {"SourceHost": src, "DestinationHost": dst, "PingType": pt}
        row.update(st.as_dict())
        groups.append(row)
    return pd.DataFrame(groups).sort_values(
        ["SourceHost", "DestinationHost", "PingType"]
    ).reset_index(drop=True)


def detect_spikes(df: pd.DataFrame, threshold_ms: float) -> pd.DataFrame:
    """Rows where latency exceeded threshold_ms."""
    return df[df["PingTimeMillis"] > threshold_ms].reset_index(drop=True)


def detect_gaps(df: pd.DataFrame, expected_interval_seconds: float, gap_factor: float = 3.0) -> pd.DataFrame:
    """
    Flags likely outages: consecutive samples (for the same source/dest/type)
    spaced further apart than `gap_factor` * expected_interval_seconds.
    """
    out_rows = []
    for (src, dst, pt), g in df.groupby(["SourceHost", "DestinationHost", "PingType"]):
        g = g.sort_values("Timestamp")
        diffs = g["Timestamp"].diff().dt.total_seconds()
        gap_mask = diffs > (expected_interval_seconds * gap_factor)
        for idx in g[gap_mask].index:
            pos = g.index.get_loc(idx)
            prev_ts = g.iloc[pos - 1]["Timestamp"]
            out_rows.append({
                "SourceHost": src,
                "DestinationHost": dst,
                "PingType": pt,
                "GapStart": prev_ts,
                "GapEnd": g.loc[idx, "Timestamp"],
                "GapSeconds": diffs.loc[idx],
            })
    return pd.DataFrame(out_rows).sort_values("GapStart") if out_rows else pd.DataFrame(
        columns=["SourceHost", "DestinationHost", "PingType", "GapStart", "GapEnd", "GapSeconds"]
    )
