# Sift — AI Recruiting Assistant

A scaled-down AI recruiting pipeline built to explore how retrieval-augmented generation (RAG) makes AI hiring tools explainable instead of a black box.

Sift generates job descriptions, filters candidates with semantic retrieval before scoring them, drafts per-candidate interview prep, and can send AI-drafted outreach emails to shortlisted candidates — all with a human approving the final send.

> Portfolio project. Not affiliated with any commercial recruiting platform.

---

## Why this exists

The naive way to do AI candidate screening is to paste a full job description and a full resume into a prompt and ask for a match score. That works for five candidates. At fifty, the model starts drowning real signal in generic language — "collaborative team player," "fast-paced environment" — that shows up in every profile whether or not the person can do the job.

Sift's answer: narrow the candidate pool cheaply with semantic retrieval first, then spend the expensive, explainable scoring step only on candidates worth reading closely. It's the same two-stage shape production recruiting platforms use at real scale, built small enough to fit in a portfolio project.

---

## What it actually does

| Feature | Description |
|---|---|
| **Job description generator** | Turns a one-line role idea into a structured job post, plus a distilled key-skills list used later for retrieval. |
| **RAG-filtered candidate scoring** | Embeds every candidate profile locally, retrieves the subset most semantically similar to the role, and only sends *those* candidates to the LLM for a 0–100 score with a written explanation. |
| **Interview prep briefing** | Per-candidate strengths, gaps to probe, and three tailored interview questions, generated on demand. |
| **Shortlist outreach (human-in-the-loop)** | Candidates scoring above a threshold are surfaced for a recruiter to review. On approval, an AI-drafted outreach email is sent through a sandboxed mail service — nothing sends automatically without a click. |

---

## How a candidate moves through the pipeline

```
1. Describe the role
   → LLM turns a rough idea into a structured job post + key-skills list

2. Embed the pool
   → Every candidate profile is embedded locally (no API cost, no data leaves the machine)

3. Retrieve, don't guess          [RAG]
   → The role's key skills are embedded too; cosine similarity narrows
     the pool to the candidates actually worth reading closely

4. Score with reasons
   → Only retrieved candidates reach the LLM — each gets a score
     AND a plain-language explanation of what matched and what didn't

5. Brief the interviewer
   → Pick a scored candidate, get strengths, gaps, and targeted questions

6. Shortlist & outreach            [human approves]
   → High scorers surface in a shortlist; a recruiter reviews and
     manually triggers a drafted outreach email
```

Retrieval and scoring are deliberately separate steps. Not because it looks more sophisticated — because they solve different problems. Retrieval is cheap and approximate; scoring is expensive and needs to be explainable.

---

## Tech stack

**Frontend**
- HTML, CSS, JavaScript (no framework — a landing page + a working demo app)

**Backend**
- Java, Spring Boot, Spring Data JPA

**AI / RAG**
- Groq API (`openai/gpt-oss-120b`) for generation, scoring, and email drafting
- LangChain4j for the embedding + retrieval pipeline
- `all-MiniLM-L6-v2` — local embedding model (no external calls, no per-embedding cost)
- In-memory vector store (`InMemoryEmbeddingStore`) — sufficient at this scale; see [Design decisions](#design-decisions-worth-knowing) below

**Data**
- MySQL

**Email**
- Spring Mail + Mailtrap (sandboxed — outreach emails are captured for review, never actually delivered to real inboxes)

---

## Project structure

```
sift-ai-recruiter/
├── backend/                  # Spring Boot API
│   ├── src/main/java/com/ex/ducking/
│   │   ├── controller/       # REST endpoints
│   │   ├── service/          # Business logic (LLM calls, embeddings, scoring, email)
│   │   ├── model/            # JPA entities
│   │   ├── repository/       # Spring Data repositories
│   │   └── config/           # Beans + startup embedding seeder
│   └── src/main/resources/
│       └── application.properties.example   # copy this → application.properties
└── frontend/
    ├── index.html            # landing page (loads by default)
    ├── app.html               # the working demo app
    ├── script.js
    └── style.css
```

---

## Running it locally

**Prerequisites:** JDK 21+, Maven, MySQL, a free [Groq](https://console.groq.com) API key, a free [Mailtrap](https://mailtrap.io) account (for the outreach feature).

1. **Clone the repo**
   ```bash
   git clone https://github.com/Ankit-0909/sift-ai-recruiter.git
   cd sift-ai-recruiter
   ```

2. **Create the database**
   ```sql
   CREATE DATABASE kynto_clone;
   ```

3. **Configure the backend**
   ```bash
   cd backend/src/main/resources
   cp application.properties.example application.properties
   ```
   Fill in your MySQL credentials, Groq API key, and Mailtrap SMTP credentials.

4. **Run the backend**
   ```bash
   cd ../../../..
   ./mvnw spring-boot:run
   ```
   On startup, candidate embeddings are generated automatically (see `DataInitializer`) — no manual step needed.

5. **Seed synthetic candidates** (first run only)
   ```
   POST http://localhost:8080/api/seed/candidates?count=10
   ```

6. **Open the frontend**
   Open `frontend/index.html` in a browser (or serve it with VS Code's Live Server). It loads the landing page first; click **"Try the live demo"** to reach the working app (`app.html`).

---

## What's real, what's simulated

Stated plainly rather than hidden — this is a portfolio project, not a production system.

**Actually working:**
- Live AI job description + key-skills generation
- Real semantic retrieval over locally embedded candidate profiles
- Explainable AI scoring — every score ships with a reason, not just a number
- Generated, per-candidate interview briefings
- AI-drafted outreach emails sent through a real (sandboxed) SMTP pipeline

**Simulated for this scope:**
- The candidate pool is synthetic, AI-generated data — not sourced from real people or licensed data providers
- No calendar sync or interview scheduling
- No live voice interview pipeline
- The vector store is in-memory — it resets on restart, and is rebuilt automatically on startup rather than persisted to disk

---

## Design decisions worth knowing

A few choices made deliberately, with the trade-off understood rather than accidental:

- **In-memory vector store instead of a dedicated vector DB.** At 10 candidates, re-embedding on startup costs a few seconds and adds zero infrastructure. At real scale, this would move to `pgvector` or a standalone store like Qdrant so embeddings persist and don't need recomputing.
- **Retrieval uses a distilled `keySkills` field, not the full job description.** Embedding the entire generated JD diluted the signal — generic phrases like "collaborate with stakeholders" show up in every posting regardless of role, so a separate, skills-only field is generated alongside the JD specifically for retrieval.
- **Scoring is upsert, not append.** Re-running "Score all candidates" updates existing score records for a given job–candidate pair instead of creating duplicates, so re-scoring is idempotent.
- **Outreach requires a manual click.** The system drafts everything — the shortlist, the email copy — but a human decides whether it actually goes out. No candidate is contacted without a person approving it.
- **Synthetic data over real sourcing.** Real candidate sourcing depends on licensed data providers (People Data Labs, ZoomInfo, etc.) — expensive and out of scope for a portfolio piece. The engineering focus here is the retrieval and scoring logic, not data acquisition.

---

## Roadmap / possible next steps

- Migrate the manual Groq REST calls to LangChain4j's AI Services for cleaner structured-output handling (the RAG pipeline already uses LangChain4j; the plain chat-completion calls currently don't)
- Persistent vector storage (`pgvector`) instead of in-memory
- Calendar integration for actual interview scheduling, separate from the current shortlist-and-email outreach step

---

Built by Ankit
