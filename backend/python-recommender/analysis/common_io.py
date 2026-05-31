"""Shared I/O helpers for matrix factorization recommender."""

from __future__ import annotations

from pathlib import Path
from typing import Dict, List

import numpy as np
import pandas as pd

DEFAULT_GROUP_SIZE = 3


def _normalize_tokens(values: List[str]) -> List[str]:
    seen = set()
    out = []
    for value in values:
        token = str(value).strip().lower()
        if token and token not in seen:
            seen.add(token)
            out.append(token)
    return out


def load_students(path: str) -> List[Dict]:
    p = Path(path)
    if not p.exists():
        raise FileNotFoundError(f"Students file not found: {path}")
    if p.stat().st_size == 0:
        raise ValueError(f"Students file is empty: {path}")

    df = pd.read_excel(p, engine="openpyxl", header=0)
    cols = [str(c).strip().lower() for c in df.columns]

    def _col_idx(default_idx: int, aliases: List[str]) -> int:
        for i, col in enumerate(cols):
            if any(a in col for a in aliases):
                return i
        return min(default_idx, max(0, len(cols) - 1))

    email_idx = _col_idx(0, ["email", "e-mail"])
    name_idx = _col_idx(1, ["full", "name"])
    dept_idx = _col_idx(3, ["department", "dept"])
    year_idx = _col_idx(4, ["year"])
    skills_idx = _col_idx(5, ["skill"])

    students = []
    for _, row in df.iterrows():
        email = str(row.iloc[email_idx]).strip() if pd.notna(row.iloc[email_idx]) else ""
        if not email or "@" not in email:
            continue

        full_name = str(row.iloc[name_idx]).strip() if pd.notna(row.iloc[name_idx]) else ""
        department = str(row.iloc[dept_idx]).strip() if pd.notna(row.iloc[dept_idx]) else ""
        try:
            year = int(float(row.iloc[year_idx])) if pd.notna(row.iloc[year_idx]) else None
        except (TypeError, ValueError):
            year = None
        skills_str = str(row.iloc[skills_idx]) if pd.notna(row.iloc[skills_idx]) else ""
        skills = _normalize_tokens(skills_str.split(","))

        students.append(
            {
                "email": email,
                "full_name": full_name,
                "department": department,
                "year": year,
                "skills": skills,
            }
        )
    return students


def load_faculty(path: str) -> List[Dict]:
    p = Path(path)
    if not p.exists() or p.stat().st_size == 0:
        return []

    df = pd.read_excel(p, engine="openpyxl", header=0)
    cols = [str(c).strip().lower() for c in df.columns]

    def _col_idx(default_idx: int, aliases: List[str]) -> int:
        for i, col in enumerate(cols):
            if any(a in col for a in aliases):
                return i
        return min(default_idx, max(0, len(cols) - 1))

    email_idx = _col_idx(0, ["email", "e-mail"])
    name_idx = _col_idx(1, ["full", "name"])
    exp_idx = _col_idx(3, ["expertise", "skill"])
    max_idx = _col_idx(4, ["max", "group"])

    faculty = []
    for _, row in df.iterrows():
        email = str(row.iloc[email_idx]).strip() if pd.notna(row.iloc[email_idx]) else ""
        if not email or "@" not in email:
            continue
        name = str(row.iloc[name_idx]).strip() if pd.notna(row.iloc[name_idx]) else ""
        exp_str = str(row.iloc[exp_idx]) if pd.notna(row.iloc[exp_idx]) else ""
        expertise = _normalize_tokens(exp_str.split(","))
        try:
            max_groups = max(1, int(float(row.iloc[max_idx]))) if pd.notna(row.iloc[max_idx]) else 3
        except (TypeError, ValueError):
            max_groups = 3
        faculty.append(
            {
                "email": email,
                "full_name": name,
                "expertise": expertise,
                "max_groups": max_groups,
            }
        )
    return faculty


def jaccard(a: List[str], b: List[str]) -> float:
    sa, sb = set(a), set(b)
    if not sa and not sb:
        return 1.0
    union = sa | sb
    if not union:
        return 0.0
    return len(sa & sb) / len(union)


def build_skill_matrix(students: List[Dict]) -> np.ndarray:
    vocab = {}
    for s in students:
        for sk in s["skills"]:
            if sk not in vocab:
                vocab[sk] = len(vocab)
    dim = max(1, len(vocab))
    X = np.zeros((len(students), dim), dtype=float)
    for i, s in enumerate(students):
        for sk in s["skills"]:
            X[i, vocab[sk]] = 1.0
    return X


def cosine_similarity_matrix(X: np.ndarray) -> np.ndarray:
    if X.size == 0:
        return np.zeros((0, 0), dtype=float)
    norms = np.linalg.norm(X, axis=1, keepdims=True)
    norms[norms < 1e-10] = 1.0
    xn = X / norms
    similarity = np.clip(xn @ xn.T, -1.0, 1.0)
    return (similarity + 1.0) / 2.0


def form_groups_from_similarity(students: List[Dict], sim: np.ndarray, group_size: int = DEFAULT_GROUP_SIZE) -> List[Dict]:
    n = len(students)
    if n < group_size:
        return []

    unused = set(range(n))
    groups = []
    while len(unused) >= group_size:
        seed = max(unused, key=lambda i: max((sim[i, j] for j in unused if j != i), default=0.0))
        others = [j for j in unused if j != seed]
        best = None
        best_score = -1.0
        for a in range(len(others)):
            for b in range(a + 1, len(others)):
                j, k = others[a], others[b]
                score = float((sim[seed, j] + sim[seed, k] + sim[j, k]) / 3.0)
                if score > best_score:
                    best_score = score
                    best = (j, k)
        if best is None:
            break
        i, j, k = seed, best[0], best[1]
        groups.append(
            {
                "indices": [i, j, k],
                "students": [students[i], students[j], students[k]],
                "score": round(best_score, 4),
            }
        )
        unused.remove(i)
        unused.remove(j)
        unused.remove(k)
    return groups


def assign_faculty(groups: List[Dict], faculty: List[Dict]) -> List[Dict]:
    if not faculty:
        for group in groups:
            group["faculty"] = None
        return groups

    assigned = [0] * len(faculty)
    for group in groups:
        group_skills = []
        for student in group["students"]:
            group_skills.extend(student["skills"])
        group_skills = _normalize_tokens(group_skills)

        best_idx = -1
        best_score = -1.0
        for idx, fac in enumerate(faculty):
            if assigned[idx] >= fac["max_groups"]:
                continue
            score = jaccard(group_skills, fac["expertise"])
            if score > best_score:
                best_score = score
                best_idx = idx
        group["faculty"] = faculty[best_idx] if best_idx >= 0 else None
        if best_idx >= 0:
            assigned[best_idx] += 1
    return groups


def write_output(path: str, groups: List[Dict], algorithm_name: str) -> None:
    p = Path(path)
    p.parent.mkdir(parents=True, exist_ok=True)

    rows = []
    for i, group in enumerate(groups, start=1):
        students = group["students"]
        faculty = group.get("faculty")
        faculty_name = ""
        if faculty:
            faculty_name = faculty.get("email") or faculty.get("full_name") or ""

        def student_name(idx: int) -> str:
            if idx >= len(students):
                return ""
            return students[idx].get("email") or students[idx].get("full_name") or ""

        rows.append(
            {
                "group_id": f"G{i}",
                "student1": student_name(0),
                "student2": student_name(1),
                "student3": student_name(2),
                "faculty_name": faculty_name,
                "similarity_score": float(group.get("score", 0.0)),
                "algorithm_used": algorithm_name,
            }
        )

    pd.DataFrame(rows).to_excel(p, index=False, engine="openpyxl")
    print(f"Wrote {len(rows)} groups to {p}")
