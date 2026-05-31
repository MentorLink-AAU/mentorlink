"""
Common runner for the matrix factorization recommender.
"""

from __future__ import annotations

import argparse
import sys
from typing import Callable

from common_io import assign_faculty, form_groups_from_similarity, load_faculty, load_students, write_output


def run_pipeline(
    build_similarity: Callable,
    algorithm_name: str,
    students_path: str,
    faculty_path: str,
    output_path: str,
    group_size: int = 3,
):
    students = load_students(students_path)
    if len(students) < group_size:
        raise ValueError(f"Need at least {group_size} students.")
    faculty = load_faculty(faculty_path)

    print(f"Loaded {len(students)} students and {len(faculty)} faculty")
    print(f"Building similarity matrix for {algorithm_name} ...")
    sim = build_similarity(students, faculty)
    groups = form_groups_from_similarity(students, sim, group_size=group_size)
    assign_faculty(groups, faculty)
    write_output(output_path, groups, algorithm_name)
    print(f"{algorithm_name}: wrote {len(groups)} groups to {output_path}")


def run_cli(build_similarity: Callable, algorithm_name: str):
    parser = argparse.ArgumentParser(description=f"{algorithm_name} recommender")
    parser.add_argument("students", help="Path to students Excel")
    parser.add_argument("faculty", help="Path to faculty Excel")
    parser.add_argument("output", help="Path to output Excel")
    parser.add_argument("--group-size", type=int, default=3, help="Students per group (default: 3)")
    args = parser.parse_args()

    try:
        run_pipeline(
            build_similarity=build_similarity,
            algorithm_name=algorithm_name,
            students_path=args.students,
            faculty_path=args.faculty,
            output_path=args.output,
            group_size=max(2, int(args.group_size)),
        )
        return 0
    except Exception as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1
