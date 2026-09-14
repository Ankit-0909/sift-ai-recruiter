let currentGeneratedText = "";
let currentKeySkills = "";

const API_BASE = "https://sift-ai-recruiter.onrender.com";

/*
const API_BASE = (window.location.hostname === "127.0.0.1" || window.location.hostname === "localhost")
    ? "http://localhost:8080"
    : "https://sift-ai-recruiter.onrender.com";
*/
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
    const mandatorySkills = document.getElementById('mandatorySkillsInput').value;
    const preferredSkills = document.getElementById('preferredSkillsInput').value;
    const minExperience = document.getElementById('minExpInput').value;

    try {
        const response = await fetch(`${API_BASE}/api/job-descriptions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                title: idea,
                description: currentGeneratedText,
                requirements: "",
                keySkills: currentKeySkills,
                mandatorySkills: mandatorySkills,
                preferredSkills: preferredSkills,
                minExperience: minExperience ? parseInt(minExperience) : null
            })
        });

        if (response.ok) {
            statusDiv.innerText = "Saved successfully!";
            loadAllJobs();
            loadJobsForScoring();
            loadOutreachJobs();
            loadJobsForPrep();
            loadJobsForHistory();
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
            loadJobsForScoring();
            loadJobsForPrep();
            loadOutreachJobs();
            loadJobsForHistory();
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

async function loadJobsForPrep() {
    const response = await fetch(`${API_BASE}/api/job-descriptions`);
    const jobs = await response.json();
    const select = document.getElementById('prepJobSelect');
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

        const scoresHTML = data.scores.map(s => `
            <div class="score-card">
                <h3>${s.candidate.name} — Score: ${s.score}/100</h3>
                <p>${s.explanation}</p>
            </div>
        `).join('');

        resultsDiv.innerHTML = ragNote + scoresHTML;
    } catch (error) {
        resultsDiv.innerHTML = "Error: " + error.message;
    }
}

function toggleCustomCriteriaPanel() {
    const panel = document.getElementById('customCriteriaPanel');
    panel.style.display = (panel.style.display === 'none') ? 'block' : 'none';
}

async function scoreWithCustomCriteria() {
    const jobId = document.getElementById('jobSelect').value;
    const resultsDiv = document.getElementById('scoreResults');
    resultsDiv.innerHTML = "Scoring with custom criteria... this process may take a few moments";

    const criteria = {
        mandatorySkills: document.getElementById('customMandatorySkills').value,
        preferredSkills: document.getElementById('customPreferredSkills').value,
        minExperience: document.getElementById('customMinExp').value ? parseInt(document.getElementById('customMinExp').value) : null,
        recruiterNotes: document.getElementById('customRecruiterNotes').value
    };

    try {
        const response = await fetch(`${API_BASE}/api/scoring/score-all-custom/${jobId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(criteria)
        });
        const data = await response.json();

        const note = `
            <p class="rag-note">
                Scored ${data.candidatesScored} candidates (out of ${data.totalCandidatesInPool} total)
                using your custom criteria — RAG filtering was skipped so your guidance could be applied to the full pool.
            </p>
        `;

        const scoresHTML = data.scores.map(s => `
            <div class="score-card">
                <h3>${s.candidate.name} — Score: ${s.score}/100</h3>
                <p>${s.explanation}</p>
            </div>
        `).join('');

        resultsDiv.innerHTML = note + scoresHTML;
    } catch (error) {
        resultsDiv.innerHTML = "Error: " + error.message;
    }
}

async function loadCandidatesForPrep() {
    const response = await fetch(`${API_BASE}/api/candidates`);
    const candidates = await response.json();
    const select = document.getElementById('candidateSelect');
    select.innerHTML = candidates.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
}

async function generateInterviewPrep() {
    const candidateId = document.getElementById('candidateSelect').value;
    const jobId = document.getElementById('prepJobSelect').value;
    const resultDiv = document.getElementById('prepResult');
    resultDiv.innerHTML = "Generating prep... please wait";

    try {
        const response = await fetch(`${API_BASE}/api/interview-prep/generate/${candidateId}?jobId=${jobId}`, {
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

async function loadCandidateProfiles() {
    const listDiv = document.getElementById('candidateList');
    listDiv.innerHTML = "Loading...";

    try {
        const response = await fetch(`${API_BASE}/api/candidates`);
        const candidates = await response.json();

        if (candidates.length === 0) {
            listDiv.innerHTML = "<p>No candidates in the pool yet.</p>";
            return;
        }

        listDiv.innerHTML = candidates.map(c => {
            const skills = c.skills.split(',').map(s => `<span class="skill-chip">${s.trim()}</span>`).join('');
            return `
                <div class="candidate-card">
                    <h3>${c.name}</h3>
                    <div class="candidate-meta">${c.experienceYears} years experience &middot; ${c.email}</div>
                    <div class="skill-chips">${skills}</div>
                    <p>${c.resumeSummary}</p>
                </div>
            `;
        }).join('');

    } catch (error) {
        listDiv.innerHTML = "Error loading candidates: " + error.message;
    }
}

async function loadJobsForHistory() {
    const response = await fetch(`${API_BASE}/api/job-descriptions`);
    const jobs = await response.json();
    const select = document.getElementById('historyJobSelect');
    select.innerHTML = jobs.map(job => `<option value="${job.id}">${job.title}</option>`).join('');
}

async function loadPreviousScores() {
    const jobId = document.getElementById('historyJobSelect').value;
    const resultsDiv = document.getElementById('historyResults');
    resultsDiv.innerHTML = "Loading saved scores...";

    try {
        const response = await fetch(`${API_BASE}/api/scoring/job/${jobId}`);
        const scores = await response.json();

        if (scores.length === 0) {
            resultsDiv.innerHTML = "<p>No scores saved yet for this job.</p>";
            return;
        }

        resultsDiv.innerHTML = scores.map(s => `
            <div class="score-card">
                <h3>${s.candidate.name} — Score: ${s.score}/100</h3>
                <p>${s.explanation}</p>
            </div>
        `).join('');

    } catch (error) {
        resultsDiv.innerHTML = "Error: " + error.message;
    }
}

async function addCandidateManually() {
    const name = document.getElementById('newCandName').value;
    const email = document.getElementById('newCandEmail').value;
    const experienceYears = document.getElementById('newCandExp').value;
    const description = document.getElementById('newCandDesc').value;
    const statusDiv = document.getElementById('addCandStatus');

    if (!name.trim() || !email.trim() || !description.trim()) {
        statusDiv.innerText = "Please fill in name, email, and description.";
        return;
    }

    statusDiv.innerText = "Adding candidate...";

    try {
        const response = await fetch(`${API_BASE}/api/candidates/add-from-text`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name: name,
                email: email,
                experienceYears: parseInt(experienceYears),
                description: description
            })
        });

        if (response.ok) {
            statusDiv.innerText = "Candidate added successfully!";
            document.getElementById('newCandName').value = '';
            document.getElementById('newCandEmail').value = '';
            document.getElementById('newCandExp').value = '';
            document.getElementById('newCandDesc').value = '';
            loadCandidateProfiles();
            loadCandidatesForPrep();
        } else {
            statusDiv.innerText = "Failed to add candidate.";
        }
    } catch (error) {
        statusDiv.innerText = "Error: " + error.message;
    }
}

window.onload = function() {
    loadAllJobs();
    loadJobsForScoring();
    loadJobsForPrep();
    loadCandidatesForPrep();
    loadOutreachJobs();
    loadCandidateProfiles();
    loadJobsForHistory();
};