"""
Step 11: Post-Processing.
Remove duplicate sentences, normalize whitespace, format section headings.
"""

import re
from typing import List


def _split_sentences(text: str) -> List[str]:
    """Split text into sentences (simple heuristic)."""
    sentences = re.split(r"(?<=[.!?])\s+", text)
    return [s.strip() for s in sentences if s.strip()]


def _deduplicate_sentences(text: str) -> str:
    """Remove duplicate consecutive sentences."""
    sentences = _split_sentences(text)
    seen = set()
    unique = []
    for s in sentences:
        key = s.lower().strip()
        if key not in seen:
            seen.add(key)
            unique.append(s)
    return " ".join(unique)


def _format_section_headings(text: str) -> str:
    """
    Ensure structured layout:
    Objective:
    ...
    Methodology:
    ...
    etc.
    """
    section_keywords = [
        "Objective",
        "Methodology",
        "Technologies Used",
        "Key Results",
        "Conclusion",
    ]

    result = text
    # Normalize section headers - ensure they end with colon and newline
    for kw in section_keywords:
        # Match "Objective" or "Objective:" at start of line or after newline
        pattern = rf"(\n|^)\s*({re.escape(kw)})\s*:?\s*"
        replacement = rf"\1\2:\n"
        result = re.sub(pattern, replacement, result, flags=re.IGNORECASE)

    # Normalize whitespace
    result = re.sub(r" +", " ", result)
    result = re.sub(r"\n{3,}", "\n\n", result)
    result = result.strip()

    return result


def post_process_summary(summary: str) -> str:
    """
    Post-process the final summary:
    - Remove duplicate sentences
    - Normalize whitespace
    - Format section headings
    """
    if not summary or not summary.strip():
        return summary

    s = _deduplicate_sentences(summary)
    s = _format_section_headings(s)
    s = re.sub(r" +", " ", s).strip()
    return s
