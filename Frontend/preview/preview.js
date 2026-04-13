document.addEventListener("DOMContentLoaded", async () => {
  const index = localStorage.getItem("editingIndex");
  const projects = JSON.parse(localStorage.getItem("myProjects")) || [];
  const container = document.getElementById("preview_content");
  const titleElement = document.getElementById("preview-title");
  const currentProjectId = projects[index]?.id || 1;

  if (index !== null && projects[index]) {
    titleElement.innerText = `SRS Preview: ${projects[index].name}`;

    try {
      const [templateRes, dataRes] = await Promise.all([
        fetch("http://localhost:8080/api/get-template"),
        fetch(
          `http://localhost:8080/api/get-srs?project_id=${currentProjectId}`,
        ),
      ]);

      if (!templateRes.ok || !dataRes.ok) throw new Error("Server Error");

      const template = await templateRes.json();
      const savedText = await dataRes.text();
      let savedData =
        savedText && savedText.trim().startsWith("{")
          ? JSON.parse(savedText)
          : {};

      renderFullDocument(savedData, template, projects[index]);
    } catch (e) {
      console.error("Preview Error:", e);
      container.innerHTML = `<div class="error-msg">⚠️ Failed to connect to Sovereign Engine.</div>`;
    }
  }

  function renderFullDocument(content, template, projectInfo) {
    container.innerHTML = "";

    const authors = content["meta.authors"]
      ? content["meta.authors"].split(",").map((a) => a.trim())
      : [projectInfo.author || "Developer"];
    const org = content["meta.org"] || "Sovereign Engine Org";
    const version = content["meta.version"] || "1.0 Approved";
    const date = content["meta.date"] || new Date().toLocaleDateString();

    const cover = document.createElement("div");
    cover.className = "cover-page";
    cover.innerHTML = `
      <div class="cover-header">Software Requirements Specification</div>
      <div class="cover-sub">for</div>
      <div class="cover-project">${projectInfo.name}</div>
      <div class="cover-version">Version ${version}</div>
      <div class="cover-footer">
        <p>Prepared by: <strong>${authors.join(", ")}</strong></p>
        <p>Organization: <strong>${org}</strong></p>
        <p>Date: ${date}</p>
      </div>
    `;
    container.appendChild(cover);

    const tocPage = document.createElement("div");
    tocPage.className = "toc-page";
    tocPage.innerHTML = `<h2>Table of Contents</h2>`;
    const tocList = document.createElement("ul");
    tocList.className = "toc-list";

    for (let key in template.sections) {
      const section = template.sections[key];
      if (key === "appendices") continue;

      const li = document.createElement("li");
      li.innerHTML = `<span>${section.title}</span><span class="toc-dots"></span>`;
      tocList.appendChild(li);

      for (let subKey in section) {
        if (section[subKey].label) {
          const subLi = document.createElement("li");
          subLi.className = "toc-sub";
          subLi.innerHTML = `<span>${section[subKey].label}</span><span class="toc-dots"></span>`;
          tocList.appendChild(subLi);
        }
      }
    }
    tocPage.appendChild(tocList);
    container.appendChild(tocPage);

    const revPage = document.createElement("div");
    revPage.className = "revision-page";

    let revRowsHtml = "";
    const revCount = Object.keys(content).filter((k) =>
      k.startsWith("rev.name."),
    ).length;

    if (revCount > 0) {
      for (let i = 0; i < revCount; i++) {
        revRowsHtml += `
          <tr>
            <td>${content[`rev.name.${i}`] || ""}</td>
            <td>${content[`rev.date.${i}`] || ""}</td>
            <td>${content[`rev.reason.${i}`] || ""}</td>
            <td>${content[`rev.ver.${i}`] || ""}</td>
          </tr>`;
      }
    } else {
      revRowsHtml = `<tr><td colspan="4" style="text-align:center;">No revisions recorded.</td></tr>`;
    }

    revPage.innerHTML = `
      <h2>Revision History</h2>
      <table class="rev-table">
        <thead><tr><th>Name/Author</th><th>Date</th><th>Reason</th><th>Version</th></tr></thead>
        <tbody>${revRowsHtml}</tbody>
      </table>
    `;
    container.appendChild(revPage);

    renderContent(content);
  }

  function renderContent(content) {
    const sections = {};
    Object.keys(content)
      .sort()
      .forEach((key) => {
        if (key.startsWith("meta.") || key.startsWith("rev.")) return;

        const parts = key.split(".");
        const sectionKey = parts[0];
        const fieldLabel = parts.slice(1).join(" ");

        if (!sections[sectionKey]) sections[sectionKey] = [];

        if (fieldLabel.includes("feature_")) {
          const featureMatch = fieldLabel.match(/feature_(\d+)_(\w+)/);
          if (featureMatch) {
            const idx = featureMatch[1];
            const type = featureMatch[2];
            let group = sections[sectionKey].find(
              (f) => f.isFeature && f.index === idx,
            );
            if (!group) {
              group = { isFeature: true, index: idx, data: {} };
              sections[sectionKey].push(group);
            }
            group.data[type] = content[key];
            return;
          }
        }
        sections[sectionKey].push({
          label: fieldLabel || "Content",
          text: content[key],
        });
      });

    for (const [name, fields] of Object.entries(sections)) {
      const sectionDiv = document.createElement("div");
      sectionDiv.className = "doc-section";
      sectionDiv.innerHTML = `<h2>${format(name)}</h2>`;

      fields.forEach((f) => {
        if (f.isFeature) {
          const featDiv = document.createElement("div");
          featDiv.className = "feature-preview-block";
          featDiv.innerHTML = `
            <h3>Feature ${parseInt(f.index) + 1}: ${format(f.data.desc || "Description")}</h3>
            <p class="doc-text"><strong>Stimulus:</strong> ${f.data.stimulus || "N/A"}</p>
            <p class="doc-text"><strong>Functional:</strong> ${f.data.functional || "N/A"}</p>
          `;
          sectionDiv.appendChild(featDiv);
        } else {
          const h3 = document.createElement("h3");
          h3.innerText = format(f.label);
          const p = document.createElement("p");
          p.className = "doc-text";
          p.innerText = f.text || "Not specified.";
          sectionDiv.appendChild(h3);
          sectionDiv.appendChild(p);
        }
      });
      container.appendChild(sectionDiv);
    }
  }

  function format(str) {
    if (!str) return "";
    return str
      .replace(/_/g, " ")
      .replace(/\b\w/g, (l) => l.toUpperCase())
      .replace(/(\d+)\s(\d+)/g, "$1.$2");
  }
});

function downloadAsDoc() {
  const content = document.getElementById("preview_content").innerHTML;
  const style = `
    <style>
      body { font-family: 'Times New Roman', serif; padding: 1in; }
      h2 { color: black; border-bottom: 2px solid black; text-transform: uppercase; margin-top: 25px; font-size: 16pt; }
      h3 { font-style: italic; color: #333; margin-top: 15px; font-size: 13pt; }
      .doc-text { margin-bottom: 12px; text-align: justify; line-height: 1.5; }
      .cover-page { text-align: center; margin-bottom: 2in; padding-top: 2in; page-break-after: always; }
      .cover-project { font-size: 28pt; font-weight: bold; text-decoration: underline; margin-bottom: 0.5in; }
      .toc-page, .revision-page { page-break-after: always; }
      .toc-list { list-style: none; padding: 0; }
      .rev-table { width: 100%; border-collapse: collapse; margin-bottom: 0.5in; }
      .rev-table th, .rev-table td { border: 1px solid black; padding: 8px; text-align: left; }
    </style>
  `;

  const html = `
    <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
      <head><meta charset="utf-8">${style}</head>
      <body>${content}</body>
    </html>
  `;

  const blob = new Blob(["\ufeff", html], { type: "application/msword" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "SRS_Document.doc";
  link.click();
  URL.revokeObjectURL(url);
}
