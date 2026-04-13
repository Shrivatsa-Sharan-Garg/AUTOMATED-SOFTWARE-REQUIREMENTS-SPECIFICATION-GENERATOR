document.addEventListener("DOMContentLoaded", async () => {
  const status = document.getElementById("save-status");
  const form = document.getElementById("srs-form");
  const titleElement = document.getElementById("srs-title");

  const index = localStorage.getItem("editingIndex");
  const projects = JSON.parse(localStorage.getItem("myProjects")) || [];
  const currentProjectId = projects[index]?.id || 1;

  if (index !== null && projects[index]) {
    titleElement.innerText = `Edit SRS for ${projects[index].name}`;
  }

  try {
    const [templateRes, savedDataRes] = await Promise.all([
      fetch("http://localhost:8080/api/get-template"),
      fetch(`http://localhost:8080/api/get-srs?project_id=${currentProjectId}`),
    ]);

    if (!templateRes.ok || !savedDataRes.ok) {
      throw new Error(
        `Server Error: Template(${templateRes.status}) Data(${savedDataRes.status})`,
      );
    }

    const template = await templateRes.json();
    let savedData = {};
    const savedText = await savedDataRes.text();
    if (savedText && savedText.trim().startsWith("{")) {
      savedData = JSON.parse(savedText);
    }

    renderForm(template.sections, savedData);
  } catch (err) {
    status.innerText = "⚠️ Error loading document data. Check Server.";
    console.error("Initialization Error:", err);
  }

  function renderForm(sections, savedData) {
    form.innerHTML = "";
    const getVal = (key, def = "") =>
      savedData && savedData[key] !== undefined ? savedData[key] : def;

    const metaDiv = document.createElement("div");
    metaDiv.className = "section metadata-section";
    metaDiv.innerHTML = `
      <h2>Document Metadata</h2>
      <div class="meta-grid">
        <label>Organization: <input type="text" name="meta.org" value="${getVal("meta.org")}"></label>
        <label>Version: <input type="text" name="meta.version" value="${getVal("meta.version", "1.0 Approved")}"></label>
        <label>Authors (Comma separated): <input type="text" name="meta.authors" value="${getVal("meta.authors")}" placeholder="Name 1, Name 2"></label>
        <label>Release Date: <input type="date" name="meta.date" value="${getVal("meta.date")}"></label>
      </div>
    `;
    form.appendChild(metaDiv);

    const revDiv = document.createElement("div");
    revDiv.className = "section revision-section";
    revDiv.innerHTML = `
      <h2>Revision History</h2>
      <table id="rev-table" style="width:100%; border-collapse: collapse;">
        <thead><tr><th>Name/Author</th><th>Date</th><th>Reason</th><th>Version</th><th></th></tr></thead>
        <tbody id="rev-body"></tbody>
      </table>
      <button type="button" id="add-rev-row" style="margin-top:10px; cursor:pointer;">+ Add Revision</button>
    `;
    form.appendChild(revDiv);

    const addRevRow = (idx, data = {}) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td><input type="text" name="rev.name.${idx}" value="${data.name || ""}"></td>
        <td><input type="date" name="rev.date.${idx}" value="${data.date || ""}"></td>
        <td><input type="text" name="rev.reason.${idx}" value="${data.reason || ""}"></td>
        <td><input type="text" name="rev.ver.${idx}" value="${data.ver || ""}" style="width:60px;"></td>
        <td><button type="button" class="del-row" style="color:red; border:none; background:none; cursor:pointer;">✖</button></td>
      `;
      tr.querySelector(".del-row").onclick = () => {
        tr.remove();
        triggerAutoSave();
      };
      document.getElementById("rev-body").appendChild(tr);
    };

    let revCount = Object.keys(savedData).filter((k) =>
      k.startsWith("rev.name."),
    ).length;
    if (revCount > 0) {
      for (let i = 0; i < revCount; i++)
        addRevRow(i, {
          name: savedData[`rev.name.${i}`],
          date: savedData[`rev.date.${i}`],
          reason: savedData[`rev.reason.${i}`],
          ver: savedData[`rev.ver.${i}`],
        });
    } else {
      addRevRow(0);
    }
    document.getElementById("add-rev-row").onclick = () =>
      addRevRow(document.getElementById("rev-body").children.length);

    for (let sectionKey in sections) {
      if (sectionKey === "appendices") {
        for (let appKey in sections[sectionKey])
          createSectionUI(appKey, sections[sectionKey][appKey]);
      } else {
        createSectionUI(sectionKey, sections[sectionKey]);
      }
    }

    function createSectionUI(key, data) {
      const sectionDiv = document.createElement("div");
      sectionDiv.className = "section";
      sectionDiv.id = `section-${key}`;
      sectionDiv.innerHTML = `<h2>${data.title || key.replace(/_/g, " ").toUpperCase()}</h2>`;

      if (data.instruction) {
        sectionDiv.innerHTML += `<p class="instruction-text">&lt;${data.instruction}&gt;</p>`;
      }

      if (data.hasOwnProperty("content")) {
        const fieldName = `${key}.content`;
        sectionDiv.innerHTML += `<textarea name="${fieldName}">${getVal(fieldName, data.content)}</textarea><br><br>`;
      } else {
        for (let subKey in data) {
          if (["title", "instruction", "features"].includes(subKey)) continue;
          const field = data[subKey];
          if (field && typeof field === "object") {
            const fName = `${key}.${subKey}`;
            sectionDiv.innerHTML += `<label><strong>${field.label || subKey}</strong></label><br>
                <textarea name="${fName}">${getVal(fName, field.content)}</textarea><br><br>`;
          }
        }
      }

      if (key === "3_functional_requirements" || data.features) {
        const featContainer = document.createElement("div");
        featContainer.className = "features-container";
        sectionDiv.appendChild(featContainer);

        const addFeatUI = (idx, featData = {}) => {
          const dK = `${key}.feature_${idx}_desc`,
            sK = `${key}.feature_${idx}_stimulus`,
            fK = `${key}.feature_${idx}_functional`;
          const featDiv = document.createElement("div");
          featDiv.className = "feature-block";
          featDiv.style =
            "border: 1px solid #ccc; padding: 15px; margin-bottom: 10px; position: relative;";
          featDiv.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:center;">
                <h3>System Feature ${idx + 1}</h3>
                <button type="button" class="del-feat" style="color:red; border:none; background:none; cursor:pointer;">Remove Feature ✖</button>
            </div>
            <label>Description/Priority</label>
            <textarea name="${dK}" placeholder="Enter feature name and priority">${getVal(dK, featData.desc || "")}</textarea>
            <label>Stimulus/Response</label>
            <textarea name="${sK}" placeholder="How does system respond to trigger?">${getVal(sK, featData.stimulus || "")}</textarea>
            <label>Functional Requirements</label>
            <textarea name="${fK}" placeholder="Specific functional details">${getVal(fK, featData.functional || "")}</textarea>
          `;
          featDiv.querySelector(".del-feat").onclick = () => {
            featDiv.remove();
            triggerAutoSave();
          };
          featContainer.appendChild(featDiv);
        };

        let featCount = Object.keys(savedData).filter(
          (k) => k.startsWith(`${key}.feature_`) && k.endsWith("_desc"),
        ).length;

        if (featCount > 0) {
          for (let i = 0; i < featCount; i++) {
            addFeatUI(i, {
              desc: savedData[`${key}.feature_${i}_desc`],
              stimulus: savedData[`${key}.feature_${i}_stimulus`],
              functional: savedData[`${key}.feature_${i}_functional`],
            });
          }
        } else if (data.features) {
          data.features.forEach((f, i) =>
            addFeatUI(i, {
              desc: f["3.1.1_priority"],
              stimulus: f["3.1.2_stimulus"],
              functional: f["3.1.3_functional_reqs"],
            }),
          );
        }

        const addFeatBtn = document.createElement("button");
        addFeatBtn.type = "button";
        addFeatBtn.innerText = "+ Add System Feature";
        addFeatBtn.style =
          "background: #28a745; color: white; padding: 10px; margin-top: 10px; cursor:pointer;";
        addFeatBtn.onclick = () => addFeatUI(featContainer.children.length);
        sectionDiv.appendChild(addFeatBtn);
      }

      form.appendChild(sectionDiv);
    }

    const saveBtn = document.createElement("button");
    saveBtn.type = "submit";
    saveBtn.innerText = "Save Now";
    form.appendChild(saveBtn);
  }

  let timeout = null;
  const triggerAutoSave = () => {
    status.innerText = "Typing...";
    clearTimeout(timeout);
    timeout = setTimeout(async () => {
      status.innerText = "Saving... 💾";
      const formData = new FormData(form);
      const content = {};
      formData.forEach((value, key) => {
        content[key] = value;
      });

      try {
        const res = await fetch("http://localhost:8080/api/save-srs", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            project_id: currentProjectId,
            content_json: content,
          }),
        });
        status.innerText = res.ok ? "All changes saved." : "Save failed.";
      } catch (e) {
        status.innerText = "Server unreachable.";
      }
    }, 1500);
  };

  form.addEventListener("input", triggerAutoSave);
  form.onsubmit = (e) => e.preventDefault();
});
