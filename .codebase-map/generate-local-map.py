#!/usr/bin/env python3
"""Deterministic, local-only structural maps for the DAFI Desktop JavaFX application."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "src/main/java"
OUT = ROOT / ".codebase-map"

TYPE_RE = re.compile(r"\b(?:public\s+)?(?:abstract\s+)?(class|interface|record|enum)\s+(\w+)")
METHOD_RE = re.compile(r"^\s*(?:public|protected)\s+(?:static\s+)?[^;={]+?\s+(\w+)\s*\(([^)]*)\)")
IMPORT_RE = re.compile(r"^\s*import\s+(com\.dafi\.desktop\.[\w.]+);", re.M)
ANNOTATION_RE = re.compile(r"^\s*@((?:FXML|RestController|Controller|Service|Repository|Document|RequestMapping|GetMapping|PostMapping|PutMapping|DeleteMapping)\b[^\n]*)", re.M)
FXML_RE = re.compile(r"^\s*@FXML", re.M)
HEADING_RE = re.compile(r"^(#{1,3})\s+(.+?)\s*$", re.M)


def token_estimate(path: Path) -> int:
    return max(1, len(path.read_text(encoding="utf-8")) // 4)


def java_maps() -> None:
    out = OUT / "dafi-desktop"
    out.mkdir(parents=True, exist_ok=True)
    files = sorted(SRC.rglob("*.java"))
    class_to_file: dict[str, str] = {}
    parsed: list[tuple[Path, str, str, list[str], list[str], list[tuple[str, str]], int]] = []

    for file in files:
        text = file.read_text(encoding="utf-8")
        rel = file.relative_to(ROOT).as_posix()
        match = TYPE_RE.search(text)
        kind, name = match.groups() if match else ("file", file.stem)
        class_to_file[name] = rel
        imports = [item.rsplit(".", 1)[-1] for item in IMPORT_RE.findall(text)]
        annotations = ["@" + item.strip() for item in ANNOTATION_RE.findall(text)]
        methods = [(mname, re.sub(r"\s+", " ", args).strip()) for mname, args in METHOD_RE.findall(text)]
        fxml_count = len(FXML_RE.findall(text))
        parsed.append((file, rel, f"{kind} {name}", imports, annotations, methods, fxml_count))

    # Structural tree
    tree = ["# dafi-desktop | Java source tree", "# hexagonal architecture: domain, application, adapters, infrastructure"]
    tree.append(f"# total files: {len(parsed)}")
    tree.append("")

    # Group by layer
    layers = {
        "domain": [],
        "application": [],
        "adapters/inbound": [],
        "adapters/outbound": [],
        "infrastructure": [],
        "tests": [],
    }
    for _, rel, kind_name, *_ in parsed:
        if "/test/" in rel:
            layers["tests"].append(rel)
        elif "/domain/" in rel:
            layers["domain"].append(rel)
        elif "/application/" in rel:
            layers["application"].append(rel)
        elif "/adapters/inbound/" in rel:
            layers["adapters/inbound"].append(rel)
        elif "/adapters/outbound/" in rel:
            layers["adapters/outbound"].append(rel)
        elif "/infrastructure/" in rel:
            layers["infrastructure"].append(rel)
        else:
            layers["infrastructure"].append(rel)

    for layer, files_list in layers.items():
        if files_list:
            tree.append(f"\n## {layer}")
            tree.extend(f"  {f}" for f in sorted(files_list))

    (out / "map-structural.tree").write_text("\n".join(tree) + "\n", encoding="utf-8")

    # Semantic map
    semantic = ["# dafi-desktop structural map | imports shown only for com.dafi.desktop",
                "# legend: cl=class if=interface rc=record en=enum; m=public/protected method; @fxml=FXML fields"]
    edges: list[str] = []
    for _, rel, kind_name, imports, annotations, methods, fxml_count in parsed:
        targets = [class_to_file[item] for item in imports if item in class_to_file]
        semantic.append(f"\n{rel} > {', '.join(targets)}")
        short_kind, name = kind_name.split(" ", 1)
        abbreviations = {"class": "cl", "interface": "if", "record": "rc", "enum": "en", "file": "fl"}
        semantic.append(f"  {abbreviations.get(short_kind, short_kind)} {name}")
        if annotations:
            semantic.append("  " + " ".join(annotations))
        if fxml_count > 0:
            semantic.append(f"  @fxml x{fxml_count}")
        for mname, args in methods:
            semantic.append(f"  m {mname}({args})")
        edges.extend(f"{rel} -> {target}" for target in targets)

    (out / "map-semantic.dsl").write_text("\n".join(semantic) + "\n", encoding="utf-8")

    # Relations graph
    graph = ["# dafi-desktop internal Java import graph"] + sorted(set(edges))
    (out / "map-relations.graph").write_text("\n".join(graph) + "\n", encoding="utf-8")


def documentation_map() -> None:
    """Index Markdown docs without injecting full prose."""
    excluded_parts = {".git", "node_modules", "target", "dist", ".codebase-map"}
    documents = []
    for file in sorted(ROOT.rglob("*.md")):
        rel = file.relative_to(ROOT)
        if any(part in excluded_parts for part in rel.parts):
            continue
        headings = [f"{'  ' * (len(level) - 1)}- {title}" for level, title in HEADING_RE.findall(file.read_text(encoding="utf-8"))]
        documents.append((rel.as_posix(), headings))

    lines = [
        "# dafi-desktop documentation context index",
        "# Architecture: hexagonal (domain, application, adapters, infrastructure)",
        "# Content is intentionally not copied; open only the listed relevant document.",
    ]
    for rel, headings in documents:
        lines.append(f"\n{rel}")
        lines.extend(headings or ["- (no Markdown headings)"])

    (OUT / "docs-context.md").write_text("\n".join(lines) + "\n", encoding="utf-8")


def fxml_map() -> None:
    """Map FXML files and their controller bindings."""
    fxml_dir = ROOT / "src/main/resources/fxml"
    if not fxml_dir.exists():
        return

    lines = ["# dafi-desktop FXML structure", ""]
    for file in sorted(fxml_dir.glob("*.fxml")):
        text = file.read_text(encoding="utf-8")
        rel = file.relative_to(ROOT).as_posix()
        controller = re.search(r'fx:controller="([^"]+)"', text)
        fx_ids = re.findall(r'fx:id="(\w+)"', text)
        actions = re.findall(r'#(\w+)', text)

        lines.append(f"\n{rel}")
        if controller:
            lines.append(f"  controller: {controller.group(1)}")
        if fx_ids:
            lines.append(f"  fx:ids: {', '.join(fx_ids)}")
        if actions:
            lines.append(f"  actions: {', '.join(sorted(set(actions)))}")

    (OUT / "map-fxml.dsl").write_text("\n".join(lines) + "\n", encoding="utf-8")


if __name__ == "__main__":
    if len(sys.argv) != 1:
        raise SystemExit("Usage: generate-local-map.py")
    java_maps()
    documentation_map()
    fxml_map()
    for file in sorted(OUT.rglob("map-*")):
        print(f"{file.relative_to(ROOT)}: {file.stat().st_size} bytes, ~{token_estimate(file)} tokens")
