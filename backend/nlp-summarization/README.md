# MentorLink NLP Summarization Service

Python Flask service that uses **PyMuPDF** and **HuggingFace LongT5** to generate structured academic summaries from PDF project reports.

## Requirements

- Python 3.9+
- CPU-only (no GPU required)
- ~2GB RAM for model loading
- ~1.5GB disk for model cache

## Installation

```bash
cd nlp-summarization
pip install -r requirements.txt
```

**Note:** First run will download `google/long-t5-tglobal-base` (~770M params) from HuggingFace. This can take several minutes.

## Run

```bash
python app.py
```

Service runs on `http://localhost:5001` by default. Set `PORT` env var to override.

## API

### Health Check
```
GET /health
```

### Summarize PDF
```
POST /summarize
Content-Type: multipart/form-data
Body: file=<PDF file>
```

**Response:**
```json
{"summary": "Objective:\n...\n\nMethodology:\n...\n\nTechnologies Used:\n...\n\nKey Results:\n...\n\nConclusion:\n..."}
```

**Or send JSON with file path** (when file is on shared filesystem):
```json
{"pdf_path": "/absolute/path/to/file.pdf"}
```

## Pipeline

1. **PDF Extraction** – PyMuPDF
2. **Section Filtering** – Remove Certificate, Acknowledgement, References, etc.
3. **Text Cleaning** – Normalize whitespace, merge broken lines
4. **Chunking** – ~1200 words per chunk
5. **Chunk Summarization** – LongT5 per chunk
6. **Aggregation** – Combine + final summarization pass
7. **Post-Processing** – Deduplicate, format section headings

## Configuration

- `PORT` – HTTP port (default: 5001)
