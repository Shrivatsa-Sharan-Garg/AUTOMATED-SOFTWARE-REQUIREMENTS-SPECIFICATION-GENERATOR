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

    const getSavedValue = (fieldName, defaultValue = "") => {
      return savedData && savedData[fieldName] !== undefined
        ? savedData[fieldName]
        : defaultValue;
    };

    for (let sectionKey in sections) {
      const sectionData = sections[sectionKey];
      if (sectionKey === "appendices") {
        for (let appKey in sectionData) {
          createSectionUI(appKey, sectionData[appKey]);
        }
      } else {
        createSectionUI(sectionKey, sectionData);
      }
    }

    function createSectionUI(key, data) {
      const sectionDiv = document.createElement("div");
      sectionDiv.className = "section";
      const displayTitle = data.title || key.replace(/_/g, " ").toUpperCase();
      sectionDiv.innerHTML = `<h2>${displayTitle}</h2>`;

      if (data.instruction) {
        const instr = document.createElement("p");
        instr.className = "instruction-text";
        instr.innerHTML = `&lt;${data.instruction}&gt;`;
        sectionDiv.appendChild(instr);
      }

      if (data.hasOwnProperty("content")) {
        const fieldName = `${key}.content`;
        const div = document.createElement("div");
        div.innerHTML = `<textarea name="${fieldName}">${getSavedValue(fieldName, data.content)}</textarea><br><br>`;
        sectionDiv.appendChild(div);
      } else {
        for (let subKey in data) {
          if (["title", "instruction", "features"].includes(subKey)) continue;
          const field = data[subKey];
          if (field && typeof field === "object") {
            const fieldName = `${key}.${subKey}`;
            const div = document.createElement("div");
            div.innerHTML = `
                <label><strong>${field.label || subKey}</strong></label><br>
                <textarea name="${fieldName}">${getSavedValue(fieldName, field.content)}</textarea><br><br>
            `;
            sectionDiv.appendChild(div);
          }
        }
      }

      if (data.features && Array.isArray(data.features)) {
        data.features.forEach((feat, i) => {
          const featDiv = document.createElement("div");
          featDiv.className = "feature-block";
          const dK = `${key}.feature_${i}_desc`,
            sK = `${key}.feature_${i}_stimulus`,
            fK = `${key}.feature_${i}_functional`;

          featDiv.innerHTML = `
            <h3>Feature: ${feat.feature_name}</h3>
            <textarea name="${dK}">${getSavedValue(dK, feat["3.1.1_priority"])}</textarea>
            <textarea name="${sK}">${getSavedValue(sK, feat["3.1.2_stimulus"])}</textarea>
            <textarea name="${fK}">${getSavedValue(fK, feat["3.1.3_functional_reqs"])}</textarea>
          `;
          sectionDiv.appendChild(featDiv);
        });
      }
      form.appendChild(sectionDiv);
    }

    const saveBtn = document.createElement("button");
    saveBtn.type = "submit";
    saveBtn.innerText = "Final Save";
    form.appendChild(saveBtn);
    status.innerText = "Ready. ✨";
  }

  let timeout = null;
  form.addEventListener("input", () => {
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
  });

  form.onsubmit = (e) => e.preventDefault();
});
