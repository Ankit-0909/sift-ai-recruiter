let currentGeneratedText = "";
let currentKeySkills = "";
const API_BASE = "https://sift-ai-recruiter.onrender.com";


async function generateJobDescription() {
    const idea = document.getElementById('ideaInput').value;
    const resultDiv = document.getElementById('result');

    if (!idea.trim()) {
        resultDiv.innerText = "Please write down the idea first.";
        return;
    }

    resultDiv.innerText = "Generating...";

    try {
        const response = await fetch(`${API_BASE}/api/generate/job-description`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idea: idea })
        });

        const data = await response.json();
        currentGeneratedText = data.description;
        currentKeySkills = data.keySkills;
        resultDiv.innerText = currentGeneratedText;

        document.getElementById('saveBtn').style.display = 'inline-block';
    } catch (error) {
        resultDiv.innerText = "Error: " + error.message;
    }
}

async function saveJobDescription() {
    const idea = document.getElementById('ideaInput').value;
    const statusDiv = document.getElementById('saveStatus');

    try {
        const response = await fetch(`${API_BASE}/api/job-descriptions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                title: idea,
                description: currentGeneratedText,
                requirements: "",
                keySkills: currentKeySkills
            })
        });

        if (response.ok) {
            statusDiv.innerText = "Saved successfully!";
            loadAllJobs();
            loadJobsForScoring()
            loadOutreachJobs();
        } else {
            statusDiv.innerText = "Save failed.";
        }
    } catch (error) {
        statusDiv.innerText = "Error: " + error.message;
    }
}

async function loadAllJobs() {
    const jobListDiv = document.getElementById('jobList');
    jobListDiv.innerHTML = "Loading...";

    try {
        const response = await fetch(`${API_BASE}/api/job-descriptions`);
        const jobs = await response.json();

        if (jobs.length === 0) {
            jobListDiv.innerHTML = "<p>No jobs saved yet.</p>";
            return;
        }

        jobListDiv.innerHTML = jobs.map(job => `
            <div class="job-card">
                <h3>${job.title}</h3>
                <p>${job.description}</p>
                <button onclick="deleteJob(${job.id})">Delete</button>
            </div>
        `).join('');

    } catch (error) {
        jobListDiv.innerHTML = "Error loading jobs: " + error.message;
    }
}

async function deleteJob(id) {
    try {
        const response = await fetch(`${API_BASE}/api/job-descriptions/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            loadAllJobs();
        } else {
            alert("Delete failed");
        }
    } catch (error) {
        alert("Error: " + error.message);
    }
}

async function loadJobsForScoring() {
    const response = await fetch(`${API_BASE}/api/job-descriptions`);
    const jobs = await response.json();
    const select = document.getElementById('jobSelect');
    select.innerHTML = jobs.map(job => `<option value="${job.id}">${job.title}</option>`).join('');
}

async function scoreAllCandidates() {
    const jobId = document.getElementById('jobSelect').value;
    const resultsDiv = document.getElementById('scoreResults');
    resultsDiv.innerHTML = "Scoring in progress... this process may take a few moments";

    try {
        const response = await fetch(`${API_BASE}/api/scoring/score-all/${jobId}`, { method: 'POST' });
        const data = await response.json();


        const ragNote = `
            <p class="rag-note">
                Showing ${data.candidatesScored} semantically relevant candidates
                (out of ${data.totalCandidatesInPool} total) — filtered using RAG retrieval.
            </p>
        `;
        resultsDiv.innerHTML = ragNote;

        loadRankedScores(jobId);
    } catch (error) {
        resultsDiv.innerHTML = "Error: " + error.message;
    }
}


async function loadRankedScores(jobId) {
    const response = await fetch(`${API_BASE}/api/scoring/job/${jobId}`);
    const scores = await response.json();
    const resultsDiv = document.getElementById('scoreResults');


    const existingNote = resultsDiv.querySelector('.rag-note');
    const noteHTML = existingNote ? existingNote.outerHTML : '';

    resultsDiv.innerHTML = noteHTML + scores.map(s => `
        <div class="score-card">
            <h3>${s.candidate.name} — Score: ${s.score}/100</h3>
            <p>${s.explanation}</p>
        </div>
    `).join('');
}

async function loadCandidatesForPrep() {
    const response = await fetch(`${API_BASE}/api/candidates`);
    const candidates = await response.json();
    const select = document.getElementById('candidateSelect');
    select.innerHTML = candidates.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
}


async function generateInterviewPrep() {
    const candidateId = document.getElementById('candidateSelect').value;
    const resultDiv = document.getElementById('prepResult');
    resultDiv.innerHTML = "Generating prep... please wait";

    try {
        const response = await fetch(`${API_BASE}/api/interview-prep/generate/${candidateId}`, {
            method: 'POST'
        });
        const prep = await response.json();

        resultDiv.innerHTML = `
            <div class="prep-card">
                <h3>Strengths</h3>
                <p>${prep.strengths}</p>
                <h3>Weaknesses / Areas to Probe</h3>
                <p>${prep.weaknesses}</p>
                <h3>Suggested Questions</h3>
                <p>${prep.suggestedQuestions}</p>
            </div>
        `;
    } catch (error) {
        resultDiv.innerHTML = "Error: " + error.message;
    }
}

async function loadOutreachJobs() {
    const response = await fetch(`${API_BASE}/api/job-descriptions`);
    const jobs = await response.json();
    const select = document.getElementById('outreachJobSelect');
    select.innerHTML = jobs.map(job => `<option value="${job.id}">${job.title}</option>`).join('');
}


async function loadShortlisted() {
    const jobId = document.getElementById('outreachJobSelect').value;
    const threshold = document.getElementById('thresholdInput').value;
    const resultsDiv = document.getElementById('shortlistResults');
    resultsDiv.innerHTML = "Loading...";

    try {
        const response = await fetch(`${API_BASE}/api/outreach/shortlisted/${jobId}?threshold=${threshold}`);
        const candidates = await response.json();

        if (candidates.length === 0) {
            resultsDiv.innerHTML = "<p>No candidates meet this threshold.</p>";
            return;
        }

        resultsDiv.innerHTML = candidates.map(c => `
            <div class="outreach-card" id="outreach-${c.id}">
                <h3>${c.candidate.name} — Score: ${c.score}/100</h3>
                <p>${c.explanation}</p>
                ${c.contacted
                    ? `<span class="badge-contacted">✓ Contacted on ${new Date(c.contactedAt).toLocaleDateString()}</span>`
                    : `<button onclick="sendOutreach(${c.id})">Send Outreach Email</button>`
                }
            </div>
        `).join('');

    } catch (error) {
        resultsDiv.innerHTML = "Error: " + error.message;
    }
}


async function sendOutreach(scoreId) {
    const cardDiv = document.getElementById(`outreach-${scoreId}`);
    const button = cardDiv.querySelector('button');
    button.disabled = true;
    button.innerText = "Sending...";

    try {
        const response = await fetch(`${API_BASE}/api/outreach/send/${scoreId}`, {
            method: 'POST'
        });

        if (response.ok) {
            const updated = await response.json();
            button.outerHTML = `<span class="badge-contacted">✓ Contacted just now</span>`;
        } else {
            button.disabled = false;
            button.innerText = "Send Outreach Email";
            alert("Failed to send — check console/backend logs.");
        }
    } catch (error) {
        button.disabled = false;
        button.innerText = "Send Outreach Email";
        alert("Error: " + error.message);
    }
}



window.onload = function() {
    loadAllJobs();
    loadJobsForScoring();
    loadCandidatesForPrep();
    loadOutreachJobs();
};