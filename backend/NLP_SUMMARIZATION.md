# NLP PDF Summarization Module

Automated academic PDF summarization using **PyMuPDF** + **HuggingFace LongT5**.

## Architecture

```
Student uploads PDF → Spring Boot API → Saves file → POST to Python service
                                                        ↓
                                              PyMuPDF text extraction
                                                        ↓
                                              Section filtering + cleaning
                                                        ↓
                                              Hierarchical chunking (~1200 words)
                                                        ↓
                                              LongT5 summarization (per chunk)
                                                        ↓
                                              Final aggregation + post-processing
                                                        ↓
                                              Return structured summary → Store in DB
```

## Quick Start

### 1. Start Python NLP Service

```bash
cd nlp-summarization
pip install -r requirements.txt
python app.py
```

Service runs at `http://localhost:5001`. First run downloads the LongT5 model (~1.5GB).

### 2. Start MentorLink Backend

```bash
mvn spring-boot:run
```

### 3. API Usage

**Summarize report:**
```http
POST /api/projects/{projectId}/summarize-report
Authorization: Bearer <JWT>
Content-Type: multipart/form-data
file: <PDF file>
```

**List summaries:**
```http
GET /api/projects/{projectId}/summaries
Authorization: Bearer <JWT>
```

## Output Format

Structured summary with sections:
- **Objective**
- **Methodology**
- **Technologies Used**
- **Key Results**
- **Conclusion**

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `app.nlp.summarization.url` | `http://localhost:5001` | Python NLP service URL |
| `NLP_SUMMARIZATION_URL` | (env override) | Same as above |

## Database

Table `report_summaries`:
- `project_id`, `student_id` (submitted_by)
- `report_file_path`, `original_filename`
- `generated_summary` (TEXT)
- `created_at`, `updated_at`
