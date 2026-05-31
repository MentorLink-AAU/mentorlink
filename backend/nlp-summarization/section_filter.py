"""
Step 2: Section Filtering.
Remove sections that should not be summarized (Certificate, Acknowledgement,
References, Bibliography, Annexure, Plagiarism Report, etc.)
"""

import re
from typing import List, Tuple

# Keywords that indicate sections to remove (case-insensitive)
REMOVAL_KEYWORDS = [
    "certificate",
    "acknowledgement",
    "acknowledgment",
    "references",
    "bibliography",
    "annexure",
    "annex",
    "plagiarism report",
    "declaration",
    "appendix",
    "appendices",
    "table of contents",
    "contents",
    "index",
    "abbreviations",
]


def _find_section_boundaries(text: str) -> List[Tuple[int, int, str]]:
    """
    Find section headers and their approximate end positions.
    Returns list of (start_idx, end_idx, section_title).
    """
    lines = text.split("\n")
    sections = []
    current_start = 0
    current_text = ""

    for i, line in enumerate(lines):
        stripped = line.strip()
        # Check if this line looks like a section header (often all caps or numbered)
        is_header = False
        for kw in REMOVAL_KEYWORDS:
            if kw in stripped.lower() and len(stripped) < 80:
                is_header = True
                break
        # Also match "CHAPTER X" or "1. References" style
        if re.match(r"^(\d+\.?\s*)?(certificate|acknowledg(e)?ment|references|bibliography|annexure?|plagiarism|declaration|appendix|table of contents)", stripped, re.IGNORECASE):
            is_header = True

        if is_header and current_text.strip():
            end_idx = current_start + len(current_text)
            sections.append((current_start, end_idx, stripped[:50]))
            current_text = ""
            current_start = end_idx
        current_text += line + "\n"

    if current_text.strip():
        sections.append((current_start, current_start + len(current_text), "remaining"))

    return sections


def filter_irrelevant_sections(text: str) -> str:
    """
    Remove sections containing removal keywords using regex-based detection.
    Keeps only the main report body.
    """
    if not text or not text.strip():
        return ""

    lines = text.split("\n")
    result_lines = []
    skip_until_next_section = False
    i = 0

    while i < len(lines):
        line = lines[i]
        stripped = line.strip().lower()

        # Check if this line starts a section to remove
        should_remove = False
        for kw in REMOVAL_KEYWORDS:
            if kw in stripped and len(line.strip()) < 100:
                should_remove = True
                break

        if should_remove:
            # Skip this section - consume lines until we hit another clear section
            # or a substantial blank/header that suggests new section
            skip_until_next_section = True
            i += 1
            continue

        # If we're skipping, look for end of section (blank line + new header, or end of doc)
        if skip_until_next_section:
            # End skip when we hit a line that looks like a new chapter/section (e.g. "1." or "Introduction")
            if stripped and re.match(r"^(\d+\.\s+)?[A-Z].{10,}", line.strip()):
                skip_until_next_section = False
                result_lines.append(line)
            elif i > 0 and lines[i - 1].strip() == "" and stripped and len(stripped) > 5:
                # New paragraph after blank - could be new section
                skip_until_next_section = False
                result_lines.append(line)
            i += 1
            continue

        result_lines.append(line)
        i += 1

    return "\n".join(result_lines)
