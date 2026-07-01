#!/usr/bin/env python3
"""
Ping Log Monitor - Desktop GUI
================================

A minimal Tkinter app (ships with every standard Python install, including
on Windows - no extra install needed for the window itself) that wraps the
CLI functionality for people who'd rather point-and-click:

1. Pick the root folder containing your raw ping logs
   (.../<sourceHost>/monitor/<pingType>-ping/<destHost>/logYYYYMMDD.txt)
2. Build the normal-form file
3. Filter by source / destination / ping type
4. View summary stats and a latency chart

Run with:  python gui.py
"""

from __future__ import annotations

import threading
import tkinter as tk
from pathlib import Path
from tkinter import filedialog, messagebox, simpledialog, ttk

from ping_monitor import (
    build_normal_form,
    load_normal_form,
    unique_host_pairs,
    filter_rows,
    compute_stats,
    detect_spikes,
    capture_ping_file,
    write_normal_form,
)

APP_TITLE = "Ping Log Monitor"


class PingMonitorApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title(APP_TITLE)
        self.geometry("980x640")

        self.df = None
        self.normal_form_path: Path | None = None

        self._build_widgets()

    # ------------------------------------------------------------------ UI
    def _build_widgets(self) -> None:
        top = ttk.Frame(self, padding=10)
        top.pack(fill="x")

        ttk.Label(top, text="Raw log root folder:").grid(row=0, column=0, sticky="w")
        self.root_var = tk.StringVar()
        ttk.Entry(top, textvariable=self.root_var, width=60).grid(row=0, column=1, padx=5)
        ttk.Button(top, text="Browse...", command=self._pick_root).grid(row=0, column=2)
        ttk.Button(top, text="Build normal-form file", command=self._build_async).grid(row=0, column=3, padx=5)
        ttk.Button(top, text="Capture single file...", command=self._capture_file).grid(row=0, column=4, padx=5)

        ttk.Label(top, text="Normal-form file:").grid(row=1, column=0, sticky="w", pady=(8, 0))
        self.file_var = tk.StringVar(value="PingLogs_NormalForm.txt")
        ttk.Entry(top, textvariable=self.file_var, width=60).grid(row=1, column=1, padx=5, pady=(8, 0))
        ttk.Button(top, text="Browse...", command=self._pick_file).grid(row=1, column=2, pady=(8, 0))
        ttk.Button(top, text="Load", command=self._load_file).grid(row=1, column=3, padx=5, pady=(8, 0))

        self.status_var = tk.StringVar(value="Ready.")
        ttk.Label(self, textvariable=self.status_var, padding=(10, 0)).pack(fill="x")

        # --- filters -----------------------------------------------------
        filt = ttk.LabelFrame(self, text="Filters", padding=10)
        filt.pack(fill="x", padx=10, pady=5)

        ttk.Label(filt, text="Source host:").grid(row=0, column=0, sticky="w")
        self.source_cb = ttk.Combobox(filt, width=25, values=[])
        self.source_cb.grid(row=0, column=1, padx=5)

        ttk.Label(filt, text="Destination host:").grid(row=0, column=2, sticky="w")
        self.dest_cb = ttk.Combobox(filt, width=25, values=[])
        self.dest_cb.grid(row=0, column=3, padx=5)

        ttk.Label(filt, text="Ping type:").grid(row=0, column=4, sticky="w")
        self.pingtype_cb = ttk.Combobox(filt, width=12, values=[])
        self.pingtype_cb.grid(row=0, column=5, padx=5)

        ttk.Button(filt, text="Apply filter", command=self._apply_filter).grid(row=0, column=6, padx=5)
        ttk.Button(filt, text="Clear", command=self._clear_filter).grid(row=0, column=7)

        # --- results table -------------------------------------------------
        table_frame = ttk.Frame(self, padding=(10, 0))
        table_frame.pack(fill="both", expand=True)

        columns = ("Timestamp", "SourceHost", "DestinationHost", "PingType", "PingTimeMillis")
        self.tree = ttk.Treeview(table_frame, columns=columns, show="headings", height=15)
        for col in columns:
            self.tree.heading(col, text=col)
            self.tree.column(col, width=150, anchor="center")
        self.tree.pack(side="left", fill="both", expand=True)

        scroll = ttk.Scrollbar(table_frame, orient="vertical", command=self.tree.yview)
        self.tree.configure(yscroll=scroll.set)
        scroll.pack(side="right", fill="y")

        # --- action bar ------------------------------------------------
        actions = ttk.Frame(self, padding=10)
        actions.pack(fill="x")
        ttk.Button(actions, text="Show stats", command=self._show_stats).pack(side="left", padx=5)
        ttk.Button(actions, text="Plot latency...", command=self._plot).pack(side="left", padx=5)
        ttk.Label(actions, text="Spike threshold (ms):").pack(side="left", padx=(20, 5))
        self.spike_var = tk.StringVar(value="100")
        ttk.Entry(actions, textvariable=self.spike_var, width=8).pack(side="left")
        ttk.Button(actions, text="Find spikes", command=self._find_spikes).pack(side="left", padx=5)

    # -------------------------------------------------------------- helpers
    def _pick_root(self) -> None:
        d = filedialog.askdirectory(title="Select raw log root folder")
        if d:
            self.root_var.set(d)

    def _pick_file(self) -> None:
        f = filedialog.askopenfilename(title="Select normal-form TSV file",
                                        filetypes=[("Text files", "*.txt"), ("All files", "*.*")])
        if f:
            self.file_var.set(f)

    def _build_async(self) -> None:
        root = self.root_var.get().strip()
        if not root:
            messagebox.showwarning(APP_TITLE, "Pick a root folder first.")
            return
        out = self.file_var.get().strip() or "PingLogs_NormalForm.txt"
        self.status_var.set("Scanning and building... this can take a while for large log sets.")
        self.update_idletasks()

        def worker():
            try:
                count, errors = build_normal_form(Path(root), Path(out))
                msg = f"Wrote {count} rows to {out}."
                if errors:
                    msg += f" ({len(errors)} file(s) skipped - unexpected path layout)"
                self.status_var.set(msg)
                self._load_file()
            except Exception as e:  # noqa: BLE001
                self.status_var.set("Build failed.")
                messagebox.showerror(APP_TITLE, str(e))

        threading.Thread(target=worker, daemon=True).start()

    def _capture_file(self) -> None:
        """
        Normalise a single loose log file (e.g. a raw mcas HostChecker
        export) that isn't sitting in the monitor/*-ping/ folder layout,
        and merge it into the current normal-form file.
        """
        input_path = filedialog.askopenfilename(
            title="Select raw log file to capture",
            filetypes=[("Text files", "*.txt"), ("All files", "*.*")],
        )
        if not input_path:
            return

        source = simpledialog.askstring(
            APP_TITLE, "Source host that ran these checks:", parent=self
        )
        if not source:
            return

        ping_type = simpledialog.askstring(
            APP_TITLE, "Ping type:", initialvalue="mcas", parent=self
        ) or "mcas"

        out = self.file_var.get().strip() or "PingLogs_NormalForm.txt"
        self.status_var.set(f"Capturing {input_path}...")
        self.update_idletasks()

        def worker():
            try:
                rows = list(capture_ping_file(
                    path=Path(input_path), source_host=source, ping_type=ping_type,
                ))
                if not rows:
                    self.status_var.set("No matching ping lines found in that file.")
                    messagebox.showinfo(APP_TITLE, "No matching ping lines found in that file.")
                    return
                count = write_normal_form(rows, Path(out), append=True)
                self.status_var.set(
                    f"Captured {len(rows)} rows from {Path(input_path).name} into {out} ({count} total rows)."
                )
                self._load_file()
            except ValueError as e:
                # Most likely: couldn't infer the year from the filename.
                self.status_var.set("Capture failed.")
                messagebox.showerror(APP_TITLE, str(e))
            except Exception as e:  # noqa: BLE001
                self.status_var.set("Capture failed.")
                messagebox.showerror(APP_TITLE, str(e))

        threading.Thread(target=worker, daemon=True).start()

    def _load_file(self) -> None:
        path = Path(self.file_var.get().strip())
        if not path.exists():
            messagebox.showwarning(APP_TITLE, f"File not found: {path}")
            return
        try:
            self.df = load_normal_form(path)
            self.normal_form_path = path
        except Exception as e:  # noqa: BLE001
            messagebox.showerror(APP_TITLE, f"Could not load file:\n{e}")
            return

        pairs = unique_host_pairs(self.df)
        self.source_cb["values"] = sorted(pairs["SourceHost"].unique().tolist())
        self.dest_cb["values"] = sorted(pairs["DestinationHost"].unique().tolist())
        self.pingtype_cb["values"] = sorted(pairs["PingType"].unique().tolist())

        self.status_var.set(f"Loaded {len(self.df)} rows from {path}.")
        self._render_table(self.df.head(500))

    def _current_filter_kwargs(self) -> dict:
        return dict(
            source=self.source_cb.get() or None,
            dest=self.dest_cb.get() or None,
            ping_type=self.pingtype_cb.get() or None,
        )

    def _apply_filter(self) -> None:
        if self.df is None:
            messagebox.showwarning(APP_TITLE, "Load a normal-form file first.")
            return
        filtered = filter_rows(self.df, **self._current_filter_kwargs())
        self.status_var.set(f"Showing {min(len(filtered), 500)} of {len(filtered)} matching rows.")
        self._render_table(filtered.head(500))

    def _clear_filter(self) -> None:
        self.source_cb.set("")
        self.dest_cb.set("")
        self.pingtype_cb.set("")
        if self.df is not None:
            self._render_table(self.df.head(500))
            self.status_var.set(f"Loaded {len(self.df)} rows.")

    def _render_table(self, df) -> None:
        self.tree.delete(*self.tree.get_children())
        for _, row in df.iterrows():
            self.tree.insert("", "end", values=(
                row["Timestamp"], row["SourceHost"], row["DestinationHost"],
                row["PingType"], f'{row["PingTimeMillis"]:.3f}',
            ))

    def _show_stats(self) -> None:
        if self.df is None:
            messagebox.showwarning(APP_TITLE, "Load a normal-form file first.")
            return
        filtered = filter_rows(self.df, **self._current_filter_kwargs())
        st = compute_stats(filtered)
        if st is None:
            messagebox.showinfo(APP_TITLE, "No matching rows.")
            return
        lines = "\n".join(f"{k}: {v}" for k, v in st.as_dict().items())
        messagebox.showinfo("Ping stats", lines)

    def _find_spikes(self) -> None:
        if self.df is None:
            messagebox.showwarning(APP_TITLE, "Load a normal-form file first.")
            return
        try:
            threshold = float(self.spike_var.get())
        except ValueError:
            messagebox.showwarning(APP_TITLE, "Threshold must be a number.")
            return
        filtered = filter_rows(self.df, **self._current_filter_kwargs())
        spikes = detect_spikes(filtered, threshold_ms=threshold)
        self._render_table(spikes.head(500))
        self.status_var.set(f"{len(spikes)} row(s) above {threshold}ms.")

    def _plot(self) -> None:
        if self.df is None:
            messagebox.showwarning(APP_TITLE, "Load a normal-form file first.")
            return
        filtered = filter_rows(self.df, **self._current_filter_kwargs())
        if filtered.empty:
            messagebox.showinfo(APP_TITLE, "No matching rows to plot.")
            return

        out_path = filedialog.asksaveasfilename(
            title="Save plot as...", defaultextension=".png",
            filetypes=[("PNG image", "*.png")], initialfile="latency.png",
        )
        if not out_path:
            return

        import matplotlib
        matplotlib.use("Agg")
        import matplotlib.pyplot as plt

        fig, ax = plt.subplots(figsize=(12, 5))
        for (src, dst, pt), g in filtered.groupby(["SourceHost", "DestinationHost", "PingType"]):
            ax.plot(g["Timestamp"], g["PingTimeMillis"], marker=".", markersize=2,
                    linewidth=0.8, label=f"{src} -> {dst} ({pt})")
        ax.set_xlabel("Time")
        ax.set_ylabel("Ping time (ms)")
        ax.set_title("Ping latency over time")
        ax.legend(fontsize=8)
        fig.autofmt_xdate()
        fig.tight_layout()
        fig.savefig(out_path, dpi=150)
        plt.close(fig)

        messagebox.showinfo(APP_TITLE, f"Saved plot to:\n{out_path}")


def main() -> None:
    app = PingMonitorApp()
    app.mainloop()


if __name__ == "__main__":
    main()
