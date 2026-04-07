document.addEventListener("DOMContentLoaded", async () => {
  const status = document.getElementById("save-status");
  const form = document.getElementById("srs-form");
  const titleElement = document.getElementById("srs-title");

  // Use localStorage to maintain session context for which project is being edited
  const index = localStorage.getItem("editingIndex");
  const projects = JSON.parse(localStorage.getItem("myProjects")) || [];

  if (index !== null && projects[index]) {
    titleElement.innerText = `Edit Software Requirements Specification for ${projects[index].name}`;
  }

  try {
    const response = await fetch("http://localhost:8080/api/get-template");
    const template = await response.json();
    renderForm(template.sections);
  } catch (err) {
    status.innerText = "Error loading template.";
  }

  function renderForm(sections) {
    form.innerHTML = "";

    for (let sectionKey in sections) {
      const sectionData = sections[sectionKey];

      // Handle the "appendices" container to prevent "undefined" headers
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

      // Formatting the section title
      const displayTitle = data.title || key.replace(/_/g, " ").toUpperCase();
      sectionDiv.innerHTML = `<h2>${displayTitle}</h2>`;

      // Main section instruction
      if (data.instruction) {
        const instr = document.createElement("p");
        instr.style.fontStyle = "italic";
        instr.style.color = "#555";
        instr.style.fontSize = "0.9em";
        instr.innerHTML = `&lt;${data.instruction}&gt;`;
        sectionDiv.appendChild(instr);
      }

      // Case 1: Standard content sections (like Section 6 or Appendices)
      if (data.hasOwnProperty("content")) {
        const div = document.createElement("div");
        div.innerHTML = `
            <textarea name="${key}.content" placeholder="Enter content here...">${data.content || ""}</textarea>
            <br><br>
        `;
        sectionDiv.appendChild(div);
      }
      // Case 2: Nested fields (1.1, 1.2, etc.)
      else {
        for (let subKey in data) {
          if (["title", "instruction", "features"].includes(subKey)) continue;

          const field = data[subKey];
          if (field && typeof field === "object") {
            const div = document.createElement("div");
            div.innerHTML = `
                <label><strong>${field.label || subKey}</strong></label><br>
                <small style="color: #666; display: block; margin-bottom: 5px;">${field.instruction || ""}</small>
                <textarea name="${key}.${subKey}">${field.content || ""}</textarea>
                <br><br>
            `;
            sectionDiv.appendChild(div);
          }
        }
      }

      // Case 3: System Features (Section 3) formatted per your template
      if (data.features && Array.isArray(data.features)) {
        data.features.forEach((feat, i) => {
          const featDiv = document.createElement("div");
          featDiv.className = "feature-block";
          featDiv.style.borderLeft = "4px solid #007bff";
          featDiv.style.paddingLeft = "15px";
          featDiv.style.marginTop = "20px";

          featDiv.innerHTML = `
            <h3>System Feature ${i + 1}: ${feat.feature_name}</h3>
            <p style="font-style: italic; font-size: 0.85em; color: #666;">
              &lt;Don’t really say “System Feature ${i + 1}.” State the feature name in just a few words.&gt;
            </p>

            <div class="sub-feature">
              <label><strong>3.1.${i + 1}.1 Description and Priority</strong></label>
              <textarea name="${key}.feature_${i}_desc" 
                placeholder="Provide a short description of the feature and indicate whether it is of High, Medium, or Low priority...">${feat["3.1.1_priority"] || ""}</textarea>
            </div>

            <div class="sub-feature">
              <label><strong>3.1.${i + 1}.2 Stimulus/Response Sequences</strong></label>
              <textarea name="${key}.feature_${i}_stimulus" 
                placeholder="List the sequences of user actions and system responses that stimulate the behavior defined for this feature...">${feat["3.1.2_stimulus"] || ""}</textarea>
            </div>

            <div class="sub-feature">
              <label><strong>3.1.${i + 1}.3 Functional Requirements</strong></label>
              <textarea name="${key}.feature_${i}_functional" 
                placeholder="REQ-1: \nREQ-2: \n\nItemize the detailed functional requirements associated with this feature...">${feat["3.1.3_functional_reqs"] || ""}</textarea>
            </div>
          `;
          sectionDiv.appendChild(featDiv);
        });
      }

      form.appendChild(sectionDiv);
    }

    // Append Final Save Button
    const saveBtn = document.createElement("button");
    saveBtn.type = "submit";
    saveBtn.id = "final-save-btn";
    saveBtn.innerText = "Save Progress";
    form.appendChild(saveBtn);

    status.innerText = "Template Loaded. Ready to edit. ✨";
  }

  // Auto-save logic with debounce
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
            project_id: projects[index]?.id || 1,
            content_json: content,
          }),
        });

        if (res.ok) status.innerText = "All changes saved.";
        else status.innerText = "Save failed.";
      } catch (e) {
        status.innerText = "Server unreachable.";
      }
    }, 1500);
  });

  form.onsubmit = (e) => e.preventDefault();
});
