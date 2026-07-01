#!/usr/bin/env python3
"""
Ping Log Monitor - CLI
=======================

A pure-Python replacement for the bash/perl/awk pipeline used to normalise
and analyse ping-monitor logs.

Usage examples
--------------
Build the normal-form file from a folder tree of raw logs:

    python cli.py build --root ./logs --out PingLogs_NormalForm.txt

List every unique source/destination/ping-type combination found:

    python cli.py list-pairs --file PingLogs_NormalForm.txt

Show summary stats for one host pair:

    python cli.py stats --file PingLogs_NormalForm.txt --source hostA --dest db01.example.com --pingtype icmp

Show a stats table across everything:

    python cli.py stats --file PingLogs_NormalForm.txt

Find latency spikes over 100ms:

    python cli.py spikes --file PingLogs_NormalForm.txt --threshold 100

Find likely outages (gaps), assuming pings every 5s:

    python cli.py gaps --file PingLogs_NormalForm.txt --interval 5

Plot latency over time for one host pair (saves a PNG):

    python cli.py plot --file PingLogs_NormalForm.txt --source hostA --dest db01.example.com --pingtype icmp --out latency.png
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from ping_monitor import (
    build_normal_form,
    load_normal_form,
    unique_host_pairs,
    filter_rows,
    compute_stats,
    stats_by_group,
    detect_spikes,
    detect_gaps,
)


def cmd_build(args: argparse.Namespace) -> None:
    def progress(path: Path) -> None:
        print(f"processing: {path}", file=sys.stderr)

    count, errors = build_normal_form(
        root=Path(args.root),
        output_path=Path(args.out),
        tz=None if args.tz.lower() == "none" else args.tz,
        on_progress=progress if args.verbose else None,
    )
    print(f"Wrote {count} rows to {args.out}")
    if errors:
        print(f"\n{len(errors)} file(s) skipped (did not match expected path layout):", file=sys.stderr)
        for e in errors:
            print(f"  - {e}", file=sys.stderr)


def cmd_list_pairs(args: argparse.Namespace) -> None:
    df = load_normal_form(Path(args.file))
    pairs = unique_host_pairs(df)
    if pairs.empty:
        print("No rows found.")
        return
    print(pairs.to_string(index=False))


def cmd_stats(args: argparse.Namespace) -> None:
    df = load_normal_form(Path(args.file))
    df = filter_rows(df, source=args.source, dest=args.dest, ping_type=args.pingtype,
                      start=args.start, end=args.end)
    if args.source or args.dest or args.pingtype:
        st = compute_stats(df)
        if st is None:
            print("No matching rows.")
            return
        for k, v in st.as_dict().items():
            print(f"{k:>10}: {v}")
    else:
        table = stats_by_group(df)
        if table.empty:
            print("No rows found.")
            return
        print(table.to_string(index=False))


def cmd_spikes(args: argparse.Namespace) -> None:
    df = load_normal_form(Path(args.file))
    df = filter_rows(df, source=args.source, dest=args.dest, ping_type=args.pingtype,
                      start=args.start, end=args.end)
    spikes = detect_spikes(df, threshold_ms=args.threshold)
    if spikes.empty:
        print(f"No pings above {args.threshold}ms.")
        return
    print(spikes.to_string(index=False))
    if args.out:
        spikes.to_csv(args.out, index=False)
        print(f"\nSaved to {args.out}")


def cmd_gaps(args: argparse.Namespace) -> None:
    df = load_normal_form(Path(args.file))
    df = filter_rows(df, source=args.source, dest=args.dest, ping_type=args.pingtype,
                      start=args.start, end=args.end)
    gaps = detect_gaps(df, expected_interval_seconds=args.interval, gap_factor=args.factor)
    if gaps.empty:
        print("No gaps detected.")
        return
    print(gaps.to_string(index=False))
    if args.out:
        gaps.to_csv(args.out, index=False)
        print(f"\nSaved to {args.out}")


def cmd_plot(args: argparse.Namespace) -> None:
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt

    df = load_normal_form(Path(args.file))
    df = filter_rows(df, source=args.source, dest=args.dest, ping_type=args.pingtype,
                      start=args.start, end=args.end)
    if df.empty:
        print("No matching rows to plot.")
        return

    fig, ax = plt.subplots(figsize=(12, 5))
    for (src, dst, pt), g in df.groupby(["SourceHost", "DestinationHost", "PingType"]):
        ax.plot(g["Timestamp"], g["PingTimeMillis"], marker=".", markersize=2,
                linewidth=0.8, label=f"{src} -> {dst} ({pt})")
    ax.set_xlabel("Time")
    ax.set_ylabel("Ping time (ms)")
    ax.set_title("Ping latency over time")
    ax.legend(fontsize=8)
    fig.autofmt_xdate()
    fig.tight_layout()
    fig.savefig(args.out, dpi=150)
    print(f"Saved plot to {args.out}")


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(description="Ping log monitor - build & analyse ping-monitor logs.")
    sub = p.add_subparsers(dest="command", required=True)

    b = sub.add_parser("build", help="Scan a folder tree and build the normal-form TSV file.")
    b.add_argument("--root", required=True, help="Root folder to scan (contains .../monitor/*-ping/*/logYYYYMMDD.txt)")
    b.add_argument("--out", default="PingLogs_NormalForm.txt", help="Output TSV path.")
    b.add_argument("--tz", default="Australia/Adelaide", help="Timezone name, or 'none' for naive timestamps.")
    b.add_argument("--verbose", action="store_true", help="Print each file as it is processed.")
    b.set_defaults(func=cmd_build)

    lp = sub.add_parser("list-pairs", help="List unique source/destination/ping-type combinations.")
    lp.add_argument("--file", default="PingLogs_NormalForm.txt")
    lp.set_defaults(func=cmd_list_pairs)

    common = dict(
        source=("--source", "Filter by source host"),
        dest=("--dest", "Filter by destination host"),
        pingtype=("--pingtype", "Filter by ping type (icmp/tcp/mcas/...)"),
        start=("--start", "Only rows >= this timestamp (e.g. 2026-06-29)"),
        end=("--end", "Only rows <= this timestamp"),
    )

    def add_filters(sp: argparse.ArgumentParser) -> None:
        sp.add_argument("--file", default="PingLogs_NormalForm.txt")
        for flag, help_text in common.values():
            sp.add_argument(flag, help=help_text)

    st = sub.add_parser("stats", help="Show summary stats (overall table, or one host pair if filters given).")
    add_filters(st)
    st.set_defaults(func=cmd_stats)

    sp_spikes = sub.add_parser("spikes", help="List pings above a latency threshold.")
    add_filters(sp_spikes)
    sp_spikes.add_argument("--threshold", type=float, required=True, help="Latency threshold in ms.")
    sp_spikes.add_argument("--out", help="Optional CSV output path.")
    sp_spikes.set_defaults(func=cmd_spikes)

    gp = sub.add_parser("gaps", help="Detect likely outages (missing expected pings).")
    add_filters(gp)
    gp.add_argument("--interval", type=float, required=True, help="Expected seconds between pings.")
    gp.add_argument("--factor", type=float, default=3.0, help="Gap must exceed interval * factor to be flagged.")
    gp.add_argument("--out", help="Optional CSV output path.")
    gp.set_defaults(func=cmd_gaps)

    pl = sub.add_parser("plot", help="Plot latency over time to a PNG file.")
    add_filters(pl)
    pl.add_argument("--out", default="latency.png", help="Output PNG path.")
    pl.set_defaults(func=cmd_plot)

    return p


def main(argv=None) -> None:
    parser = build_parser()
    args = parser.parse_args(argv)
    args.func(args)


if __name__ == "__main__":
    main()
